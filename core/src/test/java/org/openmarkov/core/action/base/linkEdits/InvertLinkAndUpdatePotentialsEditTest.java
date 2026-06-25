/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link InvertLinkAndUpdatePotentialsEdit}.
 *
 * <p>Issue #472: after do → undo → redo, the child node ended up with an empty
 * potential list because {@code redo()} mistakenly added {@code childNewPotential}
 * to {@code xNewPotentials} instead of {@code yNewPotentials}.
 *
 * <p>Second bug (shared-parents aliasing): when X and Y had shared parents (set B),
 * {@code doEdit()} aliased {@code newParents = parentParents} and then mutated it
 * before using it to compute the second set, so B-nodes were incorrectly placed in
 * {@code linksToUndo}. {@code undo()} then deleted those pre-existing links.
 *
 * @author Manuel Arias
 */
class InvertLinkAndUpdatePotentialsEditTest {

    // Net: X → Y
    // X: 2 states, Y: 2 states
    private ProbNet net;
    private Variable x, y;
    private Node nodeX, nodeY;

    @BeforeEach
    void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);

        x = new Variable("X", new State[]{new State("x0"), new State("x1")});
        y = new Variable("Y", new State[]{new State("y0"), new State("y1")});

        nodeX = net.addNode(x, NodeType.CHANCE);
        nodeY = net.addNode(y, NodeType.CHANCE);
        net.addLink(nodeX, nodeY, true);

        // P(X): uniform
        TablePotential px = new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.6, 0.4});
        nodeX.setPotentials(List.of(px));

        // P(Y | X): [y0|x0, y1|x0, y0|x1, y1|x1]
        TablePotential pyGivenX = new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1, 0.3, 0.7});
        nodeY.setPotentials(List.of(pyGivenX));
    }

    @Test
    void afterDoEdit_bothNodesHaveOnePotential() throws DoEditException {
        InvertLinkAndUpdatePotentialsEdit edit =
                new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();

        assertEquals(1, nodeX.getPotentials().size(), "X must have exactly one potential after inversion");
        assertEquals(1, nodeY.getPotentials().size(), "Y must have exactly one potential after inversion");
    }

    @Test
    void afterUndo_potentialsAreRestoredToOriginals() throws DoEditException {
        TablePotential origPx     = (TablePotential) nodeX.getPotentials().getFirst();
        TablePotential origPyGivenX = (TablePotential) nodeY.getPotentials().getFirst();

        InvertLinkAndUpdatePotentialsEdit edit =
                new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();
        edit.undo();

        assertSame(origPx,      nodeX.getPotentials().getFirst(), "X's potential must be the original after undo");
        assertSame(origPyGivenX, nodeY.getPotentials().getFirst(), "Y's potential must be the original after undo");
    }

    /**
     * Regression test for issue #472.
     * Before the fix, {@code redo()} added {@code childNewPotential} to
     * {@code xNewPotentials} instead of {@code yNewPotentials}, leaving Y with
     * an empty potential list after undo+redo.
     */
    @Test
    void afterUndoRedo_bothNodesHaveOnePotential() throws DoEditException {
        InvertLinkAndUpdatePotentialsEdit edit =
                new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();
        edit.undo();
        edit.redo();

        assertEquals(1, nodeX.getPotentials().size(),
                "X must have exactly one potential after undo+redo");
        assertEquals(1, nodeY.getPotentials().size(),
                "Y must have exactly one potential after undo+redo (was 0 before fix)");
    }

    @Test
    void afterUndoRedo_parentPotentialIsRestoredOnParent() throws DoEditException {
        InvertLinkAndUpdatePotentialsEdit edit =
                new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();
        TablePotential afterDo_X = (TablePotential) nodeX.getPotentials().getFirst();
        edit.undo();
        edit.redo();

        assertSame(afterDo_X, nodeX.getPotentials().getFirst(),
                "X's potential after redo must be the one computed by doEdit()");
    }

    @Test
    void afterUndoRedo_childPotentialIsRestoredOnChild() throws DoEditException {
        InvertLinkAndUpdatePotentialsEdit edit =
                new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();
        TablePotential afterDo_Y = (TablePotential) nodeY.getPotentials().getFirst();
        edit.undo();
        edit.redo();

        assertSame(afterDo_Y, nodeY.getPotentials().getFirst(),
                "Y's potential after redo must be the one computed by doEdit()");
    }

    // ------------------------------------------------------------------
    // Shared-parents (B ≠ ∅) — regression for aliasing bug in doEdit()
    //
    // Net: A → X, A → Y, X → Y  (A is a shared parent of X and Y, set B = {A})
    // Inverting X → Y must not touch the pre-existing A→X and A→Y links.
    // Before the fix, A was incorrectly placed in linksToUndo, so undo()
    // deleted the A→X link that was there before the inversion.
    // ------------------------------------------------------------------

    @Test
    void withSharedParent_afterDoEdit_sharedParentLinkToXStillExists() throws DoEditException {
        // Net: A → X, A → Y, X → Y
        Variable a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        Node nodeA = net.addNode(a, NodeType.CHANCE);
        net.addLink(nodeA, nodeX, true);
        net.addLink(nodeA, nodeY, true);

        // Assign minimal potentials
        nodeA.setPotentials(List.of(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.5, 0.5})));
        nodeX.setPotentials(List.of(new TablePotential(List.of(x, a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.8, 0.2, 0.4, 0.6})));
        nodeY.setPotentials(List.of(new TablePotential(List.of(y, x, a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1, 0.7, 0.3, 0.6, 0.4, 0.2, 0.8})));

        InvertLinkAndUpdatePotentialsEdit edit = new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();

        assertTrue(nodeX.getParents().contains(nodeA),
                "A must still be a parent of X after inversion");
    }

    @Test
    void withSharedParent_afterUndo_sharedParentLinkToXRestored() throws DoEditException {
        // Net: A → X, A → Y, X → Y
        Variable a = new Variable("A", new State[]{new State("a0"), new State("a1")});
        Node nodeA = net.addNode(a, NodeType.CHANCE);
        net.addLink(nodeA, nodeX, true);
        net.addLink(nodeA, nodeY, true);

        nodeA.setPotentials(List.of(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.5, 0.5})));
        nodeX.setPotentials(List.of(new TablePotential(List.of(x, a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.8, 0.2, 0.4, 0.6})));
        nodeY.setPotentials(List.of(new TablePotential(List.of(y, x, a), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1, 0.7, 0.3, 0.6, 0.4, 0.2, 0.8})));

        InvertLinkAndUpdatePotentialsEdit edit = new InvertLinkAndUpdatePotentialsEdit(net, x, y);
        edit.executeEdit();
        edit.undo();

        // Before the aliasing fix, undo() deleted the pre-existing A→X link
        assertTrue(nodeX.getParents().contains(nodeA),
                "A→X must still exist after undo (was deleted before the aliasing fix)");
        assertEquals(1, nodeX.getParents().size(),
                "X must have exactly one parent (A) after undo");
    }
}
