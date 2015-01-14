package eu.amidst.core.learning;

import eu.amidst.core.database.DataBase;
import eu.amidst.core.database.filereaders.DynamicDataOnDiskFromFile;
import eu.amidst.core.database.filereaders.arffFileReader.ARFFDataReader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.DAG;
import eu.amidst.core.models.DynamicBayesianNetwork;
import eu.amidst.core.models.DynamicDAG;
import eu.amidst.core.utils.BayesianNetworkGenerator;
import eu.amidst.core.utils.BayesianNetworkSampler;
import eu.amidst.core.variables.DynamicVariables;
import eu.amidst.core.variables.StaticVariables;
import eu.amidst.core.variables.Variable;

import java.io.IOException;
import java.util.Random;

/**
 * Created by andresmasegosa on 06/01/15.
 */
public class DynamicNaiveBayesClassifier {

    int classVarID;
    DynamicBayesianNetwork bnModel;
    boolean parallelMode = true;

    public boolean isParallelMode() {
        return parallelMode;
    }

    public void setParallelMode(boolean parallelMode) {
        this.parallelMode = parallelMode;
    }

    public int getClassVarID() {
        return classVarID;
    }

    public void setClassVarID(int classVarID) {
        this.classVarID = classVarID;
    }

    public DynamicBayesianNetwork getDynamicBNModel() {
        return bnModel;
    }

    private DynamicDAG dynamicNaiveBayesStructure(DataBase dataBase){

        DynamicVariables modelHeader = new DynamicVariables(dataBase.getAttributes());
        Variable classVar = modelHeader.getVariableById(this.getClassVarID());
        DynamicDAG dag = new DynamicDAG(modelHeader);

        if (parallelMode){
            dag.getParentSetsTime0().parallelStream()
                    .filter(w -> w.getMainVar().getVarID() != classVar.getVarID())
                    .forEach(w -> w.addParent(classVar));

            dag.getParentSetsTimeT().parallelStream()
                    .filter(w -> w.getMainVar().getVarID() != classVar.getVarID())
                    .forEach(w -> {
                        w.addParent(classVar);
                        w.addParent(modelHeader.getTemporalClone(w.getMainVar()));
                    });
        }else {
            dag.getParentSetsTime0().stream()
                    .filter(w -> w.getMainVar().getVarID() != classVar.getVarID())
                    .forEach(w -> w.addParent(classVar));

            dag.getParentSetsTimeT().stream()
                    .filter(w -> w.getMainVar().getVarID() != classVar.getVarID())
                    .forEach(w -> {
                        w.addParent(classVar);
                        w.addParent(modelHeader.getTemporalClone(w.getMainVar()));
                    });
        }

        dag.getParentSetTimeT(classVar).addParent(modelHeader.getTemporalClone(classVar));

        return dag;
    }

    public void learn(DataBase dataBase){
        LearningEngine.setDynamicStructuralLearningAlgorithm(this::dynamicNaiveBayesStructure);
        MaximumLikelihood.setParallelMode(this.isParallelMode());
        LearningEngine.setDynamicParameterLearningAlgorithm(MaximumLikelihood::learnDynamic);
        bnModel = LearningEngine.learnDynamicModel(dataBase);
    }

    public static void main(String[] args) throws IOException {

        BayesianNetworkGenerator.setNumberOfContinuousVars(0);
        BayesianNetworkGenerator.setNumberOfDiscreteVars(5);
        BayesianNetworkGenerator.setNumberOfStates(3);
        BayesianNetwork bn = BayesianNetworkGenerator.generateNaiveBayes(new Random(0));

        int sampleSize = 10000;
        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setParallelMode(false);
        String file = "./datasets/randomdata.arff";
        sampler.sampleToAnARFFFile(file,sampleSize);

        DataBase data = new DynamicDataOnDiskFromFile(new ARFFDataReader(file));

        for (int i = 1; i <= 5; i++) {
            DynamicNaiveBayesClassifier model = new DynamicNaiveBayesClassifier();
            model.setClassVarID(data.getAttributes().getNumberOfAttributes() - 1);
            model.setParallelMode(true);
            model.learn(data);
            DynamicBayesianNetwork nbClassifier = model.getDynamicBNModel();
            System.out.println(nbClassifier.toString());
        }

    }
}
