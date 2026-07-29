/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Unit test for {@link ExponentialHazardPotential}: what a constructor receives, it must keep.
 *
 * @author Manuel Arias
 */
class ExponentialHazardPotentialTest {

    /**
     * The three-argument constructor used to throw its coefficients away and store null, which
     * blew up on the first projection.
     */
    @Test
    void coefficientsGivenToTheThreeArgumentConstructorAreKept() {
        double[] coefficients = {0.3};
        ExponentialHazardPotential potential = new ExponentialHazardPotential(
                List.of(new Variable("X", 2)), PotentialRole.CONDITIONAL_PROBABILITY, coefficients);
        assertArrayEquals(coefficients, potential.getCoefficients());
    }
}
