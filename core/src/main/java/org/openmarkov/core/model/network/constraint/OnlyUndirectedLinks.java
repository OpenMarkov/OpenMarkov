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
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "OnlyUndirectedLinks", defaultBehavior = ConstraintBehavior.NO) public class OnlyUndirectedLinks
		extends PNConstraint {
    
    // Attributes.
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> nodes = probNet.getNodes();
		for (Node node : nodes) {
			// Only check children because with this is enough
			// to look for directed links
            List<Node> children = probNet.getChildren(node);
            if (!children.isEmpty()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyUndirectedLinksCannotHaveChildren(this, node, children));
            }
            List<Node> parents = probNet.getParents(node);
            if (!parents.isEmpty()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyUndirectedLinksCannotHaveParents(this, node, parents));
            }
        }
	}
 
}
