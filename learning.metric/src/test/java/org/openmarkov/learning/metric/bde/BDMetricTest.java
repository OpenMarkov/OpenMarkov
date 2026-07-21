/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.bde;

import org.junit.jupiter.api.Test;
import org.openmarkov.learning.metric.MetricTestSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BDMetricTest {

    /** Incremental deltas must agree with a full recompute for every add/remove/invert. */
    @Test
    public void incrementalScoringIsConsistent() {
        List<String> mismatches = MetricTestSupport.deltaMismatches(() -> new BDeMetric(1.0));
        assertTrue(mismatches.isEmpty(), "incremental scoring inconsistent: " + mismatches);
    }

    /**
     * The equivalent sample size (alpha) passed to the constructor must affect the score;
     * it used to be ignored because the prior was hard-coded to 1.
     */
    @Test
    public void equivalentSampleSizeAffectsScore() {
        double scoreAlpha1 = MetricTestSupport.fullScore(() -> new BDeMetric(1.0), new int[][] {});
        double scoreAlpha8 = MetricTestSupport.fullScore(() -> new BDeMetric(8.0), new int[][] {});
        assertTrue(Math.abs(scoreAlpha1 - scoreAlpha8) > 1e-6,
                "different equivalent sample sizes must give different scores, both were " + scoreAlpha1);
    }
}
