package org.openmarkov.gui.productTour.tour.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.productTour.tour.MessagePlacement;
import org.openmarkov.gui.productTour.tour.TourEffects;

import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Shape;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextRequest {
    
    private final @NotNull JTextComponent component;
    
    private @NotNull Function<Component, Shape> at;
    private final String defaultText;
    private final Predicate<JTextComponent> validWhen;
    private @Nullable TourEffects.ShowMessage message;
    
    public TextRequest(@NotNull JTextComponent component, String defaultText, Predicate<JTextComponent> validWhen) {
        this.component = component;
        
        this.at = (ignored)-> component.getBounds();
        this.defaultText = defaultText;
        this.validWhen = validWhen;
    }
    
    public TextRequest at(@NotNull Function<Component, Shape> at) {
        this.at = at;
        return this;
    }
    
    public TextRequest at(@NotNull Supplier<Shape> at) {
        this.at = (ignored)->at.get();
        return this;
    }
    
    public @NotNull TextRequest withMessage(@NotNull String message) {
        this.message=new TourEffects.ShowMessage(component, message, MessagePlacement.RIGHT, this.at);
        return this;
    }
    
    public @NotNull TextRequest withMessage(@NotNull String message, @NotNull MessagePlacement messagePlacement) {
        this.message=new TourEffects.ShowMessage(component, message, messagePlacement, this.at);
        return this;
    }
    
    public @NotNull JTextComponent getComponent(){
        return this.component;
    }
    
    
    public @NotNull Function<Component, Shape> getAt(){
        return this.at;
    }
    
    public @Nullable TourEffects.ShowMessage getMessage() {
        return this.message;
    }
    
    public Predicate<JTextComponent> getValidWhen() {
        return validWhen;
    }
    
    public String getDefaultText() {
        return defaultText;
    }
}
