/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.k2;

import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;

/**
 * This class implements the K2 metric. Note that the K2 metric is
 * exactly the BayesianMetric with parameter alpha set to 1.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@MetricType(name = "K2") public class K2Metric extends BayesianMetric {

	//Constructor

	/**
	 * After constructing the metric, we evaluate the given net.
	 */
	public K2Metric() {
		super(1);
	}
}
