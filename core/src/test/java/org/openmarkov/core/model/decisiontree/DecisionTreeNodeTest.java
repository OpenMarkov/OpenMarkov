/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Safety-net tests for {@link DecisionTreeNode} introduced in Phase 0
 * of the decisiontree refactoring plan.
 *
 * <p>These tests freeze current behaviour so later refactoring phases
 * can be validated against this baseline.
 */
class DecisionTreeNodeTest {

    private ProbNet probNet;
    private Variable decisionVariable;
    private Variable chanceVariable;
    private Node decisionNode;
    private Node chanceNode;

    @BeforeEach
    void setUp() {
        State yes = new State("yes");
        State no = new State("no");
        decisionVariable = new Variable("D", new State[]{yes, no});
        chanceVariable = new Variable("C", new State[]{yes, no});

        probNet = new ProbNet();
        decisionNode = probNet.addNode(decisionVariable, NodeType.DECISION);
        chanceNode = probNet.addNode(chanceVariable, NodeType.CHANCE);
    }

    @Test
    void constructorFromNodeAndNetworkSetsBothFields() {
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(decisionNode, probNet);

        assertSame(decisionVariable, node.getVariable());
        assertEquals(NodeType.DECISION, node.getNodeType());
        assertSame(probNet, node.getNetwork());
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    void constructorFromNodeOnlyLeavesNetworkNull() {
        // Documents existing behaviour: the (Node) constructor does not set network.
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(chanceNode);

        assertSame(chanceVariable, node.getVariable());
        assertNull(node.getNetwork());
    }

    @Test
    void constructorFromVariableResolvesNodeFromNetwork() {
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(chanceVariable, probNet);

        assertSame(chanceVariable, node.getVariable());
        assertEquals(NodeType.CHANCE, node.getNodeType());
        assertSame(probNet, node.getNetwork());
    }

    @Test
    void addChildLinksParentAndChildSymmetrically() {
        StubDecisionTreeNode<Double> parent = new StubDecisionTreeNode<>(decisionNode, probNet);
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet);

        parent.addChild(branch);

        List<DecisionTreeElement> children = parent.getChildren();
        assertEquals(1, children.size());
        assertSame(branch, children.get(0));
        // setParent should have been called as part of addChild
        assertSame(parent, branch.getParent());
    }

    @Test
    void getBranchStatesWithoutParentReturnsEmptyEvidence() {
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(decisionNode, probNet);

        EvidenceCase evidence = node.getBranchStates();

        assertNotNull(evidence);
        assertTrue(evidence.getFindings().isEmpty());
    }

    @Test
    void getBranchStatesWithParentDelegates() {
        StubDecisionTreeNode<Double> root = new StubDecisionTreeNode<>(decisionNode, probNet);
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable,
                decisionVariable.getStates()[0]);
        root.addChild(branch);

        StubDecisionTreeNode<Double> child = new StubDecisionTreeNode<>(chanceNode, probNet);
        branch.setChild(child);

        EvidenceCase evidence = child.getBranchStates();
        // Inherited from branch -> includes the finding D=yes
        assertEquals(1, evidence.getFindings().size());
        assertSame(decisionVariable, evidence.getFindings().get(0).getVariable());
    }

    @Test
    void scenarioProbabilityRoundTrip() {
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(decisionNode, probNet);

        // Default sentinel
        assertEquals(Double.NEGATIVE_INFINITY, node.getScenarioProbability());

        node.setScenarioProbability(0.42);
        assertEquals(0.42, node.getScenarioProbability());
    }

    @Test
    void copyTransfersStateFromOtherNode() {
        StubDecisionTreeNode<Double> source = new StubDecisionTreeNode<>(decisionNode, probNet);
        source.setUtility(7.5);
        source.setScenarioProbability(0.3);
        UnicritNodeEvaluation evaluation = new UnicritNodeEvaluation();
        evaluation.setProb(0.3);
        evaluation.setUtility(7.5);
        source.setEvaluation(evaluation);

        StubDecisionTreeNode<Double> dest = new StubDecisionTreeNode<>(chanceNode, probNet);
        dest.copy(source);

        assertEquals(7.5, dest.getUtility());
        assertEquals(0.3, dest.getScenarioProbability());
        assertSame(decisionVariable, dest.getVariable());
        assertEquals(NodeType.DECISION, dest.getNodeType());
        assertSame(probNet, dest.getNetwork());
        // The evaluation is state like any other: a copy that keeps the plain
        // numbers but drops the evaluation leaves the node contradicting itself.
        assertSame(evaluation, dest.getEvaluation());
    }

    @Test
    void copyDoesNotShareChildrenListWithSource() {
        StubDecisionTreeNode<Double> source = new StubDecisionTreeNode<>(decisionNode, probNet);
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet);
        source.addChild(branch);

        StubDecisionTreeNode<Double> dest = new StubDecisionTreeNode<>(chanceNode, probNet);
        dest.copy(source);

        // Mutating the source list after copy must not leak into the destination.
        DecisionTreeBranch extra = new DecisionTreeBranch(probNet);
        source.addChild(extra);

        assertEquals(1, dest.getChildren().size());
        assertEquals(2, source.getChildren().size());
    }

    /**
     * The one use of {@code copy} is grafting a fresh subtree onto a leaf of an
     * existing tree, so the destination keeps its own place: its parent link is
     * untouched and the evidence accumulated along the path above it survives the
     * graft. The previous contract reset the parent to null and expected the
     * caller to re-attach; the only caller never did, and the grafted node came
     * back with empty path evidence.
     */
    @Test
    void copyKeepsTheDestinationsPlaceInTheTree() {
        StubDecisionTreeNode<Double> root = new StubDecisionTreeNode<>(decisionNode, probNet);
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable,
                decisionVariable.getStates()[0]);
        root.addChild(branch);

        StubDecisionTreeNode<Double> dest = new StubDecisionTreeNode<>(chanceNode, probNet);
        branch.setChild(dest);   // dest hangs from the tree: D=yes on its path

        StubDecisionTreeNode<Double> source = new StubDecisionTreeNode<>(chanceNode, probNet);
        dest.copy(source);

        assertEquals(1, dest.getBranchStates().getFindings().size(),
                "the path evidence above the grafted node must survive the graft");
    }

    /**
     * The shared children must point at the node they now hang from. If they kept
     * the source as parent, the source — an orphan outside the tree — would stay
     * reachable, and the path evidence of the grafted subtree would be computed
     * through it.
     */
    @Test
    void copyReparentsTheSharedChildrenToTheDestination() {
        StubDecisionTreeNode<Double> source = new StubDecisionTreeNode<>(decisionNode, probNet);
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable,
                decisionVariable.getStates()[0]);
        source.addChild(branch);

        StubDecisionTreeNode<Double> dest = new StubDecisionTreeNode<>(chanceNode, probNet);
        dest.copy(source);

        assertSame(dest, ((DecisionTreeBranch<?>) dest.getChildren().get(0)).getParent());
    }

    @Test
    void utilityGetterAndSetterRoundTrip() {
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(decisionNode, probNet);

        assertNull(node.getUtility());
        node.setUtility(3.14);
        assertEquals(3.14, node.getUtility());
    }

    @Test
    void toStringContainsVariableName() {
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(decisionNode, probNet);
        assertTrue(node.toString().contains("D"));
    }
}
