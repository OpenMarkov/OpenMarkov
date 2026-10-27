/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With a ratio over the reference value or a percentage of the probability, the spider measures the variation of
 * each parameter in proportion to its reference value. A parameter whose reference value is 0 had no line with the
 * ratio and, with the percentage, made the axis infinite, so that the spider showed no line at all (error 317 of the
 * pending list). It is now left out of the spider, which names it below its title, and the other parameters are drawn
 * as usual. In the network, «prevalence» has reference value 0.14 and «utilpresent», a normal of mean 0, has 0.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The spider is a window")
class AParameterOfReferenceValueZeroIsLeftOutOfTheSpiderTest {

    @ParameterizedTest
    @CsvSource({ "RORV, 1.25, prevalence", "POPP, 90, prevalence", "PORV, 25, prevalence utilpresent" })
    void theOtherParametersAreDrawnAndItIsNamed(DeterministicAxisVariationType type, double value, String shown)
            throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            ProbNet probNet = new PGMXReader().read(
                    getClass().getResource("/ID-a-utility-of-mean-zero-sa.pgmx")).probNet();
            SensitivityAnalysisModel model = new SensitivityAnalysisModel();
            // The named parameters, as the window of the analysis offers them
            model.setSelectedUncertainParametersXAxis(new ArrayList<>(
                    SensitivityAnalysisController.byLabel(SystematicSampling.getUncertainParameters(probNet)).values()));
            AxisVariation variation = new AxisVariation();
            variation.setVariationType(type);
            variation.setVariationValue(value);
            model.setHorizontalAxisVariation(variation);
            model.setNumberOfIterationsSimulations(10);
            JFreeChart[] spider = new JFreeChart[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    TornadoSpiderDialog dialog = new TornadoSpiderDialog(MainGUI.INSTANCE, probNet, new EvidenceCase(),
                                                                         model);
                    spider[0] = dialog.getSpiderChartPanel().getChart();
                    dialog.dispose();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });

            XYPlot plot = spider[0].getXYPlot();
            XYDataset lines = plot.getDataset();
            List<String> drawn = new ArrayList<>();
            for (int line = 0; line < lines.getSeriesCount(); line++) {
                drawn.add(lines.getSeriesKey(line).toString());
            }
            assertEquals(List.of(shown.split(" ")), drawn);
            assertTrue(Double.isFinite(plot.getDomainAxis().getLowerBound())
                       && Double.isFinite(plot.getDomainAxis().getUpperBound()),
                       "The axis goes from " + plot.getDomainAxis().getLowerBound() + " to "
                               + plot.getDomainAxis().getUpperBound());
            List<String> notes = new ArrayList<>();
            for (int subtitle = 0; subtitle < spider[0].getSubtitleCount(); subtitle++) {
                if (spider[0].getSubtitle(subtitle) instanceof TextTitle note) {
                    notes.add(note.getText());
                }
            }
            assertEquals(drawn.contains("utilpresent") ? List.of()
                                 : List.of("Not shown, as their reference value is 0: utilpresent"), notes);
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
