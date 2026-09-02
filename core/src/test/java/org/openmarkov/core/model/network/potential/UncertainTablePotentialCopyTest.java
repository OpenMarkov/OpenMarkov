/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A copy of an uncertain table must carry its own uncertain values: they are written in place
 * by {@link TablePotential#setUncertainValuesConsistently} and sampled by the sensitivity analysis.
 *
 * @author Manuel Arias
 */
class UncertainTablePotentialCopyTest {

    private UncertainValue first;
    private UncertainValue second;
    private UncertainTablePotential potential;

    @BeforeEach
    void setUp() {
        Variable a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        potential = new UncertainTablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1});
        first = new UncertainValue(0.9);
        second = new UncertainValue(0.1);
        potential.setUncertainValuesConsistently(List.of(first, second), List.of(0.9, 0.1), 0);
    }

    @Test
    void editingTheUncertaintyOfACopyLeavesTheOriginalAlone() {
        UncertainTablePotential copy = (UncertainTablePotential) potential.copy();

        copy.setUncertainValuesConsistently(List.of(new UncertainValue(0.5), new UncertainValue(0.5)),
                List.of(0.5, 0.5), 0);

        assertSame(first, potential.getUncertainValues()[0]);
        assertSame(second, potential.getUncertainValues()[1]);
    }

    @Test
    void aCopyCarriesItsOwnDistributions() {
        UncertainTablePotential copy = (UncertainTablePotential) potential.copy();

        assertNotSame(potential.getUncertainValues(), copy.getUncertainValues());
        assertNotSame(first, copy.getUncertainValues()[0]);
        assertNotSame(first.getProbDensFunction(), copy.getUncertainValues()[0].getProbDensFunction());
    }
}
