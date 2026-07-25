/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 * Forbids loops: a closed path leaving the orientation of the links aside. A network with this
 * constraint is a poly-tree — between any two nodes there is at most one path.
 * <p>
 * A link closes a loop when its two ends are <em>already</em> joined by some other path. That is the
 * whole check, and it is asked of the network without touching it, by having {@code existsPath}
 * ignore the very link being judged.
 * <p>
 * It used to be done the other way round: the link was <strong>removed</strong> from the network, the
 * path looked for, and the link added back — except that it was added back the other way round
 * ({@code removeLink(node2, node1)} … {@code addLink(node1, node2)}), so every arc of the network came
 * out reversed. As this check runs during ordinary validation, validating a network left it corrupted,
 * in silence, and a constraint is precisely what must not modify what it validates.
 */
@Constraint(name = "NoLoops", defaultBehavior = ConstraintBehavior.OPTIONAL) public class NoLoops extends PNConstraint {

	@Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		for (Link<Node> link : probNet.getLinks()) {
			if (probNet.existsPath(link.getFrom(), link.getTo(), false, List.of(link))) {
				constraintChecker.addException(
						new ConstraintViolatedException.ThereIsALoop(this, link.getFrom(), link.getTo()));
			}
		}
	}

}
