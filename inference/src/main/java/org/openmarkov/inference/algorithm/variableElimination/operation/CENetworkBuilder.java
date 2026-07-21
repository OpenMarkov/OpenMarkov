/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.operation;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.UtilityFunctionComputer;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Network-level transformations on {@link ProbNet} for cost-effectiveness analysis.
 * Pure algebraic operations on {@link GTablePotential}s are in {@link CEAlgebra}.
 *
 * @author Manuel Arias Calleja
 */
public class CENetworkBuilder {

    /**
     * Transforms the utility structure of an influence diagram so that it
     * contains only leaf cost and effectiveness nodes (removing super-value nodes).
     *
     * @param markovDecisionNetwork {@code ProbNet}
     * @return the modified network
     */
    public static ProbNet getCostEffectivenessNetwork(ProbNet markovDecisionNetwork) {
        List<Node> utilityNodes = markovDecisionNetwork.getNodes(NodeType.UTILITY);
        List<Node> utilityNodesWithoutChildren = getUtilitiesWithoutChildren(utilityNodes);

        removeSuperValueNodes(
                CEBaseOperations.getNodesWithGivenCriterion(utilityNodesWithoutChildren, CECriterion.Cost),
                markovDecisionNetwork);
        removeSuperValueNodes(
                CEBaseOperations.getNodesWithGivenCriterion(utilityNodesWithoutChildren, CECriterion.Effectiveness),
                markovDecisionNetwork);
        return markovDecisionNetwork;
    }

    /**
     * Filters a list of utility nodes to return only those with no child nodes
     * (i.e., leaf utility nodes).
     *
     * @param utilityNodes the utility nodes to filter
     * @return utility nodes without children
     */
    public static List<Node> getUtilitiesWithoutChildren(List<Node> utilityNodes) {
        ArrayList<Node> utilityNodesWithoutChildren = new ArrayList<>();
        for (Node utilityNode : utilityNodes) {
            if (utilityNode.getNumChildren() == 0) {
                utilityNodesWithoutChildren.add(utilityNode);
            }
        }
        return utilityNodesWithoutChildren;
    }

    /**
     * Removes super-value nodes from the network. When {@code isBiCriteria} is
     * {@code true}, cost and effectiveness nodes are treated separately.
     *
     * @param probNet      the network to modify
     * @param isBiCriteria whether to handle cost/effectiveness criteria separately
     */
    public static void removeSuperValueNodes(ProbNet probNet, boolean isBiCriteria) {
        List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
        List<Node> utilityNodesWithoutChildren = getUtilitiesWithoutChildren(utilityNodes);
        if (isBiCriteria) {
            removeSuperValueNodes(
                    CEBaseOperations.getNodesWithGivenCriterion(utilityNodesWithoutChildren, CECriterion.Cost),
                    probNet);
            removeSuperValueNodes(
                    CEBaseOperations.getNodesWithGivenCriterion(utilityNodesWithoutChildren, CECriterion.Effectiveness),
                    probNet);
        } else {
            removeSuperValueNodes(utilityNodesWithoutChildren, probNet);
        }
    }

    /**
     * Replaces each super-value node in {@code utilityNodes} with its computed
     * utility function, removing intermediate utility parents.
     *
     * @param utilityNodes     leaf utility nodes to process
     * @param influenceDiagram the network to modify
     */
    public static void removeSuperValueNodes(List<Node> utilityNodes, ProbNet influenceDiagram) {
        for (Node utilityNode : utilityNodes) {
            if (utilityNode.isSuperValueNode()) {
                TablePotential potential;
                try {
                    potential = UtilityFunctionComputer.computeUtilityFunction(utilityNode);
                    List<Potential> potentials = new ArrayList<>();
                    potentials.add(potential);
                    utilityNode.setPotentials(potentials);
                    removeUtilityParents(utilityNode, influenceDiagram);
                    for (Variable variable : potential.getVariables()) {
                        Node node = influenceDiagram.getNode(variable);
                        influenceDiagram.addLink(node, utilityNode, true);
                    }
                } catch (NonProjectablePotentialException e1) {
                    throw new UnreachableException(e1);
                }
            }
        }
    }

    /**
     * Transforms an influence diagram into its cost-effectiveness form:
     * removes separate cost and effectiveness utility nodes and replaces
     * them with a single {@link GTablePotential} combining both.
     *
     * @param influenceDiagram the influence diagram to transform
     * @param lambdaMin        minimum willingness-to-pay threshold
     * @param lambdaMax        maximum willingness-to-pay threshold
     * @return the modified influence diagram
     */
    public static ProbNet getInitializedID(ProbNet influenceDiagram, double lambdaMin, double lambdaMax) {
        influenceDiagram = getCostEffectivenessNetwork(influenceDiagram);
        TablePotential costPotential = getCostPotential(influenceDiagram);
        TablePotential effectivenessPotential = getEffectivenessPotential(influenceDiagram);
        GTablePotential utilityPotential = CEAlgebra.getCEPotential(costPotential, effectivenessPotential, lambdaMin, lambdaMax);
        utilityPotential.setPotentialRole(PotentialRole.UNSPECIFIED);

        List<Node> costAndEffectivenessNodes = influenceDiagram.getNodes(NodeType.UTILITY);
        HashSet<Node> parents = new HashSet<>();
        for (Node node : costAndEffectivenessNodes) {
            parents.addAll(node.getParents());
        }
        for (Node node : costAndEffectivenessNodes) {
            influenceDiagram.removePotentials(node.getPotentials());
        }
        for (Node node : costAndEffectivenessNodes) {
            influenceDiagram.removeNode(node);
        }

        Variable ceVariable = new Variable("CostEffectiveness");
        ceVariable.setVariableType(VariableType.NUMERIC);
        Node ceNode = influenceDiagram.addNode(ceVariable, NodeType.UTILITY);
        utilityPotential.setCriterion(ceVariable.getDecisionCriterion());
        ceNode.addPotential(utilityPotential);
        for (Node parent : parents) {
            influenceDiagram.addLink(parent.getVariable(), ceVariable, true);
        }
        return influenceDiagram;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void removeUtilityParents(Node node, ProbNet influenceDiagram) {
        List<Node> parentsNodes = node.getParents();
        for (Node parentNode : parentsNodes) {
            if (parentNode.getNodeType() == NodeType.UTILITY) {
                influenceDiagram.removeLink(parentNode, node, true);
                removeUtilityParents(parentNode, influenceDiagram);
                influenceDiagram.removeNode(parentNode);
            }
        }
    }

    private static TablePotential getCostPotential(ProbNet influenceDiagram) {
        List<Variable> variables = influenceDiagram.getVariables();
        ArrayList<TablePotential> costPotentials = new ArrayList<>();
        for (Variable variable : variables) {
            if (CEBaseOperations.isCostVariable(variable)) {
                Collection<Potential> potentialsVariable = influenceDiagram.getPotentials(variable);
                for (Potential potential : potentialsVariable) {
                    if (potential.getCriterion().getCECriterion() == CECriterion.Cost) {
                        costPotentials.add((TablePotential) potential);
                    }
                }
            }
        }
        return DiscretePotentialOperations.sum(costPotentials);
    }

    private static TablePotential getEffectivenessPotential(ProbNet influenceDiagram) {
        List<Variable> variables = influenceDiagram.getVariables();
        ArrayList<TablePotential> effectivenessPotentials = new ArrayList<>();
        for (Variable variable : variables) {
            if (CEBaseOperations.isEffectivenessVariable(variable)) {
                Collection<Potential> potentialsVariable = influenceDiagram.getPotentials(variable);
                for (Potential potential : potentialsVariable) {
                    if (potential.getCriterion().getCECriterion() == CECriterion.Effectiveness) {
                        effectivenessPotentials.add((TablePotential) potential);
                    }
                }
            }
        }
        return DiscretePotentialOperations.sum(effectivenessPotentials);
    }
}
