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
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An edit on a network opened in a tab can be undone from the start, also when the network is a copy
 * of another one, as an expanded network is.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ANetworkOpenedInATabCanUndoFromTheStartTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Tag(TestSpeed.MEDIUM)
    @Test void aCopyOfANetwork() throws Exception {
        ProbNet original = new ProbNet(BayesianNetworkType.getUniqueInstance());
        original.setName("original");
        Variable a = new Variable("A", "a0", "a1");
        Variable b = new Variable("B", "b0", "b1");
        original.addNode(a, NodeType.CHANCE);
        original.addNode(b, NodeType.CHANCE);
        ProbNet copy = original.copy();

        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(copy);
        try {
            new AddLinkEdit(copy, a, b, true).executeEdit();
            assertTrue(copy.getPNESupport().getCanUndo());
        } finally {
            mainPanel.forceClose(tab);
        }
    }
}
