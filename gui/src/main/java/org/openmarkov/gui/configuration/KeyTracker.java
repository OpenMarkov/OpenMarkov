package org.openmarkov.gui.configuration;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class KeyTracker {
    
    private static final Set<Integer> HELD_KEYS = new LinkedHashSet<>();
    
    static {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!(event instanceof KeyEvent keyEvent)) {
                return;
            }
            switch (event.getID()) {
                case KeyEvent.KEY_RELEASED, KeyEvent.KEY_TYPED -> KeyTracker.HELD_KEYS.remove(keyEvent.getKeyCode());
                case KeyEvent.KEY_PRESSED -> KeyTracker.HELD_KEYS.add(keyEvent.getKeyCode());
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }
    
    public static boolean isHeld(int key, int... otherKeys) {
        return KeyTracker.HELD_KEYS.contains(key) && (otherKeys.length==0 || Arrays.stream(otherKeys).allMatch(KeyTracker.HELD_KEYS::contains));
    };
    
    public static IntStream getHeldKeys() {
        return HELD_KEYS.stream().mapToInt(key -> key);
    }
    
}
