/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.List;

/**
 * Gamma probability density function in its canonical parametrisation by
 * shape {@code k} and scale {@code theta}.
 */
@ProbDensFunctionType(name = "Gamma", isValidForProbabilities = false, parameters = { "k",
		"theta" }) public class GammaFunction extends GammaAbstract {
	private double k;
	private double theta;

	/**
	 * @param k k
	 * @param theta theta
	 */
	public GammaFunction(double k, double theta) {
		this.k = k;
		this.theta = theta;
		this.kAbstract = k;
		this.thetaAbstract = theta;
	}

	public GammaFunction() {
		this(0.0, 0.0);
	}

	public GammaFunction(GammaFunction gammaFunction) {
		super();
		this.kAbstract = gammaFunction.kAbstract;
		this.k = gammaFunction.k;
		this.thetaAbstract = gammaFunction.thetaAbstract;
		this.theta = gammaFunction.theta;
	}

	/**
	 *
	 */
	@Override public void verifyParameters(double[] parameters) {
		if (!((parameters[0] > 0) && (parameters[1] > 0))) {
			throw new InvalidArgumentException(List.of(parameters[0], parameters[1]), "N", "Parameters should be positive");
		}
	}
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (theta <= 0) {
            throw new InvalidArgumentException(theta, "theta", "should be a number bigger than 0");
        }
        if (k <= 0) {
            throw new InvalidArgumentException(k, "k", "should be a number bigger than 0");
        }
	}

	@Override public double[] getParameters() {
		double[] a = new double[2];
		a[0] = k;
		a[1] = theta;
		return a;
	}

	@Override public void setParameters(double[] parameters) {
		k = parameters[0];
		theta = parameters[1];
		this.kAbstract = k;
		this.thetaAbstract = theta;
	}

	@Override public ProbDensFunction copy() {
		return new GammaFunction(this);
	}

}
