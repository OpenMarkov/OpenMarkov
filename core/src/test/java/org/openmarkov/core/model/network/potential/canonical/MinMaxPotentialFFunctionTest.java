/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the deterministic f-function table of {@link MinPotential} and {@link MaxPotential}.
 * The table must be a well-formed deterministic CPT: for every combination of parent states, exactly
 * one child state has value 1.0, namely the minimum (resp. maximum) of the parent states.
 *
 * @author Manuel Arias
 */
class MinMaxPotentialFFunctionTest {

    private static final int NUM_STATES = 3;

    @Test void minFFunctionEncodesTheMinimumOverItsParents() {
        Variable child = new Variable("Y", NUM_STATES);
        Variable parent = new Variable("P1", NUM_STATES);
        MinPotential potential = new MinPotential(new ArrayList<>(List.of(child, parent)));

        TablePotential f = potential.getFFunctionPotential();

        assertDeterministicTable(f, NUM_STATES, Math::min);
    }

    @Test void maxFFunctionEncodesTheMaximumOverItsParents() {
        Variable child = new Variable("Y", NUM_STATES);
        Variable parent = new Variable("P1", NUM_STATES);
        MaxPotential potential = new MaxPotential(new ArrayList<>(List.of(child, parent)));

        TablePotential f = potential.getFFunctionPotential();

        assertDeterministicTable(f, NUM_STATES, Math::max);
    }

    @Test void minFFunctionWithTwoParents() {
        Variable child = new Variable("Y", NUM_STATES);
        MinPotential potential = new MinPotential(new ArrayList<>(
                List.of(child, new Variable("P1", NUM_STATES), new Variable("P2", NUM_STATES))));

        assertDeterministicTable(potential.getFFunctionPotential(), NUM_STATES, Math::min);
    }

    @Test void maxFFunctionWithTwoParents() {
        Variable child = new Variable("Y", NUM_STATES);
        MaxPotential potential = new MaxPotential(new ArrayList<>(
                List.of(child, new Variable("P1", NUM_STATES), new Variable("P2", NUM_STATES))));

        assertDeterministicTable(potential.getFFunctionPotential(), NUM_STATES, Math::max);
    }

    /**
     * The f-function's variables are [child, z_1, ..., z_p, leaky], all with {@code numChildStates}
     * states, the child first (fastest-varying in the value layout). Each z/leaky is a candidate child
     * value; the deterministic child value is {@code reduce} over them.
     */
    private void assertDeterministicTable(TablePotential f, int numChildStates, IntBinaryOperator reduce) {
        int numParents = f.getVariables().size() - 1;
        double[] values = f.getValues();
        int numColumns = values.length / numChildStates;

        for (int column = 0; column < numColumns; column++) {
            int base = column * numChildStates;

            int rest = column;
            int expected = -1;
            for (int p = 0; p < numParents; p++) {
                int parentState = rest % numChildStates;
                expected = (expected == -1) ? parentState : reduce.applyAsInt(expected, parentState);
                rest /= numChildStates;
            }

            int onesCount = 0;
            int selected = -1;
            for (int s = 0; s < numChildStates; s++) {
                double v = values[base + s];
                assertThat(v).as("f value must be 0 or 1 (column %d, state %d)", column, s).isIn(0.0, 1.0);
                if (v == 1.0) {
                    onesCount++;
                    selected = s;
                }
            }
            assertThat(onesCount).as("column %d must be one-hot", column).isEqualTo(1);
            assertThat(selected).as("selected child state for column %d", column).isEqualTo(expected);
        }
    }
}
