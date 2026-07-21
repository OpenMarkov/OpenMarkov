package org.openmarkov.core.action.base;

/**
 * Options that control how a sub-edit history stack is closed.
 * {@link #FORGET} discards the edits without merging them into the parent stack;
 * {@link #UNDO} reverses all edits in the stack before closing.
 */
public enum CloseEditStackOptions {
    FORGET,
    UNDO,
}
