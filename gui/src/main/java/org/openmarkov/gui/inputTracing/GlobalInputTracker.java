package org.openmarkov.gui.inputTracing;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlobalInputTracker {
    
    public enum MouseWheel {
        UP, DOWN, NONE
    }
    
    // Timeout (ms) to reset wheel state to NONE if no further scroll events arrive
    private static final int WHEEL_RELEASE_DELAY_MS = 150;
    
    // Active tracking collections
    static final Set<Integer> HELD_KEY_CODES = Collections.synchronizedSet(new HashSet<>());
    static final Set<Integer> HELD_MOUSE_BUTTONS = Collections.synchronizedSet(new HashSet<>());
    static final Map<Integer, Integer> MOUSE_CLICK_COUNTS = Collections.synchronizedMap(new HashMap<>());
    static MouseWheel currentMouseWheelState = MouseWheel.NONE;
    
    // Timer to reset wheel state to NONE
    private static Timer wheelReleaseTimer;
    
    static {
        // Initialize timer on EDT
        SwingUtilities.invokeLater(() -> {
            wheelReleaseTimer = new Timer(WHEEL_RELEASE_DELAY_MS, e -> resetWheelState());
            wheelReleaseTimer.setRepeats(false);
        });
        
        // 1. Global Key Tracking (KeyEventDispatcher)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                HELD_KEY_CODES.add(e.getKeyCode());
                updateState("Global listener - Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode()), e);
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                HELD_KEY_CODES.remove(e.getKeyCode());
                updateState("Global listener - Key Released: " + KeyEvent.getKeyText(e.getKeyCode()), e);
            }
            return false;
        });
        
        // 2. Global Mouse & Wheel Tracking (AWTEventListener)
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseWheelEvent wheelEvent) {
                int rotation = wheelEvent.getWheelRotation();
                if (rotation < 0) {
                    currentMouseWheelState = MouseWheel.UP;
                    if (wheelReleaseTimer != null) {
                        wheelReleaseTimer.restart();
                    }
                    updateState("Global listener - Wheel UP", wheelEvent);
                } else if (rotation > 0) {
                    currentMouseWheelState = MouseWheel.DOWN;
                    if (wheelReleaseTimer != null) {
                        wheelReleaseTimer.restart();
                    }
                    updateState("Global listener - Wheel DOWN", wheelEvent);
                }
            } else if (event instanceof MouseEvent mouseEvent) {
                int button = mouseEvent.getButton();
                
                switch (mouseEvent.getID()) {
                    case MouseEvent.MOUSE_PRESSED -> {
                        if (button != MouseEvent.NOBUTTON) {
                            HELD_MOUSE_BUTTONS.add(button);
                            MOUSE_CLICK_COUNTS.put(button, mouseEvent.getClickCount());
                            updateState("Global listener - Mouse Pressed: " + getButtonName(button), mouseEvent);
                        }
                    }
                    case MouseEvent.MOUSE_RELEASED -> {
                        if (button != MouseEvent.NOBUTTON) {
                            HELD_MOUSE_BUTTONS.remove(button);
                            updateState("Global listener - Mouse Released: " + getButtonName(button), mouseEvent);
                        }
                    }
                    case MouseEvent.MOUSE_CLICKED -> {
                        if (button != MouseEvent.NOBUTTON) {
                            MOUSE_CLICK_COUNTS.put(button, mouseEvent.getClickCount());
                            updateState("Global listener - Mouse Clicked: " + getButtonName(button), mouseEvent);
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        
        // 3. Focus loss cleanup
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", evt -> {
            if (evt.getNewValue() == null) {
                HELD_KEY_CODES.clear();
                HELD_MOUSE_BUTTONS.clear();
                currentMouseWheelState = MouseWheel.NONE;
                if (wheelReleaseTimer != null) wheelReleaseTimer.stop();
                updateState("Global listener - Focus Lost (Cleared)", null);
            }
        });
    }
    
    private static void resetWheelState() {
        currentMouseWheelState = MouseWheel.NONE;
        updateState("Global listener - Wheel Auto-Reset (NONE)", null);
    }
    
    public static Set<Integer> getHeldKeyCodes() {
        return new HashSet<>(HELD_KEY_CODES);
    }
    
    public static Set<Integer> getHeldMouseButtons() {
        return new HashSet<>(HELD_MOUSE_BUTTONS);
    }
    
    public static Map<Integer, Integer> getMouseClickCounts() {
        return new HashMap<>(MOUSE_CLICK_COUNTS);
    }
    
    public static MouseWheel getMouseWheelState() {
        return currentMouseWheelState;
    }
    
    private static void updateState(String sourceTag, InputEvent event) {
        if (sourceTag.startsWith("Component listener")) {
            return;
        }
        StringBuilder keys = new StringBuilder();
        for (int code : getHeldKeyCodes()) {
            keys.append(KeyEvent.getKeyText(code)).append(" ");
        }
        
        StringBuilder buttons = new StringBuilder();
        for (int btn : getHeldMouseButtons()) {
            buttons.append(getButtonName(btn)).append(" ");
        }
//        System.out.println("[" + sourceTag + "] -> Held Keys: [" +
//                                   (keys.length() > 0 ? keys.toString().trim() : "None") +
//                                   "] | Held Mouse: [" + (buttons.length() > 0 ? buttons.toString().trim() : "None") +
//                                   "] | Wheel: [" + getMouseWheelState() +
//                                   "] | Click Counts: " + getMouseClickCounts());
        
        InputTracer.INSTANCE.update(event);
//        System.out.println(InputTracer.INSTANCE);
    }
    
    // --- Demo Application ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Global vs Component Listener State Test");
            frame.setSize(650, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            
            JPanel testPanel = new JPanel();
            testPanel.setBackground(new Color(230, 240, 250));
            testPanel.setFocusable(true);
            
            JLabel infoLabel = new JLabel("Click here to focus, then press keys, click mouse, or scroll wheel");
            testPanel.add(infoLabel);
            
            // --- COMPONENT LISTENERS ---
            
            testPanel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    updateState("Component listener - Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode()), e);
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    updateState("Component listener - Key Released: " + KeyEvent.getKeyText(e.getKeyCode()), e);
                }
            });
            
            testPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    testPanel.requestFocusInWindow();
                    updateState("Component listener - Mouse Pressed: " + getButtonName(e.getButton()), e);
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    updateState("Component listener - Mouse Released: " + getButtonName(e.getButton()), e);
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    updateState("Component listener - Mouse Clicked: " + getButtonName(e.getButton()), e);
                }
            });
            
            testPanel.addMouseWheelListener(e -> {
                updateState("Component listener - Wheel Scrolled: " + (e.getWheelRotation() < 0 ? "UP" : "DOWN"), e);
            });
            
            JButton openDialogBtn = new JButton("Open Modal Dialog");
            openDialogBtn.addActionListener(e -> {
                JDialog dialog = new JDialog(frame, "Modal Dialog", true);
                dialog.setSize(300, 200);
                dialog.setLayout(new FlowLayout());
                dialog.add(new JLabel("Global tracker remains active here!"));
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            });
            
            JPanel topPanel = new JPanel();
            topPanel.add(openDialogBtn);
            
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(testPanel, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            testPanel.requestFocusInWindow();
        });
    }
    
    private static String getButtonName(int button) {
        return switch (button) {
            case MouseEvent.BUTTON1 -> "Left(B1)";
            case MouseEvent.BUTTON2 -> "Middle(B2)";
            case MouseEvent.BUTTON3 -> "Right(B3)";
            default -> "Button" + button;
        };
    }
}