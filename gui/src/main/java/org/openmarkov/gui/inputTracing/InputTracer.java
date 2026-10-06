package org.openmarkov.gui.inputTracing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.bindings.Input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InputTracer {
    
    public static final InputTracer INSTANCE = new InputTracer();
    
    public @NotNull InputStack currentInput;
    public @NotNull InputStack previousInput;
    private @NotNull InputChange currentInputChange;
    private @Nullable InputEvent changeCausingEvent;
    
    private InputTracer() {
        this.currentInput = new InputStack(Collections.emptySet(), Collections.emptyMap(), InputStack.MouseWheel.NONE);
        this.previousInput = new InputStack(Collections.emptySet(), Collections.emptyMap(), InputStack.MouseWheel.NONE);
        this.currentInputChange = InputChange.REMAINS;
    }
    
    enum InputChange {
        GAINDED_INPUT,
        LOST_INPUT,
        REMAINS
    }
    
    @Override public String toString() {
        return "InputTracer{" +
                "currentInputChange=" + currentInputChange +
                ", currentInput=" + currentInput +
                ", previousInput=" + previousInput +
                ", changeCausingEvent=" + changeCausingEvent +
                '}';
    }
    
    public class InputStack {
        public final Set<Integer> heldKeys;
        public final Map<Integer, Integer> heldClicks;
        public final MouseWheel wheel;
        
        public enum MouseWheel {
            UP, DOWN, NONE
        }
        
        private InputStack(Set<Integer> heldKeys, Map<Integer, Integer> heldClicks, MouseWheel wheel) {
            this.heldKeys = heldKeys;
            this.heldClicks = heldClicks;
            this.wheel = wheel;
        }
        
        @Override public String toString() {
            String keys = this.heldKeys.stream()
                                       .mapToInt(code -> code)
                                       .mapToObj(KeyEvent::getKeyText)
                                       .collect(Collectors.joining(" "));
            
            StringBuilder buttons = new StringBuilder();
            for (var entry : this.heldClicks.entrySet()) {
                buttons.append(Input.buttonName(entry.getKey()))
                       .append(" - ")
                       .append(entry.getValue())
                       .append(" clicks ");
            }
            
            return "Held Keys: [" +
                    (!keys.isEmpty() ? keys.trim() : "None") +
                    "] | Held Mouse: [" + (!buttons.isEmpty() ? buttons.toString().trim() : "None") +
                    "] | Wheel: [" + this.wheel + "]";
        }
    }
    
    
    void update(InputEvent event) {
        this.previousInput = this.currentInput;
        
        Map<Integer, Integer> heldClicks = new HashMap<>();
        for (var heldMouseButton : GlobalInputTracker.HELD_MOUSE_BUTTONS) {
            heldClicks.put(heldMouseButton, GlobalInputTracker.MOUSE_CLICK_COUNTS.get(heldMouseButton));
        }
        var heldKeys = new HashSet<>(GlobalInputTracker.HELD_KEY_CODES);
        var wheel = switch (GlobalInputTracker.currentMouseWheelState) {
            case UP -> InputStack.MouseWheel.UP;
            case DOWN -> InputStack.MouseWheel.DOWN;
            case NONE -> InputStack.MouseWheel.NONE;
        };
        
        this.currentInput = new InputStack(heldKeys, heldClicks, wheel);
        this.changeCausingEvent = event;
        updateChangeState();
    }
    
    private void updateChangeState() {
        this.currentInputChange = InputChange.REMAINS;
        if (this.previousInput.wheel != this.currentInput.wheel) {
            if (this.previousInput.wheel == InputStack.MouseWheel.NONE) {
                this.currentInputChange = InputChange.GAINDED_INPUT;
            } else {
                this.currentInputChange = InputChange.LOST_INPUT;
            }
            return;
        }
        if (this.previousInput.heldKeys.size() != this.currentInput.heldKeys.size()) {
            if (this.previousInput.heldKeys.size() < this.currentInput.heldKeys.size()) {
                this.currentInputChange = InputChange.GAINDED_INPUT;
            } else {
                this.currentInputChange = InputChange.LOST_INPUT;
            }
            return;
        }
        if (this.previousInput.heldClicks.size() != this.currentInput.heldClicks.size()) {
            if (this.previousInput.heldClicks.size() < this.currentInput.heldClicks.size()) {
                this.currentInputChange = InputChange.GAINDED_INPUT;
            } else {
                this.currentInputChange = InputChange.LOST_INPUT;
            }
            return;
        }
    }
    
}
