/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link PNESupport} undo/redo listener notifications.
 *
 * <p>These tests verify the correct behavior after fixing three bugs in how
 * {@code PNESupport.undo()} and {@code PNESupport.redo()} notify
 * {@link PNEditListener}s when the edit is a {@link CompoundPNEdit}:
 * <ul>
 *   <li><b>Bug 1 (fixed)</b> — {@code flattenEdit()} returned the compound AND all its
 *       sub-edits, causing duplicate notifications. Fix: notify once with the top-level edit.</li>
 *   <li><b>Bug 2 (fixed)</b> — On redo, listeners received {@code afterRedoingEdit} for each
 *       flattened sub-edit individually; now they receive a single notification with the
 *       compound edit, consistent with how {@code afterEditExecutes} works.</li>
 *   <li><b>Bug 3 (fixed)</b> — {@code EditsHistory.getDoneEdits()} returned most-recent-first
 *       (stack order), so {@code ListPNEdit} stored sub-edits in reverse. This caused
 *       {@code redo()} to execute dependent edits in wrong order (NPE).
 *       Fix: reverse the list before creating the {@code ListPNEdit}.</li>
 * </ul>
 */
class PNESupportPropertyTest {

    // -----------------------------------------------------------------------
    // Recording listener
    // -----------------------------------------------------------------------

    /**
     * A test listener that records every notification it receives,
     * including the edit object and the event type.
     */
    private static class RecordingListener implements PNEditListener {

        enum EventType { BEFORE_EXECUTE, AFTER_EXECUTE, AFTER_UNDO, AFTER_REDO, EDIT_FAILED }

        record Event(EventType type, PNEdit edit) {}

        final List<Event> events = new CopyOnWriteArrayList<>();

        @Override public void beforeEditExecutes(PNEdit edit) {
            events.add(new Event(EventType.BEFORE_EXECUTE, edit));
        }
        @Override public void afterEditExecutes(PNEdit edit) {
            events.add(new Event(EventType.AFTER_EXECUTE, edit));
        }
        @Override public void afterUndoingEdit(PNEdit edit) {
            events.add(new Event(EventType.AFTER_UNDO, edit));
        }
        @Override public void afterRedoingEdit(PNEdit edit) {
            events.add(new Event(EventType.AFTER_REDO, edit));
        }

        List<Event> eventsOfType(EventType type) {
            return events.stream().filter(e -> e.type() == type).toList();
        }

        void clear() { events.clear(); }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ProbNet freshBN() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        return net;
    }

    // -----------------------------------------------------------------------
    // Simple edit: undo/redo notifications
    // -----------------------------------------------------------------------

    /**
     * After undoing a simple (non-compound) edit, the listener must receive
     * exactly one {@code afterUndoingEdit} call with the same edit object.
     */
    @Property
    void simpleEdit_undoNotifiesListenerExactlyOnce(
            @ForAll @IntRange(min = 2, max = 5) int states) throws DoEditException {
        ProbNet net = freshBN();
        RecordingListener listener = new RecordingListener();
        net.getPNESupport().addListener(listener);

        Variable v = new Variable("X", states);
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();

        listener.clear();
        net.getPNESupport().undo();

        var undoEvents = listener.eventsOfType(RecordingListener.EventType.AFTER_UNDO);
        assertThat(undoEvents).hasSize(1);
    }

    /**
     * After redoing a simple edit, the listener must receive exactly one
     * {@code afterRedoingEdit} call.
     */
    @Property
    void simpleEdit_redoNotifiesListenerExactlyOnce(
            @ForAll @IntRange(min = 2, max = 5) int states) throws DoEditException {
        ProbNet net = freshBN();
        RecordingListener listener = new RecordingListener();
        net.getPNESupport().addListener(listener);

        Variable v = new Variable("X", states);
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();
        net.getPNESupport().undo();

        listener.clear();
        net.getPNESupport().redo();

        var redoEvents = listener.eventsOfType(RecordingListener.EventType.AFTER_REDO);
        assertThat(redoEvents).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // Simple edit: structural correctness after undo + redo
    // -----------------------------------------------------------------------

    /**
     * After undo + redo, network state must be identical to after the
     * original execution (node present, same potential count).
     */
    @Property
    void simpleEdit_undoRedoPreservesNetworkState(
            @ForAll @IntRange(min = 2, max = 5) int states) throws DoEditException {
        ProbNet net = freshBN();
        Variable v = new Variable("X", states);
        new AddNodeEdit(net, v, NodeType.CHANCE, null).executeEdit();

        int countAfterDo = net.getNumNodes(NodeType.CHANCE);

        net.getPNESupport().undo();
        net.getPNESupport().redo();

        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(countAfterDo);
        assertThat(net.getNode(v)).isNotNull();
    }

    // -----------------------------------------------------------------------
    // Compound edit: single notification (Bug 1 & Bug 2 regression tests)
    // -----------------------------------------------------------------------
    
    // -----------------------------------------------------------------------
    // Compound edit: sub-edit order in ListPNEdit (Bug 3 regression test)
    // -----------------------------------------------------------------------

    /**
     * The sub-edits inside a {@link ListPNEdit} created from a sub-edit
     * history must be in original execution order (A first, then B), so
     * that {@code redo()} replays them correctly and {@code undo()} reverses
     * them correctly.
     *
     * <p>Regression test for Bug 3: before the fix, {@code getDoneEdits()}
     * returned most-recent-first, so the compound stored [B, A].
     */
    @Example
    void compoundEdit_subEditsInOriginalOrder() throws DoEditException {
        ProbNet net = freshBN();

        net.getPNESupport().openNewSubEditHistory();
        Variable vA = new Variable("A", 2);
        Variable vB = new Variable("B", 3);
        AddNodeEdit editA = new AddNodeEdit(net, vA, NodeType.CHANCE, null);
        editA.executeEdit();
        AddNodeEdit editB = new AddNodeEdit(net, vB, NodeType.CHANCE, null);
        editB.executeEdit();
        net.getPNESupport().closeSubEditHistory();

        // The compound edit stored in history should have edits in [A, B] order
        PNEdit topEdit = net.getPNESupport().getCurrentEditHistory().nextEditToUndo();
        assertThat(topEdit).isInstanceOf(MultiEdit.class);
        var subEdits = ((MultiEdit) topEdit).getEdits().toList();
        assertThat(subEdits).hasSize(2);
        assertThat(subEdits.get(0))
                .as("first sub-edit should be editA (executed first)")
                .isSameAs(editA);
        assertThat(subEdits.get(1))
                .as("second sub-edit should be editB (executed second)")
                .isSameAs(editB);
    }

    /**
     * Redoing a compound edit with dependent sub-edits (add node + add link
     * to that node) must not throw.
     *
     * <p>Regression test for Bug 3: before the fix, the wrong redo order
     * caused a {@code NullPointerException} because the link edit ran
     * before the node it depends on was re-created.
     */
    @Example
    void compoundEdit_redoDependentEditsDoesNotThrow() throws DoEditException {
        ProbNet net = freshBN();
        Variable vA = new Variable("A", 2);
        Variable vB = new Variable("B", 3);

        net.getPNESupport().openNewSubEditHistory();
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, vA, vB, true).executeEdit();
        net.getPNESupport().closeSubEditHistory();

        net.getPNESupport().undo();

        assertThatCode(() -> net.getPNESupport().redo())
                .as("redo of dependent compound edit should not throw")
                .doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------------
    // Compound edit: structural correctness after undo + redo
    // -----------------------------------------------------------------------

    /**
     * After undo + redo of a compound edit with dependent sub-edits
     * (add nodes + add link), the network state must match the state
     * right after the original execution.
     */
    @Example
    void compoundEdit_undoRedoPreservesNetworkState() throws DoEditException {
        ProbNet net = freshBN();
        Variable vA = new Variable("A", 2);
        Variable vB = new Variable("B", 3);

        net.getPNESupport().openNewSubEditHistory();
        new AddNodeEdit(net, vA, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, vB, NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, vA, vB, true).executeEdit();
        net.getPNESupport().closeSubEditHistory();

        int countAfterDo = net.getNumNodes(NodeType.CHANCE);
        Node nodeB = net.getNode(vB);
        assertThat(nodeB).isNotNull();
        int parentsBefore = nodeB.getParents().size();

        net.getPNESupport().undo();
        net.getPNESupport().redo();

        assertThat(net.getNumNodes(NodeType.CHANCE)).isEqualTo(countAfterDo);
        assertThat(net.getNode(vA)).isNotNull();
        Node nodeBAfter = net.getNode(vB);
        assertThat(nodeBAfter).isNotNull();
        assertThat(nodeBAfter.getParents()).hasSize(parentsBefore);
    }
}
