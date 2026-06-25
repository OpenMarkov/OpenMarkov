/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link TablePotential#tableProject(EvidenceCase, org.openmarkov.core.inference.InferenceOptions)}.
 *
 * <p>The primary regression risk in the Rediseño-Opción-3 refactoring is that
 * changes to the {@code Potential} class hierarchy may silently alter how
 * {@code tableProject} computes index offsets and copies values, producing a
 * wrong posterior without throwing any exception.</p>
 *
 * <p>These tests verify the expected behaviour of the <em>current</em>
 * implementation (compact result array, {@code initialPosition = 0}), and must
 * not be confused with the @Disabled {@code testProject1} in
 * {@link TablePotentialTest}, which was testing an older behaviour.</p>
 *
 * <h3>Naming conventions for the two-variable fixture</h3>
 * <pre>
 * Variables  [X(2), Y(2)]   offsets  X=1, Y=2
 * Table      {0.0, 1.0, 2.0, 3.0}
 *            idx 0: (X=0,Y=0) → 0.0
 *            idx 1: (X=1,Y=0) → 1.0
 *            idx 2: (X=0,Y=1) → 2.0
 *            idx 3: (X=1,Y=1) → 3.0
 * </pre>
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TablePotentialProjectionRegressionTest {

    private static final double DELTA = 1.0e-6;

    // Two binary variables
    private Variable x;
    private Variable y;

    /** P(X,Y): values {0, 1, 2, 3}. */
    private TablePotential tpXY;

    // Three-variable potential for middle-variable tests
    private Variable vA;  // 3 states
    private Variable vB;  // 2 states
    private Variable vC;  // 3 states

    /** P(A,B,C): a 3×2×3 = 18-entry potential with distinct values. */
    private TablePotential tpABC;

    @BeforeEach
    public void setUp() {
        State s0 = new State("0");
        State s1 = new State("1");
        State s2 = new State("2");

        x = new Variable("X", new State[]{s0, s1});
        y = new Variable("Y", new State[]{new State("0"), new State("1")});

        tpXY = new TablePotential(List.of(x, y), PotentialRole.CONDITIONAL_PROBABILITY,
                                  new double[]{0.0, 1.0, 2.0, 3.0});

        vA = new Variable("A", new State[]{s0, s1, s2});
        vB = new Variable("B", new State[]{new State("0"), new State("1")});
        vC = new Variable("C", new State[]{new State("0"), new State("1"), new State("2")});

        // P(A,B,C): variables [A(3), B(2), C(3)], offsets A=1, B=3, C=6
        // Fill with distinct values 1..18 to make index errors easy to detect
        double[] tableABC = new double[18];
        for (int i = 0; i < 18; i++) tableABC[i] = i + 1.0;
        tpABC = new TablePotential(List.of(vA, vB, vC), PotentialRole.CONDITIONAL_PROBABILITY, tableABC);
    }

    // -----------------------------------------------------------------------
    // Two-variable potential — project last variable (Y)
    // -----------------------------------------------------------------------

    /**
     * Projecting Y=0 from P(X,Y) must yield P(X | Y=0) = {0.0, 1.0}.
     *
     * <p>This mirrors {@link TablePotentialTest#testProject2()} but for the first
     * state of the last variable.</p>
     */
    @Test
    public void tableProject_lastVariable_stateZero()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(y, 0));

        TablePotential projected = tpXY.tableProject(ev, null);

        assertEquals(1, projected.getVariables().size(), "Must keep only X");
        assertEquals(x, projected.getVariables().getFirst());
        assertEquals(2, projected.getValues().length);
        assertEquals(0, projected.getInitialPosition());
        assertArrayEquals(new double[]{0.0, 1.0}, projected.getValues(), DELTA);
    }

    /**
     * Projecting Y=1 from P(X,Y) must yield P(X | Y=1) = {2.0, 3.0}.
     * (Equivalent to the enabled testProject2 in TablePotentialTest.)
     */
    @Test
    public void tableProject_lastVariable_stateOne()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(y, 1));

        TablePotential projected = tpXY.tableProject(ev, null);

        assertEquals(1, projected.getVariables().size());
        assertEquals(x, projected.getVariables().getFirst());
        assertEquals(0, projected.getInitialPosition());
        assertArrayEquals(new double[]{2.0, 3.0}, projected.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Two-variable potential — project first variable (X)
    // -----------------------------------------------------------------------

    /**
     * Projecting X=0 from P(X,Y) must yield P(Y | X=0) = {0.0, 2.0}.
     *
     * <p>offsets: X=1, Y=2.  At X=0: (X=0,Y=0) = values[0] = 0.0;
     * (X=0,Y=1) = values[2] = 2.0.</p>
     */
    @Test
    public void tableProject_firstVariable_stateZero()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(x, 0));

        TablePotential projected = tpXY.tableProject(ev, null);

        assertEquals(1, projected.getVariables().size(), "Must keep only Y");
        assertEquals(y, projected.getVariables().getFirst());
        assertEquals(2, projected.getValues().length);
        assertEquals(0, projected.getInitialPosition());
        assertArrayEquals(new double[]{0.0, 2.0}, projected.getValues(), DELTA);
    }

    /**
     * Projecting X=1 from P(X,Y) must yield P(Y | X=1) = {1.0, 3.0}.
     */
    @Test
    public void tableProject_firstVariable_stateOne()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(x, 1));

        TablePotential projected = tpXY.tableProject(ev, null);

        assertEquals(1, projected.getVariables().size());
        assertEquals(y, projected.getVariables().getFirst());
        assertEquals(0, projected.getInitialPosition());
        assertArrayEquals(new double[]{1.0, 3.0}, projected.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Evidence variable not present in the potential (must be a no-op)
    // -----------------------------------------------------------------------

    /**
     * Evidence about a variable not appearing in the potential must be ignored;
     * the result must equal the original potential.
     */
    @Test
    public void tableProject_evidenceVariableAbsent_returnsOriginal()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        Variable z = new Variable("Z", new State[]{new State("0"), new State("1")});
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(z, 0)); // Z is not in tpXY

        TablePotential projected = tpXY.tableProject(ev, null);

        assertEquals(2, projected.getVariables().size(),
                "Variables of interest must be unchanged when evidence is irrelevant");
        assertArrayEquals(tpXY.getValues(), projected.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Project both variables → constant potential
    // -----------------------------------------------------------------------

    /**
     * Projecting all variables must yield a constant (0-variable) potential
     * whose single value is the table entry at that configuration.
     *
     * <p>P(X=0,Y=1) = values[0*1 + 1*2] = values[2] = 2.0.</p>
     */
    @Test
    public void tableProject_allVariables_yieldsConstant()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(x, 0));
        ev.addFinding(new Finding(y, 1));

        TablePotential projected = tpXY.tableProject(ev, null);

        assertEquals(0, projected.getVariables().size(),
                "Result must be a constant (0-variable) potential");
        assertEquals(1, projected.getValues().length);
        assertEquals(2.0, projected.getValues()[0], DELTA);
    }

    // -----------------------------------------------------------------------
    // Three-variable potential — project the middle variable
    // -----------------------------------------------------------------------

    /**
     * tpABC has variables [A(3), B(2), C(3)] with values 1..18.
     * Projecting the middle variable B=0 must extract the A×C slice at B=0.
     *
     * <p>offsets: A=1, B=3, C=6.  At B=0, the slice starts at offset B=0:
     * <pre>
     * (A=0,B=0,C=0) → values[0] =  1.0
     * (A=1,B=0,C=0) → values[1] =  2.0
     * (A=2,B=0,C=0) → values[2] =  3.0
     * (A=0,B=0,C=1) → values[6] =  7.0
     * (A=1,B=0,C=1) → values[7] =  8.0
     * (A=2,B=0,C=1) → values[8] =  9.0
     * (A=0,B=0,C=2) → values[12] = 13.0
     * (A=1,B=0,C=2) → values[13] = 14.0
     * (A=2,B=0,C=2) → values[14] = 15.0
     * </pre>
     * </p>
     */
    @Test
    public void tableProject_middleVariable_stateZero()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(vB, 0));

        TablePotential projected = tpABC.tableProject(ev, null);

        assertEquals(2, projected.getVariables().size(),
                "Result must have 2 variables: [A, C]");
        assertTrue(projected.getVariables().containsAll(List.of(vA, vC)));
        assertEquals(9, projected.getValues().length, "3×3 = 9 entries");
        assertEquals(0, projected.getInitialPosition());

        // Verify values using accumulated offsets against the result variable order
        // The result has [A, C] (B removed).  We check that each (a,c) entry
        // matches what tpABC had at (A=a, B=0, C=c).
        List<Variable> resultVars = projected.getVariables();
        int[] resultOffsets = projected.getOffsets();

        for (int a = 0; a < 3; a++) {
            for (int c = 0; c < 3; c++) {
                int origIdx  = a * 1 + 0 * 3 + c * 6;   // offset for (A=a, B=0, C=c)
                double expected = tpABC.getValues()[origIdx];

                // Find a in result variable list
                int aPos = resultVars.indexOf(vA);
                int cPos = resultVars.indexOf(vC);
                int resultIdx = a * resultOffsets[aPos] + c * resultOffsets[cPos];

                assertEquals(expected, projected.getValues()[resultIdx], DELTA,
                        String.format("Mismatch at (A=%d, B=0, C=%d)", a, c));
            }
        }
    }

    /**
     * Projecting B=1 must extract the complementary slice.
     */
    @Test
    public void tableProject_middleVariable_stateOne()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(vB, 1));

        TablePotential projected = tpABC.tableProject(ev, null);

        assertEquals(2, projected.getVariables().size());
        assertEquals(9, projected.getValues().length);

        List<Variable> resultVars = projected.getVariables();
        int[] resultOffsets = projected.getOffsets();

        for (int a = 0; a < 3; a++) {
            for (int c = 0; c < 3; c++) {
                int origIdx = a * 1 + 1 * 3 + c * 6;  // B=1 slice
                double expected = tpABC.getValues()[origIdx];

                int aPos = resultVars.indexOf(vA);
                int cPos = resultVars.indexOf(vC);
                int resultIdx = a * resultOffsets[aPos] + c * resultOffsets[cPos];

                assertEquals(expected, projected.getValues()[resultIdx], DELTA,
                        String.format("Mismatch at (A=%d, B=1, C=%d)", a, c));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Projection consistency with multiply
    // -----------------------------------------------------------------------

    /**
     * Multiplying two projected potentials must give the same result as
     * multiplying the originals and then projecting.
     *
     * <p>This tests the full pipeline that VE uses:
     * {@code tableProject} → {@code multiply} → {@code multiplyAndMarginalize}.</p>
     */
    @Test
    public void tableProject_thenMultiply_consistentWithMultiplyThenProject()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        // Two potentials over [X, Y]
        double[] tableP = {0.1, 0.2, 0.3, 0.4};
        double[] tableQ = {0.5, 0.6, 0.7, 0.8};
        Variable xv = new Variable("Xv", new State[]{new State("0"), new State("1")});
        Variable yv = new Variable("Yv", new State[]{new State("0"), new State("1")});
        TablePotential tp = new TablePotential(List.of(xv, yv), PotentialRole.CONDITIONAL_PROBABILITY, tableP);
        TablePotential tq = new TablePotential(List.of(xv, yv), PotentialRole.CONDITIONAL_PROBABILITY, tableQ);

        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(yv, 0)); // Y=0

        // Path A: project both, then multiply
        TablePotential projP = tp.tableProject(ev, null);
        TablePotential projQ = tq.tableProject(ev, null);
        TablePotential productOfProjected = DiscretePotentialOperations.multiply(List.of(projP, projQ));

        // Path B: multiply both, then project
        TablePotential product = DiscretePotentialOperations.multiply(List.of(tp, tq));
        TablePotential projectedProduct = product.tableProject(ev, null);

        assertEquals(productOfProjected.getVariables().size(), projectedProduct.getVariables().size());
        assertArrayEquals(productOfProjected.getValues(), projectedProduct.getValues(), DELTA,
                "project-then-multiply must equal multiply-then-project");
    }
}
