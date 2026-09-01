/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

/**
 * @author Manuel Arias
 */
public class WeibullFunctionTest extends ProbDensFunctionTest {

    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new WeibullFunction();
    }

    @Override public double[] initializeParams() {
        // setParameters reads [lambda, k]: lambda = 2.0 (scale), k = 3.0 (shape)
        return new double[]{2.0, 3.0};
    }
    
    

}
