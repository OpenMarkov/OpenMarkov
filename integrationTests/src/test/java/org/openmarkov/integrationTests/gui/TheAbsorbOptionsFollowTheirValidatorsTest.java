/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import networks.Networks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.menutoolbar.menu.NodeContextualMenu;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The menu of a node offers «Absorb this node» and «Absorb parents» only where the validator of each one accepts the
 * node (error 301). «Absorb this node» no longer follows the rule of issue 513, which asked every parent for a table
 * and so turned it off on the results of DAN-reactor, whose parent is a decision, though they can be absorbed.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheAbsorbOptionsFollowTheirValidatorsTest {

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

    /** Whether the option with the action command {@code command} of the menu of {@code node} in {@code network} is enabled. */
    private boolean isEnabled(String network, String node, String command) throws Exception {
        ProbNet net = new PGMXReader().read(Networks.getNetworks().filter(u -> u.getPath().endsWith(network))
                                                    .findFirst().orElseThrow()).probNet();
        AtomicReference<Boolean> enabled = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
            VisualNode visualNode = tab.getVisualNetwork().getAllNodes().stream()
                                       .filter(visual -> visual.getNode().getName().equals(node))
                                       .findFirst().orElseThrow();
            NodeContextualMenu menu = new NodeContextualMenu(_ -> { }, visualNode, tab);
            for (Component component : menu.getComponents()) {
                if (component instanceof JMenuItem item && command.equals(item.getActionCommand())) {
                    enabled.set(item.isEnabled());
                }
            }
        });
        return enabled.get();
    }

    @ParameterizedTest(name = "{1} of {0}: {2}")
    @CsvSource({
            "/bn/BN-asia.pgmx, Smoker, false",
            "/bn/BN-asia.pgmx, Dyspnea, false",
            "/id/ID-CEA-minimal.pgmx, Therapy, true",
            "/dan/DAN-reactor.pgmx, Result of advanced reactor, true",
    })
    void absorbThisNode(String network, String node, boolean enabled) throws Exception {
        assertEquals(enabled, isEnabled(network, node, "AbsorbNode"));
    }

    @ParameterizedTest(name = "{1} of {0}: {2}")
    @CsvSource({
            "/bn/BN-asia.pgmx, Tuberculosis, false",
            "/id/ID-used-car-buyer.pgmx, Total, true",
    })
    void absorbParents(String network, String node, boolean enabled) throws Exception {
        assertEquals(enabled, isEnabled(network, node, "AbsorbParents"));
    }
}
