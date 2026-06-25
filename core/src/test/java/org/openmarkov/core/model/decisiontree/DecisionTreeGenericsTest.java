/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Phase 2 of the decisiontree refactor: verifies that {@link DecisionTreeBranch}
 * is properly parametrised with the same {@code <T>} as
 * {@link DecisionTreeNode}, so {@code getUtility()}, {@code getChild()} and
 * {@code getParent()} return strongly-typed values without casts.
 *
 * <p>If any of these tests start to require an unchecked cast to compile, the
 * generic typing has regressed.
 */
class DecisionTreeGenericsTest {

    @Test
    void branchOfDoubleReturnsDoubleUtilityWithoutCast() {
        ProbNet probNet = new ProbNet();
        Variable decision = new Variable("D", new State[]{new State("a"), new State("b")});
        Variable chance = new Variable("C", new State[]{new State("a"), new State("b")});
        Node decisionNode = probNet.addNode(decision, NodeType.DECISION);
        Node chanceNode = probNet.addNode(chance, NodeType.CHANCE);

        DecisionTreeBranch<Double> branch = new DecisionTreeBranch<>(probNet,
                decision, decision.getStates()[0]);
        StubDecisionTreeNode<Double> node = new StubDecisionTreeNode<>(chanceNode, probNet);
        node.setUtility(7.5);
        branch.setChild(node);

        // Direct typed access; no cast needed.
        Double utility = branch.getUtility();
        assertEquals(7.5, utility);

        DecisionTreeNode<Double> typedChild = branch.getChild();
        assertSame(node, typedChild);
    }

    @Test
    void branchOfStringReturnsStringUtilityWithoutCast() {
        // A different T proves the generic propagates through both Node and Branch.
        ProbNet probNet = new ProbNet();
        Variable variable = new Variable("X", new State[]{new State("a"), new State("b")});
        Node node = probNet.addNode(variable, NodeType.UTILITY);

        StubDecisionTreeNode<String> stub = new StubDecisionTreeNode<>(node, probNet);
        stub.setUtility("answer");

        DecisionTreeBranch<String> branch = new DecisionTreeBranch<>(probNet);
        branch.setChild(stub);

        String utility = branch.getUtility();
        assertEquals("answer", utility);
    }

    @Test
    void parentAccessorIsTyped() {
        ProbNet probNet = new ProbNet();
        Variable v = new Variable("V", new State[]{new State("a"), new State("b")});
        Node node = probNet.addNode(v, NodeType.DECISION);

        StubDecisionTreeNode<Double> parent = new StubDecisionTreeNode<>(node, probNet);
        DecisionTreeBranch<Double> branch = new DecisionTreeBranch<>(probNet);
        parent.addChild(branch);

        DecisionTreeNode<Double> typedParent = branch.getParent();
        assertSame(parent, typedParent);
    }
}
