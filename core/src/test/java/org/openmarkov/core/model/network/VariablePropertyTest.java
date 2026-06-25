/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link Variable}.
 * Complements {@link VariableTest} (which covers specific examples) by verifying
 * invariants that must hold for arbitrary inputs.
 */
class VariablePropertyTest {

    // -----------------------------------------------------------------------
    // Name / baseName / timeSlice invariants
    // -----------------------------------------------------------------------

    /**
     * After constructing a Variable with any alpha name, the full {@code name}
     * field must equal {@code baseName} for non-temporal variables, or
     * {@code baseName + " [" + timeSlice + "]"} for temporal ones.
     */
    @Property
    void nameIsConsistentWithBaseNameAndTimeSlice(@ForAll("alphaNames") String name) {
        Variable v = new Variable(name, 2);
        if (v.isTemporal()) {
            assertThat(v.getName()).isEqualTo(v.getBaseName() + " [" + v.getTimeSlice() + "]");
        } else {
            assertThat(v.getName()).isEqualTo(v.getBaseName());
            assertThat(v.getName()).isEqualTo(name);
        }
    }

    /**
     * After calling {@code setTimeSlice(t)}, the name must always end with {@code " [t]"},
     * {@code isTemporal()} must be true, and {@code getBaseName()} must be unchanged.
     */
    @Property
    void setTimeSliceUpdatesNameAndPreservesBaseName(
            @ForAll("alphaNames") String baseName,
            @ForAll @IntRange(max = 100) int timeSlice) {
        Variable v = new Variable(baseName, 2);
        v.setTimeSlice(timeSlice);

        assertThat(v.isTemporal()).isTrue();
        assertThat(v.getTimeSlice()).isEqualTo(timeSlice);
        assertThat(v.getBaseName()).isEqualTo(baseName);
        assertThat(v.getName()).isEqualTo(baseName + " [" + timeSlice + "]");
    }

    /**
     * A variable constructed with a non-temporal name must not be temporal.
     */
    @Property
    void variableWithPlainNameIsNotTemporal(@ForAll("alphaNames") String name) {
        Variable v = new Variable(name, 2);
        // Alpha-only names cannot accidentally match " [N]" syntax
        assertThat(v.isTemporal()).isFalse();
        assertThat(v.getTimeSlice()).isEqualTo(Variable.noTemporalTimeSlice);
    }

    // -----------------------------------------------------------------------
    // State count invariants
    // -----------------------------------------------------------------------

    /**
     * {@code getNumStates()} must equal the value passed to the int constructor.
     */
    @Property
    void numStatesMatchesIntConstructor(@ForAll @IntRange(min = 1, max = 50) int numStates) {
        Variable v = new Variable("X", numStates);
        assertThat(v.getNumStates()).isEqualTo(numStates);
    }

    /**
     * {@code getNumStates()} must equal the length of the State array passed to the constructor.
     */
    @Property
    void numStatesMatchesStateArrayLength(@ForAll("stateArrays") State[] states) {
        Variable v = new Variable("X", states);
        assertThat(v.getNumStates()).isEqualTo(states.length);
    }

    /**
     * {@code getStateIndex(getStateName(i)) == i} for every valid index — i.e.
     * getStateIndex is the left-inverse of getStateName.
     */
    @Property
    void stateIndexIsInverseOfStateName(@ForAll("stateArrays") State[] states) {
        Variable v = new Variable("X", states);
        for (int i = 0; i < states.length; i++) {
            assertThat(v.getStateIndex(v.getStateName(i))).isEqualTo(i);
        }
    }

    /**
     * {@code containsState} must return true for every state that was passed
     * to the constructor.
     */
    @Property
    void containsStateReturnsTrueForConstructedStates(@ForAll("stateArrays") State[] states) {
        Variable v = new Variable("X", states);
        for (State s : states) {
            assertThat(v.containsState(s.getName())).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Copy-constructor invariants
    // -----------------------------------------------------------------------

    /**
     * The copy constructor must produce a variable with the same observable
     * properties as the original.
     */
    @Property
    void copyConstructorPreservesAllObservableProperties(
            @ForAll("discreteVariables") Variable original) {
        Variable copy = new Variable(original);

        assertThat(copy.getName()).isEqualTo(original.getName());
        assertThat(copy.getBaseName()).isEqualTo(original.getBaseName());
        assertThat(copy.getNumStates()).isEqualTo(original.getNumStates());
        assertThat(copy.getVariableType()).isEqualTo(original.getVariableType());
        assertThat(copy.isTemporal()).isEqualTo(original.isTemporal());
        if (original.isTemporal()) {
            assertThat(copy.getTimeSlice()).isEqualTo(original.getTimeSlice());
        }
    }

    /**
     * Renaming the copy must not alter the original's name.
     */
    @Property
    void copyIsIndependentFromOriginalForNameMutation(
            @ForAll("discreteVariables") Variable original) {
        String originalName = original.getName();
        Variable copy = new Variable(original);
        copy.setName("Renamed");
        assertThat(original.getName()).isEqualTo(originalName);
    }

    // -----------------------------------------------------------------------
    // Delta potential invariants
    // -----------------------------------------------------------------------

    /**
     * A delta potential created for any state of a variable must sum to 1.0.
     */
    @Property
    void deltaTablePotentialSumsToOne(@ForAll("discreteVariables") Variable v) {
        for (int i = 0; i < v.getNumStates(); i++) {
            TablePotential delta = v.createDeltaTablePotential(i);
            double sum = Arrays.stream(delta.getValues()).sum();
            assertThat(sum).isCloseTo(1.0, within(1e-10));
        }
    }

    /**
     * A delta potential must have exactly one non-zero entry, equal to 1.0,
     * at the position of the chosen state.
     */
    @Property
    void deltaTablePotentialHasExactlyOneNonZeroEntry(@ForAll("discreteVariables") Variable v) {
        for (int i = 0; i < v.getNumStates(); i++) {
            TablePotential delta = v.createDeltaTablePotential(i);
            double[] values = delta.getValues();
            long nonZeroCount = Arrays.stream(values).filter(val -> val != 0.0).count();
            assertThat(nonZeroCount).isEqualTo(1);
            assertThat(values[i]).isEqualTo(1.0);
        }
    }

    /**
     * The delta potential for state i must be zero at every other position j != i.
     */
    @Property
    void deltaTablePotentialIsZeroAtAllOtherPositions(@ForAll("discreteVariables") Variable v) {
        for (int i = 0; i < v.getNumStates(); i++) {
            double[] values = v.createDeltaTablePotential(i).getValues();
            for (int j = 0; j < values.length; j++) {
                if (j != i) {
                    assertThat(values[j]).isEqualTo(0.0);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    /** Alpha strings: safe base names that contain no " [" sequences. */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<String> alphaNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    /** State arrays with distinct names (s0, s1, …, sN). */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<State[]> stateArrays() {
        return Arbitraries.integers().between(1, 10).map(n -> {
            State[] states = new State[n];
            for (int i = 0; i < n; i++) {
                states[i] = new State("s" + i);
            }
            return states;
        });
    }

    /** Discrete variables with an alpha name and between 1 and 10 states. */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<Variable> discreteVariables() {
        return Arbitraries.integers().between(1, 10).flatMap(numStates ->
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                .map(name -> new Variable(name, numStates))
        );
    }
}
