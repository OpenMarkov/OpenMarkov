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

/**
 * This class implements the DistinctLinks constraint, which establishes that the network
 * can not have two equal links.
 *
 * @author ckonig
 * @author manuel arias
 *
 */
@Constraint(name = "DistinctLinks", defaultBehavior = ConstraintBehavior.YES)
public class DistinctLinks extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Node> nodes = probNet.getNodes();
        for (Node node : nodes) {
            boolean linksAreValid = probNet.getNumLinks(node) <= (
                    probNet.getNumChildren(node) + probNet.getNumParents(node) + probNet.getNumSiblings(node)
            );
            if (!linksAreValid) {
                constraintChecker.addException(new ConstraintViolatedException.NodeHasRepeatedLinks(this, node));
            }
        }
    }
    
    
    public static boolean checkLink(GraphNetwork graph, Node node1, Node node2, boolean directed) {
        return !(
                (graph.getLink(node1, node2, directed) != null) || (
                        !directed && graph.getLink(node2, node1, directed) != null
                )
        );
    }
    
}
