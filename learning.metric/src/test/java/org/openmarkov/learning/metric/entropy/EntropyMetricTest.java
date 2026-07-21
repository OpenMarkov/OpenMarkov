/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.entropy;

import org.junit.jupiter.api.Test;
import org.openmarkov.learning.metric.MetricTestSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntropyMetricTest {

    /** Incremental deltas must agree with a full recompute for every add/remove/invert. */
    @Test
    public void incrementalScoringIsConsistent() {
        List<String> mismatches = MetricTestSupport.deltaMismatches(EntropyMetric::new);
        assertTrue(mismatches.isEmpty(), "incremental scoring inconsistent: " + mismatches);
    }

    /**
     * Absolute anchor: for a disconnected net the entropy is the sum of the marginal
     * entropies of A, B and C over the reference dataset (12 cases):
     * A(6,6) and C(6,6) give 12 ln(1/2) each; B(3,6,3) gives 6 ln(1/4) + 6 ln(1/2).
     */
    @Test
    public void totalScoreOfDisconnectedNetIsSumOfMarginalEntropies() {
        double expected = 12 * Math.log(0.5)                   // A
                + 6 * Math.log(0.25) + 6 * Math.log(0.5)       // B
                + 12 * Math.log(0.5);                          // C
        double score = MetricTestSupport.fullScore(EntropyMetric::new, new int[][] {});
        assertEquals(expected, score, 1e-6);
    }
}
