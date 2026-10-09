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
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Every directed cycle of more than one node contains at least one event node.
 *
 * @author Manuel Arias
 */
@Constraint(name = "EveryCycleHasAnEvent", defaultBehavior = ConstraintBehavior.NO)
public class EveryCycleHasAnEvent extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node parent : probNet.getNodes()) {
            for (Node child : probNet.getChildren(parent)) {
                if (closesACycleWithoutEvents(probNet, parent, child, null, null)) {
                    constraintChecker.addException(
                            new ConstraintViolatedException.CycleWithoutAnEvent(this, parent, child));
                }
            }
        }
    }

    /**
     * @param ignoredParent with {@code ignoredChild}, a link that is taken as absent; null if there is none
     * @return whether a link from {@code parent} to {@code child} is part of a cycle with no event node
     */
    public static boolean closesACycleWithoutEvents(GraphNetwork probNet, Node parent, Node child,
                                                    Node ignoredParent, Node ignoredChild) {
        if (parent == child || isEvent(parent) || isEvent(child)) {
            return false;
        }
        Set<Node> visited = new HashSet<>();
        Deque<Node> pending = new ArrayDeque<>();
        pending.add(child);
        while (!pending.isEmpty()) {
            Node node = pending.remove();
            if (node == parent) {
                return true;
            }
            if (visited.add(node)) {
                for (Node next : probNet.getChildren(node)) {
                    if (!isEvent(next) && !(node == ignoredParent && next == ignoredChild)) {
                        pending.add(next);
                    }
                }
            }
        }
        return false;
    }

    private static boolean isEvent(Node node) {
        return node.getNodeType() == NodeType.EVENT;
    }
}
