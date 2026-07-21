/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Algebraic invariant tests for {@link DiscretePotentialOperations}.
 *
 * <p>These tests protect against silent numerical regressions in the operations
 * that drive Variable Elimination inference ({@code multiplyAndMarginalize},
 * {@code marginalize}, {@code normalize}).  They are particularly relevant when
 * refactoring the potential class hierarchy (Rediseño-Opción-3): a change that
 * rearranges index calculations may produce numerically wrong results without
 * throwing any exception, and these tests catch that.</p>
 *
 * <p>All expected values are pre-computed by hand from the fixtures in
 * {@link SharedTestUtilities}:</p>
 * <ul>
 *   <li>{@code tpAB}  – P(A,B):  variables [A(3), B(3)], 9 entries.</li>
 *   <li>{@code tpA}   – P(A):    variable  [A(3)],       3 entries.</li>
 *   <li>{@code tpCAB} – P(C,A,D): variables [C(2), A(3), D(2)], 12 entries
 *       (note: field name is tpCAB but the third variable is D, not B).</li>
 *   <li>{@code tpConstant07} – constant 0.7.</li>
 * </ul>
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DiscretePotentialOperationsAlgebraicInvariantsTest {

    /** Tolerance for double comparisons. */
    private static final double DELTA = 1.0e-8;

    private SharedTestUtilities u;

    @BeforeEach
    public void setUp() {
        u = new SharedTestUtilities();
    }

    // -----------------------------------------------------------------------
    // Marginalize — single-variable elimination
    // -----------------------------------------------------------------------

    /**
     * tpAB = P(A,B).  Eliminating A gives P(B).
     * <pre>
     * P(B=0) = P(A=0,B=0) + P(A=1,B=0) + P(A=2,B=0) = 0.1+0.2+0.7 = 1.0
     * P(B=1) = 0.2+0.5+0.3 = 1.0
     * P(B=2) = 0.6+0.3+0.1 = 1.0
     * </pre>
     */
    @Test
    public void marginalize_eliminatesFirstVariable_producesCorrectValues() {
        TablePotential result = DiscretePotentialOperations.marginalize(u.tpAB, u.a);

        assertEquals(1, result.getVariables().size(), "Result should have 1 variable (B)");
        assertEquals(u.b, result.getVariables().getFirst());
        assertEquals(3, result.getValues().length);
        assertArrayEquals(new double[]{1.0, 1.0, 1.0}, result.getValues(), DELTA);
    }

    /**
     * tpAB = P(A,B).  Eliminating B gives P(A).
     * <pre>
     * P(A=0) = P(A=0,B=0) + P(A=0,B=1) + P(A=0,B=2) = 0.1+0.2+0.6 = 0.9
     * P(A=1) = 0.2+0.5+0.3 = 1.0
     * P(A=2) = 0.7+0.3+0.1 = 1.1
     * </pre>
     */
    @Test
    public void marginalize_eliminatesSecondVariable_producesCorrectValues() {
        TablePotential result = DiscretePotentialOperations.marginalize(u.tpAB, u.b);

        assertEquals(1, result.getVariables().size(), "Result should have 1 variable (A)");
        assertEquals(u.a, result.getVariables().getFirst());
        assertArrayEquals(new double[]{0.9, 1.0, 1.1}, result.getValues(), DELTA);
    }

    /**
     * The {@code marginalize(potential, variablesOfInterest)} overload should
     * keep only the listed variables and produce the same result as the
     * single-variable-elimination overload.
     */
    @Test
    public void marginalize_byVariablesOfInterest_keepsSingleVariable() {
        // Keep A → eliminates B — same as marginalize(tpAB, b)
        TablePotential keepA = DiscretePotentialOperations.marginalize(u.tpAB, List.of(u.a));
        TablePotential elimB = DiscretePotentialOperations.marginalize(u.tpAB, u.b);

        assertEquals(1, keepA.getVariables().size());
        assertArrayEquals(elimB.getValues(), keepA.getValues(), DELTA);
        assertArrayEquals(new double[]{0.9, 1.0, 1.1}, keepA.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Marginalize — commutativity
    // -----------------------------------------------------------------------

    /**
     * Eliminating variables one at a time in either order must give the same
     * result as eliminating them both at once.
     *
     * <p>tpCAD = P(C,A,D).  Marginalizing C and then A must equal marginalizing
     * A and then C.  Both paths should yield P(D) = {3.0, 3.0}.</p>
     */
    @Test
    public void marginalize_commutativity_twoVariablesInEitherOrderMatch() {
        // Path 1: eliminate C, then A
        TablePotential elimC    = DiscretePotentialOperations.marginalize(u.tpCAB, u.c);
        TablePotential elimCA   = DiscretePotentialOperations.marginalize(elimC, u.a);

        // Path 2: eliminate A, then C
        TablePotential elimA    = DiscretePotentialOperations.marginalize(u.tpCAB, u.a);
        TablePotential elimAC   = DiscretePotentialOperations.marginalize(elimA, u.c);

        assertEquals(1, elimCA.getVariables().size());
        assertEquals(1, elimAC.getVariables().size());
        assertArrayEquals(new double[]{3.0, 3.0}, elimCA.getValues(), DELTA);
        assertArrayEquals(elimCA.getValues(), elimAC.getValues(), DELTA,
                "Marginalizing in different orders must yield identical results");
    }

    /**
     * Marginalizing by keeping a single variable in one call must equal the
     * stepwise result.
     */
    @Test
    public void marginalize_commutativity_matchesKeepSingleVariable() {
        // Direct: keep only D
        TablePotential direct = DiscretePotentialOperations.marginalize(u.tpCAB, List.of(u.d));

        // Stepwise: eliminate C, then A
        TablePotential elimC  = DiscretePotentialOperations.marginalize(u.tpCAB, u.c);
        TablePotential stepwise = DiscretePotentialOperations.marginalize(elimC, u.a);

        assertEquals(1, direct.getVariables().size(), "keepD result should have 1 variable (D)");
        assertArrayEquals(stepwise.getValues(), direct.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // MultiplyAndMarginalize — exact values
    // -----------------------------------------------------------------------

    /**
     * {@code multiplyAndMarginalize([tpAB, tpA], keep=[A], elim=[B])}
     *
     * <p>Intermediate product P(A,B)*P(A):
     * <pre>
     * (A=0,B=0): 0.1*0.5 = 0.05    (A=0,B=1): 0.2*0.5 = 0.10    (A=0,B=2): 0.6*0.5 = 0.30
     * (A=1,B=0): 0.2*0.4 = 0.08    (A=1,B=1): 0.5*0.4 = 0.20    (A=1,B=2): 0.3*0.4 = 0.12
     * (A=2,B=0): 0.7*0.1 = 0.07    (A=2,B=1): 0.3*0.1 = 0.03    (A=2,B=2): 0.1*0.1 = 0.01
     * </pre>
     * After eliminating B:
     * P(A=0) = 0.05+0.10+0.30 = 0.45
     * P(A=1) = 0.08+0.20+0.12 = 0.40
     * P(A=2) = 0.07+0.03+0.01 = 0.11
     * </p>
     */
    @Test
    public void multiplyAndMarginalize_keepA_eliminateB_producesCorrectValues() {
        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(u.tpAB, u.tpA), List.of(u.a), List.of(u.b));

        assertEquals(1, result.getVariables().size());
        assertEquals(u.a, result.getVariables().getFirst());
        assertArrayEquals(new double[]{0.45, 0.40, 0.11}, result.getValues(), DELTA);
    }

    /**
     * {@code multiplyAndMarginalize([tpAB, tpA], keep=[B], elim=[A])}
     *
     * <p>From the same intermediate product:
     * P(B=0) = 0.05+0.08+0.07 = 0.20
     * P(B=1) = 0.10+0.20+0.03 = 0.33
     * P(B=2) = 0.30+0.12+0.01 = 0.43
     * </p>
     */
    @Test
    public void multiplyAndMarginalize_keepB_eliminateA_producesCorrectValues() {
        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(u.tpAB, u.tpA), List.of(u.b), List.of(u.a));

        assertEquals(1, result.getVariables().size());
        assertEquals(u.b, result.getVariables().getFirst());
        assertArrayEquals(new double[]{0.20, 0.33, 0.43}, result.getValues(), DELTA);
    }

    /**
     * The single-variable-to-eliminate overload must match the explicit keep/elim overload.
     */
    @Test
    public void multiplyAndMarginalize_singleVariableOverload_matchesExplicitOverload() {
        TablePotential explicit = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(u.tpAB), List.of(u.b), List.of(u.a));

        TablePotential singleVar = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(u.tpAB), u.a);   // eliminates A

        assertArrayEquals(explicit.getValues(), singleVar.getValues(), DELTA);
        assertArrayEquals(new double[]{1.0, 1.0, 1.0}, singleVar.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Distributivity: multiply-then-marginalize == multiplyAndMarginalize
    // -----------------------------------------------------------------------

    /**
     * Two-step (multiply, then marginalize) must produce the same result as the
     * one-step {@code multiplyAndMarginalize}.  This is the distributivity law
     * exploited by Variable Elimination.
     */
    @Test
    public void distributivity_multiplyThenMarginalizeEqualsDirectCall() {
        // Two-step
        TablePotential product = DiscretePotentialOperations.multiply(List.of(u.tpAB, u.tpA));
        TablePotential twoStep = DiscretePotentialOperations.marginalize(product, u.b); // eliminate B

        // One-step
        TablePotential oneStep = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(u.tpAB, u.tpA), List.of(u.a), List.of(u.b));

        assertEquals(twoStep.getVariables().size(), oneStep.getVariables().size());
        assertArrayEquals(twoStep.getValues(), oneStep.getValues(), DELTA,
                "multiply-then-marginalize must equal multiplyAndMarginalize");
    }

    // -----------------------------------------------------------------------
    // Multiply with a constant factor
    // -----------------------------------------------------------------------

    /**
     * Multiplying by a constant and then eliminating A must scale the B-marginal
     * by that constant.
     *
     * <p>P(B) = {1.0, 1.0, 1.0}, so 0.7 * P(B) = {0.7, 0.7, 0.7}.</p>
     */
    @Test
    public void multiplyAndMarginalize_withConstantFactor_scalesBMarginal() {
        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(u.tpAB, u.tpConstant07), List.of(u.b), List.of(u.a));

        assertArrayEquals(new double[]{0.7, 0.7, 0.7}, result.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Normalize
    // -----------------------------------------------------------------------

    /**
     * For {@code CONDITIONAL_PROBABILITY} potentials, normalize divides each
     * group of {@code numStates[0]} values by its row-sum — not the whole table.
     * Each row must sum to 1.0 after normalization.
     *
     * <p>The fixture tpAB already has normalised rows ({sum=1.0} per row), so
     * we build an unnormalised version (2×tpAB) to make the test meaningful.</p>
     */
    @Test
    public void normalize_conditionalProbability_eachRowSumsToOne() throws CannotNormalizePotentialException {
        // tpAB * 2: each row sums to 2.0 before normalization
        double[] doubled = Arrays.stream(u.tpAB.getValues()).map(v -> v * 2.0).toArray();
        TablePotential unnormalized = new TablePotential(u.variablesAB,
                PotentialRole.CONDITIONAL_PROBABILITY, doubled);

        DiscretePotentialOperations.normalize(unnormalized);

        int rowSize = u.a.getNumStates(); // 3 — first variable's state count
        for (int i = 0; i < unnormalized.getValues().length; i += rowSize) {
            double rowSum = 0.0;
            for (int j = 0; j < rowSize; j++) rowSum += unnormalized.getValues()[i + j];
            assertEquals(1.0, rowSum, DELTA,
                    "Row starting at index " + i + " must sum to 1.0 after normalization");
        }
        // No negative values
        for (double v : unnormalized.getValues()) {
            assertTrue(v >= 0.0, "Normalized value must be non-negative");
        }
    }

    /**
     * For {@code JOINT_PROBABILITY} potentials, normalize divides the whole table
     * by its total sum, so the result sums to 1.0.
     */
    @Test
    public void normalize_jointProbability_tableSumsToOne() throws CannotNormalizePotentialException {
        // tpAB values sum to 3.0 — use them as a joint potential
        TablePotential joint = new TablePotential(u.variablesAB,
                PotentialRole.JOINT_PROBABILITY, u.tpAB.getValues().clone());

        DiscretePotentialOperations.normalize(joint);

        double sum = Arrays.stream(joint.getValues()).sum();
        assertEquals(1.0, sum, DELTA);
        for (double v : joint.getValues()) {
            assertTrue(v >= 0.0, "Normalized value must be non-negative");
        }
    }

    /**
     * Normalizing a JOINT_PROBABILITY potential that already sums to 1 must not
     * change its values (idempotency).
     *
     * <p>Note: {@code normalize} mutates the input in place, so we work on clones
     * to be able to compare before/after independently.</p>
     */
    @Test
    public void normalize_alreadyNormalized_isIdempotent() throws CannotNormalizePotentialException {
        // Build a joint potential that already sums to 1.0
        double[] table = {0.5, 0.4, 0.1};
        TablePotential joint = new TablePotential(u.variablesA,
                PotentialRole.JOINT_PROBABILITY, table.clone());

        double[] afterOnce = DiscretePotentialOperations.normalize(joint).getValues().clone();
        double[] afterTwice = DiscretePotentialOperations.normalize(joint).getValues().clone();

        assertArrayEquals(new double[]{0.5, 0.4, 0.1}, afterOnce, DELTA);
        assertArrayEquals(afterOnce, afterTwice, DELTA,
                "normalize(normalize(f)) must equal normalize(f)");
    }

    /**
     * Idempotency holds for unnormalised potentials too: normalising twice must give
     * the same result as normalising once.
     */
    @Test
    public void normalize_idempotency_holdsForUnnormalisedPotential() throws CannotNormalizePotentialException {
        // Joint potential with sum 3.0
        TablePotential joint = new TablePotential(u.variablesAB,
                PotentialRole.JOINT_PROBABILITY, u.tpAB.getValues().clone());

        double[] afterOnce = DiscretePotentialOperations.normalize(joint).getValues().clone();
        double[] afterTwice = DiscretePotentialOperations.normalize(joint).getValues().clone();

        assertArrayEquals(afterOnce, afterTwice, DELTA,
                "normalize(normalize(f)) must equal normalize(f)");
    }

    // -----------------------------------------------------------------------
    // Sum-preservation invariant
    // -----------------------------------------------------------------------

    /**
     * Marginalization preserves the total sum of the potential.
     *
     * <p>sum(tpAB) = 3.0.  Both P(A) and P(B) marginals must also sum to 3.0.</p>
     */
    @Test
    public void marginalize_sumIsPreserved() {
        double originalSum = Arrays.stream(u.tpAB.getValues()).sum(); // 3.0

        double sumMargA = Arrays.stream(
                DiscretePotentialOperations.marginalize(u.tpAB, u.a).getValues()).sum();
        double sumMargB = Arrays.stream(
                DiscretePotentialOperations.marginalize(u.tpAB, u.b).getValues()).sum();

        assertEquals(originalSum, sumMargA, DELTA,
                "Marginalizing A must preserve the total sum");
        assertEquals(originalSum, sumMargB, DELTA,
                "Marginalizing B must preserve the total sum");
    }
}
