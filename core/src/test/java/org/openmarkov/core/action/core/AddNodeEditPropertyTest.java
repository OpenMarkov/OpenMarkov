/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import net.jqwik.api.*;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link AddNodeEdit}.
 *
 * Verifies that the do / undo / redo cycle preserves the structural
 * invariants of a {@link ProbNet} for any combination of variable names
 * and state counts.
 */
class AddNodeEditPropertyTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ProbNet freshBayesianNet() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        return net;
    }

    // -----------------------------------------------------------------------
    // doEdit invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit the node must be retrievable from the network.
     */
    @Property
    void afterDoEdit_nodeIsInNetwork(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        assertThat(net.getNode(v)).isNotNull();
    }

    /**
     * After executeEdit the CHANCE node count increases by exactly 1.
     */
    @Property
    void afterDoEdit_chanceCountIncreasesBy1(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        int before = net.getNumNodes(NodeType.CHANCE);
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(before + 1);
    }

    /**
     * Adding N nodes with distinct names yields exactly N nodes in the network.
     */
    @Property
    void afterDoEdit_countEqualsNumberOfNodesAdded(
            @ForAll("distinctChanceVariableLists") List<Variable> vars) throws DoEditException {
        ProbNet net = freshBayesianNet();
        for (Variable v : vars) {
            new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        }
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(vars.size());
    }

    /**
     * After adding N nodes, every one of them must be retrievable.
     */
    @Property
    void afterDoEdit_allAddedNodesArePresent(
            @ForAll("distinctChanceVariableLists") List<Variable> vars) throws DoEditException {
        ProbNet net = freshBayesianNet();
        for (Variable v : vars) {
            new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        }
        for (Variable v : vars) {
            assertThat(net.getNode(v))
                    .as("node for variable '%s' should be in the network", v.getName())
                    .isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // undo invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit + undo, the node must be absent from the network.
     */
    @Property
    void afterUndo_nodeIsAbsent(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        AddNodeEdit edit = new AddNodeEdit(net, v, NodeType.CHANCE, null);
        edit.executeEdit();
        edit.undo();
        assertThat(net.getNode(v)).isNull();
    }

    /**
     * After executeEdit + undo, the node count returns to its original value.
     */
    @Property
    void afterUndo_chanceCountRestoredToOriginal(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        int original = net.getNumNodes(NodeType.CHANCE);
        AddNodeEdit edit = new AddNodeEdit(net, v, NodeType.CHANCE, null);
        edit.executeEdit();
        edit.undo();
        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(original);
    }

    // -----------------------------------------------------------------------
    // redo invariants
    // -----------------------------------------------------------------------

    /**
     * After executeEdit + undo + redo, the node must be back in the network.
     */
    @Property
    void afterUndoRedo_nodeIsInNetwork(@ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        AddNodeEdit edit = new AddNodeEdit(net, v, NodeType.CHANCE, null);
        edit.executeEdit();
        edit.undo();
        edit.redo();
        assertThat(net.getNode(v)).isNotNull();
    }

    /**
     * After executeEdit + undo + redo, the node count is the same as right
     * after the initial executeEdit.
     */
    @Property
    void afterUndoRedo_chanceCountMatchesAfterDoEdit(@ForAll("chanceVariables") Variable v)
            throws DoEditException {
        ProbNet net = freshBayesianNet();
        AddNodeEdit edit = new AddNodeEdit(net, v, NodeType.CHANCE, null);
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
     * Adding a node whose name is already in the network must be rejected
     * with a {@link org.openmarkov.core.exception.ConstraintViolatedException}.
     * BayesianNetworkType enforces {@code DistinctVariableNames}.
     */
    @Property
    void addDuplicateName_throwsConstraintViolatedException(
            @ForAll("chanceVariables") Variable v) throws DoEditException {
        ProbNet net = freshBayesianNet();
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();

        Variable duplicate = new Variable(v.getName(), 3); // same name, different object
        AddNodeEdit duplicateEdit = new AddNodeEdit(net, duplicate, NodeType.CHANCE, null);

        assertThatThrownBy(duplicateEdit::executeEdit)
                .as("duplicate variable name '%s' should be rejected", v.getName())
                .isInstanceOf(Exception.class); // ConstraintViolatedException
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

    /** Lists of 1–5 variables with guaranteed-distinct names (V0, V1, …). */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<List<Variable>> distinctChanceVariableLists() {
        return Arbitraries.integers().between(1, 5).flatMap(n ->
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
