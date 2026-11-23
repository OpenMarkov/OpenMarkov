/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
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

/**
 * No node has more than one potential: its table, its tree or its canonical model.
 *
 * @author Manuel Arias
 */
@Constraint(name = "OneRelationPerNode", defaultBehavior = ConstraintBehavior.YES)
public class OneRelationPerNode extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node node : probNet.getNodes()) {
            if (node.getNumPotentials() > 1) {
                constraintChecker.addException(
                        new ConstraintViolatedException.NodeHasMoreThanOneRelation(this, node, node.getNumPotentials()));
            }
        }
    }

}
