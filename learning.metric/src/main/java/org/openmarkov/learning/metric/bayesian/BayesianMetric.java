/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.bayesian;

import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.metric.util.MathUtils;

/**
 * This class implements the Bayesian metric.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @author ibermejo
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@MetricType(name = "Bayesian") public class BayesianMetric extends Metric {
	//Members
	/**
	 * Parameter alpha
	 */
    protected double alpha;
	protected double logGammaAlpha = 0;

	// Constructor

	/**
	 * After constructing the metric, we evaluate the given net.
	 *
     * @param alpha {@code double} alpha parameter.
	 */
	public BayesianMetric(double alpha) {
		this.alpha = alpha;
	}
    
    /**
     * Scores potential table
     *
     * @return {@code double} score
     */
    @Override
    public double score(TablePotential tablePotential) {
		double nodeScore = 0;
		int numStates = tablePotential.getVariable(0).getNumStates();
		double[] freq = tablePotential.getValues();
		int position = 0;
		double n_ij;
		double n_ijk;

		double scoreCorrectionAlpha = 0.0;
		double logGammaAlpha = MathUtils.lnGamma(alpha);

		if (alpha != 0) {
			scoreCorrectionAlpha += MathUtils.lnGamma(numStates * alpha);
			scoreCorrectionAlpha -= numStates * logGammaAlpha;
		}

		while (position < freq.length) {
			n_ij = 0;
			// k-th state of the node
			for (int k = 0; k < numStates; k++) {
				if (freq[position] != 0) {
					n_ijk = freq[position];
					n_ij += n_ijk + alpha;
					nodeScore += MathUtils.lnGamma(alpha + n_ijk);
				} else if (alpha != 0) {
					n_ij += alpha;
					nodeScore += logGammaAlpha;
				}
				position++;
			}
			if (n_ij != 0)
				nodeScore -= MathUtils.lnGamma(n_ij);
			nodeScore += scoreCorrectionAlpha;
		}
		return nodeScore;
	}
}
