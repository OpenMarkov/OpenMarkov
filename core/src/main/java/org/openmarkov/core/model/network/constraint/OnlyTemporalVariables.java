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

import java.util.List;

@Constraint(name = "OnlyTemporalVariables", defaultBehavior = ConstraintBehavior.NO) public class OnlyTemporalVariables
		extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			if (!variable.isTemporal()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyTemporalVariablesAllowed(this, variable));
			}
		}
	}
 
}
