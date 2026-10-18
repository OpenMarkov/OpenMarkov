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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.dialog.SensitivityAnalysisDialog;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the sensitivity analysis with the scope of one decision, the analysis gets the scenario chosen for that decision,
 * and the scenario of another decision is free afterwards, even when the analysis fails.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class AFailedSensitivityAnalysisLeavesTheScenarioFreeTest {

    private static boolean ignoreStorage;

    private NetworkEditorPanel tab;
    private SensitivityAnalysisDialog dialog;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @AfterEach void closeTheWindows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) {
                dialog.dispose();
            }
            MainGUI.INSTANCE.mainPanel.forceClose(tab);
        });
    }

    /** The rows of the box «Scenario», with ! after a selector that cannot be changed. */
    private static List<String> scenario(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel box && box.getBorder() instanceof TitledBorder border
                    && border.getTitle().equals("Scenario")) {
                List<String> rows = new ArrayList<>();
                for (Component row : box.getComponents()) {
                    if (row instanceof JPanel line && line.getComponentCount() == 2
                            && line.getComponent(0) instanceof JLabel label
                            && line.getComponent(1) instanceof JComboBox<?> selector) {
                        rows.add(label.getText() + "=" + selector.getSelectedItem() + (selector.isEnabled() ? "" : "!"));
                    }
                }
                return rows;
            }
            if (component instanceof Container inner) {
                List<String> rows = scenario(inner);
                if (rows != null) {
                    return rows;
                }
            }
        }
        return null;
    }

    /** The selector that offers {@code item}, set to it. */
    private void choose(String item) throws Exception {
        SwingUtilities.invokeAndWait(() -> chooseIn(dialog.getContentPane(), item));
    }

    private static boolean chooseIn(Container container, String item) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> box) {
                for (int i = 0; i < box.getItemCount(); i++) {
                    if (String.valueOf(box.getItemAt(i)).equals(item)) {
                        box.setSelectedIndex(i);
                        return true;
                    }
                }
            } else if (component instanceof Container inner && chooseIn(inner, item)) {
                return true;
            }
        }
        return false;
    }

    /** The selector of the scenario after the label {@code name}, set to {@code state}. */
    private void chooseIn(String name, String state) throws Exception {
        SwingUtilities.invokeAndWait(() -> chooseInRow(dialog.getContentPane(), name, state));
    }

    private static void chooseInRow(Container container, String name, String state) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel row && row.getComponentCount() == 2
                    && row.getComponent(0) instanceof JLabel label && label.getText().equals(name)
                    && row.getComponent(1) instanceof JComboBox<?> box) {
                box.setSelectedItem(state);
            } else if (component instanceof Container inner) {
                chooseInRow(inner, name, state);
            }
        }
    }

    @Test void theScenarioOfAnotherDecisionIsFreeAfterAFailedAnalysis() throws Exception {
        ProbNet net = new PGMXReader().read(Networks.getNetworks()
                                                    .filter(u -> u.getPath().endsWith("/id/ID-CEA-test-2therapies-new-test.pgmx"))
                                                    .findFirst().orElseThrow()).probNet();
        List<String> analysed = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
            tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
            SensitivityAnalysisController controller = new SensitivityAnalysisController(MainGUI.INSTANCE) {
                @Override public void runAnalysis(EvidenceCase evidence) {
                    evidence.getFindings().forEach(finding -> analysed.add(finding.getVariable().getName() + "="
                                                                           + finding.getState()));
                    throw new IllegalArgumentException("The analysis fails");
                }
            };
            dialog = new SensitivityAnalysisDialog(MainGUI.INSTANCE, controller);
        });
        choose("One decision");
        choose("Therapy");
        chooseIn("Dec: Test", "yes");
        chooseIn("Test", "positive");

        InvocationTargetException failure = assertThrows(InvocationTargetException.class,
                                                         () -> SwingUtilities.invokeAndWait(() -> dialog.getOKButton().doClick()));
        assertEquals("The analysis fails", failure.getCause().getMessage());
        assertTrue(analysed.containsAll(List.of("Dec: Test=yes", "Test=positive")), analysed.toString());

        choose("Dec: New test");
        assertEquals(List.of("Dec: Test=no", "Test=not done"), scenario(dialog.getContentPane()));
    }
}
