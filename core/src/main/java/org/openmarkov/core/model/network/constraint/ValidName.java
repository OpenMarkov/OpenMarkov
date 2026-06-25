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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * checks that the name field is filled and there isn't any node with the same
 * name.
 */
public class ValidName extends PNConstraint {
    // Attributes.
    
    /**
     * This method checks that the name field is filled and there isn't any node
     * with the same name.
     *
     * @return true, if the name field isn't empty and there isn't any node with
     * this name; otherwise, false.
     */
    public static boolean nameIsAlreadyPresent(Node node, String newName) {
        var variables = new ArrayList<>(node.getProbNet().getVariables());
        variables.remove(node.getVariable());
        var variablesNames = variables.stream().map(v -> v.getBaseName()).collect(Collectors.toSet());
        return variablesNames.contains(newName);
    }
    
    /**
     * This method checks if exists the specified node.
     *
     * @param name name of the node to search.
     *
     * @return true if the node exists; otherwise, false.
     */
    public static boolean existNode(String name, GraphNetwork probNet) {
        probNet.getNode(name);
        return true;
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
