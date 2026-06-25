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
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.ProperUtilityPotentials;
import org.openmarkov.core.action.base.UsesVariable;

/**
 * Edit that removes a node (and its links) from a {@code ProbNet}. Supports undo
 * by re-adding the node.
 */
@SuppressWarnings("serial") public class RemoveNodeEdit extends PNEdit implements UsesVariable {
    
    // Attributes
    protected final Variable variable;
    /**
     * Node associated to variable
     */
    private final Node node;
    
    // Constructor
    
    /**
     * @param probNet {@code ProbNet}
     * @param node    {@code Node}
     */
    public RemoveNodeEdit(ProbNet probNet, Node node) {
        super(probNet);
        this.variable = node.getVariable();
        this.node = node;
    }
    
    /**
     * Creates a remove-node edit by looking up the node for the given variable.
     *
     * @param probNet  the network containing the node
     * @param variable the variable whose node will be removed
     */
    public RemoveNodeEdit(ProbNet probNet, Variable variable) {
        super(probNet);
        this.variable = variable;
        this.node = probNet.getNode(variable);
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(ProperUtilityPotentials.class) instanceof ProperUtilityPotentials constraint) {
            int numUtilities = probNet.getNumNodes(NodeType.UTILITY);
            if (this.getNodeType() == NodeType.UTILITY) {
                numUtilities = numUtilities - 1;
            }
            if (numUtilities <= 0) {
                constraintChecker.addException(new ConstraintViolatedException.NetworkHasNoUtilityNodes(constraint, this.probNet));
            }
        }
    }
    
    // Methods
    @Override protected void doEdit() throws DoEditException.NodeIsNull {
        if (node == null) {
            throw new DoEditException.NodeIsNull(probNet, this);
        }
        probNet.removeNode(node);
        
    }
    
    @Override public void undo() {
        super.undo();
        probNet.addNode(node);
    }
    
    /**
     * @return nodeType {@code NodeType}
     */
    public NodeType getNodeType() {
        return node.getNodeType();
    }
    
    /**
     * @return variable {@code Variable}
     */
    @Override public Variable getVariable() {
        return variable;
    }
    
    public String toString() {
        if (variable == null) {
            return "RemoveNodeEdit: null";
        }
        return "RemoveNodeEdit: " + variable.getName();
    }
    
}
