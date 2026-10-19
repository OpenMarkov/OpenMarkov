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
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The interval that «Add» puts in what a link reveals starts at the upper limit of the previous one: closed if the
 * previous one leaves that number out, and open if it takes it, so that the number falls in exactly one of them
 * (error 314). The first one goes from minus infinity to infinity, as before.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANewRevealingIntervalStartsWhereThePreviousOneEndsTest {

    private Link<Node> link;

    @BeforeEach void setUp() {
        ProbNet network = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        Node age = network.addNode(new Variable("Age", true, 0, 100, true, 1), NodeType.CHANCE);
        Node test = network.addNode(new Variable("Test", "negative", "positive"), NodeType.CHANCE);
        network.addLink(age, test, true);
        link = network.getLink(age, test, true);
    }

    private PartitionedInterval interval(int row) {
        return link.getRevealingIntervals().get(row);
    }

    /** Adds the first interval, ends it at 5 and adds the second one. */
    private void twoIntervalsCutAt5(boolean fiveInTheFirst) throws Exception {
        new RevelationIntervalEdit(link, StateAction.ADD, 0, 0, false).executeEdit();
        new RevelationIntervalEdit(link, StateAction.MODIFY_VALUE_INTERVAL, 0, 5, false).executeEdit();
        if (fiveInTheFirst) {
            new RevelationIntervalEdit(link, StateAction.MODIFY_DELIMITER_INTERVAL, 0, 0, false).executeEdit();
        }
        new RevelationIntervalEdit(link, StateAction.ADD, 1, 0, false).executeEdit();
    }

    @Test void theFirstIntervalGoesFromMinusInfinityToInfinityAsBefore() throws Exception {
        new RevelationIntervalEdit(link, StateAction.ADD, 0, 0, false).executeEdit();

        assertEquals(Double.NEGATIVE_INFINITY, interval(0).getMin());
        assertEquals(Double.POSITIVE_INFINITY, interval(0).getMax());
        assertFalse(interval(0).getBelongsToLeftSide(0));
        assertFalse(interval(0).getBelongsToLeftSide(1));
    }

    @Test void afterAnIntervalThatLeavesTheLimitOutTheNewOneTakesIt() throws Exception {
        twoIntervalsCutAt5(false);

        assertFalse(interval(0).contains(5));
        assertTrue(interval(1).contains(5));
    }

    @Test void afterAnIntervalThatTakesTheLimitTheNewOneLeavesItOut() throws Exception {
        twoIntervalsCutAt5(true);

        assertTrue(interval(0).contains(5));
        assertFalse(interval(1).contains(5));
    }
}
