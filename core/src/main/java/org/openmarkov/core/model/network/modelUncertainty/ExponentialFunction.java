/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

/**
 * Exponential probability density function with rate {@code lambda}, defined
 * over {@code [0, +inf)}. Inherits inverse-CDF sampling from
 * {@link ProbDensFunctionWithKnownInverseCDF}.
 */
public class ExponentialFunction extends ProbDensFunctionWithKnownInverseCDF {
	private double lambda;

	public ExponentialFunction() {
		this(0.0);
	}

	public ExponentialFunction(double lambda) {
		this.lambda = lambda;
	}

	public ExponentialFunction(ExponentialFunction exponentialFunction) {
		super();
		this.lambda = exponentialFunction.lambda;
	}

	/**
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * @param lambda the lambda to set
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	@Override public double[] getParameters() {
		double[] a = new double[1];
		a[0] = lambda;
		return a;
	}

	@Override public void setParameters(double[] params) {
		lambda = params[0];
	}
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (lambda <= 0) {
            throw new InvalidArgumentException(lambda, "lambda", "should be a number bigger than 0");
        }
	}

	@Override public double getMean() {
		return 1 / lambda;
	}

	@Override public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}

	@Override public double getInverseCumulativeDistributionFunction(double y) {
		return (-1.0 / lambda) * Math.log(1.0 - y);
	}

	@Override public double getVariance() {
		return Math.pow(lambda, -2.0);
	}

	@Override public double getMinimum() {
		return 0;
	}

	@Override public ProbDensFunction copy() {
		return new ExponentialFunction(this);
	}
}
