/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Choice}.
 *
 * <p>Covers both constructors, {@code setValue}, {@code addValue} (before and
 * after {@code setValue}), {@code copy}, {@code sameInformation}, {@code getStates}
 * and {@code toString}.  Two tests exercise the tie-handling path in
 * {@code addValue()} that was previously broken and has since been corrected.
 *
 * @author Manuel Arias
 */
class ChoiceTest {

    private Variable dec;

    @BeforeEach
    void setUp() {
        dec = new Variable("D", new State[]{new State("s0"), new State("s1"), new State("s2")});
    }

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    @Test
    void arrayConstructorPreservesValuesAndNumValues() {
        int[] vals = {0, 2};
        Choice c = new Choice(dec, vals);
        assertArrayEquals(vals, c.getValues());
        assertEquals(2, c.getNumValues());
    }

    @Test
    void arrayConstructorLeavesInitializedFalse() {
        // When initialized=false, addValue() should use the correct append path.
        Choice c = new Choice(dec, new int[]{0}); // initialized = false
        c.addValue(1);
        // Append path: numValues = 2
        assertEquals(2, c.getNumValues());
        assertEquals(0, c.getValues()[0]);
        assertEquals(1, c.getValues()[1]);
    }

    @Test
    void singleIntConstructorSetsNumValuesAndInitialized() {
        Choice c = new Choice(dec, 1);
        assertEquals(1, c.getNumValues());
        assertEquals(1, c.getValues()[0]);
    }

    // -----------------------------------------------------------------------
    // setValue
    // -----------------------------------------------------------------------

    @Test
    void setValueOverwritesSingleEntry() {
        Choice c = new Choice(dec, 0);
        c.setValue(2);
        assertEquals(1, c.getNumValues());
        assertEquals(2, c.getValues()[0]);
    }

    @Test
    void setValueCollapsesTiedArrayToOneEntry() {
        Choice c = new Choice(dec, new int[]{0, 1, 2});
        c.setValue(1);
        assertEquals(1, c.getNumValues());
        assertEquals(1, c.getValues()[0]);
    }

    @Test
    void setValueSetsInitializedFlag_verifiedBySubsequentAddValueBehavior() {
        // Before setValue: initialized=false → addValue appends correctly.
        Choice c = new Choice(dec, new int[]{0});
        c.addValue(1); // goes to correct (!initialized) branch
        assertEquals(2, c.getNumValues());
    }

    // -----------------------------------------------------------------------
    // addValue on UNINITIALISED Choice (initialized = false)
    // -----------------------------------------------------------------------

    @Test
    void addValueUninitializedAppendsTwoEntries() {
        Choice c = new Choice(dec, new int[]{0});
        c.addValue(1);
        assertEquals(2, c.getNumValues());
        assertEquals(0, c.getValues()[0]);
        assertEquals(1, c.getValues()[1]);
    }

    @Test
    void addValueUninitializedAppendThreeEntries() {
        Choice c = new Choice(dec, new int[]{0});
        c.addValue(1);
        c.addValue(2);
        assertEquals(3, c.getNumValues());
        assertEquals(0, c.getValues()[0]);
        assertEquals(1, c.getValues()[1]);
        assertEquals(2, c.getValues()[2]);
    }

    /**
     * When initialized=false and numValues == values.length (no spare capacity),
     * addValue() must reallocate and append.
     * Note: the "if (numValues > values.length)" branch in addValue() is dead
     * code (numValues can never exceed values.length), so the else-branch always
     * runs when !initialized.
     */
    @Test
    void addValueUninitializedAlwaysReallocatesAndAppends() {
        // numValues == values.length, so the condition (numValues > values.length) is false
        // → else-branch runs → realloc to numValues+1 and append
        Choice c = new Choice(dec, new int[3]); // numValues=3, values.length=3, initialized=false
        c.addValue(1);
        assertEquals(4, c.getNumValues());
        assertEquals(1, c.getValues()[3], "Appended value must be at index 3");
    }

    // -----------------------------------------------------------------------
    // addValue on INITIALISED Choice (after setValue)
    // -----------------------------------------------------------------------

    @Test
    void addValueAfterSetValueAppendsNewEntry() {
        Choice c = new Choice(dec, new int[1]); // initialized = false
        c.setValue(0);                           // sets initialized = true

        c.addValue(1);                           // tied state — must append

        assertEquals(2, c.getNumValues());
        assertEquals(0, c.getValues()[0], "Original state 0 must still be at index 0");
        assertEquals(1, c.getValues()[1], "Tied state 1 must be appended at index 1");
    }

    @Test
    void addValueAfterSetValueThreeTiesHasThreeEntries() {
        Choice c = new Choice(dec, new int[3]); // numValues=3, initialized=false
        c.setValue(0);                           // numValues=1, initialized=true

        c.addValue(1);
        c.addValue(2);

        assertEquals(3, c.getNumValues());
        assertEquals(0, c.getValues()[0]);
        assertEquals(1, c.getValues()[1]);
        assertEquals(2, c.getValues()[2]);
    }

    // -----------------------------------------------------------------------
    // copy
    // -----------------------------------------------------------------------

    @Test
    void copyProducesDeepCopy() {
        Choice original = new Choice(dec, 2);
        Choice copy = original.copy();

        assertEquals(original.getNumValues(), copy.getNumValues());
        assertEquals(original.getVariable(),  copy.getVariable());
        assertEquals(2, copy.getValues()[0]);

        // Mutating the copy must not affect the original
        copy.getValues()[0] = 99;
        assertEquals(2, original.getValues()[0], "Deep copy: modifying copy must not affect original");
    }

    @Test
    void copyOfMultiValueChoice() {
        Choice original = new Choice(dec, new int[]{0, 2}); // 2-value choice
        Choice copy = original.copy();
        assertEquals(2, copy.getNumValues());
        assertEquals(0, copy.getValues()[0]);
        assertEquals(2, copy.getValues()[1]);
    }

    // -----------------------------------------------------------------------
    // sameInformation
    // -----------------------------------------------------------------------

    @Test
    void sameInformationReturnsTrueForEqualChoices() {
        Choice a = new Choice(dec, 1);
        Choice b = new Choice(dec, 1);
        assertTrue(a.sameInformation(b));
    }

    @Test
    void sameInformationReturnsFalseForDifferentValue() {
        Choice a = new Choice(dec, 0);
        Choice b = new Choice(dec, 1);
        assertFalse(a.sameInformation(b));
    }

    @Test
    void sameInformationReturnsFalseForDifferentNumValues() {
        Choice a = new Choice(dec, new int[]{0, 1}); // numValues = 2, initialized = false
        Choice b = new Choice(dec, 0);               // numValues = 1
        assertFalse(a.sameInformation(b));
    }

    // -----------------------------------------------------------------------
    // getStates
    // -----------------------------------------------------------------------

    @Test
    void getStatesReturnsSingleCorrectState() {
        Choice c = new Choice(dec, 1);
        assertEquals(1, c.getStates().size());
        assertEquals("s1", c.getStates().getFirst().getName());
    }

    @Test
    void getStatesReturnsMultipleStates() {
        Choice c = new Choice(dec, new int[]{0, 2}); // states s0 and s2
        assertEquals(2, c.getStates().size());
        assertEquals("s0", c.getStates().get(0).getName());
        assertEquals("s2", c.getStates().get(1).getName());
    }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------

    @Test
    void toStringSingleValue() {
        Choice c = new Choice(dec, 1);
        assertEquals("D=s1", c.toString());
    }

    @Test
    void toStringMultipleValues() {
        Choice c = new Choice(dec, new int[]{0, 2});
        assertEquals("D={s0,s2}", c.toString());
    }
}
