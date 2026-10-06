package org.openmarkov.gui.bindings;

import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A full on-screen keyboard visualization for Java Swing.
 * <p>
 * Design:
 * - The physical shape of the keyboard (which key sits where, how wide it is,
 * which KeyEvent.VK_ code it corresponds to) is layout-independent: it's the
 * same rectangle of plastic whether you bought it in the US, France or Germany.
 * - Only the LABEL printed on each key changes between QWERTY / AZERTY / QWERTZ.
 * - So we define ONE template of KeySpecs (positions + key codes, QWERTY labels
 * as the default), then apply a small "label override" map per layout to
 * relabel (and in a couple of cases, functionally swap) specific keys.
 * <p>
 * This mirrors how real layout switching works at the OS level: the same
 * physical key continues to report the same position, but produces a
 * different character.
 * <p>
 * Caveat: KeyEvent.VK_ codes are nominally tied to US/ANSI key naming, and
 * exactly which physical key fires which VK_ code for AZERTY/QWERTZ users can
 * vary a bit by OS/JVM. Treat the VK_ codes here as "identifier for a physical
 * position" for highlighting purposes, not a guarantee of the character sent.
 */
public class KeyboardPanel extends JPanel {
    
    // ---- Grid geometry -----------------------------------------------
    // Coordinates are in abstract "half-key" units. A standard key is 2
    // units wide/1 unit tall. UNIT_PX/ROW_PX convert that to pixels.
    private static final int UNIT_PX = 24;
    private static final int ROW_PX = 40;
    private static final int PAD = 3;
    
    public record KeySpec(String defaultLabel, int keyCode, int x, int y, int w, int h) {
        KeySpec(String label, int keyCode, int x, int y, int w) {
            this(label, keyCode, x, y, w, 1);
        }
    }
    
    public enum Layout {QWERTY, AZERTY, QWERTZ}
    
    // Several physical keys share a KeyEvent code (left/right Shift, left/right
    // Ctrl, main Enter/numpad Enter), so key code -> button is one-to-many.
    private final Map<Integer, java.util.List<JButton>> buttonsByKeyCode = new HashMap<>();
    private final java.util.List<KeySpec> template = buildTemplate();
    private final java.util.List<JButton> buttonsInTemplateOrder = new ArrayList<>();
    private Layout currentLayout = Layout.QWERTY;
    
    public Stream<Map.Entry<Integer, List<JButton>>> buttonsByKeyCode() {
        return this.buttonsByKeyCode.entrySet().stream();
    }
    
    public KeyboardPanel() {
        setLayout(null);
        setBackground(new Color(235, 235, 238));
        buildButtons();
        applyLayout(Layout.QWERTY);
        
        
        int maxX = 0, maxY = 0;
        for (KeySpec k : template) {
            maxX = Math.max(maxX, k.x() + k.w());
            maxY = Math.max(maxY, k.y() + k.h());
        }
        setPreferredSize(new Dimension(maxX * UNIT_PX + PAD, maxY * ROW_PX + PAD));
    }
    
    public void setKeyHighlight(int keyCode, boolean pressed) {
        java.util.List<JButton> btns = buttonsByKeyCode.get(keyCode);
        if (btns != null) {
            for (JButton btn : btns) {
                btn.setBackground((pressed ? GUIColors.Bindings.BOUND : GUIColors.Bindings.UNBOUND).getColor());
                btn.getModel().setPressed(pressed);
            }
        }
    }
    
    // ---- Building the buttons -----------------------------------------
    
    private void buildButtons() {
        for (KeySpec k : template) {
            JButton btn = new JButton(k.defaultLabel());
            btn.setFocusable(false);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFont(btn.getFont().deriveFont(11f));
            btn.setBackground(GUIColors.Bindings.UNBOUND.getColor());
            btn.setBounds(
                    k.x() * UNIT_PX + PAD,
                    k.y() * ROW_PX + PAD,
                    k.w() * UNIT_PX - PAD,
                    k.h() * ROW_PX - PAD
            );
            add(btn);
            buttonsInTemplateOrder.add(btn);
            buttonsByKeyCode.computeIfAbsent(k.keyCode(), c -> new ArrayList<>()).add(btn);
        }
    }
    
    /** Swap every key's label to match the requested layout. */
    public void applyLayout(Layout layout) {
        this.currentLayout = layout;
        Map<Integer, String> overrides = switch (layout) {
            case QWERTY -> Map.of();
            case AZERTY -> azertyOverrides();
            case QWERTZ -> qwertzOverrides();
        };
        for (int i = 0; i < template.size(); i++) {
            KeySpec k = template.get(i);
            String label = overrides.getOrDefault(k.keyCode(), k.defaultLabel());
            buttonsInTemplateOrder.get(i).setText(label);
        }
    }
    
    public Layout getCurrentLayout() {
        return currentLayout;
    }
    
    /** Highlight whichever key button is pressed, even without focus on it. */
    private void installGlobalKeyHighlighting() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .addKeyEventDispatcher(e -> {
                                java.util.List<JButton> btns = buttonsByKeyCode.get(e.getKeyCode());
                                if (btns != null) {
                                    boolean pressed = e.getID() == KeyEvent.KEY_PRESSED;
                                    for (JButton btn : btns) {
                                        btn.getModel().setArmed(pressed);
                                        btn.getModel().setPressed(pressed);
                                    }
                                }
                                return false; // never consume; let the event keep propagating
                            });
    }
    
    // ---- The physical template (QWERTY labels, layout-independent shape) ---
    
    private static java.util.List<KeySpec> buildTemplate() {
        java.util.List<KeySpec> keys = new ArrayList<>();
        
        // ---------------- Function row (y = 0) ----------------
        keys.add(new KeySpec("Esc", KeyEvent.VK_ESCAPE, 0, 0, 2));
        keys.add(new KeySpec("F1", KeyEvent.VK_F1, 4, 0, 2));
        keys.add(new KeySpec("F2", KeyEvent.VK_F2, 6, 0, 2));
        keys.add(new KeySpec("F3", KeyEvent.VK_F3, 8, 0, 2));
        keys.add(new KeySpec("F4", KeyEvent.VK_F4, 10, 0, 2));
        keys.add(new KeySpec("F5", KeyEvent.VK_F5, 13, 0, 2));
        keys.add(new KeySpec("F6", KeyEvent.VK_F6, 15, 0, 2));
        keys.add(new KeySpec("F7", KeyEvent.VK_F7, 17, 0, 2));
        keys.add(new KeySpec("F8", KeyEvent.VK_F8, 19, 0, 2));
        keys.add(new KeySpec("F9", KeyEvent.VK_F9, 22, 0, 2));
        keys.add(new KeySpec("F10", KeyEvent.VK_F10, 24, 0, 2));
        keys.add(new KeySpec("F11", KeyEvent.VK_F11, 26, 0, 2));
        keys.add(new KeySpec("F12", KeyEvent.VK_F12, 28, 0, 2));
        keys.add(new KeySpec("PrtSc", KeyEvent.VK_PRINTSCREEN, 32, 0, 2));
        keys.add(new KeySpec("ScrLk", KeyEvent.VK_SCROLL_LOCK, 34, 0, 2));
        keys.add(new KeySpec("Pause", KeyEvent.VK_PAUSE, 36, 0, 2));
        
        // ---------------- Number row (y = 2) ----------------
        keys.add(new KeySpec("`", KeyEvent.VK_BACK_QUOTE, 0, 2, 2));
        keys.add(new KeySpec("1", KeyEvent.VK_1, 2, 2, 2));
        keys.add(new KeySpec("2", KeyEvent.VK_2, 4, 2, 2));
        keys.add(new KeySpec("3", KeyEvent.VK_3, 6, 2, 2));
        keys.add(new KeySpec("4", KeyEvent.VK_4, 8, 2, 2));
        keys.add(new KeySpec("5", KeyEvent.VK_5, 10, 2, 2));
        keys.add(new KeySpec("6", KeyEvent.VK_6, 12, 2, 2));
        keys.add(new KeySpec("7", KeyEvent.VK_7, 14, 2, 2));
        keys.add(new KeySpec("8", KeyEvent.VK_8, 16, 2, 2));
        keys.add(new KeySpec("9", KeyEvent.VK_9, 18, 2, 2));
        keys.add(new KeySpec("0", KeyEvent.VK_0, 20, 2, 2));
        keys.add(new KeySpec("-", KeyEvent.VK_MINUS, 22, 2, 2));
        keys.add(new KeySpec("=", KeyEvent.VK_EQUALS, 24, 2, 2));
        keys.add(new KeySpec("Backspace", KeyEvent.VK_BACK_SPACE, 26, 2, 4));
        
        // ---------------- QWERTY row (y = 3) ----------------
        keys.add(new KeySpec("Tab", KeyEvent.VK_TAB, 0, 3, 3));
        keys.add(new KeySpec("Q", KeyEvent.VK_Q, 3, 3, 2));
        keys.add(new KeySpec("W", KeyEvent.VK_W, 5, 3, 2));
        keys.add(new KeySpec("E", KeyEvent.VK_E, 7, 3, 2));
        keys.add(new KeySpec("R", KeyEvent.VK_R, 9, 3, 2));
        keys.add(new KeySpec("T", KeyEvent.VK_T, 11, 3, 2));
        keys.add(new KeySpec("Y", KeyEvent.VK_Y, 13, 3, 2));
        keys.add(new KeySpec("U", KeyEvent.VK_U, 15, 3, 2));
        keys.add(new KeySpec("I", KeyEvent.VK_I, 17, 3, 2));
        keys.add(new KeySpec("O", KeyEvent.VK_O, 19, 3, 2));
        keys.add(new KeySpec("P", KeyEvent.VK_P, 21, 3, 2));
        keys.add(new KeySpec("[", KeyEvent.VK_OPEN_BRACKET, 23, 3, 2));
        keys.add(new KeySpec("]", KeyEvent.VK_CLOSE_BRACKET, 25, 3, 2));
        keys.add(new KeySpec("\\", KeyEvent.VK_BACK_SLASH, 27, 3, 3));
        
        // ---------------- ASDF row (y = 4) ----------------
        keys.add(new KeySpec("Caps Lock", KeyEvent.VK_CAPS_LOCK, 0, 4, 4));
        keys.add(new KeySpec("A", KeyEvent.VK_A, 4, 4, 2));
        keys.add(new KeySpec("S", KeyEvent.VK_S, 6, 4, 2));
        keys.add(new KeySpec("D", KeyEvent.VK_D, 8, 4, 2));
        keys.add(new KeySpec("F", KeyEvent.VK_F, 10, 4, 2));
        keys.add(new KeySpec("G", KeyEvent.VK_G, 12, 4, 2));
        keys.add(new KeySpec("H", KeyEvent.VK_H, 14, 4, 2));
        keys.add(new KeySpec("J", KeyEvent.VK_J, 16, 4, 2));
        keys.add(new KeySpec("K", KeyEvent.VK_K, 18, 4, 2));
        keys.add(new KeySpec("L", KeyEvent.VK_L, 20, 4, 2));
        keys.add(new KeySpec(";", KeyEvent.VK_SEMICOLON, 22, 4, 2));
        keys.add(new KeySpec("'", KeyEvent.VK_QUOTE, 24, 4, 2));
        keys.add(new KeySpec("Enter", KeyEvent.VK_ENTER, 26, 4, 4));
        
        // ---------------- ZXCV row (y = 5) ----------------
        keys.add(new KeySpec("Shift", KeyEvent.VK_SHIFT, 0, 5, 5));
        keys.add(new KeySpec("Z", KeyEvent.VK_Z, 5, 5, 2));
        keys.add(new KeySpec("X", KeyEvent.VK_X, 7, 5, 2));
        keys.add(new KeySpec("C", KeyEvent.VK_C, 9, 5, 2));
        keys.add(new KeySpec("V", KeyEvent.VK_V, 11, 5, 2));
        keys.add(new KeySpec("B", KeyEvent.VK_B, 13, 5, 2));
        keys.add(new KeySpec("N", KeyEvent.VK_N, 15, 5, 2));
        keys.add(new KeySpec("M", KeyEvent.VK_M, 17, 5, 2));
        keys.add(new KeySpec(",", KeyEvent.VK_COMMA, 19, 5, 2));
        keys.add(new KeySpec(".", KeyEvent.VK_PERIOD, 21, 5, 2));
        keys.add(new KeySpec("/", KeyEvent.VK_SLASH, 23, 5, 2));
        keys.add(new KeySpec("Shift", KeyEvent.VK_SHIFT, 25, 5, 5));
        
        // ---------------- Bottom / modifier row (y = 6) ----------------
        keys.add(new KeySpec("Ctrl", KeyEvent.VK_CONTROL, 0, 6, 3));
        keys.add(new KeySpec("Win", KeyEvent.VK_WINDOWS, 3, 6, 2));
        keys.add(new KeySpec("Alt", KeyEvent.VK_ALT, 5, 6, 3));
        keys.add(new KeySpec("Space", KeyEvent.VK_SPACE, 8, 6, 14));
        keys.add(new KeySpec("Alt Gr", KeyEvent.VK_ALT_GRAPH, 22, 6, 3));
        keys.add(new KeySpec("Ctrl", KeyEvent.VK_CONTROL, 25, 6, 5));
        
        // ---------------- Navigation cluster (aligned under PrtSc/ScrLk/Pause) ----
        keys.add(new KeySpec("Ins", KeyEvent.VK_INSERT, 32, 2, 2));
        keys.add(new KeySpec("Home", KeyEvent.VK_HOME, 34, 2, 2));
        keys.add(new KeySpec("Pg Up", KeyEvent.VK_PAGE_UP, 36, 2, 2));
        keys.add(new KeySpec("Del", KeyEvent.VK_DELETE, 32, 3, 2));
        keys.add(new KeySpec("End", KeyEvent.VK_END, 34, 3, 2));
        keys.add(new KeySpec("Pg Dn", KeyEvent.VK_PAGE_DOWN, 36, 3, 2));
        keys.add(new KeySpec("\u2191", KeyEvent.VK_UP, 34, 5, 2));
        keys.add(new KeySpec("\u2190", KeyEvent.VK_LEFT, 32, 6, 2));
        keys.add(new KeySpec("\u2193", KeyEvent.VK_DOWN, 34, 6, 2));
        keys.add(new KeySpec("\u2192", KeyEvent.VK_RIGHT, 36, 6, 2));
        
        // ---------------- Numpad ----------------
        keys.add(new KeySpec("Num Lock", KeyEvent.VK_NUM_LOCK, 40, 2, 2));
        keys.add(new KeySpec("/", KeyEvent.VK_DIVIDE, 42, 2, 2));
        keys.add(new KeySpec("*", KeyEvent.VK_MULTIPLY, 44, 2, 2));
        keys.add(new KeySpec("-", KeyEvent.VK_SUBTRACT, 46, 2, 2));
        keys.add(new KeySpec("7", KeyEvent.VK_NUMPAD7, 40, 3, 2));
        keys.add(new KeySpec("8", KeyEvent.VK_NUMPAD8, 42, 3, 2));
        keys.add(new KeySpec("9", KeyEvent.VK_NUMPAD9, 44, 3, 2));
        keys.add(new KeySpec("+", KeyEvent.VK_ADD, 46, 3, 2, 2));
        keys.add(new KeySpec("4", KeyEvent.VK_NUMPAD4, 40, 4, 2));
        keys.add(new KeySpec("5", KeyEvent.VK_NUMPAD5, 42, 4, 2));
        keys.add(new KeySpec("6", KeyEvent.VK_NUMPAD6, 44, 4, 2));
        keys.add(new KeySpec("1", KeyEvent.VK_NUMPAD1, 40, 5, 2));
        keys.add(new KeySpec("2", KeyEvent.VK_NUMPAD2, 42, 5, 2));
        keys.add(new KeySpec("3", KeyEvent.VK_NUMPAD3, 44, 5, 2));
        keys.add(new KeySpec("Enter", KeyEvent.VK_ENTER, 46, 5, 2, 2));
        keys.add(new KeySpec("0", KeyEvent.VK_NUMPAD0, 40, 6, 4));
        keys.add(new KeySpec(".", KeyEvent.VK_DECIMAL, 44, 6, 2));
        
        return keys;
    }
    
    // ---- Per-layout label overrides ------------------------------------
    // Only the keys that actually differ from QWERTY need an entry.
    private static Map<Integer, String> azertyOverrides() {
        Map<Integer, String> m = new HashMap<>();
        // number row -> AZERTY symbols (unshifted)
        m.put(KeyEvent.VK_BACK_QUOTE, "\u00B2");
        m.put(KeyEvent.VK_1, "&");
        m.put(KeyEvent.VK_2, "\u00E9");
        m.put(KeyEvent.VK_3, "\"");
        m.put(KeyEvent.VK_4, "'");
        m.put(KeyEvent.VK_5, "(");
        m.put(KeyEvent.VK_6, "-");
        m.put(KeyEvent.VK_7, "\u00E8");
        m.put(KeyEvent.VK_8, "_");
        m.put(KeyEvent.VK_9, "\u00E7");
        m.put(KeyEvent.VK_0, "\u00E0");
        m.put(KeyEvent.VK_MINUS, ")");
        m.put(KeyEvent.VK_EQUALS, "=");
        // top letter row: Q<->A, W<->Z
        m.put(KeyEvent.VK_Q, "A");
        m.put(KeyEvent.VK_W, "Z");
        m.put(KeyEvent.VK_OPEN_BRACKET, "^");
        m.put(KeyEvent.VK_CLOSE_BRACKET, "$");
        m.put(KeyEvent.VK_BACK_SLASH, "*");
        // home row: A->Q, L/;/' shift down to M/ù
        m.put(KeyEvent.VK_A, "Q");
        m.put(KeyEvent.VK_SEMICOLON, "M");
        m.put(KeyEvent.VK_QUOTE, "\u00D9");
        // bottom row: Z->W, M/,/. /  shift to ,/;/:/!
        m.put(KeyEvent.VK_Z, "W");
        m.put(KeyEvent.VK_M, ",");
        m.put(KeyEvent.VK_COMMA, ";");
        m.put(KeyEvent.VK_PERIOD, ":");
        m.put(KeyEvent.VK_SLASH, "!");
        return m;
    }
    
    private static Map<Integer, String> qwertzOverrides() {
        Map<Integer, String> m = new HashMap<>();
        m.put(KeyEvent.VK_BACK_QUOTE, "^");
        m.put(KeyEvent.VK_MINUS, "\u00DF");
        m.put(KeyEvent.VK_EQUALS, "\u00B4");
        // Y and Z swap
        m.put(KeyEvent.VK_Y, "Z");
        m.put(KeyEvent.VK_Z, "Y");
        m.put(KeyEvent.VK_OPEN_BRACKET, "\u00DC");
        m.put(KeyEvent.VK_CLOSE_BRACKET, "*");
        m.put(KeyEvent.VK_SEMICOLON, "\u00D6");
        m.put(KeyEvent.VK_QUOTE, "\u00C4");
        m.put(KeyEvent.VK_SLASH, "-");
        return m;
    }
    
}