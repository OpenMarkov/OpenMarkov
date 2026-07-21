/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Maintains two stacks of {@link PNEdit}s (done and undone) to support
 * undo and redo operations on a probabilistic network.
 *
 * @author jrico
 */
public class EditsHistory {
    private final ArrayDeque<PNEdit> doneEdits = new ArrayDeque<>();
    private final ArrayDeque<PNEdit> undoneEdits = new ArrayDeque<>();
    
    /**
     * @return {@code true} if there are edits that can be undone
     */
    public boolean canUndo() {
        return !this.doneEdits.isEmpty();
    }
    
    /**
     * @return {@code true} if there are edits that can be redone
     */
    public boolean canRedo() {
        return !this.undoneEdits.isEmpty();
    }
    
    /**
     * Returns the next edit that would be undone, without actually undoing it.
     *
     * @return the next edit to undo, or {@code null} if the done stack is empty
     */
    public @Nullable PNEdit nextEditToUndo() {
        if (!canUndo()) {
            return null;
        }
        return this.doneEdits.getLast();
    }
    
    /**
     * Returns the next edit that would be redone, without actually redoing it.
     *
     * @return the next edit to redo, or {@code null} if the undone stack is empty
     */
    public @Nullable PNEdit nextEditToRedo() {
        if (!canRedo()) {
            return null;
        }
        return this.undoneEdits.getFirst();
    }
    
    /**
     * Undoes the most recent edit and moves it to the undone stack.
     *
     * @return the edit that was undone, or {@code null} if nothing to undo
     */
    public @Nullable PNEdit undo() {
        if (!canUndo()) {
            return null;
        }
        //Redoes the edit.
        PNEdit undoneEdit = this.doneEdits.getLast();
        undoneEdit.undo();
        //Then adds it to the undone list.
        this.undoneEdits.addFirst(this.doneEdits.removeLast());
        return undoneEdit;
    }
    
    /**
     * Similar to undo, but it does not append the undone edit to the undoneEdits list, nor it triggers the undo event.
     * <p>
     * This is used to make an edit to disappear silently.
     */
    public @Nullable PNEdit removeLastDone() {
        if (!canUndo()) {
            return null;
        }
        return this.doneEdits.removeLast();
    }
    
    /**
     * Redoes the most recently undone edit and moves it back to the done stack.
     *
     * @return the edit that was redone, or {@code null} if nothing to redo
     */
    public @Nullable PNEdit redo() {
        if (!canRedo()) {
            return null;
        }
        //Redoes the edit.
        PNEdit redoneEdit = this.undoneEdits.getFirst();
        redoneEdit.redo();
        //Then adds it to the undone list.
        this.doneEdits.addLast(this.undoneEdits.removeFirst());
        return redoneEdit;
    }
    
    /**
     * Clears all undone edits, making them no longer available for redo.
     */
    public void removeUndoneEdits() {
        this.undoneEdits.clear();
    }
    
    public void removeDoneEdits() {
        this.doneEdits.clear();
    }
    
    /**
     * Records a new edit, clearing any undone edits (redo is no longer possible).
     *
     * @param edit the edit to record
     */
    public void addEdit(PNEdit edit) {
        removeUndoneEdits();
        this.doneEdits.addLast(edit);
    }
    
    /**
     * @return a copy of the done edits list (most recent done last)
     */
    public ArrayList<PNEdit> getDoneEdits() {
        return new ArrayList<>(this.doneEdits);
    }
    
    /**
     * @return a copy of the undone edits list (most recently undone first)
     */
    public ArrayList<PNEdit> getUndoneEdits() {
        return new ArrayList<>(this.undoneEdits);
    }
    
}
