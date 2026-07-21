/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.aic;

import org.junit.jupiter.api.Test;
import org.openmarkov.learning.metric.MetricTestSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AICMetricTest {

    /**
     * Incremental deltas must agree with a full recompute for every add/remove/invert.
     * This exercises the AIC dimension bookkeeping, including inversions between variables
     * of different arity.
     */
    @Test
    public void incrementalScoringIsConsistent() {
        List<String> mismatches = MetricTestSupport.deltaMismatches(AICMetric::new);
        assertTrue(mismatches.isEmpty(), "incremental scoring inconsistent: " + mismatches);
    }
}
