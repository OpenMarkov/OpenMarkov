/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.aic.AICMetric;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;
import org.openmarkov.learning.metric.bde.BDeMetric;
import org.openmarkov.learning.metric.entropy.EntropyMetric;
import org.openmarkov.learning.metric.k2.K2Metric;
import org.openmarkov.learning.metric.mdlm.MDLMetric;

/**
 * Evaluation measure types supported by the BN evaluation module.
 *
 * <p>Each scoring constant knows how to instantiate the {@link Metric} that
 * computes it; non-scoring constants ({@link #CONFUSIONMATRIX},
 * {@link #LOGLIKELIHOOD}) return {@code null} from {@link #newMetric()}.
 */
public enum MeasureType {

    CONFUSIONMATRIX,
    LOGLIKELIHOOD,
    BAYES   { @Override public Metric newMetric() { return new BayesianMetric(0.5); } },
    AIC     { @Override public Metric newMetric() { return new AICMetric(); } },
    ENTROPY { @Override public Metric newMetric() { return new EntropyMetric(); } },
    BDE     { @Override public Metric newMetric() { return new BDeMetric(0.5); } },
    K2      { @Override public Metric newMetric() { return new K2Metric(); } },
    MDL     { @Override public Metric newMetric() { return new MDLMetric(); } };

    /**
     * @return a fresh, uninitialised metric instance for this measure type, or
     *         {@code null} when the measure has no associated {@link Metric}
     *         (confusion matrix, log-likelihood — computed directly).
     */
    public Metric newMetric() {
        return null;
    }
}
