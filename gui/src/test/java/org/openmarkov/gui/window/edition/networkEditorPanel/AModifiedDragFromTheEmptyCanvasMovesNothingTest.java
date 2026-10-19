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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the network editor, with A selected, a drag from the empty canvas with Shift or Control held does not move A:
 * moving the nodes needs the button to be pressed on what is selected (error 313).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class AModifiedDragFromTheEmptyCanvasMovesNothingTest {

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

    /** A Bayesian network with A at (100, 100), selected, and B at (600, 100), at a zoom of 100 %. */
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
            tab.getVisualNetwork().setSelectedNode("A", true);
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

    /** The key goes through the focus manager, where the program follows the keys, and then to the canvas. */
    private void key(int id, long when, int keyCode, int modifiers) {
        KeyEvent key = new KeyEvent(tab, id, when, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
        tab.dispatchEvent(key);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(tab, key);
    }

    /** Holds {@code keyCode}, drags the mouse from (300, 300) by ten steps of (10, 10) and lets go of both. */
    private void dragFromTheEmptyCanvasHolding(int keyCode, int keyMask) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            long now = System.currentTimeMillis();
            double x = 300, y = 300;
            key(KeyEvent.KEY_PRESSED, now, keyCode, keyMask);
            tab.dispatchEvent(event(MouseEvent.MOUSE_MOVED, ++now, keyMask, x, y, MouseEvent.NOBUTTON));
            tab.dispatchEvent(event(MouseEvent.MOUSE_PRESSED, ++now, keyMask | InputEvent.BUTTON1_DOWN_MASK, x, y,
                                    MouseEvent.BUTTON1));
            for (int step = 0; step < 10; step++) {
                x += 10;
                y += 10;
                tab.dispatchEvent(event(MouseEvent.MOUSE_DRAGGED, ++now, keyMask | InputEvent.BUTTON1_DOWN_MASK, x, y,
                                        MouseEvent.NOBUTTON));
            }
            tab.dispatchEvent(event(MouseEvent.MOUSE_RELEASED, ++now, keyMask, x, y, MouseEvent.BUTTON1));
            key(KeyEvent.KEY_RELEASED, ++now, keyCode, 0);
        });
    }

    private VisualNode node(String name) {
        return tab.getVisualNetwork().getAllNodes().stream().filter(node -> node.getNode().getName().equals(name))
                  .findFirst().orElseThrow();
    }

    @Test void withShiftTheSelectedNodeStaysWhereItWas() throws Exception {
        dragFromTheEmptyCanvasHolding(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK);

        assertEquals(100, node("A").getPosition().x, 1e-9);
        assertEquals(100, node("A").getPosition().y, 1e-9);
    }

    @Test void withControlTheSelectedNodeStaysWhereItWas() throws Exception {
        dragFromTheEmptyCanvasHolding(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK);

        assertEquals(100, node("A").getPosition().x, 1e-9);
        assertEquals(100, node("A").getPosition().y, 1e-9);
    }
}
