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
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link RemoveLinkEdit}.
 *
 * <p>Verifies that the do / undo / redo cycle correctly maintains:
 * <ul>
 *   <li>the parent–child relationship in the graph,</li>
 *   <li>the variable lists of the affected node's potentials, and</li>
 *   <li>the identity of the potential objects restored by undo.</li>
 * </ul>
 */
class RemoveLinkEditPropertyTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private record LinkedBN(ProbNet net, Variable vA, Variable vB, Node nodeA, Node nodeB) {}

    /**
     * Creates a two-node BN with a directed link A→B already in place.
     */
    private LinkedBN linkedBN(int statesA, int statesB) throws DoEditException {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable vA = new Variable("A", statesA);
        Variable vB = new Variable("B", statesB);
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, vA, vB, true).executeEdit();
        return new LinkedBN(net, vA, vB, net.getNode(vA), net.getNode(vB));
    }

    /**
     * Creates a three-node chain A→B→C.
     */
    private record ChainBN(ProbNet net, Variable vA, Variable vB, Variable vC,
                           Node nodeA, Node nodeB, Node nodeC) {}

    private ChainBN chainBN(int sA, int sB, int sC) throws DoEditException {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable vA = new Variable("A", sA);
        Variable vB = new Variable("B", sB);
        Variable vC = new Variable("C", sC);
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vC, NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, vA, vB, true).executeEdit();
        new AddLinkEdit(net, vB, vC, true).executeEdit();
        return new ChainBN(net, vA, vB, vC, net.getNode(vA), net.getNode(vB), net.getNode(vC));
    }

    // -----------------------------------------------------------------------
    // doEdit invariants
    // -----------------------------------------------------------------------

    /**
     * After removing A→B, node A must not appear in B's parent list.
     */
    @Property
    void afterDoEdit_parentIsRemovedFromChildParentsList(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        assertThat(g.nodeB().getParents()).doesNotContain(g.nodeA());
    }

    /**
     * After removing A→B, A's variable must not appear in B's potential variables.
     */
    @Property
    void afterDoEdit_parentVariableRemovedFromChildPotential(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        List<Variable> potVars = g.nodeB().getPotentials().getFirst().getVariables();
        assertThat(potVars).doesNotContain(g.vA());
    }

    /**
     * After removing A→B, B still has exactly one potential.
     */
    @Property
    void afterDoEdit_childHasExactlyOnePotential(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        assertThat(g.nodeB().getPotentials()).hasSize(1);
    }

    /**
     * Removing A→B in a chain A→B→C does not affect the link B→C.
     */
    @Property
    void afterDoEdit_otherLinksUnaffected(
            @ForAll @IntRange(min = 2, max = 4) int sA,
            @ForAll @IntRange(min = 2, max = 4) int sB,
            @ForAll @IntRange(min = 2, max = 4) int sC) throws DoEditException {
        var g = chainBN(sA, sB, sC);
        new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true).executeEdit();
        assertThat(g.nodeC().getParents()).contains(g.nodeB());
    }

    // -----------------------------------------------------------------------
    // undo invariants
    // -----------------------------------------------------------------------

    /**
     * After removeLink + undo, node A must be back in B's parent list.
     */
    @Property
    void afterUndo_parentIsRestoredInChildParentsList(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        RemoveLinkEdit edit = new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();
        assertThat(g.nodeB().getParents()).contains(g.nodeA());
    }

    /**
     * After removeLink + undo, B's potentials are restored by identity
     * (same object references, not copies).
     */
    @Property
    void afterUndo_oldPotentialsAreRestoredByIdentity(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        Potential potBeforeRemove = g.nodeB().getPotentials().getFirst();

        RemoveLinkEdit edit = new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();

        assertThat(g.nodeB().getPotentials().getFirst()).isSameAs(potBeforeRemove);
    }

    /**
     * After removeLink + undo, B's potential variables must contain A's variable
     * again (matching the state before the removal).
     */
    @Property
    void afterUndo_childPotentialVariablesRestored(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        List<Variable> varsBefore =
                new ArrayList<>(g.nodeB().getPotentials().getFirst().getVariables());

        RemoveLinkEdit edit = new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();

        List<Variable> varsAfterUndo = g.nodeB().getPotentials().getFirst().getVariables();
        assertThat(varsAfterUndo).containsExactlyElementsOf(varsBefore);
    }

    // -----------------------------------------------------------------------
    // redo invariants
    // -----------------------------------------------------------------------

    /**
     * After removeLink + undo + redo, A must not be in B's parents.
     */
    @Property
    void afterUndoRedo_parentIsRemovedAgain(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        RemoveLinkEdit edit = new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();
        edit.redo();
        assertThat(g.nodeB().getParents()).doesNotContain(g.nodeA());
    }

    /**
     * After removeLink + undo + redo, A's variable must not be in B's potential.
     */
    @Property
    void afterUndoRedo_parentVariableRemovedFromChildPotentialAgain(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        var g = linkedBN(sA, sB);
        RemoveLinkEdit edit = new RemoveLinkEdit(g.net(), g.vA(), g.vB(), true);
        edit.executeEdit();
        edit.undo();
        edit.redo();
        List<Variable> potVars = g.nodeB().getPotentials().getFirst().getVariables();
        assertThat(potVars).doesNotContain(g.vA());
    }

    // -----------------------------------------------------------------------
    // Structural round-trip: add then remove restores original state
    // -----------------------------------------------------------------------

    /**
     * Adding a link and then immediately removing it must leave B's potential
     * variables in the same state as before the link was added.
     */
    @Property
    void addThenRemove_potentialVariablesRestoredToOriginal(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable vA = new Variable("A", sA);
        Variable vB = new Variable("B", sB);
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        Node nodeB = net.getNode(vB);

        List<Variable> varsOriginal =
                new ArrayList<>(nodeB.getPotentials().getFirst().getVariables());

        new AddLinkEdit(net, vA, vB, true).executeEdit();
        new RemoveLinkEdit(net, vA, vB, true).executeEdit();

        List<Variable> varsAfter = nodeB.getPotentials().getFirst().getVariables();
        assertThat(varsAfter).containsExactlyElementsOf(varsOriginal);
    }

    // -----------------------------------------------------------------------
    // UTILITY node paths (SumPotential / UniformPotential)
    // -----------------------------------------------------------------------

    /**
     * Removing a link from a NUMERIC parent to a UTILITY node that retains
     * at least one other NUMERIC parent must produce a {@link SumPotential}
     * without the removed parent's variable.
     * (After removal, {@code onlyNumericalParents()} must still be true for
     * the SumPotential branch to execute.)
     */
    @Example
    void removeLink_utilityWithOnlyNumericalParents_producesSumPotential() throws DoEditException {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);

        Variable vNum1 = new Variable("Num1"); // NUMERIC
        Variable vNum2 = new Variable("Num2"); // NUMERIC
        Variable vU = new Variable("U");       // NUMERIC → UTILITY node
        new AddNodeEdit(net, vNum1, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vNum2, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vU, NodeType.UTILITY, null).executeEdit();
        new AddLinkEdit(net, vNum1, vU, true).executeEdit();
        new AddLinkEdit(net, vNum2, vU, true).executeEdit();

        Node nodeU = net.getNode(vU);
        assertThat(nodeU.onlyNumericalParents()).isTrue();

        new RemoveLinkEdit(net, vNum1, vU, true).executeEdit();

        // After removing Num1, Num2 remains → still only numerical parents
        Potential newPot = nodeU.getPotentials().getFirst();
        assertThat(newPot).isInstanceOf(SumPotential.class);
        assertThat(newPot.getVariables()).doesNotContain(vNum1);
        assertThat(nodeU.onlyNumericalParents()).isTrue();
    }

    /**
     * Removing a link to a UTILITY node that has mixed parents (numerical +
     * finite-states) must produce a {@link UniformPotential}.
     */
    @Example
    void removeLink_utilityWithMixedParents_producesUniformPotential() throws DoEditException {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);

        Variable vFS = new Variable("FS", 3);  // FINITE_STATES
        Variable vNum = new Variable("Num");    // NUMERIC
        Variable vU = new Variable("U");        // NUMERIC → UTILITY
        new AddNodeEdit(net, vFS, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vNum, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vU, NodeType.UTILITY, null).executeEdit();
        new AddLinkEdit(net, vFS, vU, true).executeEdit();
        new AddLinkEdit(net, vNum, vU, true).executeEdit();

        Node nodeU = net.getNode(vU);
        assertThat(nodeU.onlyNumericalParents()).isFalse();

        // Remove the numerical parent; FS parent remains → still mixed=false after,
        // but at the moment of doEdit the node has mixed parents
        new RemoveLinkEdit(net, vNum, vU, true).executeEdit();

        Potential newPot = nodeU.getPotentials().getFirst();
        assertThat(newPot).isInstanceOf(UniformPotential.class);
        assertThat(newPot.getVariables()).doesNotContain(vNum);
    }

    /**
     * After removing a link to a UTILITY node + undo, the original potentials
     * must be fully restored.
     */
    @Example
    void removeLink_utilityNode_undoRestoresPotentials() throws DoEditException {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);

        Variable vNum = new Variable("Num");
        Variable vU = new Variable("U");
        new AddNodeEdit(net, vNum, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vU, NodeType.UTILITY, null).executeEdit();
        new AddLinkEdit(net, vNum, vU, true).executeEdit();

        Node nodeU = net.getNode(vU);
        Potential potBefore = nodeU.getPotentials().getFirst();

        RemoveLinkEdit edit = new RemoveLinkEdit(net, vNum, vU, true);
        edit.executeEdit();
        edit.undo();

        assertThat(nodeU.getPotentials().getFirst()).isSameAs(potBefore);
    }

    // -----------------------------------------------------------------------
    // Explicit links — restrictions and revealing states/intervals in undo
    // -----------------------------------------------------------------------

    /**
     * When the network uses explicit links, undo must restore all link
     * properties: restrictions potential, revealing states, and revealing
     * intervals.
     */
    @Example
    void removeLink_explicitLinks_undoRestoresAllLinkProperties() throws DoEditException {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);

        Variable vA = new Variable("A", 2);
        Variable vB = new Variable("B", 2);
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, vA, vB, true).executeEdit();

        // Convert to explicit links and set all three properties on the link
        net.makeLinksExplicit(false);
        assertThat(net.hasExplicitLinks()).isTrue();

        Link<Node> link = net.getLink(net.getNode(vA), net.getNode(vB), true);
        assertThat(link).isNotNull();

        // Set revealing states
        List<State> revealingStates = List.of(new State("revealed"));
        link.setRevealingStates(revealingStates);

        // Set revealing intervals
        List<PartitionedInterval> revealingIntervals = List.of(
                new PartitionedInterval(new double[]{0.0, 1.0}, new boolean[]{true, true}));
        link.setRevealingIntervals(revealingIntervals);

        // Set restrictions potential
        TablePotential restrictionsPot = new TablePotential(
                List.of(vA, vB), PotentialRole.LINK_RESTRICTION);
        double[] values = restrictionsPot.getValues();
        values[0] = 1.0; values[1] = 0.0; values[2] = 1.0; values[3] = 1.0;
        link.setRestrictionsPotential(restrictionsPot);

        // Remove and undo
        RemoveLinkEdit edit = new RemoveLinkEdit(net, vA, vB, true);
        edit.executeEdit();
        edit.undo();

        // Verify all link properties are restored
        Link<Node> restoredLink = net.getLink(net.getNode(vA), net.getNode(vB), true);
        assertThat(restoredLink).isNotNull();
        assertThat(restoredLink.getRevealingStates())
                .as("revealing states should be restored after undo")
                .isEqualTo(revealingStates);
        assertThat(restoredLink.getRevealingIntervals())
                .as("revealing intervals should be restored after undo")
                .isEqualTo(revealingIntervals);
        assertThat(restoredLink.getRestrictionsPotential())
                .as("restrictions potential should be restored after undo")
                .isSameAs(restrictionsPot);
    }
}
