/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Family of {@link UncertainValue}s following a Dirichlet distribution.
 * Each member is sampled from an independent {@link GammaFunction} with the
 * corresponding {@code alpha} parameter and the resulting vector is normalised
 * to sum one, which is the standard sampling procedure for the Dirichlet.
 */
public class DirichletFamily extends FamilyDistribution {
	public DirichletFamily(List<UncertainValue> uncertainValues) {
		super(filterByFunction(DirichletFunction.class, uncertainValues));
	}

	public DirichletFamily(double[] alphas) {
		family = new ArrayList<>();
		for (Double alpha : alphas) {
			family.add(new UncertainValue(new DirichletFunction(alpha)));
		}
	}

	@Override public double[] getMean() {
		return Tools.normalize(super.getMean());
	}

	@Override public double[] getSample(Random randomGenerator) {
		return Tools.normalize(super.getSample(randomGenerator));
	}

	@Override public double[] getVariance() {
        
        double[] alpha = super.getMean();
        double sumAlpha = Tools.sum(alpha);
        double[] variance = new double[alpha.length];
		for (int i = 0; i < alpha.length; i++) {
			double alphaI = alpha[i];
			variance[i] = alphaI * (sumAlpha - alphaI) / (Math.pow(sumAlpha, 2.0) * (sumAlpha + 1.0));
		}
		return variance;
	}
}
