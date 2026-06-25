/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.MarkovNetworkType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Static utility methods for complex potential queries on a {@link ProbNet}.
 * <p>
 * These methods perform filtered searches over a network's potentials based on
 * the variable's neighbourhood (probability vs. utility potentials), or build
 * derived networks from projected potentials.
 * <p>
 * Follows the same pattern as {@link ProbNetClassifier},
 * {@link ProbNetAgentManager} and {@link VariableStateOperations}.
 *
 * @author Manuel Arias
 * @see ProbNet
 */
public final class ProbNetPotentialQueries {

    private ProbNetPotentialQueries() {
    }

    /**
     * Gets all probability potentials (non-utility) that contain the given
     * variable. Searches the node associated with the variable and its
     * neighbours.
     *
     * @param probNet  the network to query
     * @param variable variable that belongs to this network
     * @return list of probability potentials containing {@code variable}
     */
    public static List<Potential> getProbPotentials(GraphNetwork probNet, Variable variable) {
        Node nodeVariable = probNet.getNode(variable);
        List<Node> allNodes = probNet.getNeighbors(nodeVariable);
        allNodes.add(nodeVariable);
        List<Potential> potentialsVariable = new ArrayList<>();
        for (Node node : allNodes) {
            for (Potential potential : node.getPotentials()) {
                if (potential.getVariables().contains(variable)
                        && potential.getVariable(0).getDecisionCriterion() == null
                        && potential.getCriterion() == null) {
                    potentialsVariable.add(potential);
                }
            }
        }
        return potentialsVariable;
    }

    /**
     * Gets all utility potentials that contain the given variable.
     * Constant utility potentials are also returned.
     * Searches the node associated with the variable and its neighbours.
     *
     * @param probNet  the network to query
     * @param variable variable that belongs to this network
     * @return list of utility potentials containing {@code variable}
     */
    public static List<Potential> getUtilityPotentials(GraphNetwork probNet, Variable variable) {
        Node nodeVariable = probNet.getNode(variable);
        List<Node> allNodes = probNet.getNeighbors(nodeVariable);
        allNodes.add(nodeVariable);
        List<Potential> potentialsVariable = new ArrayList<>();
        for (Node node : allNodes) {
            for (Potential potential : node.getPotentials()) {
                List<Variable> variables = potential.getVariables();
                if (variables.contains(variable)
                        && (potential.getCriterion() != null || (node.getNodeType() == NodeType.UTILITY
                        && node.getVariable().getDecisionCriterion() != null))) {
                    potentialsVariable.add(potential);
                }
            }
        }
        return potentialsVariable;
    }

    /**
     * Projects all potentials in the network onto the given evidence,
     * returning them in topological order as {@link TablePotential}s.
     *
     * @param probNet      the network whose potentials to project
     * @param evidenceCase the evidence to project onto
     * @return projected potentials in topological order
     * @throws NonProjectablePotentialException if a potential cannot be projected
     */
    public static List<TablePotential> tableProjectPotentials(ProbNet probNet,
                                                              EvidenceCase evidenceCase)
            throws NonProjectablePotentialException {
        List<Potential> originalPotentials = probNet.getSortedPotentials();
        List<TablePotential> projectedPotentials = new ArrayList<>();
        for (Potential potential : originalPotentials) {
            InferenceOptions inferenceOptions = new InferenceOptions(probNet, null);
            projectedPotentials.add(
                    potential.tableProject(evidenceCase, inferenceOptions, projectedPotentials));
        }
        return projectedPotentials;
    }

    /**
     * Builds a Markov decision network from projected potentials.
     * The resulting network has undirected links forming cliques from
     * the variables in each potential.
     *
     * @param probNet                  the original network (used for node type inference
     *                                 and inference options)
     * @param projectedTablePotentials the potentials to add to the Markov network
     * @return a new Markov network with undirected links
     */
    public static ProbNet buildMarkovDecisionNetwork(ProbNet probNet,
                                                     Collection<? extends Potential> projectedTablePotentials) {
        ProbNet markovDecisionNetwork = new ProbNet(MarkovNetworkType.getUniqueInstance());
        markovDecisionNetwork.addConstraint(new OnlyUndirectedLinks());
        for (Potential potential : projectedTablePotentials) {
            markovDecisionNetwork.addPotential(potential, probNet);
        }
        markovDecisionNetwork.setInferenceOptions(probNet.getInferenceOptions());
        return markovDecisionNetwork;
    }
}
