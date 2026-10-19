/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.full;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tools → «Cost effectiveness analysis» does what the button of the toolbar does, and is enabled when the button is.
 * It used to be enabled with any network open and to go always to the exact algorithms: with a network of events it
 * ended in «This algorithm cannot evaluate DESNet», while the button runs the simulation; with a Bayesian network it
 * showed an empty table (error 214).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheCostEffectivenessItemDoesWhatTheButtonDoesTest {

    private static final String COMMAND = "Tools.CostEffectivenessDeterministic";

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    private static AbstractButton button(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button && COMMAND.equals(button.getActionCommand())) {
                return button;
            }
            if (component instanceof Container inner && button(inner) != null) {
                return button(inner);
            }
        }
        return null;
    }

    private static JMenuItem item() {
        String text = StringDatabase.getUniqueInstance().getString("Tools.CostEffectiveness");
        JMenuBar menuBar = MainGUI.INSTANCE.mainPanel.getMainMenu();
        for (int menu = 0; menu < menuBar.getMenuCount(); menu++) {
            JMenu items = menuBar.getMenu(menu);
            for (int item = 0; items != null && item < items.getItemCount(); item++) {
                if (items.getItem(item) != null && text.equals(items.getItem(item).getText())) {
                    return items.getItem(item);
                }
            }
        }
        return null;
    }

    private static ProbNet net(NetworkType type, int criteria) {
        ProbNet net = new ProbNet(type);
        net.setName(type.getClass().getSimpleName() + " with " + criteria + " criteria");
        if (criteria == 2) {
            net.setDecisionCriteria(List.of(new Criterion("Cost"), new Criterion("Effectiveness")));
        }
        return net;
    }

    /** Whether the item is enabled with {@code net} open, after checking that the button is enabled alike. */
    private static boolean enabledWith(ProbNet net) throws Exception {
        AtomicReference<NetworkEditorPanel> tab = new AtomicReference<>();
        AtomicReference<Boolean> buttonEnabled = new AtomicReference<>();
        AtomicReference<Boolean> itemEnabled = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            tab.set(MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net));
            buttonEnabled.set(button(MainGUI.INSTANCE.mainPanel.getStandardToolBar()).isEnabled());
            itemEnabled.set(item().isEnabled());
        });
        SwingUtilities.invokeAndWait(() -> assertTrue(tab.get().close()));
        assertEquals(buttonEnabled.get(), itemEnabled.get(), net.getName());
        return itemEnabled.get();
    }

    @Test void itIsEnabledWhenTheButtonIs() throws Exception {
        assertFalse(enabledWith(net(BayesianNetworkType.getUniqueInstance(), 1)));
        assertFalse(enabledWith(net(DecisionAnalysisNetworkType.getUniqueInstance(), 1)));
        assertTrue(enabledWith(net(InfluenceDiagramType.getUniqueInstance(), 2)));
        assertTrue(enabledWith(net(DESNetworkType.getUniqueInstance(), 1)));
        SwingUtilities.invokeAndWait(() -> assertFalse(item().isEnabled(), "with no network open"));
    }

    @Test void itGoesTheWayOfTheButton() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JMenuItem item = item();
            assertNotNull(item);
            assertEquals(COMMAND, item.getActionCommand());
            assertTrue(Arrays.asList(item.getActionListeners())
                             .contains(MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant()));
        });
    }
}
