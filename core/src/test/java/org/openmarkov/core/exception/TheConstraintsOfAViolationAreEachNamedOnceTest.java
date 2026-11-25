/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.NoEventNodes;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The constraints that a violation names: its own, or, when there are several violations, each constraint once. A
 * violation of several had no constraint of its own, and evaluating the decision tree of a network that broke more than
 * one ended in a NullPointerException instead of saying which (error 451 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheConstraintsOfAViolationAreEachNamedOnceTest {

    private final ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
    private final Node a = probNet.addNode(new Variable("A", "no", "yes"), NodeType.CHANCE);
    private final Node b = probNet.addNode(new Variable("B", "no", "yes"), NodeType.CHANCE);
    private final NoCycle noCycle = new NoCycle();
    private final NoEventNodes noEventNodes = new NoEventNodes();

    @Test void aViolationNamesItsConstraint() {
        assertEquals(List.of(noCycle), new ConstraintViolatedException.ThereIsACycle(noCycle, a, b).constraints());
    }

    @Test void twoViolationsOfOneConstraintNameItOnce() {
        ConstraintViolatedException both = new ConstraintViolatedException.MultipleConstraintsViolated(List.of(
                new ConstraintViolatedException.ThereIsACycle(noCycle, a, b),
                new ConstraintViolatedException.ThereIsACycle(noCycle, b, a)));
        assertEquals(List.of(noCycle), both.constraints());
    }

    @Test void violationsOfTwoConstraintsNameBoth() {
        ConstraintViolatedException both = new ConstraintViolatedException.MultipleConstraintsViolated(List.of(
                new ConstraintViolatedException.ThereIsACycle(noCycle, a, b),
                new ConstraintViolatedException.CannotHaveEventNodeException(noEventNodes, b.getVariable())));
        assertEquals(List.of(noCycle, noEventNodes), both.constraints());
    }
}
