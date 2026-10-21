/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.CategoryItemEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tornado diagram gave its axis of parameters a negative margin from 16 parameters on, so the bars at its ends,
 * those of the parameters with the largest effect, were drawn out of the chart, and from 20 on some were not seen at
 * all (error 91). Every bar is now drawn inside the chart, apart from the others.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class EveryBarOfTheTornadoIsDrawnInsideItsChartTest {

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    /** A chance node of thirty states and a utility whose thirty values are named uncertain parameters. */
    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-thirty-parameters-sa.pgmx");
        try (InputStream network = EveryBarOfTheTornadoIsDrawnInsideItsChartTest.class.getResourceAsStream(
                "/ID-thirty-parameters-sa.pgmx")) {
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

    @ParameterizedTest @ValueSource(ints = {5, 15, 16, 20, 30})
    void everyBarIsInsideTheChartAndApartFromTheOthers(int parameters) throws Exception {
        List<Rectangle2D> bars = new ArrayList<>();
        Rectangle2D[] chart = new Rectangle2D[1];
        SwingUtilities.invokeAndWait(() -> {
            TornadoSpiderDialog dialog = openTheTornado(parameters);
            try {
                ChartRenderingInfo info = new ChartRenderingInfo();
                dialog.getTornadoChart().getChart().createBufferedImage(600, 400, info);
                chart[0] = info.getPlotInfo().getDataArea();
                for (Object entity : info.getEntityCollection().getEntities()) {
                    if (entity instanceof CategoryItemEntity bar) {
                        bars.add(bar.getArea().getBounds2D());
                    }
                }
            } finally {
                dialog.dispose();
            }
        });

        assertEquals(parameters, bars.size());
        bars.sort(Comparator.comparingDouble(Rectangle2D::getMinY));
        for (Rectangle2D bar : bars) {
            assertTrue(bar.getMinY() >= chart[0].getMinY() && bar.getMaxY() <= chart[0].getMaxY(),
                       "a bar from " + bar.getMinY() + " to " + bar.getMaxY() + " is out of the chart, from "
                               + chart[0].getMinY() + " to " + chart[0].getMaxY());
        }
        for (int i = 1; i < bars.size(); i++) {
            assertTrue(bars.get(i).getMinY() > bars.get(i - 1).getMaxY(),
                       "the bars ending at " + bars.get(i - 1).getMaxY() + " and starting at "
                               + bars.get(i).getMinY() + " touch");
        }
    }

    /** The tornado of the first {@code parameters} parameters of the utility, each varied by 20 %. */
    private static TornadoSpiderDialog openTheTornado(int parameters) {
        ProbNet net = tab.getProbNet();
        List<UncertainParameter> selected = new ArrayList<>(SystematicSampling.getUncertainParameters(net));
        selected.sort(Comparator.comparing(UncertainParameter::getName));
        SensitivityAnalysisModel model = new SensitivityAnalysisModel();
        model.setSelectedUncertainParametersXAxis(selected.subList(0, parameters));
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(DeterministicAxisVariationType.POPP);
        variation.setVariationValue(20);
        model.setHorizontalAxisVariation(variation);
        model.setNumberOfIterationsSimulations(2);
        try {
            return new TornadoSpiderDialog(MainGUI.INSTANCE, net, new EvidenceCase(), model);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
