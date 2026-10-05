package org.openmarkov.learning.metric.cmi.accuracy;


import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.metric.cache.Cache;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.core.util.Util;
import org.openmarkov.learning.metric.cmi.util.MetricUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.openmarkov.learning.metric.cmi.util.MetricUtils.*;

@MetricType(name = "Accuracy", classConditionedMetric = true)
public class Accuracy extends Metric {


    //Frequencies of each value for the class variable
    private double[] freqRootNode;

    //Frequencies two-dimensional crosstab for an attribute and the class variable
    private Map<String, double[][][]> crossTabs = new HashMap<>();

    //Frequencies of each value for the class variable in the training set of each fold
    private double[][] trainingClassCounts;

    //Three dimensional crosstab: attribute, parent, class
    private Map<String, double[][][][]> _2ndLevelCrosstab = new HashMap<>();

    //Flag to use relations between attributes apart from the class (augmented NB)
    private boolean augmentedNet = false;

    private Dataset dataset;
    private double alpha = 0;
    public static final int KFOLD = 10;

    protected String classVariable;
    protected MetricUtils utils;


    private int[][] training;
    private int[][] test;


    @Override
    public double score(TablePotential nodePotential) {
        double score;
        if(!augmentedNet){
            score = scoreNB(nodePotential);
        }else{
            score = scoreAugmentedNet(new Variable[]{nodePotential.getVariable(0), nodePotential.getVariable(1)});
        }
        return score;
    }

    @Override
    public double getScore(PNEdit edit) {
        if(augmentedNet){
            initCache();
            return score(new TablePotential(Arrays.asList(((BaseLinkEdit) edit).getVariableFrom(),
                                                          ((BaseLinkEdit) edit).getVariableTo()), PotentialRole.CONDITIONAL_PROBABILITY));
        }
        return super.getScore(edit);
    }


    /**
     * Return the score for an augmented NB model
     * @param v list of pairs of variables representing the current model
     * @return the result
     */
    private double scoreAugmentedNet(Variable[] v){
        LinkedList<Variable[]> augmentedNet = new LinkedList<>();
        
        getNonRootNodes().stream().filter(n -> n.getNumParents() > 1).toList().forEach(node -> {
            augmentedNet.add(new Variable[]{node.getVariable(),
                    node.getParents().stream().filter(n -> n != getRootNode()).toList().get(0).getVariable()});
        });

        if(!v[0].getName().equals(getRootNode().getName()) && !v[1].getName().equals(getRootNode().getName())){
            augmentedNet.add(v);
        }else{
            getNonRootNodes().stream().filter(n->n.getNumParents()<2 && !n.getName().equals(v[1].getName())).forEach(tail ->{
                augmentedNet.add(new Variable[]{v[1], tail.getVariable()});
            });
        }

        return computeAugmentedNetAccuracy(augmentedNet);
    }


    private double scoreNB(TablePotential nodePotential){
        Set<Variable> variables = new HashSet<>();
        variables.add(nodePotential.getVariable(1));
        if(!probNet.getLinks().isEmpty()){
            variables.addAll(super.probNet.getLinks().stream().map(Link::getTo).map(Node::getVariable).toList());
        }
        return computeNBNetAccuracy(variables);
    }

    private double scoreNB(Variable variable, boolean addLink){
        Set<Variable> variables = !probNet.getLinks().isEmpty()?
                super.probNet.getLinks()
                             .stream()
                             .map(Link::getTo)
                             .map(Node::getVariable)
                             .collect(Collectors.toSet()) : new HashSet<>();
        if(addLink){
            variables.add(variable);
        }else{
            variables.remove(variable);
        }
        return computeNBNetAccuracy(variables);
    }


    /**
     * Computes the bayesian net accuracy
     * @param variables list of variables that conforms the model
     * @return accuracy value
     */
    public double computeNBNetAccuracy(Collection<Variable> variables){
        int counter = 0;

        for(int i=0; i<dataset.getTest().length; i++){
            for (int[] row : dataset.getTest()[i]) {
                if(row[getClassVariableIndex()]==predictClassValue(row, variables, i)){
                    counter++;
                }
            }
        }

        return (double) counter/(dataset.getTest()[0].length*KFOLD);
    }


    /**
     * Computes the accuracy of an augmented NB net
     * @param variables the variables
     * @return the result
     */
    public double computeAugmentedNetAccuracy(List<Variable[]> variables){

        int counter = 0;

        for(int it =0; it<dataset.getTest().length; it++){
            for (int[] row : dataset.getTest()[it]) {
                int predictedClass = predictClassValueAugmentedNB(row, variables, it);
                if(row[getClassVariableIndex()]==predictedClass){
                    counter++;
                }
            }
        }
        return (double) counter/(dataset.getTest()[0].length*KFOLD);
    }


    /**
     * Checks the most probable class for an instance given an specific model
     * @param row case used to predict the class
     * @param variables list of variables that conform the model
     * @param it fold of the case: only its training set is used
     * @return the most probable class
     */
    public int predictClassValue(int[] row, Collection<Variable> variables, int it){
        double[] classCounts = getTrainingClassCounts(it);
        int trainingSize = dataset.getTraining()[it].length;
        // Logarithms, so that many features do not make the product vanish
        double[] logProbs = new double[classCounts.length];
        for(int j=0; j<classCounts.length; j++){
            logProbs[j] = Math.log(classCounts[j]/trainingSize);
        }

        for (Variable variable : variables) {
            int index = getIndexVariable(variable.getName());
            double[][] crossTab = crossTabs.get(variable.getName())[it];
            for(int j=0; j<classCounts.length; j++){
                // A class absent from the training set is already impossible
                if(classCounts[j] > 0){
                    // Alpha is added once per state, so that the probabilities of the states add up to one
                    logProbs[j] += Math.log((crossTab[row[index]][j] + alpha)/(classCounts[j] + alpha*variable.getNumStates()));
                }
            }
        }

        int predictedClass = 0;
        for(int j=1; j<logProbs.length; j++){
            if(logProbs[j] > logProbs[predictedClass]){
                predictedClass = j;
            }
        }
        return predictedClass;
    }

    /** Number of cases of each class in the training set of a fold. */
    private double[] getTrainingClassCounts(int it){
        if(trainingClassCounts == null){
            int numClasses = getRootNode().getVariable().getNumStates();
            int classIndex = getClassVariableIndex();
            trainingClassCounts = new double[KFOLD][numClasses];
            for(int fold=0; fold<KFOLD; fold++){
                for (int[] trainingRow : dataset.getTraining()[fold]) {
                    trainingClassCounts[fold][trainingRow[classIndex]]++;
                }
            }
        }
        return trainingClassCounts[it];
    }

    /**
     * Returns the most probable class value given a case and an augmented NB model
     * @param row the row
     * @param van the van
     * @return the result
     */
    public int predictClassValueAugmentedNB(int[] row, List<Variable[]> van, int it){
        double[] probs = getProbRootNode();
        
        for (String variable : getNonRootNodes().stream().map(Node::getVariable).map(Variable::getName).toList()) {
            int index = getIndexVariable(variable);
            for(int i=0; i<freqRootNode.length;i++){
                probs[i]*= ((crossTabs.get(variable)[it][row[index]][i]+ alpha)/(freqRootNode[i]+ alpha*getNonRootNodes().size()))
                ;
            }
        }

        van.forEach(arc->{
            probs[row[getClassVariableIndex()]] *= getProb(arc[0], arc[1], row, it);
        });

        return getIndexOfMaxValue(probs);
    }


    protected double getProb(Variable tail, Variable head, int[] row, int it){
        double[][][][] ct = _2ndLevelCrosstab.get(tail.getName()+"-"+head.getName());
        double count = 0;

        double value = ct[it][row[getIndexVariable(tail)]][row[getIndexVariable(head)]][row[getClassVariableIndex()]];
        for(int i=0; i < tail.getNumStates(); i++){
            count+=ct[it][i][row[getIndexVariable(head)]][row[getClassVariableIndex()]];
        }
        
        return (value + alpha) / (count + alpha * getNonRootNodes().size());
    }



    private double[][][] buildProbDistributionForNode(Node n){
        double[][][] crossTab = new double[10][n.getVariable().getNumStates()][getRootNode().getVariable().getNumStates()];

        IntStream.range(0,10).forEach(it->{
            TablePotential potential = getAbsoluteFrequencies(probNet, caseDatabase, getRootNode(),
                                                            new ArrayList<>(Arrays.asList(getRootNode().getVariable(), n.getVariable())), dataset.getTraining()[it]);
            int rootStates = getRootNode().getVariable().getNumStates();

            for(int i=0; i<n.getVariable().getNumStates();i++){
                crossTab[it][i]=Arrays.copyOfRange(potential.getValues(), i*rootStates, (i+1)*rootStates);
            }
        });

        return crossTab;
    }



    /**
     * Computes the probabilities for each class value
     * @return array with probability values
     */
    private double[] getProbRootNode(){
        double[] freq = Util.getAbsoluteFreq(probNet, caseDatabase, getRootNode()).getValues();
        for(int i =0; i<freq.length;i++){
            freq[i]=freq[i]/caseDatabase.getNumCases();
        }
        return freq;
    }


    @Override
    protected void initCache() {
        this.cache = new Cache();
        dataset = dataset==null? new Dataset(caseDatabase, KFOLD):dataset;
        utils = new MetricUtils(probNet, classVariable);
        cachedNodeScores = new HashMap<>();
        cache.flush(probNet);
        cachedScore = 0;
        this.freqRootNode=(this.freqRootNode==null)?Util.getAbsoluteFreq(probNet, caseDatabase, getRootNode()).getValues():this.freqRootNode;
        this.training=dataset.getTraining()[0];
        this.test=dataset.getTest()[0];

        if(augmentedNet){
            getNonRootNodes().forEach(n1->{
                crossTabs.put(n1.getName(), buildProbDistributionForNode(n1));
                getNonRootNodes().forEach(n2->{
                    build2ndLevelProbDistribution(n1, n2);
                });
            });

        }else{
            getNonRootNodes().forEach(n ->{
                crossTabs.put(n.getName(), buildProbDistributionForNode(n));
            });
            getNonRootNodes().forEach(node ->{
                AddLinkEdit addEdit = new AddLinkEdit(probNet, getRootNode().getVariable(), node.getVariable(), true);
                cache.cacheScore(addEdit, this.score(addEdit, false));
                RemoveLinkEdit remEdit = new RemoveLinkEdit(probNet, getRootNode().getVariable(), node.getVariable(), true);
                cache.cacheScore(remEdit,  this.score(remEdit, false));
            });
        }
    }



    protected double[][][][] build2ndLevelProbDistribution(Node n1, Node n2){
        int rootStates = getRootNode().getVariable().getNumStates();
        int fstStates = n1.getVariable().getNumStates();
        int sndStates = n2.getVariable().getNumStates();
        double[][][][] crossTab = new double[KFOLD][fstStates][sndStates][rootStates];

        for(int it=0; it<KFOLD; it++){
            // The table has the variables in the order (n1, class, n2), and the first one changes fastest
            double[] values = buildAbsoluteFreqCrossTab(probNet, caseDatabase, n1, n2, getRootNode(),  dataset.getTraining()[it]).getValues();
            for(int i=0; i<fstStates;i++){
                for(int j=0; j<sndStates; j++){
                    for(int k=0; k<rootStates; k++){
                        crossTab[it][i][j][k] = values[i + fstStates * (k + rootStates * j)];
                    }
                }
            }
        }
        _2ndLevelCrosstab.put(n1.getName()+"-"+n2.getName(), crossTab);
        return crossTab;
    }


    public void resetCache(){
        this.cache=null;
    }




    /**
     * Scores the associated network with the link given in the received edition added. We only have to recalculate the score
     * of the destination node.
     *
     * @param edition {@code BaseLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is definitive (UndoableEditHappened called this method) or not.
     * @return {@code double} score of the net with the given edition
     */
    protected double scoreEdit(BaseLinkEdit edition, boolean change) {
        return score(getAbsoluteFrequencies(edition.getProbNet(), caseDatabase, getRootNode(), new ArrayList<>(Arrays.asList(getRootNode().getVariable(), edition.getVariableTo())), dataset.getTraining()[0]));
    }

    @Override protected double score(AddLinkEdit edition, boolean change) {
        return augmentedNet ? scoreAugmentedNet(new Variable[]{edition.getVariableFrom(), edition.getVariableTo()}) :
                scoreNB(edition.getVariableTo(), true);
    }

    @Override protected double score(RemoveLinkEdit edition, boolean change) {
        return augmentedNet ? scoreAugmentedNet(new Variable[]{edition.getVariableFrom(), edition.getVariableTo()}) :
                scoreNB(edition.getVariableTo(), false);
    }

    protected List<Node> getNonRootNodes(){
        return utils.getNonRootNodes();
    }

    private int getIndexVariable(String name){
        return caseDatabase.getVariables().indexOf(caseDatabase.getVariable(name));
    }

    private int getIndexVariable(Variable variable){
        return caseDatabase.getVariables().indexOf(variable);
    }

    private int getClassVariableIndex(){
        return caseDatabase.getVariables().indexOf(caseDatabase.getVariable(classVariable));
    }

    protected Node getRootNode(){
        return utils.getRootNode();
    }

    public void setClassVariable(String classVariable) {
        this.classVariable = classVariable;
    }

    public void setAugmentedNet(boolean augmentedNet) {
        this.augmentedNet = augmentedNet;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getAlpha() {
        return alpha;
    }

/*    @Override
    public void undoableEditHappened(UndoableEditEvent event) {
        if(!augmentedNet){
            super.undoableEditHappened(event);
        }
    }

    @Override
    public void undoEditHappened(UndoableEditEvent event) {
        if(!augmentedNet){
            super.undoEditHappened(event);
        }

    }*/

}
