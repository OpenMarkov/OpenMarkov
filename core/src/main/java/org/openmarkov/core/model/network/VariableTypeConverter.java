/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * Handles the conversion of a node's variable type (FINITE_STATES, NUMERIC,
 * DISCRETIZED) and the cascading updates to potentials of the node and its
 * children.
 * <p>
 * Extracted from {@link Node} to separate complex transformation logic from
 * the node's core identity and CRUD responsibilities.
 *
 * @author Manuel Arias
 */
public final class VariableTypeConverter {

    private VariableTypeConverter() {
    }
    
    public enum VariableConversionOptions{
        DontUpdateSelfPotential
    }

    /**
     * Changes the variable type of a node and updates potentials for the node
     * and its children accordingly.
     *
     * @param node            the node whose variable type is being changed
     * @param newType         the new variable type
     * @param updatePotential
     */
    public static void convertVariableType(Node node, VariableType newType, EnumSet<VariableConversionOptions> options) {
        if(options==null){
            options = EnumSet.noneOf(VariableConversionOptions.class);
        }
        
        VariableType currentType = node.getVariable().getVariableType();

        // Set the new variable type
        node.getVariable().setVariableType(newType);
        switch (currentType){
            case FINITE_STATES, DISCRETIZED -> {
                if (newType == VariableType.NUMERIC) {
                    setPotentialsNodeAndChildren(node, options);
                }
            }
            case NUMERIC -> {
                setPotentialsNodeAndChildren(node, options);
                if (newType == VariableType.DISCRETIZED
                        && node.getVariable().getPartitionedInterval().getNumSubintervals() == 1) {
                    
                    // If there is only one interval, set default partitioned interval
                    PartitionedInterval interval = new PartitionedInterval(
                            node.getVariable().getDefaultInterval(node.getVariable().getNumStates()),
                            Variable.getDefaultBelongs(node.getVariable().getNumStates())
                    );
                    node.getVariable().setPartitionedInterval(interval);
                }
            }
            case EVENT -> {
            }
        }
        


        if (currentType==VariableType.NUMERIC) {
            // Build the list of variables (node and its parents)
            List<Variable> variables = new ArrayList<>();
            if (node.getNodeType() != NodeType.UTILITY) {
                variables.add(node.getVariable());
            }
            for (Node parent : node.getProbNet().getParents(node)) {
                variables.add(parent.getVariable());
            }

            // Create and assign uniform potential
            UniformPotential uniformPotential = new UniformPotential(
                    variables,
                    node.getPotentials().getFirst().getPotentialRole()
            );

            node.setPotentials(new ArrayList<>(List.of(uniformPotential)));
            if(!options.contains(VariableConversionOptions.DontUpdateSelfPotential)){
                setUniformPotentialToNode(node);
            }
        }
    }

    /**
     * Clears revealing conditions and link restrictions on all links of the node.
     *
     * @param node the node whose links are being reset
     */
    public static void resetLinks(Node node) {
        ProbNet probNet = node.getProbNet();

        List<Node> children = probNet.getChildren(node);
        for (Node child : children) {
            Link<Node> link = probNet.getLink(node, child, true);
            if (link.hasRevealingConditions()) {
                link.setRevealingIntervals(new ArrayList<>());
                link.setRevealingStates(new ArrayList<>());
            }
        }

        for (Link<Node> link : probNet.getLinks(node)) {
            if (link.hasRestrictions()) {
                link.setRestrictionsPotential(null);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void setPotentialsNodeAndChildren(Node node, EnumSet<VariableConversionOptions> options) {
        ProbNet probNet = node.getProbNet();
        if(!options.contains(VariableConversionOptions.DontUpdateSelfPotential)){
            setUniformPotentialToNode(node);
        }
        for (Node child : probNet.getChildren(node)) {
            if (child.getNodeType() == NodeType.UTILITY) {
                List<Potential> newPotentials = new ArrayList<>();
                if (child.onlyNumericalParents()) {
                    for (Potential oldPotential : child.getPotentials()) {
                        Potential newPotential = new SumPotential(oldPotential.getVariables(),
                                oldPotential.getPotentialRole());
                        newPotentials.add(newPotential);
                    }
                } else {
                    for (Potential oldPotential : child.getPotentials()) {
                        Potential newPotential = new UniformPotential(oldPotential.getVariables(),
                                oldPotential.getPotentialRole());
                        newPotentials.add(newPotential);
                    }
                }
                child.setPotentials(newPotentials);
            } else {
                if (child.getPotentials() != null && !child.getPotentials().isEmpty()) {
                    setUniformPotentialToNode(child);
                }
            }
        }
    }

    private static void setUniformPotentialToNode(Node node) {
        Potential uniformPotential = PotentialOperations.getUniformPotential(
                node.getProbNet(), node.getVariable(), node.getNodeType());
        node.setPotentials(new ArrayList<>(List.of(uniformPotential)));
    }
}
