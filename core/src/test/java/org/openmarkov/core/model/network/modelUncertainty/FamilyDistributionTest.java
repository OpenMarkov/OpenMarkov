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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author manolo
 */
@Disabled public abstract class FamilyDistributionTest {
    
    private FamilyDistribution family;
    
    private final double maxErrorMean = 0.001;
    private final double maxErrorStDeviation = 0.01;
    
    @Tag(TestSpeed.SLOW)
    @Test public void testMeanAndVariance() {
        
        int numSamples = 1000000;
        Random randomGenerator = new XORShiftRandom();
        List<UncertainValue> list = initializeListUncertainValues();
        family = newFamilyDistribution(list);
        
        List<double[]> samples = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            samples.add(family.getSample(randomGenerator));
        }
        testMean(samples);
        testStandardDeviation(samples);
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
    
    public abstract FamilyDistribution newFamilyDistribution(List<UncertainValue> list);
    
    /**
     * @return
     */
    public abstract List<UncertainValue> initializeListUncertainValues();
    
    /**
     * @param samples
     */
    protected void testMean(List<double[]> samples) {
        
        int numChildrenFam = samples.get(0).length;
        double[] auxSamples;
        double[] meanSample = new double[numChildrenFam];
        for (int i = 0; i < numChildrenFam; i++) {
            auxSamples = new double[samples.size()];
            for (int j = 0; j < samples.size(); j++) {
                auxSamples[j] = samples.get(j)[i];
            }
            meanSample[i] = Tools.meanSample(auxSamples);
        }
        
        assertMeanTest(meanSample, family.getMean(), maxErrorMean);
        
    }
    
    /**
     * @param samples
     */
    protected void testStandardDeviation(List<double[]> samples) {
        int numChildrenFam = samples.get(0).length;
        double[] auxSamples;
        double[] stDSample = new double[numChildrenFam];
        for (int i = 0; i < numChildrenFam; i++) {
            auxSamples = new double[samples.size()];
            for (int j = 0; j < samples.size(); j++) {
                auxSamples[j] = samples.get(j)[i];
            }
            stDSample[i] = Math.sqrt(Tools.varianceSample(auxSamples));
        }
        assertMeanTest(stDSample, family.getStandardDeviation(), maxErrorStDeviation);
        
    }
    
    /**
     * @param meanSample
     * @param meanFamily
     * @param maxErrorMean2
     */
    protected void assertMeanTest(double[] meanSample, double[] meanFamily, double maxErrorMean2) {
        
        for (int i = 0; i < meanSample.length; i++) {
            assertEquals(meanSample[i], meanFamily[i], maxErrorMean2);
        }
    }
}
