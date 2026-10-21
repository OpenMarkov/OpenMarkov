/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * With a user defined interval that does not reach zero, the axes of the map of two parameters reached it, and left
 * between zero and the cells an empty strip painted with the background of the plot (error 255). Each axis now goes
 * from half a cell before the first to half a cell after the last.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheAxesOfTheMapSpanItsCellsTest {

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    /** A chance node and a utility, with the prevalence and the utility if present as named uncertain parameters. */
    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-one-chance-sa.pgmx");
        try (InputStream network = TheAxesOfTheMapSpanItsCellsTest.class.getResourceAsStream(
                "/ID-one-chance-sa.pgmx")) {
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

    /** The prevalence from 0.1 to 0.2 and the utility from 4 to 6, in four intervals each. */
    @Test void eachAxisGoesFromHalfACellBeforeTheFirstToHalfACellAfterTheLast() throws Exception {
        double[][] axes = new double[2][];
        SwingUtilities.invokeAndWait(() -> {
            ProbNet net = tab.getProbNet();
            SensitivityAnalysisModel model = new SensitivityAnalysisModel();
            model.setSelectedUncertainParametersXAxis(List.of(parameter(net, "prevalence")));
            model.setSelectedUncertainParametersYAxis(List.of(parameter(net, "utilpresent")));
            model.setHorizontalAxisVariation(interval(0.1, 0.2));
            model.setVerticalAxisVariation(interval(4, 6));
            model.setNumberOfIterationsSimulations(4);
            MapDialog dialog;
            try {
                dialog = new MapDialog(MainGUI.INSTANCE, net, new EvidenceCase(), model);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            try {
                XYPlot plot = chartPanel(dialog.getContentPane()).getChart().getXYPlot();
                axes[0] = bounds(plot.getDomainAxis());
                axes[1] = bounds(plot.getRangeAxis());
            } finally {
                dialog.dispose();
            }
        });

        assertEquals(0.1 - 0.0125, axes[0][0], 1e-9);
        assertEquals(0.2 + 0.0125, axes[0][1], 1e-9);
        assertEquals(4 - 0.25, axes[1][0], 1e-9);
        assertEquals(6 + 0.25, axes[1][1], 1e-9);
    }

    private static UncertainParameter parameter(ProbNet net, String name) {
        return SystematicSampling.getUncertainParameters(net).stream()
                                 .filter(parameter -> name.equals(parameter.getName())).findFirst().orElseThrow();
    }

    private static AxisVariation interval(double lower, double upper) {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(DeterministicAxisVariationType.UDIN);
        variation.setVariationBounds(new double[]{lower, upper});
        return variation;
    }

    private static double[] bounds(ValueAxis axis) {
        return new double[]{axis.getLowerBound(), axis.getUpperBound()};
    }

    private static ChartPanel chartPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof ChartPanel chartPanel) {
                return chartPanel;
            }
            if (component instanceof Container inner) {
                ChartPanel found = chartPanel(inner);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
