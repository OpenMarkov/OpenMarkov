/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.DES;

import org.apache.commons.math3.stat.StatUtils;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.MonteCarloOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * First tests for {@link SimulationResults}. Characterisation tests: they document that the
 * undiscounted and discounted variances each equal the true variance of their own array.
 * <p>
 * Note: these do NOT fail against the previous code, where the discounted variance was computed
 * with StatUtils.variance(discountedValues, mean) using the undiscounted mean. That two-argument
 * overload is offset-invariant (it subtracts a correction term for the supplied mean), so the
 * wrong mean produced the same result up to floating-point noise. The one-line change to pass the
 * discounted mean is for clarity and numerical stability, not a change of result.
 *
 * @author Manuel Arias
 */
public class SimulationResultsTest {

    private static final double TOLERANCE = 1.0e-9;

    /**
     * Discounted values are on a different scale from the undiscounted ones.
     */
    private SimulationResults buildResults() {
        double[] values = {10.0, 20.0, 30.0, 40.0};
        double[] discountedValues = {1.0, 2.0, 3.0, 4.0};
        SimulationResults results = new SimulationResults(values.length);
        for (int i = 0; i < values.length; i++) {
            results.setValue(i, values[i], discountedValues[i]);
        }
        results.calculateStatisticalProperties(new MonteCarloOptions());
        return results;
    }

    @Test public void undiscountedVarianceIsComputedAroundItsOwnMean() {
        SimulationResults results = buildResults();
        assertEquals(StatUtils.variance(new double[]{10.0, 20.0, 30.0, 40.0}),
                results.getSampleVariance(), TOLERANCE);
    }

    @Test public void discountedVarianceIsComputedAroundTheDiscountedMean() {
        SimulationResults results = buildResults();
        // Oracle: the single-argument overload centres on the array's true mean by definition.
        assertEquals(StatUtils.variance(new double[]{1.0, 2.0, 3.0, 4.0}),
                results.getDiscountedSampleVariance(), TOLERANCE);
    }
}
