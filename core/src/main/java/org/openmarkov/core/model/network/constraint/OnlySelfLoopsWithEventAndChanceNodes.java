package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

/**
 * This constrain only allows self loops in event nodes. Self loop is a directed edge which the same origin and destination
 *
 * @author cmyago - cmyago adapted it from NoSelfLoop - 31/12/2019 - this constraint is only used in DESNets.
 * 04/10/2023 FI
 * @version 1.1 - self-loops in Chance and Event nodes - 05/04/2020
 */
@Constraint(name = "OnySelfLoopsWithEventAndChanceNodes", defaultBehavior = ConstraintBehavior.NO)
public class OnlySelfLoopsWithEventAndChanceNodes extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node node : probNet.getNodes()) {
            if (probNet.isChild(node, node) && !((node.getNodeType() == NodeType.EVENT) || (node.getNodeType() == NodeType.CHANCE))) {
                constraintChecker.addException(new ConstraintViolatedException.OnlySelfLoopsWithEventAndChanceNodesException(this, node));
            }
        }
    }
}
