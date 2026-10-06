package org.openmarkov.gui.bindings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class BindingCommandGroup<TInputEvent extends InputEvent, BindGroup extends BindingCommandGroup.BindingGroup> {
    
    public sealed interface BindingGroup permits Binding, BindingGroup.BindingList {
        public final class BindingList extends ArrayList<Binding> implements BindingGroup {
            public BindingList(Binding... bindings) {
                this.addAll(List.of(bindings));
            }
        }
        
        public default String name() {
            return switch (this) {
                case Binding binding -> binding.locator;
                case BindingList bindingList -> "[" + bindingList.stream()
                                                                 .map(binding -> binding.locator)
                                                                 .collect(Collectors.joining(", ")) + "]";
            };
        }
    }
    
    
    public enum OnEvent {
        KEY_PRESSED,
        KEY_RELEASED,
        KEY_TYPED,
        MOUSE_WHEEL_MOVED,
        
        MOUSE_CLICKED,
        MOUSE_PRESSED,
        MOUSE_RELEASED,
        MOUSE_ENTERED,
        MOUSE_EXITED,
        MOUSE_DRAGGED,
        MOUSE_MOVED;
        
        static @Nullable OnEvent of(InputEvent inputEvent) {
            return switch (inputEvent) {
                case KeyEvent keyEvent when keyEvent.getClass() == KeyEvent.class -> switch (keyEvent.getID()) {
                    case KeyEvent.KEY_PRESSED -> OnEvent.KEY_PRESSED;
                    case KeyEvent.KEY_RELEASED -> OnEvent.KEY_RELEASED;
                    case KeyEvent.KEY_TYPED -> OnEvent.KEY_TYPED;
                    default -> null;
                };
                case MouseWheelEvent mouseEvent when mouseEvent.getClass() == MouseWheelEvent.class ->
                        OnEvent.MOUSE_WHEEL_MOVED;
                case MouseEvent mouseEvent when mouseEvent.getClass() == MouseEvent.class ->
                        switch (mouseEvent.getID()) {
                            case MouseEvent.MOUSE_CLICKED -> OnEvent.MOUSE_CLICKED;
                            case MouseEvent.MOUSE_PRESSED -> OnEvent.MOUSE_PRESSED;
                            case MouseEvent.MOUSE_RELEASED -> OnEvent.MOUSE_RELEASED;
                            case MouseEvent.MOUSE_ENTERED -> OnEvent.MOUSE_ENTERED;
                            case MouseEvent.MOUSE_EXITED -> OnEvent.MOUSE_EXITED;
                            case MouseEvent.MOUSE_DRAGGED -> OnEvent.MOUSE_DRAGGED;
                            case MouseEvent.MOUSE_MOVED -> OnEvent.MOUSE_MOVED;
                            default -> null;
                        };
                default -> null;
            };
        }
    }
    
    public enum GroupReaction {
        ALL, ANY, NONE;
    }
    
    public final @NotNull BindGroup bindings;
    private @NotNull Binding.MeetCondition meetCondition;
    private @NotNull GroupReaction groupReaction;
    
    private @Nullable Binding.MeetCondition previousMeetCondition;
    private @Nullable GroupReaction previousGroupReaction;
    
    final @NotNull Set<OnEvent> eventsToReact;
    
    public final boolean isBlocker;
    public final @NotNull Class<TInputEvent> inputKind;
    
    private @NotNull BiConsumer<TInputEvent, BindGroup> onCommand;
    private @NotNull List<BiPredicate<TInputEvent, BindGroup>> processIf;
    
    public static <TInputEvent extends InputEvent, BindGroup extends BindingGroup> BindingCommandGroup<TInputEvent, BindGroup> of(
            BindGroup binding,
            @NotNull Binding.MeetCondition meetCondition,
            boolean blocksOtherCommands,
            @Nullable Class<TInputEvent> inputKind,
            @NotNull List<OnEvent> eventsToReact
    ) {
        return new BindingCommandGroup<>(binding,
                                         meetCondition,
                                         blocksOtherCommands,
                                         Optional.ofNullable(inputKind)
                                                 .orElseGet(() -> (Class<TInputEvent>) InputEvent.class),
                                         eventsToReact);
    }
    
    BindingCommandGroup(
            BindGroup binding,
            @NotNull Binding.MeetCondition meetCondition,
            boolean blocksOtherCommands,
            @NotNull Class<TInputEvent> inputKind,
            @NotNull List<OnEvent> eventsToReact
    ) {
        this.bindings = binding;
        this.meetCondition = meetCondition;
        this.isBlocker = blocksOtherCommands;
        this.inputKind = inputKind;
        this.groupReaction = GroupReaction.ANY;
        this.onCommand = (_, _) -> {
        };
        this.eventsToReact = Set.copyOf(eventsToReact);
        this.processIf = new ArrayList<>();
    }
    
    public BindingCommandGroup<TInputEvent, BindGroup> withAction(BiConsumer<TInputEvent, BindGroup> onCommand) {
        this.onCommand = onCommand;
        return this;
    }
    
    public BindingCommandGroup<TInputEvent, BindGroup> reactingOn(@NotNull Binding.MeetCondition previousMeetCondition, @NotNull GroupReaction previousGroupReaction, @NotNull Binding.MeetCondition nextMeetCondition, @NotNull GroupReaction nextGroupReaction) {
        this.previousMeetCondition = previousMeetCondition;
        this.previousGroupReaction = previousGroupReaction;
        this.meetCondition = nextMeetCondition;
        this.groupReaction = nextGroupReaction;
        return this;
    }
    
    public BindingCommandGroup<TInputEvent, BindGroup> processIf(BiPredicate<TInputEvent, BindGroup> condition) {
        this.processIf.add(condition);
        return this;
    }
    
    public boolean isTriggered() {
        if (this.previousMeetCondition != null && this.previousGroupReaction != null) {
            boolean pastTriggered = isTriggeredBy(this.previousGroupReaction, this.previousMeetCondition, false);
            if (!pastTriggered) {
                return false;
            }
        }
        return isTriggeredBy(this.groupReaction, this.meetCondition, true);
    }
    
    private boolean isTriggeredBy(@NotNull GroupReaction reactionGroup, @NotNull Binding.MeetCondition meetCondition, boolean directionIsForward) {
        return switch (this.bindings) {
            case Binding binding -> switch (reactionGroup) {
                case ALL, ANY -> binding.isMet(meetCondition, directionIsForward);
                case NONE -> !binding.isMet(meetCondition, directionIsForward);
            };
            case BindingGroup.BindingList bindingList -> switch (reactionGroup) {
                case ALL -> bindingList.stream().allMatch(binding -> binding.isMet(meetCondition, directionIsForward));
                case ANY -> bindingList.stream().anyMatch(binding -> binding.isMet(meetCondition, directionIsForward));
                case NONE ->
                        bindingList.stream().noneMatch(binding -> binding.isMet(meetCondition, directionIsForward));
            };
        };
    }
    
    boolean allowsProcessing(InputEvent inputEvent) {
        if (this.processIf.isEmpty()) {
            return true;
        }
        TInputEvent tInputEvent = this.inputKind.cast(inputEvent);
        return this.processIf.stream().anyMatch(condition -> condition.test(tInputEvent, this.bindings));
    }
    
    void triggerAction(InputEvent inputEvent) {
        this.onCommand.accept(this.inputKind.cast(inputEvent), this.bindings);
    }
    
    
}
