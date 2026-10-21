/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Safety-net tests for {@link DecisionTreeBranch} introduced in Phase 0
 * of the decisiontree refactoring plan.
 */
class DecisionTreeBranchTest {

    private ProbNet probNet;
    private Variable decisionVariable;
    private State yes;
    private State no;
    private Node decisionNode;
    private Node chanceNode;

    @BeforeEach
    void setUp() {
        yes = new State("yes");
        no = new State("no");
        decisionVariable = new Variable("D", new State[]{yes, no});
        Variable chanceVariable = new Variable("C", new State[]{yes, no});

        probNet = new ProbNet();
        decisionNode = probNet.addNode(decisionVariable, NodeType.DECISION);
        chanceNode = probNet.addNode(chanceVariable, NodeType.CHANCE);
    }

    @Test
    void constructorStoresTheStateAndTheVariableIsTheOneOfTheParent() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);
        assertSame(yes, branch.getBranchState());
        assertNull(branch.getBranchVariable(), "a branch that hangs from no node has no variable yet");

        new DecisionTreeNode(decisionNode, probNet).addChild(branch);

        assertSame(decisionVariable, branch.getBranchVariable());
    }

    @Test
    void theRootBranchHasNeitherStateNorVariable() {
        DecisionTreeBranch branch = new DecisionTreeBranch();

        assertNull(branch.getBranchVariable());
        assertNull(branch.getBranchState());
    }

    @Test
    void branchProbabilityIsNotANumberUntilItIsSet() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);
        assertTrue(Double.isNaN(branch.getBranchProbability()));

        branch.setBranchProbability(0.25);
        assertEquals(0.25, branch.getBranchProbability());
    }

    @Test
    void branchProbabilityFromTheEvaluationsIsZeroWhenTheParentScenarioIsZero() {
        DecisionTreeNode parent = new DecisionTreeNode(chanceNode, probNet);
        parent.setEvaluation(new UnicritNodeEvaluation(0.0, 0.0));
        DecisionTreeBranch branch = hangChild(parent, yes, 0.5);

        parent.setBranchProbabilitiesFromEvaluations();

        assertEquals(0.0, branch.getBranchProbability());
    }

    @Test
    void branchProbabilityFromTheEvaluationsIsTheRatioOfTheScenarios() {
        DecisionTreeNode parent = new DecisionTreeNode(chanceNode, probNet);
        parent.setEvaluation(new UnicritNodeEvaluation(0.4, 0.0));
        DecisionTreeBranch first = hangChild(parent, yes, 0.1);
        DecisionTreeBranch second = hangChild(parent, no, 0.3);

        parent.setBranchProbabilitiesFromEvaluations();

        assertEquals(0.25, first.getBranchProbability(), 1e-12);
        assertEquals(0.75, second.getBranchProbability(), 1e-12);
    }

    /** Hangs from the node a branch whose child has the given scenario probability. */
    private DecisionTreeBranch hangChild(DecisionTreeNode parent, State state, double scenarioProbability) {
        DecisionTreeBranch branch = new DecisionTreeBranch(state);
        parent.addChild(branch);
        DecisionTreeNode child = new DecisionTreeNode(decisionNode, probNet);
        child.setEvaluation(new UnicritNodeEvaluation(scenarioProbability, 0.0));
        branch.setChild(child);
        return branch;
    }

    @Test
    void getChildrenIsEmptyWhileTheBranchHasNoChild() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);

        assertTrue(branch.getChildren().isEmpty());
    }

    /**
     * The two kinds of element answer the same question the same way. Consumers walk
     * the tree through {@link DecisionTreeElement#getChildren()} without knowing which
     * one they hold, so "no children" has to look alike from both.
     */
    @Test
    void bothKindsOfElementReportNoChildrenTheSameWay() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);
        DecisionTreeNode node = new DecisionTreeNode(chanceNode, probNet);

        assertEquals(node.getChildren().isEmpty(), branch.getChildren().isEmpty());
    }

    /**
     * What the empty list buys: a walk over a half-built tree reaches its end instead
     * of dereferencing a placeholder. This mirrors the recursion in
     * {@code DecisionTreeManagerImpl.inferenceExpandLevels} and in the GUI's
     * {@code DecisionTreeModel.buildPanelTree}, which is where it used to fail.
     */
    @Test
    void walkingABranchWithNoChildYetDoesNotFail() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);

        assertDoesNotThrow(() -> countElements(branch));
        assertEquals(1, countElements(branch));
    }

    private static int countElements(DecisionTreeElement element) {
        int count = 1;
        for (DecisionTreeElement child : element.getChildren()) {
            count += countElements(child);
        }
        return count;
    }

    @Test
    void setChildLinksBranchAndNode() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);
        DecisionTreeNode child = new DecisionTreeNode(chanceNode, probNet);

        branch.setChild(child);

        assertSame(child, branch.getChild());
        assertEquals(1, branch.getChildren().size());
        assertSame(child, branch.getChildren().get(0));
        assertSame(branch, child.getParentBranch());
    }

    /**
     * Branch with no associated variable (e.g. the synthetic root) must produce
     * a stable, non-throwing string. Fixed in Phase 1 of the decisiontree refactor.
     */
    @Test
    void toStringDoesNotThrowWhenBranchVariableIsNull() {
        assertDoesNotThrow(new DecisionTreeBranch()::toString);
        assertDoesNotThrow(new DecisionTreeBranch(yes)::toString);
    }

    @Test
    void toStringWithBranchVariableContainsVariableName() {
        DecisionTreeBranch branch = new DecisionTreeBranch(yes);
        new DecisionTreeNode(decisionNode, probNet).addChild(branch);

        assertTrue(branch.toString().contains("D"));
    }
}
