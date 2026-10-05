/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

/**
 * A scalar-valued measure (e.g. log-likelihood, BDE score). Extends {@link Measure}
 * to store a single double value that can be accumulated and averaged across iterations.
 */
public class MeasureValue extends Measure {
    
    private double value;
    
    /** Number of iterations (folds or samples) that the value is the average of. */
    private int numIterations = 1;
    
    public MeasureValue(MeasureType type) {
        super(type);
        value = 0.0;
    }
    
    @Override public void setNumCases(int numCases) {
        super.setNumCases(numCases);
    }
    
    public double getValue() {
        return value;
    }
    
    
    /**
     * Sets the measure value and the number of cases it was computed from.
     *
     * @param value    the computed measure value
     * @param numCases the number of cases used in the computation
     */
    public void setValue(double value, int numCases) {
        this.value = value;
        super.setNumCases(numCases);
    }
    
    /**
     * Divides the accumulated value by the number of iterations to compute the average.
     * The number of cases stays as the total of all the iterations: their average may not be an integer.
     *
     * @param numIterations the number of iterations to average over
     */
    public void averageValue(int numIterations) {
        value = value / (double) numIterations;
        this.numIterations = numIterations;
    }
    
    /**
     * @return minus the value per case: the value added up over all the iterations, divided by
     * the cases of all of them
     */
    public double getLossPerCase() {
        return -value * numIterations / super.getNumCases();
    }
    
    @Override
    public void accumulate(Measure measure) {
        double valueToAdd = ((MeasureValue) measure).getValue();
        value = value + valueToAdd;
        super.setNumCases(super.getNumCases() + measure.getNumCases());
    }
    
}
