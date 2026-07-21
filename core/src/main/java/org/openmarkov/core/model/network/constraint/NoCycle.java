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

import java.util.Collections;
import java.util.List;

@Constraint(name = "NoCycle", defaultBehavior = ConstraintBehavior.YES) public class NoCycle extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		for (Node parent : probNet.getNodes()) {
			List<Node> children = probNet.getChildren(parent);
			for (Node child : children) {
				if (probNet.existsPath(child, parent, true, Collections.emptyList())) {
                    constraintChecker.addException(new ConstraintViolatedException.ThereIsACycle(this, parent, child));
				}
			}
		}
	}
 
}