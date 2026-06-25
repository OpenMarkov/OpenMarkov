/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

/**
 * Triangular probability density function defined by its minimum, maximum and
 * mode. Sampling uses inverse-CDF transformation provided by
 * {@link ProbDensFunctionWithKnownInverseCDF}.
 */
@ProbDensFunctionType(name = "Triangular", parameters = {"minimum", "maximum",
        "mode"}) public class TriangularFunction extends ProbDensFunctionWithKnownInverseCDF {
    /**
     * Minimum
     */
    private double minimum;
    /**
     * Maximum
     */
    private double maximum;
    /**
     * Mode
     */
    private double mode;
    
    public TriangularFunction() {
        this(0.0, 1.0, 0.5);
    }
    
    public TriangularFunction(double minimum, double maximum, double mode) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.mode = mode;
    }
    
    public TriangularFunction(TriangularFunction triangularFunction) {
        super();
        this.maximum = triangularFunction.maximum;
        this.minimum = triangularFunction.minimum;
        this.mode = triangularFunction.mode;
    }
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (mode <= minimum) {
            throw new InvalidArgumentException(mode, "mode", "is lower or equal to minimum (" + minimum + ")");
        }
        if (mode > maximum) {
            throw new InvalidArgumentException(mode, "mode", "is bigger than maximum (" + maximum + ")");
        }
        if (minimum >= maximum) {
            throw new InvalidArgumentException(minimum, "minimum", "is bigger or equal to maximum (" + maximum + ")");
        }
        if (isChanceVariable) {
            if (minimum < 0) {
                throw new InvalidArgumentException(minimum, "minimum", "should be a positive number or zero");
            }
            if (maximum > 1) {
                throw new InvalidArgumentException(maximum, "maximum", "should be less or equal to 1");
            }
        }
    }
    
    @Override public double[] getParameters() {
        return new double[]{minimum, maximum, mode};
    }
    
    @Override public void setParameters(double[] params) {
        minimum = params[0];
        maximum = params[1];
        mode = params[2];
    }
    
    @Override public double getMaximum() {
        return maximum;
    }
    
    @Override public double getMean() {
        return (minimum + maximum + mode) / 3;
    }
    
    @Override public double getInverseCumulativeDistributionFunction(double y) {
        double sample;
        double diffBA = maximum - minimum;
        double diffBC = maximum - mode;
        double diffCA = mode - minimum;
        double ratioCABA = diffCA / diffBA;
        // if (x<ratioCABA){
        if (y < ratioCABA) {
            // if (x<c){
            sample = minimum + Math.sqrt(y * diffBA * diffCA);
        } else {
            sample = maximum - Math.sqrt((1 - y) * diffBA * diffBC);
        }
        return sample;
    }
    
    @Override public double getVariance() {
        return (
                Tools.square(minimum) + Tools.square(maximum) + Tools.square(mode) - minimum * maximum - minimum * mode
                        - maximum * mode
        ) / 18;
    }
    
    @Override public double getMinimum() {
        return minimum;
    }
    
    @Override public ProbDensFunction copy() {
        return new TriangularFunction(this);
    }
}
