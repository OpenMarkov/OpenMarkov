/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The utility node of the next cycle keeps the criterion of the one it comes from, not a copy, so that an expanded
 * model has one criterion object for each criterion of the network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AShiftedUtilityNodeKeepsItsCriterionTest {

    @Test void theCriterionIsTheSameObject() {
        ProbNet net = new ProbNet(MIDType.getUniqueInstance());
        Criterion cost = new Criterion("Cost", "euro");
        net.setDecisionCriteria(new ArrayList<>(List.of(cost)));
        Variable utility = new Variable("Cost of therapy");
        utility.setTimeSlice(0);
        utility.setDecisionCriterion(cost);
        Node node = net.addNode(utility, NodeType.UTILITY);

        Node next = net.addShiftedNode(node, 1, 0, 0);

        assertSame(cost, next.getVariable().getDecisionCriterion());
    }
}
