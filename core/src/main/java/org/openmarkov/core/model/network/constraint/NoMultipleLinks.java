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
 * This class implements the NoMultipleLinks constraint, which establishes the following rules:
 * - The undirected link between A and B is both incompatible with any directed link between A and B.
 * - The directed link between A and B is compatible with the directed link between B and A
 *
 * @author ckonig
 */
@Constraint(name = "NoMultipleLinks", defaultBehavior = ConstraintBehavior.YES)
public class NoMultipleLinks extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Node> nodesGraph = probNet.getNodes();
        for (Node node : nodesGraph) {
            for (Link<Node> link : probNet.getLinks(node)) {
                this.checkLink(probNet, constraintChecker, link.getFrom(), link.getTo(), link.isDirected());
            }
        }
    }
    
    /*****
     * Checks if a link between node1 and node2
     * satisfies the restriction of noMultipleLinks
     * @param probNet Network
     * @param constraintChecker the constraint checker
     * @param node1 First node
     * @param node2 Second node
     * @param directed - true if the link is directed
     */
    public void checkLink(GraphNetwork probNet, ConstraintChecker constraintChecker, Node node1, Node node2, boolean directed) {
        if (directed) {
            this.checkDirectedLink(probNet, constraintChecker, node1, node2);
        } else {
            this.checkUndirectedLink(probNet, constraintChecker, node1, node2);
        }
    }
    
    /*********
     * Checks if a directed link between {@code node1} and {@code node2} satisfies the restriction of noMultipleLinks
     * @param probNet Network
     * @param constraintChecker the constraint checker
     * @param node1 First node
     * @param node2 Second node
     * @return {@code true} if the link between {@code node1} and {@code node2}has no multipleLinks
     */
    private void checkDirectedLink(GraphNetwork probNet, ConstraintChecker constraintChecker, Node node1, Node node2) {
        if (probNet.getLink(node1, node2, false) != null) {
            constraintChecker.addException(new ConstraintViolatedException.DirectedLinkCannotMatchAnUndirectedLink(this, node1, node2));
        }
    }
    
    /*****
     * Checks if a undirected link between {@code node1} and
     * {@code node2} satisfies the restriction of noMultipleLinks
     * @param probNet Network
     * @param constraintChecker the constraint checker
     * @param node1 First node
     * @param node2 Second node
     * @return {@code true} if the link between {@code node1} and
     *         {@code node2}has no multipleLinks
     */
    private void checkUndirectedLink(GraphNetwork probNet, ConstraintChecker constraintChecker, Node node1, Node node2) {
        // neither a directed link from node1 -> node2 nor node2 -> node1 may exist
        if (probNet.getLink(node1, node2, true) != null) {
            constraintChecker.addException(new ConstraintViolatedException.DirectedLinkCannotMatchAnUndirectedLink(this, node1, node2));
        }
        if (probNet.getLink(node2, node1, true) != null) {
            constraintChecker.addException(new ConstraintViolatedException.DirectedLinkCannotMatchAnUndirectedLink(this, node2, node1));
        }
    }
    
}