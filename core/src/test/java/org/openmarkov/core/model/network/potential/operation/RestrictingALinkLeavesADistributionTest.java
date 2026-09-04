/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Restricting a link zeroes the combinations it forbids and shares the probability among the
 * states still allowed. When the states still allowed added up to zero, only a child of two
 * states was repaired, and a child of three was left with a column of zeros.
 *
 * @author Manuel Arias
 */
public class RestrictingALinkLeavesADistributionTest {

    private static final double PRECISION = 1e-12;

    /**
     * Builds a parent of two states and a child of {@code statesOfTheChild}, gives the child the
     * values passed, forbids the first state of the child under the first state of the parent, and
     * answers the table of the child.
     */
    private TablePotential afterForbiddingTheFirstCombination(int statesOfTheChild, double... values) {
        Variable parent = new Variable("A", new State[] { new State("A1"), new State("A2") });
        State[] states = new State[statesOfTheChild];
        for (int i = 0; i < statesOfTheChild; i++) {
            states[i] = new State("B" + (i + 1));
        }
        Variable child = new Variable("B", states);

        ProbNet net = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        net.addNode(parent, NodeType.CHANCE);
        Node childNode = new Node(net, child, NodeType.CHANCE);
        net.addNode(childNode);
        net.addLink(parent, child, true);

        TablePotential potential = new TablePotential(Arrays.asList(child, parent),
                PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setValues(values);
        childNode.addPotential(potential);
        net.addPotential(potential);

        Link<Node> link = net.getLinks().getFirst();
        LinkOperations.initializesRestrictionsPotential(link);
        TablePotential restrictions = (TablePotential) link.getRestrictionsPotential();
        restrictions.setValue(restrictions.getVariables(), new int[] { 0, 0 }, 0);
        LinkRestrictionPotentialOperations.updatePotentialByAddLinkRestriction(childNode, restrictions, 0, 0);
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void aChildOfThreeStatesKeepsAColumnThatAddsUpToOne() {
        TablePotential potential = afterForbiddingTheFirstCombination(3, 1, 0, 0, 0.2, 0.3, 0.5);

        assertArrayEquals(new double[] { 0, 0.5, 0.5, 0.2, 0.3, 0.5 }, potential.getValues(), PRECISION,
                "the column of the forbidden state was left at zero");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theForbiddenStateGetsNothing() {
        TablePotential potential = afterForbiddingTheFirstCombination(3, 1, 0, 0, 0.2, 0.3, 0.5);

        assertArrayEquals(new double[] { 0.0 }, new double[] { potential.getValues()[0] }, PRECISION,
                "the state the restriction forbids must keep no probability");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aChildOfTwoStatesIsUnchanged() {
        TablePotential potential = afterForbiddingTheFirstCombination(2, 1, 0, 0.4, 0.6);

        assertArrayEquals(new double[] { 0, 1, 0.4, 0.6 }, potential.getValues(), PRECISION,
                "the only state left takes all the probability, as it already did");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aColumnThatStillAddsUpIsJustNormalized() {
        TablePotential potential = afterForbiddingTheFirstCombination(3, 0.5, 0.2, 0.3, 0.2, 0.3, 0.5);

        assertArrayEquals(new double[] { 0, 0.4, 0.6, 0.2, 0.3, 0.5 }, potential.getValues(), PRECISION,
                "what was left is shared in proportion, not evenly");
    }

    @Tag(TestSpeed.FAST)
    @Test public void everyRemainingStateGetsTheSameShare() {
        TablePotential potential = afterForbiddingTheFirstCombination(4, 1, 0, 0, 0, 0.1, 0.2, 0.3, 0.4);

        assertArrayEquals(new double[] { 0, 1.0 / 3, 1.0 / 3, 1.0 / 3, 0.1, 0.2, 0.3, 0.4 },
                potential.getValues(), PRECISION, "three states left, a third each");
    }
}
