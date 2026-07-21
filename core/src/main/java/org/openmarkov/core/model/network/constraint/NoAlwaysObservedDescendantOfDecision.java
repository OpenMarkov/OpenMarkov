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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.Collections;
import java.util.List;

@Constraint(name = "NoAlwaysObservedDescendantOfDecision", defaultBehavior = ConstraintBehavior.YES)
public class NoAlwaysObservedDescendantOfDecision
        extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
        List<Node> alwaysObservedNodes = probNet.getNodes().stream().filter(Node::isAlwaysObserved).toList();
        for (Node alwaysObservedNode : alwaysObservedNodes) {
            for (Node ancestorDecisionNode : NoAlwaysObservedDescendantOfDecision.ancestorInList(probNet, alwaysObservedNode, decisionNodes)) {
                constraintChecker.addException(new ConstraintViolatedException.AlwaysObservedVariableIsDescendantOfDecisionNode(this, alwaysObservedNode, ancestorDecisionNode));
            }
        }
    }
    
    /**
     * @param node  Node
     * @param nodes Nodes
     *
     * @return true if 'node' has some ancestor in 'nodes' (considering
     * direction of the links)
     */
    public static List<Node> ancestorInList(GraphNetwork network, Node node, List<Node> nodes) {
        return nodes.stream().filter(networkNode -> isReachable(network, networkNode, node)).toList();
    }
    
    public static List<Node> descendantInList(GraphNetwork network, Node node, List<Node> nodes) {
        return nodes.stream().filter(networkNode -> isReachable(network, node, networkNode)).toList();
    }
    
    private static boolean isReachable(GraphNetwork network, Node node1, Node node2) {
        return network.existsPath(node1, node2, true, Collections.emptyList());
    }
    
}
