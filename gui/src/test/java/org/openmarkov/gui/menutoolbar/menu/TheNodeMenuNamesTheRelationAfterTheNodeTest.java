/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.menutoolbar.menu;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.WorkingMode;

import javax.swing.JMenuItem;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The relation entry of a node's contextual menu says what the Edit menu says: utility for a utility node, and
 * «View» instead of «Edit» when asking the network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheNodeMenuNamesTheRelationAfterTheNodeTest {

    private static boolean ignoreStorage;

    private MainPanel mainPanel;
    private NetworkEditorPanel tab;
    private Node disease;
    private Node cost;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void anInfluenceDiagram() {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.setName("relations");
        disease = net.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
        cost = net.addNode(new Variable("Cost"), NodeType.UTILITY);
        mainPanel = MainGUI.INSTANCE.mainPanel;
        tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
    }

    @AfterEach void closeTheNetwork() {
        mainPanel.forceClose(tab);
    }

    @Test void whileEditingTheNetwork() {
        assertEquals("Edit utility ...", relationEntryOf(cost).getText());
        assertEquals("Edit probability ...", relationEntryOf(disease).getText());
    }

    @Test void whileAskingTheNetwork() {
        tab.setWorkingMode(WorkingMode.INFERENCE);
        assertEquals("View utility ...", relationEntryOf(cost).getText());
        assertEquals("View probability ...", relationEntryOf(disease).getText());
    }

    private JMenuItem relationEntryOf(Node node) {
        var menu = new NodeContextualMenu(_ -> {
        }, tab.getVisualNetwork().getVisualNodeOf(node), tab);
        for (Component component : menu.getComponents()) {
            if (component instanceof JMenuItem item
                    && ActionCommands.EDIT_POTENTIAL.getCommandName().equals(item.getActionCommand())) {
                return item;
            }
        }
        return fail("The menu of " + node.getName() + " has no relation entry");
    }
}
