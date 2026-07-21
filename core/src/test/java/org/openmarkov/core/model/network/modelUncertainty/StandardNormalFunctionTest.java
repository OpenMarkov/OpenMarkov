/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author manolo
 * @author Manuel Arias
 */
public class StandardNormalFunctionTest extends ProbDensFunctionTest {

    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new StandardNormalFunction();
    }

    @Override public double[] initializeParams() {
        return new double[0];
    }

    /**
     * Checks the inverse CDF against externally tabulated standard normal quantiles.
     * <p>
     * The sampling checks inherited from {@link ProbDensFunctionTest} cannot verify this:
     * {@code getSample} is itself defined in terms of the inverse CDF, so both sides of that
     * comparison come from the same function, and {@code getInterval} takes the absolute value
     * of the result, discarding the sign. Comparing against an independent source is the only
     * way to pin the inverse CDF down.
     */
    @Test public void inverseCdfMatchesTabulatedQuantiles() {
        // Standard normal quantiles, 6 decimal places
        double[][] tabulated = {
                { 0.001, -3.090232 }, { 0.010, -2.326348 }, { 0.025, -1.959964 },
                { 0.050, -1.644854 }, { 0.100, -1.281552 }, { 0.250, -0.674490 },
                { 0.500, 0.000000 },
                { 0.750, 0.674490 }, { 0.900, 1.281552 }, { 0.950, 1.644854 },
                { 0.975, 1.959964 }, { 0.990, 2.326348 }, { 0.999, 3.090232 }
        };
        // Odeh and Evans (1974) is accurate to about 1e-6 over this range
        double admissibleError = 1e-5;

        StandardNormalFunction standardNormal = new StandardNormalFunction();
        for (double[] quantile : tabulated) {
            double beta = quantile[0];
            double expected = quantile[1];
            assertEquals(expected, standardNormal.getInverseCumulativeDistributionFunction(beta), admissibleError,
                    "inverse CDF at beta = " + beta);
        }
    }

    /**
     * An inverse CDF is monotonically increasing by definition. This is the property that a
     * mirrored implementation violates most visibly, and it holds for every parametrisation,
     * so it is worth asserting independently of any tabulated value.
     */
    @Test public void inverseCdfIsMonotonicallyIncreasing() {
        StandardNormalFunction standardNormal = new StandardNormalFunction();
        double previous = standardNormal.getInverseCumulativeDistributionFunction(0.001);
        for (double beta = 0.002; beta < 0.999; beta += 0.001) {
            double current = standardNormal.getInverseCumulativeDistributionFunction(beta);
            assertTrue(current > previous,
                    "inverse CDF decreased between beta = " + (beta - 0.001) + " and beta = " + beta);
            previous = current;
        }
    }

}
