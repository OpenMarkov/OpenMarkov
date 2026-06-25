/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.constraint.DistinctVariableNames;
import org.openmarkov.core.model.network.constraint.NoEmptyName;
import org.openmarkov.core.model.network.constraint.ValidName;
import org.openmarkov.core.action.base.PNEdit;

/**
 * {@code NodeBaseNameEdit} is a simple edit that allow modify the node
 * name.
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
@SuppressWarnings("serial") public class NodeBaseNameEdit extends PNEdit {
    
    
    private final String previousName;
    
    private final String newName;
    
    private final Node node;
    
    /**
     * Creates a new {@code NodeBaseNameEdit} with the node and new name
     * specified.
     *
     * @param node    the node that will be modified
     * @param newName the new name of the node
     */
    public NodeBaseNameEdit(Node node, String newName) {
        super(node.getProbNet());
        this.node = node;
        this.previousName = this.node.getVariable().getBaseName();
        this.newName = newName;
    }
    
    @Override
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(DistinctVariableNames.class) instanceof DistinctVariableNames constraint) {
            if (ValidName.nameIsAlreadyPresent(node, newName)) {
                constraintChecker.addException(new ConstraintViolatedException.VariableNameIsAlreadyPresent(constraint, this.newName));
            }
        }
        if (probNet.getConstraintOfClass(NoEmptyName.class) instanceof NoEmptyName constraint) {
            if ((this.newName == null) || (this.newName.contentEquals(""))) {
                constraintChecker.addException(new ConstraintViolatedException.NameOfVariableCannotBeEmpty(constraint, this.node.getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(ValidName.class) instanceof ValidName constraint) {
            if ((this.newName == null) || (this.newName.contentEquals(""))) {
                constraintChecker.addException(new ConstraintViolatedException.NameOfVariableCannotBeEmpty(constraint, this.node.getVariable()));
            }
            if (ValidName.nameIsAlreadyPresent(node, newName)) {
                constraintChecker.addException(new ConstraintViolatedException.NameOfVariableIsAlreadyPresent(constraint, newName));
            }
        }
    }
    
    
    @Override protected void doEdit() {
        node.getVariable().setBaseName(newName);
    }
    
    @Override public void undo() {
        super.undo();
        node.getVariable().setBaseName(previousName);
    }
    
}
