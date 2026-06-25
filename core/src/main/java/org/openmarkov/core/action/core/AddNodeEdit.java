/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.*;

import java.util.Optional;

/**
 * {@code AddNodeEdit} is a edit that allow add a node to
 * {@code ProbNet} object.
 *
 * @author mpalacios
 * @version 1 21/12/10
 */
public class AddNodeEdit extends PNEdit {
    
    // Atribbutes
    /**
     * The new Variable object that match the new node.
     */
    protected final Variable variable;
    
    /**
     * The node type of the new node.
     */
    protected final NodeType nodeType;
    
    /**
     * Graphic position of the new node
     */
    protected final Point2D.Double cursorPosition;
    
    /**
     * The new node
     */
    protected Node newNode;
    
    /**
     * Creates a new {@code AddNodeEdit} with the network where the new
     * new node will be added and basic information about it.
     *
     * @param probNet        the {@code ProbNet} where the new node will be added.
     * @param variable       the variable contained in the new node
     * @param nodeType       The new node type.
     * @param cursorPosition the position (coordinates X,Y) of the node.
     */
    public AddNodeEdit(ProbNet probNet, Variable variable, NodeType nodeType, @Nullable Point2D.Double cursorPosition) {
        super(probNet);
        this.cursorPosition = cursorPosition==null? new Point2D.Double(): cursorPosition.clone();
        this.probNet = probNet;
        this.nodeType = nodeType;
        this.variable = variable;
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(DistinctVariableNames.class) instanceof DistinctVariableNames constraint) {
            if (probNet.getVariablesNames().contains(this.variable.getName())) {
                constraintChecker.addException(new ConstraintViolatedException.VariableNameIsAlreadyPresent(constraint, this.variable.getName()));
            }
        }
        if (probNet.getConstraintOfClass(NoEmptyName.class) instanceof NoEmptyName constraint) {
            String name = this.variable.getName();
            if ((name == null) || (name.contentEquals(""))) {
                constraintChecker.addException(new ConstraintViolatedException.NameOfVariableCannotBeEmpty(constraint, variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyAtemporalVariables.class) instanceof OnlyAtemporalVariables constraint) {
            if (this.variable.isTemporal()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyAtemporalVariablesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyChanceNodes.class) instanceof OnlyChanceNodes constraint) {
            if (this.nodeType != NodeType.CHANCE) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyChanceNodesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(NoEventNodes.class) instanceof NoEventNodes constraint) {
            if (this.nodeType == NodeType.EVENT) {
                constraintChecker.addException(new ConstraintViolatedException.CannotHaveEventNodeException(constraint, this.newNode));
            }
        }
        if (probNet.getConstraintOfClass(OnlyContinuousVariables.class) instanceof OnlyContinuousVariables constraint) {
            if (this.variable.getVariableType() != VariableType.NUMERIC) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyContinuousVariablesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyDiscreteVariables.class) instanceof OnlyDiscreteVariables constraint) {
            if (this.variable.getVariableType() != VariableType.DISCRETIZED) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyDiscreteVariablesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyFiniteStatesVariables.class) instanceof OnlyFiniteStatesVariables constraint) {
            if (!OnlyFiniteStatesVariables.nodeIsFinite(
                    this.nodeType, this.variable.getVariableType())) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyFiniteStatesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyNumericVariables.class) instanceof OnlyNumericVariables constraint) {
            if (this.variable.getVariableType() != VariableType.NUMERIC) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyNumericVariablesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyOneAgent.class) instanceof OnlyOneAgent constraint) {
            if (this.variable.getAgent() != null) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyOneAgentAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(OnlyTemporalVariables.class) instanceof OnlyTemporalVariables constraint) {
            if (!this.variable.isTemporal()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyTemporalVariablesAllowed(constraint, this.variable));
            }
        }
        if (probNet.getConstraintOfClass(ProperUtilityPotentials.class) instanceof ProperUtilityPotentials constraint) {
            if (!this.variable.isTemporal()) {
                int numUtilities = probNet.getNumNodes(NodeType.UTILITY);
                if (this.nodeType == NodeType.UTILITY) {
                    numUtilities = numUtilities + 1;
                }
                if (numUtilities <= 0) {
                    constraintChecker.addException(new ConstraintViolatedException.NetworkHasNoUtilityNodes(constraint, this.probNet));
                }
            }
        }
        
        
    }
    
    @Override protected void doEdit() {
        probNet.addNodeConsistently(variable,nodeType,cursorPosition);
        newNode = probNet.getNode(variable);
    }
    
    @Override public void undo() {
        super.undo();
        probNet.removeNode(newNode);
    }
    
    /**
     * @return newNode the new {@code Node} added
     */
    public Node getNode() {
        return newNode;
    }
    
    public Variable getVariable() {
        return variable;
    }
    
    public NodeType getNodeType() {
        return nodeType;
    }
    
    public Point2D.Double getCursorPosition() {
        return cursorPosition;
    }
    
    @Override public void redo() {
        setTypicalRedo(false);
        super.redo();
        probNet.addNode(newNode);
    }
}
