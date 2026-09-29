/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Moving a state of a variable reorders every potential that mentions it: all the potentials of its
 * node and of its children, an imposed policy among them.
 *
 * @author Manuel Arias
 */
class MovingAStateReordersEveryPotentialTest {

    private static double[] valuesOf(Potential potential) {
        return ((TablePotential) potential).getValues();
    }

    /** A has two potentials, as some networks of the repository do. */
    @Tag(TestSpeed.FAST)
    @Test void aNodeKeepsAllItsPotentials() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable a = new Variable("A", "a0", "a1");
        Node node = net.addNode(a, NodeType.CHANCE);
        node.setPotentials(List.of(
                new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }),
                new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.1, 0.9 })));

        new NodeStateEdit(node, StateAction.DOWN, 0, "").executeEdit();

        List<Potential> potentials = node.getPotentials();
        assertEquals(2, potentials.size());
        assertArrayEquals(new double[] { 0.7, 0.3 }, valuesOf(potentials.get(0)));
        assertArrayEquals(new double[] { 0.9, 0.1 }, valuesOf(potentials.get(1)));
    }

    /** B → D; the imposed policy chooses d1 when B is b0, and d0 when B is b1. */
    @Tag(TestSpeed.FAST)
    @Test void anImposedPolicyFollowsTheStatesOfItsParent() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable b = new Variable("B", "b0", "b1");
        Variable d = new Variable("D", "d0", "d1");
        Node nodeB = net.addNode(b, NodeType.CHANCE);
        Node nodeD = net.addNode(d, NodeType.DECISION);
        net.addLink(b, d, true);
        nodeB.setPotential(new TablePotential(List.of(b), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.4, 0.6 }));
        nodeD.setPotential(new TablePotential(List.of(d, b), PotentialRole.POLICY, new double[] { 0, 1, 1, 0 }));

        new NodeStateEdit(nodeB, StateAction.DOWN, 0, "").executeEdit();

        assertArrayEquals(new double[] { 1, 0, 0, 1 }, valuesOf(nodeD.getPotentials().getFirst()));
    }
}
