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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
 * In the network editor, in both modes, a click on a place of the canvas without anything leaves nothing selected
 * (error 300).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class AClickOnTheEmptyCanvasClearsTheSelectionTest {

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

    @AfterEach void closeTheTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> MainGUI.INSTANCE.mainPanel.forceClose(tab));
    }

    /** A Bayesian network with A at (100, 100) and B at (300, 100). */
    private static ProbNet twoNodes() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("two nodes");
        Variable a = new Variable("A", "absent", "present");
        Variable b = new Variable("B", "absent", "present");
        net.addNode(a, NodeType.CHANCE).setCoordinateX(100);
        net.getNode(a).setCoordinateY(100);
        net.addNode(b, NodeType.CHANCE).setCoordinateX(300);
        net.getNode(b).setCoordinateY(100);
        net.addLink(a, b, true);
        return net;
    }

    /** Moves the mouse to the point (x, y) of the network and clicks there with the left button. */
    private void clickAt(double x, double y) {
        ZoomManager zoom = tab.getZoomManager();
        int screenX = (int) Math.round(zoom.panelToScreen(x));
        int screenY = (int) Math.round(zoom.panelToScreen(y));
        long now = System.currentTimeMillis();
        tab.dispatchEvent(new MouseEvent(tab, MouseEvent.MOUSE_MOVED, now, 0, screenX, screenY, 0, false,
                                         MouseEvent.NOBUTTON));
        tab.dispatchEvent(new MouseEvent(tab, MouseEvent.MOUSE_PRESSED, now + 1, InputEvent.BUTTON1_DOWN_MASK, screenX,
                                         screenY, 1, false, MouseEvent.BUTTON1));
        tab.dispatchEvent(new MouseEvent(tab, MouseEvent.MOUSE_RELEASED, now + 2, 0, screenX, screenY, 1, false,
                                         MouseEvent.BUTTON1));
        tab.dispatchEvent(new MouseEvent(tab, MouseEvent.MOUSE_CLICKED, now + 3, 0, screenX, screenY, 1, false,
                                         MouseEvent.BUTTON1));
    }

    private List<String> selected() {
        List<String> names = new ArrayList<>();
        for (VisualNode node : tab.getVisualNetwork().getSelectedNodes()) {
            names.add(node.getNode().getName());
        }
        return names;
    }

    @ParameterizedTest
    @EnumSource(NetworkEditorPanel.WorkingMode.class)
    void aClickWhereThereIsNothingLeavesNothingSelected(NetworkEditorPanel.WorkingMode mode) throws Exception {
        List<String> before = new ArrayList<>();
        List<String> after = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(twoNodes());
            tab.setWorkingMode(mode);
            tab.getVisualNetwork().setSelectedNode("A", true);
            before.addAll(selected());
        });
        SwingUtilities.invokeAndWait(() -> clickAt(200, 400));
        SwingUtilities.invokeAndWait(() -> after.addAll(selected()));

        assertEquals(List.of("A"), before);
        assertEquals(List.of(), after);
    }
}
