package org.openmarkov.gui.bindings;

import org.openmarkov.gui.inputTracing.InputTracer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputCombination {
    
    public InputCombination(Collection<? extends Input> inputs) {
        this.inputs = Set.copyOf(inputs);
    }
    
    public InputCombination(Input... inputs) {
        this.inputs = Set.of(inputs);
    }
    
    private Set<? extends Input> inputs;
    
    @Override public boolean equals(Object obj) {
        return obj instanceof InputCombination other && other.inputs.equals(inputs);
    }
    
    public Stream<? extends Input> inputs() {
        return this.inputs.stream();
    }
    
    public boolean isMet(Binding.MeetCondition metCondition, Binding.Exclusivity exclusivity, boolean directionIsForward) {
        InputTracer.InputStack previousInput = InputTracer.INSTANCE.previousInput;
        InputTracer.InputStack currentInput = InputTracer.INSTANCE.currentInput;
        final boolean wasMet = this.isMetByInputStack(directionIsForward ? previousInput : currentInput, exclusivity);
        final boolean isMet = this.isMetByInputStack(directionIsForward ? currentInput : previousInput, exclusivity);
        return switch (metCondition) {
            case ON_GAIN -> !wasMet && isMet;
            case ON_LOSE -> wasMet && !isMet;
            case HELD -> isMet;
            case NOT_PRESENT -> !isMet;
        };
    }
    
    private boolean isMetByInputStack(InputTracer.InputStack stack, Binding.Exclusivity exclusivity) {
        return switch (exclusivity) {
            case INCLUSIVE -> this.inputs.stream().allMatch(input -> switch (input) {
                case Input.Click click ->
                        stack.heldClicks.containsKey(click.mouseButton()) && stack.heldClicks.get(click.mouseButton()) == click.times();
                case Input.Key key -> stack.heldKeys.contains(key.key());
                case Input.MouseWheel mouseWheel -> switch (stack.wheel) {
                    case UP -> mouseWheel.up();
                    case DOWN -> !mouseWheel.up();
                    case NONE -> false;
                };
            });
            case EXCLUSIVE -> {
                Map<Integer, Integer> remainingClicks = new HashMap<>(stack.heldClicks);
                Collection<Integer> remainingKeys = new HashSet<>(stack.heldKeys);
                InputTracer.InputStack.MouseWheel remainingWheel = stack.wheel;
                for (Input input : this.inputs) {
                    var isMetLocalInput = switch (input) {
                        case Input.Click click ->
                                remainingClicks.containsKey(click.mouseButton()) && remainingClicks.remove(click.mouseButton()) == click.times();
                        case Input.Key key -> remainingKeys.remove(key.key());
                        case Input.MouseWheel mouseWheel -> {
                            InputTracer.InputStack.MouseWheel wheel = remainingWheel;
                            remainingWheel = InputTracer.InputStack.MouseWheel.NONE;
                            yield switch (wheel) {
                                case UP -> mouseWheel.up();
                                case DOWN -> !mouseWheel.up();
                                case NONE -> false;
                            };
                        }
                    };
                    if (!isMetLocalInput) {
                        yield false;
                    }
                }
                if (!remainingClicks.isEmpty()) {
                    yield false;
                }
                if (!remainingKeys.isEmpty()) {
                    yield false;
                }
                if (remainingWheel != InputTracer.InputStack.MouseWheel.NONE) {
                    yield false;
                }
                yield true;
            }
        };
        
        
    }
    
    @Override public String toString() {
        if (this.inputs.isEmpty()) {
            return "Unassigned";
        }
        return this.inputs().sorted().map(Object::toString).collect(Collectors.joining(" + "));
    }
}
