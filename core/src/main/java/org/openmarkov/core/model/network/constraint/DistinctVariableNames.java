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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.ArrayList;
import java.util.List;

@Constraint(name = "DistinctVariableNames", defaultBehavior = ConstraintBehavior.YES)
public class DistinctVariableNames extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Variable> variablesProbNet = probNet.getVariables();
        List<String> variablesProbNetNames = new ArrayList<>();
        for (Variable variable : variablesProbNet) {
            variablesProbNetNames.add(variable.getName());
        }
        // check that new variables have distinct names
        int numVariables = variablesProbNetNames.size();
        for (int i = 0; i < numVariables - 1; i++) {
            for (int j = i + 1; j < numVariables; j++) {
                if (variablesProbNetNames.get(i).compareTo(variablesProbNetNames.get(j)) == 0) {
                    constraintChecker.addException(new ConstraintViolatedException.VariableNameIsAlreadyPresent(this, variablesProbNetNames.get(i)));
                }
            }
        }
    }
    
}
