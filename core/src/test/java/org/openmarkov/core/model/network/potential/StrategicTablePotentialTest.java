/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StrategicTablePotential#reorder(List)} and
 * {@link StrategicTablePotential#reorder(Variable, State[])}.
 * <p>
 * Verifies that both {@code values[]} and {@code strategyTrees[]} are kept
 * in sync when the potential is reordered, and that {@code null} strategyTrees
 * is handled gracefully.
 *
 * @author Manuel Arias
 */
class StrategicTablePotentialTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static State state(String name) {
        return new State(name);
    }

    private static Variable var(String name, State... states) {
        return new Variable(name, states);
    }

    private static final Variable MARKER_VAR = var("__marker__", state("m0"), state("m1"));

    /** Builds a distinct StrategyTree instance used only for reference-equality checks. */
    private static StrategyTree tree() {
        return new StrategyTree(MARKER_VAR);
    }

    // ------------------------------------------------------------------
    // reorder(List<Variable>) — variable reordering
    // ------------------------------------------------------------------

    @Test
    void reorderVariablesRearrangesBothValuesAndStrategyTrees() {
        State s0 = state("s0"), s1 = state("s1");
        State t0 = state("t0"), t1 = state("t1"), t2 = state("t2");
        Variable x = var("X", s0, s1);   // 2 states
        Variable y = var("Y", t0, t1, t2); // 3 states

        // Original order: [X, Y] — table size = 6
        // values[i] = i + 1.0  for i in 0..5
        StrategicTablePotential stp = new StrategicTablePotential(List.of(x, y), PotentialRole.UNSPECIFIED);
        StrategyTree[] origTrees = new StrategyTree[6];
        for (int i = 0; i < 6; i++) {
            stp.getValues()[i] = i + 1.0;
            origTrees[i] = tree();
        }
        stp.strategyTrees = origTrees;

        // Reorder to [Y, X]
        StrategicTablePotential reordered = (StrategicTablePotential) stp.reorder(List.of(y, x));

        // For [Y, X]: offsets are [1, 3] — position(y=j, x=i) = j*1 + i*3
        // Original [X, Y]: offset [1, 2]  — position(x=i, y=j) = i*1 + j*2
        // Mapping: orig[i + j*2] → new[j + i*3]
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                int origPos = i + j * 2;
                int newPos  = j + i * 3;
                assertEquals(origPos + 1.0, reordered.getValues()[newPos], 1e-12,
                        "values mismatch at (x=" + i + ", y=" + j + ")");
                assertSame(origTrees[origPos], reordered.strategyTrees[newPos],
                        "strategyTree mismatch at (x=" + i + ", y=" + j + ")");
            }
        }
    }

    @Test
    void reorderVariablesWithNullStrategyTreesDoesNotThrow() {
        State s0 = state("s0"), s1 = state("s1");
        State t0 = state("t0"), t1 = state("t1");
        Variable x = var("X", s0, s1);
        Variable y = var("Y", t0, t1);

        StrategicTablePotential stp = new StrategicTablePotential(List.of(x, y), PotentialRole.UNSPECIFIED);
        // strategyTrees is null (not set)
        assertNull(stp.strategyTrees);

        StrategicTablePotential reordered = (StrategicTablePotential) stp.reorder(List.of(y, x));
        assertNotNull(reordered);
        assertNull(reordered.strategyTrees, "strategyTrees should remain null");
    }

    // ------------------------------------------------------------------
    // reorder(Variable, State[]) — state reordering
    // ------------------------------------------------------------------

    @Test
    void reorderStatesRearrangesBothValuesAndStrategyTrees() {
        State a = state("a"), b = state("b"), c = state("c");
        Variable x = var("X", a, b, c); // 3 states
        Variable y = var("Y", state("t0"), state("t1")); // 2 states, stays first

        // [Y, X] — size 6
        // values: 1..6
        StrategicTablePotential stp = new StrategicTablePotential(List.of(y, x), PotentialRole.UNSPECIFIED);
        StrategyTree[] origTrees = new StrategyTree[6];
        for (int i = 0; i < 6; i++) {
            stp.getValues()[i] = i + 1.0;
            origTrees[i] = tree();
        }
        stp.strategyTrees = origTrees;

        // Reorder X's states: [a, b, c] → [c, b, a]
        // displacement for old state i: new_pos_of_state_i - i
        // old[0]=a → new[2]   displacement = +2
        // old[1]=b → new[1]   displacement =  0
        // old[2]=c → new[0]   displacement = -2
        StrategicTablePotential reordered = stp.reorder(x, new State[]{c, b, a});

        // offset for X in [Y,X] = 2 (numStates(Y)=2)
        // For each position i in original:
        //   indexOfState = (i / 2) % 3
        //   newIndex = i + displacement[indexOfState] * 2
        int offset = 2;
        int[] disp = {2, 0, -2};  // for states a, b, c
        double[] origValues = stp.getValues();
        for (int i = 0; i < 6; i++) {
            int stateIdx = (i / offset) % 3;
            int newIdx = i + disp[stateIdx] * offset;
            assertEquals(origValues[i], reordered.getValues()[newIdx], 1e-12,
                    "values mismatch at orig pos " + i);
            assertSame(origTrees[i], reordered.strategyTrees[newIdx],
                    "strategyTree mismatch at orig pos " + i);
        }
    }

    @Test
    void reorderStatesWithNullStrategyTreesDoesNotThrow() {
        State s0 = state("s0"), s1 = state("s1");
        Variable x = var("X", s0, s1);

        StrategicTablePotential stp = new StrategicTablePotential(List.of(x), PotentialRole.UNSPECIFIED);
        // strategyTrees is null
        assertNull(stp.strategyTrees);

        StrategicTablePotential reordered = stp.reorder(x, new State[]{s1, s0});
        assertNotNull(reordered);
        assertNull(reordered.strategyTrees);
    }

    @Test
    void reorderStatesReturnsStrategicTablePotential() {
        State s0 = state("s0"), s1 = state("s1");
        Variable x = var("X", s0, s1);

        StrategicTablePotential stp = new StrategicTablePotential(List.of(x), PotentialRole.UNSPECIFIED);
        Potential result = stp.reorder(x, new State[]{s1, s0});
        assertInstanceOf(StrategicTablePotential.class, result,
                "reorder(Variable, State[]) must return StrategicTablePotential, not TablePotential");
    }

    @Test
    void reorderVariablesReturnsStrategicTablePotential() {
        State s0 = state("s0"), s1 = state("s1");
        State t0 = state("t0"), t1 = state("t1");
        Variable x = var("X", s0, s1);
        Variable y = var("Y", t0, t1);

        StrategicTablePotential stp = new StrategicTablePotential(List.of(x, y), PotentialRole.UNSPECIFIED);
        Potential result = stp.reorder(List.of(y, x));
        assertInstanceOf(StrategicTablePotential.class, result,
                "reorder(List<Variable>) must return StrategicTablePotential");
    }

    @Test
    void reorderDoesNotMutateOriginal() {
        State s0 = state("s0"), s1 = state("s1");
        State t0 = state("t0"), t1 = state("t1");
        Variable x = var("X", s0, s1);
        Variable y = var("Y", t0, t1);

        StrategicTablePotential stp = new StrategicTablePotential(List.of(x, y), PotentialRole.UNSPECIFIED);
        stp.strategyTrees = new StrategyTree[]{tree(), tree(), tree(), tree()};
        double[] origValues = stp.getValues().clone();
        StrategyTree[] origTrees = stp.strategyTrees.clone();

        stp.reorder(List.of(y, x));

        assertArrayEquals(origValues, stp.getValues(), "reorder must not mutate original values");
        assertArrayEquals(origTrees, stp.strategyTrees, "reorder must not mutate original strategyTrees");
    }
}
