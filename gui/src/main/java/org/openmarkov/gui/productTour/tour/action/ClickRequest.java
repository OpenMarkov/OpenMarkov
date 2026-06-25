package org.openmarkov.gui.productTour.tour.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.productTour.tour.MessagePlacement;
import org.openmarkov.gui.productTour.tour.TourEffects;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClickRequest {
    
    private final @NotNull Component component;
    private @NotNull ClickKind clickKind;
    private @NotNull ReactionMode reactionMode;
    
    private @NotNull Function<Component, Shape> at;
    private boolean atWasManuallySet;
    private @Nullable TourEffects.ShowMessage message;
    
    private ClickRequest(@NotNull Component component) {
        this.component = component;
        this.clickKind = ClickKind.CLICK;
        this.at = (ignored)-> component.getBounds();
        this.atWasManuallySet = false;
        this.reactionMode=ReactionMode.AFTER_LISTENERS;
    }
    
    public static ClickRequest of(@NotNull Component component){
        return new ClickRequest(component);
    }
    
    public static ClickRequest of(@NotNull NetworkEditorPanel networkEditorPanel, VisualElement node){
        return new ClickRequest(networkEditorPanel)
                .at(()->node.getShape((Graphics2D) networkEditorPanel.getGraphics()));
    }
    
    public ClickRequest at(@NotNull Function<Component, Shape> at) {
        this.at = at;
        this.atWasManuallySet=true;
        return this;
    }
    
    public ClickRequest at(@NotNull Supplier<Shape> at) {
        this.at = (ignored)->at.get();
        this.atWasManuallySet=true;
        return this;
    }
    
    public @NotNull ClickRequest withMessage(@NotNull String message) {
        this.message=new TourEffects.ShowMessage(component, message, MessagePlacement.RIGHT, this.at);
        return this;
    }
    
    public @NotNull ClickRequest withMessage(@NotNull String message, @NotNull MessagePlacement messagePlacement) {
        this.message=new TourEffects.ShowMessage(component, message, messagePlacement, this.at);
        return this;
    }
    
    public ClickRequest clickKind(ClickKind clickKind) {
        this.clickKind=clickKind;
        return this;
    }
    
    public ClickRequest reactionMode(ReactionMode reactionMode) {
        this.reactionMode=reactionMode;
        return this;
    }
    
    public @NotNull Component getComponent(){
        return this.component;
    }
    
    public @NotNull ClickKind getClickKind(){
        return this.clickKind;
    }
    
    public @NotNull Function<Component, Shape> getAt(){
        return this.at;
    }
    
    public @NotNull ReactionMode getReactionMode() {
        return this.reactionMode;
    }
    
    public @Nullable TourEffects.ShowMessage getMessage() {
        return this.message;
    }
    
    public boolean atWasManuallySet(){
        return atWasManuallySet;
    }
    
    public enum ReactionMode {
        BEFORE_LISTENERS,
        AFTER_LISTENERS,
    }
    
    
}
