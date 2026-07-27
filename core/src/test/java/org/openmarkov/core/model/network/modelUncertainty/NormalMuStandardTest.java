/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The class had no test, and the inherited consistency checks cannot catch its
 * defect: it squared the standard deviation on the way into the base, so it
 * sampled and reported a distribution with standard deviation s² instead of
 * the s the user asked for — internally consistent, wrong against the
 * parameter. These tests assert against the PARAMETER's meaning.
 *
 * @author Manuel Arias
 */
public class NormalMuStandardTest extends ProbDensFunctionTest {

    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new NormalMuStandard();
    }

    @Override public double[] initializeParams() {
        return new double[]{73.0, 10.0};
    }

    /** s = 4 means variance 16 — not 256, which is what squaring s on the way in produced. */
    @Test public void varianceIsTheSquareOfTheGivenStandardDeviation() {
        assertEquals(16.0, new NormalMuStandard(0.0, 4.0).getVariance(), 1e-12);
    }

    /** And the samples spread like s, not like s². */
    @Test public void samplesSpreadLikeTheGivenStandardDeviation() {
        NormalMuStandard normal = new NormalMuStandard(0.0, 4.0);
        Random seeded = new Random(7);
        int numSamples = 20_000;
        double sumOfSquares = 0;
        for (int i = 0; i < numSamples; i++) {
            double sample = normal.getSample(seeded);
            sumOfSquares += sample * sample;
        }
        double sampledStandardDeviation = Math.sqrt(sumOfSquares / numSamples);

        assertTrue(Math.abs(sampledStandardDeviation - 4.0) < 0.2,
                "sampled standard deviation was " + sampledStandardDeviation + ", asked for 4.0");
    }

    /**
     * setParameters used to take the base's path only: the sigma changed while
     * mu and standard here kept their old values, so getParameters answered
     * stale numbers and behaviour depended on how the object was built.
     */
    @Test public void setParametersKeepsTheReportedParametersInStep() {
        NormalMuStandard normal = new NormalMuStandard();

        normal.setParameters(new double[]{2.0, 3.0});

        assertArrayEquals(new double[]{2.0, 3.0}, normal.getParameters(), 1e-12);
        assertEquals(9.0, normal.getVariance(), 1e-12);
    }

    /**
     * A Normal's mean may be any real number; what must be positive is the
     * standard deviation. The old check rejected every non-positive mean with a
     * message copied from the Beta family, and accepted a negative deviation.
     */
    @Test public void anyMeanIsValidAndANonPositiveStandardDeviationIsNot() {
        NormalMuStandard normal = new NormalMuStandard();

        assertDoesNotThrow(() -> normal.verifyParameters(new double[]{-5.0, 2.0}));
        assertThrows(InvalidArgumentException.class,
                () -> normal.verifyParameters(new double[]{1.0, -2.0}));
    }
}
