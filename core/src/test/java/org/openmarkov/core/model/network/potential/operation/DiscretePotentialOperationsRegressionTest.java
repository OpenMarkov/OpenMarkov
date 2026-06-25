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
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link DiscretePotentialOperations} covering methods
 * not exercised by {@link DiscretePotentialOperationsTest}.
 *
 * <p>Network used in variable-elimination tests:
 * <pre>
 *   A (2 states) → B (2 states)
 *   P(A): [0.4, 0.6]
 *   P(B|A): A=a0: [0.9, 0.1]   A=a1: [0.3, 0.7]
 *   P(B) expected after eliminating A:
 *     P(B=b0) = 0.4*0.9 + 0.6*0.3 = 0.54
 *     P(B=b1) = 0.4*0.1 + 0.6*0.7 = 0.46
 * </pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DiscretePotentialOperationsRegressionTest {

    private static final double DELTA = 1e-9;

    private Variable a;  // 2 states a0, a1
    private Variable b;  // 2 states b0, b1

    private TablePotential pA;    // P(A)
    private TablePotential pBgA;  // P(B|A)

    @BeforeEach
    public void setUp() {
        a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        b = new Variable("B", new State[]{new State("b0"), new State("b1")});

        pA   = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.4, 0.6});
        pBgA = new TablePotential(List.of(b, a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1,   // a=a0: P(b0|a0)=0.9, P(b1|a0)=0.1  (offset[A]=2)
                             0.3, 0.7}); // a=a1
    }

    // -----------------------------------------------------------------------
    // multiplyAndEliminate
    // -----------------------------------------------------------------------

    @Test
    public void multiplyAndEliminateProducesCorrectMarginalVariableB() {
        Potential result = DiscretePotentialOperations.multiplyAndEliminate(
                List.of(pA, pBgA), List.of(a));

        assertInstanceOf(TablePotential.class, result);
        TablePotential marginal = (TablePotential) result;

        assertEquals(1, marginal.getVariables().size());
        assertTrue(marginal.getVariables().contains(b));

        // P(B=b0) = 0.4*0.9 + 0.6*0.3 = 0.54
        // P(B=b1) = 0.4*0.1 + 0.6*0.7 = 0.46
        double[] values = marginal.getValues();
        assertEquals(0.54, values[0], 1e-6);
        assertEquals(0.46, values[1], 1e-6);
    }

    @Test
    public void multiplyAndEliminateEliminatingAllVariablesProducesConstant() {
        // Eliminate both A and B → result is a constant (no variables)
        Potential result = DiscretePotentialOperations.multiplyAndEliminate(
                List.of(pA, pBgA), List.of(a, b));

        assertInstanceOf(TablePotential.class, result);
        TablePotential constant = (TablePotential) result;
        assertEquals(0, constant.getVariables().size());
        // The constant is the sum of all joint probabilities = 1.0
        assertEquals(1.0, constant.getValues()[0], 1e-6);
    }

    @Test
    public void multiplyAndEliminateWithSinglePotentialAndOnlyVariable() {
        // Marginalise P(A) over A → constant 1.0
        Potential result = DiscretePotentialOperations.multiplyAndEliminate(
                List.of(pA), List.of(a));

        TablePotential constant = (TablePotential) result;
        assertEquals(1.0, constant.getValues()[0], 1e-6);
    }

    // -----------------------------------------------------------------------
    // createZeroProbabilityPotential
    // -----------------------------------------------------------------------

    @Test
    public void createZeroProbabilityPotentialHasValueZero() {
        TablePotential zero = DiscretePotentialOperations.createZeroProbabilityPotential();
        assertEquals(0.0, zero.getValues()[0], DELTA);
    }

    @Test
    public void createZeroProbabilityPotentialHasCorrectRole() {
        TablePotential zero = DiscretePotentialOperations.createZeroProbabilityPotential();
        assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, zero.getPotentialRole());
    }

    @Test
    public void createZeroProbabilityPotentialHasNoVariables() {
        TablePotential zero = DiscretePotentialOperations.createZeroProbabilityPotential();
        assertTrue(zero.getVariables().isEmpty());
    }

    // -----------------------------------------------------------------------
    // createUnityProbabilityPotential
    // -----------------------------------------------------------------------

    @Test
    public void createUnityProbabilityPotentialHasValueOne() {
        TablePotential unity = DiscretePotentialOperations.createUnityProbabilityPotential();
        assertEquals(1.0, unity.getValues()[0], DELTA);
    }

    @Test
    public void createUnityProbabilityPotentialHasCorrectRole() {
        TablePotential unity = DiscretePotentialOperations.createUnityProbabilityPotential();
        assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, unity.getPotentialRole());
    }

    // -----------------------------------------------------------------------
    // createOneValuePotential
    // -----------------------------------------------------------------------

    @Test
    public void createOneValuePotentialWithArbitraryValue() {
        TablePotential p = DiscretePotentialOperations.createOneValuePotential(
                PotentialRole.UNSPECIFIED, 3.14);
        assertEquals(3.14, p.getValues()[0], DELTA);
        assertEquals(PotentialRole.UNSPECIFIED, p.getPotentialRole());
    }

    @Test
    public void createOneValuePotentialHasTableSizeOne() {
        TablePotential p = DiscretePotentialOperations.createOneValuePotential(
                PotentialRole.CONDITIONAL_PROBABILITY, 0.5);
        assertEquals(1, p.getTableSize());
    }

    // -----------------------------------------------------------------------
    // createZeroUtilityPotential
    // -----------------------------------------------------------------------

    @Test
    public void createZeroUtilityPotentialWithNullNetHasValueZero() {
        TablePotential util = DiscretePotentialOperations.createZeroUtilityPotential(null);
        assertEquals(0.0, util.getValues()[0], DELTA);
    }

    // -----------------------------------------------------------------------
    // almostEqual
    // -----------------------------------------------------------------------

    @Test
    public void almostEqualReturnsTrueForIdenticalValues() {
        assertTrue(DiscretePotentialOperations.almostEqual(1.0, 1.0));
    }

    @Test
    public void almostEqualReturnsTrueForDifferenceWithinTolerance() {
        double maxError = DiscretePotentialOperations.maxRoundErrorAllowed;
        double a = 1.0;
        double b = a + maxError * a * 0.5; // half the tolerance
        assertTrue(DiscretePotentialOperations.almostEqual(a, b));
    }

    @Test
    public void almostEqualReturnsFalseForDifferenceExceedingTolerance() {
        assertFalse(DiscretePotentialOperations.almostEqual(1.0, 2.0));
    }

    @Test
    public void almostEqualWithZeroBaseIsStrictlyEqual() {
        // When a=0: tolerance = maxError * |0| = 0, so only exact zero passes.
        assertTrue(DiscretePotentialOperations.almostEqual(0.0, 0.0));
        assertFalse(DiscretePotentialOperations.almostEqual(0.0, 1e-10));
    }

    // -----------------------------------------------------------------------
    // sum(double[])
    // -----------------------------------------------------------------------

    @Test
    public void sumOfEmptyArrayIsZero() {
        assertEquals(0.0, DiscretePotentialOperations.sum(new double[0]), DELTA);
    }

    @Test
    public void sumOfSingleElement() {
        assertEquals(3.14, DiscretePotentialOperations.sum(new double[]{3.14}), DELTA);
    }

    @Test
    public void sumOfMultipleElements() {
        assertEquals(6.0, DiscretePotentialOperations.sum(new double[]{1.0, 2.0, 3.0}), DELTA);
    }

    @Test
    public void sumOfProbabilityDistributionIsOne() {
        assertEquals(1.0, DiscretePotentialOperations.sum(pA.getValues()), 1e-9);
    }

    // -----------------------------------------------------------------------
    // normalize — edge cases
    // -----------------------------------------------------------------------

    @Test
    public void normalizeAllZerosThrows() {
        TablePotential allZero = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.0, 0.0});
        assertThrows(CannotNormalizePotentialException.class,
                () -> DiscretePotentialOperations.normalize(allZero));
    }

    @Test
    public void normalizeAlreadyNormalizedIsIdentity() throws CannotNormalizePotentialException {
        TablePotential result = DiscretePotentialOperations.normalize(pA);
        // P(A) sums to 1 already; each value should be the same.
        assertEquals(0.4, result.getValues()[0], 1e-6);
        assertEquals(0.6, result.getValues()[1], 1e-6);
    }

    @Test
    public void normalizeUnnormalizedPotential() throws CannotNormalizePotentialException {
        TablePotential unnorm = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{2.0, 6.0});  // sum = 8
        TablePotential result = DiscretePotentialOperations.normalize(unnorm);
        assertEquals(0.25, result.getValues()[0], 1e-6);
        assertEquals(0.75, result.getValues()[1], 1e-6);
    }

    // -----------------------------------------------------------------------
    // imposeOtherDistributionWhenDistributionIsZero
    // -----------------------------------------------------------------------

    @Test
    public void imposeOtherDistributionLeavesNonZeroConfigurationsUnchanged() {
        // P(A|B): B has 2 states. P(A|b0) = [0.3, 0.7] — nonzero.
        Variable bLocal = new Variable("Bloc", new State[]{new State("b0"), new State("b1")});
        Variable aLocal = new Variable("Aloc", new State[]{new State("a0"), new State("a1")});
        TablePotential p = new TablePotential(List.of(aLocal, bLocal),
                PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.3, 0.7, 0.0, 0.0});

        TablePotential result = DiscretePotentialOperations.imposeOtherDistributionWhenDistributionIsZero(p);

        // b0 column untouched
        assertEquals(0.3, result.getValues()[0], DELTA);
        assertEquals(0.7, result.getValues()[1], DELTA);
    }

    @Test
    public void imposeOtherDistributionFixesAllZeroColumn() {
        // P(A|B): B has 2 states. P(A|b1) = [0.0, 0.0] — all zeros.
        Variable bLocal = new Variable("Bloc", new State[]{new State("b0"), new State("b1")});
        Variable aLocal = new Variable("Aloc", new State[]{new State("a0"), new State("a1")});
        TablePotential p = new TablePotential(List.of(aLocal, bLocal),
                PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.3, 0.7,  // b0
                             0.0, 0.0}); // b1 — all zeros → should become [1.0, 0.0]

        TablePotential result = DiscretePotentialOperations.imposeOtherDistributionWhenDistributionIsZero(p);

        // b1 column: first state gets 1.0, rest 0.0
        assertEquals(1.0, result.getValues()[2], DELTA);
        assertEquals(0.0, result.getValues()[3], DELTA);
    }

    // -----------------------------------------------------------------------
    // marginalize(TablePotential, Variable)
    // -----------------------------------------------------------------------

    @Test
    public void marginalizeByVariableEliminatesIt() {
        // Joint P(A,B): build from pA and pBgA then marginalize over B → P(A).
        // Use a joint potential P(A,B) directly.
        TablePotential joint = new TablePotential(List.of(a, b),
                PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.36, 0.24, 0.04, 0.36}); // P(a0,b0)=0.36 etc.

        TablePotential margA = DiscretePotentialOperations.marginalize(joint, b);

        assertEquals(1, margA.getVariables().size());
        assertTrue(margA.getVariables().contains(a));
        // P(A=a0) = 0.36 + 0.04 = 0.40
        assertEquals(0.40, margA.getValues()[0], 1e-6);
        // P(A=a1) = 0.24 + 0.36 = 0.60
        assertEquals(0.60, margA.getValues()[1], 1e-6);
    }

    @Test
    public void marginalizeByVariableListKeepsOnlyRequestedVariables() {
        TablePotential joint = new TablePotential(List.of(a, b),
                PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.36, 0.24, 0.04, 0.36});

        // Keep only A, which eliminates B.
        TablePotential margA = DiscretePotentialOperations.marginalize(joint, List.of(a));

        assertEquals(1, margA.getVariables().size());
        assertFalse(margA.getVariables().contains(b));
    }

    @Test
    public void marginalizeAllVariablesProducesConstantSummingToOne() {
        TablePotential pANorm = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.4, 0.6});
        TablePotential constant = DiscretePotentialOperations.marginalize(pANorm, a);
        assertEquals(0, constant.getVariables().size());
        assertEquals(1.0, constant.getValues()[0], 1e-6);
    }

    // -----------------------------------------------------------------------
    // getRole(Collection<? extends Potential>)
    // -----------------------------------------------------------------------

    @Test
    public void getRoleWithAllConditionalProbReturnsConditionalProb() {
        PotentialRole role = DiscretePotentialOperations.getRole(List.of(pA, pBgA));
        assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, role);
    }

    @Test
    public void getRoleWithAdditivePotentialReturnsUnspecified() {
        // isAdditive() returns true when criterion != null.
        TablePotential utility = new TablePotential(List.of(a), PotentialRole.UNSPECIFIED);
        utility.setCriterion(new org.openmarkov.core.model.network.Criterion("Cost"));

        PotentialRole role = DiscretePotentialOperations.getRole(List.of(pA, utility));
        assertEquals(PotentialRole.UNSPECIFIED, role);
    }

    @Test
    public void getRoleWithJointProbabilityReturnsJointProb() {
        TablePotential joint = new TablePotential(List.of(a, b), PotentialRole.JOINT_PROBABILITY);

        PotentialRole role = DiscretePotentialOperations.getRole(List.of(joint));
        assertEquals(PotentialRole.JOINT_PROBABILITY, role);
    }

    @Test
    public void getRoleAdditiveTakesPrecedenceOverJointProbability() {
        // If there is both an additive and a joint-prob potential, UNSPECIFIED wins.
        TablePotential joint = new TablePotential(List.of(a), PotentialRole.JOINT_PROBABILITY);
        TablePotential additive = new TablePotential(List.of(b), PotentialRole.UNSPECIFIED);
        additive.setCriterion(new org.openmarkov.core.model.network.Criterion("Cost"));

        PotentialRole role = DiscretePotentialOperations.getRole(List.of(joint, additive));
        assertEquals(PotentialRole.UNSPECIFIED, role);
    }

    // -----------------------------------------------------------------------
    // isThereAUtilityPotential — stub: always returns false
    // -----------------------------------------------------------------------

    @Test
    public void isThereAUtilityPotentialAlwaysReturnsFalse() {
        // The method body is commented out; it always returns false regardless of input.
        TablePotential additive = new TablePotential(List.of(a), PotentialRole.UNSPECIFIED);
        additive.setCriterion(new org.openmarkov.core.model.network.Criterion("Cost"));

        assertFalse(DiscretePotentialOperations.isThereAUtilityPotential(List.of(additive)),
                "isThereAUtilityPotential has its body commented out and always returns false");
    }

    // -----------------------------------------------------------------------
    // multiplyAndMarginalize(TablePotential prob, TablePotential util, Variable)
    // -----------------------------------------------------------------------

    @Test
    public void multiplyAndMarginalizeWithUnitProbConstantReturnsUtilityUnchanged() {
        // When prob potential is a constant = 1.0, the method returns the utility directly.
        TablePotential unitProb = DiscretePotentialOperations.createUnityProbabilityPotential();
        TablePotential utility = new TablePotential(List.of(a), PotentialRole.UNSPECIFIED,
                new double[]{3.0, 7.0});

        // variableToEliminate is not in the utility, but the constant-prob shortcut
        // triggers before that is needed.
        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(unitProb, utility, b);

        assertSame(utility, result,
                "When prob is a unit constant, the utility potential is returned as-is");
    }

    @Test
    public void multiplyAndMarginalizeWithProbConstantScalesUtility() {
        // When prob is a constant p ≠ 1, utility values are scaled by p.
        TablePotential halfProb = DiscretePotentialOperations
                .createOneValuePotential(PotentialRole.CONDITIONAL_PROBABILITY, 0.5);
        TablePotential utility = new TablePotential(List.of(a), PotentialRole.UNSPECIFIED,
                new double[]{4.0, 8.0});

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(halfProb, utility, b);

        assertEquals(2.0, result.getValues()[0], 1e-6);
        assertEquals(4.0, result.getValues()[1], 1e-6);
    }

    @Test
    public void multiplyAndMarginalizeWithSharedVariableEliminatesIt() {
        // P(A): prob,  U(A): utility.  Eliminate A → scalar = Σ_A P(A)·U(A)
        TablePotential utility = new TablePotential(List.of(a), PotentialRole.UNSPECIFIED,
                new double[]{10.0, 20.0}); // U(a0)=10, U(a1)=20

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(pA, utility, a);

        // E[U] = 0.4*10 + 0.6*20 = 4 + 12 = 16
        assertEquals(0, result.getVariables().size());
        assertEquals(16.0, result.getValues()[0], 1e-6);
    }

    @Test
    public void imposeOtherDistributionOnConstantPotentialIsNoOp() {
        // A constant potential (tableSize == 1) should be returned unchanged.
        TablePotential constant = new TablePotential(List.of(a),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.0, 0.0});
        // single variable, tableSize=2 → not a constant, but length ≤ 1 check...
        // Let's use a genuinely constant one (no variables):
        TablePotential trueConst = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY);
        trueConst.getValues()[0] = 0.0;
        TablePotential result = DiscretePotentialOperations.imposeOtherDistributionWhenDistributionIsZero(trueConst);
        assertSame(trueConst, result, "Constant potential must be returned as-is");
    }
}
