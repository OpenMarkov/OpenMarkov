/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Static utility methods for modifying the states of a {@link Variable} when
 * it is attached to a {@link Node} in a {@link ProbNet}.  These operations
 * have side-effects on potentials, links and child nodes, so they do not
 * belong on {@code Variable} itself (which is a pure domain-model class).
 *
 * @author Manuel Arias
 * @see Variable
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
                    Potential last = lastPotential.getFirst();
                    afterTheStatesChange(node, () -> new UniformPotential(last.getVariables(), last.getPotentialRole()));
                }

                nodes = probNet.getChildren(node);

                for (Node child : nodes) {
                    if (child.getNodeType() == NodeType.UTILITY) {
                        child.setPotentials(new ArrayList<>(List.of(PotentialUtils.generateDefaultPotential(child))));
                    } else if (!child.getPotentials().isEmpty()) {
                        Potential last = child.getPotentials().getFirst();
                        afterTheStatesChange(child, () -> new UniformPotential(last.getVariables(), last.getPotentialRole()));
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
        variable.checkStateName(newName, null);
        State[] oldStates = variable.getStates();
        ArrayList<State> list = Arrays.stream(oldStates)
                                      .collect(Collectors.toCollection(ArrayList::new));
        list.add(stateIndex, new State(newName));
        variable.setStates(list.toArray(new State[0]));
        int[] oldStateOf = new int[list.size()];
        for (int i = 0; i < oldStateOf.length; i++) {
            oldStateOf[i] = i < stateIndex ? i : (i == stateIndex ? -1 : i - 1);
        }
        setUniformPotential(node, oldStates.length, oldStateOf);
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
        int[] oldStateOf = new int[newStates.length];
        for (int i = 0; i < oldStateOf.length; i++) {
            oldStateOf[i] = i < selectedStateIndex ? i : i + 1;
        }
        setUniformPotential(node, oldStates.length, oldStateOf);
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
            // getStates() returns a clone of the array, but the State objects inside are shared
            // references, so renaming through the variable modifies the actual state. The variable
            // is the owner of the renaming rule: it rejects a name another state already has, and
            // it moves the additional properties of the state, which are indexed by name.
            variable.renameState(variable.getStates()[selectedStateIndex], newName);
        }
    }

    private static void reorderStatesInNodeAndChildren(Node node, Variable variable,
                                                       State[] newStates) {
        // Every potential is asked first and none is installed until all of them have answered,
        // so a potential that refuses leaves the network as it was instead of half changed.
        refuseIfAnyPotentialCannotBeReordered(node, variable);
        Map<Node, List<Potential>> reordered = new HashMap<>();
        reordered.put(node, reorderedPotentials(node, variable, newStates));
        for (Node child : node.getChildren()) {
            reordered.put(child, reorderedPotentials(child, variable, newStates));
        }
        reordered.forEach(Node::setPotentials);
        variable.setStates(newStates);
        resetLink(node);
    }

    /** @throws NotSupportedOperationException if the node or one of its children refuses. */
    private static void refuseIfAnyPotentialCannotBeReordered(Node node, Variable variable) {
        List<Node> affected = new ArrayList<>(node.getChildren());
        affected.add(node);
        for (Node auxNode : affected) {
            for (Potential potential : auxNode.getPotentials()) {
                String why = potential.whyStatesCannotBeReordered(variable);
                if (why != null) {
                    throw new NotSupportedOperationException(why);
                }
            }
        }
    }

    /**
     * The potentials of a chance, utility or decision node with the states of {@code variable} in
     * their new order; those of any other node, unchanged.
     */
    private static List<Potential> reorderedPotentials(Node auxNode, Variable variable, State[] newStates) {
        List<Potential> potentials = auxNode.getPotentials();
        NodeType nodeType = auxNode.getNodeType();
        if (nodeType != NodeType.CHANCE && nodeType != NodeType.UTILITY && nodeType != NodeType.DECISION) {
            return potentials;
        }
        List<Potential> reordered = new ArrayList<>();
        for (Potential potential : potentials) {
            Potential newPotential = potential.contains(variable) ? potential.reorder(variable, newStates) : null;
            reordered.add(newPotential != null ? newPotential : potential);
        }
        return reordered;
    }

    /**
     * Sets a uniform potential for the edited node and its children, except for a decision, which loses its
     * imposed policy, and for a utility child, which keeps its values ({@link #withTheNewStatesOf}).
     *
     * @param oldStateOf the old index of each state of the edited variable, or -1 for a new one
     */
    private static void setUniformPotential(Node node, int oldNumStates, int[] oldStateOf) {
        ProbNet probNet = node.getProbNet();
        afterTheStatesChange(node, () -> PotentialOperations.getUniformPotential(
                probNet, node.getVariable(), node.getNodeType()));
        for (Node child : node.getChildren()) {
            if (child.getNodeType() == NodeType.UTILITY) {
                child.setPotentials(withTheNewStatesOf(child, node.getVariable(), oldNumStates, oldStateOf));
            } else {
                afterTheStatesChange(child, () -> PotentialOperations.getUniformPotential(
                        probNet, child.getVariable(), child.getNodeType()));
            }
        }
    }

    /**
     * The relation of a node that is not a utility once its states, or those of a parent, change in number: a decision
     * loses its imposed policy and is optimized again; any other node gets {@code uniformPotential}.
     */
    private static void afterTheStatesChange(Node node, Supplier<Potential> uniformPotential) {
        if (node.getNodeType() == NodeType.DECISION) {
            node.clearPotentials();
        } else {
            node.setPotentials(new ArrayList<>(List.of(uniformPotential.get())));
        }
    }

    /**
     * The relations of a utility node once a state of its parent has been added or removed: a table or an exact
     * distribution keeps the values of the configurations that are still there, with 0 in those of a new state;
     * any other relation becomes that of a new utility node.
     *
     * @param oldStateOf the old index of each state of the parent, or -1 for a new one
     */
    private static List<Potential> withTheNewStatesOf(Node utility, Variable parent, int oldNumStates, int[] oldStateOf) {
        List<Potential> potentials = new ArrayList<>();
        for (Potential potential : utility.getPotentials()) {
            List<Variable> variables = potential.getVariables();
            Potential newPotential = null;
            if (!variables.contains(parent)) {
                newPotential = potential;
            } else if (potential instanceof ExactDistrPotential exactDistrPotential) {
                double[] values = valuesWithTheNewStates(variables.subList(1, variables.size()),
                                                         exactDistrPotential.getValues(), parent, oldNumStates, oldStateOf);
                if (values != null) {
                    newPotential = new ExactDistrPotential(variables, potential.getPotentialRole(), values);
                }
            } else if (potential.getClass() == TablePotential.class || potential.getClass() == UncertainTablePotential.class) {
                double[] values = valuesWithTheNewStates(variables, ((TablePotential) potential).getValues(),
                                                         parent, oldNumStates, oldStateOf);
                if (values != null) {
                    newPotential = new TablePotential(variables, potential.getPotentialRole(), values);
                }
            }
            if (newPotential == null) {
                newPotential = PotentialUtils.generateDefaultPotential(utility);
            }
            if (newPotential != potential) {
                newPotential.setComment(potential.getComment());
                newPotential.setCriterion(potential.getCriterion());
            }
            potentials.add(newPotential);
        }
        return potentials;
    }

    /**
     * The values of a table over {@code variables} once {@code parent} has its new states, or {@code null} if
     * {@code oldValues} do not fit the old states. The first variable changes fastest.
     */
    private static double[] valuesWithTheNewStates(List<Variable> variables, double[] oldValues, Variable parent,
                                                   int oldNumStates, int[] oldStateOf) {
        int position = variables.indexOf(parent);
        int[] newDimensions = variables.stream().mapToInt(Variable::getNumStates).toArray();
        int[] oldOffsets = new int[newDimensions.length];
        int oldSize = 1;
        for (int i = 0; i < newDimensions.length; i++) {
            oldOffsets[i] = oldSize;
            oldSize *= i == position ? oldNumStates : newDimensions[i];
        }
        if (oldValues.length != oldSize) {
            return null;
        }
        int newSize = Arrays.stream(newDimensions).reduce(1, (a, b) -> a * b);
        double[] newValues = new double[newSize];
        for (int cell = 0; cell < newSize; cell++) {
            int rest = cell;
            int oldCell = 0;
            boolean isNew = false;
            for (int i = 0; i < newDimensions.length; i++) {
                int state = rest % newDimensions[i];
                rest /= newDimensions[i];
                if (i == position) {
                    state = oldStateOf[state];
                    isNew = state < 0;
                }
                oldCell += state * oldOffsets[i];
            }
            if (!isNew) {
                newValues[cell] = oldValues[oldCell];
            }
        }
        return newValues;
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
            if (LinkOperations.hasRevealingConditions(link)) {
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
