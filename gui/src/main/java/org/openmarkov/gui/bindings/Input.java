package org.openmarkov.gui.bindings;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;

public sealed interface Input extends Serializable, Comparable<Input> {
    
    record Key(int key) implements Input {
        @Override public @NotNull String toString() {
            return KeyEvent.getKeyText(this.key);
        }
        
        @Override public boolean equals(Object obj) {
            return obj instanceof Key(int otherKey) && this.key == otherKey;
        }
    }
    
    record Click(int mouseButton, int times) implements Input {
        @Override public @NotNull String toString() {
            boolean isPlural = this.times != 1;
            
            return (isPlural ? this.times + " " : "") + buttonName(this.mouseButton) + (isPlural ? "s" : "");
        }
        
        @Override public boolean equals(Object obj) {
            return obj instanceof Click(int otherButton, int otherTimes)
                    && this.mouseButton == otherButton && this.times == otherTimes;
        }
    }
    
    record MouseWheel(boolean up) implements Input {
        @Override public @NotNull String toString() {
            return "Mouse wheel " + (this.up ? "up" : "down");
        }
        
        @Override public boolean equals(Object obj) {
            return obj instanceof MouseWheel(boolean upOther) && this.up == upOther;
        }
    }
    
    @Override default int compareTo(@NotNull Input other) {
        return switch (Tuples.record(this, other)) {
            case Tuple2Record(Key thisKey, Key otherKey) -> Integer.compare(thisKey.key, otherKey.key);
            case Tuple2Record(Click thisClick, Click otherClick) ->
                    Integer.compare(thisClick.mouseButton, otherClick.mouseButton);
            case Tuple2Record(MouseWheel thisMouse, MouseWheel otherMouse) ->
                    Boolean.compare(thisMouse.up, otherMouse.up);
            
            case Tuple2Record(Key _, Click _) -> -1;
            case Tuple2Record(Key _, MouseWheel _) -> -1;
            case Tuple2Record(Click _, Key _) -> 1;
            case Tuple2Record(MouseWheel _, Key _) -> 1;
            
            case Tuple2Record(Click _, MouseWheel _) -> -1;
            case Tuple2Record(MouseWheel _, Click _) -> 1;
        };
    }
    
    static String buttonName(int button) {
        return switch (button) {
            case MouseEvent.BUTTON1 -> "Left click";
            case MouseEvent.BUTTON2 -> "Middle click";
            case MouseEvent.BUTTON3 -> "Right click";
            case 4 -> "First side button click";
            case 5 -> "Second side button click";
            default -> "Button" + button;
        };
    }
}
