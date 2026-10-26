/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Weibull hazard on the age and the sex gives, for each cycle, a probability between 0 and 1, or says why it
 * cannot: a negative rate is refused, and a time between 0 and 1 is the first cycle (error 278). The same holds
 * when a simulation samples it with the rate of its parent Lambda.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AHazardGivesOnlyProbabilitiesTest {

    private final Variable risk = new Variable("Revision Risk", "no", "yes");
    private final Variable age = new Variable("Age", true, 0.0, Double.POSITIVE_INFINITY, false, 1.0);
    private final Variable sex = new Variable("Sex", "Female", "Male");

    /** A hazard with the coefficients of Gamma, Constant, Age and Sex. */
    private WeibullHazardPotential hazard(boolean log, double... coefficients) {
        risk.setTimeSlice(5);
        age.setTimeSlice(5);
        WeibullHazardPotential hazard = new WeibullHazardPotential(List.of(risk, age, sex),
                                                                   PotentialRole.CONDITIONAL_PROBABILITY);
        hazard.setCoefficients(coefficients);
        hazard.setTimeVariable(age);
        hazard.setLog(log);
        return hazard;
    }

    /** A hazard of shape 2 on a parent Lambda, sampled at the age {@code years} with that {@code lambda}. */
    private double sample(double lambda, double years, double randomNumber) throws Exception {
        Variable lambdaVariable = new Variable("Lambda", true, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                                               false, 0.001);
        WeibullHazardPotential hazard = new WeibullHazardPotential(
                List.of(risk, lambdaVariable, age), PotentialRole.CONDITIONAL_PROBABILITY,
                new VariableExpression[] { VariableExpression.Common.GAMMA, VariableExpression.Common.CONSTANT },
                new double[] { Math.log(2), 0 }, null);
        hazard.setTimeVariable(age);
        EvidenceCase evidence = atAge(years);
        evidence.addFinding(new Finding(lambdaVariable, lambda));
        return hazard.sampleConditionedVariable(new double[] { randomNumber }, evidence);
    }

    private EvidenceCase atAge(double years) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(age, years));
        return evidence;
    }

    private static void assertProbabilities(double[] values) {
        for (double value : values) {
            assertTrue(value >= 0 && value <= 1, () -> value + " is not a probability");
        }
    }

    @Test
    void aNegativeRateIsRefusedNamingTheParents() throws Exception {
        WeibullHazardPotential hazard = hazard(false, 0, -1, 0, 0.5);
        EvidenceCase sixtyFive = atAge(65);

        NonProjectablePotentialException.HazardRateIsNegative e = assertThrows(
                NonProjectablePotentialException.HazardRateIsNegative.class,
                () -> hazard.tableProject(sixtyFive, null));
        assertAll(() -> assertEquals(risk, e.variable),
                  () -> assertEquals("with Sex = Female, ", e.where),
                  () -> assertEquals("-1", e.rate),
                  () -> assertTrue(e.localize().contains("with Sex = Female, its rate"), e.localize()));
    }

    @Test
    void aPositiveRateWithoutTheLogarithmGivesProbabilities() throws Exception {
        assertProbabilities(hazard(false, 0, 0.01, 0, 0.02).tableProject(atAge(65), null).getValues());
    }

    @Test
    void aTimeBetweenZeroAndOneIsTheFirstCycle() throws Exception {
        double shape = Math.exp(0.3);
        double[] values = hazard(true, 0.3, -2, 0, 0.5).tableProject(atAge(0.5), null).getValues();

        assertProbabilities(values);
        // From 0 to 0.5, with the rate of a woman and then of a man
        assertEquals(1 - Math.exp(-Math.exp(-2) * Math.pow(0.5, shape)), values[1], 1e-12);
        assertEquals(1 - Math.exp(-Math.exp(-1.5) * Math.pow(0.5, shape)), values[3], 1e-12);
    }

    @Test
    void aTimeAfterTheFirstCycleCountsOneCycle() throws Exception {
        double shape = Math.exp(0.3);
        double[] values = hazard(true, 0.3, -2, 0, 0.5).tableProject(atAge(2.5), null).getValues();

        assertEquals(1 - Math.exp(-Math.exp(-2) * (Math.pow(2.5, shape) - Math.pow(1.5, shape))), values[1], 1e-12);
    }

    @Test
    void aSimulationRefusesANegativeLambda() {
        NonProjectablePotentialException.HazardLambdaIsNegative e = assertThrows(
                NonProjectablePotentialException.HazardLambdaIsNegative.class, () -> sample(-1, 65, 0.5));
        assertAll(() -> assertEquals(risk, e.variable),
                  () -> assertEquals("-1", e.lambda),
                  () -> assertTrue(e.localize().contains("the value of the parent Lambda, is -1"), e.localize()));
    }

    @Test
    void aSimulationTakesATimeBetweenZeroAndOneAsTheFirstCycle() throws Exception {
        // From 0 to 0.5 with rate 1 and shape 2, the event happens with probability 1 - exp(-0.25), about 0.22
        assertAll(() -> assertEquals(1, sample(1, 0.5, 0.2)),
                  () -> assertEquals(0, sample(1, 0.5, 0.25)));
    }
}
