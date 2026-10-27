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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

/**
 * The chance nodes of a network of events have finite states or are numeric, not divided into intervals: the
 * simulation takes the value of a chance node as a state or as a number, and nothing says which number stands for an
 * interval (error 106 of the pending list).
 *
 * @author Manuel Arias
 */
@Constraint(name = "NoDiscretizedChanceNodes", defaultBehavior = ConstraintBehavior.NO)
public class NoDiscretizedChanceNodes extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node node : probNet.getNodes()) {
            if (!admits(node.getNodeType(), node.getVariable().getVariableType())) {
                constraintChecker.addException(new ConstraintViolatedException.DiscretizedChanceNode(this, node));
            }
        }
    }

    /** @return whether a node of {@code nodeType} can have a variable of {@code variableType} */
    public static boolean admits(NodeType nodeType, VariableType variableType) {
        return nodeType != NodeType.CHANCE || variableType != VariableType.DISCRETIZED;
    }
}
