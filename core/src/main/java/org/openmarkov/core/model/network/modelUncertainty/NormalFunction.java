/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.Random;

/**
 * Normal (Gaussian) probability density function with mean {@code mu} and
 * variance {@code sigma}. Sampling is built on top of the
 * {@link StandardNormalFunction}.
 */
@ProbDensFunctionType(name = "Normal", isValidForProbabilities = false, parameters = { "mu",
		"sigma" }) public class NormalFunction extends ProbDensFunction {
	private double mu;
	private double sigma;
	private StandardNormalFunction standard;

	public NormalFunction() {
		this(0.0, 1.0);
	}

	public NormalFunction(double mu, double sigma) {
		this.mu = mu;
		this.sigma = sigma;
		standard = new StandardNormalFunction();
	}

	public NormalFunction(NormalFunction normalFunction) {
		super();
		this.mu = normalFunction.mu;
		this.sigma = normalFunction.sigma;
		if (normalFunction.standard != null) {
			this.standard = (StandardNormalFunction) normalFunction.standard.copy();
		}
	}

	/**
	 * @param parameters - parameters[1]= mu and parameters[0] = sigma^2
	 * @throws IllegalArgumentException - thrown if sigma&#60;0
	 */
	@Override public void verifyParameters(double[] parameters) {
		if (!(parameters[0] > 0)) {
			throw new InvalidArgumentException(parameters[0], "N", "N should be greater than 0");
		}
	}

	//CMI
	//For Univariate
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (sigma <= 0) {
            throw new InvalidArgumentException(sigma, "sigma", "should be a number bigger than 0");
        }
	}
	//CMF

	@Override public double[] getParameters() {
		double[] a = new double[2];
		a[0] = mu;
		a[1] = sigma;
		return a;
	}

	@Override public void setParameters(double[] args) {
		mu = args[0];
		sigma = args[1];
	}

	@Override public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}

	@Override public double getMean() {
		// TODO Auto-generated method stub
		return mu;
	}

	@Override public double getSample(Random randomGenerator) {
		return translationFromStandardNormal(standard.getSample(randomGenerator));
	}

	private double translationFromStandardNormal(double x) {
		return sigma * x + mu;
	}

	@Override public double getVariance() {
		return Math.pow(sigma, 2.0);
	}

	@Override public double getMinimum() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override public DomainInterval getInterval(double p) {
		DomainInterval standardInterval = standard.getInterval(p);
		return new DomainInterval(translationFromStandardNormal(standardInterval.min()),
				translationFromStandardNormal(standardInterval.max()));
	}

	@Override public ProbDensFunction copy() {
		return new NormalFunction(this);
	}
}
