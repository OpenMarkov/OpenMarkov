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
 * 		Q( p |a, b) = (1/a)*ln(1 - (b/a)* ln(1-p ))
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
	 * Returns Double.NaN because it is not implemented yet
	 * Should return the mean of this GompertzFunction distribution.
	 * The mean of a Gompertz distribution is E[X]= (b/a)*exp(b/a)*integral[0,infinity]((1/a)*exp(b*x/a)*log(x)dx
	 * @return Double.NaN
	 */
	@Override public double getMean() {
		throw new RuntimeException("Method for Gompertz function not implemented yet");
//		return Double.NaN;
	}

	/**
	 * Returns Double.NaN because it is not implemented yet
	 * Should return the variance of this GompertzFunction distribution
	 * @return Double.NaN
	 */
	@Override public double getVariance() {
		throw new RuntimeException("Method for Gompertz function not implemented yet");
//		return Double.NaN;
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
	 * Inverse cumulative distribution Q( y |a, b) = (1/a)*ln(1 - (b/a)* ln(1-y ))
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
