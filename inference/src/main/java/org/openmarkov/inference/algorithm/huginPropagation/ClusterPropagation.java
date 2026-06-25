/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.EvidencePotentials;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.heuristic.rollout.RolloutElimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This abstract class defines the basic operations to create a
 * {@code ClusterForest} given a {@code ProbNet} and to obtain the
 * individual and join probabilities of a set of variables.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public abstract class ClusterPropagation extends InferenceAlgorithm implements Propagation {
    // Attributes
    protected ClusterForest clusterForest;
    /**
     * Indicates next node to eliminate when compiling the net.
     */
    protected EliminationHeuristic heuristic;
    protected boolean netCompiled;
    protected boolean isEvidencePropagated;
    protected EvidenceCase evidence = new EvidenceCase();
    /**
     * Indicates the amount of intermediate results stored by the propagation
     * algorithm
     */
    protected StorageLevel storageLevel = StorageLevel.MEDIUM;
    private EvidenceCase postResolutionEvidence = new EvidenceCase();
    
    /**
     * @param probNet {@code ProbNet}.
     *
     * @throws NotEvaluableNetworkException notEvaluableNetworkException
     */
    public ClusterPropagation(ProbNet probNet) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(probNet);
        this.probNet = probNet.copy();
        this.pNESupport = probNet.getPNESupport();
        netCompiled = false;
        isEvidencePropagated = false;
    }
    
    /**
     * @param network  network
     * @param evidence evidence
     *
     * @return markovNetworkInference
     */
    public static ProbNet projectTablesAndBuildMarkovDecisionNetwork(ProbNet network, EvidenceCase evidence)
            throws NonProjectablePotentialException {
        List<TablePotential> returnedProjectedPotentials = network.tableProjectPotentials(evidence);
        List<TablePotential> projectedPotentials = new ArrayList<>();
        
        for (TablePotential potential : returnedProjectedPotentials) {
            
            if (!potential.getVariables().isEmpty()) {
                projectedPotentials.add(potential);
            } else if (potential.isAdditive()) {
                // It is a utility potential
                if (potential.getValues()[0] != 0) {
                    projectedPotentials.add(potential);
                }
            } else {
                // It is a probability potential
                if (potential.getValues()[0] != 1) {
                    projectedPotentials.add(potential);
                }
            }
            
        }

        return network.buildMarkovDecisionNetwork(projectedPotentials);
    }
    
    // Constructor
    
    public EvidenceCase getPostResolutionEvidence() {
        return postResolutionEvidence;
    }
    
    // Methods
    
    @Override
    public void setPostResolutionEvidence(EvidenceCase postResolutionEvidence) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        this.postResolutionEvidence = postResolutionEvidence;
            updateEvidence();
    }
    
    
    /**
     * Creates a ClusterForest given a MarkovNetwork and a query (a set of
     * variables of interest).
     *
     * @param markovNet {@code ProbNet}.
     * @param heuristic {@code EliminationHeuristic}.
     *
     */
    protected abstract ClusterForest createForest(ProbNet markovNet, EliminationHeuristic heuristic,
                                                  List<Node> queryNodes)
    ;
    
    /**
     * Creates a {@code ClusterForest} given a {@code MarkovNet}.
     *
     * @param markovNet {@code ProbNet}.
     * @param heuristic {@code EliminationHeuristic}.
     */
    protected abstract ClusterForest createForest(ProbNet markovNet, EliminationHeuristic heuristic);
    
    public static boolean isEvaluable(ProbNet probNet) {
        return probNet.getNetworkType().equals(BayesianNetworkType.getUniqueInstance());
    }
    
    public static TablePotential getGlobalUtility() {
        throw new UnsupportedOperationException(
                "getGlobalUtility not implemented for ClusterPropagation");
    }
    
    @Override
    public HashMap<Variable, TablePotential> getPosteriorValues() throws CannotNormalizePotentialException, NonProjectablePotentialException {
        return getPosteriorValues(probNet.getVariables());
    }
    
    /**
     * Creates a {@code ClusterForest}, introduce the evidence and for each
     * root cluster collects the evidence and distribute the evidence.
     *
     * @param variablesOfInterest variablesOfInterest
     *
     * @return A {@code HashMap} with a potential for each variable
     * identified by the variable name
     */
    public HashMap<Variable, TablePotential> getPosteriorValues(List<Variable> variablesOfInterest) throws NonProjectablePotentialException, CannotNormalizePotentialException {
        // to be returned
        HashMap<Variable, TablePotential> individualProbabilities = new HashMap<>();
        
        if (!netCompiled) {
            compilePriorPotentials();
        }
        if (!isEvidencePropagated) {
            // propagates evidence
            propagateProbabilities();
        }
        // gets the posterior probability of each variable
        List<Variable> variablesNoEvidence = new ArrayList<>(variablesOfInterest);
        variablesNoEvidence.removeAll(evidence.getVariables());
        for (Variable variable : variablesOfInterest) {
            ClusterOfVariables cluster = clusterForest.getCluster(variable);
            List<Variable> variablesToKeep = new ArrayList<>(1);
            variablesToKeep.add(variable);
            individualProbabilities.put(variable, DiscretePotentialOperations
                    .marginalize(cluster.getPosteriorPotential(storageLevel), variablesToKeep));
        }
        // Normalize potentials in individualProbabilities
        for (Variable variable : variablesNoEvidence) {
            individualProbabilities
                    .put(variable, DiscretePotentialOperations.normalize(individualProbabilities.get(variable)));
        }
        return EvidencePotentials.addEvidencePotentials(individualProbabilities, variablesOfInterest, evidence);
    }
    
    @Override
    public void setPreResolutionEvidence(EvidenceCase preResolutionEvidence) throws IncompatibleEvidenceException, NonProjectablePotentialException {
        super.setPreResolutionEvidence(preResolutionEvidence);
        updateEvidence();
    }
    
    /**
     * Creates the {@code clusterForest}, introduces evidence and calls
     * {@code collectEvidence} in all the root clusters
     *
     * @param variables {@code ArrayList} of {@code Variable}
     *
     * @return One marginalized potential with the
     * {@code variablesOfInterest} join probability table.
     * {@code Potential}
     */
    public TablePotential getJointProbability(List<Variable> variables) throws NonProjectablePotentialException, CannotNormalizePotentialException {
        if (!netCompiled) {
            compilePriorPotentials();
        }
        if (!isEvidencePropagated) {
            propagateProbabilities();
        }
        ClusterOfVariables queryCluster = getQueryCluster(clusterForest, variables);
        TablePotential jointProbability = DiscretePotentialOperations
                .marginalize(queryCluster.getPosteriorPotential(storageLevel), variables);
        // TODO Investigate why at this point the potential's role is CONDITIONAL PROBABILITY
        jointProbability.setPotentialRole(PotentialRole.JOINT_PROBABILITY);
        jointProbability = DiscretePotentialOperations.normalize(jointProbability);
        return jointProbability;
    }
    
    /**
     * Looks for the cluster that contains all the {@code queryVariables}
     *
     * @param clusterForest  {@code ClusterForest}
     * @param queryVariables {@code ArrayList} of {@code Variable}
     *
     * @return A {@code ClusterOfVariables}
     */
    protected static ClusterOfVariables getQueryCluster(ClusterForest clusterForest, List<Variable> queryVariables) {
        // Brute force algorithm
        int numQueryVariables = queryVariables.size();
        for (ClusterOfVariables cluster : clusterForest.getNodes()) {
            List<Variable> clusterVariables = cluster.getVariables();
            if ((clusterVariables.size() >= numQueryVariables) && (clusterVariables.containsAll(queryVariables))) {
                return cluster;
            }
        }
        return null;
    }
    
    private void updateEvidence() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        evidence = joinPreAndPostResolutionEvidence();
        
        if (!netCompiled) {
            compilePriorPotentials();
        }
        introduceEvidence(evidence);
        isEvidencePropagated = false;
    }
    
    private void propagateProbabilities() {
        for (ClusterOfVariables cluster : clusterForest.getRootClusters()) {
            // collects the evidence and assigns the resulting potential
            // as the posterior potential of this root cluster
            TablePotential collectedEvidence = cluster.collectEvidence(storageLevel);
            cluster.setPosteriorPotential(collectedEvidence);
            cluster.distributeEvidence(storageLevel);
        }
        isEvidencePropagated = true;
    }
    
    private EvidenceCase joinPreAndPostResolutionEvidence() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        EvidenceCase evidence = new EvidenceCase(getPreResolutionEvidence());
        evidence.addFindings(getPostResolutionEvidence().getFindings());
        return evidence;
    }
    
    /**
     * For each {@code Finding} in the {@code EvidenceCase} gets the
     * {@code Potential} associated to the probability and insert this in
     * the {@code ClusterOfVariables} associated to the finding variable
     *
     * @param evidenceCase {@code EvidenceCase}.
     */
    private void introduceEvidence(EvidenceCase evidenceCase) {
        // gets the evidence in an ArrayList<Potential>
        List<Finding> findings = null;
        if (evidenceCase != null) {
            findings = evidenceCase.getFindings();
        }
        if ((findings != null) && (!findings.isEmpty())) {
            ArrayList<TablePotential> evidencePotentials = new ArrayList<TablePotential>();
            for (Finding finding : findings) {
                // Role = JOIN_PROBABILITY only for Bayesian Networks
                evidencePotentials.add(finding.getVariable().deltaTablePotential(finding.getState()));
            }
            // inserts the evidence
            for (TablePotential potential : evidencePotentials) {
                // each potential has only one variable in the 0 position
                Variable variable = potential.getVariables().get(0);
                // selects a cluster containing the variable
                ClusterOfVariables cluster = clusterForest.getCluster(variable);
                if (cluster == null) {
                    // Variable has no cluster — it has no potential in the compiled network
                    // (e.g. it is absent from the Markov network after projection).
                    // Skip this evidence finding; inference proceeds on the remaining variables.
                    continue;
                }
                cluster.addEvidencePotential(potential);
            }
        }
    }
    
    /**
     * @return storageLevel {@code StorageLevel}.
     */
    public StorageLevel getStorageLevel() {
        return storageLevel;
    }
    
    /**
     * @param storageLevel {@code StorageLevel}.
     */
    public void setStorageLevel(StorageLevel storageLevel) {
        this.storageLevel = storageLevel;
    }
    
    /**
     * Creates a {@code ClusterForest} given the potentials stored in the
     * {@code probNet}
     */
    public void compilePriorPotentials() throws NonProjectablePotentialException {
        //            ProbNet markovNet = probNet.getMarkovDecisionNetwork();
        // TODO -FIX!!!
        ProbNet markovNet = projectTablesAndBuildMarkovDecisionNetwork(probNet, null);
        heuristic = heuristicFactory(markovNet);
        clusterForest = createForest(markovNet, heuristic);
        // Multiply prior potentials in each clique to form one prior potential
        for (ClusterOfVariables rootCluster : clusterForest.getRootClusters()) {
            rootCluster.compilePriorPotentials();
        }
        netCompiled = true;
    }
    
    /**
     * Creates an heuristic associated to {@code network}
     *
     * @param markovNetwork {@code MarkovDecisionNetwork}
     *
     * @return {@code EliminationHeuristic}
     */
    private static EliminationHeuristic heuristicFactory(ProbNet markovNetwork) {
        List<List<Variable>> variables = new ArrayList<>();
        variables.add(markovNetwork.getChanceAndDecisionVariables());
        return new RolloutElimination(markovNetwork, variables);
    }
    
    public String toString() {
        return this.getClass()
                   .getSimpleName() + "\n" + "Storage level: " + storageLevel + "\n" + (netCompiled ? "Net compiled." : "Net not compiled.");
    }
    
    /**
     * Indicates the amount of intermediate results stored by the propagation
     * algorithm
     */
    public enum StorageLevel {
        NO_STORAGE, // No storage
        MEDIUM, // Medium storage = up going messages
        FULL // Maximum storage = up going messages and posterior potentials.
    }
}
