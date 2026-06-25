/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 *  This class implements the constraint of not allowing event nodes in the network. It is set by default.
 */
@Constraint(name = "NoEventNodes", defaultBehavior = ConstraintBehavior.YES)
public class NoEventNodes extends PNConstraint {
	
	@Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> eventNodes = probNet.getNodes(NodeType.EVENT);
		for(Node node : eventNodes) {
			constraintChecker.addException(new ConstraintViolatedException.CannotHaveEventNodeException(this, node));
		}
	}

}
