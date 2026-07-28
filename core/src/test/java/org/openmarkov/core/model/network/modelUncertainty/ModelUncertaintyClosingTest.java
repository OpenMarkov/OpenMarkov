/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The closing round of the subpackage's report: the degenerate seed of the
 * random generator (B4), the shared sampling family of the Beta's copy (B5),
 * the null parameters of the Erlang (B6) and the unimplemented moments of the
 * Gompertz (B8). Where a number is asserted, the oracle is computed here from
 * the distribution's definition, by a different path than the code under test.
 *
 * @author Manuel Arias
 */
class ModelUncertaintyClosingTest {

    /**
     * Zero is the XORShift fixed point: seeded with it, the old generator
     * answered 0.0 forever, and zero is the most natural seed a caller can
     * pick. Seeding with zero must give a working, reproducible generator.
     */
    @Test
    void seedZeroGivesAWorkingReproducibleGenerator() {
        XORShiftRandom seededWithZero = new XORShiftRandom();
        seededWithZero.setSeed(0);

        boolean anyNonZero = false;
        for (int i = 0; i < 10; i++) {
            anyNonZero |= seededWithZero.nextDouble() != 0.0;
        }
        assertTrue(anyNonZero, "seeded with 0, every draw used to come out 0.0");

        XORShiftRandom first = new XORShiftRandom();
        XORShiftRandom second = new XORShiftRandom();
        first.setSeed(0);
        second.setSeed(0);
        for (int i = 0; i < 5; i++) {
            assertEquals(first.nextDouble(), second.nextDouble(),
                    "the same seed must give the same sequence");
        }
    }

    /** The copy of a Beta must not share the original's mutable sampling family. */
    @Test
    void aCopiedBetaHasItsOwnSamplingFamily() throws Exception {
        BetaFunction original = new BetaFunction(2.0, 3.0);
        BetaFunction copied = new BetaFunction(original);

        Field samplingFamily = BetaFunction.class.getDeclaredField("dirichletForSampling");
        samplingFamily.setAccessible(true);

        assertNotSame(samplingFamily.get(original), samplingFamily.get(copied));
    }

    /** getParameters answered null - a filler that became an NPE in any generic consumer. */
    @Test
    void erlangAnswersItsParametersAndItsToStringWorks() {
        ErlangFunction erlang = new ErlangFunction();
        erlang.setParameters(new double[]{3.0, 2.0});

        assertArrayEquals(new double[]{3.0, 2.0}, erlang.getParameters(), 1e-12);
        assertDoesNotThrow(erlang::toString);
    }

    /**
     * The Gompertz moments, checked against the definition: the mean is the
     * integral of the survival function S(x) = exp(-(b/a)(e^(ax)-1)) over
     * [0, inf), and E[X^2] is the integral of 2x S(x). Both are computed here
     * by trapezoid over x - a different path than the quantile integration the
     * class uses. The methods used to throw a plain RuntimeException, and the
     * mean is what sensitivity analysis takes as the base-line value.
     */
    @Test
    void gompertzMomentsMatchTheDefinition() {
        double a = 0.1;
        double b = 0.05;
        GompertzFunction gompertz = new GompertzFunction(a, b);

        double step = 0.001;
        double meanByDefinition = 0;
        double secondMomentByDefinition = 0;
        for (double x = step / 2; x < 300; x += step) {
            double survival = Math.exp(-(b / a) * (Math.exp(a * x) - 1));
            meanByDefinition += survival * step;
            secondMomentByDefinition += 2 * x * survival * step;
        }
        double varianceByDefinition = secondMomentByDefinition - meanByDefinition * meanByDefinition;

        assertEquals(meanByDefinition, gompertz.getMean(), 1e-3 * meanByDefinition);
        assertEquals(varianceByDefinition, gompertz.getVariance(), 1e-2 * varianceByDefinition);
    }

    /** Shape zero is the Exponential with rate b; its moments have closed forms. */
    @Test
    void gompertzWithShapeZeroIsTheExponential() {
        GompertzFunction exponentialInDisguise = new GompertzFunction(0.0, 0.25);

        assertEquals(4.0, exponentialInDisguise.getMean(), 1e-12);
        assertEquals(16.0, exponentialInDisguise.getVariance(), 1e-12);
    }
}
