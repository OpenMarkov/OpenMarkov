/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
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
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnMap;
import org.openmarkov.sensitivityanalysis.model.AnalysisType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Dialog that shows Map sensitivity analysis
 *
 * @author jperez-martin
 */
public class MapDialog extends JDialog {
    
    /**
     * Sensitivity Analysis Map task
     */
    private VESensAnMap veSensAnMap;
    
    /**
     * Uncertain parameter of first axis
     */
    private UncertainParameter hUncertainParameter;
    
    /**
     * Uncertain parameter of second axis
     */
    private UncertainParameter vUncertainParameter;
    
    /**
     * Variation of the first uncertain parameter
     */
    private AxisVariation hAxisVariation;
    
    /**
     * Variation of the second uncertain parameter
     */
    private AxisVariation vAxisVariation;
    
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
     * Dialog with map analysis
     *
     * @param owner                    Owner window
     * @param probNet                  ProbNet
     * @param preResolutionEvidence    PreResolutionEvidence (included the scenario if applies)
     * @param sensitivityAnalysisModel Sensitivity model parameters
     */
    public MapDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence,
                     SensitivityAnalysisModel sensitivityAnalysisModel) throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {
        super(owner);
        
        this.probNet = probNet;
        this.hUncertainParameter = sensitivityAnalysisModel.getSelectedUncertainParametersXAxis().get(0);
        this.vUncertainParameter = sensitivityAnalysisModel.getSelectedUncertainParametersYAxis().get(0);
        this.hAxisVariation = sensitivityAnalysisModel.getHorizontalAxisVariation();
        this.vAxisVariation = sensitivityAnalysisModel.getVerticalAxisVariation();
        this.iterations = sensitivityAnalysisModel.getNumberOfIterationsSimulations();
        this.decisionVariable = sensitivityAnalysisModel.getDecisionVariable();
        this.selectedScenario = sensitivityAnalysisModel.getSelectedScenario();
        
        // Run the task
        veSensAnMap = new VESensAnMap(probNet, preResolutionEvidence, hUncertainParameter, hAxisVariation,
                                      vUncertainParameter, vAxisVariation, iterations, decisionVariable);
        
        
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
        jContentPane.add(getMapPanel(), BorderLayout.CENTER);
        return jContentPane;
    }
    
    /**
     * Gets a JPanel with the chart
     *
     * @return
     */
    private JPanel getMapPanel() {
        JFreeChart mapChart = getMapChart();
        return new ChartPanel(mapChart);
    }
    
    /**
     * Build the chart
     *
     * @return
     */
    private JFreeChart getMapChart() {
        
        // Format domain axis
        // Set the domain axis format
        DecimalFormat hDecimalFormat = null;
        if (hAxisVariation.getVariationType() == DeterministicAxisVariationType.PORV || hAxisVariation
                .getVariationType() == DeterministicAxisVariationType.POPP) {
            hDecimalFormat = new DecimalFormat("+##.##%;-##.##%");
        } else if (hAxisVariation.getVariationType() == DeterministicAxisVariationType.RORV || hAxisVariation
                .getVariationType() == DeterministicAxisVariationType.UDIN) {
            hDecimalFormat = new DecimalFormat("0.000;-0.000");
        }
        
        // Get the minimum and maximum values of variation to define horizontal intervals
        double hMinVariationValue, hMaxVariationValue;
        if (hAxisVariation.getVariationType() == DeterministicAxisVariationType.UDIN) {
            hMinVariationValue = hAxisVariation.getVariationBounds()[0];
            hMaxVariationValue = hAxisVariation.getVariationBounds()[1];
        } else {
            hMinVariationValue = -hAxisVariation.getVariationValue() / 100;
            hMaxVariationValue = +hAxisVariation.getVariationValue() / 100;
        }
        
        // Size of every horizontal interval
        double hVariationInterval = (hMaxVariationValue - hMinVariationValue) / iterations;
        
        // Format range axis
        // Set the range axis format
        DecimalFormat vDecimalFormat = null;
        if (vAxisVariation.getVariationType() == DeterministicAxisVariationType.PORV || vAxisVariation
                .getVariationType() == DeterministicAxisVariationType.POPP) {
            vDecimalFormat = new DecimalFormat("+##.##%;-##.##%");
        } else if (vAxisVariation.getVariationType() == DeterministicAxisVariationType.RORV || vAxisVariation
                .getVariationType() == DeterministicAxisVariationType.UDIN) {
            vDecimalFormat = new DecimalFormat("0.000;-0.000");
        }
        
        // Get the minimum and maximum values of variation to define horizontal intervals
        double vMinVariationValue, vMaxVariationValue;
        if (hAxisVariation.getVariationType() == DeterministicAxisVariationType.UDIN) {
            vMinVariationValue = hAxisVariation.getVariationBounds()[0];
            vMaxVariationValue = hAxisVariation.getVariationBounds()[1];
        } else {
            vMinVariationValue = -hAxisVariation.getVariationValue() / 100;
            vMaxVariationValue = +hAxisVariation.getVariationValue() / 100;
        }
        
        // Size of every horizontal interval
        double vVariationInterval = (vMaxVariationValue - vMinVariationValue) / iterations;
        
        double minRangeUtility = Double.MAX_VALUE;
        double maxRangeUtility = Double.MIN_VALUE;
        
        // get the potential
        TablePotential uncertainParameterPotential = veSensAnMap.getUncertainParametersPotentials()
                                                                .get(hUncertainParameter);
        
        int decisionVariableStates = 0;
        if (decisionVariable != null) {
            decisionVariableStates += decisionVariable.getNumStates();
        } else {
            decisionVariableStates = 1;
        }
        
        int totalIterations = iterations + 1;
        
        // Dimensions of the arrays
        double[] xvalues = new double[totalIterations * totalIterations];
        double[] yvalues = new double[totalIterations * totalIterations];
        double[] zvalues = new double[totalIterations * totalIterations];
        
        // iterate rows and columns of map
        for (int row = 0; row < totalIterations; row++) {
            for (int column = 0; column < totalIterations; column++) {
                int dataPosition = row * totalIterations + column;
                
                double zAxisValue = uncertainParameterPotential.getValues()[dataPosition];
                
                // If the result is conditioned on a decision, we need to confront the utilities of each state of the decision
                if (decisionVariable != null) {
                    
                    // That variable stores the state index of the decision that gives greater utility
                    int greatUtilityStateIndex = 0;
                    
                    // decisionVariableStateIndex skips "0" position because it was calculated in a before step (zAxisValue)
                    for (int decisionVariableStateIndex = 1;
                         decisionVariableStateIndex < decisionVariableStates; decisionVariableStateIndex++) {
                        int dataPositionForThatDecision = dataPosition + (
                                decisionVariableStateIndex * totalIterations * totalIterations
                        );
                        double zAxisValueForThatDecision = uncertainParameterPotential
                                .getValues()[dataPositionForThatDecision];
                        if (zAxisValueForThatDecision > zAxisValue) {
                            greatUtilityStateIndex = decisionVariableStateIndex;
                        } else if (zAxisValueForThatDecision == zAxisValue) {
                            greatUtilityStateIndex = -1;
                        }
                    }
                    
                    zAxisValue = greatUtilityStateIndex;
                }
                
                // Update minimum utility found
                if (zAxisValue < minRangeUtility) {
                    minRangeUtility = zAxisValue;
                }
                
                // Update maximum utility found
                if (zAxisValue > maxRangeUtility) {
                    maxRangeUtility = zAxisValue;
                }
                
                double xAxisValue = hMinVariationValue + (hVariationInterval * column);
                double yAxisValue = vMinVariationValue + (vVariationInterval * row);
                
                xvalues[dataPosition] = xAxisValue;
                yvalues[dataPosition] = yAxisValue;
                zvalues[dataPosition] = zAxisValue;
                
            }
        }
        
        double[][] data = new double[][]{xvalues, yvalues, zvalues};
        
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("Series 1", data);
        
        // Set the x axis format
        NumberAxis xAxis = new NumberAxis(hUncertainParameter.getName() + " - " + stringDatabase
                .getString(hAxisVariation.getVariationType().toString()));
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);
        xAxis.setNumberFormatOverride(hDecimalFormat);
        
        // Set the y axis format
        NumberAxis yAxis = new NumberAxis(vUncertainParameter.getName() + " - " + stringDatabase
                .getString(vAxisVariation.getVariationType().toString()));
        yAxis.setLowerMargin(0);
        yAxis.setUpperMargin(0);
        yAxis.setNumberFormatOverride(vDecimalFormat);
        
        XYBlockRenderer renderer = new XYBlockRenderer();
        
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(GUIColors.SensitivityAnalysis.PLOT_BACKGROUND.getColor());
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        
        JFreeChart chart = new JFreeChart(stringDatabase.getString(AnalysisType.MAP.toString()), plot);
        chart.setBackgroundPaint(GUIColors.SensitivityAnalysis.CHART_BACKGROUND.getColor());
        
        NumberAxis scaleAxis = new NumberAxis(stringDatabase.getString("SensitivityAnalysis.General.Scale"));
        scaleAxis.setRange(minRangeUtility, maxRangeUtility);
        
        if (decisionVariable != null) {
            // If the result is conditioned on a decision variable we will paint each point with the color of the
            // winner decision state
            DefaultDrawingSupplier defaultDrawingSupplier = new DefaultDrawingSupplier();
            LegendItemCollection chartLegend = new LegendItemCollection();
            LookupPaintScale paintScale = new LookupPaintScale();
            Shape shape = new Rectangle(10, 10);
            LegendItem drawLegendItem = new LegendItem(stringDatabase.getString("SensitivityAnalysis.General.Draw"),
                                                       // Legend label
                                                       "",     // Legend description
                                                       "",     // Tooltip
                                                       "",     // Url
                                                       shape,      // Shape
                                                       defaultDrawingSupplier.getNextPaint()); // Fill paint
            chartLegend.add(drawLegendItem);
            paintScale.add(-1.0, drawLegendItem.getFillPaint());
            
            for (int stateIndex = 0; stateIndex < decisionVariableStates; stateIndex++) {
                LegendItem legendItem = new LegendItem(
                        decisionVariable.getName() + " = " + decisionVariable.getStateName(stateIndex), // Legend label
                        "",     // Legend description
                        "",     // Tooltip
                        "",     // Url
                        shape,      // Shape
                        defaultDrawingSupplier.getNextPaint()); // Fill paint
                paintScale.add(stateIndex, legendItem.getFillPaint());
                chartLegend.add(legendItem);
            }
            plot.setFixedLegendItems(chartLegend);
            renderer.setPaintScale(paintScale);
        } else {
            // If the result is not conditioned on a decision variable we will paint an utility scale
            PaintScale paintScale = new GrayPaintScale(minRangeUtility, maxRangeUtility);
            renderer.setPaintScale(paintScale);
            
            PaintScaleLegend psl = new PaintScaleLegend(paintScale, scaleAxis);
            psl.setAxisOffset(5.0);
            psl.setPosition(RectangleEdge.RIGHT);
            psl.setMargin(new RectangleInsets(5, 5, 5, 5));
            
            chart.removeLegend();
            chart.addSubtitle(psl);
        }
        
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
