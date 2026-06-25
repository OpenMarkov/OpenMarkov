/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

/**
 * Gamma probability density function parameterised by mean ({@code mu}) and
 * standard deviation ({@code sigma}). Internally these are converted to the
 * canonical shape/scale parameters of {@link GammaAbstract}.
 */
@ProbDensFunctionType(name = "Gamma-mv", isValidForProbabilities = false, parameters = { "mean",
		"standard deviation" }) public class GammamvFunction extends GammaAbstract {
	private double mu;
	private double sigma;

	public GammamvFunction() {
		this(0.0, 0.0);
	}

	public GammamvFunction(double mu, double sigma) {
		setParameters(new double[] { mu, sigma });
	}

	public GammamvFunction(GammamvFunction gammamvFunction) {
		super();
		this.kAbstract = gammamvFunction.kAbstract;
		this.thetaAbstract = gammamvFunction.thetaAbstract;
		this.mu = gammamvFunction.mu;
		this.sigma = gammamvFunction.sigma;
	}

	@Override public double[] getParameters() {
		return new double[] { mu, sigma };
	}

	@Override public void setParameters(double[] parameters) {
		mu = parameters[0];
		sigma = parameters[1];
		this.kAbstract = Math.pow(mu / sigma, 2);
		this.thetaAbstract = Math.pow(sigma, 2) / mu;
	}
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (sigma <= 0) {
            throw new InvalidArgumentException(sigma, "sigma", "should be a number bigger than 0");
        }
        if (mu <= 0) {
            throw new InvalidArgumentException(mu, "mu", "should be a number bigger than 0");
        }
	}

	@Override public ProbDensFunction copy() {
		return new GammamvFunction(this);
	}
}
