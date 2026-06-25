/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.gui.loader.element.CursorLoader;

import java.awt.Cursor;

/**
 * Types of states when selecting nodes: - DEFAULT: the mouse cursor is set to
 * default and nothing is happening. - MOVING_NODES: the mouse cursor is set to
 * moveCursor and the nodes can be moved. - SELECTING_NODES: the mouse cursor is
 * set to selectionCursor and various visual elements can be selected
 *
 * @author jmendoza
 * @version 1.0
 * @version 1.1 - jrico: Reduced over complexity
 */
public enum SelectionState {
    
    /** State actived when nothing is happening. */
    NOTHING,
    /** Various nodes are moved. */
    MOVING,
    /** Various nodes can be selected using the selection rectangle. */
    SELECTING,
    /** Creating a link from a node to another */
    CREATING_LINK;
    
    /**
     * Cursor associated to the state.
     */
    private Cursor currentCursor = null;
    
    static {
        for (SelectionState state : SelectionState.values()) {
            state.currentCursor = switch (state) {
                case NOTHING -> CursorLoader.CURSOR_DEFAULT;
                case MOVING -> CursorLoader.CURSOR_NODES_MOVEMENT;
                case SELECTING -> CursorLoader.CURSOR_MULTIPLE_SELECTION;
                case CREATING_LINK -> CursorLoader.CURSOR_LINK.get();
            };
        }
    }
    
    /**
     * Returns the cursor associated to the state.
     *
     * @return the cursor associated to the state.
     */
    public Cursor getCursor() {
        return this.currentCursor;
    }
}
