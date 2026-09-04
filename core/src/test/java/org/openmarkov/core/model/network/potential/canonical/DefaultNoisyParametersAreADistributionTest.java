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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The default noisy parameters of a canonical model give every state of the parent a distribution
 * over the states of the child, also when the parent has more states than the child.
 *
 * @author Manuel Arias
 */
public class DefaultNoisyParametersAreADistributionTest {

    private static final double PRECISION = 1e-12;

    @Tag(TestSpeed.FAST)
    @Test public void aParentWithMoreStatesThanTheChildLeavesNoColumnEmpty() {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 3);

        double[] parameters = new MaxPotential(List.of(child, parent)).getNoisyParameters(parent);

        assertArrayEquals(new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, 1.0 }, parameters, PRECISION,
                "the third state of the parent must keep the child at its last state, and was: "
                        + Arrays.toString(parameters));
    }

    @Tag(TestSpeed.FAST)
    @Test public void everyColumnOfTheExpandedTableAddsUpToOne() {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 3);

        TablePotential table = new MaxPotential(List.of(child, parent)).getCPT();

        double[] values = table.getValues();
        for (int column = 0; column * child.getNumStates() < values.length; ++column) {
            double sum = 0;
            for (int state = 0; state < child.getNumStates(); ++state) {
                sum += values[column * child.getNumStates() + state];
            }
            assertEquals(1.0, sum, PRECISION, "column " + column + " of " + Arrays.toString(values));
        }
    }

    @Tag(TestSpeed.FAST)
    @Test public void theMinimumFamilyIsTreatedTheSameWay() {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 3);

        double[] parameters = new MinPotential(List.of(child, parent)).getNoisyParameters(parent);

        assertArrayEquals(new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, 1.0 }, parameters, PRECISION,
                "the third state of the parent must keep the child at its last state, and was: "
                        + Arrays.toString(parameters));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aParentWithAsManyStatesAsTheChildKeepsEachStateOnItsOwn() {
        Variable child = new Variable("C", 3);
        Variable parent = new Variable("P", 3);

        double[] parameters = new MaxPotential(List.of(child, parent)).getNoisyParameters(parent);

        assertArrayEquals(new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 }, parameters, PRECISION,
                "each state of the parent must keep the state of the same index of the child, and was: "
                        + Arrays.toString(parameters));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aParentWithFewerStatesThanTheChildKeepsEachStateOnItsOwn() {
        Variable child = new Variable("C", 4);
        Variable parent = new Variable("P", 2);

        double[] parameters = new MaxPotential(List.of(child, parent)).getNoisyParameters(parent);

        assertArrayEquals(new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 }, parameters, PRECISION,
                "each state of the parent must keep the state of the same index of the child, and was: "
                        + Arrays.toString(parameters));
    }
}
