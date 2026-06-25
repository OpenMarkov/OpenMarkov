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
     * Divides the accumulated value and case count by the number of iterations to compute the average.
     *
     * @param numIterations the number of iterations to average over
     */
    public void averageValue(int numIterations) {
        value = value / (double) numIterations;
        super.setNumCases(super.getNumCases() / numIterations);
    }
    
    @Override
    public void accumulate(Measure measure) {
        double valueToAdd = ((MeasureValue) measure).getValue();
        value = value + valueToAdd;
        super.setNumCases(super.getNumCases() + measure.getNumCases());
    }
    
}
