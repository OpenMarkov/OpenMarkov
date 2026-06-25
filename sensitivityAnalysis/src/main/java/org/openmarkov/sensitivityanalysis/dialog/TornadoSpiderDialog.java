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
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.DomainInterval;
import org.openmarkov.core.model.network.modelUncertainty.Tools;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnTornadoSpider;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;
import org.openmarkov.sensitivityanalysis.model.TornadoBar;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Dialog that shows Map sensitivity analysis
 *
 * @author jperez
 */
public class TornadoSpiderDialog extends JDialog {
    
    /**
     * Sensitivity Analysis Tornado/Spider task
     */
    private VESensAnTornadoSpider veSensAnTornadoSpider = null;
    
    /**
     * List of selected uncertain parameters
     */
    private List<UncertainParameter> uncertainParameters;
    /**
     * Specific scenario based in previous evidence (decision nodes)
     */
    private EvidenceCase preResolutionEvidence;
    /**
     * Utility of reference (base case, without uncertainty)
     */
    private double utilityReference;
    
    /**
     * Conditioned decision variable for the decision scope
     */
    private Variable decisionVariable;
    
    /**
     * Selected scenario for the selected decision
     */
    private List<Finding> selectedScenario;
    
    /**
     * Axis variation of x axis
     */
    private AxisVariation axisVariation;
    
    /**
     * Number of points/iterations in the sensitivity range of each parameter
     */
    private int iterations;
    
    /**
     * Tabbed pane
     */
    private JTabbedPane tabbedPane;
    
    /**
     * ProbNet
     */
    private ProbNet probNet;
    
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * GUI controls to switch between absolute and relative criteria values
     */
    private JComboBox<State> interventionSpiderDecisionSelector;
    private JComboBox<State> relativeSpiderDecisionSelector;
    private JComboBox<State> interventionTornadoDecisionSelector;
    private JComboBox<State> relativeTornadoDecisionSelector;
    private JCheckBox showOlineSpider;
    private JCheckBox showOlineTornado;
    private JPanel tornadoPanel;
    private ChartPanel tornadoChartPanel;
    private JPanel spiderPanel;
    private ChartPanel spiderChartPanel;
    
    
    /**
     * Dialog for tornado/spider analysis
     *
     * @param owner                    Owner window
     * @param probNet                  ProbNet
     * @param preResolutionEvidence    PreResolutionEvidence (included the scenario if applies)
     * @param sensitivityAnalysisModel Sensitivity model parameters
     */
    public TornadoSpiderDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence,
                               SensitivityAnalysisModel sensitivityAnalysisModel) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(owner);
        this.probNet = probNet;
        this.uncertainParameters = sensitivityAnalysisModel.getSelectedUncertainParametersXAxis();
        this.axisVariation = sensitivityAnalysisModel.getHorizontalAxisVariation();
        this.iterations = sensitivityAnalysisModel.getNumberOfIterationsSimulations();
        this.decisionVariable = sensitivityAnalysisModel.getDecisionVariable();
        this.selectedScenario = sensitivityAnalysisModel.getSelectedScenario();
        this.preResolutionEvidence = preResolutionEvidence;
        
        runSensitivityAnalysisTask();
        
        setCharts();
        
        initializeDialog(owner);
    }
    
    /**
     * Run the sensitivity analysis task for the uncertainty parameters
     */
    private void runSensitivityAnalysisTask() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        // Run the task without uncertainty and set the utility reference
        VEEvaluation veEvaluation = new VEEvaluation(probNet);
        veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
        this.utilityReference = veEvaluation.getUtility().getValues()[0];
        // Run the task with uncertainty
        veSensAnTornadoSpider = new VESensAnTornadoSpider(probNet, preResolutionEvidence, uncertainParameters,
                                                          axisVariation, iterations, decisionVariable);
        
        
    }
    
    /**
     * Sets the main panel.
     */
    private void initializeDialog(Window owner) {
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
    
    ;
    
    /**
     * Set chart panels
     */
    private void setCharts() {
        this.setTitle("OpenMarkov - " + StringDatabase.getUniqueInstance()
                                                      .getString("SensitivityAnalysis.Title") + " - "
                              + probNet.getName());
        this.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
        setContentPane(getJContentPane());
        pack();
    }
    
    /**
     * Gets the content pane with the tabbed pane
     */
    private JPanel getJContentPane() {
        JPanel jContentPane = new JPanel();
        jContentPane.setLayout(new BorderLayout());
        jContentPane.add(getTabbedPane(), BorderLayout.CENTER);
        return jContentPane;
    }
    
    /**
     * Gets the tabbed pane with tornado and spider panels
     *
     * @return tabbed pane
     */
    private JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            tabbedPane.addTab(stringDatabase.getString("SensitivityAnalysis.Type.Tornado"), null, getTornadoPanel(), null);
            tabbedPane.addTab(stringDatabase.getString("SensitivityAnalysis.Type.Spider"), null, getSpiderPanel(), null);
        }
        return tabbedPane;
    }
    
    /**
     * Gets a JPanel with the tornado chart
     *
     * @return tornado chart panel
     */
    private JPanel getTornadoPanel() {
        tornadoPanel = new JPanel();
        tornadoPanel.setLayout(new BorderLayout());
        //Adding the options panel for the referenced chart
        if (decisionVariable != null) {
            //utilityReference = 0;
            tornadoPanel.add(getOptionsChartPanel("tornado"), BorderLayout.EAST);
        }
        tornadoPanel.add(getTornadoChart(), BorderLayout.CENTER);
        
        return tornadoPanel;
    }
    
    /**
     * Gets the tornado chart
     *
     * @return tornado chart
     */
    public ChartPanel getTornadoChart() {
        List<TornadoBar> tornadoBars = getTornadoBars();
        JFreeChart tornadoChart = getTornadoChart(tornadoBars);
        tornadoChartPanel = new ChartPanel(tornadoChart);
        
        return tornadoChartPanel;
    }
    
    /**
     * Builds the options panel for the compared interventions of the selected decision
     *
     * @return
     */
    public JPanel getOptionsChartPanel(String panelName) {
        JPanel optionsChartPanel = new JPanel();
        optionsChartPanel.setPreferredSize(new Dimension(140, 0));
        optionsChartPanel.setLayout(new BorderLayout());
        
        JPanel interventionValuesPanel = new JPanel();
        interventionValuesPanel.setBorder(new TitledBorder(stringDatabase.getString("SensitivityAnalysis.General.display")));
        interventionValuesPanel.setLayout(new BoxLayout(interventionValuesPanel, BoxLayout.Y_AXIS));
        
        // Intervention selector
        interventionValuesPanel.add(createLabel("intervention"));
        JComboBox<State> interventionDecisionSelector = createComboBox((decisionVariable.getStates().length > 1 ? 1 : 0), panelName);
        interventionValuesPanel.add(interventionDecisionSelector);
        interventionValuesPanel.add(new JPanel());
        
        // Reference selector
        interventionValuesPanel.add(createLabel("relativeTo"));
        JComboBox<State> relativeDecisionSelector = createComboBox(0, panelName);
        interventionValuesPanel.add(relativeDecisionSelector);
        interventionValuesPanel.add(new JPanel());
        
        // Hide/Show '0' line
        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox showOline = createCheckbox(panelName);
        checkBoxPanel.add(showOline);
        interventionValuesPanel.add(checkBoxPanel);
        
        optionsChartPanel.add(interventionValuesPanel, BorderLayout.NORTH);
        
        // Store references to the selectors based on the panel name
        if (panelName.equals("tornado")) {
            interventionTornadoDecisionSelector = interventionDecisionSelector;
            relativeTornadoDecisionSelector = relativeDecisionSelector;
            showOlineTornado = showOline;
        } else {
            interventionSpiderDecisionSelector = interventionDecisionSelector;
            relativeSpiderDecisionSelector = relativeDecisionSelector;
            showOlineSpider = showOline;
        }
        
        return optionsChartPanel;
    }
    
    
    /**
     * Gets the tornado bars for all uncertain parameters
     *
     * @return tornado bars
     */
    private List<TornadoBar> getTornadoBars() {
        List<TornadoBar> tornadoBars = new ArrayList<>();
        
        for (UncertainParameter uncertainParameter : uncertainParameters) {
            TablePotential potential = veSensAnTornadoSpider.getUncertainParametersPotentials().get(uncertainParameter);
            // Get min and max values
            double minValue = Double.MAX_VALUE;
            double maxValue = Double.MIN_VALUE;
            double minInterventionUtility = Double.MAX_VALUE;
            double maxInterventionUtility = Double.MIN_VALUE;
            double minReferenceUtility = Double.MAX_VALUE;
            double maxReferenceUtility = Double.MIN_VALUE;
            
            if (decisionVariable != null) {
                int interventionDecisionIndex = interventionTornadoDecisionSelector.getSelectedIndex();
                int relativeDecisionIndex = relativeTornadoDecisionSelector.getSelectedIndex();
                // For intervention utility values
                for (int i = 0; i <= iterations; i++) {
                    double value = potential.getValues()[i + (interventionDecisionIndex * (iterations + 1))];
                    minInterventionUtility = Math.min(minInterventionUtility, value);
                    maxInterventionUtility = Math.max(maxInterventionUtility, value);
                }
                // For reference utility values
                for (int j = 0; j <= iterations; j++) {
                    double value = potential.getValues()[j + (relativeDecisionIndex * (iterations + 1))];
                    minReferenceUtility = Math.min(minReferenceUtility, value);
                    maxReferenceUtility = Math.max(maxReferenceUtility, value);
                }
                minValue = Math.min(minInterventionUtility - minReferenceUtility, maxInterventionUtility - maxReferenceUtility);
                maxValue = Math.max(maxInterventionUtility - maxReferenceUtility, minInterventionUtility - minReferenceUtility);
                
            } else {
                for (double value : potential.getValues()) {
                    minValue = Math.min(minValue, value);
                    maxValue = Math.max(maxValue, value);
                }
            }
            
            TornadoBar tornadoBar = new TornadoBar(uncertainParameter, minValue, maxValue);
            tornadoBars.add(tornadoBar);
        }
        // Order tornado bars (first the largest)
        Collections.sort(tornadoBars);
        
        return tornadoBars;
    }
    
    /**
     * Build the tornado chart
     *
     * @return tornado bars
     */
    private JFreeChart getTornadoChart(List<TornadoBar> tornadoBars) {
        // JFreeChart attributes definition. Number of bars in the chart, one per parameter
        int bars = uncertainParameters.size();
        String[] seriesKeys = new String[]{""};
        String[] categoryKeys = new String[bars];
        Number[][] starts = new Number[1][bars];
        Number[][] ends = new Number[1][bars];
        
        // Place the series
        int series = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double reductionCoef = Math.pow(10, -6);
        
        // Iterates through the series
        for (TornadoBar tornadoBar : tornadoBars) {
            categoryKeys[series] = tornadoBar.getUncertainParameter().getName();
            double minValue = tornadoBar.getMinValue();
            double maxValue = tornadoBar.getMaxValue();
            
            // For point lists that contains the same value (the uncertain it's not relevant in that cases)
            if (minValue == maxValue) {
                if (minValue == 0) {
                    minValue -= reductionCoef;
                    maxValue += reductionCoef;
                } else {
                    minValue = minValue - minValue * reductionCoef;
                    maxValue = maxValue + maxValue * reductionCoef;
                }
            }
            min = Math.min(minValue, min);
            max = Math.max(maxValue, max);
            
            starts[0][series] = minValue;
            ends[0][series] = maxValue;
            series++;
        }
        // Set the JFreeChart parameters call
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(seriesKeys, categoryKeys, starts,
                                                                                    ends);
        
        // Set the category Axis
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setLabel(stringDatabase.getString("SensitivityAnalysis.General.Parameters"));
        categoryAxis.setLabelFont(MainGUI.INSTANCE.mainPanel.getFont().deriveFont(14.0f));
        categoryAxis.setTickLabelFont(MainGUI.INSTANCE.mainPanel.getFont().deriveFont(12.0f));
        
        double space = 0.475 - (bars * 0.03);
        categoryAxis.setLowerMargin(space);
        categoryAxis.setUpperMargin(space);
        categoryAxis.setCategoryMargin(0.01);
        

        IntervalBarRenderer renderer = new IntervalBarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                return GUIColors.SensitivityAnalysis.BAR_COLORS.get(column % GUIColors.SensitivityAnalysis.BAR_COLORS.size())
                                                               .getColor();
            }
        };
        // Set the values Axis
        ValueAxis valueAxis = new NumberAxis();
        valueAxis.setRange(min - (max - min) * 0.05, max + (max - min) * 0.05);
        
        // Set the presentation settings
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.05);
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        
        if (decisionVariable == null) {
            // Set the vertical axis reference utility line
            ValueMarker referenceMarker = new ValueMarker(utilityReference);
            referenceMarker.setLabel(new DecimalFormat("#.###").format(utilityReference));
            referenceMarker.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
            referenceMarker.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
            plot.addRangeMarker(referenceMarker);
            
        } else {
            if (showOlineTornado.isSelected()) {
                ValueMarker referenceMarker = new ValueMarker(0);
                referenceMarker.setLabel(new DecimalFormat("#.###").format(0));
                referenceMarker.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
                referenceMarker.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
                plot.addRangeMarker(referenceMarker);
                double lowerBound = valueAxis.getLowerBound();
                double upperBound = valueAxis.getUpperBound();
                if (lowerBound > 0) {
                    lowerBound = 0;
                }
                if (upperBound < 0) {
                    upperBound = 0;
                }
                valueAxis.setRange(lowerBound - (upperBound - lowerBound) * 0.05, upperBound + (upperBound - lowerBound) * 0.05);
            }
        }
        plot.getRangeAxis().setLabel(stringDatabase.getString("SensitivityAnalysis.General.ExpectedUtility"));
        JFreeChart chart = new JFreeChart(plot);
        //chart.addSubtitle(new TextTitle(stringDatabase.getString(axisVariation.getVariationType().toString())));
        chart.setTitle(stringDatabase.getString("SensitivityAnalysis.Type.Tornado"));
        chart.removeLegend();
        
        return chart;
    }
    
    /**
     * Gets a JPanel with the spider chart and the options panel
     *
     * @return spider chart panel
     */
    private JPanel getSpiderPanel() {
        spiderPanel = new JPanel();
        spiderPanel.setLayout(new BorderLayout());
        if (decisionVariable != null) {
            spiderPanel.add(getOptionsChartPanel("spider"), BorderLayout.EAST);
        }
        spiderPanel.add(getSpiderChart(), BorderLayout.CENTER);
        
        return spiderPanel;
    }
    
    /**
     * Build the spider chart with the analysis data
     *
     * @return spider chart
     */
    private JPanel getSpiderChart() {
        // JFreeChart attributes definition
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        // Set the domain axis format
        DecimalFormat decimalFormat = null;
        DeterministicAxisVariationType variationType = axisVariation.getVariationType();
        if (variationType == DeterministicAxisVariationType.PORV || variationType == DeterministicAxisVariationType.POPP) {
            decimalFormat = new DecimalFormat("+##%;-##%");
        } else if (variationType == DeterministicAxisVariationType.RORV || variationType == DeterministicAxisVariationType.UDIN) {
            decimalFormat = new DecimalFormat("0.000;-0.000");
        }
        
        boolean isVariationTypePOPPOrRORV = isPOPPOrRORV(variationType);
        
        // Get the minimum and maximum values of variation to define horizontal intervals
        double minVariationValue = 0, maxVariationValue = 0, variationInterval = 0;
        if (variationType == DeterministicAxisVariationType.UDIN) {
            minVariationValue = axisVariation.getVariationBounds()[0];
            maxVariationValue = axisVariation.getVariationBounds()[1];
        } else if (!isVariationTypePOPPOrRORV) {
            minVariationValue = -axisVariation.getVariationValue() / 100;
            maxVariationValue = +axisVariation.getVariationValue() / 100;
        }
        
        // Size of every horizontal interval
        if (!isVariationTypePOPPOrRORV) {
            variationInterval = (maxVariationValue - minVariationValue) / iterations;
        }
        
        double minRangeUtility = Double.MAX_VALUE;
        double maxRangeUtility = Double.MIN_VALUE;
        double[] minVariationValues = new double[uncertainParameters.size()];
        double[] maxVariationValues = new double[uncertainParameters.size()];
        int iParam = 0;
        
        HashMap<UncertainParameter, DomainInterval> sampledIntervals = veSensAnTornadoSpider.getSampledInterval();
        for (UncertainParameter uncertainParameter : uncertainParameters) {
            //if (variationType.equals(DeterministicAxisVariationType.POPP)){
            if (isVariationTypePOPPOrRORV) {
                double referenceValue = uncertainParameter.getBaseLineValue();
                DomainInterval sampledInterval = sampledIntervals.get(uncertainParameter);
                minVariationValue = -Math.abs(sampledInterval.min() - referenceValue) / Math.abs(referenceValue);
                maxVariationValue = Math.abs(sampledInterval.max() - referenceValue) / Math.abs(referenceValue);
                minVariationValues[iParam] = minVariationValue;
                maxVariationValues[iParam] = maxVariationValue;
                variationInterval = (maxVariationValue - minVariationValue) / iterations;
            }
            TablePotential uncertainParameterPotential = veSensAnTornadoSpider.getUncertainParametersPotentials()
                                                                              .get(uncertainParameter);
            XYSeries series = new XYSeries(uncertainParameter.getName());
            int seriesIndex = 0;
            
            // Get the count of the iteration
            int horizontalIteration = 0;
            double[] potentials = uncertainParameterPotential.getValues();
            for (int i = 0; i <= iterations; i++) {
                double value;
                //Global utility
                if (decisionVariable == null) {
                    value = potentials[i];
                    //OneDecsion utility
                } else {
                    double valueIntervention = potentials[i + (interventionSpiderDecisionSelector.getSelectedIndex() * (iterations + 1))];
                    double valueReference = potentials[i + (relativeSpiderDecisionSelector.getSelectedIndex() * (iterations + 1))];
                    value = valueIntervention - valueReference;
                }
                // Update minimum and maximum utility found
                minRangeUtility = Math.min(minRangeUtility, value);
                maxRangeUtility = Math.max(maxRangeUtility, value);
                
                double variationValue = minVariationValue + (variationInterval * horizontalIteration);
                series.add(variationValue, value);
                
                horizontalIteration++;
            }
            dataset.addSeries(series);
            
            // Set series presentation settings
            renderer.setSeriesShapesVisible(seriesIndex, false);
            renderer.setSeriesStroke(seriesIndex, new BasicStroke(2.5f));
            iParam++;
            
        }
        
        // Set the JFreeChart parameters call
        /* Spider diagram chart */
        JFreeChart chart = ChartFactory
                .createXYLineChart(stringDatabase.getString("SensitivityAnalysis.Type.Spider"), // Chart title
                                   stringDatabase.getString(variationType.toStringSpiderLegend()),
                                   // X axis label
                                   stringDatabase.getString("SensitivityAnalysis.General.ExpectedUtility"),
                                   // Y axis label
                                   dataset,                                            // data
                                   PlotOrientation.VERTICAL,                            // Orientation
                                   true,                                                // Legend
                                   true,                                                // ToolTips
                                   false                                                // Urls
                );
        
        XYPlot plot = (XYPlot) chart.getPlot();
        
        // Set the domain axis
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        if (isVariationTypePOPPOrRORV) {
            minVariationValue = Tools.min(minVariationValues);
            maxVariationValue = Tools.max(maxVariationValues);
        }
        domainAxis.setRange(minVariationValue - (maxVariationValue - minVariationValue) * 0.02, maxVariationValue + (maxVariationValue - minVariationValue) * 0.02);
        double size = (domainAxis.getUpperBound() - domainAxis.getLowerBound()) / 10;
        domainAxis.setTickUnit(new NumberTickUnit(size, new DecimalFormat()));
        domainAxis.setNumberFormatOverride(decimalFormat);
        
        // Set the range axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(minRangeUtility * 0.999, maxRangeUtility * 1.001);
        if (decisionVariable == null) {
            // Set the horizontal axis reference utility line
            ValueMarker referenceMarker = new ValueMarker(utilityReference);
            referenceMarker.setLabel(new DecimalFormat("#.###").format(utilityReference));
            referenceMarker.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
            referenceMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
            plot.addRangeMarker(referenceMarker);
        } else {
            if (showOlineSpider.isSelected()) {
                // Set the horizontal axis 0 line as reference
                ValueMarker referenceMarkerOline = new ValueMarker(0);
                referenceMarkerOline.setLabel(new DecimalFormat("#.###").format(0));
                referenceMarkerOline.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
                referenceMarkerOline.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
                plot.addRangeMarker(referenceMarkerOline);
                double lowerBound = rangeAxis.getLowerBound();
                double upperBound = rangeAxis.getUpperBound();
                if (lowerBound > 0) {
                    lowerBound = 0;
                }
                if (upperBound < 0) {
                    upperBound = 0;
                }
                rangeAxis.setRange(lowerBound - ((upperBound - lowerBound) * 0.04), upperBound + ((upperBound - lowerBound) * 0.04));
            }
        }
        
        // Set the vertical axis reference utility line at 0%
        ValueMarker zeroMarker = new ValueMarker(0.0);
        zeroMarker.setPaint(GUIColors.SensitivityAnalysis.TEXT.getColor());
        plot.addDomainMarker(zeroMarker);
        
        // Set general aspects
        plot.setRenderer(renderer);
        chart.getLegend().setPosition(RectangleEdge.RIGHT);
        
        spiderChartPanel = new ChartPanel(chart);
        
        return spiderChartPanel;
    }
    
    static boolean isPOPPOrRORV(DeterministicAxisVariationType variationType) {
        return variationType == DeterministicAxisVariationType.POPP || variationType == DeterministicAxisVariationType.RORV;
    }
    
    /**
     * Repaint and refresh the chart panel and its components
     */
    private void refreshChartPanels(String panelName) {
        if (panelName.equals("tornado")) {
            tornadoPanel.remove(tornadoChartPanel);
            tornadoPanel.add(getTornadoChart(), BorderLayout.CENTER);
        } else if (panelName.equals("spider")) {
            spiderPanel.remove(spiderChartPanel);
            spiderPanel.add(getSpiderChart(), BorderLayout.CENTER);
        }
        this.setVisible(false);
        this.setVisible(true);
    }
    
    /* Returns a panel and its label */
    private JPanel createLabel(String label) {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel(stringDatabase.getString("SensitivityAnalysis.General." + label)));
        return labelPanel;
    }
    
    /**
     * Returns the intervention selector panel with all the states of the selected decision.
     * Avoid the same intervention (state) in both selectors
     *
     * @param selectedState
     * @param panelName
     *
     * @return
     */
    private JComboBox<State> createComboBox(int selectedState, String panelName) {
        JComboBox<State> comboBox = new JComboBox<>();
        for (State state : decisionVariable.getStates()) {
            comboBox.addItem(state);
        }
        comboBox.setSelectedIndex(selectedState);
        comboBox.setEnabled(true);
        comboBox.addActionListener(e -> {
            @SuppressWarnings("unchecked")
            JComboBox<State> source = (JComboBox<State>) e.getSource();
            JComboBox<State> other = (source == interventionTornadoDecisionSelector || source == interventionSpiderDecisionSelector) ?
                    (source == interventionTornadoDecisionSelector ? relativeTornadoDecisionSelector : relativeSpiderDecisionSelector) :
                    (source == relativeTornadoDecisionSelector ? interventionTornadoDecisionSelector : interventionSpiderDecisionSelector);
            if (source.getSelectedIndex() == other.getSelectedIndex()) {
                other.setSelectedIndex((other.getSelectedIndex() + 1) % other.getItemCount());
            }
            refreshChartPanels(panelName);
        });
        return comboBox;
    }
    
    private JCheckBox createCheckbox(String panelName) {
        JCheckBox checkbox = new JCheckBox(stringDatabase.getString("SensitivityAnalysis.General.line0"));
        checkbox.addActionListener(e -> {
            refreshChartPanels(panelName);
        });
        return checkbox;
    }
    
}
