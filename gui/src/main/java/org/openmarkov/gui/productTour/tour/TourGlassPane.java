package org.openmarkov.gui.productTour.tour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.graphics.PopupTextbox;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class TourGlassPane<Target extends Window> extends JComponent {
    
    private final @NotNull Tour tour;
    private final @NotNull Target target;
    private final @NotNull KeyEventDispatcher keyEventDispatcher;
    private final @NotNull KeyEventPostProcessor keyEventPostProcessor;
    
    private Component lastHoveredComponent = null;
    private int forceResetInRepaintInPaint = TourGlassPane.FORCE_RESET_N_TIMES_IN_REPAINT;
    private static final int FORCE_RESET_N_TIMES_IN_REPAINT = 10;
    
    public TourGlassPane(Tour tour, Target target) {
        this.tour = tour;
        this.target = target;
        this.setOpaque(false);
        Map<Component, List<OverrideInput>> triggeredInputs = new HashMap<>();
        this.keyEventDispatcher = keyEvent -> {
            if (!this.isVisible()) {
                return false;
            }
            Component keyEventComponent = keyEvent.getComponent();
            var base = this.tour
                    .overrideInputsOf(keyEventComponent)
                    .toList();
            var overrideInputs = base
                    .stream()
                    .filter(overrideInput -> overrideInput.allowsProcessKeyEventFor(keyEvent, keyEventComponent))
                    .toList();
            if (overrideInputs.isEmpty()) {
                return true;
            }
            if (!triggeredInputs.containsKey(keyEventComponent)) {
                triggeredInputs.put(keyEventComponent, overrideInputs);
            } else {
                overrideInputs.forEach(overrideInput -> overrideInput
                        .beforeProcessingKeyEvent(keyEvent, keyEventComponent));
                triggeredInputs.put(keyEventComponent, Stream.concat(
                        triggeredInputs.get(keyEventComponent).stream(),
                        overrideInputs.stream()
                ).toList());
            }
            return false;
        };
        this.keyEventPostProcessor = keyEvent -> {
            Component keyEventComponent = keyEvent.getComponent();
            if (triggeredInputs.containsKey(keyEventComponent)) {
                List<OverrideInput> overrideInputs = triggeredInputs.remove(keyEventComponent);
                overrideInputs.forEach(overrideInput -> overrideInput
                        .afterProcessingKeyEvent(keyEvent, keyEventComponent));
            }
            return false;
        };
        
        
        MouseAdapter adapter = new MouseAdapter() {
            
            @Override public void mousePressed(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseReleased(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseClicked(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseMoved(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseDragged(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseEntered(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseExited(MouseEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
            
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                TourGlassPane.this.handleMouse(e);
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = (JRootPane) SwingUtilities.getAncestorOfClass(JRootPane.class, this);
        if (root != null) {
            root.addComponentListener(new ComponentListener() {
                @Override public void componentResized(ComponentEvent e) {
                    TourGlassPane.this.resetBoundsAndRepaint();
                }
                
                @Override public void componentMoved(ComponentEvent e) {
                    TourGlassPane.this.resetBoundsAndRepaint();
                }
                
                @Override public void componentShown(ComponentEvent e) {
                    TourGlassPane.this.resetBoundsAndRepaint();
                }
                
                @Override public void componentHidden(ComponentEvent e) {
                    TourGlassPane.this.resetBoundsAndRepaint();
                }
            });
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .addKeyEventDispatcher(this.keyEventDispatcher);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .addKeyEventPostProcessor(this.keyEventPostProcessor);
    }
    
    @Override
    public void removeNotify() {
        
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .removeKeyEventDispatcher(this.keyEventDispatcher);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .removeKeyEventPostProcessor(this.keyEventPostProcessor);
        super.removeNotify();
    }
    
    private void handleMouse(MouseEvent e) {
        Point p = e.getPoint();
        
        var messageOfMouseEvent = forceInConcurrentException(()-> this.tour.getEffects(TourEffects.ShowMessage.class)
                                           .filter(message -> ComponentUtilities.parentsWithSelfUpToWindow(message.component())
                                                                                .contains(this.target))
                                           .filter(message -> message.drawnRect()
                                                                     .get() instanceof Shape shape && shape.contains(p))
                                           .findFirst());
        if (messageOfMouseEvent.isPresent()) {
            var message = messageOfMouseEvent.get();
            if (e.getID() == MouseEvent.MOUSE_MOVED) {
                this.redispatchHover(e, true);
            }
            if (
                    message.hasContinueButton()
                            && message.continueButtonRect().get() instanceof Shape shape
                            && message.onClicksContinueButton().get() != null
                            && shape.contains(p)
                            && (e.getID() == MouseEvent.MOUSE_CLICKED || e.getID() == MouseEvent.MOUSE_RELEASED)) {
                message.onClicksContinueButton().get().run();
            }
            return;
        }
        if (e.getID() == MouseEvent.MOUSE_MOVED) {
            this.redispatchHover(e, false);
            return;
        }
        if (e.getID() == MouseEvent.MOUSE_PRESSED && !lastMenuElements.isEmpty() && lastMenuElements.getLast() instanceof JMenuItem menuComponent) {
            Rectangle componentBounds = menuComponent.getBounds();
            this.dispatchMouseEvent(menuComponent, MouseEvent.MOUSE_PRESSED, e, false, new Point((int) componentBounds.getCenterX(), (int) componentBounds.getCenterY()));
            this.dispatchMouseEvent(menuComponent, MouseEvent.MOUSE_CLICKED, e, false, new Point((int) componentBounds.getCenterX(), (int) componentBounds.getCenterY()));
            for (ActionListener actionListener : menuComponent.getActionListeners()) {
                actionListener.actionPerformed(new ActionEvent(menuComponent, 0, "actionPerformed"));
            }
            this.dispatchMouseEvent(menuComponent, MouseEvent.MOUSE_RELEASED, e, false, new Point((int) componentBounds.getCenterX(), (int) componentBounds.getCenterY()));
        }
        this.redispatch(e, false);
    }
    
    private void redispatch(MouseEvent e, boolean removePositionOnEvent) {
        JRootPane root = (JRootPane) SwingUtilities.getAncestorOfClass(JRootPane.class, this);
        // Search from the layered pane so the glass pane is never a candidate
        JLayeredPane layered = root.getLayeredPane();
        Point lp = SwingUtilities.convertPoint(this, e.getPoint(), layered);
        Component target = SwingUtilities.getDeepestComponentAt(layered, lp.x, lp.y);
        if (target == null || target == this) {
            return;
        }
        
        Point tp = SwingUtilities.convertPoint(this, e.getPoint(), target);
        dispatchMouseEvent(target, e.getID(), e, removePositionOnEvent, tp);
    }
    
    private List<MenuElement> lastMenuElements = Collections.emptyList();
    
    private void redispatchHover(MouseEvent e, boolean removePositionOnEvent) {
        lastMenuElements = Arrays.stream(MenuSelectionManager.defaultManager().getSelectedPath()).toList();
        JRootPane root = (JRootPane) SwingUtilities.getAncestorOfClass(JRootPane.class, this);
        JLayeredPane layered = root.getLayeredPane();
        Point lp = SwingUtilities.convertPoint(this, e.getPoint(), layered);
        @Nullable Component currentTarget = SwingUtilities.getDeepestComponentAt(layered, lp.x, lp.y);
        if (currentTarget == this) {
            currentTarget = null;
        }
        if (currentTarget != this.lastHoveredComponent) {
            this.handleHierarchyTransition(e, this.lastHoveredComponent, currentTarget, removePositionOnEvent);
            this.lastHoveredComponent = currentTarget;
        } else {
            this.redispatch(e, removePositionOnEvent);
        }
    }
    
    private void handleHierarchyTransition(MouseEvent e, Component previouslyHoveredComponent, Component currentlyHoveredComponent, boolean removePositionOnEvent) {
        List<Component> previousComponentHierarchy = previouslyHoveredComponent != null ?
                ComponentUtilities.parentsWithSelfUpToWindow(previouslyHoveredComponent)
                : Collections.emptyList();
        List<Component> currentComponentHierarchy = ComponentUtilities.parentsWithSelfUpToWindow(currentlyHoveredComponent);
        int exitedInPreviousHierarchy = previousComponentHierarchy.size() - 1;
        int enteredInCurrentHierarchy = currentComponentHierarchy.size() - 1;
        while (exitedInPreviousHierarchy >= 0 && enteredInCurrentHierarchy >= 0 && previousComponentHierarchy.get(exitedInPreviousHierarchy) == currentComponentHierarchy.get(enteredInCurrentHierarchy)) {
            exitedInPreviousHierarchy--;
            enteredInCurrentHierarchy--;
        }
        
        for (int i = 0; i <= exitedInPreviousHierarchy; i++) {
            Component target = previousComponentHierarchy.get(i);
            Point p = SwingUtilities.convertPoint(this, e.getPoint(), target);
            dispatchMouseEvent(target, MouseEvent.MOUSE_EXITED, e, removePositionOnEvent, p);
        }
        for (int i = enteredInCurrentHierarchy; i >= 0; i--) {
            Component target = currentComponentHierarchy.get(i);
            Point p = SwingUtilities.convertPoint(this, e.getPoint(), target);
            dispatchMouseEvent(target, MouseEvent.MOUSE_ENTERED, e, removePositionOnEvent, p);
        }
    }
    
    static <T> T forceInConcurrentException(Supplier<T> supplier) {
        int attemptToForce = 0;
        while (true) {
            try {
                return supplier.get();
            } catch (ConcurrentModificationException concurrentModificationException) {
                attemptToForce++;
                if(attemptToForce > 1000) {
                    throw concurrentModificationException;
                }
                System.err.println(concurrentModificationException);
                concurrentModificationException.printStackTrace();
            }
        }
    }
    
    static void forceInConcurrentException(Runnable action) {
        forceInConcurrentException(()->{
            action.run();
            return null;
        });
    }
    
    private void dispatchMouseEvent(Component target, int e, MouseEvent e1, boolean removePositionOnEvent, Point tp) {
        MouseEvent mouseEvent = new MouseEvent(
                target, e, e1.getWhen(), e1.getModifiersEx(),
                removePositionOnEvent ? -1 : tp.x, removePositionOnEvent ? -1 : tp.y, e1.getClickCount(), e1.isPopupTrigger(), e1.getButton()
        );
        List<OverrideInput> overrideInputs = forceInConcurrentException(() -> this.tour
                .overrideInputsOf(target)
                .toList()
                .stream()
                .filter(overrideInput -> overrideInput.allowsProcessMouseEventFor(mouseEvent, target))
                .toList());
        if (overrideInputs.isEmpty()) {
            return;
        }
        overrideInputs.forEach(overrideInput -> overrideInput.beforeProcessingMouseEvent(mouseEvent, target));
        
        target.dispatchEvent(mouseEvent);
        overrideInputs.forEach(overrideInput -> overrideInput.afterProcessingMouseEvent(mouseEvent, target));
    }
    
    @Override public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            resetRepaintCount();
        }
    }
    
    public void resetRepaintCount() {
        this.forceResetInRepaintInPaint = TourGlassPane.FORCE_RESET_N_TIMES_IN_REPAINT;
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        if (this.forceResetInRepaintInPaint > 0) {
            this.forceResetInRepaintInPaint -= 1;
            this.resetBoundsAndRepaint();
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        var backgroundEffect = this.tour.getEffect(TourEffects.UseBackground.class);
        if (backgroundEffect.isPresent()) {
            var backgroundColor = backgroundEffect.get().color();
            Area overlay = new Area(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
            this.tour.getEffects(TourEffects.SkipBackgroundOnComponent.class)
                     .filter(skippedOverlayComponent -> skippedOverlayComponent.component().isVisible())
                     .filter(skippedOverlayComponent -> ComponentUtilities.parentsWithSelfUpToWindow(skippedOverlayComponent.component())
                                                                          .contains(this.target))
                     .forEach(skippedOverlayComponent -> {
                         Point p = SwingUtilities.convertPoint(
                                 skippedOverlayComponent.component().getParent(),
                                 skippedOverlayComponent.component().getLocation(),
                                 this
                         );
                         var componentArea = AffineTransform.getTranslateInstance(p.x, p.y)
                                                            .createTransformedShape(skippedOverlayComponent.at()
                                                                                                           .apply(skippedOverlayComponent.component()));
                         overlay.subtract(new Area(componentArea));
                     });
            g2.setColor(backgroundColor);
            g2.fill(overlay);
        }
        
        var previousStroke = g2.getStroke();
        this.tour.getEffects(TourEffects.DrawBorderOn.class)
                 .filter(drawBorder -> drawBorder.component().isVisible())
                 .filter(drawBorder -> ComponentUtilities.parentsWithSelfUpToWindow(drawBorder.component())
                                                         .contains(this.target))
                 .forEach(drawBorder -> {
                     Point p = SwingUtilities.convertPoint(
                             drawBorder.component().getParent(),
                             drawBorder.component().getLocation(),
                             this
                     );
                     var componentArea = AffineTransform.getTranslateInstance(p.x, p.y)
                                                        .createTransformedShape(drawBorder.at()
                                                                                          .apply(drawBorder.component()));
                     
                     g2.setStroke(drawBorder.stroke());
                     g2.setColor(drawBorder.color());
                     g2.draw(componentArea);
                     
                 });
        g2.setStroke(previousStroke);
        
        for (var message : this.tour.getEffects(TourEffects.ShowMessage.class)
                                    .filter(message -> {
                                        ArrayList<Component> parents = ComponentUtilities.parentsWithSelfUpToWindow(message.component());
                                        return parents
                                                .contains(this.target);
                                    })
                                    .toList()) {
            Point componentLocation = SwingUtilities.convertPoint(
                    message.component().getParent(),
                    message.component().getLocation(),
                    this
            );
            Shape componentShape = message.componentBounds().apply(message.component());
            var shapeBounds = componentShape.getBounds2D();
            PopupTextbox textBox = new PopupTextbox(message.message());
            if (message.hasContinueButton()) {
                textBox.withButton(new PopupTextbox("Continue").withColors(
                        Color.WHITE,
                        new Color(0, 72, 255),
                        new Color(0, 0, 255)
                ));
            }
            Dimension textBoxDimensions = textBox.dimensions(g);
            
            var isInside = message.placement().isInside();
            Point drawPoint = new Point(
                    (int) switch (message.placement().horizontalPlacement()) {
                        case LEFT -> componentLocation.x - (isInside ? 0 : textBoxDimensions.width);
                        case CENTER ->
                                componentLocation.x + (shapeBounds.getWidth() / 2) - ((double) textBoxDimensions.width / 2);
                        case RIGHT ->
                                componentLocation.x + shapeBounds.getWidth() - (isInside ? textBoxDimensions.width : 0);
                    },
                    (int) switch (message.placement().verticalPlacement()) {
                        case TOP -> componentLocation.y - (isInside ? 0 : textBoxDimensions.height);
                        case CENTER ->
                                componentLocation.y + (shapeBounds.getHeight() / 2) - ((double) textBoxDimensions.height / 2);
                        case BOTTOM ->
                                componentLocation.y + shapeBounds.getHeight() - (isInside ? textBoxDimensions.height : 0);
                    }
            );
            drawPoint = new Point(
                    drawPoint.x + switch (message.placement()) {
                        case CENTER_OF_COMPONENT, BOTTOM_INSIDE, TOP_INSIDE, BOTTOM, TOP -> 0;
                        case RIGHT, LEFT_INSIDE -> TourGlassPane.MESSAGE_OFFSET;
                        case LEFT, RIGHT_INSIDE -> -TourGlassPane.MESSAGE_OFFSET;
                    },
                    drawPoint.y + switch (message.placement()) {
                        case CENTER_OF_COMPONENT, LEFT_INSIDE, RIGHT_INSIDE, LEFT, RIGHT -> 0;
                        case TOP, BOTTOM_INSIDE -> -TourGlassPane.MESSAGE_OFFSET;
                        case BOTTOM, TOP_INSIDE -> TourGlassPane.MESSAGE_OFFSET;
                    }
            );
            drawPoint = new Point((int) (drawPoint.x + shapeBounds.getX()), (int) (drawPoint.y + shapeBounds.getY()));
            drawPoint = new Point(
                    Math.max(0 + TourGlassPane.MESSAGE_OFFSET, Math.min(drawPoint.x, this.getSize().width - textBoxDimensions.width - TourGlassPane.MESSAGE_OFFSET)),
                    Math.max(0 + TourGlassPane.MESSAGE_OFFSET, Math.min(drawPoint.y, this.getSize().height - textBoxDimensions.height - TourGlassPane.MESSAGE_OFFSET)));
            
            textBox.paint(g2, drawPoint.x, drawPoint.y);
            message.drawnRect().set(new Rectangle(drawPoint, textBoxDimensions));
            message.continueButtonRect().set(textBox.buttonRect(g, drawPoint.x, drawPoint.y));
        }
        g2.dispose();
    }
    
    void resetBoundsAndRepaint() {
        JRootPane root = (JRootPane) SwingUtilities.getAncestorOfClass(JRootPane.class, TourGlassPane.this);
        if (root == null) {
            TourGlassPane.this.repaint();
            return;
        }
        TourGlassPane.this.setBounds(0, 0, root.getWidth(), root.getHeight());
        TourGlassPane.this.repaint();
        root.repaint();
    }
    
    private static final int MESSAGE_OFFSET = 10;
}