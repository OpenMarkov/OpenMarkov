/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the probabilistic analysis of cost-effectiveness, hiding an intervention in one tab also unticks its check box in
 * the other tab, which hides it too (error 94).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheCheckBoxesOfBothTabsAgreeTest {

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    private CEProbabilisticDialog dialog;

    /** A test and a therapy with three options, evaluated by cost and effectiveness. */
    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-CEA-test-2therapies.pgmx");
        try (InputStream network = TheCheckBoxesOfBothTabsAgreeTest.class.getResourceAsStream(
                "/ID-CEA-test-2therapies.pgmx")) {
            Files.copy(network, file);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainGUI.INSTANCE.mainPanel.openNetwork(file.toString());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel();
        });
    }

    @AfterAll static void closeTheNetwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> tab.close());
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @AfterEach void closeTheDialog() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) {
                dialog.dispose();
            }
        });
    }

    @Test void hidingAnInterventionInThePlaneUnticksItInTheCurve() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<JCheckBox> plane = openTheDialog(), curve = plane.subList(3, 6);
            plane = plane.subList(0, 3);

            plane.get(1).doClick();

            assertEquals(List.of(true, false, true), ticked(plane));
            assertEquals(List.of(true, false, true), ticked(curve));

            plane.get(1).doClick();

            assertEquals(List.of(true, true, true), ticked(curve));
        });
    }

    @Test void hidingAnInterventionInTheCurveUnticksItInThePlane() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<JCheckBox> plane = openTheDialog(), curve = plane.subList(3, 6);
            plane = plane.subList(0, 3);

            curve.get(2).doClick();

            assertEquals(List.of(true, true, false), ticked(plane));
            assertEquals(List.of(true, true, false), ticked(curve));
        });
    }

    /**
     * Runs the analysis on the options of the therapy with a few simulations, and returns the check boxes of the plane
     * followed by those of the curve.
     */
    private List<JCheckBox> openTheDialog() {
        ProbNet net = tab.getProbNet();
        SensitivityAnalysisModel model = new SensitivityAnalysisModel();
        model.setDecisionVariable(net.getVariable("Therapy"));
        model.setNumberOfIterationsSimulations(20);
        model.setSeed(1L);
        try {
            dialog = new CEProbabilisticDialog(MainGUI.INSTANCE, net, new EvidenceCase(), model);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        List<JCheckBox> checkBoxes = new ArrayList<>();
        collect(dialog.getContentPane(), checkBoxes);
        assertEquals(List.of("no", "therapy 1", "therapy 2", "no", "therapy 1", "therapy 2"),
                     checkBoxes.stream().map(JCheckBox::getText).toList());
        return checkBoxes;
    }

    private static List<Boolean> ticked(List<JCheckBox> checkBoxes) {
        return checkBoxes.stream().map(JCheckBox::isSelected).toList();
    }

    private static void collect(Container container, List<JCheckBox> checkBoxes) {
        for (Component component : container.getComponents()) {
            if (component instanceof JCheckBox checkBox) {
                checkBoxes.add(checkBox);
            } else if (component instanceof Container inner) {
                collect(inner, checkBoxes);
            }
        }
    }
}
