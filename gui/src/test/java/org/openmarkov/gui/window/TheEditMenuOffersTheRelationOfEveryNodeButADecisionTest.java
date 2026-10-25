/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

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
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The relation entry of the Edit menu is enabled for every selected node that is not a decision, as the node's menu
 * and Alt+click offer it, and it is labelled after the node.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheEditMenuOffersTheRelationOfEveryNodeButADecisionTest {

    private static boolean ignoreStorage;

    private MainPanel mainPanel;
    private NetworkEditorPanel tab;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void aNetworkWithANodeOfEachType() {
        ProbNet net = new ProbNet(DESNetworkType.getUniqueInstance());
        net.setName("relations");
        net.addNode(new Variable("Ill", "no", "yes"), NodeType.CHANCE);
        net.addNode(new Variable("Therapy", "no", "yes"), NodeType.DECISION);
        net.addNode(new Variable("QALY"), NodeType.UTILITY);
        Variable onset = new Variable("Onset", "no", "yes");
        onset.setVariableType(VariableType.EVENT);
        net.addNode(onset, NodeType.EVENT);
        mainPanel = MainGUI.INSTANCE.mainPanel;
        tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
    }

    @AfterEach void closeTheNetwork() {
        mainPanel.forceClose(tab);
    }

    @Test void anEventOffersItsRelation() {
        JMenuItem entry = relationEntryWith("Onset");
        assertTrue(entry.isEnabled());
        assertEquals("Edit probability ...", entry.getText());
    }

    @Test void aChanceNodeOffersItsRelation() {
        JMenuItem entry = relationEntryWith("Ill");
        assertTrue(entry.isEnabled());
        assertEquals("Edit probability ...", entry.getText());
    }

    @Test void aUtilityNodeOffersItsUtility() {
        JMenuItem entry = relationEntryWith("QALY");
        assertTrue(entry.isEnabled());
        assertEquals("Edit utility ...", entry.getText());
    }

    @Test void aDecisionDoesNot() {
        assertFalse(relationEntryWith("Therapy").isEnabled());
    }

    /** The relation entry of the Edit menu with only the node {@code name} selected. */
    private JMenuItem relationEntryWith(String name) {
        tab.getVisualNetwork().setSelectedAllObjects(false);
        tab.getVisualNetwork().setSelectedNode(name, true);
        mainPanel.getMainPanelMenuAssistant().objectsSelected();
        JMenuItem entry = itemOf(mainPanel.getMainMenu().getMenu(1), ActionCommands.EDIT_POTENTIAL);
        assertNotNull(entry);
        return entry;
    }

    private static JMenuItem itemOf(JMenu menu, ActionCommands command) {
        for (Component component : menu.getMenuComponents()) {
            if (component instanceof JMenuItem item && command.getCommandName().equals(item.getActionCommand())) {
                return item;
            }
        }
        return null;
    }
}
