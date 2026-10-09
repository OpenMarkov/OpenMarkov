/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The work of eliminating a variable X of two states, with A of three states, B of four and C of five.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheWorkOfAnEliminationTest {

    private final Variable x = new Variable("X", 2);
    private final Variable a = new Variable("A", 3);
    private final Variable b = new Variable("B", 4);
    private final Variable c = new Variable("C", 5);

    @Test void withProbabilitiesAloneItIsTheirJointTableOncePerTable() {
        // P(X, A) and P(X, B): a joint table of 2 * 3 * 4 = 24 cells, built from two tables
        assertEquals(48, EliminationPlan.workOf(List.of(List.of(x, a), List.of(x, b)), List.of()));
    }

    @Test void eachUtilityAddsTheJointTableWidenedWithItsOwnVariables() {
        // P(X, A) has 6 cells; U(X, C) widens it to 6 * 5 = 30 and U(X, A) leaves it at 6
        assertEquals(6 + 30 + 6, EliminationPlan.workOf(List.of(List.of(x, a)), List.of(List.of(x, c), List.of(x, a))));
    }
}
