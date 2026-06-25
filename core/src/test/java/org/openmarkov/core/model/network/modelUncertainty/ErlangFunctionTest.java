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
public class ErlangFunctionTest extends ProbDensFunctionTest {
    
    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new ErlangFunction();
    }
    
    @Override public double[] initializeParams() {
        return new double[]{2.0, 1.5};
    }
    
}
