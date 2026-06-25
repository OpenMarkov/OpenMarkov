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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;

@Constraint(name = "ProperUtilityPotentials", defaultBehavior = ConstraintBehavior.OPTIONAL) public class ProperUtilityPotentials
		extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
        if (utilityNodes.isEmpty()) {
            constraintChecker.addException(new ConstraintViolatedException.NetworkHasNoUtilityNodes(this, (ProbNet) probNet));
        } else {
            for (Node utilityNode : utilityNodes) {
                List<Potential> utilityPotentials = utilityNode.getPotentials();
                if ((utilityPotentials == null) || (utilityPotentials.isEmpty())) {
                    constraintChecker.addException(new ConstraintViolatedException.UtilityNodeHasNoPotentials(this, utilityNode));
                }
            }
        }
	}

	public String toString() {
		return this.getClass().getName();
	}
 
}
