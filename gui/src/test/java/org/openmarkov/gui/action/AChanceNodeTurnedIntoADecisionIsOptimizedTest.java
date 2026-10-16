/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A chance node turned into a decision does not keep its conditional probability table as an
 * imposed policy: the decision is optimized.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AChanceNodeTurnedIntoADecisionIsOptimizedTest {

    private ProbNet net;
    private Variable c;
    private Node nodeC;

    /** A → C → U, with P(C | A) = (0.3, 0.7; 0.6, 0.4) and U(C) = (10, 20). */
    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable a = new Variable("A", "a0", "a1");
        c = new Variable("C", "c0", "c1");
        Variable u = new Variable("U");
        Node nodeA = net.addNode(a, NodeType.CHANCE);
        nodeC = net.addNode(c, NodeType.CHANCE);
        Node nodeU = net.addNode(u, NodeType.UTILITY);
        Criterion criterion = new Criterion("Health", "QALY");
        net.setDecisionCriteria(new ArrayList<>(List.of(criterion)));
        u.setDecisionCriterion(criterion);
        net.addLink(a, c, true);
        net.addLink(c, u, true);
        nodeA.setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5, 0.5 }));
        nodeC.setPotential(new TablePotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7, 0.6, 0.4 }));
        nodeU.setPotential(new TablePotential(List.of(u, c), PotentialRole.UNSPECIFIED, new double[] { 10, 20 }));
    }

    @Test void theDecisionHasNoImposedPolicy() throws Exception {
        new ChangeNodeTypeEdit(nodeC, NodeType.DECISION).executeEdit();

        assertFalse(nodeC.hasPolicy());
        VEEvaluation evaluation = new VEEvaluation(net);
        assertTrue(evaluation.getOptimalPolicies().containsKey(c));
        assertEquals(20.0, evaluation.getUtility().getValues()[0], 1e-9);
    }

    @Test void undoingGivesBackTheChanceNodeAndItsTable() throws Exception {
        net.getPNESupport().setWithUndo(true);
        new ChangeNodeTypeEdit(nodeC, NodeType.DECISION).executeEdit();
        net.getPNESupport().undo();

        assertEquals(NodeType.CHANCE, nodeC.getNodeType());
        assertArrayEquals(new double[] { 0.3, 0.7, 0.6, 0.4 }, ((TablePotential) nodeC.getPotential()).getValues());
    }
}
