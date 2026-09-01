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

/**
 * The network may have at most one utility node. Several utility nodes mean a utility that is a sum
 * — or another combination — of separate terms, which the algorithms of some network types are not
 * written to put together.
 * <p>
 * This is an upper bound only: a network with no utility node at all satisfies it. Demanding at
 * least one is what {@link UtilityNodes} and {@link ProperUtilityPotentials} do.
 */
@Constraint(name = "OnlyOneUtilityNode", defaultBehavior = ConstraintBehavior.OPTIONAL) public class OnlyOneUtilityNode
		extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		if (utilityNodes.size() > 1) {
			constraintChecker.addException(
					new ConstraintViolatedException.NetworkCanHaveOnlyOneUtilityNode(this, utilityNodes));
		}
	}

}
