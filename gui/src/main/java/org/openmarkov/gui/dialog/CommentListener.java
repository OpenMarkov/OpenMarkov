/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.exception.DoEditException;

/**
 * Listener interface notified when a comment foreground associated with a network element
 * has been modified by the user.
 */
public interface CommentListener {
    /**
     * Called when the comment foreground has changed so the edit can be applied to the model.
     *
     * @throws DoEditException if the edit cannot be executed
     */
    void commentHasChanged() throws DoEditException;
}
