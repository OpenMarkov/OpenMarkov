/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import networks.Networks;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.dialog.MapDialog;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Container;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Map (two-way analysis)» of ID-decide-test with decision scope: the first entry of the legend is the draw, painted
 * where the value is −1, and the next ones are the options of the decision, painted where the value is their index.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
class EachOptionOfTheMapHasTheColourOfItsLegendTest {

    private static AxisVariation aQuarterAround() {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(DeterministicAxisVariationType.PORV);
        variation.setVariationValue(25);
        return variation;
    }

    private static List<UncertainParameter> parameter(ProbNet probNet, String name) {
        return List.of(SystematicSampling.getUncertainParameters(probNet).stream()
                                         .filter(parameter -> name.equals(parameter.getName()))
                                         .findFirst()
                                         .orElseThrow());
    }

    private static JFreeChart chartOf(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof ChartPanel chartPanel) {
                return chartPanel.getChart();
            }
            if (component instanceof Container inner) {
                JFreeChart chart = chartOf(inner);
                if (chart != null) {
                    return chart;
                }
            }
        }
        return null;
    }

    @Test
    void theDrawAndEachOption() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/networks/id/ID-decide-test.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet probNet = new PGMXReader().read(url).probNet();
        SensitivityAnalysisModel model = new SensitivityAnalysisModel();
        model.setSelectedUncertainParametersXAxis(parameter(probNet, "prevalence"));
        model.setSelectedUncertainParametersYAxis(parameter(probNet, "sensitivity"));
        model.setHorizontalAxisVariation(aQuarterAround());
        model.setVerticalAxisVariation(aQuarterAround());
        model.setNumberOfIterationsSimulations(4);
        model.setDecisionVariable(probNet.getVariable("Do test?"));
        MapDialog dialog = new MapDialog(new JFrame(), probNet, new EvidenceCase(), model);
        JFreeChart chart = chartOf(dialog.getContentPane());
        dialog.dispose();

        XYPlot plot = chart.getXYPlot();
        PaintScale scale = ((XYBlockRenderer) plot.getRenderer()).getPaintScale();
        LegendItemCollection legend = plot.getFixedLegendItems();
        assertEquals(3, legend.getItemCount());
        for (int item = 0; item < legend.getItemCount(); item++) {
            assertEquals(legend.get(item).getFillPaint(), scale.getPaint(item - 1), legend.get(item).getLabel());
        }
    }
}
