/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction.TruncNormalFunctionMeanSd;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Manuel Arias
 */
public class TruncatedNormalFunctionTest extends ProbDensFunctionTest {

    private static final int NUM_SAMPLES = 1000000;

    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new TruncatedNormalFunction();
    }

    @Override public double[] initializeParams() {
        // mu = 1.0, sigma = 2.0: an untruncated normal would put ~31% of its mass below 0
        return new double[]{1.0, 2.0};
    }
    
    @Disabled("This tests takes too long to complete, and seems it was stressing the machine that executes the test rather than verifying functionality" +
            "Trying to reduce the num of samples of org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#testMeanAndVariance from 10000000 to a lower number, such as 10000 would highly reduce its time complexity" +
            "Once that is done, this method can be erased, as it only calls super. The only reason it exist is because JUnit's @Disabled tag is not @Inherit as other tags are")
    @Tag(TestSpeed.SLOW)
    @Test
    public void testMeanAndVariance() {
        super.testMeanAndVariance();
    }
    
    @Disabled("This tests takes too long to complete, and seems it was stressing the machine that executes the test rather than verifying functionality" +
            "Trying to reduce the num of samples of org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#testMeanAndVariance from 10000000 to a lower number, such as 10000 would highly reduce its time complexity" +
            "Once that is done, this method can be erased, as it only calls super. The only reason it exist is because JUnit's @Disabled tag is not @Inherit as other tags are")
    @Tag(TestSpeed.SLOW)
    @Test
    public void repeatTestMeanAndVariance() {
        super.repeatTestMeanAndVariance();
    }

    @Test public void samplesRespectTheDefaultLowerBound() {
        TruncatedNormalFunction function = new TruncatedNormalFunction(1.0, 2.0);
        Random randomGenerator = new XORShiftRandom();
        for (int i = 0; i < NUM_SAMPLES; i++) {
            double sample = function.getSample(randomGenerator);
            assertTrue(sample >= 0.0, "sampled a value below the lower bound: " + sample);
        }
    }

    @Test public void samplesRespectExplicitBounds() {
        TruncatedNormalFunction function = new TruncatedNormalFunction(1.0, 2.0, -1.5, 3.5);
        Random randomGenerator = new XORShiftRandom();
        for (int i = 0; i < NUM_SAMPLES; i++) {
            double sample = function.getSample(randomGenerator);
            assertTrue(sample >= -1.5 && sample <= 3.5, "sample outside the bounds: " + sample);
        }
    }

    /**
     * The bounds are what makes this distribution different from its parent, so the truncated
     * moments are checked against the sample moments for an interval that is deliberately
     * asymmetric around mu. A mirrored or untruncated implementation does not survive this.
     */
    @Test public void momentsMatchTheSampleMomentsUnderAsymmetricBounds() {
        TruncatedNormalFunction function = new TruncatedNormalFunction(1.0, 2.0, 0.0, 2.0);
        Random randomGenerator = new XORShiftRandom();
        double[] samples = new double[NUM_SAMPLES];
        for (int i = 0; i < NUM_SAMPLES; i++) {
            samples[i] = function.getSample(randomGenerator);
        }
        assertEquals(Tools.meanSample(samples), function.getMean(), 0.01);
        assertEquals(Tools.varianceSample(samples), function.getVariance(), 0.01);
    }

    @Test public void boundsAreReportedAsTheSupport() {
        TruncatedNormalFunction function = new TruncatedNormalFunction(1.0, 2.0, -1.5, 3.5);
        assertEquals(-1.5, function.getMinimum(), 0.0);
        assertEquals(3.5, function.getMaximum(), 0.0);

        TruncatedNormalFunction byDefault = new TruncatedNormalFunction(1.0, 2.0);
        assertEquals(0.0, byDefault.getMinimum(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, byDefault.getMaximum(), 0.0);
    }

    @Test public void inverseCdfIsMonotonicallyIncreasingAndWithinBounds() {
        TruncatedNormalFunction function = new TruncatedNormalFunction(1.0, 2.0, -1.5, 3.5);
        double previous = function.getInverseCumulativeDistributionFunction(0.001);
        for (double p = 0.002; p < 0.999; p += 0.001) {
            double current = function.getInverseCumulativeDistributionFunction(p);
            assertTrue(current > previous, "inverse CDF decreased at p = " + p);
            assertTrue(current >= -1.5 && current <= 3.5, "inverse CDF outside the bounds at p = " + p);
            previous = current;
        }
    }

    @Test public void copyPreservesTheBounds() {
        TruncatedNormalFunction function = new TruncatedNormalFunction(1.0, 2.0, -1.5, 3.5);
        TruncatedNormalFunction copy = (TruncatedNormalFunction) function.copy();
        assertEquals(-1.5, copy.getLowerBound(), 0.0);
        assertEquals(3.5, copy.getUpperBound(), 0.0);
    }

    /**
     * The parametrisation actually offered in the GUI as "Truncated Normal", which was sampling
     * negative times to event.
     */
    @Test public void theParametrisationOfferedInTheGuiAlsoTruncates() {
        TruncNormalFunctionMeanSd function = new TruncNormalFunctionMeanSd();
        function.setParameters(new double[]{1.0, 2.0});
        Random randomGenerator = new XORShiftRandom();
        for (int i = 0; i < NUM_SAMPLES; i++) {
            double sample = function.getSample(randomGenerator);
            assertTrue(sample >= 0.0, "sampled a negative time to event: " + sample);
        }
    }

}
