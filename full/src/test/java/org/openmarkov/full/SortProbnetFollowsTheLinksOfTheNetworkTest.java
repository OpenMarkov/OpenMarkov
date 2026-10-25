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
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tools → «Sort probnet» is enabled when the Tools menu opens on a network with links, and disabled on one without
 * them, however the network got or lost its links.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class SortProbnetFollowsTheLinksOfTheNetworkTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** Whether «Sort probnet» is enabled after opening and closing the Tools menu. */
    private static boolean enabledWhenTheMenuOpens() {
        JMenuBar menuBar = MainGUI.INSTANCE.mainPanel.getMainMenu();
        for (int menu = 0; menu < menuBar.getMenuCount(); menu++) {
            JMenu items = menuBar.getMenu(menu);
            if (items == null) {
                continue;
            }
            items.setSelected(true);
            items.setSelected(false);
            for (int item = 0; item < items.getItemCount(); item++) {
                JMenuItem entry = items.getItem(item);
                if (entry != null && "Sort probnet".equals(entry.getText())) {
                    return entry.isEnabled();
                }
            }
        }
        return fail("There is no «Sort probnet»");
    }

    @Test void itFollowsTheLinksOfTheNetwork() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("links");
        Variable a = net.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE).getVariable();
        Variable b = net.addNode(new Variable("B", "b0", "b1"), NodeType.CHANCE).getVariable();
        AtomicReference<NetworkEditorPanel> tab = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            tab.set(MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net));
            assertFalse(enabledWhenTheMenuOpens(), "a network born without links");
            try {
                new AddLinkEdit(net, a, b, true).executeEdit();
                assertTrue(enabledWhenTheMenuOpens(), "after drawing the first link");
                new RemoveLinkEdit(net, a, b, true).executeEdit();
                assertFalse(enabledWhenTheMenuOpens(), "after removing the only link");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        SwingUtilities.invokeAndWait(() -> MainGUI.INSTANCE.mainPanel.forceClose(tab.get()));
    }
}
