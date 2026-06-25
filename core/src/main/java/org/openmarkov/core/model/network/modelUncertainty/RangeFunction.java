/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

/**
 * Continuous uniform probability density function on the interval
 * {@code [lowerBound, upperBound]}.
 */
@ProbDensFunctionType(name = "Range", parameters = {"lower bound", "upper bound"}) public class RangeFunction
        extends ProbDensFunctionWithKnownInverseCDF {
    private double lowerBound;
    private double upperBound;
    
    public RangeFunction() {
        this(0.0, 1.0);
    }
    
    /**
     * @param lowerBound Lower bound
     * @param upperBound Upper bound
     */
    public RangeFunction(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    
    public RangeFunction(RangeFunction rangeFunction) {
        super();
        this.lowerBound = rangeFunction.lowerBound;
        this.upperBound = rangeFunction.upperBound;
    }
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (!(lowerBound < upperBound)) {
            throw new InvalidArgumentException("Lower bound (" + lowerBound + ") should be less than the upper bound (" + upperBound + ")");
        }
        if (isChanceVariable) {
            if (upperBound > 1) {
                throw new InvalidArgumentException(upperBound, "Upper bound", "It should be less or equal to 1");
            }
            if (lowerBound <= 0) {
                throw new InvalidArgumentException(lowerBound, "Lower bound", "It should be bigger than 0");
            }
        }
    }
    
    @Override public double[] getParameters() {
        return new double[]{lowerBound, upperBound};
    }
    
    @Override public void setParameters(double[] params) {
        lowerBound = params[0];
        upperBound = params[1];
    }
    
    @Override public double getMaximum() {
        return upperBound;
    }
    
    @Override public double getMean() {
        return (lowerBound + upperBound) / 2;
    }
    
    @Override public double getInverseCumulativeDistributionFunction(double y) {
        return lowerBound + (upperBound - lowerBound) * y;
    }
    
    @Override public double getVariance() {
        return Math.pow(upperBound - lowerBound, 2.0) / 12;
    }
    
    @Override public double getMinimum() {
        return lowerBound;
    }
    
    @Override public ProbDensFunction copy() {
        return new RangeFunction(this);
    }
}
