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
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "OnlyNumericVariables", defaultBehavior = ConstraintBehavior.OPTIONAL) public class OnlyNumericVariables
		extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			VariableType varType = variable.getVariableType();
			if (varType != VariableType.NUMERIC) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyNumericVariablesAllowed(this, variable));
			}
		}
	}
 
 
}
