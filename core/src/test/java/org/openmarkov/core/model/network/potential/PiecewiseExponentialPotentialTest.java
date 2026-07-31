/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the sampling of {@link PiecewiseExponentialPotential}: the two edges where the
 * inverse-distribution formula used to answer NaN, a number that is not a number and poisons every
 * comparison downstream.
 *
 * @author Manuel Arias
 */
class PiecewiseExponentialPotentialTest {

    private static PiecewiseExponentialPotential defaultPotential() {
        return new PiecewiseExponentialPotential(List.of(new Variable("X", 2)),
                                                 PotentialRole.CONDITIONAL_PROBABILITY);
    }

    /**
     * A drawn 0 means the survival never falls that low: the event does not happen in any
     * representable time. The formula used to answer NaN.
     */
    @Test
    void aDrawOfZeroMeansTheEventNeverHappens() throws Exception {
        assertEquals(Double.POSITIVE_INFINITY,
                     defaultPotential().sampleConditionedVariable(new double[]{0.0}, new EvidenceCase()));
    }

    /**
     * During a stretch with probability 0 the survival is flat, so the inverse distribution jumps
     * to the first later time where the risk resumes. The formula used to divide 0 by 0 there.
     */
    @Test
    void aStretchWithNoRiskDefersTheEventToWhereTheRiskResumes() throws Exception {
        PiecewiseExponentialPotential potential = defaultPotential();
        TreeMap<Double, Double> table = new TreeMap<>();
        table.put(0.0, 0.0);
        table.put(5.0, 0.5);
        potential.setPiecewiseTable(table);

        assertEquals(5.0, potential.sampleConditionedVariable(new double[]{1.0}, new EvidenceCase()));
    }
}
