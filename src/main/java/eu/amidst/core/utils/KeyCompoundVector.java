package eu.amidst.core.utils;

import eu.amidst.core.exponentialfamily.MomentParameters;
import eu.amidst.core.exponentialfamily.NaturalParameters;
import eu.amidst.core.exponentialfamily.SufficientStatistics;
import org.apache.commons.collections.map.HashedMap;

import java.util.List;
import java.util.Map;

/**
 * Created by andresmasegosa on 09/03/15.
 */

public class KeyCompoundVector<E> implements SufficientStatistics, MomentParameters, NaturalParameters {

    int size;
    Map<E,IndexedVector<E>> baseVectors;

    public KeyCompoundVector() {
        baseVectors = new HashedMap();
        this.size=0;
    }

    public void addVector(E key, Vector vector){
        this.baseVectors.put(key,new IndexedVector<E>(key, vector));
        this.size+=vector.size();
    }

    public void setVectorByPosition(E key, Vector vec) {
        baseVectors.get(key).setVector(vec);
    }

    public Vector getVectorByPosition(E key) {
        return this.baseVectors.get(key).getVector();
    }

    @Override
    public double get(int i) {
        int total = 0;
        for (int j = 0; j < this.baseVectors.size(); j++) {
            if (i < (total + this.baseVectors.get(j).getVector().size())) {
                return this.baseVectors.get(j).getVector().get(i - total);
            } else {
                total += this.baseVectors.get(j).getVector().size();
            }
        }
        return Double.NaN;
    }

    @Override
    public void set(int i, double val) {
        int total = 0;
        for (int j = 0; j < this.baseVectors.size(); j++) {
            if (i < (total + this.baseVectors.get(j).getVector().size())) {
                this.baseVectors.get(j).getVector().set(i - total, val);
            } else {
                total += this.baseVectors.get(j).getVector().size();
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void sum(Vector vector) {
        this.sum((CompoundVector) vector);
    }

    @Override
    public void copy(Vector vector) {
        this.copy((CompoundVector) vector);
    }

    @Override
    public void divideBy(double val) {
        this.baseVectors.values().stream().forEach(w -> w.getVector().divideBy(val));
    }

    @Override
    public double dotProduct(Vector vec) {
        return this.dotProduct((CompoundVector) vec);
    }

    public double dotProduct(KeyCompoundVector vec) {
        return this.baseVectors.values().stream().mapToDouble(w -> w.getVector().dotProduct(vec.getVectorByPosition(w.getIndex()))).sum();
    }

    public void copy(KeyCompoundVector vector) {
        if (vector.size() != this.size())
            throw new IllegalArgumentException("Error in variable Vector. Method copy. The parameter vec has a different size. ");

        this.baseVectors.values().stream().forEach(w -> w.getVector().copy(vector.getVectorByPosition(w.getIndex())));

    }

    public void sum(KeyCompoundVector vector) {
        this.baseVectors.values().stream().forEach(w -> w.getVector().sum(vector.getVectorByPosition(w.getIndex())));
    }


    static class IndexedVector<E> {
        Vector vector;
        E index;

        IndexedVector(E index1, Vector vec1) {
            this.vector = vec1;
            this.index = index1;
        }

        public Vector getVector() {
            return vector;
        }

        public E getIndex() {
            return index;
        }

        public void setVector(Vector vector) {
            this.vector = vector;
        }
    }

    @FunctionalInterface
    public interface VectorBuilder {
        public Vector createZeroedVector();
    }

}