/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.java.initialization.Lazy;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 * This class implements various methods that are used by the rest of classes of
 * the application.
 *
 * @author jmendoza
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
    
    public static @NotNull JPanel wrapInJPanel(@NotNull Component component,
                                               @SuppressWarnings("AbsoluteAlignmentInUserInterface")
                                               @MagicConstant(intValues = {FlowLayout.LEFT, FlowLayout.CENTER, FlowLayout.RIGHT, FlowLayout.LEADING, FlowLayout.TRAILING})
                                               int alignment) {
        JPanel wrapper = new JPanel(new FlowLayout(alignment, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(component);
        return wrapper;
    }
    
    public static @NotNull JPanel wrapInJPanel(@NotNull Component component) {
        return wrapInJPanel(component, FlowLayout.LEADING);
    }
    
    public static @NotNull JPanel joinComponents(
            @SuppressWarnings("AbsoluteAlignmentInUserInterface")
            @MagicConstant(intValues = {FlowLayout.LEFT, FlowLayout.CENTER, FlowLayout.RIGHT, FlowLayout.LEADING, FlowLayout.TRAILING})
            int align, Component... components) {
        JPanel wrapper = new JPanel(new FlowLayout(align, 0, 0));
        wrapper.setOpaque(false);
        for (Component component : components) {
            wrapper.add(component);
        }
        return wrapper;
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
    
    public static @NotNull JLabel generateTooltipElement(String toolTipText) {
        var restoreDimensionsVisualTooltip = new JLabel("ⓘ");
        Font defaultFont = UIManager.getFont("Label.font");
        restoreDimensionsVisualTooltip.setFont(defaultFont.deriveFont(Font.PLAIN, 20));
        restoreDimensionsVisualTooltip.setToolTipText(toolTipText);
        return restoreDimensionsVisualTooltip;
    }
    
    public static void addHelp(Component component, Lazy<JDialog> helpDialog) {
        component.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F1) {
                    e.consume();
                    helpDialog.get().setVisible(true);
                }
            }
            
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F1) {
                    e.consume();
                    helpDialog.get().setVisible(true);
                }
            }
            
            @Override public void keyReleased(KeyEvent e) {
            
            }
        });
    }
    
    public static @NotNull Lazy<JDialog> generateHelpDialog(String helpTitle, String help) {
        return Lazy.of(() -> {
            var helpDialog = new DialogBase();
            JButton exitHelpButton = new JButton("Exit help");
            helpDialog.setCancelButton(exitHelpButton);
            helpDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            helpDialog.setTitle(helpTitle);
            helpDialog.setLayout(new BorderLayout());
            helpDialog.setModal(false);
            exitHelpButton.addActionListener(e -> helpDialog.dispose());
            JTextPane content = new JTextPane();
            content.setContentType("text/html");
            content.setEditable(false);
            content.setText(help);
            helpDialog.add(new JScrollPane(content), BorderLayout.CENTER);
            helpDialog.setMinimumSize(new Dimension(400, 300));
            helpDialog.pack();
            helpDialog.setMaximumSize(new Dimension(600, 800));
            SwingUtilities.invokeLater(() -> {
                helpDialog.setMinimumSize(new Dimension(0, 0));
                helpDialog.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            });
            return helpDialog;
        });
    }
}
