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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JMenuItem;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * In ID-decide-test.pgmx the decision Therapy has no imposed policy: its menu has «Remove policy» disabled, and
 * removing its policy leaves nothing to undo.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class RemovePolicyIsOfferedOnlyWithAPolicyTest {

    private static boolean ignoreStorage;

    private MainPanel mainPanel;
    private NetworkEditorPanel tab;
    private Node therapy;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void openTheNetwork() throws Exception {
        ProbNet net = new PGMXReader().read(RemovePolicyIsOfferedOnlyWithAPolicyTest.class.getResource(
                "/ID-decide-test.pgmx")).probNet();
        net.getPNESupport().setWithUndo(true);
        therapy = net.getNode("Therapy");
        assertTrue(therapy.getPotentials().isEmpty());
        mainPanel = MainGUI.INSTANCE.mainPanel;
        tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
        tab.getVisualNetwork().setSelectedNode("Therapy", true);
    }

    @AfterEach void closeTheNetwork() {
        mainPanel.forceClose(tab);
    }

    @Test void theMenuOfADecisionWithoutAPolicyHasRemovePolicyDisabled() {
        var menu = new NodeContextualMenu(_ -> {
        }, tab.getVisualNetwork().getVisualNodeOf(therapy), tab);
        for (Component component : menu.getComponents()) {
            if (component instanceof JMenuItem item
                    && ActionCommands.DECISION_REMOVE_POLICY.getCommandName().equals(item.getActionCommand())) {
                assertFalse(item.isEnabled());
                return;
            }
        }
        fail("The menu of Therapy has no «Remove policy»");
    }

    @Test void removingThePolicyOfADecisionWithoutOneLeavesNothingToUndo() {
        tab.removePolicyFromNode();
        assertTrue(therapy.getPotentials().isEmpty());
        assertFalse(therapy.getProbNet().getPNESupport().getCanUndo());
    }
}
