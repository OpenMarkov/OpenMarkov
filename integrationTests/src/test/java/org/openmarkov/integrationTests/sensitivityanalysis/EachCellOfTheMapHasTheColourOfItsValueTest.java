/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import networks.Networks;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYZDataset;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Map (two-way analysis)» of ID-decide-test with prevalence and sensitivity, a variation of 25 % and 50 points in
 * each parameter: «Do test? = no» wins in 15 cells of a corner and «yes» in the rest. The chart is painted into an
 * image and the centre of each cell must have the colour of its value.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
class EachCellOfTheMapHasTheColourOfItsValueTest {

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

    @ParameterizedTest
    @CsvSource({ "Do test?", "''" })
    void inBothScopes(String decision) throws Exception {
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
        model.setNumberOfIterationsSimulations(50);
        if (!decision.isEmpty()) {
            model.setDecisionVariable(probNet.getVariable(decision));
        }
        MapDialog dialog = new MapDialog(new JFrame(), probNet, new EvidenceCase(), model);
        JFreeChart chart = chartOf(dialog.getContentPane());
        dialog.dispose();

        BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo();
        chart.draw(graphics, new Rectangle2D.Double(0, 0, 800, 800), info);
        graphics.dispose();
        Rectangle2D dataArea = info.getPlotInfo().getDataArea();
        XYPlot plot = chart.getXYPlot();
        PaintScale scale = ((XYBlockRenderer) plot.getRenderer()).getPaintScale();
        XYZDataset cells = (XYZDataset) plot.getDataset();

        int wrongCells = 0;
        for (int cell = 0; cell < cells.getItemCount(0); cell++) {
            int x = (int) plot.getDomainAxis().valueToJava2D(cells.getXValue(0, cell), dataArea, RectangleEdge.BOTTOM);
            int y = (int) plot.getRangeAxis().valueToJava2D(cells.getYValue(0, cell), dataArea, RectangleEdge.LEFT);
            Color expected = (Color) scale.getPaint(cells.getZValue(0, cell));
            if ((image.getRGB(x, y) & 0xFFFFFF) != (expected.getRGB() & 0xFFFFFF)) {
                wrongCells++;
            }
        }
        assertEquals(0, wrongCells, "cells painted with another colour, of " + cells.getItemCount(0));
    }
}
