/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


/**
 * @author manolo
 */
public abstract class ProbDensFunctionTest {

    /**
     * Samples drawn to estimate the mean, the standard deviation and the tail
     * masses. The estimators converge as 1/sqrt(SAMPLE_COUNT), so this figure
     * fixes how tight the tolerances below can be: the standard error of the
     * sample mean is one hundredth of a standard deviation, and maxErrorMean
     * leaves a margin of six of those standard errors.
     */
    private static final int SAMPLE_COUNT = 100_000;

    /**
     * The generator is seeded explicitly so that a failure always reproduces.
     * Sampling error is real but bounded, and a test that draws a different
     * sample on every run reports that bound as an intermittent failure.
     */
    private static final long SEED = 20260901L;

    /**
     * Central probability mass whose interval is checked by
     * {@link #testQuantileFunction(double[])}.
     */
    private static final double QUANTILE_PROBABILITY = 0.65;

    protected final double maxErrorMean = 0.01;
    ProbDensFunction pdf;
    private final double maxErrorStDeviation = 0.01;

    /**
     * Tolerance for the mass observed beyond each end of the quantile interval.
     * It is an absolute tolerance on a probability, so unlike the mean and the
     * standard deviation it must not be scaled by {@link #getFactorError()}.
     */
    private final double maxErrorQuantile = 0.01;

    public abstract ProbDensFunction newProbDensFunctionInstance();

    @Tag(TestSpeed.MEDIUM)
    @Test public void testMeanAndVariance() {
        Random randomGenerator = new XORShiftRandom();
        randomGenerator.setSeed(SEED);
        pdf = newProbDensFunctionInstance();
        pdf.setParameters(initializeParams());

        double[] samples = new double[SAMPLE_COUNT];
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            samples[i] = pdf.getSample(randomGenerator);
        }
        testMean(samples);
        testStandardDeviation(samples);
        testQuantileFunction(samples);
    }

    @Test public void copyProbDensFuncion() {
        ProbDensFunction probDensFunction = newProbDensFunctionInstance();
        ProbDensFunction copyProbDensFunction = probDensFunction.copy();

        assertNotSame(probDensFunction, copyProbDensFunction);
    }

    public void testQuantileFunction(double[] samples) {
        int numSamplesLowestExtreme = 0;
        int numSamplesUpperExtreme = 0;

        DomainInterval interval = pdf.getInterval(QUANTILE_PROBABILITY);
        double min = interval.min();
        double max = interval.max();

        for (double sample : samples) {
            if (sample < min) {
                numSamplesLowestExtreme++;
            } else if (sample > max) {
                numSamplesUpperExtreme++;
            }
        }
        double extremeProbMass = (1.0 - QUANTILE_PROBABILITY) / 2.0;
        double numSamples = samples.length;
        assertEquals(numSamplesLowestExtreme / numSamples, extremeProbMass, maxErrorQuantile);
        assertEquals(numSamplesUpperExtreme / numSamples, extremeProbMass, maxErrorQuantile);
    }

    /**
     * @param samples
     */
    private void testStandardDeviation(double[] samples) {
        double variance = Tools.varianceSample(samples);
        assertMeanTest(Math.sqrt(variance), pdf.getStandardDeviation(), maxErrorStDeviation);
    }

    /**
     * @return
     */
    protected double getFactorError() {
        return 2.0 * pdf.getStandardDeviation();
    }

    public void testMean(double[] samples) {

        double mean = Tools.meanSample(samples);
        assertMeanTest(mean, pdf.getMean(), maxErrorMean);
    }

    /**
     * @param samplesMean true if the difference between two means is lower than
     *                    maxError
     * @param pdfMean
     * @param maxError
     */
    public void assertMeanTest(double samplesMean, double pdfMean, double maxError) {
        assertEquals(samplesMean, pdfMean, getFactorError() * maxError);
    }

    /**
     * @return
     */
    public abstract double[] initializeParams();

}
