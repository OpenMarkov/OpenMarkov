/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.constraint.ValidState;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.java.cloneUtils.CloneUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * {@code NodeReplaceStatesEdit} is a simple edit that allows modify the
 * states of node
 *
 * @author Miguel Palacios
 * @version 1.0 10/05/2011
 */

@SuppressWarnings("serial") public class NodeReplaceStatesEdit extends PNEdit {
    
    
    private final State[] lastStates;
    
    private final State[] newStates;
    
    private final Node node;
    
    private final boolean numberOfStatesChanged;
    
    private final List<Link<Node>> oldLinks;
    private final HashMap<Node, List<Potential>> potentialsByNode;
    /**
     * The intervals of the variable before the edit. Replacing the states of a variable divided into intervals gives
     * it default ones, which undoing the edit used to leave (error 24 of the pending list)
     */
    private final PartitionedInterval lastPartitionedInterval;
    
    public NodeReplaceStatesEdit(Node node, State[] newStates) {
        super(node.getProbNet());
        this.node = node;
        this.newStates = newStates;
        this.lastStates = node.getVariable().getStates();
        this.lastPartitionedInterval = node.getVariable().getPartitionedInterval();
        this.numberOfStatesChanged = this.newStates.length != this.lastStates.length;
        if (this.numberOfStatesChanged) {
            this.potentialsByNode = new HashMap<>();
            this.potentialsByNode.put(this.node, copiesOf(node.getPotentials()));
            node.getChildren().forEach(child -> this.potentialsByNode.put(child, copiesOf(child.getPotentials())));
            this.oldLinks = this.node.getProbNet().getLinks(node).stream().map(CloneUtils::safeClone).toList();
        } else {
            this.potentialsByNode = new HashMap<>();
            this.oldLinks = Collections.emptyList();
        }
    }
    
    /** A copy of each relation of a node; empty for a node without relation, such as a decision without policy. */
    private static List<Potential> copiesOf(List<Potential> potentials) {
        return potentials.stream().map(Potential::copy).toList();
    }
    
    // Methods
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (this.probNet.getConstraintOfClass(ValidState.class) instanceof ValidState constraint) {
            constraint.checkStates(constraintChecker, this.node, this.newStates);
        }
    }
    
    @Override protected void doEdit() {
        this.node.getVariable().replaceStates(this.node, this.newStates);
    }
    
    @Override public void undo() {
        super.undo();
        this.node.getVariable().replaceStates(this.node, this.lastStates);
        this.node.getVariable().setPartitionedInterval(this.lastPartitionedInterval);
        if (this.numberOfStatesChanged) {
            for (var potentialAndNode : this.potentialsByNode.entrySet()) {
                potentialAndNode.getKey().setPotentials(potentialAndNode.getValue());
            }
            for (var oldLink : this.oldLinks) {
                var link = this.probNet.getLink(oldLink.getFrom(), oldLink.getTo(), oldLink.isDirected());
                link.setRevealingStates(oldLink.getRevealingStates());
                link.setRevealingIntervals(oldLink.getRevealingIntervals());
                link.setRestrictionsPotential(oldLink.getRestrictionsPotential());
            }
        }
    }
    
}
