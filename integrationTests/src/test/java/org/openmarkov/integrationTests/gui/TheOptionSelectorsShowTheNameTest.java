/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.costEffectiveness.CEDecisionResults;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.dialog.CEProbabilisticDialog;
import org.openmarkov.sensitivityanalysis.dialog.TornadoSpiderDialog;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The selectors of an option of the decision in the cost-effectiveness results, in the probabilistic analysis and in
 * the tornado and spider diagrams show the name of the option, not «state no».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialogs need a screen")
class TheOptionSelectorsShowTheNameTest {

    private static boolean ignoreStorage;

    private static ProbNet net;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void readTheNetwork() throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        net = new PGMXReader().read(IntegrationTest.class.getResource("/networks/id/ID-CEA-test-2therapies.pgmx"))
                              .probNet();
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void theCostEffectivenessResults() throws Exception {
        assertEachOptionShowsItsName(() -> new CEDecisionResults(MainGUI.INSTANCE, net, new EvidenceCase(),
                                                                 net.getVariable("Therapy")));
    }

    @Test void theProbabilisticAnalysis() throws Exception {
        assertEachOptionShowsItsName(() -> {
            SensitivityAnalysisModel model = theModel();
            model.setNumberOfIterationsSimulations(20);
            model.setSeed(1L);
            return new CEProbabilisticDialog(MainGUI.INSTANCE, net, new EvidenceCase(), model);
        });
    }

    @Test void theTornadoAndTheSpider() throws Exception {
        assertEachOptionShowsItsName(() -> {
            SensitivityAnalysisModel model = theModel();
            model.setSelectedUncertainParametersXAxis(SystematicSampling.getUncertainParameters(net).stream()
                                                                       .filter(parameter -> parameter.getName() != null)
                                                                       .toList());
            AxisVariation variation = new AxisVariation();
            variation.setVariationType(DeterministicAxisVariationType.POPP);
            variation.setVariationValue(20);
            model.setHorizontalAxisVariation(variation);
            model.setNumberOfIterationsSimulations(2);
            return new TornadoSpiderDialog(MainGUI.INSTANCE, net, new EvidenceCase(), model);
        });
    }

    private static SensitivityAnalysisModel theModel() {
        SensitivityAnalysisModel model = new SensitivityAnalysisModel();
        model.setDecisionVariable(net.getVariable("Therapy"));
        return model;
    }

    private interface DialogMaker {
        JDialog make() throws Exception;
    }

    /** Every selector of an option of the dialog shows, for its first option, the name of that option. */
    private static void assertEachOptionShowsItsName(DialogMaker maker) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JDialog dialog;
            try {
                dialog = maker.make();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            try {
                List<JComboBox<?>> selectors = new ArrayList<>();
                collect(dialog.getContentPane(), selectors);
                selectors.removeIf(comboBox -> comboBox.getItemCount() == 0 || !(comboBox.getItemAt(0) instanceof State));
                assertFalse(selectors.isEmpty(), "the dialog has no selector of an option");
                for (JComboBox<?> comboBox : selectors) {
                    State first = (State) comboBox.getItemAt(0);
                    @SuppressWarnings("unchecked") var renderer = (ListCellRenderer<Object>) comboBox.getRenderer();
                    Component cell = renderer.getListCellRendererComponent(new JList<>(), first, 0, false, false);
                    assertEquals(first.getName(), ((JLabel) cell).getText());
                }
            } finally {
                dialog.dispose();
            }
        });
    }

    private static void collect(Container container, List<JComboBox<?>> found) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> comboBox) {
                found.add(comboBox);
            } else if (component instanceof Container inner) {
                collect(inner, found);
            }
        }
    }
}
