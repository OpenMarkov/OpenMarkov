/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.mdl;

import org.junit.jupiter.api.Test;
import org.openmarkov.learning.metric.MetricTestSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MDLMetricTest {

    /**
     * Incremental deltas must agree with a full recompute for every add/remove/invert.
     * This exercises the MDL dimension bookkeeping, including inversions between variables
     * of different arity.
     */
    @Test
    public void incrementalScoringIsConsistent() {
        List<String> mismatches = MetricTestSupport.deltaMismatches(MDLMetric::new);
        assertTrue(mismatches.isEmpty(), "incremental scoring inconsistent: " + mismatches);
    }
}
