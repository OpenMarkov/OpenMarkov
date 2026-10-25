/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.decisiontree.DecisionTreeEditor;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With the tab of a decision tree in front, «Select all», the cost-effectiveness analysis and the sensitivity
 * analysis are disabled, as with no network open, and reaching them does nothing.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ADecisionTreeTabDisablesWhatActsOnANetworkTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void costEffectivenessAndSelectAll() throws Exception {
        withTheTreeOf("/ID-CEA-minimal.pgmx", ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, () -> {
            assertFalse(selectAll().isEnabled());
            assertFalse(button(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC).isEnabled());
            assertDoesNotThrow(() -> act(ActionCommands.SELECT_ALL));
            assertDoesNotThrow(() -> act(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC));
        });
    }

    @Test void sensitivityAnalysis() throws Exception {
        withTheTreeOf("/ID-one-utility-sa.pgmx", ActionCommands.SENSITIVITY_ANALYSIS,
                      () -> assertFalse(button(ActionCommands.SENSITIVITY_ANALYSIS).isEnabled()));
    }

    /**
     * Opens {@code resource}, checks that {@code enabled} is enabled on its tab, and runs {@code check} with the tab
     * of its decision tree in front.
     */
    private static void withTheTreeOf(String resource, ActionCommands enabled, Runnable check) throws Exception {
        ProbNet net = new PGMXReader().read(ADecisionTreeTabDisablesWhatActsOnANetworkTest.class.getResource(resource))
                                      .probNet();
        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        SwingUtilities.invokeAndWait(() -> {
            NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
            try {
                assertTrue(selectAll().isEnabled());
                assertTrue(button(enabled).isEnabled());
                DecisionTreeEditor tree = new DecisionTreeEditor(tab);
                mainPanel.addCloseableTab("Decision tree", tree);
                mainPanel.getTabPanels().setSelectedComponent(tree);
                try {
                    check.run();
                } finally {
                    mainPanel.getTabPanels().remove(tree);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                mainPanel.forceClose(tab);
            }
        });
    }

    private static JMenuItem selectAll() {
        JMenu edit = MainGUI.INSTANCE.mainPanel.getMainMenu().getMenu(1);
        for (Component component : edit.getMenuComponents()) {
            if (component instanceof JMenuItem item
                    && ActionCommands.SELECT_ALL.getCommandName().equals(item.getActionCommand())) {
                return item;
            }
        }
        throw new AssertionError("The Edit menu has no «Select all»");
    }

    private static AbstractButton button(ActionCommands command) {
        AbstractButton button = button(MainGUI.INSTANCE.mainPanel.getStandardToolBar(), command.getCommandName());
        assertNotNull(button, command.getCommandName());
        return button;
    }

    private static AbstractButton button(Container container, String command) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button && command.equals(button.getActionCommand())) {
                return button;
            }
            if (component instanceof Container inner && button(inner, command) != null) {
                return button(inner, command);
            }
        }
        return null;
    }

    private static void act(ActionCommands command) {
        MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant()
                                  .actionPerformed(new ActionEvent(selectAll(), ActionEvent.ACTION_PERFORMED,
                                                                   command.getCommandName()));
    }
}
