/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link TablePotential#reorder(List)} and
 * {@link TablePotential#tableProject(EvidenceCase, org.openmarkov.core.inference.InferenceOptions)}.
 *
 * <p>Both methods contain non-trivial index arithmetic that is critical for
 * inference correctness. A wrong accumulated-offset or first-position calculation
 * produces silent wrong results — no exception, just bad probabilities.
 *
 * <p>The key invariant tested for {@code reorder}: for any assignment of states
 * to variables, the value in the reordered potential equals the value in the
 * original. This is verified by calling {@link TablePotential#getValue(List, int[])}
 * on both potentials with the same variable list, which resolves positions by
 * variable identity and is therefore ordering-independent.
 *
 * <p>The key invariant for {@code tableProject}: for every state of an unobserved
 * variable, the projected value equals the original value at (evidence state,
 * unobserved state).
 */
class TablePotentialReorderProjectPropertyTest {

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Creates a potential filled with distinct sequential values (1.0, 2.0, …)
     * so that every cell is uniquely identifiable by value.
     */
    private TablePotential filledPotential(List<Variable> vars) {
        int size = vars.stream().mapToInt(Variable::getNumStates).reduce(1, (a, b) -> a * b);
        double[] values = new double[size];
        for (int i = 0; i < size; i++) values[i] = i + 1.0;
        return new TablePotential(vars, PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    // -----------------------------------------------------------------------
    // reorder(List<Variable>) — variable-order permutation
    // -----------------------------------------------------------------------

    /**
     * Swapping the two variables of a 2-variable potential must preserve the
     * value for every state assignment.
     *
     * <p>This is the fundamental correctness invariant for variable reordering:
     * changing the internal variable order is an implementation detail that
     * must be invisible to any caller looking up values by variable identity.
     */
    @Property
    void reorderSwap2Vars_preservesValueForAllConfigurations(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) {
        Variable a = new Variable("A", sA);
        Variable b = new Variable("B", sB);
        TablePotential original = filledPotential(List.of(a, b));
        TablePotential swapped = (TablePotential) original.reorder(List.of(b, a));

        for (int ia = 0; ia < sA; ia++) {
            for (int ib = 0; ib < sB; ib++) {
                double origVal = original.getValue(List.of(a, b), new int[]{ia, ib});
                double swapVal = swapped.getValue(List.of(a, b), new int[]{ia, ib});
                assertThat(swapVal)
                        .as("value at (A=%d, B=%d) must be identical after swapping variable order", ia, ib)
                        .isEqualTo(origVal);
            }
        }
    }

    /**
     * Rotating three variables [A,B,C] → [C,A,B] must preserve the value for
     * every state assignment.
     *
     * <p>Tests the accumulated-offset arithmetic in the 3-variable case, where
     * a wrong offset for the middle variable can cancel out in the 2-variable
     * case but surface here.
     */
    @Property
    void reorderThreeVars_preservesValueForAllConfigurations(
            @ForAll @IntRange(min = 2, max = 4) int sA,
            @ForAll @IntRange(min = 2, max = 4) int sB,
            @ForAll @IntRange(min = 2, max = 4) int sC) {
        Variable a = new Variable("A", sA);
        Variable b = new Variable("B", sB);
        Variable c = new Variable("C", sC);
        TablePotential original = filledPotential(List.of(a, b, c));
        TablePotential reordered = (TablePotential) original.reorder(List.of(c, a, b));

        for (int ia = 0; ia < sA; ia++) {
            for (int ib = 0; ib < sB; ib++) {
                for (int ic = 0; ic < sC; ic++) {
                    double origVal = original.getValue(List.of(a, b, c), new int[]{ia, ib, ic});
                    double reordVal = reordered.getValue(List.of(a, b, c), new int[]{ia, ib, ic});
                    assertThat(reordVal)
                            .as("value at (A=%d, B=%d, C=%d) must be preserved after [C,A,B] rotation", ia, ib, ic)
                            .isEqualTo(origVal);
                }
            }
        }
    }

    /**
     * Reordering with the same variable order must produce an identical values array.
     */
    @Property
    void reorderSameOrder_producesIdenticalValues(@ForAll("variableLists") List<Variable> variables) {
        TablePotential original = filledPotential(variables);
        double[] before = original.getValues().clone();
        TablePotential reordered = (TablePotential) original.reorder(new ArrayList<>(variables));
        assertThat(reordered.getValues()).containsExactly(before);
    }

    /**
     * Swapping [A,B] → [B,A] and then back to [A,B] must restore the original
     * values array exactly (round-trip identity).
     */
    @Property
    void reorderRoundTrip_restoresOriginalValues(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) {
        Variable a = new Variable("A", sA);
        Variable b = new Variable("B", sB);
        TablePotential original = filledPotential(List.of(a, b));
        double[] originalValues = original.getValues().clone();

        TablePotential swapped = (TablePotential) original.reorder(List.of(b, a));
        TablePotential restored = (TablePotential) swapped.reorder(List.of(a, b));

        assertThat(restored.getValues()).containsExactly(originalValues);
    }

    /**
     * Reordering must not change the table size.
     */
    @Property
    void reorder_preservesTableSize(@ForAll("variableLists") List<Variable> variables) {
        TablePotential original = filledPotential(variables);
        List<Variable> reversed = new ArrayList<>(variables);
        Collections.reverse(reversed);
        TablePotential reordered = (TablePotential) original.reorder(reversed);
        assertThat(reordered.getTableSize()).isEqualTo(original.getTableSize());
    }

    /**
     * The multiset of values (sorted) must be identical before and after reordering
     * — no value is gained, lost, or duplicated by the permutation.
     */
    @Property
    void reorder_preservesValueMultiset(@ForAll("variableLists") List<Variable> variables) {
        TablePotential original = filledPotential(variables);
        List<Variable> reversed = new ArrayList<>(variables);
        Collections.reverse(reversed);
        TablePotential reordered = (TablePotential) original.reorder(reversed);

        double[] origSorted = original.getValues().clone();
        double[] reordSorted = reordered.getValues().clone();
        Arrays.sort(origSorted);
        Arrays.sort(reordSorted);
        assertThat(reordSorted).containsExactly(origSorted);
    }

    // -----------------------------------------------------------------------
    // tableProject — evidence projection
    // -----------------------------------------------------------------------

    /**
     * Projecting with null evidence must return the same potential object.
     * (The code path that checks {@code numVariables == numUnobservedVariables}
     * and returns {@code this}.)
     */
    @Property
    void tableProject_nullEvidence_returnsSameObject(
            @ForAll("variableLists") List<Variable> variables) throws NonProjectablePotentialException {
        TablePotential tp = filledPotential(variables);
        TablePotential projected = tp.tableProject(null, null);
        assertThat(projected).isSameAs(tp);
    }

    /**
     * When all variables are observed, the result must contain exactly one value
     * equal to the original table value at the observed state combination.
     */
    @Property
    void tableProject_allVariablesObserved_returnsScalarMatchingOriginal(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB,
            @ForAll @IntRange(min = 0, max = 4) int rawStateA,
            @ForAll @IntRange(min = 0, max = 4) int rawStateB)
            throws
            IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        int stateA = rawStateA % sA;
        int stateB = rawStateB % sB;
        Variable a = new Variable("A", sA);
        Variable b = new Variable("B", sB);
        TablePotential original = filledPotential(List.of(a, b));

        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(a, stateA));
        ev.addFinding(new Finding(b, stateB));

        TablePotential projected = original.tableProject(ev, null);

        assertThat(projected.getTableSize()).isEqualTo(1);
        double expectedVal = original.getValue(List.of(a, b), new int[]{stateA, stateB});
        assertThat(projected.getValues()[0])
                .as("scalar projection at (A=%d, B=%d) must equal original value", stateA, stateB)
                .isEqualTo(expectedVal);
    }

    /**
     * Projecting one variable out of a 2-variable potential must yield a
     * single-variable result whose values match the corresponding slice of the
     * original table.
     *
     * <p>This is the core correctness check for {@code tableProject}: a wrong
     * {@code firstPosition} or a wrong accumulated offset silently produces
     * shifted or scrambled values.
     */
    @Property
    void tableProject_oneVarObserved_eachSliceMatchesOriginal(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB,
            @ForAll @IntRange(min = 0, max = 4) int rawStateA)
            throws
            IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        int stateA = rawStateA % sA;
        Variable a = new Variable("A", sA);
        Variable b = new Variable("B", sB);
        TablePotential original = filledPotential(List.of(a, b));

        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(a, stateA));

        TablePotential projected = original.tableProject(ev, null);

        assertThat(projected.getVariables()).containsExactly(b);
        assertThat(projected.getTableSize()).isEqualTo(sB);

        for (int ib = 0; ib < sB; ib++) {
            double expectedVal = original.getValue(List.of(a, b), new int[]{stateA, ib});
            double projectedVal = projected.getValue(List.of(b), new int[]{ib});
            assertThat(projectedVal)
                    .as("projected value at B=%d (A fixed to %d) must equal original slice", ib, stateA)
                    .isEqualTo(expectedVal);
        }
    }

    /**
     * The table size of the projected potential must equal the product of the
     * number of states of the unobserved variables.
     */
    @Property
    void tableProject_resultTableSize_equalsProductOfUnobservedStates(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB,
            @ForAll @IntRange(min = 2, max = 5) int sC,
            @ForAll @IntRange(min = 0, max = 4) int rawStateA)
            throws
            IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        int stateA = rawStateA % sA;
        Variable a = new Variable("A", sA);
        Variable b = new Variable("B", sB);
        Variable c = new Variable("C", sC);
        TablePotential original = filledPotential(List.of(a, b, c));

        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(a, stateA));

        TablePotential projected = original.tableProject(ev, null);

        assertThat(projected.getTableSize())
                .as("projected table size must equal sB * sC = %d * %d", sB, sC)
                .isEqualTo(sB * sC);
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    /** Lists of 1–4 variables with 2–5 states each, with unique names. */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<List<Variable>> variableLists() {
        return Arbitraries.integers().between(1, 4).flatMap(numVars ->
                Arbitraries.integers().between(2, 5)
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
