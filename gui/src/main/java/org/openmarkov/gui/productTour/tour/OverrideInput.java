package org.openmarkov.gui.productTour.tour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.java.swing.ComponentUtilities;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OverrideInput {
    
    private @NotNull Component target;
    private final boolean includesSubComponents;
    private @Nullable AllowProcessKeyEventWhen allowProcessKeyEventWhen;
    private @Nullable KeyEventProcessor beforeProcessingKeyEvent;
    private @Nullable KeyEventProcessor afterProcessingKeyEvent;
    private @Nullable AllowProcessMouseEventWhen allowProcessMouseEventWhen;
    private @Nullable MouseEventProcessor afterProcessingMouseEvent;
    private @Nullable MouseEventProcessor beforeProcessingMouseEvent;
    
    public OverrideInput(@NotNull Component target, boolean includesSubComponents) {
        this.target = target;
        this.includesSubComponents = includesSubComponents;
        this.allowProcessKeyEventWhen = null;
        this.allowProcessMouseEventWhen = null;
        this.beforeProcessingKeyEvent = null;
        this.beforeProcessingMouseEvent = null;
        this.afterProcessingKeyEvent = null;
        this.afterProcessingMouseEvent = null;
    }
    
    public OverrideInput allowProcessKeyEventWhen(@NotNull AllowProcessKeyEventWhen allowProcessKeyEventWhen) {
        if(this.allowProcessKeyEventWhen==null){
            this.allowProcessKeyEventWhen = allowProcessKeyEventWhen;
        }else{
            this.allowProcessKeyEventWhen = this.allowProcessKeyEventWhen.and(allowProcessKeyEventWhen);
        }
        return this;
    }
    
    public OverrideInput afterProcessingKeyEvent(@NotNull KeyEventProcessor afterProcessingKeyEvent) {
        if(this.afterProcessingKeyEvent==null){
            this.afterProcessingKeyEvent = afterProcessingKeyEvent;
        }else{
            this.afterProcessingKeyEvent = this.afterProcessingKeyEvent.andThen(afterProcessingKeyEvent);
        }
        return this;
    }
    
    public OverrideInput beforeProcessingKeyEvent(@NotNull KeyEventProcessor beforeProcessingKeyEvent) {
        if(this.beforeProcessingKeyEvent==null){
            this.beforeProcessingKeyEvent = beforeProcessingKeyEvent;
        }else{
            this.beforeProcessingKeyEvent = this.beforeProcessingKeyEvent.andThen(beforeProcessingKeyEvent);
        }
        return this;
    }
    
    public OverrideInput allowProcessMouseEventWhen(@NotNull AllowProcessMouseEventWhen allowProcessMouseEventWhen) {
        if(this.allowProcessMouseEventWhen==null){
            this.allowProcessMouseEventWhen = allowProcessMouseEventWhen;
        }else{
            this.allowProcessMouseEventWhen = this.allowProcessMouseEventWhen.and(allowProcessMouseEventWhen);
        }
        return this;
    }
    
    public OverrideInput allowProcessMouseEventOn(int... mouseEvents) {
        var mouseEventsSet = Arrays.stream(mouseEvents).mapToObj(Integer::valueOf).collect(Collectors.toSet());
        return this.allowProcessMouseEventWhen((mouseEvent, component) -> mouseEventsSet.contains(mouseEvent.getID()));
    }
    
    public OverrideInput afterProcessingMouseEvent(@NotNull MouseEventProcessor afterProcessingMouseEvent) {
        if(this.afterProcessingMouseEvent==null){
            this.afterProcessingMouseEvent = afterProcessingMouseEvent;
        }else{
            this.afterProcessingMouseEvent = this.afterProcessingMouseEvent.andThen(afterProcessingMouseEvent);
        }
        return this;
    }
    
    public OverrideInput beforeProcessingMouseEvent(@NotNull MouseEventProcessor beforeProcessingMouseEvent) {
        if(this.beforeProcessingMouseEvent==null){
            this.beforeProcessingMouseEvent = beforeProcessingMouseEvent;
        }else{
            this.beforeProcessingMouseEvent = this.beforeProcessingMouseEvent.andThen(beforeProcessingMouseEvent);
        }
        return this;
    }
    
    public boolean allowsProcessKeyEventFor(KeyEvent keyEvent, Component component) {
        if(this.allowProcessKeyEventWhen==null){
            return false;
        }
        if(!this.isAppliableTo(component)){
            return false;
        }
        return this.allowProcessKeyEventWhen.allowWhen(keyEvent, component);
    }
    
    public void beforeProcessingKeyEvent(KeyEvent keyEvent, Component component) {
        if(this.beforeProcessingKeyEvent==null){
            return;
        }
        if(!this.isAppliableTo(component)){
            return ;
        }
        this.beforeProcessingKeyEvent.process(keyEvent, component);
    }

    public void afterProcessingKeyEvent(KeyEvent keyEvent, Component component) {
        if(this.afterProcessingKeyEvent==null){
            return;
        }
        if(!this.isAppliableTo(component)){
            return ;
        }
        this.afterProcessingKeyEvent.process(keyEvent, component);
    }
    
    public boolean allowsProcessMouseEventFor(MouseEvent mouseEvent, Component component) {
        if(this.allowProcessMouseEventWhen==null){
            return false;
        }
        if(!this.isAppliableTo(component)){
            return false;
        }
        return this.allowProcessMouseEventWhen.allowWhen(mouseEvent, component);
    }
    
    public void afterProcessingMouseEvent(MouseEvent mouseEvent, Component component) {
        if(this.afterProcessingMouseEvent ==null){
            return;
        }
        if(!this.isAppliableTo(component)){
            return ;
        }
        this.afterProcessingMouseEvent.process(mouseEvent, component);
    }
    
    public void beforeProcessingMouseEvent(MouseEvent mouseEvent, Component component) {
        if(this.beforeProcessingMouseEvent ==null){
            return;
        }
        if(!this.isAppliableTo(component)){
            return ;
        }
        this.beforeProcessingMouseEvent.process(mouseEvent, component);
    }
    
    public boolean isAppliableTo(Component component) {
        if(this.target==component){
            return true;
        }
        if(this.includesSubComponents && ComponentUtilities.parentsUpToWindow(component).contains(component)){
            return true;
        }
        return false;
    }
    
    public @NotNull Component getTarget() {
        return this.target;
    }
    
    @FunctionalInterface
    public interface AllowProcessKeyEventWhen {
        boolean allowWhen(KeyEvent keyEvent, Component component);
        
        default AllowProcessKeyEventWhen and(AllowProcessKeyEventWhen other){
            return (keyEvent, component) -> this.allowWhen(keyEvent, component)&&other.allowWhen(keyEvent, component);
        };
    }
    
    @FunctionalInterface
    public interface KeyEventProcessor {
        void process(KeyEvent keyEvent, Component component);
        
        default KeyEventProcessor andThen(KeyEventProcessor other){
            return (keyEvent, component) -> {
                this.process(keyEvent, component);
                other.process(keyEvent, component);
            };
        }
    }
    
    @FunctionalInterface
    public interface AllowProcessMouseEventWhen {
        boolean allowWhen(MouseEvent mouseEvent, Component component);
        
        default AllowProcessMouseEventWhen and(AllowProcessMouseEventWhen other){
            return (mouseEvent, component) -> this.allowWhen(mouseEvent, component) && other.allowWhen(mouseEvent, component);
        };
    }
    
    @FunctionalInterface
    public interface MouseEventProcessor {
        void process(MouseEvent mouseEvent, Component component);
        
        default MouseEventProcessor andThen(MouseEventProcessor other){
            return (mouseEvent, component) -> {
                this.process(mouseEvent, component);
                other.process(mouseEvent, component);
            };
        }
    }
    
    
}
