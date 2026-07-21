/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.validator;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/******
 * This class validates if a chance or decision node can be absorbed into an utility node.
 *
 * @author iagoparís - summer 2018
 *
 */
public class AbsorbNodeValidator {
    // The node is of decision or chance and has only a utility child
    public static boolean validate(Node node) {

        // Test if decision or chance
        boolean isDecisionOrChance = node.getNodeType() == NodeType.CHANCE || node.getNodeType() == NodeType.DECISION;
        if (!isDecisionOrChance) {
            return false;
        }

        // Test if only utility children and no grandchildren
        List<Node> children = node.getChildren();
        if (children.isEmpty()) {
            return false;
        } else {
            for (Node child : children) {
                if (child.getNodeType() != NodeType.UTILITY || !child.getChildren().isEmpty()) {
                    return false;
                }
            }
        }

        // For decision nodes: all other parents of the utility children must be
        // informational predecessors of this decision. Otherwise, maximizing over
        // the decision assumes knowledge the decision maker does not have. (Issue #506)
        if (node.getNodeType() == NodeType.DECISION) {
            List<Variable> infoPredecessors = ProbNetOperations.getInformationalPredecessors(
                    node.getProbNet(), node.getVariable());
            for (Node child : children) {
                for (Node parent : child.getParents()) {
                    if (parent != node && !infoPredecessors.contains(parent.getVariable())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
