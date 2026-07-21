/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.constraint.NoAlwaysObservedDescendantOfDecision;

import java.util.List;

import static org.openmarkov.core.model.network.constraint.NoAlwaysObservedDescendantOfDecision.ancestorInList;

/**
 * {@code NodeAlwaysObservedEdit} is a simple edit that allow modify the always observed property of a variable
 * name.
 */

@SuppressWarnings("serial") public class NodeAlwaysObservedEdit extends PNEdit {
    
    private final boolean lastAlwaysObserved;
    private final boolean newAlwaysObserved;
    private final Node node;
    
    /**
     * Creates a new {@code AlwaysObservedEdit} with the node and new 'alwaysObserved'
     * specified.
     *
     * @param node              the node that will be edited
     * @param newAlwaysObserved the new alwaysObserved
     */
    public NodeAlwaysObservedEdit(Node node, boolean newAlwaysObserved) {
        super(node.getProbNet());
        this.lastAlwaysObserved = node.isAlwaysObserved();
        this.newAlwaysObserved = newAlwaysObserved;
        this.node = node;
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(NoAlwaysObservedDescendantOfDecision.class) instanceof NoAlwaysObservedDescendantOfDecision constraint) {
            List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
            NodeAlwaysObservedEdit nodeAlwaysObservedEdit = this;
            if (this.newAlwaysObserved) {
                for (Node ancestorDecisionNode : ancestorInList(probNet, nodeAlwaysObservedEdit.getNode(), decisionNodes)) {
                    constraintChecker.addException(new ConstraintViolatedException.AlwaysObservedVariableIsDescendantOfDecisionNode(constraint, this.node, ancestorDecisionNode));
                }
            }
        }
    }
    
    @Override protected void doEdit() {
        node.setAlwaysObserved(newAlwaysObserved);
    }
    
    @Override public void undo() {
        super.undo();
        node.setAlwaysObserved(lastAlwaysObserved);
    }
    
    /**
     * Gets the new alwaysObserved after the edition
     *
     * @return the new alwaysObserved
     */
    public boolean getNewAlwaysObserved() {
        return newAlwaysObserved;
    }
    
    /**
     * Gets the new alwaysObserved before the edition
     *
     * @return the last alwaysObserved
     */
    public boolean getLastAlwaysObserved() {
        return lastAlwaysObserved;
    }
    
    public Node getNode() {
        return node;
    }
    
}
