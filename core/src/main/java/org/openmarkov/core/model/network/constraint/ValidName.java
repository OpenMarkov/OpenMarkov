/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * checks that the name field is filled and there isn't any node with the same
 * name.
 */
public class ValidName extends PNConstraint {
    // Attributes.
    
    /**
     * Whether another variable of the network already has the name that the node would get with the base
     * name {@code newName}; its twins in other time slices share the base name but not the name.
     */
    public static boolean nameIsAlreadyPresent(Node node, String newName) {
        String fullName = node.getVariable().nameWithBaseName(newName);
        return node.getProbNet().getVariables().stream()
                   .anyMatch(variable -> variable != node.getVariable() && variable.getName().equals(fullName));
    }
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Variable> variables = probNet.getVariables();
        for (Variable variable : variables) {
            String name = variable.getName();
            if ((name == null) || (name.contentEquals(""))) {
                constraintChecker.addException(new ConstraintViolatedException.NameOfVariableCannotBeEmpty(this, variable));
            }
        }
    }
    
}
