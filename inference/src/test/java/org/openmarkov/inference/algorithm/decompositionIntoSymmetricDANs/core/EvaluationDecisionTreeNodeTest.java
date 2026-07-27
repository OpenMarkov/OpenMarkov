/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EvaluationDecisionTreeNode#isBestDecision} is what paints the green
 * mark on the optimal branch of a decision node. The GUI asks it while
 * painting, so it must answer for a half-built tree too: a branch that has no
 * utility yet is simply not the best one, rather than a
 * {@code NullPointerException} in the renderer.
 *
 * @author Manuel Arias
 */
class EvaluationDecisionTreeNodeTest {

    private ProbNet probNet;
    private Variable decisionVariable;
    private State yes;
    private State no;
    private EvaluationDecisionTreeNode decisionTreeNode;

    @BeforeEach
    void setUp() {
        yes = new State("yes");
        no = new State("no");
        decisionVariable = new Variable("D", new State[]{yes, no});
        probNet = new ProbNet();
        Node decisionNode = probNet.addNode(decisionVariable, NodeType.DECISION);
        decisionTreeNode = new EvaluationDecisionTreeNode(decisionNode, probNet);
    }

    private DecisionTreeBranch<Double> branchWithUtility(State state, Double utility) {
        DecisionTreeBranch<Double> branch = new DecisionTreeBranch<>(probNet, decisionVariable, state);
        decisionTreeNode.addChild(branch);
        if (utility != null) {
            Variable chance = new Variable("after " + state.getName(), "a", "b");
            Node chanceNode = probNet.addNode(chance, NodeType.CHANCE);
            EvaluationDecisionTreeNode child = new EvaluationDecisionTreeNode(chanceNode, probNet);
            child.setUtility(utility);
            branch.setChild(child);
        }
        return branch;
    }

    @Test
    void theBranchWithTheHighestUtilityIsTheBestDecision() {
        DecisionTreeBranch<Double> best = branchWithUtility(yes, 10.0);
        DecisionTreeBranch<Double> worst = branchWithUtility(no, 3.0);

        assertTrue(decisionTreeNode.isBestDecision(best));
        assertFalse(decisionTreeNode.isBestDecision(worst));
    }

    /**
     * A branch without a child has no utility yet. Asking whether it is the best
     * one must answer "no", not throw: the previous code unboxed the missing
     * utility and died in the renderer, far from the half-built branch.
     */
    @Test
    void aBranchWithoutUtilityIsNotTheBestDecisionAndDoesNotThrow() {
        DecisionTreeBranch<Double> evaluated = branchWithUtility(yes, 10.0);
        DecisionTreeBranch<Double> unevaluated = branchWithUtility(no, null);

        assertDoesNotThrow(() -> decisionTreeNode.isBestDecision(unevaluated));
        assertFalse(decisionTreeNode.isBestDecision(unevaluated));
        // And the evaluated branch is judged against the branches that do have a
        // utility, ignoring the pending one instead of failing on it.
        assertDoesNotThrow(() -> decisionTreeNode.isBestDecision(evaluated));
        assertTrue(decisionTreeNode.isBestDecision(evaluated));
    }
}
