/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link SetPotentialVariablesEdit}.
 *
 * <p>Previously the edit called {@code Potential.setVariables()} directly,
 * which changed the variable list without reordering the underlying
 * {@code values[]} array.  Because table indexing (offsets) depends on
 * variable order, the old approach produced a corrupted CPT (issue #562).
 *
 * <p>The fix uses {@code Potential.reorder(List)} to obtain a properly
 * reordered copy.
 *
 * @author Manuel Arias
 */
class SetPotentialVariablesEditTest {

    // ------------------------------------------------------------------
    // Fixture
    //   Net: A → C, B → C
    //   C's potential variables: [C, A, B]  (C first = conditioned, then parents)
    //   A has 2 states, B has 3 states → table size = 2 * 2 * 3 = 12
    // ------------------------------------------------------------------

    private Variable a, b, c;
    private Node nodeC;
    private TablePotential cpt;

    @BeforeEach
    void setUp() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());

        a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        b = new Variable("B", new State[]{new State("b0"), new State("b1"), new State("b2")});
        c = new Variable("C", new State[]{new State("c0"), new State("c1")});

        Node nodeA = net.addNode(a, NodeType.CHANCE);
        Node nodeB = net.addNode(b, NodeType.CHANCE);
        nodeC = net.addNode(c, NodeType.CHANCE);
        net.addLink(nodeA, nodeC, true);
        net.addLink(nodeB, nodeC, true);

        // P(C | A, B): variables = [C, A, B]
        // offsets: C=1, A=2, B=4  (numStates: C=2, A=2)
        // Layout (12 entries):
        //   pos  = cState*1 + aState*2 + bState*4
        //   0: c0,a0,b0  1: c1,a0,b0
        //   2: c0,a1,b0  3: c1,a1,b0
        //   4: c0,a0,b1  5: c1,a0,b1
        //   6: c0,a1,b1  7: c1,a1,b1
        //   8: c0,a0,b2  9: c1,a0,b2
        //  10: c0,a1,b2 11: c1,a1,b2
        double[] values = {
                0.9, 0.1,   // c0|a0,b0 ; c1|a0,b0
                0.8, 0.2,   // c0|a1,b0 ; c1|a1,b0
                0.7, 0.3,   // c0|a0,b1 ; c1|a0,b1
                0.6, 0.4,   // c0|a1,b1 ; c1|a1,b1
                0.5, 0.5,   // c0|a0,b2 ; c1|a0,b2
                0.4, 0.6    // c0|a1,b2 ; c1|a1,b2
        };
        cpt = new TablePotential(List.of(c, a, b), PotentialRole.CONDITIONAL_PROBABILITY, values);
        nodeC.setPotentials(List.of(cpt));
    }

    // ------------------------------------------------------------------
    // Core correctness: reorder [C, A, B] → [C, B, A]
    // ------------------------------------------------------------------

    @Test
    void reorderingVariablesPreservesConditionalProbabilities() throws DoEditException {
        // [C, A, B] → [C, B, A]
        SetPotentialVariablesEdit edit = new SetPotentialVariablesEdit(nodeC, List.of(c, b, a));
        edit.executeEdit();

        TablePotential reordered = (TablePotential) nodeC.getPotentials().getFirst();

        // New layout: [C, B, A], offsets: C=1, B=2, A=6
        // pos = cState + bState*2 + aState*6
        // P(c0|a0,b0) = reordered.getValues()[c=0 + b=0*2 + a=0*6] = values[0]
        assertEquals(0.9, reordered.getValues()[0 + 0 + 0], 1e-12, "P(c0|a0,b0)");
        assertEquals(0.1, reordered.getValues()[1 + 0 + 0], 1e-12, "P(c1|a0,b0)");

        // P(c0|a0,b1): new pos = 0 + 1*2 + 0*6 = 2
        // original pos for (c0,a0,b1) = 0 + 0*2 + 1*4 = 4 → value 0.7
        assertEquals(0.7, reordered.getValues()[0 + 2 + 0], 1e-12, "P(c0|a0,b1)");

        // P(c0|a1,b0): new pos = 0 + 0*2 + 1*6 = 6
        // original pos for (c0,a1,b0) = 0 + 1*2 + 0*4 = 2 → value 0.8
        assertEquals(0.8, reordered.getValues()[0 + 0 + 6], 1e-12, "P(c0|a1,b0)");

        // P(c0|a1,b2): new pos = 0 + 2*2 + 1*6 = 10
        // original pos for (c0,a1,b2) = 0 + 1*2 + 2*4 = 10 → value 0.4
        assertEquals(0.4, reordered.getValues()[0 + 4 + 6], 1e-12, "P(c0|a1,b2)");
    }

    @Test
    void reorderingCreatesNewPotential() throws DoEditException {
        SetPotentialVariablesEdit edit = new SetPotentialVariablesEdit(nodeC, List.of(c, b, a));
        edit.executeEdit();

        Potential result = nodeC.getPotentials().getFirst();
        assertNotSame(cpt, result, "reorder() must return a new potential, not mutate the original");
    }

    @Test
    void reorderedPotentialHasNewVariableOrder() throws DoEditException {
        SetPotentialVariablesEdit edit = new SetPotentialVariablesEdit(nodeC, List.of(c, b, a));
        edit.executeEdit();

        assertEquals(List.of(c, b, a), nodeC.getPotentials().getFirst().getVariables());
    }

    // ------------------------------------------------------------------
    // Undo
    // ------------------------------------------------------------------

    @Test
    void undoRestoresOriginalPotential() throws DoEditException {
        SetPotentialVariablesEdit edit = new SetPotentialVariablesEdit(nodeC, List.of(c, b, a));
        edit.executeEdit();
        edit.undo();

        assertSame(cpt, nodeC.getPotentials().getFirst(),
                "undo() must restore the original potential object");
    }

    @Test
    void undoRestoresOriginalValues() throws DoEditException {
        double[] origValues = cpt.getValues().clone();
        SetPotentialVariablesEdit edit = new SetPotentialVariablesEdit(nodeC, List.of(c, b, a));
        edit.executeEdit();
        edit.undo();

        TablePotential restored = (TablePotential) nodeC.getPotentials().getFirst();
        assertArrayEquals(origValues, restored.getValues(), 1e-12,
                "undo() must restore the original values array");
    }

    @Test
    void undoRestoresOriginalVariableOrder() throws DoEditException {
        SetPotentialVariablesEdit edit = new SetPotentialVariablesEdit(nodeC, List.of(c, b, a));
        edit.executeEdit();
        edit.undo();

        assertEquals(List.of(c, a, b), nodeC.getPotentials().getFirst().getVariables(),
                "undo() must restore the original variable order");
    }
}
