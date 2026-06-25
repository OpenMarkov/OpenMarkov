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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 * When a network is first time created if it is multiagent then would be initialize
 * with two agents Agent 1 and Agent 2 that could be modified by the user, if the network
 * is not multiagent then no agents would have been created.
 *
 * @author myebra
 */
@Constraint(name = "OnlyOneAgent", defaultBehavior = ConstraintBehavior.YES) public class OnlyOneAgent
		extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			if (variable.getAgent() != null) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyOneAgentAllowed(this, variable));
			}
		}
		ProbNet pn = (ProbNet) probNet;
		if (pn.getAgents() != null) {
            constraintChecker.addException(new ConstraintViolatedException.OnlyOneAgentAllowedInNetwork(this, pn));
		}
	}
 
}
