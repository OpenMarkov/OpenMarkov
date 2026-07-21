/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TablePotentialArithmetic}.
 *
 * <p>Because {@code TablePotentialArithmetic} is package-private, the tests
 * live in the same package and call the class directly, covering the edge-cases
 * that the higher-level {@link DiscretePotentialOperations} tests do not reach.</p>
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TablePotentialArithmeticTest {

    private static final double DELTA = 1e-9;

    private Variable a;
    private Variable b;
    private TablePotential pA;   // P(A): [0.3, 0.7]
    private TablePotential pB;   // P(B): [0.6, 0.4]
    private TablePotential pAB;  // P(A,B): 4-entry table

    @BeforeEach
    void setUp() {
        State[] twoStates = {new State("s0"), new State("s1")};
        a = new Variable("A", twoStates);
        b = new Variable("B", twoStates);

        pA = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.3, 0.7});
        pB = new TablePotential(List.of(b), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.6, 0.4});
        pAB = new TablePotential(List.of(a, b), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.1, 0.2, 0.3, 0.4});
    }

    // -----------------------------------------------------------------------
    // multiply — boundary cases
    // -----------------------------------------------------------------------

    @Test
    void multiplyEmptyListReturnsIdentityConstant() {
        TablePotential result = TablePotentialArithmetic.multiply(Collections.emptyList());

        assertNotNull(result, "multiply([]) must not return null");
        assertTrue(result.getVariables().isEmpty());
        assertEquals(1.0, result.getValues()[0], DELTA,
                "Multiplicative identity for empty list must be 1.0");
    }

    @Test
    void multiplySinglePotentialReturnsSamePotential() {
        TablePotential result = TablePotentialArithmetic.multiply(List.of(pA));
        assertSame(pA, result);
    }

    @Test
    void multiplyTwoPotentialsWithDisjointVariables() {
        // P(A) * P(B) should produce a joint P(A,B) with 4 entries
        TablePotential result = TablePotentialArithmetic.multiply(List.of(pA, pB));

        assertEquals(2, result.getVariables().size());
        assertTrue(result.getVariables().containsAll(List.of(a, b)));
        assertEquals(4, result.getValues().length);
    }

    @Test
    void multiplyTwoPotentialsSumOfJointEqualsProduct() {
        // Sum of joint P(A,B) = P(A) * P(B) for independent variables must equal 1.0
        TablePotential result = TablePotentialArithmetic.multiply(List.of(pA, pB));
        double sum = 0;
        for (double v : result.getValues()) sum += v;
        assertEquals(1.0, sum, 1e-6);
    }

    @Test
    void multiplyConstantPotentialScalesResult() {
        TablePotential constant2 = new TablePotential(
                new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{2.0});

        TablePotential result = TablePotentialArithmetic.multiply(List.of(pA, constant2));

        // Each value in pA should be doubled
        assertEquals(0.6, result.getValues()[0], 1e-6);
        assertEquals(1.4, result.getValues()[1], 1e-6);
    }

    @Test
    void multiplyVarargVariantMatchesList() {
        TablePotential fromList   = TablePotentialArithmetic.multiply(List.of(pA, pB));
        TablePotential fromVararg = TablePotentialArithmetic.multiply(pA, pB);

        assertArrayEquals(fromList.getValues(), fromVararg.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // sum — boundary cases
    // -----------------------------------------------------------------------

    @Test
    void sumNullListReturnsZeroConstant() {
        TablePotential result = TablePotentialArithmetic.sum((List<TablePotential>) null);

        assertNotNull(result);
        assertTrue(result.getVariables().isEmpty());
        assertEquals(0.0, result.getValues()[0], DELTA);
    }

    @Test
    void sumEmptyListReturnsZeroConstant() {
        TablePotential result = TablePotentialArithmetic.sum(Collections.emptyList());

        assertTrue(result.getVariables().isEmpty());
        assertEquals(0.0, result.getValues()[0], DELTA);
    }

    @Test
    void sumSinglePotentialReturnsSamePotential() {
        TablePotential result = TablePotentialArithmetic.sum(List.of(pA));
        assertSame(pA, result);
    }

    @Test
    void sumTwoPotentialsWithSameVariables() {
        // P(A) + P(A): each entry doubles
        TablePotential result = TablePotentialArithmetic.sum(List.of(pA, pA));

        assertEquals(1, result.getVariables().size());
        assertEquals(0.6, result.getValues()[0], 1e-6);
        assertEquals(1.4, result.getValues()[1], 1e-6);
    }

    @Test
    void sumVarargVariantMatchesList() {
        TablePotential fromList   = TablePotentialArithmetic.sum(List.of(pA, pB));
        TablePotential fromVararg = TablePotentialArithmetic.sum(pA, pB);

        assertArrayEquals(fromList.getValues(), fromVararg.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // getRole
    // -----------------------------------------------------------------------

    @Test
    void getRoleWithOnlyConditionalProbabilityPotentials() {
        assertEquals(PotentialRole.CONDITIONAL_PROBABILITY,
                TablePotentialArithmetic.getRole(List.of(pA, pB)));
    }

    @Test
    void getRoleWithAtLeastOneJointProbabilityPotential() {
        TablePotential joint = new TablePotential(List.of(a), PotentialRole.JOINT_PROBABILITY,
                new double[]{0.3, 0.7});
        assertEquals(PotentialRole.JOINT_PROBABILITY,
                TablePotentialArithmetic.getRole(List.of(pA, joint)));
    }

    @Test
    void getRoleWithAdditivePotentialReturnsUnspecified() {
        // isAdditive() returns true when the potential has a criterion set
        TablePotential utility = new TablePotential(List.of(a), PotentialRole.UNSPECIFIED,
                new double[]{10.0, 20.0});
        utility.setCriterion(new org.openmarkov.core.model.network.Criterion("Cost", "EUR"));
        PotentialRole role = TablePotentialArithmetic.getRole(List.of(pA, utility));
        assertEquals(PotentialRole.UNSPECIFIED, role);
    }

    // -----------------------------------------------------------------------
    // divide
    // -----------------------------------------------------------------------

    @Test
    void dividePotentialsByIdentityLeavesNumeratorUnchanged() {
        TablePotential one = new TablePotential(
                new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{1.0});

        Potential result = TablePotentialArithmetic.dividePotentials(pA, one);

        assertInstanceOf(TablePotential.class, result);
        double[] values = ((TablePotential) result).getValues();
        assertEquals(0.3, values[0], 1e-6);
        assertEquals(0.7, values[1], 1e-6);
    }

    @Test
    void dividePotentialsByUniformDenominatorHalvesAllEntries() {
        // P(A) / 0.5 (constant) → each entry doubled... wait: / 0.5 = * 2
        TablePotential half = new TablePotential(
                new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.5});

        Potential result = TablePotentialArithmetic.dividePotentials(pA, half);

        double[] values = ((TablePotential) result).getValues();
        assertEquals(0.6, values[0], 1e-6);  // 0.3 / 0.5
        assertEquals(1.4, values[1], 1e-6);  // 0.7 / 0.5
    }

    @Test
    void dividePotentialsByNonTablePotentialThrows() {
        assertThrows(Exception.class,
                () -> TablePotentialArithmetic.dividePotentials(null, pA));
    }
}
