/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.ValidState;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.StateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code NodeStateEdit} is a simple edit that allow modify the states of one
 * node.
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
public class NodeStateEdit extends PNEdit {
    /**
     *
     */
    private static final long serialVersionUID = 4325259909756103849L;
    
    /**
     * The new state
     */
    private final State newState;
    /**
     * The last state before the edition
     */
    private State oldState = new State("");
    /**
     * index of the state selected in the view
     */
    private int selectedStateIndex;
    
    /**
     * The node that the stats belongs to
     */
    private final Node node;
    /**
     * The action to carry out
     */
    private final StateAction stateAction;
    /**
     * The last partitioned interval before the edition
     */
    private final PartitionedInterval currentPartitionedInterval;
    /**
     * The last states before the edition
     */
    private final State[] oldStates;
    /***
     * Map with the link restriction potential for each link.
     */
    private final Map<Link<Node>, double[]> linkRestrictionMap;
    /***
     * Map with the revelation condition list for each link.
     */
    private final Map<Link<Node>, List> revelationConditionMap;
    
    /**
     * Map with the list of potentials of each neighbour of the node
     */
    private final HashMap<Variable, List<Potential>> listOldPotentials;
    
    /**
     * List of potentials of the node
     */
    private final List<Potential> oldPotentials;
    
    /**
     * the index (in the table) associated to the state to edit
     */
    private final int indexState;
    
    private final String newName;
    
    private String oldName;
    
    /**
     * Creates a new {@code NodeStateEdit} to carry out the specified action on the
     * specified state.
     *
     * @param node        the node that will be edited.
     * @param stateAction the action to carry out
     * @param stateIndex  the index (in the table) associated to the state to edit
     * @param newName     a new string for the state edited if the action is ADD.
     */
    public NodeStateEdit(Node node, StateAction stateAction, int stateIndex, String newName) {
        super(node.getProbNet());
        this.node = node;
        this.newName = newName;
        this.indexState = stateIndex;

        Variable variable = node.getVariable();

        // When adding a new state selectedStateIndex is not used
        this.selectedStateIndex = 0;
        if (stateAction != StateAction.ADD) {
            this.selectedStateIndex = variable.getNumStates() - (stateIndex + 1);
        }

        // For RENAME, MOVE and DELETE
        this.oldStates = new State[variable.getStates().length];
        System.arraycopy(variable.getStates(), 0, this.oldStates, 0, variable.getStates().length);
        
        State selectedState = oldStates[selectedStateIndex];
        this.newState = (stateAction != StateAction.RENAME) ? new State(newName) : selectedState;

        if (stateAction != StateAction.ADD) {
            this.oldState = selectedState;
            this.oldName = variable.getStateName(selectedStateIndex);
        }

        this.stateAction = stateAction;
        this.currentPartitionedInterval = variable.getPartitionedInterval();
        this.linkRestrictionMap = new HashMap<>();
        this.revelationConditionMap = new HashMap<>();

        // Save node potentials and neighbours
        this.oldPotentials = probNet.getPotentials(variable);
        this.listOldPotentials = new HashMap<>();
        for (Node nodeNeighbour : probNet.getNeighbors(node)) {
            this.listOldPotentials.put(
                    nodeNeighbour.getVariable(),
                    probNet.getPotentials(nodeNeighbour.getVariable())
            );
        }
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(ValidState.class) instanceof ValidState constraint) {
            constraint.checkState(constraintChecker, this.getNewState()
                                                         .getName(), this.getNode(), this.getStateAction());
        }
    }
    
    @Override protected void doEdit() {
        node.getVariable().modifyState(node, stateAction, this.indexState, newName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void undo() {
        super.undo();
        switch (stateAction) {
            case RENAME:
                oldState.setName(oldName);
                break;
            case ADD:
            case REMOVE:
            case UP:
            case DOWN:
                // We restore the states
                node.getVariable().setStates(oldStates);
                
                // We restore the intervals if the variable type was discretized
                if (node.getVariable().getVariableType() == VariableType.DISCRETIZED) {
                    node.getVariable().setPartitionedInterval(currentPartitionedInterval);
                }
                
                // We restore the link restriction's
                for (Link<Node> link : linkRestrictionMap.keySet()) {
                    link.initializesRestrictionsPotential();
                    TablePotential restrictionPotential = (TablePotential) link.getRestrictionsPotential();
                    restrictionPotential.setValues(linkRestrictionMap.get(link));
                }
                for (Link<Node> link : revelationConditionMap.keySet()) {
                    VariableType varType = link.getFrom().getVariable().getVariableType();
                    if ((varType == VariableType.NUMERIC)) {
                        link.setRevealingIntervals(revelationConditionMap.get(link));
                    } else {
                        link.setRevealingStates(revelationConditionMap.get(link));
                    }

                }

                // We restore the potentials
                node.setPotentials(oldPotentials);
                for (Variable variable : listOldPotentials.keySet()) {
                    probNet.getNode(variable).setPotentials(listOldPotentials.get(variable));
                }

                break;
        }

    }

    
    // TODO redo() implementation
    
    /**
     * Gets the new state created if the action was ADD
     *
     * @return the new state
     */
    public State getNewState() {
        return newState;
    }
    
    /**
     * Gets the new state created if the action was ADD
     *
     * @return the new state
     */
    public State getLastState() {
        return oldState;
    }
    
    public Node getNode() {
        return node;
    }
    
    public StateAction getStateAction() {
        return stateAction;
    }
    


    
    /****
     * This method resets the link restriction and revelation conditions of the
     * links of the node
     *
     * @param node Node
     */
    private void resetLink(Node node) {
        
        for (Link<Node> link : node.getLinks()) {
            if (link.hasRestrictions()) {
                double[] lastPotential = ((TablePotential) link.getRestrictionsPotential()).getValues().clone();
                linkRestrictionMap.put(link, lastPotential);
                link.setRestrictionsPotential(null);
                
            }
        }
        
        for (Node child : node.getChildren()) {
            Link<Node> link = probNet.getLink(node, child, true);
            if (link.hasRevealingConditions()) {
                VariableType varType = link.getFrom().getVariable().getVariableType();
                if (varType == VariableType.NUMERIC) {
                    this.revelationConditionMap.put(link, link.getRevealingIntervals());
                    link.setRevealingIntervals(new ArrayList<PartitionedInterval>());
                } else {
                    this.revelationConditionMap.put(link, link.getRevealingStates());
                    link.setRevealingStates(new ArrayList<State>());
                }
            }
        }
        
    }
    
    public int getIndexState() {
        return indexState;
    }
    
    public String getNewName() {
        return newName;
    }
    
}
