/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the sampling of {@link PiecewiseExponentialPotential}: the two edges where the
 * inverse-distribution formula used to answer NaN, a number that is not a number and poisons every
 * comparison downstream; the values given as rates, which used to be drawn as probabilities (error 100 of
 * the pending list); and the values out of range, which used to give times that were not numbers (error 359).
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

    /** A potential whose stretches start at 0 and at 5, with the values 0.1 and 0.2. */
    private static PiecewiseExponentialPotential twoStretches(boolean useRates) {
        PiecewiseExponentialPotential potential = defaultPotential();
        potential.setUseRates(useRates);
        potential.setPiecewiseTable(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2)));
        return potential;
    }

    /** The time drawn when the survival to it is e^-1. */
    private static double timeWithSurvivalOneOverE(PiecewiseExponentialPotential potential) throws Exception {
        return potential.sampleConditionedVariable(new double[]{Math.exp(-1)}, new EvidenceCase());
    }

    /**
     * With rates, the survival to time t in the first stretch is e^(-0.1 t), which is e^-0.5 at 5; then it falls
     * as e^(-0.2 (t - 5)), and reaches e^-1 at 7.5.
     */
    @Test
    void ratesAreDrawnAsRates() throws Exception {
        assertEquals(7.5, timeWithSurvivalOneOverE(twoStretches(true)), 1e-12);
    }

    /**
     * With probabilities, the survival to 5 is 0.9^5; it reaches e^-1 at 5 + (1 + 5 ln 0.9) / -ln 0.8. This is the
     * draw there was before rates were taken into account.
     */
    @Test
    void probabilitiesAreDrawnAsBefore() throws Exception {
        assertEquals(5 + (1 + 5 * Math.log(0.9)) / -Math.log(0.8), timeWithSurvivalOneOverE(twoStretches(false)),
                     1e-12);
    }

    @Test
    void changingWhatTheValuesAreChangesTheDraw() throws Exception {
        PiecewiseExponentialPotential potential = twoStretches(false);
        potential.setUseRates(true);

        assertEquals(7.5, timeWithSurvivalOneOverE(potential), 1e-12);
    }

    @ParameterizedTest
    @ValueSource(doubles = { 1.5, -0.2, Double.NaN, Double.POSITIVE_INFINITY })
    void aProbabilityOutOfRangeIsRefused(double value) {
        PiecewiseExponentialPotential potential = twoStretches(false);

        assertThrows(InvalidArgumentException.class,
                     () -> potential.setPiecewiseTable(new TreeMap<>(Map.of(0.0, 0.1, 5.0, value))));
        assertEquals(Map.of(0.0, 0.1, 5.0, 0.2), potential.getPiecewiseTable());
    }

    @ParameterizedTest
    @ValueSource(doubles = { -0.2, Double.NaN, Double.POSITIVE_INFINITY })
    void aRateOutOfRangeIsRefused(double value) {
        PiecewiseExponentialPotential potential = twoStretches(true);

        assertThrows(InvalidArgumentException.class,
                     () -> potential.setPiecewiseTable(new TreeMap<>(Map.of(0.0, 0.1, 5.0, value))));
        assertEquals(Map.of(0.0, 0.1, 5.0, 0.2), potential.getPiecewiseTable());
    }

    @Test
    void aRateAboveOneIsTaken() throws Exception {
        PiecewiseExponentialPotential potential = twoStretches(true);
        potential.setPiecewiseTable(new TreeMap<>(Map.of(0.0, 2.0)));

        assertEquals(0.5, timeWithSurvivalOneOverE(potential), 1e-12);
    }

    /** A rate of 1.5 is not a probability: the values stay rates. */
    @Test
    void ratesAboveOneDoNotBecomeProbabilities() {
        PiecewiseExponentialPotential potential = twoStretches(true);
        potential.setPiecewiseTable(new TreeMap<>(Map.of(0.0, 1.5)));

        assertThrows(InvalidArgumentException.class, () -> potential.setUseRates(false));
        assertTrue(potential.isUseRates());
    }

    @Test
    void theRangeOfEachKindOfValue() {
        assertTrue(PiecewiseExponentialPotential.isValidValue(1, false));
        assertFalse(PiecewiseExponentialPotential.isValidValue(1.01, false));
        assertTrue(PiecewiseExponentialPotential.isValidValue(1.01, true));
        assertTrue(PiecewiseExponentialPotential.isValidValue(0, true));
        assertFalse(PiecewiseExponentialPotential.isValidValue(-0.01, true));
    }
}
