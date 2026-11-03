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
            GlobalInputTracker.wheelReleaseTimer = new Timer(GlobalInputTracker.WHEEL_RELEASE_DELAY_MS, e -> GlobalInputTracker.resetWheelState());
            GlobalInputTracker.wheelReleaseTimer.setRepeats(false);
        });
        
        // 1. Global Key Tracking (KeyEventDispatcher)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                GlobalInputTracker.HELD_KEY_CODES.add(e.getKeyCode());
                GlobalInputTracker.updateState("Global listener - Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode()), e);
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                GlobalInputTracker.HELD_KEY_CODES.remove(e.getKeyCode());
                GlobalInputTracker.updateState("Global listener - Key Released: " + KeyEvent.getKeyText(e.getKeyCode()), e);
            }
            return false;
        });
        
        // 2. Global Mouse & Wheel Tracking (AWTEventListener)
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseWheelEvent wheelEvent) {
                int rotation = wheelEvent.getWheelRotation();
                if (rotation < 0) {
                    GlobalInputTracker.currentMouseWheelState = MouseWheel.UP;
                    if (GlobalInputTracker.wheelReleaseTimer != null) {
                        GlobalInputTracker.wheelReleaseTimer.restart();
                    }
                    GlobalInputTracker.updateState("Global listener - Wheel UP", wheelEvent);
                } else if (rotation > 0) {
                    GlobalInputTracker.currentMouseWheelState = MouseWheel.DOWN;
                    if (GlobalInputTracker.wheelReleaseTimer != null) {
                        GlobalInputTracker.wheelReleaseTimer.restart();
                    }
                    GlobalInputTracker.updateState("Global listener - Wheel DOWN", wheelEvent);
                }
            } else if (event instanceof MouseEvent mouseEvent) {
                int button = mouseEvent.getButton();
                switch (mouseEvent.getID()) {
                    case MouseEvent.MOUSE_PRESSED -> {
                        if (button != MouseEvent.NOBUTTON) {
                            GlobalInputTracker.HELD_MOUSE_BUTTONS.add(button);
                            GlobalInputTracker.MOUSE_CLICK_COUNTS.put(button, mouseEvent.getClickCount());
                            GlobalInputTracker.updateState("Global listener - Mouse Pressed: " + GlobalInputTracker.getButtonName(button), mouseEvent);
                        }
                    }
                    case MouseEvent.MOUSE_RELEASED -> {
                        if (button != MouseEvent.NOBUTTON) {
                            GlobalInputTracker.HELD_MOUSE_BUTTONS.remove(button);
                            GlobalInputTracker.updateState("Global listener - Mouse Released: " + GlobalInputTracker.getButtonName(button), mouseEvent);
                        }
                    }
                    case MouseEvent.MOUSE_MOVED -> {
                        GlobalInputTracker.updateState("Global listener - Mouse Moved: " + GlobalInputTracker.getButtonName(button), mouseEvent);
                    }
                    case MouseEvent.MOUSE_DRAGGED -> {
                        GlobalInputTracker.updateState("Global listener - Mouse Dragged: " + GlobalInputTracker.getButtonName(button), mouseEvent);
                    }
                    case MouseEvent.MOUSE_CLICKED -> {
                        if (button != MouseEvent.NOBUTTON) {
                            GlobalInputTracker.MOUSE_CLICK_COUNTS.put(button, mouseEvent.getClickCount());
                            GlobalInputTracker.updateState("Global listener - Mouse Clicked: " + GlobalInputTracker.getButtonName(button), mouseEvent);
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        
        // 3. Focus loss cleanup
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", evt -> {
            if (evt.getNewValue() == null) {
                GlobalInputTracker.HELD_KEY_CODES.clear();
                GlobalInputTracker.HELD_MOUSE_BUTTONS.clear();
                GlobalInputTracker.currentMouseWheelState = MouseWheel.NONE;
                if (GlobalInputTracker.wheelReleaseTimer != null) GlobalInputTracker.wheelReleaseTimer.stop();
                GlobalInputTracker.updateState("Global listener - Focus Lost (Cleared)", null);
            }
        });
    }
    
    private static void resetWheelState() {
        GlobalInputTracker.currentMouseWheelState = MouseWheel.NONE;
        GlobalInputTracker.updateState("Global listener - Wheel Auto-Reset (NONE)", null);
    }
    
    public static Set<Integer> getHeldKeyCodes() {
        return new HashSet<>(GlobalInputTracker.HELD_KEY_CODES);
    }
    
    public static Set<Integer> getHeldMouseButtons() {
        return new HashSet<>(GlobalInputTracker.HELD_MOUSE_BUTTONS);
    }
    
    public static Map<Integer, Integer> getMouseClickCounts() {
        return new HashMap<>(GlobalInputTracker.MOUSE_CLICK_COUNTS);
    }
    
    public static MouseWheel getMouseWheelState() {
        return GlobalInputTracker.currentMouseWheelState;
    }
    
    private static void updateState(String sourceTag, InputEvent event) {
        if (sourceTag.startsWith("Component listener")) {
            return;
        }
        StringBuilder keys = new StringBuilder();
        for (int code : GlobalInputTracker.getHeldKeyCodes()) {
            keys.append(KeyEvent.getKeyText(code)).append(" ");
        }
        
        StringBuilder buttons = new StringBuilder();
        for (int btn : GlobalInputTracker.getHeldMouseButtons()) {
            buttons.append(GlobalInputTracker.getButtonName(btn)).append(" ");
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
                    GlobalInputTracker.updateState("Component listener - Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode()), e);
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    GlobalInputTracker.updateState("Component listener - Key Released: " + KeyEvent.getKeyText(e.getKeyCode()), e);
                }
            });
            
            testPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    testPanel.requestFocusInWindow();
                    GlobalInputTracker.updateState("Component listener - Mouse Pressed: " + GlobalInputTracker.getButtonName(e.getButton()), e);
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    GlobalInputTracker.updateState("Component listener - Mouse Released: " + GlobalInputTracker.getButtonName(e.getButton()), e);
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    GlobalInputTracker.updateState("Component listener - Mouse Clicked: " + GlobalInputTracker.getButtonName(e.getButton()), e);
                }
            });
            
            testPanel.addMouseWheelListener(e -> {
                GlobalInputTracker.updateState("Component listener - Wheel Scrolled: " + (e.getWheelRotation() < 0 ? "UP" : "DOWN"), e);
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