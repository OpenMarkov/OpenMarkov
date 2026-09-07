/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * The tables that hand out the parameters of a canonical model carry their own numbers. Writing on
 * one of them must not reach the model, which has no way of noticing: no method of its own is
 * called and the table it keeps expanded is not thrown away.
 *
 * @author Manuel Arias
 */
public class TheParametersHandedOutAreCopiesTest {

    private final Variable child = new Variable("C", 2);
    private final Variable parent = new Variable("P", 2);

    private MaxPotential aModelWorth(double... parameters) {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        potential.setNoisyParameters(parent, parameters);
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theTableOfAParentDoesNotShareTheParameters() {
        MaxPotential potential = aModelWorth(0.8, 0.2, 0.3, 0.7);

        TablePotential handedOut = potential.getNoisyPotentials().getFirst();

        assertNotSame(potential.getNoisyParameters(parent), handedOut.getValues(),
                "the table hands out the very array of the model");
    }

    @Tag(TestSpeed.FAST)
    @Test public void writingOnTheTableDoesNotReachTheModel() {
        MaxPotential potential = aModelWorth(0.8, 0.2, 0.3, 0.7);

        potential.getNoisyPotentials().getFirst().getValues()[0] = 99.0;

        assertArrayEquals(new double[] { 0.8, 0.2, 0.3, 0.7 }, potential.getNoisyParameters(parent), 1e-12,
                "writing on the table handed out reached the parameters of the model");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theLeakTableDoesNotShareTheParametersEither() {
        MaxPotential potential = aModelWorth(0.8, 0.2, 0.3, 0.7);

        TablePotential leak = potential.getLeakyPotential();

        assertNotNull(leak);
        assertNotSame(potential.getLeakyParameters(), leak.getValues(),
                "the leak table hands out the very array of the model");
        leak.getValues()[0] = 99.0;
        assertArrayEquals(new double[] { 1.0, 0.0 }, potential.getLeakyParameters(), 1e-12,
                "writing on the leak table reached the parameters of the model");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theNumbersHandedOutAreTheOnesOfTheModel() {
        MaxPotential potential = aModelWorth(0.8, 0.2, 0.3, 0.7);

        assertArrayEquals(new double[] { 0.8, 0.2, 0.3, 0.7 },
                potential.getNoisyPotentials().getFirst().getValues(), 1e-12,
                "a copy must still be a copy of the right numbers");
    }
}
