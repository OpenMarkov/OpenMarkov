/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link TablePotential#setUncertainValuesConsistently} and the
 * column-placement helpers it relies on.
 *
 * <p>These cover three defects in the uncertain-values edit path:
 * <ul>
 *   <li><b>Bug B</b> — the uncertain-values array was sized to a single column
 *       ({@code uncertainValues.size()}) instead of the whole table, so editing any column
 *       with {@code basePosition > 0} threw {@link ArrayIndexOutOfBoundsException}.</li>
 *   <li><b>Bug C</b> — a fresh array was allocated on every call, discarding the uncertain
 *       values already assigned to other columns.</li>
 *   <li><b>Bug D</b> — restoring a single column of numeric values must not replace the whole
 *       {@code values} table; {@link TablePotential#placeValuesColumn} leaves the rest untouched.</li>
 * </ul>
 *
 * <p>Layout: P(A|B) with A (2 states) conditioned on B (3 states).
 * <pre>
 *   variables = [A, B], offsets A=1, B=2, tableSize = 6
 *   column for B=b0 → basePosition 0 (positions 0,1)
 *   column for B=b1 → basePosition 2 (positions 2,3)
 *   column for B=b2 → basePosition 4 (positions 4,5)
 * </pre>
 */
class SetUncertainValuesConsistentlyTest {

    private static final double DELTA = 1e-9;

    private Variable a;   // 2 states (conditioned variable, variable 0)
    private Variable b;   // 3 states (parent)
    private UncertainTablePotential pAB;

    @BeforeEach
    void setUp() {
        a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        b = new Variable("B", new State[]{new State("b0"), new State("b1"), new State("b2")});
        pAB = new UncertainTablePotential(List.of(a, b), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1,   // B=b0
                             0.7, 0.3,   // B=b1
                             0.4, 0.6}); // B=b2
    }

    // ------------------------------------------------------------------
    // Bug B — basePosition > 0 must not throw and must size the array to the whole table
    // ------------------------------------------------------------------

    @Test
    void editingColumnWithBasePositionGreaterThanZeroDoesNotThrow() {
        UncertainValue u0 = new UncertainValue(0.45);
        UncertainValue u1 = new UncertainValue(0.55);
        // column for B=b2 starts at basePosition 4
        assertDoesNotThrow(() ->
                pAB.setUncertainValuesConsistently(List.of(u0, u1), List.of(0.45, 0.55), 4));
    }

    @Test
    void uncertainArraySpansWholeTableNotASingleColumn() {
        pAB.setUncertainValuesConsistently(List.of(new UncertainValue(0.45), new UncertainValue(0.55)),
                List.of(0.45, 0.55), 4);
        assertEquals(pAB.getValues().length, pAB.getUncertainValues().length,
                "Uncertain-values array must run parallel to the values array");
    }

    @Test
    void uncertainValuesAndNumericValuesAreStoredConsistentlyAtTheEditedColumn() {
        UncertainValue u0 = new UncertainValue(0.45);
        UncertainValue u1 = new UncertainValue(0.55);
        pAB.setUncertainValuesConsistently(List.of(u0, u1), List.of(0.45, 0.55), 4);

        UncertainValue[] uv = pAB.getUncertainValues();
        assertSame(u0, uv[4], "Uncertain value placed at the start of the edited column");
        assertSame(u1, uv[5]);
        // numeric values updated consistently at the same positions
        assertEquals(0.45, pAB.getValues()[4], DELTA);
        assertEquals(0.55, pAB.getValues()[5], DELTA);
    }

    @Test
    void editedColumnLandsOnThePositionsDictatedByTheOffsets() {
        // The middle column is the parent configuration B=b1. Its base position and the position of
        // each conditioned-variable (A) state are derived from the offsets, not hard-coded, so this
        // asserts the placement honours the table structure.
        // coordinates = [A-state, B-state]; offsets A=1, B=2
        int basePosition = pAB.getPosition(new int[]{0, 1});             // (A=a0, B=b1)
        int posA0 = pAB.getPosition(new int[]{0, 1});                    // = basePosition
        int posA1 = pAB.getPosition(new int[]{1, 1});                    // = basePosition + offsets[A]
        assertEquals(2, basePosition);
        assertEquals(2, posA0);
        assertEquals(3, posA1);

        UncertainValue uA0 = new UncertainValue(0.25);
        UncertainValue uA1 = new UncertainValue(0.75);
        pAB.setUncertainValuesConsistently(List.of(uA0, uA1), List.of(0.25, 0.75), basePosition);

        UncertainValue[] uv = pAB.getUncertainValues();
        assertSame(uA0, uv[posA0], "Uncertain value for (A=a0,B=b1) must land at its offset position");
        assertSame(uA1, uv[posA1], "Uncertain value for (A=a1,B=b1) must land at its offset position");
        assertEquals(0.25, pAB.getValues()[posA0], DELTA);
        assertEquals(0.75, pAB.getValues()[posA1], DELTA);
        // columns for B=b0 and B=b2 are untouched
        assertNull(uv[pAB.getPosition(new int[]{0, 0})]);
        assertNull(uv[pAB.getPosition(new int[]{0, 2})]);
        assertEquals(0.9, pAB.getValues()[pAB.getPosition(new int[]{0, 0})], DELTA);
        assertEquals(0.6, pAB.getValues()[pAB.getPosition(new int[]{1, 2})], DELTA);
    }

    // ------------------------------------------------------------------
    // Bug C — editing one column must preserve uncertain values of other columns
    // ------------------------------------------------------------------

    @Test
    void editingSecondColumnPreservesFirstColumnUncertainty() {
        UncertainValue first0 = new UncertainValue(0.9);
        UncertainValue first1 = new UncertainValue(0.1);
        // First set uncertainty on column B=b0 (basePosition 0)
        pAB.setUncertainValuesConsistently(List.of(first0, first1), List.of(0.9, 0.1), 0);
        // Then edit column B=b2 (basePosition 4)
        pAB.setUncertainValuesConsistently(List.of(new UncertainValue(0.45), new UncertainValue(0.55)),
                List.of(0.45, 0.55), 4);

        UncertainValue[] uv = pAB.getUncertainValues();
        assertSame(first0, uv[0], "Column B=b0 uncertainty must survive a later edit of another column");
        assertSame(first1, uv[1]);
        // and the newly edited column is present too
        assertNotNull(uv[4]);
        assertNotNull(uv[5]);
    }

    @Test
    void untouchedColumnsRemainNull() {
        pAB.setUncertainValuesConsistently(List.of(new UncertainValue(0.45), new UncertainValue(0.55)),
                List.of(0.45, 0.55), 4);
        UncertainValue[] uv = pAB.getUncertainValues();
        // column B=b1 (positions 2,3) was never edited
        assertNull(uv[2]);
        assertNull(uv[3]);
    }

    // ------------------------------------------------------------------
    // Bug D — placeValuesColumn restores one column in place, leaving the rest intact
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // Column-size consistency guard — a wrong-sized column must fail loudly,
    // not corrupt a neighbouring column or throw an opaque AIOOBE.
    // ------------------------------------------------------------------

    @Test
    void placeValuesColumnRejectsAColumnThatOverflowsTheTable() {
        // basePosition 4 leaves room for 2 cells (positions 4,5); a 3-cell column does not fit.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pAB.placeValuesColumn(List.of(0.1, 0.2, 0.3), 4));
        assertTrue(ex.getMessage().contains("does not fit"));
        // the table must be left untouched
        assertEquals(0.4, pAB.getValues()[4], DELTA);
        assertEquals(0.6, pAB.getValues()[5], DELTA);
    }

    @Test
    void placeUncertainColumnRejectsAColumnThatOverflowsTheTable() {
        // Allocate the uncertain-values array first (placeUncertainColumn is a low-level helper that
        // assumes it already exists).
        pAB.setUncertainValuesConsistently(List.of(new UncertainValue(0.9), new UncertainValue(0.1)),
                List.of(0.9, 0.1), 0);
        assertThrows(IllegalArgumentException.class,
                () -> pAB.placeUncertainColumn(
                        List.of(new UncertainValue(0.1), new UncertainValue(0.2), new UncertainValue(0.3)),
                        a, 4));
    }

    @Test
    void placeValuesColumnAcceptsAColumnThatFitsExactly() {
        assertDoesNotThrow(() -> pAB.placeValuesColumn(List.of(0.3, 0.7), 4));
        assertEquals(0.3, pAB.getValues()[4], DELTA);
        assertEquals(0.7, pAB.getValues()[5], DELTA);
    }

    @Test
    void placeValuesColumnUpdatesOnlyTheTargetColumn() {
        pAB.placeValuesColumn(List.of(0.25, 0.75), 2);   // edit column B=b1
        double[] values = pAB.getValues();
        assertEquals(6, values.length, "Values table length must be preserved");
        // edited column
        assertEquals(0.25, values[2], DELTA);
        assertEquals(0.75, values[3], DELTA);
        // neighbouring columns untouched
        assertEquals(0.9, values[0], DELTA);
        assertEquals(0.1, values[1], DELTA);
        assertEquals(0.4, values[4], DELTA);
        assertEquals(0.6, values[5], DELTA);
    }
}
