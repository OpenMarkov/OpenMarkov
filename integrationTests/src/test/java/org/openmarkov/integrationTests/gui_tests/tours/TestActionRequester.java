package org.openmarkov.integrationTests.gui_tests.tours;

import org.assertj.swing.core.MouseButton;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.gui.productTour.tour.Tour;
import org.openmarkov.gui.productTour.tour.TourEffects;
import org.openmarkov.gui.productTour.tour.TourGlassPane;
import org.openmarkov.gui.productTour.tour.action.ActionRequester;
import org.openmarkov.gui.productTour.tour.action.ClickRequest;
import org.openmarkov.gui.productTour.tour.action.TextRequest;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.util.Collections;
import java.util.List;

public class TestActionRequester extends ActionRequester {
    
    private final Robot robot;
    
    protected TestActionRequester(Tour tour, Robot robot) {
        super(tour);
        this.robot = robot;
    }
    
    @Override protected void text(@NotNull TextRequest textRequest) {
        new JTextComponentFixture(robot, textRequest.getComponent())
                .focus()
                .deleteText()
                .enterText(textRequest.getDefaultText());
    }
    
    @Override protected void click(@NotNull ClickRequest clickRequest) {
        while (true) {
            try {
                var component = clickRequest.getComponent();
                if (component instanceof JMenuItem jMenuItem) {
                    List<Component> parents = ComponentUtilities.parentsWithSelfUpToWindow(jMenuItem);
                    Collections.reverse(parents);
                    parents = parents.subList(parents.indexOf(parents.stream()
                                                                     .filter(JPopupMenu.class::isInstance)
                                                                     .findFirst()
                                                                     .get()), parents.size());
                    for (var parent : parents) {
                        Point location = parent.getLocationOnScreen();
                        int x = (int) (location.x + parent.getBounds().getCenterX());
                        int y = (int) (location.y + parent.getBounds().getCenterY());
                        robot.moveMouse(x, y);
                    }
                }
                Point location = component.getLocationOnScreen();
                int x = (int) (location.x + clickRequest.getAt().apply(component).getBounds().getCenterX());
                int y = (int) (location.y + clickRequest.getAt().apply(component).getBounds().getCenterY());
                robot.moveMouse(x, y);
                switch (clickRequest.getClickKind()) {
                    case CLICK -> {
                        robot.pressMouse(MouseButton.LEFT_BUTTON);
                        robot.releaseMouse(MouseButton.LEFT_BUTTON);
                        if (component instanceof JMenuItem jMenuItem) {
                            jMenuItem.doClick();
                            this.semaphore.release();
                        }
                    }
                    case DOUBLE_CLICK -> {
                        robot.pressMouse(MouseButton.LEFT_BUTTON);
                        robot.releaseMouse(MouseButton.LEFT_BUTTON);
                        robot.pressMouse(MouseButton.LEFT_BUTTON);
                        robot.releaseMouse(MouseButton.LEFT_BUTTON);
                    }
                    case RIGHT_CLICK -> {
                        robot.pressMouse(MouseButton.RIGHT_BUTTON);
                        robot.releaseMouse(MouseButton.RIGHT_BUTTON);
                    }
                    case DOUBLE_RIGHT_CLICK -> {
                        robot.pressMouse(MouseButton.RIGHT_BUTTON);
                        robot.releaseMouse(MouseButton.RIGHT_BUTTON);
                        robot.pressMouse(MouseButton.RIGHT_BUTTON);
                        robot.releaseMouse(MouseButton.RIGHT_BUTTON);
                    }
                }
                return;
            } catch (RuntimeException e) {
            }
        }
    }
    
    @Override protected void doMessage(TourEffects.ShowMessage message) {
        while (true) {
            try {
                //robot.waitForIdle();
                // Get the screen location of the component
                
                TourGlassPane<? extends Window> activeWindowTourGlassPane = tour.getWindowTourGlassPaneOf(message.component());
                Point location = activeWindowTourGlassPane.getLocationOnScreen();
                int x = (int) (location.x + message.continueButtonRect().get().getCenterX());
                int y = (int) (location.y + message.continueButtonRect().get().getCenterY());
                
                // Move and click
                robot.moveMouse(x, y);
                robot.pressMouse(MouseButton.LEFT_BUTTON);
                robot.releaseMouse(MouseButton.LEFT_BUTTON);
                return;
            }catch(RuntimeException e){
                System.out.println(e);
            }
        }
    }
    
}
    
    

