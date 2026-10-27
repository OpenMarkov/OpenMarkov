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
import org.openmarkov.core.model.network.VariableType;

import java.util.List;
import java.util.stream.Stream;

/******
 * This class validates if a chance or decision node can be absorbed into an utility node.
 *
 * @author iagoparís - summer 2018
 *
 */
public class AbsorbNodeValidator {
    /** @return whether the node can be absorbed into its utility children */
    public static boolean validate(Node node) {
        return hasTheShape(node) && noVariableIsNumeric(node);
    }

    /**
     * @return whether the node is of chance or decision and all its children are utility nodes without children; for
     * a decision, those utilities can only depend on what is known when deciding
     */
    public static boolean hasTheShape(Node node) {

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

    /**
     * Absorbing turns into tables the relation of the node and those of its utility children, so none of their
     * variables can be numeric, though a numeric one divided into intervals can: the parents of the node, and those of
     * its utilities, the node among them. Their parents do not need a table of their own, as a decision has none
     * (error 301 of the pending list).
     */
    public static boolean noVariableIsNumeric(Node node) {
        return Stream.concat(node.getParents().stream(),
                             node.getChildren().stream().flatMap(child -> child.getParents().stream()))
                     .noneMatch(variableNode -> variableNode.getVariable().getVariableType() == VariableType.NUMERIC);
    }
}
