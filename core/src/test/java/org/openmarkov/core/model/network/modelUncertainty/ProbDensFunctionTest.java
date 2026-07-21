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
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


/**
 * @author manolo
 */
@Disabled public abstract class ProbDensFunctionTest {
    
    protected final double maxErrorMean = 0.01;
    ProbDensFunction pdf;
    private final double maxErrorStDeviation = 0.01;
    private final double maxErrorQuantile = 0.01;
    
    public abstract ProbDensFunction newProbDensFunctionInstance();
    
    @Tag(TestSpeed.SLOW)
    @Test public void testMeanAndVariance() {
        int numSamples = 10000000;
        Random randomGenerator = new XORShiftRandom();
        pdf = newProbDensFunctionInstance();
        pdf.setParameters(initializeParams());
        
        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = pdf.getSample(randomGenerator);
        }
        testMean(samples);
        testStandardDeviation(samples);
        testQuantileFunction(samples);
    }
    
    @Tag(TestSpeed.SLOW)
    @Test public void repeatTestMeanAndVariance() {
        boolean debug = false;
        
        int numRepetitions = debug ? 10 : 1;
        
        for (int iRepetition = 0; iRepetition < numRepetitions; iRepetition++) {
            testMeanAndVariance();
            if (debug) {
                System.out.println("iRepetition= " + iRepetition);
            }
        }
    }
    
    @Test public void copyProbDensFuncion() {
        ProbDensFunction probDensFunction = newProbDensFunctionInstance();
        ProbDensFunction copyProbDensFunction = probDensFunction.copy();
        
        assertNotSame(probDensFunction, copyProbDensFunction);
    }
    
    public void testQuantileFunction(double[] samples) {
        RangeFunction pGenerator = new RangeFunction(0.6, 0.7);
        double p = pGenerator.getSample(new XORShiftRandom());
        int numSamplesLowestExtreme = 0;
        int numSamplesUpperExtreme = 0;
        
        DomainInterval interval = pdf.getInterval(p);
        double min = interval.min();
        double max = interval.max();
        
        for (double sample : samples) {
            if (sample < min) {
                numSamplesLowestExtreme++;
            } else if (sample > max) {
                numSamplesUpperExtreme++;
            }
        }
        double extremeProbMass = (1.0 - p) / 2.0;
        double numSamples = samples.length;
        assertMeanTest(numSamplesLowestExtreme / numSamples, extremeProbMass, maxErrorQuantile);
        assertMeanTest(numSamplesUpperExtreme / numSamples, extremeProbMass, maxErrorQuantile);
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
