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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link TablePotential} covering methods not exercised
 * by {@link TablePotentialTest}.
 *
 * <p>Layout used in most tests:
 * <pre>
 *   A (2 states: a0, a1)
 *   B (3 states: b0, b1, b2)
 *   potential P(A | B): variables = [A, B]
 *   offsets:  A → 1,  B → 2
 *   table positions:
 *     0: A=a0, B=b0
 *     1: A=a1, B=b0
 *     2: A=a0, B=b1
 *     3: A=a1, B=b1
 *     4: A=a0, B=b2
 *     5: A=a1, B=b2
 * </pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TablePotentialRegressionTest {

    private static final double DELTA = 1e-9;

    private Variable a;   // 2 states
    private Variable b;   // 3 states
    /** P(A|B): variables = [A, B], offsets = [1, 2] */
    private TablePotential pAB;

    @BeforeEach
    public void setUp() {
        a   = new Variable("A", new State[]{new State("a0"), new State("a1")});
        b   = new Variable("B", new State[]{new State("b0"), new State("b1"), new State("b2")});
        pAB = new TablePotential(List.of(a, b), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1,   // B=b0: P(A=a0|b0)=0.9, P(A=a1|b0)=0.1
                             0.7, 0.3,   // B=b1
                             0.4, 0.6}); // B=b2
    }

    // -----------------------------------------------------------------------
    // copy() — independent values array
    // -----------------------------------------------------------------------

    @Test
    public void copyProducesDistinctValuesArray() {
        TablePotential copy = (TablePotential) pAB.copy();
        assertNotSame(pAB.getValues(), copy.getValues());
    }

    @Test
    public void copyPreservesValues() {
        TablePotential copy = (TablePotential) pAB.copy();
        assertArrayEquals(pAB.getValues(), copy.getValues(), DELTA);
    }

    @Test
    public void copyValuesAreIndependent() {
        TablePotential copy = (TablePotential) pAB.copy();
        copy.getValues()[0] = 0.0;
        assertEquals(0.9, pAB.getValues()[0], DELTA,
                "Modifying copy must not affect original");
    }

    @Test
    public void copyPreservesVariables() {
        TablePotential copy = (TablePotential) pAB.copy();
        assertEquals(pAB.getVariables(), copy.getVariables());
    }

    @Test
    public void copyPreservesOffsets() {
        TablePotential copy = (TablePotential) pAB.copy();
        assertArrayEquals(pAB.getOffsets(), copy.getOffsets());
    }

    @Test
    public void copyPreservesRole() {
        TablePotential copy = (TablePotential) pAB.copy();
        assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, copy.getPotentialRole());
    }

    // -----------------------------------------------------------------------
    // getPosition(int[]) and getConfiguration(int)  — round-trip
    // -----------------------------------------------------------------------

    @Test
    public void getPositionForFirstEntry() {
        // A=a0 (idx 0), B=b0 (idx 0) → position 0
        assertEquals(0, pAB.getPosition(new int[]{0, 0}));
    }

    @Test
    public void getPositionForSecondEntry() {
        // A=a1 (idx 1), B=b0 (idx 0) → offset[A]=1 → position 1
        assertEquals(1, pAB.getPosition(new int[]{1, 0}));
    }

    @Test
    public void getPositionForThirdEntry() {
        // A=a0 (idx 0), B=b1 (idx 1) → offset[B]=2 → position 2
        assertEquals(2, pAB.getPosition(new int[]{0, 1}));
    }

    @Test
    public void getPositionForLastEntry() {
        // A=a1 (idx 1), B=b2 (idx 2) → 1*1 + 2*2 = 5
        assertEquals(5, pAB.getPosition(new int[]{1, 2}));
    }

    @Test
    public void getConfigurationRoundTrip() {
        for (int pos = 0; pos < pAB.getTableSize(); pos++) {
            int[] coords = pAB.getConfiguration(pos);
            assertEquals(pos, pAB.getPosition(coords),
                    "Round-trip failed at position " + pos);
        }
    }

    // -----------------------------------------------------------------------
    // getValue(EvidenceCase) and setValue(...)
    // -----------------------------------------------------------------------

    @Test
    public void getValueViaEvidenceCaseFirstEntry() throws Exception {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(a, 0));
        ev.addFinding(new Finding(b, 0));
        assertEquals(0.9, pAB.getValue(ev), DELTA);
    }

    @Test
    public void getValueViaEvidenceCaseLastEntry() throws Exception {
        EvidenceCase ev = new EvidenceCase();
        ev.addFinding(new Finding(a, 1));
        ev.addFinding(new Finding(b, 2));
        assertEquals(0.6, pAB.getValue(ev), DELTA);
    }

    @Test
    public void setValueAndRetrieve() {
        pAB.setValue(List.of(a, b), new int[]{0, 1}, 0.55);
        assertEquals(0.55, pAB.getValues()[pAB.getPosition(new int[]{0, 1})], DELTA);
    }

    // -----------------------------------------------------------------------
    // getTableSize / getDimensions
    // -----------------------------------------------------------------------

    @Test
    public void tableSizeIsProductOfStateCounts() {
        // 2 states for A × 3 states for B = 6
        assertEquals(6, pAB.getTableSize());
    }

    @Test
    public void dimensionsMatchVariableStateCounts() {
        int[] dims = pAB.getDimensions();
        assertEquals(2, dims.length);
        assertEquals(2, dims[0]); // A
        assertEquals(3, dims[1]); // B
    }

    // -----------------------------------------------------------------------
    // setUniform
    // -----------------------------------------------------------------------

    @Test
    public void setUniformMakesFirstVariableStatesEqual() {
        pAB.setUniform();
        // For a CPT P(A|B), setUniform sets each P(A=ai|b) = 1/numStates(A) = 0.5
        double expected = 1.0 / a.getNumStates();
        for (int i = 0; i < pAB.getValues().length; i++) {
            assertEquals(expected, pAB.getValues()[i], DELTA,
                    "Value at index " + i + " should be uniform");
        }
    }

    // -----------------------------------------------------------------------
    // scalePotential
    // -----------------------------------------------------------------------

    @Test
    public void scalePotentialMultipliesAllValues() {
        double[] before = pAB.getValues().clone();
        pAB.scalePotential(2.0);
        for (int i = 0; i < pAB.getValues().length; i++) {
            assertEquals(before[i] * 2.0, pAB.getValues()[i], DELTA);
        }
    }

    @Test
    public void scalePotentialByZeroProducesAllZeros() {
        pAB.scalePotential(0.0);
        for (double v : pAB.getValues()) {
            assertEquals(0.0, v, DELTA);
        }
    }

    @Test
    public void scalePotentialByOneIsIdentity() {
        double[] before = pAB.getValues().clone();
        pAB.scalePotential(1.0);
        assertArrayEquals(before, pAB.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // addVariable
    // -----------------------------------------------------------------------

    @Test
    public void addVariableExpandsTableSize() {
        Variable c = new Variable("C", new State[]{new State("c0"), new State("c1"), new State("c2")});
        TablePotential expanded = (TablePotential) pAB.addVariable(c);
        // Original has 6 entries; C has 3 states → 6 * 3 = 18
        assertEquals(18, expanded.getTableSize());
    }

    @Test
    public void addVariableReplicatesOriginalValues() {
        Variable c = new Variable("C", new State[]{new State("c0"), new State("c1")});
        TablePotential expanded = (TablePotential) pAB.addVariable(c);
        // The original 6 values must appear twice (once for C=c0, once for C=c1)
        for (int i = 0; i < pAB.getValues().length; i++) {
            assertEquals(pAB.getValues()[i], expanded.getValues()[i],             DELTA);
            assertEquals(pAB.getValues()[i], expanded.getValues()[i + pAB.getValues().length], DELTA);
        }
    }

    @Test
    public void addVariableContainsNewVariable() {
        Variable c = new Variable("C", new State[]{new State("c0"), new State("c1")});
        TablePotential expanded = (TablePotential) pAB.addVariable(c);
        assertTrue(expanded.getVariables().contains(c));
    }

    @Test
    public void addVariableDoesNotModifyOriginal() {
        Variable c = new Variable("C", new State[]{new State("c0"), new State("c1")});
        pAB.addVariable(c);
        assertEquals(6, pAB.getTableSize(), "Original must remain unchanged");
    }

    // -----------------------------------------------------------------------
    // reorder
    // -----------------------------------------------------------------------

    @Test
    public void reorderChangesVariableOrder() {
        // Original: [A, B]. Reorder to [B, A].
        TablePotential reordered = (TablePotential) pAB.reorder(List.of(b, a));
        assertEquals(b, reordered.getVariables().get(0));
        assertEquals(a, reordered.getVariables().get(1));
    }

    @Test
    public void reorderPreservesJointDistributionValues() throws Exception {
        TablePotential reordered = (TablePotential) pAB.reorder(List.of(b, a));

        // Verify all (a, b) combinations return the same probability after reordering.
        for (int ai = 0; ai < a.getNumStates(); ai++) {
            for (int bi = 0; bi < b.getNumStates(); bi++) {
                EvidenceCase ev = new EvidenceCase();
                ev.addFinding(new Finding(a, ai));
                ev.addFinding(new Finding(b, bi));
                assertEquals(pAB.getValue(ev), reordered.getValue(ev), DELTA,
                        "Value mismatch for A=" + ai + ", B=" + bi);
            }
        }
    }

    @Test
    public void reorderPreservesTableSize() {
        TablePotential reordered = (TablePotential) pAB.reorder(List.of(b, a));
        assertEquals(pAB.getTableSize(), reordered.getTableSize());
    }

    // -----------------------------------------------------------------------
    // deepCopy(ProbNet)
    // -----------------------------------------------------------------------

    @Test
    public void deepCopyResolvesVariablesFromCopyNet() {
        // Build a ProbNet, copy it, then deepCopy pAB into the copy.
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Node na = net.addNode(a, NodeType.CHANCE);
        Node nb = net.addNode(b, NodeType.CHANCE);
        net.addLink(nb, na, true);
        na.setPotentials(List.of(pAB));

        ProbNet copyNet = net.copy();

        TablePotential deepCopied = (TablePotential) pAB.deepCopy(copyNet);

        // Variables in the deep copy must be the instances from copyNet, not the originals.
        for (Variable v : deepCopied.getVariables()) {
            assertNotNull(copyNet.getNode(v),
                    "deepCopy variable " + v.getName() + " must belong to copyNet");
        }
    }

    @Test
    public void deepCopyValuesAreIndependent() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.addNode(a, NodeType.CHANCE);
        net.addNode(b, NodeType.CHANCE);
        ProbNet copyNet = net.copy();

        TablePotential deepCopied = (TablePotential) pAB.deepCopy(copyNet);
        deepCopied.getValues()[0] = 0.0;
        assertEquals(0.9, pAB.getValues()[0], DELTA,
                "Mutating deepCopy must not affect original values");
    }

    @Test
    public void deepCopyPreservesValues() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.addNode(a, NodeType.CHANCE);
        net.addNode(b, NodeType.CHANCE);
        ProbNet copyNet = net.copy();

        TablePotential deepCopied = (TablePotential) pAB.deepCopy(copyNet);
        assertArrayEquals(pAB.getValues(), deepCopied.getValues(), DELTA);
    }

    // -----------------------------------------------------------------------
    // getValues / setValues
    // -----------------------------------------------------------------------

    @Test
    public void getValuesReturnsCurrentArray() {
        double[] returned = pAB.getValues();
        assertSame(pAB.getValues(), returned,
                "getValues() must return the same array reference as .getValues()");
    }

    @Test
    public void setValuesReplacesArray() {
        double[] newValues = new double[]{1, 0, 0, 1, 1, 0};
        pAB.setValues(newValues);
        assertSame(newValues, pAB.getValues());
    }

    @Test
    public void setValuesMakesGetValuesReflectNewContents() {
        double[] newValues = new double[]{0.1, 0.9, 0.2, 0.8, 0.3, 0.7};
        pAB.setValues(newValues);
        assertEquals(0.1, pAB.getValue(List.of(a, b), new int[]{0, 0}), DELTA);
    }

    // -----------------------------------------------------------------------
    // isUncertain
    // -----------------------------------------------------------------------

    @Test
    public void isUncertainFalseByDefault() {
        assertFalse(pAB.isUncertain());
    }

    @Test
    public void isUncertainTrueWhenUncertainValuesAssigned() {
        UncertainTablePotential utp = new UncertainTablePotential(pAB.getVariables(), pAB.getPotentialRole());
        utp.uncertainValues = new UncertainValue[utp.getValues().length];
        assertTrue(utp.isUncertain());
    }

    // -----------------------------------------------------------------------
    // Constant potential (no variables)
    // -----------------------------------------------------------------------

    @Test
    public void constantPotentialHasTableSizeOne() {
        TablePotential constant = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY);
        assertEquals(1, constant.getTableSize());
    }

    @Test
    public void constantPotentialHasNoVariables() {
        TablePotential constant = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY);
        assertTrue(constant.getVariables().isEmpty());
    }
}
