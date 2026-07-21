/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.AuxiliaryOperations;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the absorption of a node into its utility child, updating
 * potentials and graph links accordingly.
 * <p>
 * Extracted from {@link Node} to separate complex domain logic from
 * the node's core identity and CRUD responsibilities.
 *
 * @author Manuel Arias
 */
public final class NodeAbsorptionHandler {

    private NodeAbsorptionHandler() {
    }

    /**
     * Absorbs {@code absorbedVariable}'s node into its single utility child,
     * multiplying (for chance parents) or maximizing (for decision parents)
     * the potentials and updating the graph links.
     *
     * @param node             the node performing the absorption (the utility child's context)
     * @param absorbedVariable the variable whose node is being absorbed
     */
    public static void absorbNodeConsistently(Node node, Variable absorbedVariable) throws NonProjectablePotentialException {
        ProbNet probNet = node.getProbNet();
        Node absorbedNode = probNet.getNode(absorbedVariable);
        Node child = absorbedNode.getChildren().getFirst();
        List<Link<Node>> newParentLinks = new ArrayList<>();
        List<Potential> oldUtilityPotentials = child.getPotentials();
        List<Potential> newPotentials = new ArrayList<>();

        /* Chance parent */
        if (absorbedNode.getNodeType() == NodeType.CHANCE) {
            for (Potential potential : oldUtilityPotentials) {

                // Potentials to multiply
                List<TablePotential> utilityAndChance = new ArrayList<>();
                utilityAndChance.add(potential.getCPT()); //Utility
                utilityAndChance.add(absorbedNode.getPotentials().getFirst().getCPT()); //Chance

                /* Obtain parameters to invoke multiplyAndMarginalize */
                // All variables from chance parent and utility child potentials
                List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(utilityAndChance);

                List<Variable> variablesToKeep = new ArrayList<>(unionVariables);
                variablesToKeep.remove(absorbedVariable);

                List<Variable> variablesToEliminate = new ArrayList<>();
                variablesToEliminate.add(absorbedVariable);

                // Discrete operation is valid because all parents are discrete
                TablePotential marginalizedPotential = DiscretePotentialOperations.
                        multiplyAndMarginalize(utilityAndChance, variablesToKeep, variablesToEliminate);

                // Convert to utility potential
                ExactDistrPotential exactDistrPotential = new ExactDistrPotential(variablesToKeep);
                exactDistrPotential.setValues(marginalizedPotential.getValues());

                newPotentials.add(exactDistrPotential);
            }

            // Parents of chance node are now parents of utility node
            for (Node parent : absorbedNode.getParents()) {
                Link<Node> link = probNet.getLink(parent, child, true);
                if (link == null) {
                    // creating the Link saves it in the graph
                    newParentLinks.add(probNet.addLink(parent, child, true));
                }
            }

            /* Decision parent */
        } else if (absorbedNode.getNodeType() == NodeType.DECISION) {
            for (Potential potential : oldUtilityPotentials) {
                TablePotential utilityPotential;

                utilityPotential = potential.getCPT();

                // Discrete operation is valid because all parents are discrete
                // maximize()[0] is always a TablePotential per its contract
                Object[] maximizeResult = DiscretePotentialOperations.maximize(utilityPotential, absorbedVariable);
                if (!(maximizeResult[0] instanceof TablePotential maximizedPotential)) {
                    throw new IllegalStateException(
                            "maximize() expected to return a TablePotential at index 0, got: "
                            + maximizeResult[0].getClass().getName());
                }
                List<Variable> newVariables = new ArrayList<>(potential.getVariables());
                newVariables.remove(absorbedVariable);

                // Convert to utility potential
                ExactDistrPotential exactDistrPotential = new ExactDistrPotential(newVariables);
                exactDistrPotential.setValues(maximizedPotential.getValues());

                newPotentials.add(exactDistrPotential);
            }
            // Parents of decision node don't turn into parents of utility node
        }
        child.setPotentials(newPotentials);
    }
}
