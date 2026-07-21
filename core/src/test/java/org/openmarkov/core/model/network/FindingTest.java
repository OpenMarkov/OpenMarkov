/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Finding}.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FindingTest {

    private Variable finiteVar;
    private Variable numericVar;
    private State state0;
    private State state1;

    @BeforeEach
    void setUp() {
        state0 = new State("yes");
        state1 = new State("no");
        finiteVar  = new Variable("Rain", new State[]{state0, state1});
        numericVar = new Variable("Temperature");
    }

    // -----------------------------------------------------------------------
    // Constructor: discrete variable + state index
    // -----------------------------------------------------------------------

    @Test
    void constructorWithStateIndexStoresVariable() {
        Finding f = new Finding(finiteVar, 0);
        assertSame(finiteVar, f.getVariable());
    }

    @Test
    void constructorWithStateIndexStoresIndex() {
        Finding f = new Finding(finiteVar, 1);
        assertEquals(1, f.getStateIndex());
    }

    @Test
    void constructorWithStateIndexSetsNumericalValueToNaN() {
        Finding f = new Finding(finiteVar, 0);
        assertTrue(Double.isNaN(f.numericalValue));
    }

    // -----------------------------------------------------------------------
    // Constructor: discrete variable + State object
    // -----------------------------------------------------------------------

    @Test
    void constructorWithStateObjectResolvesIndex() {
        Finding f = new Finding(finiteVar, state1);
        assertEquals(1, f.getStateIndex());
    }

    @Test
    void constructorWithStateObjectStoresVariable() {
        Finding f = new Finding(finiteVar, state0);
        assertSame(finiteVar, f.getVariable());
    }

    // -----------------------------------------------------------------------
    // Constructor: numerical variable
    // -----------------------------------------------------------------------

    @Test
    void constructorWithNumericalValueStoresValue() {
        Finding f = new Finding(numericVar, 37.5);
        assertEquals(37.5, f.getNumericalValue(), 1e-9);
    }

    @Test
    void constructorWithNumericalValueSetsStateIndexToMaxInt() {
        Finding f = new Finding(numericVar, 0.0);
        assertEquals(Integer.MAX_VALUE, f.getStateIndex());
    }

    // -----------------------------------------------------------------------
    // getState
    // -----------------------------------------------------------------------

    @Test
    void getStateReturnsCorrectStateName() {
        Finding f = new Finding(finiteVar, 0);
        assertEquals("yes", f.getState());
    }

    // -----------------------------------------------------------------------
    // getNumericalValue for finite-states variable
    // -----------------------------------------------------------------------

    @Test
    void getNumericalValueForFiniteStatesReturnsStateIndex() {
        // numericalValue is NaN → getNumericalValue() falls back to stateIndex
        Finding f = new Finding(finiteVar, 1);
        assertEquals(1.0, f.getNumericalValue(), 1e-9);
    }

    // -----------------------------------------------------------------------
    // setStateIndex / setNumericalValue
    // -----------------------------------------------------------------------

    @Test
    void setStateIndexUpdatesValue() {
        Finding f = new Finding(finiteVar, 0);
        f.setStateIndex(1);
        assertEquals(1, f.getStateIndex());
    }

    @Test
    void setNumericalValueUpdatesNumericalField() {
        Finding f = new Finding(numericVar, 10.0);
        f.setNumericalValue(20.0);
        assertEquals(20.0, f.getNumericalValue(), 1e-9);
    }

    // -----------------------------------------------------------------------
    // match
    // -----------------------------------------------------------------------

    @Test
    void matchReturnsTrueForExactName() {
        Finding f = new Finding(finiteVar, 0);
        assertTrue(f.match("Rain"));
    }

    @Test
    void matchReturnsFalseForDifferentName() {
        Finding f = new Finding(finiteVar, 0);
        assertFalse(f.match("Sun"));
    }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------

    @Test
    void toStringContainsVariableName() {
        Finding f = new Finding(finiteVar, 0);
        assertTrue(f.toString().contains("Rain"));
    }

    @Test
    void toStringContainsStateName() {
        Finding f = new Finding(finiteVar, 0);
        assertTrue(f.toString().contains("yes"));
    }

    @Test
    void toStringForNumericalVariableContainsValue() {
        Finding f = new Finding(numericVar, 42.0);
        assertTrue(f.toString().contains("42.0"));
    }
}
