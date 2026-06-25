/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;

/**
 * Listener interface for receiving notifications about {@link PNEdit} lifecycle events
 * such as execution, undo, redo, and constraint violations.
 */
public interface PNEditListener {
    
    /**
     * Triggered before an edit is about to happen happened.
     */
    default void beforeEditExecutes(PNEdit edit) {
    }
    
    /**
     * Triggered after an edit has happened.
     */
    default void afterEditExecutes(PNEdit edit) {
    }
    
    /**
     * Triggered after an edit has failed.
     * <p>
     * The method {@link PNEditListener#beforeEditExecutes(PNEdit)} must have happened.
     */
    default void onEditFailed(PNEdit edit, DoEditException exception) {
    }
    
    /**
     * Triggered before doing the edit and only if at least one of the constraints is violated.
     */
    default void onEditViolatesConstraints(PNEdit edit, ConstraintViolatedException ex) {
    }
    
    /**
     * Triggered after undoing an edit.
     */
    default void afterUndoingEdit(PNEdit edit) {
    }
    
    /**
     * Triggered after redoing an edit.
     */
    default void afterRedoingEdit(PNEdit edit) {
    }
    
    
}
