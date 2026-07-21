/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.bayesian;

import org.junit.jupiter.api.Test;
import org.openmarkov.learning.metric.MetricTestSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BayesianMetricTest {

    /** Incremental deltas must agree with a full recompute for every add/remove/invert. */
    @Test
    public void incrementalScoringIsConsistent() {
        List<String> mismatches = MetricTestSupport.deltaMismatches(() -> new BayesianMetric(1.0));
        assertTrue(mismatches.isEmpty(), "incremental scoring inconsistent: " + mismatches);
    }

    /**
     * With alpha = 0 the Dirichlet prior vanishes and the metric must still yield a finite
     * score. The guarded {@code alpha != 0} branches are meant to handle this, but lnGamma(0)
     * used to be evaluated unconditionally.
     */
    @Test
    public void alphaZeroYieldsAFiniteScore() {
        double score = MetricTestSupport.fullScore(() -> new BayesianMetric(0.0), new int[][] {});
        assertTrue(Double.isFinite(score), "score should be finite for alpha = 0, was " + score);
    }
}
