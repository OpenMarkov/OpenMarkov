/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.Random;

/**
 * Log-normal probability density function parameterised by the mean
 * ({@code mu}) and standard deviation ({@code sigma}) of the underlying
 * normal distribution. Sampling is delegated to an auxiliary
 * {@link NormalFunction}.
 */
@ProbDensFunctionType(name = "LogNormal", isValidForProbabilities = false, parameters = { "mu",
		"sigma" }) public class LogNormalFunction extends ProbDensFunction {
	private double mu;
	private double sigma;
	/**
	 * Auxiliary normal distribution used for sampling
	 */
	private NormalFunction normal;

	public LogNormalFunction() {
		this(0.0, 1.0);
	}

	public LogNormalFunction(double mu, double sigma) {
		this.mu = mu;
		this.sigma = sigma;
		this.normal = new NormalFunction(mu, sigma);
	}

	public LogNormalFunction(LogNormalFunction logNormalFunction) {
		super();
		this.mu = logNormalFunction.mu;
		this.sigma = logNormalFunction.sigma;
		if (logNormalFunction.normal != null) {
			this.normal = (NormalFunction) logNormalFunction.normal.copy();
		}
	}
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (sigma <= 0) {
            throw new InvalidArgumentException(sigma, "sigma", "should be a number bigger than 0");
        }
	}

	@Override public double[] getParameters() {
		return new double[] { mu, sigma };
	}

	@Override public void setParameters(double[] args) {
		mu = args[0];
		sigma = args[1];
		normal = new NormalFunction(mu, sigma);
	}

	@Override public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}

	@Override public double getMean() {
		return Math.exp(mu + Math.pow(sigma, 2.0) / 2.0);
	}

	@Override public double getSample(Random randomGenerator) {
		return Math.exp(normal.getSample(randomGenerator));
	}

	@Override public double getVariance() {
		double squareSigma = Math.pow(sigma, 2.0);
		return (Math.exp(squareSigma) - 1) * Math.exp(2 * mu + squareSigma);
	}

	@Override public double getMinimum() {
		return 0;
	}

	@Override public DomainInterval getInterval(double p) {
		LogNormalDistribution auxLognormalDist = new LogNormalDistribution(mu, sigma);
		double halfP = p / 2;
		return new DomainInterval(auxLognormalDist.inverseCumulativeProbability(0.5 - halfP),
				auxLognormalDist.inverseCumulativeProbability(0.5 + halfP));
	}

	@Override public ProbDensFunction copy() {
		return new LogNormalFunction(this);
	}
}
