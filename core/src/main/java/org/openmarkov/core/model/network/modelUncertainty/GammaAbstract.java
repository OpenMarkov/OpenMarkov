/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGeneratorFactory;

import java.util.Random;

/**
 * Common base for Gamma probability density functions. Stores the canonical
 * shape ({@code k}) and scale ({@code theta}) parameters and provides shared
 * sampling and statistical-moment implementations regardless of the concrete
 * parametrisation exposed to the user.
 *
 * @see GammaFunction
 * @see GammamvFunction
 */
public abstract class GammaAbstract extends ProbDensFunction {
	protected double kAbstract;
	protected double thetaAbstract;

	@Override public final double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}

	@Override public final double getMinimum() {
		return 0.0;
	}

	@Override public final double getMean() {
		return (kAbstract * thetaAbstract);
	}

	/**
	 * A sample drawn from the generator handed in, which is the whole point of receiving one.
	 *
	 * <p>It used to draw from Colt's static generator and ignore the argument, so two runs seeded
	 * alike came out different and no probabilistic sensitivity analysis could be reproduced - nor was
	 * a static generator with no owner safe to share between threads. Beta and Dirichlet arrive here
	 * too: a Dirichlet samples gammas and a beta samples a Dirichlet.
	 *
	 * <p>The distribution is unchanged. Colt was asked for shape k and rate 1/theta, which is the same
	 * gamma as shape k and scale theta - the pair {@link #getInterval} has always used.
	 */
	@Override public final double getSample(Random randomGenerator) {
		return new GammaDistribution(RandomGeneratorFactory.createRandomGenerator(randomGenerator),
				kAbstract, thetaAbstract).sample();
	}

	public boolean isAnErlangFunction(double epsilon) {
		return (Math.abs(kAbstract - Math.ceil(kAbstract))) < epsilon;
	}

	@Override public final double getVariance() {
		return kAbstract * Math.pow(thetaAbstract, 2.0);
	}

	@Override public DomainInterval getInterval(double p) {
		GammaDistribution auxGammaDist = new GammaDistribution(kAbstract, thetaAbstract);
		double halfP = p / 2;
		return new DomainInterval(auxGammaDist.inverseCumulativeProbability(0.5 - halfP),
				auxGammaDist.inverseCumulativeProbability(0.5 + halfP));
	}

}
