/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * Represents the basic Gompertz distribution with shape parameter a, and rate parameter b.
 * The parametrization (ignoring that they use different letters) is the same as
 * - flexurv R package (https://cran.r-project.org/web/packages/flexsurv/index.html)
 * - Andreas Wienke (2007). Frailty models in survival analysis.
 * - Sanku Dey, Fernando A. Moala & Devendra Kumar (2018) Statistical properties and different methods of estimation of Gompertz distribution with application,
 * Journal of Statistics and Management Systems, 21:5, 839-876, DOI: 10.1080/09720510.2018.1450197
 *
 * The Gompertz distribution with shape parameter a and rate parameter b has probability density function:
 *		f(x | a, b) = b exp(ax) exp(-b/a (exp(ax) - 1))
 *
 * hazard function:
 *
 * 		h(x | a, b) = b exp(ax)
 *
 * probability distribution function is
 *
 * 		F(x | a, b) = 1 - exp(-b/a (exp(ax) - 1))
 *
 * quantile function (inverse distribution function) is
 *
 * 		Q( p |a, b) = (1/a)*ln(1 - (a/b)* ln(1-p ))
 *
 * The hazard is increasing for shape a>0 and decreasing for a<0. For a=0 the Gompertz is equivalent to the exponential distribution with constant hazard and rate b.
 *
 * There are several parametrizations which may be implemented in the future. Different parametrizations are shown in:
 * 	- Wikipedia (https://en.wikipedia.org/wiki/Gompertz_distribution, accessed 22/10/2021).
 *  - John H. Pollard, Emil J. Valkovics (1992) The Gompertz Distribution and its applications, Genus , Vol. 48, No. 3/4 Universit  degli Studi di Roma La Sapienza p. 15-28
 *
 * It is important to note that manu authors do not use the terms shape and rate but only letters and that there is some conflict with this terminology. For example
 * The functions eha::dgompertz and similar available in the package eha label the parameters the other way round that flexurv,
 * so that what is called the shape by eha::dgompertz is called the rate in flexurv, and what is called 1 / scale in eha::dgompertz is
 * called the shape in flexurv (https://rdrr.io/cran/flexsurv/man/Gompertz.html accesed 22/10/2021)
 *
 * @version 1.0 - 25/10/2021 Adapted from WeibullFunction
 * @author  cmyago
 */
public class GompertzFunction extends ProbDensFunctionWithKnownInverseCDF {
	/**
	 * Shape a
	 */
	private double a;

	/**
	 * Rate b;
	 */
	private double b ;


	/**
	 * Creates a GompertzFunction object with shape a=0 and rate a=0
	 */
	public GompertzFunction() {
		this(1, 1);
	}

	/**
	 * Creates a WeibullFunction object with shape a and rate b
	 * @param a - shape parameter
	 * @param b - rate parameter
	 */
	public GompertzFunction(double a, double b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * Creates a GompertzFunction object equal to GompertzFunction
	 * @param gompertzFunction used to create an equal GompertzFunction object
	 */
	public GompertzFunction(GompertzFunction gompertzFunction) {
		super();
		this.a = gompertzFunction.a;
		this.b = gompertzFunction.b;
	}


	/**
	 * Returns an array of double where array[1] is b (rate) and array[0] is a (shape), parameters of the Gompertz function
	 * @return n array of double where array[1] is b (rate) and array[0] is a (shape), parameters of the Gompertz function
	 */
	@Override public double[] getParameters() {
		double[] parameters = new double[2];
		parameters[0] = a;
		parameters[1] = b;
		return parameters;
	}

	/**
	 * Sets params[1] as b (rate) and params[0] as a (shape) of this GompertzFunction
	 * @param params  array of double where params[1] as b (rate) and params[0] as a (shape) of this GompertzFunction
	 */
	@Override public void setParameters(double[] params) {
		a = params[0];
		b = params[1];
	}


	/*
	 * Verify the GompertzFunction params.
	 * @param isChanceVariable - not used, kept form compatibility
	 * @return true if shape a and rate b are both greater than 0
	 */
	@Override public void verifyParametersDomain(boolean isChanceVariable) {
		if(!((a > 0) && (b>0))){
			throw new IllegalArgumentException("Domain parameters should be greater than zero");
		}
	}


	/**
	 * Checks is the domain of the parameters is correct.
	 * Shape "a" and rate "b" should be greater than zero.
	 * @param parameters - parameters[1]= a and parameters[0] = b
	 * @throws IllegalArgumentException - thrown if shape a or rate b is <= 0
	 */
	@Override public void verifyParameters(double[] parameters) {
		if ((parameters[0] < 0) || (parameters[1]<0)) {
			throw new IllegalArgumentException("Wrong parameters" + this.getClass().getName());
		}
	}


	/**
	 * The mean of this Gompertz distribution, computed numerically as the
	 * integral of the quantile function over the unit interval — the Gompertz
	 * mean has no comfortable closed form. Both methods used to throw a plain
	 * RuntimeException, and the mean is what sensitivity analysis takes as the
	 * base-line value of an uncertain parameter, so any path that touched a
	 * Gompertz fell over.
	 * <p>
	 * For shape {@code a = 0} the distribution is the Exponential with rate
	 * {@code b}; for {@code a < 0} it is defective — it puts probability
	 * {@code exp(b/a)} on never failing — so its mean is infinite.
	 */
	@Override public double getMean() {
		if (a == 0) {
			return 1 / b;
		}
		if (a < 0) {
			return Double.POSITIVE_INFINITY;
		}
		return integrateQuantile(1);
	}

	/**
	 * The variance, computed numerically like {@link #getMean()}: infinite for
	 * a defective distribution, the Exponential's for {@code a = 0}.
	 */
	@Override public double getVariance() {
		if (a == 0) {
			return 1 / (b * b);
		}
		if (a < 0) {
			return Double.POSITIVE_INFINITY;
		}
		double mean = integrateQuantile(1);
		return integrateQuantile(2) - mean * mean;
	}

	/**
	 * Composite Simpson integration of {@code Q(p)^power} over the unit
	 * interval. The quantile grows like a double logarithm near {@code p = 1},
	 * so the sliver left out at the upper end contributes a negligible amount.
	 */
	private double integrateQuantile(int power) {
		final int intervals = 200_000; // even
		final double upper = 1 - 1e-12;
		double h = upper / intervals;
		double sum = 0; // Q(0) = 0, so the lower endpoint contributes nothing.
		for (int i = 1; i < intervals; i++) {
			double q = getInverseCumulativeDistributionFunction(i * h);
			sum += (i % 2 == 1 ? 4 : 2) * Math.pow(q, power);
		}
		sum += Math.pow(getInverseCumulativeDistributionFunction(upper), power);
		return sum * h / 3;
	}

	/**
	 * Returns the minimun of the support of the Gompertz distribudion. The Gompertz distribucion support is [0, +inf)
	 * @return 0
	 */
	@Override public double getMinimum() {
		return 0;
	}


	/**
	 * Returns the maximun of the support of the Gompertz distribution. The Gompertz distribution support is [0, +inf)
	 * @return Double.POSITIVE_INFINITY;
	 */
	@Override public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}


	/**
	 * Returns Q(y, a, b) where Q is the inverse cumulative distribution (quantile function) for a Gompertz distribution
	 * Inverse cumulative distribution Q( y |a, b) = (1/a)*ln(1 - (a/b)* ln(1-y ))
	 * @param y 0<=y<=1; probability
	 * @return Q(y, lambda, k) where Q is the Inverse Cumulative Distribution for this Gompertz distribution
	 */
	@Override public double getInverseCumulativeDistributionFunction(double y) {
		
		double returnValue;
		if (y < 0.0 || y > 1.0) {
			throw new OutOfRangeException(y, 0.0, 1.0);
		} else {
			returnValue = (1/a)*Math.log(1 	- (a/b)*Math.log(1-y));
		}
		return returnValue;
	}


	/**
	 * Returns a copy of this object
	 * @return a copy of this object
	 */
	@Override public ProbDensFunction copy() {
		return new GompertzFunction(this);
	}



}
