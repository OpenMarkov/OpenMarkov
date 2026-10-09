/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.apache.logging.log4j.LogManager;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.annotation.InferenceAnnotation;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.variableElimination.VariableEliminationCore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Task: propagation
 * This task returns the probability of each chance variable and the utility of each utility node.
 * <p>
 * Input: a symmetric network and a list of variables of interest.
 * Optional input: post-resolution evidence.
 * <p>
 * Output: a table for each utility or chance node
 * <p>
 * It registers itself under the name "VariableElimination" so that the inference manager can
 * offer it, and hand it out as the default algorithm wherever it applies. Before that, the
 * manager asked for an algorithm of that name that nothing provided, while the application
 * propagated evidence by building this very class by hand.
 *
 * @author mluque
 * @author fjdiez
 * @author Manuel Arias
 * @author jperez-martin
 * @author artasom
 */

@InferenceAnnotation(name = "VariableElimination")
public class VEPropagation extends VariableElimination implements Propagation {
    
    // Attributes
    private VariableEliminationCore variableEliminationCore = null;
    
    private HashMap<Variable, TablePotential> posteriorValues;
    
    /**
     * Evidence when the network has been resolved.
     * In influence diagrams this is Luque and Diez's evidence.
     */
    private EvidenceCase postResolutionEvidence;
    
    private List<Variable> variablesOfInterest;
    
    private HashMap<Variable, Potential> optimalPolicies;
    
    /**
     * @param network Probabilistic network to be resolved
     *
     * @throws NotEvaluableNetworkException Constructor
     */
    public VEPropagation(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(network);
        probNet.getInferenceOptions().getMultiCriteriaOptions()
               .setMulticriteriaType(MulticriteriaOptions.Type.UNICRITERION);
    }
    
    public VEPropagation(ProbNet network, HashMap<Variable, Potential> optimalPolicies) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(network);
        probNet.getInferenceOptions().getMultiCriteriaOptions()
               .setMulticriteriaType(MulticriteriaOptions.Type.UNICRITERION);
        this.optimalPolicies = optimalPolicies;
    }
    
    private void calculateOptimalPolicies(ProbNet probNet, EvidenceCase preResolutionEvidence, List<Node> decisionNodes)
            throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        // If there are any remaining decision nodes in the network, they do not have imposed policies
        if (TaskUtilities.hasDecisionsWithoutImposedPolicy(probNet)) {
            VEEvaluation veEvaluation = new VEEvaluation(probNet);
            veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
            
            // TODO - Remove
            for (Variable conditioningVariable : conditioningVariables) {
                if (probNet.getNode(conditioningVariable).getNodeType() == NodeType.DECISION) {
                    decisionNodes.remove(probNet.getNode(conditioningVariable));
                }
            }
            veEvaluation.setConditioningVariables(getConditioningVariables());
            
            optimalPolicies = veEvaluation.getOptimalPolicies();
            
        } else {
            optimalPolicies = new HashMap<>();
        }
    }
    
    /** Whether the findings are possible; null until it is asked. */
    private Boolean evidenceIsPossible;

    private void resolve() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
        LogManager.getLogger(getClass()).trace("Resolving VEPropagation");
        posteriorValues = new HashMap<>();
        boolean isTemporal = !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);
        List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
        
        calculateOptimalPolicies(probNet, getPreResolutionEvidence(), decisionNodes);
        
        for (Node decisionNode : decisionNodes) {
            Potential policy = optimalPolicies.get(decisionNode.getVariable());
            if (policy != null) { // If the optimal policy is null here it is just because the decision node has a policy imposed by the user
                decisionNode.setPotential(policy);
            }
        }
        
        generalPreprocessing();
//		unicriterionPreprocess();
        // TODO - Implement: For each super-value node, create a new node whose parents are all chance or decision nodes
        ProbNet beforeAbsorbing = probNet.copy();
        exactAlgorithmsPreprocessing();
        // Straight to the field, not through setPostResolutionEvidence: this is preprocessing
        // replacing its own evidence with a derived one, not a caller supplying evidence.
        this.postResolutionEvidence = TaskUtilities.extendPostResolutionEvidence(probNet, getPostResolutionEvidence());
        
        List<Variable> variablesOfInterestBelongingToEvidence = new ArrayList<>();
        EvidenceCase evidence = getAllEvidence();
        List<Variable> evidenceVariables = evidence.getVariables();
        
        if (variablesOfInterest != null) {
            for (Variable variableOfInterest : variablesOfInterest) {
                Variable variableOfInterestInProbnet = probNet.getVariable(variableOfInterest.getName());
                if (evidenceVariables.contains(variableOfInterestInProbnet)) {
                    variablesOfInterestBelongingToEvidence.add(variableOfInterestInProbnet);
                } else {
                    // A utility node absorbed by the one that adds or multiplies it is computed where it is the
                    // last one. A numeric chance node that was made discrete is not computed.
                    Node absorbed = beforeAbsorbing.getNode(variableOfInterest);
                    boolean wasAbsorbed = probNet.getNode(variableOfInterest) == null && absorbed != null
                            && absorbed.getNodeType() == NodeType.UTILITY;
                    if (probNet.getNode(variableOfInterest) != null || wasAbsorbed) {
                        ProbNet network = wasAbsorbed ? whereItIsTheLast(beforeAbsorbing, variableOfInterest)
                                : probNet.copy();
                        ProbNet markovNetwork = TaskUtilities.projectTablesAndBuildMarkovDecisionNetwork(
                                pruneNetwork(network, variableOfInterest), evidence);
                        InvokeVariableEliminationCore(markovNetwork, evidence, variableOfInterest,
                                                      network.getNode(variableOfInterest).getNodeType());
                    }
                }
            }
        }
        
        // We have to create a potential for each variable of interest that belongs to the evidence
        TablePotential probPotential = null;
        DeltaPotential deltaPotential;
        for (Variable variable : variablesOfInterestBelongingToEvidence) {
            deltaPotential = new DeltaPotential(Collections.singletonList(variable),
                                                PotentialRole.CONDITIONAL_PROBABILITY, new State(evidence.getFinding(variable)
                                                                                                         .getState()));
            probPotential = deltaPotential.tableProject(new EvidenceCase(), null);
            probPotential.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY);
            posteriorValues.put(probNet.getVariable(variable.getName()), probPotential);
        }
        
    }
    
    // Methods
    
    private void InvokeVariableEliminationCore(ProbNet network, EvidenceCase evidence, Variable variableOfInterest,
                                               NodeType typeOfTheNode)
            throws IncompatibleEvidenceException, NonProjectablePotentialException {
        // From the network the elimination will run on, which is the one passed in - not from the
        // field, which is the network before its potentials were projected. The two hold the same
        // chance and decision variables today, so this changes nothing; they stop holding the same
        // ones the moment a potential contributes factors written on a variable of its own, and then
        // taking the list from the wrong one leaves that variable to survive the elimination.
        List<Variable> variablesToEliminate = network.getChanceAndDecisionVariables();
        variablesToEliminate.remove(variableOfInterest);
        //TODO: eliminate the observable variables (DANs)
        
        // Create heuristic instance
        EliminationHeuristic heuristic = heuristicFactory(network, new ArrayList<Variable>(), evidence.getVariables(),
                                                          getConditioningVariables(), variablesToEliminate);
        
        variableEliminationCore = new VariableEliminationCore(network, heuristic, true);
        
        TablePotential posteriorValue;
        if (typeOfTheNode == NodeType.UTILITY) {
            posteriorValue = variableEliminationCore.getUtility();
            if (posteriorValue == null) {
                posteriorValue = new TablePotential(Arrays.asList(variableOfInterest), PotentialRole.UNSPECIFIED);
            }
        } else {
            posteriorValue = variableEliminationCore.getProbability();
            if (posteriorValue != null) {
                if (posteriorValue.getVariables().get(0) != variableOfInterest) {
                    // TODO - Comprobar este código
                    List<Variable> oldOrderVariables = new ArrayList<>(posteriorValue.getVariables());
                    oldOrderVariables.remove(variableOfInterest);
                    
                    List<Variable> orderedVariables = new ArrayList<>();
                    orderedVariables.add(variableOfInterest);
                    orderedVariables.addAll(oldOrderVariables);
                    posteriorValue = posteriorValue.reorder(orderedVariables);
                }
                // TODO - Realizar la normalización condicionada
                if (getConditioningVariables() == null || getConditioningVariables().isEmpty()) {
                    try {
                        DiscretePotentialOperations.normalize(posteriorValue);
                    } catch (CannotNormalizePotentialException e) {
                        // Every state has probability zero: either the evidence is impossible, or the variable
                        // does not exist, which is left as zeros
                        if (!evidenceIsPossible(evidence)) {
                            throw new IncompatibleEvidenceException.EvidenceIsImpossible();
                        }
                    }
                }
            }
        }
        
        posteriorValues.put(variableOfInterest, posteriorValue);
    }
    
    /**
     * @param preprocessedNetwork the preprocessed network
     * @param variableOfInterest the variable of interest
     *
     * @return the result
     *
     * @throws IncompatibleEvidenceException if the evidence is incompatible with the network
     */
    /** A copy of the network without the descendants of a utility node, prepared as the whole network was. */
    private ProbNet whereItIsTheLast(ProbNet network, Variable utilityVariable)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        ProbNet copy = network.copy();
        Deque<Node> pending = new ArrayDeque<>(copy.getNode(utilityVariable).getChildren());
        Set<Node> descendants = new LinkedHashSet<>();
        while (!pending.isEmpty()) {
            Node node = pending.pop();
            if (descendants.add(node)) {
                pending.addAll(node.getChildren());
            }
        }
        descendants.forEach(copy::removeNode);
        copy = TaskUtilities.discretizeNonObservedNumericVariables(copy, getPreResolutionEvidence());
        return TaskUtilities.absorbAllIntermediateNumericNodes(copy, getPreResolutionEvidence());
    }

    /** @return whether the findings, taken together, have a probability greater than zero */
    private boolean evidenceIsPossible(EvidenceCase evidence)
            throws IncompatibleEvidenceException, NonProjectablePotentialException {
        if (evidenceIsPossible == null) {
            evidenceIsPossible = evidence.getFindings().isEmpty() || probabilityOf(evidence) > 0;
        }
        return evidenceIsPossible;
    }

    /** The probability of the findings, computed on the findings and their ancestors alone. */
    private double probabilityOf(EvidenceCase evidence)
            throws IncompatibleEvidenceException, NonProjectablePotentialException {
        ProbNet network = probNet.copy();
        List<Variable> variablesNotToBePruned = new ArrayList<>();
        for (Finding finding : evidence.getFindings()) {
            Node node = network.getNode(finding.getVariable().getName());
            if (node != null && !variablesNotToBePruned.contains(node.getVariable())) {
                variablesNotToBePruned.add(node.getVariable());
                for (Node ancestor : ProbNetOperations.getNodeAncestors(node)) {
                    if (!variablesNotToBePruned.contains(ancestor.getVariable())) {
                        variablesNotToBePruned.add(ancestor.getVariable());
                    }
                }
            }
        }
        ProbNet markovNetwork = TaskUtilities.projectTablesAndBuildMarkovDecisionNetwork(
                ProbNetOperations.getPruned(network, variablesNotToBePruned, evidence), evidence);
        EliminationHeuristic heuristic = heuristicFactory(markovNetwork, new ArrayList<Variable>(),
                                                          evidence.getVariables(), getConditioningVariables(),
                                                          markovNetwork.getChanceAndDecisionVariables());
        TablePotential probability = new VariableEliminationCore(markovNetwork, heuristic, true).getProbability();
        return probability == null ? 1 : Arrays.stream(probability.getValues()).sum();
    }

    private ProbNet pruneNetwork(ProbNet preprocessedNetwork, Variable variableOfInterest)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        //Prune all the nodes except the variable of interest and its ancestors (and the corresponding findings).
        List<Variable> variablesNotToBePruned = new ArrayList<>();
        variablesNotToBePruned.add(variableOfInterest);
        for (Node node : ProbNetOperations.getNodeAncestors(preprocessedNetwork.getNode(variableOfInterest))) {
            variablesNotToBePruned.add(node.getVariable());
        }
        for (Finding finding : getAllEvidence().getFindings()) {
            if (!variablesNotToBePruned.contains(finding.getVariable())) {
                variablesNotToBePruned.add(finding.getVariable());
            }
        }
        return ProbNetOperations.getPruned(preprocessedNetwork, variablesNotToBePruned, getAllEvidence());
    }
    
    @Override public HashMap<Variable, TablePotential> getPosteriorValues()
            throws NonProjectablePotentialException, IncompatibleEvidenceException, ConstraintViolatedException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        if (posteriorValues == null) {
            resolve();
        }
        return posteriorValues;
    }
    
    public EvidenceCase getPostResolutionEvidence() {
        return postResolutionEvidence;
    }
    
    @Override public void setPostResolutionEvidence(EvidenceCase postResolutionEvidence) {
        this.postResolutionEvidence = postResolutionEvidence;
    }
    
    public EvidenceCase getAllEvidence() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        EvidenceCase evidence = new EvidenceCase(getPreResolutionEvidence());
        if (postResolutionEvidence != null) {
            evidence.addFindings(postResolutionEvidence.getFindings());
        }
        return evidence;
    }

    /**
     * As in every exact algorithm, but a numeric variable observed after the resolution is also kept at its
     * observed value, and the findings of both evidence cases move to the variables of the discretized network.
     */
    @Override void exactAlgorithmsPreprocessing() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase allEvidence = getAllEvidence();
        probNet = TaskUtilities.discretizeNonObservedNumericVariables(probNet, allEvidence);
        probNet = TaskUtilities.absorbAllIntermediateNumericNodes(probNet, allEvidence);
        replacePreResolutionEvidence(findingsOn(getPreResolutionEvidence(), allEvidence));
        if (postResolutionEvidence != null) {
            postResolutionEvidence = findingsOn(postResolutionEvidence, allEvidence);
        }
    }

    /** The findings of {@code moved} on the variables of {@code evidence}, matched by name. */
    private static EvidenceCase findingsOn(EvidenceCase evidence, EvidenceCase moved) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Set<String> names = evidence.getVariables().stream().map(Variable::getName).collect(Collectors.toSet());
        EvidenceCase findings = new EvidenceCase();
        for (Finding finding : moved.getFindings()) {
            if (names.contains(finding.getVariable().getName())) {
                findings.addFinding(finding);
            }
        }
        return findings;
    }
    
    public List<Variable> getVariablesOfInterest() {
        return variablesOfInterest;
    }
    
    @Override public void setVariablesOfInterest(List<Variable> variablesOfInterest) {
        this.variablesOfInterest = variablesOfInterest;
    }
}