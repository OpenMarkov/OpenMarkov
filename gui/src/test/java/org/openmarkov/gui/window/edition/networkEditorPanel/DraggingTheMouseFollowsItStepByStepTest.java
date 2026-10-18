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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the network editor, each step of a drag moves the selected nodes, or grows the selection band, as much as the
 * mouse moved in that step (error 302).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class DraggingTheMouseFollowsItStepByStepTest {

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

    /** Presses the left button at (x, y), moves the mouse by each of the steps and releases it. */
    private void drag(double x, double y, double[][] steps) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            long now = System.currentTimeMillis();
            double currentX = x;
            double currentY = y;
            tab.dispatchEvent(event(MouseEvent.MOUSE_MOVED, now, 0, currentX, currentY, MouseEvent.NOBUTTON));
            tab.dispatchEvent(event(MouseEvent.MOUSE_PRESSED, now + 1, InputEvent.BUTTON1_DOWN_MASK, currentX, currentY,
                                    MouseEvent.BUTTON1));
            for (double[] step : steps) {
                currentX += step[0];
                currentY += step[1];
                tab.dispatchEvent(event(MouseEvent.MOUSE_DRAGGED, ++now + 1, InputEvent.BUTTON1_DOWN_MASK, currentX,
                                        currentY, MouseEvent.NOBUTTON));
            }
            tab.dispatchEvent(event(MouseEvent.MOUSE_RELEASED, now + 2, 0, currentX, currentY, MouseEvent.BUTTON1));
        });
    }

    private static double[][] tenSteps(double dx, double dy) {
        double[][] steps = new double[10][];
        for (int i = 0; i < 10; i++) {
            steps[i] = new double[] { dx, dy };
        }
        return steps;
    }

    private VisualNode node(String name) {
        return tab.getVisualNetwork().getAllNodes().stream().filter(node -> node.getNode().getName().equals(name))
                  .findFirst().orElseThrow();
    }

    @Test void twoDragsInARowMoveEachNodeAsMuchAsTheMouse() throws Exception {
        drag(110, 110, tenSteps(8, 0));
        drag(610, 110, tenSteps(0, 8));

        assertEquals(180, node("A").getPosition().x, 1e-9);
        assertEquals(100, node("A").getPosition().y, 1e-9);
        assertEquals(600, node("B").getPosition().x, 1e-9);
        assertEquals(180, node("B").getPosition().y, 1e-9);
    }

    @Test void theLastStepOfTheBandCounts() throws Exception {
        drag(40, 40, new double[][] { { 10, 10 }, { 10, 10 }, { 400, 260 } });

        List<String> selected = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> tab.getVisualNetwork().getSelectedNodes()
                                              .forEach(node -> selected.add(node.getNode().getName())));
        assertEquals(List.of("A"), selected);
    }
}
