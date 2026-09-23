/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.ReferencedExpression;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The expression evaluator rejects scientific notation with a signed exponent, as in 1.0E-4, so
 * a parent below one thousandth made a regression impossible to compute.
 *
 * @author Manuel Arias
 */
public class ARegressionWithSmallValuesIsComputedTest {

    private final Variable y = new Variable("Y");

    private static LinearCombinationPotential twiceThe(Variable child, Variable parent) {
        return new LinearCombinationPotential(List.of(child, parent), PotentialRole.UNSPECIFIED,
                new VariableExpression[] { VariableExpression.Common.CONSTANT,
                        new VariableExpression(List.of(parent), "{" + parent.getName() + "}*2") },
                new double[] { 0, 1 });
    }

    @Tag(TestSpeed.FAST)
    @Test public void aSmallFindingOfTheParent() throws Exception {
        Variable x = new Variable("X");
        for (double value : new double[] { 1E-4, -1E-4, 1E-12, 1E7 }) {
            EvidenceCase evidence = new EvidenceCase();
            evidence.addFinding(new Finding(x, value));
            TablePotential projected = twiceThe(y, x).tableProject(evidence, null);
            assertEquals(2 * value, projected.getValues()[0], Math.abs(value) * 1E-12, "X = " + value);
        }
    }

    @Tag(TestSpeed.FAST)
    @Test public void aStateNamedWithASmallNumber() throws Exception {
        Variable d = new Variable("D", new State[] { new State("0.0001"), new State("1") });
        TablePotential projected = twiceThe(y, d).tableProject(new EvidenceCase(), null);
        assertArrayEquals(new double[] { 0.0002, 2 }, projected.getValues(), 1E-15);
    }

    @Tag(TestSpeed.FAST)
    @Test public void numbersAreWrittenWithoutExponent() {
        assertEquals("0.0001", ReferencedExpression.toExpression(1E-4));
        assertEquals("-0.000000000001", ReferencedExpression.toExpression(-1E-12));
        assertEquals("10000000", ReferencedExpression.toExpression(1E7));
        assertEquals("2", ReferencedExpression.toExpression(2.0));
    }
}
