/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.ZoomManager;

import javax.swing.SwingUtilities;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the network editor, Escape in the middle of a drag or of a selection band ends it: until the mouse button is
 * released, moving the mouse does nothing (error 303).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class EscapeEndsTheBandAndTheDragTest {

    private static boolean ignoreStorage;

    private NetworkEditorPanel tab;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** A Bayesian network with A at (100, 100) and B at (600, 100), at a zoom of 100 %. */
    @BeforeEach void openTwoNodes() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("two nodes");
        Variable a = new Variable("A", "absent", "present");
        Variable b = new Variable("B", "absent", "present");
        net.addNode(a, NodeType.CHANCE).setCoordinateX(100);
        net.getNode(a).setCoordinateY(100);
        net.addNode(b, NodeType.CHANCE).setCoordinateX(600);
        net.getNode(b).setCoordinateY(100);
        SwingUtilities.invokeAndWait(() -> {
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
            tab.setZoom(1);
        });
    }

    @AfterEach void closeTheTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> MainGUI.INSTANCE.mainPanel.forceClose(tab));
    }

    private MouseEvent event(int id, long when, int modifiers, double x, double y, int button) {
        ZoomManager zoom = tab.getZoomManager();
        return new MouseEvent(tab, id, when, modifiers, (int) Math.round(zoom.panelToScreen(x)),
                              (int) Math.round(zoom.panelToScreen(y)), id == MouseEvent.MOUSE_PRESSED ? 1 : 0, false,
                              button);
    }

    /**
     * Presses the left button at (x, y), moves the mouse by each of the steps before Escape, presses and releases
     * Escape, moves the mouse by each of the steps after it and releases the button. Escape goes through the focus
     * manager, where the program follows the keys, and then to the canvas, as if it had the focus.
     */
    private void dragWithEscape(double x, double y, double[][] before, double[][] after) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            long now = System.currentTimeMillis();
            double[] current = { x, y };
            tab.dispatchEvent(event(MouseEvent.MOUSE_MOVED, now, 0, x, y, MouseEvent.NOBUTTON));
            tab.dispatchEvent(event(MouseEvent.MOUSE_PRESSED, ++now, InputEvent.BUTTON1_DOWN_MASK, x, y,
                                    MouseEvent.BUTTON1));
            for (double[][] steps : new double[][][] { before, after }) {
                for (double[] step : steps) {
                    current[0] += step[0];
                    current[1] += step[1];
                    tab.dispatchEvent(event(MouseEvent.MOUSE_DRAGGED, ++now, InputEvent.BUTTON1_DOWN_MASK, current[0],
                                            current[1], MouseEvent.NOBUTTON));
                }
                if (steps == before) {
                    KeyEvent pressed = new KeyEvent(tab, KeyEvent.KEY_PRESSED, ++now, InputEvent.BUTTON1_DOWN_MASK,
                                                    KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                    KeyEvent released = new KeyEvent(tab, KeyEvent.KEY_RELEASED, ++now, InputEvent.BUTTON1_DOWN_MASK,
                                                     KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                    for (KeyEvent key : List.of(pressed, released)) {
                        tab.dispatchEvent(key);
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(tab, key);
                    }
                }
            }
            tab.dispatchEvent(event(MouseEvent.MOUSE_RELEASED, ++now, 0, current[0], current[1], MouseEvent.BUTTON1));
        });
    }

    private static double[][] steps(int number, double dx, double dy) {
        double[][] steps = new double[number][];
        for (int i = 0; i < number; i++) {
            steps[i] = new double[] { dx, dy };
        }
        return steps;
    }

    private VisualNode node(String name) {
        return tab.getVisualNetwork().getAllNodes().stream().filter(node -> node.getNode().getName().equals(name))
                  .findFirst().orElseThrow();
    }

    @Test void aNodeDraggedAfterEscapeStaysWhereItWas() throws Exception {
        dragWithEscape(110, 110, steps(6, 10, 0), steps(3, 10, 0));

        assertEquals(100, node("A").getPosition().x, 1e-9);
        assertEquals(100, node("A").getPosition().y, 1e-9);
    }

    @Test void aBandContinuedAfterEscapeSelectsNothing() throws Exception {
        dragWithEscape(40, 40, steps(2, 10, 10), new double[][] { { 400, 260 } });

        List<String> selected = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> tab.getVisualNetwork().getSelectedNodes()
                                              .forEach(node -> selected.add(node.getNode().getName())));
        assertEquals(List.of(), selected);
    }
}
