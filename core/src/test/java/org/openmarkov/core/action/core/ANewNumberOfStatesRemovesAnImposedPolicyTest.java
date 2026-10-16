/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Adding or removing a state of a decision with an imposed policy, or of a parent of that decision,
 * removes the policy, whose cells no longer fit the states, so that the decision is optimized.
 *
 * @author Manuel Arias
 */
class ANewNumberOfStatesRemovesAnImposedPolicyTest {

    private ProbNet net;
    private Node nodeX;
    private Node nodeD;

    /** X → D, with the imposed policy D = X. */
    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable x = new Variable("X", "x0", "x1");
        Variable d = new Variable("D", "d0", "d1");
        nodeX = net.addNode(x, NodeType.CHANCE);
        nodeD = net.addNode(d, NodeType.DECISION);
        net.addLink(x, d, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }));
        nodeD.setPotential(new TablePotential(List.of(d, x), PotentialRole.POLICY, new double[] { 1, 0, 0, 1 }));
    }

    @Tag(TestSpeed.FAST)
    @Test void aStateAddedToTheDecision() throws Exception {
        new NodeStateEdit(nodeD, StateAction.ADD, 0, "c").executeEdit();
        assertFalse(nodeD.hasPolicy());
    }

    @Tag(TestSpeed.FAST)
    @Test void aStateRemovedFromTheDecision() throws Exception {
        new NodeStateEdit(nodeD, StateAction.REMOVE, 0, "").executeEdit();
        assertFalse(nodeD.hasPolicy());
    }

    @Tag(TestSpeed.FAST)
    @Test void aStateAddedToAParentOfTheDecision() throws Exception {
        new NodeStateEdit(nodeX, StateAction.ADD, 0, "c").executeEdit();
        assertFalse(nodeD.hasPolicy());
    }

    @Tag(TestSpeed.FAST)
    @Test void undoingGivesBackThePolicy() throws Exception {
        net.getPNESupport().setWithUndo(true);
        new NodeStateEdit(nodeD, StateAction.ADD, 0, "c").executeEdit();
        net.getPNESupport().undo();
        assertArrayEquals(new double[] { 1, 0, 0, 1 }, ((TablePotential) nodeD.getPotentials().getFirst()).getValues());
    }
}
