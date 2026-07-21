/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link TablePotential}.
 * Covers structural and mathematical invariants that must hold for any valid
 * combination of variables and values, complementing the example-based tests
 * in {@link TablePotentialTest} and {@link TablePotentialRegressionTest}.
 */
class TablePotentialPropertyTest {

    // -----------------------------------------------------------------------
    // Sizing invariants
    // -----------------------------------------------------------------------

    /**
     * {@code getTableSize()} must equal the product of the number of states
     * of every variable in the potential.
     */
    @Property
    void tableSizeIsProductOfAllStateCounts(@ForAll("variableLists") List<Variable> variables) {
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        int expectedSize = variables.stream()
                .mapToInt(Variable::getNumStates)
                .reduce(1, (a, b) -> a * b);
        assertThat(tp.getTableSize()).isEqualTo(expectedSize);
    }

    /**
     * The length of {@code getValues()} must always equal {@code getTableSize()}.
     */
    @Property
    void valuesLengthEqualsTableSize(@ForAll("variableLists") List<Variable> variables) {
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        assertThat(tp.getValues()).hasSize(tp.getTableSize());
    }

    /**
     * A potential built from a single variable must have {@code tableSize == numStates}.
     */
    @Property
    void singleVariablePotentialSizeEqualsNumStates(
            @ForAll @IntRange(min = 1, max = 30) int numStates) {
        Variable v = new Variable("X", numStates);
        TablePotential tp = new TablePotential(List.of(v), PotentialRole.CONDITIONAL_PROBABILITY);
        assertThat(tp.getTableSize()).isEqualTo(numStates);
    }

    // -----------------------------------------------------------------------
    // Offset invariants
    // -----------------------------------------------------------------------

    /**
     * For n variables with dimensions d0, d1, …, d(n-1), the offsets must
     * follow the row-major stride pattern:
     * offsets[0] = 1, offsets[i] = offsets[i-1] * d(i-1).
     * This is the fundamental indexing contract used throughout inference.
     */
    @Property
    void offsetsFollowRowMajorStridePattern(@ForAll("variableLists") List<Variable> variables) {
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        int[] offsets = tp.getOffsets();

        assertThat(offsets).hasSize(variables.size());

        int expectedOffset = 1;
        for (int i = 0; i < variables.size(); i++) {
            assertThat(offsets[i])
                    .as("offset[%d] for variable with %d states", i, variables.get(i).getNumStates())
                    .isEqualTo(expectedOffset);
            expectedOffset *= variables.get(i).getNumStates();
        }
    }

    /**
     * The product of all offsets * their corresponding state counts must
     * equal tableSize — which confirms the stride pattern covers the whole table.
     */
    @Property
    void offsetTimesStateSizeCoversWholeTable(@ForAll("variableLists") List<Variable> variables) {
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        int[] offsets = tp.getOffsets();
        int maxReachable = 0;
        for (int i = 0; i < variables.size(); i++) {
            maxReachable += offsets[i] * (variables.get(i).getNumStates() - 1);
        }
        // The highest addressable index must be exactly tableSize - 1
        assertThat(maxReachable).isEqualTo(tp.getTableSize() - 1);
    }

    // -----------------------------------------------------------------------
    // scalePotential invariants
    // -----------------------------------------------------------------------

    /**
     * After {@code scalePotential(k)}, every value must equal its original
     * value multiplied by k.
     */
    @Property
    void scalePotentialMultipliesAllValues(
            @ForAll("variableLists") List<Variable> variables,
            @ForAll @DoubleRange(min = 0.01, max = 100.0) double factor) {
        int size = variables.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        double[] initial = new double[size];
        Arrays.fill(initial, 1.0);
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, initial);

        double[] before = tp.getValues().clone();
        tp.scalePotential(factor);
        double[] after = tp.getValues();

        for (int i = 0; i < before.length; i++) {
            assertThat(after[i]).isCloseTo(before[i] * factor, within(1e-9));
        }
    }

    /**
     * Scaling by 1.0 must leave all values unchanged.
     */
    @Property
    void scalePotentialByOneIsIdentity(@ForAll("variableLists") List<Variable> variables) {
        int size = variables.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        double[] initial = new double[size];
        for (int i = 0; i < size; i++) initial[i] = i + 1.0;
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, initial);

        double[] before = tp.getValues().clone();
        tp.scalePotential(1.0);

        assertThat(tp.getValues()).containsExactly(before);
    }

    /**
     * Scaling twice (by k1 then k2) must be equivalent to scaling once by k1*k2.
     */
    @Property
    void scalePotentialIsComposable(
            @ForAll("variableLists") List<Variable> variables,
            @ForAll @DoubleRange(min = 0.1, max = 10.0) double k1,
            @ForAll @DoubleRange(min = 0.1, max = 10.0) double k2) {
        int size = variables.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        double[] initial = new double[size];
        Arrays.fill(initial, 1.0);

        TablePotential tpTwice = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, initial.clone());
        tpTwice.scalePotential(k1);
        tpTwice.scalePotential(k2);

        TablePotential tpOnce = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, initial.clone());
        tpOnce.scalePotential(k1 * k2);

        for (int i = 0; i < size; i++) {
            assertThat(tpTwice.getValues()[i]).isCloseTo(tpOnce.getValues()[i], within(1e-9));
        }
    }

    // -----------------------------------------------------------------------
    // Copy-constructor invariants
    // -----------------------------------------------------------------------

    /**
     * The copy constructor must produce a potential with identical structure
     * (same tableSize, same variables, same values).
     */
    @Property
    void copyConstructorPreservesStructure(@ForAll("variableLists") List<Variable> variables) {
        int size = variables.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        double[] values = new double[size];
        for (int i = 0; i < size; i++) values[i] = i;
        TablePotential original = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, values);
        TablePotential copy = new TablePotential(original);

        assertThat(copy.getTableSize()).isEqualTo(original.getTableSize());
        assertThat(copy.getVariables()).isEqualTo(original.getVariables());
        assertThat(copy.getValues()).containsExactly(original.getValues());
    }

    /**
     * Mutating the copy's values must not affect the original.
     */
    @Property
    void copyConstructorProducesIndependentValues(@ForAll("variableLists") List<Variable> variables) {
        int size = variables.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        double[] values = new double[size];
        Arrays.fill(values, 1.0);
        TablePotential original = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, values);
        double[] originalValuesBefore = original.getValues().clone();

        TablePotential copy = new TablePotential(original);
        Arrays.fill(copy.getValues(), 99.0);

        assertThat(original.getValues()).containsExactly(originalValuesBefore);
    }

    // -----------------------------------------------------------------------
    // setUniform invariant
    // -----------------------------------------------------------------------

    /**
     * After {@code setUniform()}, all values in the table must be equal.
     * (The exact uniform value depends on the role and variable structure,
     * but uniformity itself is the invariant.)
     */
    @Property
    void setUniformProducesEqualValues(@ForAll("variableLists") List<Variable> variables) {
        int size = variables.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        TablePotential tp = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, new double[size]);
        tp.setUniform();

        double[] values = tp.getValues();
        double first = values[0];
        for (double v : values) {
            assertThat(v).isCloseTo(first, within(1e-10));
        }
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    /**
     * Lists of 1–4 variables, each with 2–6 states, with unique names.
     * Kept small to avoid exponential blowup of tableSize.
     */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<List<Variable>> variableLists() {
        return Arbitraries.integers().between(1, 4).flatMap(numVars ->
            Arbitraries.integers().between(2, 6)
                .list().ofSize(numVars)
                .map(stateCounts -> {
                    List<Variable> vars = new ArrayList<>(numVars);
                    for (int i = 0; i < numVars; i++) {
                        vars.add(new Variable("V" + i, stateCounts.get(i)));
                    }
                    return vars;
                })
        );
    }
}
