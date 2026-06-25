/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author manolo
 */
public class ExactFunctionTest extends ProbDensFunctionTest {
    
    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new ExactFunction();
    }
    
    @Override public double[] initializeParams() {
        return new double[]{5.3};
    }
    
    @Override protected double getFactorError() {
        return 1.0;
    }
    
    @Override public void testQuantileFunction(double[] samples) {
        for (double sample : samples) {
            assertEquals(sample, pdf.getMean(), this.maxErrorMean);
        }
    }
    
}
