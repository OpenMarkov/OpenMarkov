/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link AddLinkEdit}.
 *
 * <p>Verifies that the do / undo / redo cycle correctly maintains:
 * <ul>
 *   <li>the parent–child relationship in the graph,</li>
 *   <li>the variable lists of the affected node's potentials, and</li>
 *   <li>the identity of the potential objects restored by undo.</li>
 * </ul>
 *
 * <p>Also includes a regression test ({@link #undoDoesNotMutateOldUtilityPotentialVariables})
 * for the shared-state bug in {@code AddLinkEdit.doEdit()} lines 184-186, where
 * {@code oldPotential.getVariables()} is mutated in place for UTILITY nodes instead
 * of being copied first.
 */
class AddLinkEditPropertyTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Two-node BN with proper default potentials, no link yet.
     * Uses {@link AddNodeEdit} (which calls {@code addNodeConsistently})
     * so both nodes receive default potentials.
     */
    private record TwoNodeBN(ProbNet net, Variable vA, Variable vB, Node nodeA, Node nodeB) {}

    private TwoNodeBN twoNodeBN(int statesA, int statesB) throws DoEditException {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable vA = new Variable("A", statesA);
        Variable vB = new Variable("B", statesB);
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        return new TwoNodeBN(net, vA, vB, net.getNode(vA), net.getNode(vB));
    }

    // -----------------------------------------------------------------------
    // doEdit invariants
    // -----------------------------------------------------------------------

    /**
     * After addLink(A→B), node A must appear in node B's parent list.
     */
    @Property
    void afterDoEdit_parentIsInChildParentsList(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        new AddLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        assertThat(g.nodeB().getParents()).contains(g.nodeA());
    }

    /**
     * After addLink(A→B), the variable of A must appear in the variable list
     * of B's (updated) potential.
     */
    @Property
    void afterDoEdit_parentVariableAppearsInChildPotential(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        new AddLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        List<Variable> potVars = g.nodeB().getPotentials().getFirst().getVariables();
        assertThat(potVars).contains(g.vA());
    }

    /**
     * After addLink(A→B), node B has exactly one potential (the updated CPT).
     */
    @Property
    void afterDoEdit_childHasExactlyOnePotential(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        new AddLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        assertThat(g.nodeB().getPotentials()).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // undo invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit + undo, node A must not appear in node B's parent list.
     */
    @Property
    void afterUndo_parentIsRemovedFromChildParentsList(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        AddLinkEdit edit = new AddLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();
        assertThat(g.nodeB().getParents()).doesNotContain(g.nodeA());
    }

    /**
     * After executeEdit + undo, B's potential must be the same object that
     * existed before the edit (identity, not just equality).
     * This ensures undo restores the original reference, not a copy.
     */
    @Property
    void afterUndo_oldPotentialsAreRestoredByIdentity(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        Potential potentialBeforeEdit = g.nodeB().getPotentials().getFirst();

        AddLinkEdit edit = new AddLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();

        assertThat(g.nodeB().getPotentials().getFirst()).isSameAs(potentialBeforeEdit);
    }

    /**
     * After executeEdit + undo, B's potential must NOT contain A's variable —
     * i.e. the restored potential has the same variable list as before the edit.
     */
    @Property
    void afterUndo_childPotentialVariablesAreRestoredToOriginal(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        List<Variable> varsBefore =
                new ArrayList<>(g.nodeB().getPotentials().getFirst().getVariables());

        AddLinkEdit edit = new AddLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();

        List<Variable> varsAfterUndo = g.nodeB().getPotentials().getFirst().getVariables();
        assertThat(varsAfterUndo)
                .as("after undo, B's potential must not contain A's variable")
                .doesNotContain(g.vA())
                .containsExactlyElementsOf(varsBefore);
    }

    // -----------------------------------------------------------------------
    // redo invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit + undo + redo, node A must be back in node B's parents.
     */
    @Property
    void afterUndoRedo_parentIsRestoredInChildParentsList(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        AddLinkEdit edit = new AddLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();
        edit.redo();
        assertThat(g.nodeB().getParents()).contains(g.nodeA());
    }

    /**
     * After executeEdit + undo + redo, A's variable must be in B's potential again.
     */
    @Property
    void afterUndoRedo_parentVariableIsRestoredInChildPotential(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        AddLinkEdit edit = new AddLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();
        edit.redo();
        List<Variable> potVars = g.nodeB().getPotentials().getFirst().getVariables();
        assertThat(potVars).contains(g.vA());
    }

    // -----------------------------------------------------------------------
    // Constraint invariants
    // -----------------------------------------------------------------------

    /**
     * Adding a self-loop (A→A) must be rejected.
     * BayesianNetworkType enforces {@code NoSelfLoop}.
     */
    @Property
    void selfLink_throwsConstraintViolatedException(
            @ForAll @IntRange(min = 2, max = 5) int states) throws DoEditException {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable v = new Variable("A", states);
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        AddLinkEdit edit = new AddLinkEdit(net, v, v, true);
        assertThatThrownBy(edit::executeEdit)
                .as("self-loop should be rejected by NoSelfLoop constraint")
                .isInstanceOf(Exception.class);
    }

    /**
     * Adding the same directed link twice must be rejected.
     * BayesianNetworkType enforces {@code DistinctLinks} / {@code NoMultipleLinks}.
     */
    @Property
    void duplicateLink_throwsConstraintViolatedException(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = twoNodeBN(sA, sB);
        new AddLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        AddLinkEdit duplicate = new AddLinkEdit(g.net(), g.vA(), g.vB(), true);
        assertThatThrownBy(duplicate::executeEdit)
                .as("adding the same link twice should be rejected")
                .isInstanceOf(Exception.class);
    }

    // -----------------------------------------------------------------------
    // Regression test — potential mutation bug (UTILITY node path)
    // -----------------------------------------------------------------------

    /**
     * Regression test for the shared-state bug in {@code AddLinkEdit.doEdit()}
     * lines 184-186.
     *
     * <p>When the destination node is a UTILITY node with only numerical parents,
     * {@code doEdit()} obtains the variable list of the old potential via
     * {@code oldPotential.getVariables()} and adds the new parent variable
     * directly to that list before constructing the {@code SumPotential}.
     * If {@code getVariables()} returns the live backing list, the old potential
     * is permanently mutated.  After undo, the restored potential therefore
     * already contains the parent variable — which is wrong.
     *
     * <p>This test will <em>fail</em> if the bug is present, and pass once it
     * is fixed by copying the variable list before mutating it.
     */
    @Example
    void undoDoesNotMutateOldUtilityPotentialVariables() throws DoEditException {
        // Influence Diagram: C (CHANCE) → U (UTILITY)
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);

        Variable vC = new Variable("C", 2);
        Variable vU = new Variable("U"); // numeric / utility
        new AddNodeEdit(net, vC, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vU, NodeType.UTILITY, null).executeEdit();
        Node nodeU = net.getNode(vU);

        // Record the variable list of U's potential BEFORE adding the link
        List<Variable> varsBefore = new ArrayList<>(nodeU.getPotentials().getFirst().getVariables());

        AddLinkEdit edit = new AddLinkEdit(net, vC, vU, true);
        edit.executeEdit();
        edit.undo();

        // After undo the old potential object is restored.
        // Its variable list must not have been mutated to include vC.
        List<Variable> varsAfterUndo = nodeU.getPotentials().getFirst().getVariables();
        assertThat(varsAfterUndo)
                .as("undo must not leave the parent variable in the old utility potential")
                .doesNotContain(vC)
                .containsExactlyElementsOf(varsBefore);
    }
}
