/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DecisionTreeNode#isBestDecision} is what paints the green
 * mark on the optimal branch of a decision node. The GUI asks it while
 * painting, so it must answer for a half-built tree too: a branch that has no
 * utility yet is simply not the best one, rather than a
 * {@code NullPointerException} in the renderer.
 *
 * @author Manuel Arias
 */
class DecisionTreeNodeBestDecisionTest {

    private ProbNet probNet;
    private Variable decisionVariable;
    private State yes;
    private State no;
    private DecisionTreeNode decisionTreeNode;

    @BeforeEach
    void setUp() {
        yes = new State("yes");
        no = new State("no");
        decisionVariable = new Variable("D", new State[]{yes, no});
        probNet = new ProbNet();
        Node decisionNode = probNet.addNode(decisionVariable, NodeType.DECISION);
        decisionTreeNode = new DecisionTreeNode(decisionNode, probNet);
    }

    private DecisionTreeBranch branchWithUtility(State state, Double utility) {
        return branchWithEvaluation(state, utility == null ? null : new UnicritNodeEvaluation(1.0, utility));
    }

    /** A branch of the decision whose child has the given evaluation; no child if the evaluation is null. */
    private DecisionTreeBranch branchWithEvaluation(State state, DecisionTreeNodeEvaluation evaluation) {
        DecisionTreeBranch branch = new DecisionTreeBranch(state);
        decisionTreeNode.addChild(branch);
        if (evaluation != null) {
            Variable chance = new Variable("after " + state.getName(), "a", "b");
            Node chanceNode = probNet.addNode(chance, NodeType.CHANCE);
            DecisionTreeNode child = new DecisionTreeNode(chanceNode, probNet);
            child.setEvaluation(evaluation);
            branch.setChild(child);
        }
        return branch;
    }

    @Test
    void theBranchWithTheHighestUtilityIsTheBestDecision() {
        DecisionTreeBranch best = branchWithUtility(yes, 10.0);
        DecisionTreeBranch worst = branchWithUtility(no, 3.0);

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
        DecisionTreeBranch evaluated = branchWithUtility(yes, 10.0);
        DecisionTreeBranch unevaluated = branchWithUtility(no, null);

        assertDoesNotThrow(() -> decisionTreeNode.isBestDecision(unevaluated));
        assertFalse(decisionTreeNode.isBestDecision(unevaluated));
        // And the evaluated branch is judged against the branches that do have a
        // utility, ignoring the pending one instead of failing on it.
        assertDoesNotThrow(() -> decisionTreeNode.isBestDecision(evaluated));
        assertTrue(decisionTreeNode.isBestDecision(evaluated));
    }

    @Test
    void tiedBranchesAreBothTheBestDecision() {
        DecisionTreeBranch first = branchWithUtility(yes, 10.0);
        DecisionTreeBranch second = branchWithUtility(no, 10.0);

        assertTrue(decisionTreeNode.isBestDecision(first));
        assertTrue(decisionTreeNode.isBestDecision(second));
    }

    /**
     * In cost-effectiveness analysis the optimal choice depends on the
     * willingness to pay, so no single branch is marked as the best one.
     */
    @Test
    void noBranchIsTheBestDecisionInCostEffectiveness() throws Exception {
        CEP cep = new CEP(new StrategyTree[]{null}, new double[]{10.0}, new double[]{2.0}, null, 0.0, 100.0);
        DecisionTreeBranch first = branchWithEvaluation(yes, new CENodeEvaluation(1.0, cep));
        DecisionTreeBranch second = branchWithEvaluation(no, new CENodeEvaluation(1.0, cep));

        assertFalse(decisionTreeNode.isBestDecision(first));
        assertFalse(decisionTreeNode.isBestDecision(second));
    }

    /** Only the branches of a decision can be the best decision. */
    @Test
    void noBranchOfAChanceNodeIsTheBestDecision() {
        Variable chance = new Variable("C", "a", "b");
        DecisionTreeNode chanceTreeNode = new DecisionTreeNode(probNet.addNode(chance, NodeType.CHANCE), probNet);
        DecisionTreeBranch branch = new DecisionTreeBranch(chance.getStates()[0]);
        chanceTreeNode.addChild(branch);
        DecisionTreeNode child = new DecisionTreeNode(probNet.addNode(new Variable("U"), NodeType.UTILITY), probNet);
        child.setEvaluation(new UnicritNodeEvaluation(1.0, 10.0));
        branch.setChild(child);

        assertFalse(chanceTreeNode.isBestDecision(branch));
    }
}
