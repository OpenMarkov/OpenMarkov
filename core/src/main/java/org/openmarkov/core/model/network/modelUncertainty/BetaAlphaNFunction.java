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
 * Beta probability density function parameterised by {@code alpha} and
 * {@code N}, where {@code N = alpha + beta}. It is an alternative
 * parametrisation of {@link BetaFunction} more convenient when the sample size
 * {@code N} is the natural quantity to elicit.
 */
@ProbDensFunctionType(name = "BetaAlphaNFunction", univariateName = "Beta", isValidForProbabilities = false, isValidForNumeric = false, parameters = {
		"alpha", "N" }) public class BetaAlphaNFunction extends BetaFunction {
	private double alpha;
	private double n;

	public BetaAlphaNFunction() {
		super();
		alpha = 1;
		setN(1);
	}

	public BetaAlphaNFunction(double alpha, double n) {
		super(alpha, n - alpha);
		this.alpha = alpha;
		this.setN(n);
		//TODO: Was this line below inteded?
		//verifyParameters(new double[] { 1, 2 });
	}

	public BetaAlphaNFunction(BetaAlphaNFunction betaFunction) {
		this(betaFunction.getAlpha(), betaFunction.getN());
	}

	/**
	 *
	 */
	/**
	 * Alpha counts the successes and N the observations, so both are positive and N is the larger.
	 *
	 * <p>The test used to be the other way round: it threw when N WAS greater than alpha and let the
	 * impossible case through, while saying "N should be greater than alpha". It rejected exactly the
	 * parameters it exists to accept.
	 */
	@Override public void verifyParameters(double[] parameters) {
		boolean acceptable = (parameters[0] > 0) && (parameters[1] > 0) && (parameters[1] > parameters[0]);
		if (!acceptable) {
			throw new InvalidArgumentException(List.of(parameters[0], parameters[1]), "N",
					"N should be greater than alpha, and both greater than zero, but alpha was "
							+ parameters[0] + " and N was " + parameters[1] + " in " + this.getClass().getName());
		}
	}

	@Override public double[] getParameters() {
		double[] a = new double[2];
		a[0] = alpha;
		a[1] = n;
		return a;
	}

	@Override public void setParameters(double[] params) {
		alpha = params[0];
		n = params[1];

		super.setParameters(new double[] { alpha, n - alpha });
	}

	/**
	 * @return the n
	 */
	public double getN() {
		return n;
	}

	/**
	 * @param n the n to set
	 */
	public void setN(double n) {
		this.n = n;
	}

	@Override public ProbDensFunction copy() {
		return new BetaAlphaNFunction(this);
	}

}
