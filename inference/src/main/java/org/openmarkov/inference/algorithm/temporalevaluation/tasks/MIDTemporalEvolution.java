/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.temporalevaluation.tasks;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.inference.tasks.TemporalEvolution;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.variableElimination.VariableEliminationCore;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VariableElimination;

import java.util.*;

/**
 * Performs MID temporal evolution with Jorge Pérez algorithm. Based on TemporalEvaluation.
 * 14/10/2022 - conditioning to only one decision. The other decisions need to have a policy associated. FIXME Currently crashes when there is no decision
 * 15/10/2022 - Unicriterion and CE
 * 14/10/2022 - 04/11/2022 changes respecting TemporalEvaluation:
 * 1. Implements TemporalEvolution interface.
 * 2. Evaluate method split in methods for clarity and avoiding code repetition.
 * 3. Several constructors for testing and one variable vs a list of nodes.
 * 4. Deletes not needed utility nodes.
 * 5. conditioningVariables only contains the decision node without policy.
 * 6. Computes aggregated utility temporal evolution.
 * 7. Only allows zero/one decision node without policy (TODO check a network without decison node).
 * 8. Addresses the possibility of having temporal nodes sequences with first slice is not zero.
 * 9. Change decision nodes with policies to change nodes (TaskUtilities#impossePolicies)
 * 10. Obtaining discounted and non discounted values
 * 03/11/2022 TODO test cycles, units
 *
 * @author cmyago
 * @version 1.1 17/11/2022 - javadoc added; changes for working with projected utility potentials; set as progress monitor source; discounting process reworked
 */
public class MIDTemporalEvolution extends VariableElimination implements TemporalEvolution {
    public static final String AGGREGATED = "Aggregated";
    
    //copied from TemporalEvaluation
    
    /**
     * Posterior probabilities from the probabilistic and utility temporal variable sequence. Scale not applied
     * <V[0],TP_0>, <V[1],TP_1>...
     * <Variable,TablePotential> because of "HashMap<Variable, TablePotential> TemporalEvolution#getTemporalEvolution"
     */
    private final HashMap<Variable, TablePotential> posteriorValues = new HashMap<>();
    /**
     * Posterior probabilities from the probabilistic and utility temporal variable sequence when discount is applied. For utility nodes
     * <V[0],TP_0>, <V[1],TP_1>...
     * <Variable,TablePotential> because of "HashMap<Variable, TablePotential> TemporalEvolution#getTemporalEvolutionWithDiscount"
     */
    private final HashMap<Variable, TablePotential> discountedPosteriorValues = new HashMap<>();
    // Classified potentials
    protected List<List<TablePotential>> utilityPotentialBySlice;
    protected List<List<TablePotential>> probabilityPotentialBySlice;
    protected List<TablePotential> utilityPotentialAtemporal;
    protected List<TablePotential> probabilityPotentialAtemporal;
    
    //End copied
    /**
     * Atemporal values for @code{#temporalVariable} after evaluating @code{#probNet}.
     */
    protected TablePotential atemporalUtility = null;
    /**
     * True if @code{#probNet} has @link{MulticriteriaOptions.Type#UNICRITERION}.
     */
    protected boolean isUnicriterion;
    /**
     * Criterion for computing @link{#discountedPosteriorValues}
     */
    private Criterion decisionCriterion = null;
    
    /**
     * Cycles to be calculated; for testing changing the number of analysed cycles without changing .pgmx
     */
    private int numSlices;
    
    
    /**
     * Temporal Variable to be displayed; FS What happens with Numeric variables?
     * TraceTemporalEvolutionDialog and VETemporalEvolution have Variable in ther methods signature instead of nodes.
     */
    private Variable temporalVariable;
    
    /**
     * Nodes with variables whose temporal value is aggregated
     */
    private List<Node> aggregatedUtilities;
    
    
    /**
     * True if the variable is contained in a utility node
     */
    private boolean isUtility = true;
    
    /**
     * Conditioning variable. Decision variable to be used as conditioning variable. Null in global analysis
     * TemporalEvaluation performs the analysis conditioning of all decisions (line 71)
     */
    private Variable conditioningDecision;
    
    /**
     * First slice in which the temporal variable sequence appears. Not necessary when having aggregated values.
     */
    private int firstSlice = 0;
    
    
    /**
     * Creates a MIDTemporalEvolution object
     *
     * @param probNet network to be evaluated
     *
     * @throws NotEvaluableNetworkException when there are no temporal variables
     */
    private MIDTemporalEvolution(ProbNet probNet) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.VariableIsNotTemporal, ConstraintViolatedException {
        //ProbNet is copied in class InferenceAlgorithm
        //All decision nodes are added to conditioning variables
        super(probNet);
        if (probNet.hasConstraintOfClass(OnlyAtemporalVariables.class)) {
            throw new NotEvaluableNetworkException.VariableIsNotTemporal(temporalVariable);
        }
        //copied from TemporalEvaluation
        int numberOfSlices = probNet.getInferenceOptions().getTemporalOptions().getHorizon();
        
        this.utilityPotentialBySlice = new ArrayList<>();
        this.probabilityPotentialBySlice = new ArrayList<>();
        this.utilityPotentialAtemporal = new ArrayList<>();
        this.probabilityPotentialAtemporal = new ArrayList<>();
        
        // Initialize the arrays for temporal utilities and probabilities
        for (int i = 0; i <= numberOfSlices; i++) {
            this.utilityPotentialBySlice.add(new ArrayList<>());
            this.probabilityPotentialBySlice.add(new ArrayList<>());
        }
        
        //end copied
        numSlices = probNet.getInferenceOptions().getTemporalOptions().getHorizon();
    }
    
    /**
     * 11/10/2022 - one conditioning decision; the rest of the decision nodes with policies
     * FIXME - global analysis; node without policies;
     * This constructor may be used for aggregation of temporal variables
     *
     * @param probNet probNet containing temporalVariable
     *
     * @throws NotEvaluableNetworkException if the network cannot be evaluated
     */
    public MIDTemporalEvolution(ProbNet probNet, Variable temporalVariable) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.VariableIsNotTemporal, ConstraintViolatedException {
        //ProbNet is copied in class InferenceAlgorithm
        //All decision nodes are added to conditioning variables
        this(probNet);
        
        //Gets the first slice in which temporalVariable sequence is defined
        firstSlice = getFirstSlice(temporalVariable, probNet);
        this.temporalVariable = probNet.getVariable(temporalVariable.getBaseName(), firstSlice);
        //probNet IS COPIED in class InferenceAlgorithm to field probNet
        isUtility = this.probNet.getNode(temporalVariable).getNodeType() == NodeType.UTILITY;
        if (isUtility) this.decisionCriterion = temporalVariable.getDecisionCriterion();
        //One node temporal evolution is made with unicriterion analysis
        forceUnicriterion();
    }
    
    public MIDTemporalEvolution(ProbNet probNet, Variable temporalVariable, int numSlices) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.VariableIsNotTemporal, ConstraintViolatedException {
        //ProbNet is copied in class InferenceAlgorithm
        //All decision nodes are added to conditioning variables
        this(probNet, temporalVariable);
        this.numSlices = numSlices;
        
        
    }
    
    public MIDTemporalEvolution(ProbNet originalProbNet, List<Node> aggregatedUtilities) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.VariableIsNotTemporal, ConstraintViolatedException {
        //ProbNet is copied in class InferenceAlgorithm
        //All decision nodes are added to conditioning variables
        this(originalProbNet);
        this.isUnicriterion = probNet.getInferenceOptions()
                                     .getMultiCriteriaOptions()
                                     .getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION;
        
        List<String> aggregatedUtilitiesNames = aggregatedUtilities.stream()
                                                                   .map(Node::getName)
                                                                   .toList();
        this.aggregatedUtilities = this.probNet.getNodes(NodeType.UTILITY);
        this.aggregatedUtilities.removeIf(node -> !aggregatedUtilitiesNames.contains(node.getName()));
        this.isUtility = true;
    }
    
    
    public MIDTemporalEvolution(ProbNet probNet, List<Node> aggregatedUtilities, int numSlices) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.VariableIsNotTemporal, ConstraintViolatedException {
        this(probNet, aggregatedUtilities);
        this.numSlices = numSlices;
    }
    
    /**
     * Checks if the MID to which the variable to be displayed belongs has all decision nodes with policies excepting conditioningDedision.
     * If the analysis is global conditioningDecision is null and this is not applicable.
     *
     * @param network MID to which the variable to be displayed belongs
     */
    public static void checkDecision(ProbNet network, Node conditioningDecision) throws NotAllNodesHavePoliciesException {
        //Only one decision node without policy;
        if (conditioningDecision == null) {
            return;
        }
        List<Node> nodesWithoutPolicy = network.getNodes(NodeType.DECISION);
        nodesWithoutPolicy.remove(conditioningDecision);
        //FIXME What happens when the exception is raised?
        nodesWithoutPolicy.removeIf(Node::hasPolicy);
        if (!nodesWithoutPolicy.isEmpty()) {
            throw new NotAllNodesHavePoliciesException(conditioningDecision, nodesWithoutPolicy);
        }
    }
    
    //Methods from TemporalEvolution interface
    @Override
    public HashMap<Variable, TablePotential> getTemporalEvolution() {
        if (posteriorValues.isEmpty()) {
            resolve();
        }
        return posteriorValues;
    }
    
    @Override
    public HashMap<Variable, TablePotential> getTemporalEvolutionWithDiscount() {
        getTemporalEvolution();
        posteriorValues.forEach((key, value) ->
                discountedPosteriorValues.put(key, applyDiscount(key, value)));
        return discountedPosteriorValues;
    }
    
    @Override
    public HashMap<Variable, TablePotential> getTemporalEvolutionWithDiscount(Criterion criterion) {
        this.decisionCriterion = criterion;
        return getTemporalEvolutionWithDiscount();
    }
    
    @Override
    public void setDecisionVariable(Variable decisionSelected) {
        this.conditioningDecision = decisionSelected;
    }
    
    //End TemporalEvolution interface
    
    @Override
    public ProbNet getExpandedNetwork() {
        return probNet;
    }
    
    //end copied
    
    //copied from TemporalEvaluation
    @Override
    public TablePotential getAtemporalUtility() {
        if (atemporalUtility == null) {
            resolve();
        }
        return atemporalUtility;
    }
    
    private void resolve() {
        try {
            commonPreprocessing();
            tableProjectAndClassifyPotentials(probNet, getPreResolutionEvidence());
            evaluate();
        } catch (NotAllNodesHavePoliciesException | NonProjectablePotentialException |
                 IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther | CannotNormalizePotentialException e) {
            //TODO: Maybe this can actually happen
            throw new UnreachableException(e);
        }
    }
    
    //copied from TemporalEvaluation (tableProjectAndClassifyPotentials method)
    
    /**
     * Common preparation tasks before applying algorithm in all cases (one node probabilistic or utility and a list of nodes)
     * Check polities
     * Sets conditioning variable.
     */
    private void commonPreprocessing() throws NotAllNodesHavePoliciesException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        checkDecision(probNet, probNet.getNode(conditioningDecision));
        setConditioningVariables(Collections.singletonList(conditioningDecision));
        
        //Removing not needed utility nodes
        List<Node> utilityNodesToDelete = probNet.getNodes(NodeType.UTILITY);
        if (isUtility) {
            /* ArrayList#removeAll delegates to batchRemove that directly operates on the underlying array,
              and does not remove the elements individually.
              Putting into a Set the elements to lookup makes the method faster O(1)
             */
            List<Node> nodesToKeep = aggregatedUtilities;
            if (nodesToKeep == null) {
                nodesToKeep = Collections.singletonList(probNet.getNode(temporalVariable));
            }
            utilityNodesToDelete.removeAll(nodesToKeep);
        }
        utilityNodesToDelete.forEach(utilityNode -> probNet.removeNode(utilityNode));
        this.probNet = TaskUtilities.expandNetwork(this.probNet, true);
        //FIXME Informational predecessors ==null? Is this correct?
        //Replaces decision nodes with policies or informationalPredecessors with chance nodes
        //Not done in TemporalEvaluation
        TaskUtilities.imposePolicies(probNet);
        
        probNet = TaskUtilities.extendPreResolutionEvidence(probNet, getPreResolutionEvidence());
        //04/11/2022 Changed because we are keeping both discounted and no discounted results
//        if (isUtility) {
//            probNet = TaskUtilities.applyDiscounts(probNet, isTemporal);
//            //13/10/2022 only unicriterion and scale not applied
//            if (isUnicriterion) {
//                 //
////                probNet = TaskUtilities.scaleUtilitiesUnicriterion(probNet);
//            } else {
//                //Useful when evaluating the complete network. For testing.
//                probNet = TaskUtilities.scaleUtilitiesCostEffectiveness(probNet);
//            }
//        }
        //This is for testing; when we can only test temporal evolution by doing a complete analysis
        if ((isUtility) && (!isUnicriterion)) {
            probNet = TaskUtilities.applyDiscounts(probNet, true);
            //Useful when evaluating the complete network. For testing.
            probNet = TaskUtilities.scaleUtilitiesCostEffectiveness(probNet);
        }


//		LogManager.getLogger().debug("Discretizing non-observerd numeric variables");
        // Discretize non-observed numeric variables
        probNet = TaskUtilities.discretizeNonObservedNumericVariables(probNet, getPreResolutionEvidence());

//		LogManager.getLogger().debug("Absorb intermediate numeric nodes");
        // Absorb intermediate numeric nodes
        probNet = TaskUtilities.absorbAllIntermediateNumericNodes(probNet, getPreResolutionEvidence());
//		LogManager.getLogger().debug("Projecting and classifying");
    
    }
    //end copied
    
    /**
     * This method project the potentials of the network and classifies the projected potentials by two criteria:
     * Temporal or atemporal potentials and if they are utility or probability potentials.
     *
     * @param evidenceCase Evidence in that the potentials will be projected
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public void tableProjectAndClassifyPotentials(ProbNet probNet, EvidenceCase evidenceCase)
            throws NonProjectablePotentialException {
        List<Potential> originalPotentials = probNet.getSortedPotentials();
        List<TablePotential> projectedPotentials = new ArrayList<>();
        // each original potential may yield several projected potentials;
        
        for (Potential potential : originalPotentials) {
            InferenceOptions inferenceOptions = new InferenceOptions(probNet, null);
            List<TablePotential> potentials;
            TablePotential projectedPotential = potential.tableProject(evidenceCase, inferenceOptions, projectedPotentials);
            
            // Get the main variable of the potential to know if it is of utility or probability
            Variable potentialVariable = potential.getVariable(0);
            
            if (probNet.getNode(potentialVariable).getNodeType() == NodeType.UTILITY) {
                // If the utility potential is a constant potential and it's value it is equals to zero we don't needs it
                if (!projectedPotential.getVariables().isEmpty() || projectedPotential.getValues()[0] != 0) {
                    // We need to distinguish between atemporal and temporal potentials
                    if (potentialVariable.isTemporal()) {
                        // Add the potential to the potentials by slice array
                        utilityPotentialBySlice.get(potentialVariable.getTimeSlice()).add(projectedPotential);
                    } else {
                        // Add the potential to the atemporal array
                        utilityPotentialAtemporal.add(projectedPotential);
                    }
                }
            } else {
                // If the probability potential is a constant potential and it's value it is equals to one we don't needs it
                if (!projectedPotential.getVariables().isEmpty() || projectedPotential.getValues()[0] != 1) {
                    // Same logic as for utility potentials
                    if (potentialVariable.isTemporal()) {
                        probabilityPotentialBySlice.get(potentialVariable.getTimeSlice()).add(projectedPotential);
                    } else {
                        probabilityPotentialAtemporal.add(projectedPotential);
                        
                    }
                }
            }
        }
    }
    
    /**
     * First step of the evaluation algortihm: building and evaluating no temporal part of the network and the first slice.
     *
     * @return the Markov network with the atemporal and first slice potentials to be evaluated
     */
    private ProbNet buildAtemporalAndFirstSlice() {
        // With atemporal part analysed, we build then a markov network to analyse the first slice (slice zero)
        List<TablePotential> atemporalAndFirstSlicePotentials = new ArrayList<>();
        atemporalAndFirstSlicePotentials.addAll(probabilityPotentialBySlice.getFirst());
        // We add and keep all atemporal probability potentials because they could affect all the network
        atemporalAndFirstSlicePotentials.addAll(probabilityPotentialAtemporal);
        return probNet.buildMarkovDecisionNetwork(atemporalAndFirstSlicePotentials);
        
    }
    
    
    /**
     * Evaluates the timeless part of the MID <code>markovToEvaluate<code/>
     *
     * @param markovToEvaluate - Markov ProbNet to be evaluated
     */
    private void evaluateAtemporalNetwork(ProbNet markovToEvaluate) {
        ProbNet markovForInference = markovToEvaluate.copy();
        for (TablePotential utilityPotential : utilityPotentialAtemporal) {
            markovForInference.addPotential(utilityPotential);
        }
        // Create heuristic instance
        EliminationHeuristic heuristic = heuristicFactory(markovForInference, new ArrayList<>(),
                                                          getPreResolutionEvidence().getVariables(), getConditioningVariables(),
                                                          markovForInference.getChanceAndDecisionVariables());
        VariableEliminationCore variableEliminationCore = new VariableEliminationCore(markovForInference, heuristic, isUnicriterion);
        atemporalUtility = variableEliminationCore.getUtility();
    }
    
    
    /**
     * Computes the temporal evolution of <code>markovToEvaluate<code/> in time slice <code>slice<code/>
     *
     * @param slice            slice to be evaluated
     * @param markovToEvaluate Markov ProbNet to be evaluated
     *
     */
    private void evaluateSlice(int slice, ProbNet markovToEvaluate) throws CannotNormalizePotentialException, NonProjectablePotentialException {
        ProbNet markovForInference = markovToEvaluate.copy();
        //Do variables change? I want to say a variable is removed and another added or it is only changed?. Supposing they not change.
        List<Variable> variablesToEliminate = markovForInference.getChanceAndDecisionVariables();
        Variable temporalVariableSlice = null;
        if ((aggregatedUtilities != null)) {
            temporalVariableSlice = new Variable(AGGREGATED);
            temporalVariableSlice.setTimeSlice(slice);
            
        } else if (firstSlice <= slice) {
            temporalVariableSlice = probNet.getVariable(temporalVariable.getBaseName(), slice);
        }
        //If temporalVariable is a probability variable it does nothing
//        utilityPotentialBySlice.get(slice).forEach(markovForInference::addPotential);
        for (TablePotential tablePotential : utilityPotentialBySlice.get(slice)) {
            markovForInference.addPotential(tablePotential, probNet);
        }
//        utilityPotentialBySlice.get(slice).stream().map(tablePotential ->  markovForInference.addPotential(tablePotential,probNet));
        if (!isUtility)
            variablesToEliminate.remove(temporalVariableSlice);
        
        EliminationHeuristic heuristic = heuristicFactory(markovForInference, new ArrayList<>(),
                                                          getPreResolutionEvidence().getVariables(), getConditioningVariables(),
                                                          variablesToEliminate);
        //Only unicriterion
        VariableEliminationCore variableEliminationCore = new VariableEliminationCore(markovForInference, heuristic, isUnicriterion);
        //What happens when slice <=firstSlice? It is ok for temporalVariable; but ByCriterion?
        if (firstSlice <= slice) {
            if (isUtility) {
                TablePotential posteriorUtility = variableEliminationCore.getUtility();
                posteriorValues.put(temporalVariableSlice, posteriorUtility);
            } else {
                if (getPreResolutionEvidence().contains(temporalVariableSlice)) {
                    posteriorValues.put(temporalVariableSlice, createDeterministicPotential(temporalVariableSlice));
                } else {
                    posteriorValues.put(temporalVariableSlice, computePosteriorProbability(variableEliminationCore, temporalVariableSlice));
                }
            }
        }
        
    }
    
    
    /**
     * Eliminates the variables corresponding to {@code previousSlice}  from {@code markovToEvaluate}
     *
     * @param markovToEvaluate Markov ProbNet whose variables are to be eliminated
     * @param previousSlice    slice whose probability variables are eliminated
     *
     * @return the Markov ProbNet without the previous slice variables
     */
    private ProbNet removePreviousSlice(ProbNet markovToEvaluate, int previousSlice) {
        final List<Variable> previousVariables = new ArrayList<>();
        probabilityPotentialBySlice.get(previousSlice).forEach(potential -> {
            try {
                //I
                previousVariables.add(potential.getVariable(0));
            } catch (IndexOutOfBoundsException ignored) {
            }
        });
        EliminationHeuristic heuristic = heuristicFactory(markovToEvaluate, new ArrayList<>(),
                                                          getPreResolutionEvidence().getVariables(), conditioningVariables, previousVariables);
        VariableEliminationCore variableEliminationCore = new VariableEliminationCore(markovToEvaluate, heuristic, isUnicriterion);
        return variableEliminationCore.getMarkovDecisionNetwork();
    }
    
    /**
     * Computes the temporal evaluation of a probabilistic/utility node or a list of utility nodes depending on
     *
     */
    private void evaluate() throws CannotNormalizePotentialException, NonProjectablePotentialException {
        
        // First we evaluate the no temporal part of the network, for that we build a Markov network with atemporal
        // probability potentials.
        //Network containing the probabilistic part of the processed extended network evaluated in slice I
        ProbNet markovToEvaluate = buildAtemporalAndFirstSlice();
        
        //Evaluate no temporal utilities corresponding to upfront values
        if (isUtility) {
            evaluateAtemporalNetwork(markovToEvaluate);
        }
        
        evaluateSlice(0, markovToEvaluate);
        
        for (int slice = 1; slice <= numSlices; slice++) {
            // Add probabilities of the new slice
            probabilityPotentialBySlice.get(slice).forEach(markovToEvaluate::addPotential);
            evaluateSlice(slice, markovToEvaluate);
            // Delete slice slice-1 part of the network;
            // Remove the probability variables of the previous slice (keeping always atemporal probabilities)
            markovToEvaluate = removePreviousSlice(markovToEvaluate, slice - 1);
            //15/11/2022 - Progress monitor
            synchronized (MIDTemporalEvolution.class) {
                MIDTemporalEvolution.class.notifyAll();
            }
            if (Thread.currentThread().isInterrupted())
                return;
            // end
        }
    }
    
    
    /**
     * Forces unicriterion analysis independenly of multricriteria type
     */
    public void forceUnicriterion() {
        this.isUnicriterion = true;
        probNet.getInferenceOptions()
               .getMultiCriteriaOptions()
               .setMulticriteriaType(MulticriteriaOptions.Type.UNICRITERION);
        
    }
    
    /**
     * Returns the first slice for which this variable exists (that is, the variable existence makes sense)
     * FIXME I don't know if there is a straightforward way to do this.
     *
     * @param temporalVariable @link{Variable} whose first slice whe want to know
     *
     * @return the first slice for which this variable exists
     */
    private static int getFirstSlice(Variable temporalVariable, ProbNet probNet) {
        int timeSlice = 0;
        probNet.getVariable(temporalVariable.getBaseName(), timeSlice);
        return timeSlice;
    }
    
    /**
     * Creates a deterministic @link{TablePotential} with variables [@code{variableOfInterest},@link{#conditioningDecision]} when @code{variableOfInterest} belongs to pre-resolution evidence.
     *
     * @param variableOfInterest @link{Variable} whose temporal evolution is displayed.
     *
     * @return a deterministic @link{TablePotential} with "1" value for the State belonging to pre-resolution evidence.
     *
     */
    private TablePotential createDeterministicPotential(Variable variableOfInterest) throws NonProjectablePotentialException {
        // We have to create a potential for each variable of interest that belongs to the evidence
        
        DeltaPotential deltaPotential = new DeltaPotential(Collections.singletonList(variableOfInterest),
                                                           PotentialRole.CONDITIONAL_PROBABILITY, new State(getPreResolutionEvidence().getFinding(variableOfInterest)
                                                                                                                                      .getState()));
        //It can be induced evidence from parents
        TablePotential probPotential = (deltaPotential.tableProject(new EvidenceCase(), null));
        if (conditioningDecision != null) {
            probPotential = probPotential.addVariable(conditioningDecision);
        }
        return probPotential;
    }
    
    
    /**
     * Reorder variables to get P(TV|D) instead of P(D|TV). TV is a probability variable and D a decision one.
     * Adapted for testing from @link{VEPropagation} (lines 215-238); I have doubts about this code
     * TODO ask; comments are copied too
     *
     * @param variableEliminationCore performs the variable elimination algorithm
     * @param probabilityVariable     variable whose temporal sequence values we want
     *
     * @return P(TV | D)
     *
     * @
     */
    private TablePotential computePosteriorProbability(VariableEliminationCore variableEliminationCore, Variable
            probabilityVariable) throws CannotNormalizePotentialException {
        // 13/10/2022 only one conditioning decision variable
        TablePotential posteriorValue = variableEliminationCore.getProbability();
        //copied from org.openmarkov.inference.variableElimination.tasks.VEPropagation.InvokeVariableEliminationCore lines 220-236
        if (posteriorValue.getVariables().getFirst() != probabilityVariable) {
            // TODO - Comprobar este código
            List<Variable> orderedVariables = new ArrayList<>(Arrays.asList(probabilityVariable, conditioningDecision));
            posteriorValue = posteriorValue.reorder(orderedVariables);
        }
        // TODO - Realizar la normalización condicionada
        if (getConditioningVariables() == null || getConditioningVariables().isEmpty()) {
            DiscretePotentialOperations.normalize(posteriorValue);
        }
        //end copied
        return posteriorValue;
    }
    
    
    /**
     * Computes discounting to the values of {@code utilityPotential} according to the @link{Criterion} of {@code temporalUtility}.
     *
     * @param temporalUtility  Variable whose Criterion determines the discount rate
     * @param utilityPotential TablePotential with the values to be discounted
     *
     * @return @link{TablePotential} with the discounted values
     */
    private TablePotential applyDiscount(Variable temporalUtility, TablePotential utilityPotential) {
        
        double discount = CycleLength.getTemporalAdjustedDiscount(probNet.getCycleLength().getUnit(),
                                                                  probNet.getCycleLength()
                                                                         .getValue(), decisionCriterion.getDiscountUnit(),
                                                                  decisionCriterion.getDiscount());
        // Get the discount rate
        double discountRate = 1.0 / (Math.pow((1.0 + discount), temporalUtility.getTimeSlice()));
        TablePotential discountedPotential = new TablePotential(utilityPotential);
        discountedPotential.scale(discountRate);
        return discountedPotential;
    }
    
    /**
     * Criterion for computing @link{#discountedPosteriorValues}
     */
    public Criterion getDecisionCriterion() {
        return decisionCriterion;
    }
    
    /**
     * Sets the decision criterion for computing the temporal evolution
     *
     * @param decisionCriterion for computing the discounted values of the temporal evolution
     */
    public void setDecisionCriterion(Criterion decisionCriterion) {
        this.decisionCriterion = decisionCriterion;
    }
    
    
}
