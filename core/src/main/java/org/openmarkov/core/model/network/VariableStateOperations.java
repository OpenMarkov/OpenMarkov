/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static utility methods for modifying the states of a {@link Variable} when
 * it is attached to a {@link Node} in a {@link ProbNet}.  These operations
 * have side-effects on potentials, links and child nodes, so they do not
 * belong on {@code Variable} itself (which is a pure domain-model class).
 * <p>
 * Follows the same pattern as {@link VariableTypeConverter}.
 *
 * @author Manuel Arias
 * @see Variable
 * @see VariableTypeConverter
 */
public final class VariableStateOperations {

    private VariableStateOperations() {
    }

    /**
     * Dispatches a state modification action on the given variable/node.
     *
     * @param variable    the variable to modify
     * @param node        the node that owns the variable
     * @param stateAction the action to perform (ADD, REMOVE, UP, DOWN, RENAME)
     * @param stateIndex  for ADD: insertion position; for others: inverted index
     * @param newName     name for ADD/RENAME actions
     */
    public static void modifyState(Variable variable, Node node, StateAction stateAction,
                                   int stateIndex, String newName) {
        int selectedStateIndex = stateAction != StateAction.ADD
                ? variable.getNumStates() - (stateIndex + 1) : 0;
        switch (stateAction) {
            case ADD    -> addStateAt(variable, node, stateIndex, newName);
            case REMOVE -> removeStateAt(variable, node, selectedStateIndex);
            case DOWN   -> moveStateDown(variable, node, selectedStateIndex);
            case UP     -> moveStateUp(variable, node, selectedStateIndex);
            case RENAME -> renameState(variable, selectedStateIndex, newName);
        }
    }

    /**
     * Replaces all states of a variable and resets potentials for the node
     * and its children when the number of states changes.
     *
     * @param variable  the variable to modify
     * @param node      the node that owns the variable
     * @param newStates the new states array
     */
    public static void replaceStates(Variable variable, Node node, State[] newStates) {
        State[] lastStates = node.getVariable().getStates();
        List<Potential> lastPotential = node.getPotentials();
        ProbNet probNet = node.getProbNet();

        if (newStates != null) {
            List<Node> nodes;
            node.getVariable().setStates(newStates);

            if (newStates.length != lastStates.length) {

                if (!lastPotential.isEmpty()) {
                    UniformPotential newPotential = new UniformPotential(
                            lastPotential.getFirst().getVariables(),
                            lastPotential.getFirst().getPotentialRole());
                    List<Potential> newPotentials = new ArrayList<>();
                    newPotentials.add(newPotential);
                    node.setPotentials(newPotentials);
                }

                nodes = probNet.getChildren(node);

                for (Node child : nodes) {
                    if (!child.getPotentials().isEmpty()) {
                        List<Potential> container = new ArrayList<>();
                        UniformPotential childLastPotential = new UniformPotential(
                                child.getPotentials().getFirst().getVariables(),
                                child.getPotentials().getFirst().getPotentialRole());
                        container.add(childLastPotential);
                        child.setPotentials(container);
                    }
                }
                resetLink(node);
            }

            if (node.getVariable().getVariableType() == VariableType.DISCRETIZED) {
                node.getVariable().setPartitionedInterval(new PartitionedInterval(
                        node.getVariable().getDefaultInterval(node.getVariable().getNumStates()),
                        Variable.getDefaultBelongs(node.getVariable().getNumStates())));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers — extracted verbatim from Variable
    // -------------------------------------------------------------------------

    private static void addStateAt(Variable variable, Node node, int stateIndex, String newName) {
        State[] oldStates = variable.getStates();
        ArrayList<State> list = Arrays.stream(oldStates)
                                      .collect(Collectors.toCollection(ArrayList::new));
        list.add(stateIndex, new State(newName));
        variable.setStates(list.toArray(new State[0]));
        setUniformPotential(node);
        if (variable.getVariableType() == VariableType.DISCRETIZED) {
            variable.setPartitionedInterval(
                    getNewPartitionedInterval(variable, variable.getPartitionedInterval()));
        }
        resetLink(node);
    }

    private static void removeStateAt(Variable variable, Node node, int selectedStateIndex) {
        State[] oldStates = variable.getStates();
        State[] newStates = new State[variable.getNumStates() - 1];
        int dest = 0;
        boolean skipped = false;
        for (State vState : oldStates) {
            if (dest != selectedStateIndex || skipped) {
                newStates[dest++] = vState;
            } else {
                skipped = true;
            }
        }
        variable.setStates(newStates);
        setUniformPotential(node);
        if (variable.getVariableType() == VariableType.NUMERIC
                || variable.getVariableType() == VariableType.DISCRETIZED) {
            PartitionedInterval current = variable.getPartitionedInterval();
            double[]  oldLimits  = current.getLimits();
            boolean[] oldBelongs = current.getBelongsToLeftSide();
            List<Double>  newLimits  = new ArrayList<>(oldLimits.length - 1);
            List<Boolean> newBelongs = new ArrayList<>(oldBelongs.length - 1);
            for (int j = 0; j < oldLimits.length; j++) {
                if (j != selectedStateIndex) {
                    newLimits.add(oldLimits[j]);
                    newBelongs.add(oldBelongs[j]);
                }
            }
            double[]  limits  = new double[newLimits.size()];
            boolean[] belongs = new boolean[newBelongs.size()];
            for (int j = 0; j < newLimits.size(); j++) {
                limits[j]  = newLimits.get(j);
                belongs[j] = newBelongs.get(j);
            }
            variable.setPartitionedInterval(new PartitionedInterval(limits, belongs));
        }
        resetLink(node);
    }

    private static void moveStateDown(Variable variable, Node node, int selectedStateIndex) {
        if (selectedStateIndex <= 0) {
            return;
        }
        State[] oldStates = variable.getStates();
        State[] newStates = oldStates.clone();
        newStates[selectedStateIndex - 1] = oldStates[selectedStateIndex];
        newStates[selectedStateIndex]     = oldStates[selectedStateIndex - 1];
        reorderStatesInNodeAndChildren(node, variable, newStates);
    }

    private static void moveStateUp(Variable variable, Node node, int selectedStateIndex) {
        if (selectedStateIndex >= variable.getNumStates() - 1) {
            return;
        }
        State[] oldStates = variable.getStates();
        State[] newStates = oldStates.clone();
        newStates[selectedStateIndex]     = oldStates[selectedStateIndex + 1];
        newStates[selectedStateIndex + 1] = oldStates[selectedStateIndex];
        reorderStatesInNodeAndChildren(node, variable, newStates);
    }

    private static void renameState(Variable variable, int selectedStateIndex, String newName) {
        if (selectedStateIndex >= 0 && selectedStateIndex < variable.getNumStates()) {
            // getStates() returns a clone of the array, but the State objects
            // inside are shared references, so setName() modifies the actual state.
            variable.getStates()[selectedStateIndex].setName(newName);
        }
    }

    private static void reorderStatesInNodeAndChildren(Node node, Variable variable,
                                                       State[] newStates) {
        setPotentialAfterReorderingFirstPotential(node, variable, newStates);
        for (Node child : node.getChildren()) {
            setPotentialAfterReorderingFirstPotential(child, variable, newStates);
        }
        variable.setStates(newStates);
        resetLink(node);
    }

    private static void setPotentialAfterReorderingFirstPotential(Node auxNode, Variable variable,
                                                                  State[] newStates) {
        if (auxNode.getNodeType() == NodeType.CHANCE || auxNode.getNodeType() == NodeType.UTILITY) {
            Potential oldPotential = auxNode.getPotentials().getFirst();
            Potential newPotential = oldPotential.reorder(variable, newStates);
            if (newPotential != null) {
                auxNode.setPotential(newPotential);
            }
        }
    }

    /**
     * Sets uniform potential for the edited node and its children (except for
     * Decision nodes).
     */
    private static void setUniformPotential(Node node) {
        Potential uniformPotential;
        List<Potential> potentials;
        ProbNet probNet = node.getProbNet();

        if (node.getNodeType() != NodeType.DECISION) {
            uniformPotential = PotentialOperations.getUniformPotential(
                    probNet, node.getVariable(), node.getNodeType());
            potentials = new ArrayList<>();
            potentials.add(uniformPotential);
            node.setPotentials(potentials);
        }
        for (Node child : node.getChildren()) {
            if (child.getNodeType() != NodeType.DECISION) {
                potentials = new ArrayList<>();
                uniformPotential = PotentialOperations.getUniformPotential(
                        probNet, child.getVariable(), child.getNodeType());
                potentials.add(uniformPotential);
                child.setPotentials(potentials);
            }
        }
    }

    /**
     * Adds a new default sub-interval to the current PartitionedInterval.
     */
    private static PartitionedInterval getNewPartitionedInterval(Variable variable,
                                                                 PartitionedInterval currentPartitionedInterval) {
        double[] limits = currentPartitionedInterval.getLimits();
        double[] newLimits = new double[limits.length + 1];
        boolean[] belongsToLeftSide = currentPartitionedInterval.getBelongsToLeftSide();
        boolean[] newBelongsToLeftSide = new boolean[limits.length + 1];
        for (int i = 0; i < limits.length; i++) {
            newLimits[i] = limits[i];
            newBelongsToLeftSide[i] = belongsToLeftSide[i];
        }

        if (currentPartitionedInterval.getMax() == Double.POSITIVE_INFINITY) {
            newLimits[limits.length - 1] = newLimits[limits.length - 2] + variable.getPrecision();
            newLimits[limits.length] = Double.POSITIVE_INFINITY;
        } else {
            newLimits[limits.length] = currentPartitionedInterval.getMax() + variable.getPrecision();
        }
        newBelongsToLeftSide[limits.length] = false;
        return new PartitionedInterval(newLimits, newBelongsToLeftSide);
    }

    /**
     * Resets link restrictions and revelation conditions of the links of the node.
     */
    private static void resetLink(Node node) {
        Map<Link<Node>, double[]> linkRestrictionMap = new HashMap<>();
        Map<Link<Node>, List> revelationConditionMap = new HashMap<>();
        ProbNet probNet = node.getProbNet();

        for (Link<Node> link : probNet.getLinks(node)) {
            if (link.hasRestrictions()) {
                double[] lastPotential = (
                        (TablePotential) link.getRestrictionsPotential()
                ).getValues().clone();
                linkRestrictionMap.put(link, lastPotential);
                link.setRestrictionsPotential(null);
            }
        }
        List<Node> children = probNet.getChildren(node);
        for (Node child : children) {
            Link<Node> link = probNet.getLink(node, child, true);
            if (link.hasRevealingConditions()) {
                VariableType varType = link.getFrom().getVariable().getVariableType();
                if (varType == VariableType.NUMERIC) {
                    revelationConditionMap.put(link, link.getRevealingIntervals());
                    link.setRevealingIntervals(new ArrayList<PartitionedInterval>());
                } else {
                    revelationConditionMap.put(link, link.getRevealingStates());
                    link.setRevealingStates(new ArrayList<>());
                }
            }
        }
    }
}
