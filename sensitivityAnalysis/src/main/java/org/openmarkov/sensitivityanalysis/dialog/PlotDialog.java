/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.TextAnchor;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.inference.common.ScopeType;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnPlot;
import org.openmarkov.sensitivityanalysis.model.AnalysisType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Dialog that shows Plot sensitivity analysis
 *
 * @author jperez-martin
 */
public class PlotDialog extends JDialog {
    
    /**
     * Sensitivity Analysis Plot task
     */
    private VESensAnPlot veSensAnPlot;
    
    /**
     * Uncertain parameter
     */
    private UncertainParameter uncertainParameter;
    
    /**
     * Utility of reference (base case, without uncertainty)
     */
    private double utilityReference;
    
    /**
     * Axis variation of x axis
     */
    private AxisVariation axisVariation;
    
    /**
     * Number of points/iterations in the sensitivity range of each parameter
     */
    private int iterations;
    
    /**
     * Conditioned decision variable for the decision scope
     */
    private Variable decisionVariable;
    
    /**
     * Selected scenario for the selected decision
     */
    private List<Finding> selectedScenario;
    
    /**
     * ProbNet
     */
    private ProbNet probNet;
    
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * Dialog for plot analysis
     *
     * @param owner                    Owner window
     * @param probNet                  ProbNet
     * @param preResolutionEvidence    PreResolutionEvidence (included the scenario if applies)
     * @param sensitivityAnalysisModel Sensitivity model parameters
     */
    public PlotDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence,
                      SensitivityAnalysisModel sensitivityAnalysisModel) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(owner);
        this.probNet = probNet;
        this.uncertainParameter = sensitivityAnalysisModel.getSelectedUncertainParametersXAxis().get(0);
        this.axisVariation = sensitivityAnalysisModel.getHorizontalAxisVariation();
        this.iterations = sensitivityAnalysisModel.getNumberOfIterationsSimulations();
        this.decisionVariable = sensitivityAnalysisModel.getDecisionVariable();
        this.selectedScenario = sensitivityAnalysisModel.getSelectedScenario();
        
        // Run the task without uncertainty and set the utility reference
        VEEvaluation veEvaluation = new VEEvaluation(probNet);
        veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
        this.utilityReference = veEvaluation.getUtility().getValues()[0];
        
        // Run the task with uncertainty
        veSensAnPlot = new VESensAnPlot(probNet, preResolutionEvidence, uncertainParameter, axisVariation,
                                        iterations, decisionVariable);
        
        initialize();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle bounds = owner.getBounds();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;
        // center point of the owner window
        int x = bounds.x / 2 - width / 2;
        int y = bounds.y / 2 - height / 2;
        this.setBounds(x, y, width, height);
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(width, height / 2));
        setResizable(true);
        repaint();
        pack();
        this.setVisible(true);
    }
    
    /**
     * Set title, icon and contentPane
     */
    private void initialize() {
        this.setTitle(
                "OpenMarkov - " + StringDatabase.getUniqueInstance().getString("SensitivityAnalysis.Title") + " - "
                        + probNet.getName());
        this.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
        setContentPane(getJContentPane());
        pack();
    }
    
    /**
     * Gets the content pane with all panels
     *
     * @return
     */
    private JPanel getJContentPane() {
        JPanel jContentPane = new JPanel();
        jContentPane.setLayout(new BorderLayout());
        jContentPane.add(getPlotPanel(), BorderLayout.CENTER);
        return jContentPane;
    }
    
    /**
     * Gets a JPanel with the chart
     *
     * @return
     */
    private JPanel getPlotPanel() {
        JFreeChart plotChart = getPlotChart();
        
        return new ChartPanel(plotChart);
    }
    
    /**
     * Build the chart
     *
     * @return
     */
    private JFreeChart getPlotChart() {
        // JFreeChart attributes definition
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        // Set the domain axis format
        DecimalFormat decimalFormat = null;
        if (axisVariation.getVariationType() == DeterministicAxisVariationType.PORV || axisVariation
                .getVariationType() == DeterministicAxisVariationType.POPP) {
            decimalFormat = new DecimalFormat("+##.##%;-##.##%");
        } else if (axisVariation.getVariationType() == DeterministicAxisVariationType.RORV || axisVariation
                .getVariationType() == DeterministicAxisVariationType.UDIN) {
            decimalFormat = new DecimalFormat("0.000;-0.000");
        }
        
        // Get the minimum and maximum values of variation to define horizontal intervals
        double minVariationValue, maxVariationValue;
        if (axisVariation.getVariationType() == DeterministicAxisVariationType.UDIN) {
            minVariationValue = axisVariation.getVariationBounds()[0];
            maxVariationValue = axisVariation.getVariationBounds()[1];
        } else {
            minVariationValue = -axisVariation.getVariationValue() / 100;
            maxVariationValue = +axisVariation.getVariationValue() / 100;
        }
        
        // Size of every horizontal interval
        double variationInterval = (maxVariationValue - minVariationValue) / iterations;
        
        double minRangeUtility = Double.MAX_VALUE;
        double maxRangeUtility = Double.MIN_VALUE;
        
        TablePotential uncertainParameterPotential = veSensAnPlot.getUncertainParametersPotentials()
                                                                 .get(uncertainParameter);
        
        int decisionVariableStates = 0;
        if (decisionVariable != null) {
            decisionVariableStates += decisionVariable.getNumStates();
        } else {
            decisionVariableStates = 1;
        }
        
        // If we have the result conditioned on a decision, we need to paint one series for each decision state
        for (int decisionStateIndex = 0; decisionStateIndex < decisionVariableStates; decisionStateIndex++) {
            XYSeries series;
            if (decisionVariable != null) {
                series = new XYSeries(
                        decisionVariable.getName() + " = " + decisionVariable.getStateName(decisionStateIndex));
            } else {
                series = new XYSeries(uncertainParameter.getName());
            }
            
            int seriesIndex = 0;
            // Get the count of the iteration
            int horizontalIteration = 0;
            for (int valueIndex = 0; valueIndex <= iterations; valueIndex++) {
                
                int globalValueIndex = valueIndex + decisionStateIndex * (iterations + 1);
                double value = uncertainParameterPotential.getValues()[globalValueIndex];
                // Update minimum utility found
                if (value < minRangeUtility) {
                    minRangeUtility = value;
                }
                
                // Update maximum utility found
                if (value > maxRangeUtility) {
                    maxRangeUtility = value;
                }
                
                double variationValue = minVariationValue + (variationInterval * horizontalIteration);
                series.add(variationValue, value);
                
                horizontalIteration++;
            }
            dataset.addSeries(series);
            
            // Set series presentation settings
            renderer.setSeriesShapesVisible(seriesIndex, false);
            renderer.setSeriesStroke(seriesIndex, new BasicStroke(2.5f));
        }
        
        // Set the JFreeChart parameters call
        /* Plot diagram chart */
        JFreeChart chart = ChartFactory.createXYLineChart(stringDatabase.getString(AnalysisType.PLOT.toString()),
                                                          // Chart title
                                                          uncertainParameter.getName(),                                           // X axis label
                                                          stringDatabase.getString("SensitivityAnalysis.General.GlobalUtility"),  // Y axis label
                                                          dataset,                                                                // data
                                                          PlotOrientation.VERTICAL,                                               // Orientation
                                                          true,                                                                   // Legend
                                                          true,                                                                   // ToolTips
                                                          false                                                                   // Urls
        );
        
        XYPlot plot = (XYPlot) chart.getPlot();
        
        // Set the range axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(minRangeUtility * 0.999, maxRangeUtility * 1.001);
        
        // Set the domain axis
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(minVariationValue, maxVariationValue);
        domainAxis.setTickUnit(new NumberTickUnit((domainAxis.getUpperBound() - domainAxis.getLowerBound()) / 10,
                                                  new DecimalFormat()));
        domainAxis.setNumberFormatOverride(decimalFormat);
        
        // Set the horizontal axis reference utility line
        ValueMarker referenceMarker = new ValueMarker(utilityReference);
        referenceMarker.setLabel(new DecimalFormat("#.###").format(utilityReference));
        referenceMarker.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
        referenceMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
        plot.addRangeMarker(referenceMarker);
        
        // Set the vertical axis reference utility line at 0%
        ValueMarker zeroMarker = new ValueMarker(0.0);
        zeroMarker.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
        plot.addDomainMarker(zeroMarker);
        
        // Set general aspects
        plot.setRenderer(renderer);
        chart.addSubtitle(new TextTitle(stringDatabase.getString(axisVariation.getVariationType().toString())));
        
        // Adds the scope subtitle (global or decision scope)
        String scopeSubtitle;
        if (decisionVariable != null) {
            String decisionScopeSubtitle = stringDatabase.getString("ScopeSelector.Title") + " " + stringDatabase
                    .getString(ScopeType.DECISION.toString()).toLowerCase() + " - ";
            for (Finding finding : selectedScenario) {
                decisionScopeSubtitle += finding.getVariable().getName() + ": " + finding.getState() + ", ";
            }
            decisionScopeSubtitle = decisionScopeSubtitle.substring(0, decisionScopeSubtitle.length() - 2);
            
            scopeSubtitle = decisionScopeSubtitle;
            
        } else {
            scopeSubtitle = stringDatabase.getString("ScopeSelector.Title") + " " + stringDatabase
                    .getString(ScopeType.GLOBAL.toString()).toLowerCase();
            chart.removeLegend();
        }
        
        chart.addSubtitle(new TextTitle(scopeSubtitle));
        
        return chart;
    }
}
