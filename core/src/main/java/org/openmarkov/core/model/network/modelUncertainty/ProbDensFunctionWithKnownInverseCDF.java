/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

/**
 * Base class for probability density functions whose inverse cumulative
 * distribution function is known in closed form. Sampling and central interval
 * computation are then implemented generically by inverse-CDF transformation;
 * subclasses only need to provide
 * {@link #getInverseCumulativeDistributionFunction(double)}.
 */
public abstract class ProbDensFunctionWithKnownInverseCDF extends ProbDensFunction {
	@Override public DomainInterval getInterval(double p) {
		double halfP = p / 2.0;
		return new DomainInterval(getInverseCumulativeDistributionFunction(0.5 - halfP),
				getInverseCumulativeDistributionFunction(0.5 + halfP));
	}

	public abstract double getInverseCumulativeDistributionFunction(double y);

	@Override public final double getSample(Random randomGenerator) {
		return getInverseCumulativeDistributionFunction(randomGenerator.nextDouble());
	}

}
