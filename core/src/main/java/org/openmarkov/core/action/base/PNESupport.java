/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.ProbNet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used over a {@code openmarkov.inference.ProbNet} where
 * changes can be undone and redone. One edition has two parts:
 * <ol>
 * <li>Inform the listeners and
 * <li>If the action is not vetoed (with a {@code Exception}) for a
 * listener it does the edition.
 * </ol>
 *
 * @author Manuel Arias
 */
public class PNESupport implements PNEditListener /*extends UndoableEditSupport*/ {

    // Attributes
    /**
     * If {@code true} stores editions in
     * {@code openmarkov.undo#EditsHistory} for undo/redo.
     */
    private boolean withUndo;
    
    /**
     * List of undoable edits.
     *
     * @see javax.swing.undo.UndoManager
     */
    public final EditsHistoryStacker editsHistoryStacker;

    /*
    private boolean              significantEdits = true;

    private boolean              editsExecuted    = false;
    private int                  editCount;
    */

    
    // Constructor
    /**
     *
     */
    public PNESupport(ProbNet probNet) {
        super();
        this.probNet = probNet;
        this.withUndo = false;
        this.editsHistoryStacker = new EditsHistoryStacker();
        this.listeners = Collections.synchronizedSet(new LinkedHashSet<>());
        this.listeners.add(this);
    }

    // Methods

    /**
     * Set of listeners notified on edit events (undo, redo, execute).
     * Uses a concurrent set to allow safe addition and removal of listeners
     * from any thread while {@link #redo()}, {@link #undo()} or
     * {@link PNEdit#executeEdit()} iterate over it.
     */
    private final Set<PNEditListener> listeners;

    /**
     * Returns an unmodifiable view of the current listener set.
     * Use {@link #addListener(PNEditListener)} and
     * {@link #removeListener(PNEditListener)} to mutate it.
     *
     * @return unmodifiable view of registered {@link PNEditListener}s
     */
    public Set<PNEditListener> getListeners() {
        return Collections.unmodifiableSet(this.listeners);
    }

    /**
     * Replaces the entire listener set with the given collection.
     * The replacement is performed atomically relative to other
     * {@code addListener}/{@code removeListener} calls.
     *
     * @param listeners new set of listeners; must not be {@code null}
     */
    public void setListeners(Collection<? extends PNEditListener> listeners) {
        this.listeners.clear();
        this.listeners.addAll(listeners);
    }
    
    /**
     * Redoes the most recently undone edit and notifies listeners once
     * with the top-level edit (which may be a {@link CompoundPNEdit}).
     *
     * @see javax.swing.undo.UndoManager#canRedo()
     * @see javax.swing.undo.UndoManager#redo()
     */
    public ArrayList<PNEdit> redo() {
        var redoneEdit = this.editsHistoryStacker.getCurrentUndoManager().redo();
        if (redoneEdit == null) {
            return new ArrayList<>();
        }
        ArrayList<PNEdit> redoneEdits = PNESupport.flattenEdit(redoneEdit);
        for (PNEdit subRedoneEdit : redoneEdits) {
            for (PNEditListener listener : this.listeners) {
                listener.afterRedoingEdit(subRedoneEdit);
            }
        }
        return redoneEdits;
    }
    
    static ArrayList<PNEdit> flattenEdit(@Nullable PNEdit redoneEdit) {
        if (redoneEdit == null) {
            return new ArrayList<>();
        }
        var editsToVisit = new ArrayDeque<PNEdit>();
        editsToVisit.addFirst(redoneEdit);
        var flattenedEdits = new ArrayList<PNEdit>();
        while (!editsToVisit.isEmpty()) {
            var edit = editsToVisit.removeFirst();
            flattenedEdits.add(edit);
            if (edit instanceof MultiEdit multiEdit) {
                multiEdit.getEdits().forEach(editsToVisit::addLast);
            }
        }
        return flattenedEdits;
    }
    
    /**
     * Undoes the most recent edit and notifies listeners once
     * with the top-level edit (which may be a {@link CompoundPNEdit}).
     *
     * @see javax.swing.undo.UndoManager#canUndo()
     * @see javax.swing.undo.UndoManager#undo()
     */
    public ArrayList<PNEdit> undo() {
        var undoneEdit = this.editsHistoryStacker.getCurrentUndoManager().undo();
        if (undoneEdit == null) {
            return new ArrayList<>();
        }
        ArrayList<PNEdit> undoneEdits = PNESupport.flattenEdit(undoneEdit);
        for (PNEdit subUndoneEdit : undoneEdits) {
            for (PNEditListener listener : this.listeners) {
                listener.afterUndoingEdit(subUndoneEdit);
            }
        }
        return undoneEdits;
    }
    
    /**
     * Removes all undone edits from the current history, making redo unavailable.
     */
    public void removeUndoneEdits() {
        this.editsHistoryStacker.getCurrentUndoManager().removeUndoneEdits();
    }
    
    /**
     * Removes all undone edits from the current history, making redo unavailable.
     */
    public void removeDoneEdits() {
        this.editsHistoryStacker.getCurrentUndoManager().removeDoneEdits();
    }
    
    /**
     * Returns the currently active {@link EditsHistory} (may be a sub-history).
     *
     * @return the current edit history
     */
    public EditsHistory getCurrentEditHistory() {
        return this.editsHistoryStacker.getCurrentUndoManager();
    }
    
    /**
     * @return {@code true} if there are edits that can be undone
     */
    public boolean getCanUndo() {
        return this.editsHistoryStacker.getCurrentUndoManager().canUndo();
    }
    
    /**
     * @return {@code true} if there are edits that can be redone
     */
    public boolean getCanRedo() {
        return this.editsHistoryStacker.getCurrentUndoManager().canRedo();
    }
    
    /**
     * Add a {@code OpenParenthesisEdit} edit instance to
     * {@code basicUndoManager} and increases the parenthesis deph.
     */
    public void openNewSubEditHistory() {
        if (this.withUndo) {
            this.editsHistoryStacker.openNewSubEditHistory();
        }
    }
    
    /**
     * Add a {@code CloseParenthesisEdit} edit instance to
     * {@code basicUndoManager} and decreases the parenthesis deph.
     */
    public void closeSubEditHistory(CloseEditStackOptions... closeOperations) {
        if (this.withUndo) {
            this.editsHistoryStacker.closeSubEditHistory(List.of(closeOperations));
        }
    }
    
    /**
     * Cancels the current sub-edit history by undoing all its edits and discarding them.
     */
    public void cancelLastSubEditHistory() {
        this.closeSubEditHistory(CloseEditStackOptions.FORGET, CloseEditStackOptions.UNDO);
    }
    
    /**
     * @return withUndo {@code boolean}.
     */
    public boolean isWithUndo() {
        return this.withUndo;
    }
    
    /**
     * @param withUndo {@code boolean}.
     */
    public void setWithUndo(boolean withUndo) {
        this.withUndo = withUndo;
    }
    
    /**
     * @return probNet {@code ProbNet}.
     */
    /*
     * public ProbNet getProbNet() { return (ProbNet)realSource; }
     */
    public String toString() {
        return "PNESupport. probNet: " + this.probNet + " Number of listeners: " + this.listeners.size() +
                ". withUndo: " + this.withUndo;
    }
    
    private final ProbNet probNet;
    
    /**
     * Registers a listener to be notified of edit events on this network.
     * Safe to call from any thread.
     *
     * @param listener the listener to add; no-op if already registered
     */
    public void addListener(PNEditListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Unregisters a previously added listener.
     * Safe to call from any thread, including from within a listener callback.
     *
     * @param listener the listener to remove; no-op if not registered
     */
    public void removeListener(PNEditListener listener) {
        this.listeners.remove(listener);
    }
    
    private @Nullable PNEdit lastDoneEditWhenSave;
    private boolean networkIsModified = false;
    
    public boolean networkIsModified() {
        return this.networkIsModified;
    }
    
    @Override public void afterEditExecutes(PNEdit edit) {
        onEditChanges(edit);
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        onEditChanges(edit);
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        onEditChanges(edit);
    }
    
    private void onEditChanges(PNEdit edit) {
        if (edit.belongsToACompoundEdit()) {
            return;
        }
        if (this.editsHistoryStacker.getCurrentUndoManager() != this.editsHistoryStacker.getMainEditsHistory()) {
            return;
        }
        this.networkIsModified = (!this.withUndo || this.editsHistoryStacker.getCurrentUndoManager()
                                                                            .nextEditToUndo() != lastDoneEditWhenSave);
    }
    
    public void onSave() {
        this.lastDoneEditWhenSave = this.editsHistoryStacker.getMainEditsHistory().nextEditToUndo();
    }
}
