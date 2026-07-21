package org.openmarkov.gui.productTour.tour.action;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.productTour.tour.MessagePlacement;
import org.openmarkov.gui.productTour.tour.OverrideInput;
import org.openmarkov.gui.productTour.tour.PredefinedOverrideInputs;
import org.openmarkov.gui.productTour.tour.Tour;
import org.openmarkov.gui.productTour.tour.TourEffects;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public abstract class ActionRequester {
    
    protected final Tour tour;
    
    protected Semaphore semaphore;
    
    protected ActionRequester(Tour tour) {
        this.tour = tour;
        
    }
    
    private void request(List<TourEffects.TourEffect> tourEffects, Runnable action) {
        var previousOverrideInputs = this.tour.getAllOverrideInputs();
        this.tour.applyEffects(tourEffects);
        this.semaphore = new Semaphore(0);
        action.run();
        try {
            this.tour.repaintWindows();
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            throw new UnreachableException(e);
        }
        this.tour.removeEffects(tourEffects);
        var currentOverrideInputs = this.tour.getAllOverrideInputs();
        currentOverrideInputs.stream()
                             .filter(Predicate.not(previousOverrideInputs::contains))
                             .forEach(this.tour::removeOverrideInput);
        
    }
    
    public final void requestText(@NotNull TextRequest textRequest, TourEffects.TourEffect... configUI) {
        var effects = new ArrayList<TourEffects.TourEffect>(configUI.length + 2);
        if (textRequest.getMessage() != null) {
            effects.add(textRequest.getMessage());
        }
        effects.add(new TourEffects.SkipBackgroundOnComponent(textRequest.getComponent()));
        effects.add(new TourEffects.DrawBorderOn(Color.GREEN, textRequest.getComponent()));
        Collections.addAll(effects, configUI);
        this.request(effects, () -> {
                         ComponentUtilities.parentsWithSelfUpToWindow(textRequest.getComponent())
                                           .forEach(component -> {
                                               this.tour.addOverrideInput(PredefinedOverrideInputs.allowHover(component));
                                           });
                         this.tour.addOverrideInput(new OverrideInput(textRequest.getComponent(), false)
                                                            .allowProcessMouseEventOn(MouseEvent.MOUSE_CLICKED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_PRESSED));
                         this.tour.addOverrideInput(new OverrideInput(textRequest.getComponent(), false)
                                                            .allowProcessKeyEventWhen((keyEvent, component) -> true)
                                                            .afterProcessingKeyEvent(((keyEvent, component) -> {
                                                                if (textRequest.getValidWhen().test(textRequest.getComponent())) {
                                                                    this.semaphore.release();
                                                                }
                                                            })));
                         this.text(textRequest);
                     }
        );
    }
    
    protected abstract void text(@NotNull TextRequest textRequest);
    
    public final void requestClick(@NotNull ClickRequest clickRequest, TourEffects.TourEffect... configUI) {
        AtomicBoolean isSelected = new AtomicBoolean(false);
        var effects = new ArrayList<TourEffects.TourEffect>(configUI.length + 3);
        if (clickRequest.getMessage() != null) {
            effects.add(clickRequest.getMessage());
        }
        if (!(clickRequest.getComponent() instanceof JMenuItem)) {
            if (clickRequest.atWasManuallySet()) {
                effects.add(new TourEffects.SkipBackgroundOnComponent(clickRequest.getComponent(), clickRequest.getAt()));
                effects.add(new TourEffects.DrawBorderOn(Color.GREEN, clickRequest.getComponent(), clickRequest.getAt()));
            } else {
                effects.add(new TourEffects.SkipBackgroundOnComponent(clickRequest.getComponent()));
                effects.add(new TourEffects.DrawBorderOn(Color.GREEN, clickRequest.getComponent()));
            }
        }
        Collections.addAll(effects, configUI);
        
        this.request(effects, () -> {
                         ComponentUtilities.parentsWithSelfUpToWindow(clickRequest.getComponent())
                                           .forEach(component -> {
                                               this.tour.addOverrideInput(PredefinedOverrideInputs.allowHover(component));
                                           });
                         OverrideInput.MouseEventProcessor inputProcessor = (mouseEvent, ignored) -> {
                             switch (mouseEvent.getID()) {
                                 case MouseEvent.MOUSE_PRESSED -> {
                                     isSelected.set(true);
                                     var isDoubleClickRequested = switch (clickRequest.getClickKind()) {
                                         case CLICK, RIGHT_CLICK -> false;
                                         case DOUBLE_CLICK, DOUBLE_RIGHT_CLICK -> true;
                                     };
                                     if (mouseEvent.getClickCount() == 2 && isDoubleClickRequested) {
                                         this.semaphore.release();
                                     }
                                 }
                                 case MouseEvent.MOUSE_EXITED -> isSelected.set(false);
                                 case MouseEvent.MOUSE_CLICKED -> this.semaphore.release();
                                 case MouseEvent.MOUSE_RELEASED -> {
                                     if (isSelected.get()) {
                                         this.semaphore.release();
                                     }
                                 }
                             }
                         };
                         OverrideInput actionExecutor = new OverrideInput(clickRequest.getComponent(), false)
                                 .allowProcessMouseEventOn(MouseEvent.MOUSE_CLICKED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_EXITED)
                                 .allowProcessMouseEventWhen((mouseEvent, ignored) -> switch (clickRequest.getClickKind()) {
                                     case CLICK -> mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() == 1;
                                     case DOUBLE_CLICK ->
                                             mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() == 2;
                                     case RIGHT_CLICK ->
                                             mouseEvent.getButton() == MouseEvent.BUTTON3 && mouseEvent.getClickCount() == 1;
                                     case DOUBLE_RIGHT_CLICK ->
                                             mouseEvent.getButton() == MouseEvent.BUTTON3 && mouseEvent.getClickCount() == 2;
                                 })
                                 .allowProcessMouseEventWhen((mouseEvent, component) -> clickRequest.getAt()
                                                                                                    .apply(component)
                                                                                                    .contains(mouseEvent.getPoint()));
                         actionExecutor = switch (clickRequest.getReactionMode()) {
                             case BEFORE_LISTENERS -> actionExecutor.beforeProcessingMouseEvent(inputProcessor);
                             case AFTER_LISTENERS -> actionExecutor.afterProcessingMouseEvent(inputProcessor);
                         };
                         this.tour.addOverrideInput(actionExecutor);
                         this.click(clickRequest);
                     }
        );
    }
    
    protected abstract void click(@NotNull ClickRequest clickRequest);
    
    public void requestPopInfo(Component component, String contents, MessagePlacement placement, TourEffects.TourEffect... configUI) {
        var message = new TourEffects.ShowMessage(component == null ? this.tour.getActiveWindow() : component, contents, placement, component1 -> component1.getBounds(),
                                                  new AtomicReference<>(null), true,
                                                  new AtomicReference<>(null), new AtomicReference<>(null));
        this.tour.applyEffects(message);
        this.request(Arrays.asList(configUI), () -> {
            message.onClicksContinueButton().set(() -> semaphore.release());
            this.doMessage(message);
        });
        this.tour.removeEffects(message);
    }
    
    protected abstract void doMessage(TourEffects.ShowMessage message);
    
}
