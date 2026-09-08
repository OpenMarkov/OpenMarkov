/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.configuration.CSS;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.java.initialization.Lazy;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Taskbar;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

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
        Consumer<KeyEvent> pressAction = e -> {
            if (e.getKeyCode() == KeyEvent.VK_F1) {
                e.consume();
                JDialog dialog = helpDialog.get();
                boolean wasShown = dialog.isVisible();
                if (!wasShown) {
                    dialog.setLocationRelativeTo(MainGUI.INSTANCE);
                }
                dialog.setVisible(true);
            }
        };
        component.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {
                pressAction.accept(e);
            }
            
            @Override public void keyPressed(KeyEvent e) {
                pressAction.accept(e);
            }
            
            @Override public void keyReleased(KeyEvent e) {
            
            }
        });
    }
    
    public static @NotNull Lazy<JDialog> generateHelpDialog(String helpTitle, final String help) {
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
            JScrollPane scrollPane = new JScrollPane(content) {
                @Override public void updateUI() {
                    super.updateUI();
                    var horizontalValue = this.getHorizontalScrollBar().getValue();
                    var verticalValue = this.getVerticalScrollBar().getValue();
                    var caretPositon = content.getCaretPosition();
                    String helpWithCSS = "<html><head><style>" + CSS.F1_CSS.get() + "</style></head><body>" + help + "</body></html>";
                    content.setText(helpWithCSS);
                    this.getHorizontalScrollBar().setValue(horizontalValue);
                    this.getVerticalScrollBar().setValue(verticalValue);
                    content.setCaretPosition(caretPositon);
                }
            };
            helpDialog.add(scrollPane, BorderLayout.CENTER);
            helpDialog.setMinimumSize(new Dimension(400, 300));
            helpDialog.pack();
            helpDialog.setMaximumSize(new Dimension(600, 800));
            SwingUtilities.invokeLater(() -> {
                helpDialog.setMinimumSize(new Dimension(0, 0));
                helpDialog.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            });
            
            Consumer<KeyEvent> onPress = e -> {
                if (e.getKeyCode() != KeyEvent.VK_F1) {
                    return;
                }
                try {
                    Taskbar.getTaskbar().requestWindowUserAttention(helpDialog);
                    e.consume();
                } catch (UnsupportedOperationException _) {
                    //Do not rethrow this exception, it is just making the window to blink.
                }
            };
            KeyListener onPressF1 = new KeyListener() {
                @Override public void keyTyped(KeyEvent e) {
                    onPress.accept(e);
                }
                
                @Override public void keyPressed(KeyEvent e) {
                    onPress.accept(e);
                }
                
                @Override public void keyReleased(KeyEvent e) {
                
                }
            };
            helpDialog.addKeyListener(onPressF1);
            scrollPane.addKeyListener(onPressF1);
            content.addKeyListener(onPressF1);
            return helpDialog;
        });
    }
    
}
