/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.ProperUtilityPotentials;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link RemoveNodeEdit}.
 *
 * <p>Verifies that the do / undo / redo cycle preserves the structural
 * invariants of a {@link ProbNet} when removing nodes. Also tests that
 * the {@code ProperUtilityPotentials} constraint blocks removal of the
 * last utility node in an Influence Diagram.
 */
class RemoveNodeEditPropertyTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ProbNet freshBayesianNet() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        return net;
    }

    private ProbNet freshInfluenceDiagram() {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        return net;
    }

    // -----------------------------------------------------------------------
    // doEdit invariants
    // -----------------------------------------------------------------------

    /**
     * After removing a node, it must be absent from the network.
     */
    @Property
    void afterDoEdit_nodeIsAbsent(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        new RemoveNodeEdit(net, v).executeEdit();
        assertThat(net.getNode(v)).isNull();
    }

    /**
     * After removing a node, the CHANCE count decreases by exactly 1.
     */
    @Property
    void afterDoEdit_chanceCountDecreasesBy1(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        int before = net.getNumNodes(NodeType.CHANCE);
        new RemoveNodeEdit(net, v).executeEdit();
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(before - 1);
    }

    /**
     * Adding N nodes then removing one yields N-1 nodes.
     */
    @Property
    void afterDoEdit_countCorrectAfterRemovingOne(
            @ForAll("distinctChanceVariableLists") List<Variable> vars) throws DoEditException {
        ProbNet net = freshBayesianNet();
        for (Variable v : vars) {
            new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        }
        Variable toRemove = vars.getFirst();
        new RemoveNodeEdit(net, toRemove).executeEdit();
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(vars.size() - 1);
    }

    /**
     * After removing a node that has a parent link, the parent no longer lists
     * it as a child.
     */
    @Property
    void afterDoEdit_parentLosesChild(
            @ForAll @IntRange(min = 2, max = 5) int sA,
            @ForAll @IntRange(min = 2, max = 5) int sB) throws DoEditException {
        ProbNet net = freshBayesianNet();
        Variable vA = new Variable("A", sA);
        Variable vB = new Variable("B", sB);
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, vA, vB, true).executeEdit();

        Node nodeA = net.getNode(vA);
        assertThat(nodeA.getChildren()).hasSize(1);

        new RemoveNodeEdit(net, vB).executeEdit();
        assertThat(nodeA.getChildren()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // undo invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit + undo, the node must be back in the network.
     */
    @Property
    void afterUndo_nodeIsPresent(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        RemoveNodeEdit edit = new RemoveNodeEdit(net, v);
        edit.executeEdit();
        edit.undo();
        assertThat(net.getNode(v)).isNotNull();
    }

    /**
     * After executeEdit + undo, the node count is restored.
     */
    @Property
    void afterUndo_chanceCountRestored(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        int countBefore = net.getNumNodes(NodeType.CHANCE);
        RemoveNodeEdit edit = new RemoveNodeEdit(net, v);
        edit.executeEdit();
        edit.undo();
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(countBefore);
    }

    /**
     * After undo, the re-added node preserves its original potentials.
     * The same Node object is re-added, so the potential list content
     * must match what was there before removal.
     */
    @Property
    void afterUndo_potentialsPreserved(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        Node node = net.getNode(v);
        var potentialsBefore = new ArrayList<>(node.getPotentials());

        RemoveNodeEdit edit = new RemoveNodeEdit(net, v);
        edit.executeEdit();
        edit.undo();

        assertThat(net.getNode(v).getPotentials())
                .containsExactlyElementsOf(potentialsBefore);
    }

    // -----------------------------------------------------------------------
    // redo invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit + undo + redo, the node must be absent again.
     */
    @Property
    void afterUndoRedo_nodeIsAbsentAgain(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        RemoveNodeEdit edit = new RemoveNodeEdit(net, v);
        edit.executeEdit();
        edit.undo();
        edit.redo();
        assertThat(net.getNode(v)).isNull();
    }

    /**
     * After executeEdit + undo + redo, the node count matches after initial remove.
     */
    @Property
    void afterUndoRedo_chanceCountMatchesAfterDoEdit(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        RemoveNodeEdit edit = new RemoveNodeEdit(net, v);
        edit.executeEdit();
        int afterDo = net.getNumNodes(NodeType.CHANCE);
        edit.undo();
        edit.redo();
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(afterDo);
    }

    // -----------------------------------------------------------------------
    // Constraint invariants
    // -----------------------------------------------------------------------

    /**
     * Removing the only UTILITY node from a network with the
     * {@code ProperUtilityPotentials} constraint must be rejected.
     * The constraint is OPTIONAL and added after building the network
     * to avoid blocking the AddNodeEdit calls themselves.
     */
    @Example
    void removeLastUtilityNode_throwsConstraintViolatedException() throws DoEditException {
        ProbNet net = freshInfluenceDiagram();
        Variable vC = new Variable("C", 2);
        Variable vU = new Variable("U");
        new AddNodeEdit(net, vC, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vU, NodeType.UTILITY, null).executeEdit();
        // Add the constraint AFTER the network has its utility node
        net.addConstraint(new ProperUtilityPotentials());

        RemoveNodeEdit edit = new RemoveNodeEdit(net, vU);
        assertThatThrownBy(edit::executeEdit)
                .as("removing the only utility node should be rejected")
                .isInstanceOf(DoEditException.class);
    }

    /**
     * Removing one of two UTILITY nodes must succeed even when
     * {@code ProperUtilityPotentials} is active — the constraint
     * only blocks removing the <em>last</em> one.
     */
    @Example
    void removeOneOfTwoUtilityNodes_succeeds() throws DoEditException {
        ProbNet net = freshInfluenceDiagram();
        Variable vC = new Variable("C", 2);
        Variable vU1 = new Variable("U1");
        Variable vU2 = new Variable("U2");
        new AddNodeEdit(net, vC, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vU1, NodeType.UTILITY, null).executeEdit();
        new AddNodeEdit(net, vU2, NodeType.UTILITY, null).executeEdit();
        // Add the constraint AFTER the network has its utility nodes
        net.addConstraint(new ProperUtilityPotentials());

        new RemoveNodeEdit(net, vU1).executeEdit();
        assertThat(net.getNode(vU1)).isNull();
        assertThat(net.getNumNodes(NodeType.UTILITY)).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    @Provide
    @SuppressWarnings("unused")
    Arbitrary<Variable> chanceVariables() {
        return Arbitraries.integers().between(2, 6).flatMap(states ->
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(12)
                .map(name -> new Variable(name, states))
        );
    }

    /** Lists of 2–5 variables with guaranteed-distinct names (V0, V1, …). */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<List<Variable>> distinctChanceVariableLists() {
        return Arbitraries.integers().between(2, 5).flatMap(n ->
            Arbitraries.integers().between(2, 5)
                .list().ofSize(n)
                .map(stateCounts -> {
                    List<Variable> vars = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        vars.add(new Variable("V" + i, stateCounts.get(i)));
                    }
                    return vars;
                })
        );
    }
}
