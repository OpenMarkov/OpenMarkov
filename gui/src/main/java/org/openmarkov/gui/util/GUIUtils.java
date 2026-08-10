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
import org.openmarkov.gui.configuration.CSS;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.java.initialization.Lazy;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Taskbar;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
        int modifiersEx = e.getModifiersEx();
        
        return modifiersEx == 1024;
    }
    
    /**
     * Centers a dialog relative to its parent and makes it visible.
     *
     * @param dialog the dialog to display
     */
    public static void showDialog(@NotNull JDialog dialog) {
        centerDialogToParent(dialog);
        dialog.setVisible(true);
    }
    
    /// Centers a dialog relative to its parent.
    public static void centerDialogToParent(JDialog dialog) {
        var parent = dialog.getParent();
        if (parent != null) {
            dialog.setLocationRelativeTo(dialog.getParent());
        }
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
    
    public static void assignButtonsToKeys(JComponent keyReader, Iterable<? extends JButton> buttons, Consumer<ActionEvent> onCancel) {
        InputMap inputMap = keyReader.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = keyReader.getActionMap();
        
        HashSet<Integer> pickedKeys = new HashSet<>();
        int nextAvailableActionID = 0;
        for (var button : buttons) {
            List<Integer> keyCodes = Collections.emptyList();
            String buttonText = button.getText();
            buttonText = HTML_TAG_PATTERN.matcher(buttonText).replaceAll("");
            if (buttonText.length() > 0) {
                int[] allKeyCodesForChar = getAllKeyCodesForChar(buttonText.charAt(0));
                keyCodes = Arrays.stream(allKeyCodesForChar)
                                 .filter(pickedKeys::add)
                                 .boxed()
                                 .toList();
            }
            
            if (keyCodes.isEmpty()) {
                button.setText(buttonText);
            }
            if (!keyCodes.isEmpty()) {
                button.setMnemonic(keyCodes.getFirst());
                button.setText("<html><u>" + buttonText.charAt(0) + "</u>" + (buttonText.length() <= 1 ? "" : buttonText.substring(1)) + "</html>");
                for (var keycode : keyCodes) {
                    inputMap.put(KeyStroke.getKeyStroke(keycode, 0, false), "ACT_" + nextAvailableActionID + "_PRESS");
                    actionMap.put("ACT_" + nextAvailableActionID + "_PRESS", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Component focusedComponent = KeyboardFocusManager
                                    .getCurrentKeyboardFocusManager()
                                    .getFocusOwner();
                            if (focusedComponent instanceof JTextComponent) {
                                return;
                            }
                            button.getModel().setArmed(true);
                            button.getModel().setPressed(true);
                        }
                    });
                    
                    inputMap.put(KeyStroke.getKeyStroke(keycode, 0, true), "ACT_" + nextAvailableActionID + "_RELEASE");
                    actionMap.put("ACT_" + nextAvailableActionID + "_RELEASE", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (button.getModel().isPressed()) {
                                button.getModel().setArmed(false);
                                button.getModel().setPressed(false);
                                button.doClick();
                            }
                        }
                    });
                    nextAvailableActionID++;
                }
            }
        }
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancelPressedState");
        actionMap.put("cancelPressedState", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean noButtonWasPressed = true;
                for (var button : buttons) {
                    if (button.getModel().isPressed()) {
                        noButtonWasPressed = false;
                        break;
                    }
                }
                buttons.forEach(button -> {
                    button.getModel().setArmed(false);
                    button.getModel().setPressed(false);
                });
                if (noButtonWasPressed) {
                    onCancel.accept(e);
                }
            }
        });
    }
    
    public static int[] getAllKeyCodesForChar(char c) {
        int primaryCode = KeyEvent.getExtendedKeyCodeForChar(c);
        return switch (c) {
            case '+' -> new int[]{KeyEvent.VK_PLUS, KeyEvent.VK_ADD};
            case '-' -> new int[]{KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT};
            case '*' -> new int[]{KeyEvent.VK_ASTERISK, KeyEvent.VK_MULTIPLY};
            case '/' -> new int[]{KeyEvent.VK_SLASH, KeyEvent.VK_DIVIDE};
            case '.' -> new int[]{KeyEvent.VK_PERIOD, KeyEvent.VK_DECIMAL};
            case '0' -> new int[]{KeyEvent.VK_0, KeyEvent.VK_NUMPAD0};
            case '1' -> new int[]{KeyEvent.VK_1, KeyEvent.VK_NUMPAD1};
            case '2' -> new int[]{KeyEvent.VK_2, KeyEvent.VK_NUMPAD2};
            case '3' -> new int[]{KeyEvent.VK_3, KeyEvent.VK_NUMPAD3};
            case '4' -> new int[]{KeyEvent.VK_4, KeyEvent.VK_NUMPAD4};
            case '5' -> new int[]{KeyEvent.VK_5, KeyEvent.VK_NUMPAD5};
            case '6' -> new int[]{KeyEvent.VK_6, KeyEvent.VK_NUMPAD6};
            case '7' -> new int[]{KeyEvent.VK_7, KeyEvent.VK_NUMPAD7};
            case '8' -> new int[]{KeyEvent.VK_8, KeyEvent.VK_NUMPAD8};
            case '9' -> new int[]{KeyEvent.VK_9, KeyEvent.VK_NUMPAD9};
            default -> new int[]{primaryCode};
        };
    }
    
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
}
