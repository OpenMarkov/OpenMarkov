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

/**
 * No link of the network may carry a restrictions potential. Link restrictions
 * belong to the network types that model structural asymmetry — decision
 * analysis networks and their relatives, which override this constraint to
 * {@code NO} — and mean nothing to the algorithms of the other types: an
 * evaluation that does not read restrictions explores branches the restriction
 * says are impossible.
 * <p>
 * The body was a {@code TODO} stub for years, so the application announced the
 * guarantee without imposing it.
 */
@Constraint(name = "NoLinkRestriction", defaultBehavior = ConstraintBehavior.YES) public class NoLinkRestriction
		extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Link<Node> link : probNet.getLinks()) {
            if (link.hasRestrictions()) {
                constraintChecker.addException(new ConstraintViolatedException.NetworkCannotHaveLinkRestrictions(this,
                        link.getFrom().getVariable(), link.getTo().getVariable()));
            }
        }
	}

}
