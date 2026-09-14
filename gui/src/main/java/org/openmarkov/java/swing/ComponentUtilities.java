package org.openmarkov.java.swing;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ComponentUtilities {
    
    public static ArrayList<Component> parents(Component component) {
        var parents = new ArrayList<Component>();
        while (component.getParent() != null) {
            parents.add(component.getParent());
            component = component.getParent();
        }
        return parents;
    }
    
    public static ArrayList<Component> parentsWithSelf(Component component) {
        var parents = new ArrayList<Component>();
        parents.add(component);
        while (component.getParent() != null) {
            parents.add(component.getParent());
            component = component.getParent();
        }
        return parents;
    }
    
    public static ArrayList<Component> parentsUpToWindow(Component component) {
        var parents = new ArrayList<Component>();
        while (component.getParent() != null) {
            parents.add(component.getParent());
            if (component.getParent() instanceof Window) {
                break;
            }
            component = component.getParent();
        }
        return parents;
    }
    
    public static ArrayList<Component> parentsWithSelfUpToWindow(Component component) {
        var parents = new ArrayList<Component>();
        parents.add(component);
        while (true) {
            Container parent = component.getParent();
            if (parent == null && component instanceof JPopupMenu popupMenu) {
                if (popupMenu.getInvoker() instanceof Container popupMenuInvoker) {
                    parent = popupMenuInvoker;
                }
                ;
            }
            if (parent == null) break;
            parents.add(parent);
            if (parent instanceof Window) {
                break;
            }
            component = parent;
        }
        return parents;
    }
    
    public enum ComponentSearchOptions {
        DO_NOT_SEARCH_OWNED_WINDOWS
    }
    
    public static final EnumSet<ComponentSearchOptions> DEFAULT_COMPONENT_SEARCH_OPTIONS = EnumSet.noneOf(ComponentSearchOptions.class);
    
    public static Stream<Component> flatComponentsAsStream(Component component, EnumSet<ComponentSearchOptions> searchOptionsSet) {
        return Stream.concat(Stream.of(component),
                             ComponentUtilities.extractSubComponents(component, searchOptionsSet)
                                               .flatMap(component1 -> flatComponentsAsStream(component1, searchOptionsSet))
        );
    }
    
    public static <T> @Nullable T findComponent(Component component, Class<? extends T> componentClass, Predicate<? super T> predicate, ComponentSearchOptions... searchOptions) {
        return findComponents(component, componentClass, predicate, searchOptions)
                .findFirst()
                .orElse(null);
    }
    
    public static <T> Stream<T> findComponents(Component component, Class<? extends T> componentClass, Predicate<? super T> predicate, ComponentSearchOptions... searchOptions) {
        EnumSet<ComponentSearchOptions> searchOptionsSet = EnumSet.noneOf(ComponentSearchOptions.class);
        searchOptionsSet.addAll(Arrays.asList(searchOptions));
        return (Stream<T>) flatComponentsAsStream(component, searchOptionsSet)
                .filter(componentClass::isInstance)
                .map(componentClass::cast)
                .filter(predicate);
    }
    
    private static Stream<Component> extractSubComponents(Component component, EnumSet<ComponentSearchOptions> searchOptionsSet) {
        return switch (component) {
            case Window frame -> Stream.concat(
                    searchOptionsSet.contains(ComponentSearchOptions.DO_NOT_SEARCH_OWNED_WINDOWS)
                            ? Stream.empty() : Arrays.stream(frame.getOwnedWindows()),
                    Arrays.stream(frame.getComponents())
            );
            case JMenu menu -> Stream.concat(
                    Arrays.stream(menu.getMenuComponents()),
                    Arrays.stream(menu.getComponents())
            );
            case Container container -> Arrays.stream(container.getComponents());
            case null, default -> Stream.empty();
        };
    }
    
    public static void addMouseListenerFirst(Component component, MouseListener mouseListener) {
        addListenerAtBeggining(mouseListener, component::getMouseListeners, component::removeMouseListener, component::addMouseListener);
    }
    
    private static <T> void addListenerAtBeggining(T newListener, Supplier<T[]> getAllListener, Consumer<T> removeOneListener, Consumer<T> addOneListener) {
        var existing = getAllListener.get();
        for (T t : existing) {
            removeOneListener.accept(t);
        }
        addOneListener.accept(newListener);
        for (T t : existing) {
            addOneListener.accept(t);
        }
    }
    
    public static void removeInputsFor(Component component) {
        component.setEnabled(false);
        while (component.getMouseListeners().length > 0) {
            component.removeMouseListener(component.getMouseListeners()[0]);
        }
        while (component.getFocusListeners().length > 0) {
            component.removeFocusListener(component.getFocusListeners()[0]);
        }
        while (component.getKeyListeners().length > 0) {
            component.removeKeyListener(component.getKeyListeners()[0]);
        }
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
    
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    
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
}
