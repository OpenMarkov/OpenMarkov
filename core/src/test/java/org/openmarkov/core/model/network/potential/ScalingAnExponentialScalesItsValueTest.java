/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scaling exp(constant + b * X) by s must give s times its value, wherever the Constant covariate
 * is in the list.
 *
 * @author Manuel Arias
 */
public class ScalingAnExponentialScalesItsValueTest {

    private final Variable utility = new Variable("U");
    private final Variable x = new Variable("X");

    private ExponentialPotential exponential(boolean constantFirst) {
        VariableExpression covariateX = new VariableExpression(List.of(x), "{X}");
        VariableExpression constant = VariableExpression.Common.CONSTANT;
        return constantFirst
                ? new ExponentialPotential(List.of(utility, x), PotentialRole.UNSPECIFIED,
                        new VariableExpression[] { constant, covariateX }, new double[] { 0.5, 1 })
                : new ExponentialPotential(List.of(utility, x), PotentialRole.UNSPECIFIED,
                        new VariableExpression[] { covariateX, constant }, new double[] { 1, 0.5 });
    }

    private double valueWithXEqualTo3(ExponentialPotential potential) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(x, 3.0));
        return potential.tableProject(evidence, null).getValues()[0];
    }

    private void assertScalingDoubles(boolean constantFirst) throws Exception {
        ExponentialPotential potential = exponential(constantFirst);
        double before = valueWithXEqualTo3(potential);
        potential.scalePotential(2);
        assertEquals(2 * before, valueWithXEqualTo3(potential), 1E-9);
        assertEquals(Math.exp(3.5), before, 1E-9);
    }

    @Tag(TestSpeed.FAST)
    @Test public void withTheConstantFirst() throws Exception {
        assertScalingDoubles(true);
    }

    @Tag(TestSpeed.FAST)
    @Test public void withTheConstantLast() throws Exception {
        assertScalingDoubles(false);
    }

    @Tag(TestSpeed.FAST)
    @Test public void scalingByZeroGivesZero() throws Exception {
        ExponentialPotential potential = exponential(false);
        potential.scalePotential(0);
        assertEquals(0, valueWithXEqualTo3(potential), 0.0);
    }

    @Tag(TestSpeed.FAST)
    @Test public void withoutAConstantItSaysSo() {
        ExponentialPotential potential = new ExponentialPotential(List.of(utility, x), PotentialRole.UNSPECIFIED,
                new VariableExpression[] { new VariableExpression(List.of(x), "{X}") }, new double[] { 1 });
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class, () -> potential.scalePotential(2));
        assertTrue(e.getMessage().contains("Constant"), e.getMessage());
    }
}
