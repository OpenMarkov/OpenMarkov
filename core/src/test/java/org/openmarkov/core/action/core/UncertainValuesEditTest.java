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
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UncertainValuesEdit}, focusing on:
 * <ul>
 *   <li><b>Fix A</b> — a node holding a plain {@link TablePotential} is transparently upgraded to an
 *       {@link UncertainTablePotential} when uncertainty is first added (a plain potential rejects
 *       uncertain values since they were moved into the subclass).</li>
 *   <li><b>Fix D</b> — undoing restores the original potential and the original numeric values without
 *       corrupting the rest of the table.</li>
 * </ul>
 *
 * <p>Fixture: P(A | B), A (2 states) conditioned on B (3 states).
 * The edited column is B=b1 (basePosition 2, positions 2 and 3).
 */
class UncertainValuesEditTest {

    private static final double DELTA = 1e-9;

    private Variable a, b;
    private Node nodeA;
    private double[] originalValues;

    @BeforeEach
    void setUp() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        b = new Variable("B", new State[]{new State("b0"), new State("b1"), new State("b2")});
        nodeA = net.addNode(a, NodeType.CHANCE);
        Node nodeB = net.addNode(b, NodeType.CHANCE);
        net.addLink(nodeB, nodeA, true);

        originalValues = new double[]{0.9, 0.1,   // B=b0
                                      0.7, 0.3,   // B=b1
                                      0.4, 0.6};  // B=b2
        // P(A|B): variables = [A, B]; a plain TablePotential, no uncertainty.
        TablePotential cpt = new TablePotential(List.of(a, b), PotentialRole.CONDITIONAL_PROBABILITY,
                originalValues.clone());
        nodeA.setPotentials(List.of(cpt));
    }

    private UncertainValuesEdit editColumnB1() {
        // basePosition 2 = column B=b1; chance variable; selectedColumn is the UI column index (irrelevant here)
        return new UncertainValuesEdit(nodeA,
                List.of(new UncertainValue(0.25), new UncertainValue(0.75)),
                List.of(0.25, 0.75), 2, 2, true);
    }

    // ------------------------------------------------------------------
    // Fix A — plain TablePotential is upgraded instead of throwing
    // ------------------------------------------------------------------

    @Test
    void addingUncertaintyDoesNotThrowOnAPlainTablePotential() {
        assertDoesNotThrow(() -> editColumnB1().executeEdit());
    }

    @Test
    void nodePotentialIsUpgradedToUncertainTablePotential() throws DoEditException {
        assertFalse(nodeA.getPotentials().get(0) instanceof UncertainTablePotential,
                "Precondition: node starts with a plain TablePotential");
        editColumnB1().executeEdit();
        assertInstanceOf(UncertainTablePotential.class, nodeA.getPotentials().get(0),
                "Node must hold an UncertainTablePotential after adding uncertainty");
    }

    @Test
    void upgradePreservesAllValuesAndWritesTheEditedColumn() throws DoEditException {
        editColumnB1().executeEdit();
        TablePotential p = (TablePotential) nodeA.getPotentials().get(0);
        double[] v = p.getValues();
        // edited column B=b1
        assertEquals(0.25, v[2], DELTA);
        assertEquals(0.75, v[3], DELTA);
        // other columns preserved
        assertEquals(0.9, v[0], DELTA);
        assertEquals(0.1, v[1], DELTA);
        assertEquals(0.4, v[4], DELTA);
        assertEquals(0.6, v[5], DELTA);
        // uncertain values stored at the edited column only
        UncertainValue[] uv = p.getUncertainValues();
        assertNotNull(uv[2]);
        assertNotNull(uv[3]);
        assertNull(uv[0]);
        assertNull(uv[4]);
    }

    // ------------------------------------------------------------------
    // Fix D — undo restores the original plain potential and values
    // ------------------------------------------------------------------

    @Test
    void undoRestoresTheOriginalPlainPotential() throws DoEditException {
        UncertainValuesEdit edit = editColumnB1();
        edit.executeEdit();
        edit.undo();
        assertFalse(nodeA.getPotentials().get(0) instanceof UncertainTablePotential,
                "Undo must restore the original plain TablePotential");
    }

    @Test
    void undoRestoresTheOriginalValuesWithoutCorruptingTheTable() throws DoEditException {
        UncertainValuesEdit edit = editColumnB1();
        edit.executeEdit();
        edit.undo();
        TablePotential p = (TablePotential) nodeA.getPotentials().get(0);
        assertEquals(originalValues.length, p.getValues().length, "Values table must keep its length");
        assertArrayEquals(originalValues, p.getValues(), DELTA);
    }

    @Test
    void redoAfterUndoReappliesTheUpgradeAndEdit() throws DoEditException {
        UncertainValuesEdit edit = editColumnB1();
        edit.executeEdit();
        edit.undo();
        edit.redo();
        assertInstanceOf(UncertainTablePotential.class, nodeA.getPotentials().get(0));
        TablePotential p = (TablePotential) nodeA.getPotentials().get(0);
        assertEquals(0.25, p.getValues()[2], DELTA);
        assertEquals(0.75, p.getValues()[3], DELTA);
    }
}
