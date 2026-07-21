package org.openmarkov.gui.productTour.tour;

import org.jetbrains.annotations.Nullable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TourEffects {
    
    public record Deny(TourEffect tourEffect) implements TourEffect {
    }
    
    public record UseBackground(Color color) implements TourEffect {
    }
    
    public record DrawBorderOn(Color color, Component component,
                               Function<Component, ? extends Shape> at, @Nullable Stroke stroke) implements TourEffect {
        
        private static final Stroke DEFAULT_STROKE = new BasicStroke(3);
        
        public DrawBorderOn(Color color, Component component, Function<Component, ? extends Shape> at) {
            this(color, component, at, DrawBorderOn.DEFAULT_STROKE);
        }
        
        public DrawBorderOn(Color color, Component component, Supplier<? extends Shape> at) {
            this(color, component, ignored -> at.get(), DrawBorderOn.DEFAULT_STROKE);
        }
        
        public DrawBorderOn(Color color, Component component) {
            this(color, component, paintComponent ->
                         new RoundRectangle2D.Double(-5, -5, paintComponent.getWidth() + 10, paintComponent.getHeight() + 10, 10, 10),
                 DrawBorderOn.DEFAULT_STROKE);
        }
        
        public DrawBorderOn(Color color, Component component, Supplier<? extends Shape> at, Stroke stroke) {
            this(color, component, ignored -> at.get(), stroke);
        }
        
        public DrawBorderOn(Color color, Component component, Stroke stroke) {
            this(color, component, paintComponent ->
                         new RoundRectangle2D.Double(-5, -5, paintComponent.getWidth() + 10, paintComponent.getHeight() + 10, 10, 10),
                 stroke);
        }
        
    }
    
    public record SkipBackgroundOnComponent(Component component,
                                            Function<Component, ? extends Shape> at) implements TourEffect {
        
        public SkipBackgroundOnComponent(Component component, Supplier<? extends Shape> at) {
            this(component, ignored -> at.get());
        }
        
        public SkipBackgroundOnComponent(Component component) {
            this(component, paintComponent ->
                    new RoundRectangle2D.Double(-5, -5, paintComponent.getWidth() + 10, paintComponent.getHeight() + 10, 10, 10));
        }
        
    }
    
    public record ShowMessage(Component component, String message, MessagePlacement placement,
                              Function<Component, ? extends Shape> componentBounds,
                              AtomicReference<Rectangle> drawnRect,
                              boolean hasContinueButton, AtomicReference<Rectangle> continueButtonRect,
                              AtomicReference<Runnable> onClicksContinueButton) implements TourEffect {
        
        public ShowMessage(Component component, String message, MessagePlacement placement, Function<Component, ? extends Shape> componentBounds) {
            this(component, message, placement, componentBounds,
                 new AtomicReference<>(null), false,
                 new AtomicReference<>(null), new AtomicReference<>(null));
        }
        
        public ShowMessage(Component component, String message, MessagePlacement placement, Supplier<? extends Shape> componentBounds) {
            this(component, message, placement, (ignored) -> componentBounds.get());
        }
        
        public ShowMessage(Component component, String message, MessagePlacement placement) {
            this(component, message, placement, Component::getBounds);
        }
        
        public ShowMessage(Component component, String message, Function<Component, ? extends Shape> componentBounds) {
            this(component, message, MessagePlacement.BOTTOM, componentBounds);
        }
        
        public ShowMessage(Component component, String message, Supplier<? extends Shape> componentBounds) {
            this(component, message, MessagePlacement.BOTTOM, (ignored) -> componentBounds.get());
        }
        
        public ShowMessage(Component component, String message) {
            this(component, message, MessagePlacement.BOTTOM, Component::getBounds);
        }
        
    }
    
    public static interface TourEffect {
    
    }
}
