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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "OnlyChanceNodes", defaultBehavior = ConstraintBehavior.NO) public class OnlyChanceNodes
		extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> nodes = probNet.getNodes();
		for (Node node : nodes) {
			if (node.getNodeType() != NodeType.CHANCE) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyChanceNodesAllowed(this, node.getVariable()));
			}
		}
	}
 
}
