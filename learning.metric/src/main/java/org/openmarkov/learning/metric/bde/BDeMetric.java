/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.bde;

import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.metric.util.MathUtils;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;

/**
 * This class implements the BD metric.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@MetricType(name = "BDe") public class BDeMetric extends BayesianMetric {

	//Constructor

	/**
	 * Construct an instance of BDeMetric
	 *
     * @param alpha {@code double} alpha parameter
	 */
	public BDeMetric(double alpha) {
		super(alpha);
	}

	@Override public double score(TablePotential tablePotential) {
		double nodeScore = 0;
		int numStates = tablePotential.getVariable(0).getNumStates();
		double[] freq = tablePotential.getValues();
		int position = 0;
		double n_ij;
		double n_ijk;
		int parentConfigurations = freq.length / numStates;

		while (position < freq.length) {
			n_ij = 0;
			double sumStates = 0;
			//k-th state of the node
			for (int k = 0; k < numStates; k++) {
				n_ijk = freq[position];
				n_ij += n_ijk;
				sumStates += (MathUtils.lnGamma((1.0 / (numStates * parentConfigurations)) + n_ijk));
				position++;
			}
			nodeScore += (MathUtils.lnGamma(1.0 / parentConfigurations)) - MathUtils
					.lnGamma(n_ij + (1.0 / parentConfigurations)) - numStates * MathUtils
					.lnGamma(1.0 / (numStates * parentConfigurations)) + sumStates;
		}
		return nodeScore;
	}
}
