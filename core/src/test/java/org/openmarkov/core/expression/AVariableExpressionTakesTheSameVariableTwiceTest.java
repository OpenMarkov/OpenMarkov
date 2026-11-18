/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.expression;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The relation of an event with a link to itself names its variable twice, and its formulas are built on that list.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AVariableExpressionTakesTheSameVariableTwiceTest {

    @Test void theSameVariableTwiceIsAccepted() {
        Variable checkup = new Variable("Checkup");
        Variable onset = new Variable("Onset");

        assertDoesNotThrow(() -> new VariableExpression(List.of(checkup, onset, checkup), "Checkup + Onset"));
    }

    @Test void twoDifferentVariablesWithOneNameAreRefused() {
        assertThrows(IllegalStateException.class,
                     () -> new VariableExpression(List.of(new Variable("A"), new Variable("A")), "A"));
    }
}
