/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Sweep over every {@code verifyParameters} of the subpackage, written after
 * the same copied check turned up in three classes (the Beta's, pasted into
 * both Normals). Each case asserts what the distribution's own definition
 * requires of its parameters — accept the valid, reject the invalid — so a
 * validation that checks the wrong index, the wrong operand or nothing at all
 * cannot come back unnoticed.
 *
 * @author Manuel Arias
 */
class VerifyParametersSweepTest {

    /** Weibull(lambda, k): both must be positive. The old check ignored k. */
    @Test
    void weibullRequiresBothParametersPositive() {
        WeibullFunction weibull = new WeibullFunction();

        assertDoesNotThrow(() -> weibull.verifyParameters(new double[]{2.0, 3.0}));
        assertThrows(InvalidArgumentException.class, () -> weibull.verifyParameters(new double[]{-1.0, 3.0}));
        assertThrows(InvalidArgumentException.class, () -> weibull.verifyParameters(new double[]{2.0, -3.0}));
    }

    /**
     * NormalDES(mu, sigma): the mean may be any real number; sigma must be
     * positive. The old check was the copied one: it validated the mean.
     */
    @Test
    void normalDESAcceptsAnyMeanAndRequiresPositiveSigma() {
        NormalFunctionDES normal = new NormalFunctionDES();

        assertDoesNotThrow(() -> normal.verifyParameters(new double[]{-5.0, 2.0}));
        assertThrows(InvalidArgumentException.class, () -> normal.verifyParameters(new double[]{1.0, -2.0}));
    }

    /**
     * Indicator(probability, tte): the probability lives in [0, 1] and the time
     * to event is not negative. The old check looked at the fields instead of
     * the given parameters, with a parenthesization that only fired for a
     * probability above one.
     */
    @Test
    void indicatorRequiresAProbabilityAndANonNegativeTime() {
        IndicatorFunction indicator = new IndicatorFunction();

        assertDoesNotThrow(() -> indicator.verifyParameters(new double[]{0.5, 2.0}));
        assertThrows(InvalidArgumentException.class, () -> indicator.verifyParameters(new double[]{1.5, 2.0}));
        assertThrows(InvalidArgumentException.class, () -> indicator.verifyParameters(new double[]{-0.1, 2.0}));
        assertThrows(InvalidArgumentException.class, () -> indicator.verifyParameters(new double[]{0.5, -1.0}));
    }

    /** The ones that were already right, pinned so they stay right. */
    @Test
    void theCorrectValidationsStayCorrect() {
        assertDoesNotThrow(() -> new GammaFunction().verifyParameters(new double[]{2.0, 3.0}));
        assertThrows(InvalidArgumentException.class,
                () -> new GammaFunction().verifyParameters(new double[]{-2.0, 3.0}));

        assertDoesNotThrow(() -> new BetaFunction().verifyParameters(new double[]{2.0, 3.0}));
        assertThrows(InvalidArgumentException.class,
                () -> new BetaFunction().verifyParameters(new double[]{2.0, -3.0}));

        // BetaAlphaN: alpha > 0, N > alpha.
        assertDoesNotThrow(() -> new BetaAlphaNFunction().verifyParameters(new double[]{2.0, 5.0}));
        assertThrows(InvalidArgumentException.class,
                () -> new BetaAlphaNFunction().verifyParameters(new double[]{5.0, 2.0}));

        // Uniform(min, max): min must not exceed max.
        assertDoesNotThrow(() -> new UniformFunction().verifyParameters(new double[]{1.0, 2.0}));
        assertThrows(IllegalArgumentException.class,
                () -> new UniformFunction().verifyParameters(new double[]{2.0, 1.0}));

        // Exact: any value is a legitimate constant.
        assertDoesNotThrow(() -> new ExactFunction().verifyParameters(new double[]{-7.0}));
    }
}
