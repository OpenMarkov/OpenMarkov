/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnrecoverableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This class implements various methods that are used by the rest of classes of
 * the application.
 *
 * @author jmendoza
 * @version 1.2 jlgozalo - 10/05/10 - set private constructor, remove functions.
 * @version 1.3 jrico - Added showDialog.
 * and fix warnings
 */
public final class GUIUtils {
    
    /**
     * private constructor for a class with only static methods
     */
    private GUIUtils() {
    }
    
    /**
     * Returns the window that owns the component.
     *
     * @param component component whose top level window will be returned.
     *
     * @return the top level ancestor of the component, if it exists and it is a
     * Window instance, of null if it isn't a window instance.
     */
    public static Window getOwner(JComponent component) {
        Container ancestor = component.getTopLevelAncestor();
        if (ancestor instanceof Window window) {
            return window;
        }
        return null;
    }
    
    /**
     * Checks if the mouse event hasn't key modifiers.
     *
     * @param e mouse event information.
     *
     * @return true if the mouse event hasn't modifiers; otherwise, false.
     */
    public static boolean noMouseModifiers(MouseEvent e) {
        return ((e.getModifiersEx() & 0xF) == 0);
    }
    
    /**
     * Centers a dialog relative to its parent and makes it visible.
     *
     * @param dialog the dialog to display
     */
    public static void showDialog(@NotNull JDialog dialog) {
        var parent = dialog.getParent();
        if (parent != null) {
            dialog.setLocationRelativeTo(dialog.getParent());
        }
        dialog.setVisible(true);
    }
    
    
    // ── Exception wrapper ─────────────────────────────────────────
    
    @FunctionalInterface public interface UIAction {
        void execute() throws Exception;
    }
    
    public static void executeUIAction(UIAction action) {
        try {
            action.execute();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    // ── Exception wrapper ─────────────────────────────────────────
    
    @FunctionalInterface public interface UIRetAction<T> {
        T execute() throws Exception;
    }
    
    public static <T> T executeUIAction(UIRetAction<T> action) {
        try {
            return action.execute();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnrecoverableException(ex);
        }
    }
}
