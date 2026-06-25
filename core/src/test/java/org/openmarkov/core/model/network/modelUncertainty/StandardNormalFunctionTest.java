/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

/**
 * @author manolo
 */
public class StandardNormalFunctionTest extends ProbDensFunctionTest {
    
    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new StandardNormalFunction();
    }
    
    @Override public double[] initializeParams() {
        return new double[0];
    }
    
}
