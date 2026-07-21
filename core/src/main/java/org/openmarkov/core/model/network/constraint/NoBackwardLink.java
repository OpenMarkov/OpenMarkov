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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "NoBackwardLinks", defaultBehavior = ConstraintBehavior.YES) public class NoBackwardLink
        extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Node> probNetNodes = probNet.getNodes();
        for (Node node : probNetNodes) {
            // If the node is temporal
            Variable parentVariable = probNet.getVariable(node.getName());
            if (parentVariable.isTemporal()) {
                // We retrieve its children
                List<Node> children = probNet.getChildren(node);
                // and we iterate over them
                for (Node child : children) {
                    // checking if there is any not allowed link
                    Variable childVariable = probNet.getVariable(child.getName());
                    if (!NoBackwardLink.allowedLink(parentVariable, childVariable)) {
                        constraintChecker.addException(new ConstraintViolatedException.CannotAddLinkToAPreviousTimeSlice(this, parentVariable, childVariable));
                    }
                }
            }
            
        }
    }
    
    public static boolean allowedLink(Variable variable1, Variable variable2) {
        // If both variables are temporal, the second must not belong to a previous time slices
        // And the first is temporal and the second is not, the former must belong to the zeroth slice
        boolean v1IsAtemporal = !variable1.isTemporal();
        boolean v2IsAtemporal = !variable2.isTemporal();
        return v1IsAtemporal || v2IsAtemporal || variable2.getTimeSlice() >= variable1.getTimeSlice();
    }
    
}
