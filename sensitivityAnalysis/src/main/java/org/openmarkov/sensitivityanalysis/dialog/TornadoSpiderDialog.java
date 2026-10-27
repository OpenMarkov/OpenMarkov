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
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.DomainInterval;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.OMTabbedPane;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnTornadoSpider;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;
import org.openmarkov.sensitivityanalysis.model.TornadoBar;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
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
    private OMTabbedPane tabbedPane;
    
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
            tabbedPane = new OMTabbedPane();
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
    
    /** @return the spider chart shown in its tab */
    ChartPanel getSpiderChartPanel() {
        return spiderChartPanel;
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
            double[] values = potential.getValues();
            if (decisionVariable != null) {
                values = differences(values, iterations, interventionTornadoDecisionSelector.getSelectedIndex(),
                                     relativeTornadoDecisionSelector.getSelectedIndex());
            }
            double[] range = range(values);
            
            TornadoBar tornadoBar = new TornadoBar(uncertainParameter, range[0], range[1]);
            tornadoBars.add(tornadoBar);
        }
        // Order tornado bars (first the largest)
        Collections.sort(tornadoBars);
        
        return tornadoBars;
    }
    
    /** The least and the greatest of the values. */
    public static double[] range(double[] values) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return new double[]{ min, max };
    }
    
    /**
     * The expected utility of the intervention minus that of the reference at each point of the sweep of a parameter.
     * The values hold the iterations + 1 points of each option of the decision, one option after another.
     */
    public static double[] differences(double[] values, int iterations, int intervention, int reference) {
        double[] differences = new double[iterations + 1];
        for (int point = 0; point <= iterations; point++) {
            differences[point] = values[point + intervention * (iterations + 1)] - values[point + reference * (iterations + 1)];
        }
        return differences;
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
            categoryKeys[series] = tornadoBar.getUncertainParameter().getLabel();
            double minValue = tornadoBar.getMinValue();
            double maxValue = tornadoBar.getMaxValue();
            
            // For point lists that contains the same value (the uncertain it's not relevant in that cases)
            if (minValue == maxValue) {
                if (minValue == 0) {
                    minValue -= reductionCoef;
                    maxValue += reductionCoef;
                } else {
                    minValue = minValue - Math.abs(minValue) * reductionCoef;
                    maxValue = maxValue + Math.abs(maxValue) * reductionCoef;
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
        
        // The margins shrink as bars are added. JFreeChart takes a negative one, and draws the bars at the ends out of
        // the chart, so from 16 bars on they keep a small one, and the bars keep apart by a fifth of the chart
        double space = 0.475 - (bars * 0.03);
        double categoryMargin = 0.01;
        if (space < 0.01) {
            space = 0.01;
            categoryMargin = 0.2;
        }
        categoryAxis.setLowerMargin(space);
        categoryAxis.setUpperMargin(space);
        categoryAxis.setCategoryMargin(categoryMargin);
        

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
        
        double minRangeUtility = Double.POSITIVE_INFINITY;
        double maxRangeUtility = Double.NEGATIVE_INFINITY;
        double minOfTheShown = Double.POSITIVE_INFINITY;
        double maxOfTheShown = Double.NEGATIVE_INFINITY;
        // With a ratio or a percentage of the probability, the variation of each parameter is measured in proportion
        // to its reference value, which cannot be done when it is 0: those parameters are left out and named below
        // the title (error 317). Dividing by 0 left their line out and, with the percentage, made the axis infinite,
        // so that no line was drawn
        List<String> notShown = new ArrayList<>();
        
        HashMap<UncertainParameter, DomainInterval> sampledIntervals = veSensAnTornadoSpider.getSampledInterval();
        for (UncertainParameter uncertainParameter : uncertainParameters) {
            if (isVariationTypePOPPOrRORV) {
                double referenceValue = uncertainParameter.getBaseLineValue();
                if (referenceValue == 0) {
                    notShown.add(uncertainParameter.getLabel());
                    continue;
                }
                DomainInterval sampledInterval = sampledIntervals.get(uncertainParameter);
                minVariationValue = -Math.abs(sampledInterval.min() - referenceValue) / Math.abs(referenceValue);
                maxVariationValue = Math.abs(sampledInterval.max() - referenceValue) / Math.abs(referenceValue);
                minOfTheShown = Math.min(minOfTheShown, minVariationValue);
                maxOfTheShown = Math.max(maxOfTheShown, maxVariationValue);
                variationInterval = (maxVariationValue - minVariationValue) / iterations;
            }
            TablePotential uncertainParameterPotential = veSensAnTornadoSpider.getUncertainParametersPotentials()
                                                                              .get(uncertainParameter);
            XYSeries series = new XYSeries(uncertainParameter.getLabel());
            int seriesIndex = 0;
            
            // Get the count of the iteration
            int horizontalIteration = 0;
            double[] potentials = uncertainParameterPotential.getValues();
            if (decisionVariable != null) {
                potentials = differences(potentials, iterations, interventionSpiderDecisionSelector.getSelectedIndex(),
                                         relativeSpiderDecisionSelector.getSelectedIndex());
            }
            for (int i = 0; i <= iterations; i++) {
                double value = potentials[i];
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
            minVariationValue = minOfTheShown;
            maxVariationValue = maxOfTheShown;
        }
        // Without a line that spans an interval, as when every parameter is left out, the axis keeps its own range
        if (minVariationValue < maxVariationValue) {
            domainAxis.setRange(minVariationValue - (maxVariationValue - minVariationValue) * 0.02, maxVariationValue + (maxVariationValue - minVariationValue) * 0.02);
            double size = (domainAxis.getUpperBound() - domainAxis.getLowerBound()) / 10;
            domainAxis.setTickUnit(new NumberTickUnit(size, new DecimalFormat()));
        }
        domainAxis.setNumberFormatOverride(decimalFormat);
        if (!notShown.isEmpty()) {
            chart.addSubtitle(new TextTitle(stringDatabase.getString("SensitivityAnalysis.General.NotShownInTheSpider")
                                                          .replace("~", String.join(", ", notShown))));
        }
        
        // Set the range axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        double[] axisRange = PlotDialog.axisRange(minRangeUtility, maxRangeUtility);
        rangeAxis.setRange(axisRange[0], axisRange[1]);
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
        comboBox.setRenderer(new JComboBoxFunctionRender<>(State::getName));
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
