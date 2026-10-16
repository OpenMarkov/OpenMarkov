/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.dialog.node.PotentialEditDialog;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The windows "Show expected utility" and "Show optimal policy" of a decision are titled with
 * their name and the name of the decision.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialogs need a screen")
class TheWindowsOfADecisionAreTitledWithItsNameTest {

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
    @Test void expectedUtility() throws Exception {
        assertEquals("Expected utility: Therapy", titleOfTheWindowOpenedBy(NetworkEditorPanel::showExpectedUtilityOfNode));
    }

    @Tag(TestSpeed.MEDIUM)
    @Test void optimalPolicy() throws Exception {
        assertEquals("Optimal policy: Therapy", titleOfTheWindowOpenedBy(NetworkEditorPanel::showOptimalPolicyOfNode));
    }

    private interface Show {
        void on(NetworkEditorPanel tab) throws Exception;
    }

    /** Selects the decision, asks for the window, and closes it once it is showing. */
    private static String titleOfTheWindowOpenedBy(Show show) throws Exception {
        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(anInfluenceDiagram());
        try {
            tab.getVisualNetwork().setSelectedNode("Therapy", true);
            SwingUtilities.invokeLater(() -> {
                try {
                    show.on(tab);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            PotentialEditDialog dialog = theShowingDialog();
            assertNotNull(dialog, "the window did not open");
            String[] title = new String[1];
            SwingUtilities.invokeAndWait(() -> {
                title[0] = dialog.getTitle();
                dialog.dispose();
            });
            return title[0];
        } finally {
            SwingUtilities.invokeAndWait(() -> mainPanel.forceClose(tab));
        }
    }

    private static PotentialEditDialog theShowingDialog() throws Exception {
        PotentialEditDialog[] found = new PotentialEditDialog[1];
        long end = System.currentTimeMillis() + 10_000;
        while (found[0] == null && System.currentTimeMillis() < end) {
            Thread.sleep(100);
            SwingUtilities.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) {
                    if (window instanceof PotentialEditDialog dialog && dialog.isShowing()) {
                        found[0] = dialog;
                    }
                }
            });
        }
        return found[0];
    }

    /** A decision "Therapy" and a utility that depends on it. */
    private static ProbNet anInfluenceDiagram() {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.setName("decision");
        Variable therapy = new Variable("Therapy", "no", "yes");
        Variable cost = new Variable("Cost");
        net.addNode(therapy, NodeType.DECISION);
        therapy.setDecisionCriterion(new Criterion());
        net.addNode(cost, NodeType.UTILITY);
        cost.setDecisionCriterion(new Criterion());
        net.addLink(therapy, cost, true);
        net.getNode(cost).setPotential(new ExactDistrPotential(List.of(cost, therapy), PotentialRole.UNSPECIFIED, new double[]{0.0, -0.25}));
        return net;
    }
}
