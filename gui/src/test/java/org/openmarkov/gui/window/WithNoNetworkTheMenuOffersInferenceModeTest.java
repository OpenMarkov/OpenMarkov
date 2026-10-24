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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * With no network open, the Edit menu item that changes the working mode offers inference mode, the
 * one a network can go to when it opens, also after the last network was closed in inference mode.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class WithNoNetworkTheMenuOffersInferenceModeTest {

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
    @Test void afterClosingTheLastNetworkInInferenceMode() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("inference");
        net.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);

        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        MainPanelMenuAssistant menuAssistant = mainPanel.getMainPanelMenuAssistant();
        NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
        tab.setWorkingMode(NetworkEditorPanel.WorkingMode.INFERENCE);
        menuAssistant.updateOptionsNetworkDependent(tab);
        assertEquals("Switch to edition mode", mainPanel.getMainMenu().getSwitchWorkingMode().getText());

        mainPanel.forceClose(tab);
        menuAssistant.updateOptionsAllNetworkClosed();
        assertEquals("Switch to inference mode", mainPanel.getMainMenu().getSwitchWorkingMode().getText());
    }
}
