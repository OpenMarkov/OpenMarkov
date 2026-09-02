/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A potential that keeps its uncertainty in a table it holds inside must be found by whoever walks
 * the potentials asking for {@link UncertaintyCarrier}.
 *
 * @author Manuel Arias
 */
class UncertaintyCarrierDeclarationTest {

    private final Variable cost = new Variable("cost");
    private final Variable therapy = new Variable("therapy", new State[]{new State("no"), new State("yes")});
    private final UncertainValue[] uncertainValues = {new UncertainValue(0.0), new UncertainValue(-0.25)};

    @Test
    void anExactDistributionIsAskedForItsUncertaintyThroughTheInterface() {
        ExactDistrPotential potential = new ExactDistrPotential(List.of(cost, therapy),
                PotentialRole.UNSPECIFIED, new double[]{0.0, -0.25});
        potential.setUncertainValues(uncertainValues);

        UncertaintyCarrier carrier = assertInstanceOf(UncertaintyCarrier.class, potential);

        assertSame(uncertainValues, carrier.getUncertainValues());
    }

    @Test
    void aUnivariateDistributionIsAskedForItsUncertaintyThroughTheInterface() {
        UnivariateDistrPotential potential = new UnivariateDistrPotential(List.of(cost, therapy),
                PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setUncertainValues(uncertainValues);

        UncertaintyCarrier carrier = assertInstanceOf(UncertaintyCarrier.class, potential);

        assertSame(uncertainValues, carrier.getUncertainValues());
    }
}
