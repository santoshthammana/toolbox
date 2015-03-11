package eu.amidst.core.exponentialfamily;

import eu.amidst.core.utils.ArrayVector;
import eu.amidst.core.utils.Utils;
import eu.amidst.core.utils.Vector;
import eu.amidst.core.variables.Variable;
import org.apache.commons.math3.special.Gamma;

import java.util.Random;

/**
 * Created by ana@cs.aau.dk on 23/02/15.
 */
public class EF_Gamma extends EF_UnivariateDistribution {

    public static final int LOGX = 0;
    public static final int INVX = 1;
    public static final double DELTA = 0.0001;

    public EF_Gamma(Variable var1) {

        if (!var1.isGammaParameter())
            throw new IllegalArgumentException("The variable is not Gamma parameter");

        this.var = var1;
        this.naturalParameters = this.createZeroedNaturalParameters();
        this.momentParameters = this.createZeroedMomentParameters();
        double alpha = 1;
        double beta = 1;
        this.naturalParameters.set(0, alpha -1 );
        this.naturalParameters.set(1, -beta);
        this.setNaturalParameters(naturalParameters);
    }

    @Override
    public double computeLogBaseMeasure(double val) {
        return 0;
    }

    @Override
    public SufficientStatistics getSufficientStatistics(double val) {
        SufficientStatistics vec = this.createZeroedSufficientStatistics();
        vec.set(LOGX, Math.log(val));
        vec.set(INVX, val);
        return vec;
    }

    @Override
    public EF_UnivariateDistribution deepCopy() {
        EF_Gamma copy = new EF_Gamma(this.getVariable());
        copy.getNaturalParameters().copy(this.getNaturalParameters());
        copy.getMomentParameters().copy(this.getMomentParameters());

        return copy;
    }

    @Override
    public EF_UnivariateDistribution randomInitialization(Random random) {

        double randomVar = random.nextDouble()*10 + 0.01;

        double alpha = 1;
        double beta = 1;//randomVar ;

        this.getNaturalParameters().set(0, alpha - 1);
        this.getNaturalParameters().set(1, -beta);

        this.updateMomentFromNaturalParameters();

        return this;
    }

    @Override
    public void updateNaturalFromMomentParameters() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public void updateMomentFromNaturalParameters() {
        double alpha = this.naturalParameters.get(0) + 1;
        double beta = -this.naturalParameters.get(1);
        this.momentParameters.set(0, Gamma.digamma(alpha) - Math.log(beta));
        this.momentParameters.set(1, alpha / beta);
    }

    @Override
    public int sizeOfSufficientStatistics() {
        return 2;
    }

    @Override
    public double computeLogNormalizer() {
        double alpha = this.naturalParameters.get(0) + 1;
        double beta = -this.naturalParameters.get(1);
        return Gamma.logGamma(alpha) - alpha * Math.log(beta);
    }

    @Override
    public Vector createZeroedVector() {
        return new ArrayVector(2);
    }


    @Override
    public Vector getExpectedParameters() {

        Vector vec = new ArrayVector(1);
        vec.set(0, -(this.naturalParameters.get(0)+1)/this.naturalParameters.get(1));
        return vec;
    }
}