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
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEPSA;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dialog that shows the scatter plot and the acceptability curve for a probabilistic cost-effectiveness analysis
 *
 * @author jperez-martin
 */
public class CEProbabilisticDialog extends JDialog {
    
    private static final int DEFAULT_LAMBDA = 30000;
    boolean moreThanOneInterval;
    /**
     * Tabbed pane
     */
    private JTabbedPane tabbedPane;
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
    /**
     * Task to perform the cost-effectiveness probability sensitivity analysis
     */
    private VECEPSA vecepsa = null;
    private List<GTablePotential> psaResults;
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    private double evaluationWTP;
    private double referenceWTP;
    /**
     * GUI controls
     */
    private JRadioButton absoluteRadioButton;
    private JRadioButton relativeRadioButton;
    private JComboBox<State> relativeDecisionSelector;
    private boolean[] selectedStates;
    private ChartPanel ceChartPanel;
    private ChartPanel ceacChartPanel;
    private JPanel cePlanePanel;
    private JPanel ceacPanel;
    private JSlider cePlaneLambdaSelector;
    private JSlider ceacLambdaSelector;
    private JTextField cePlaneLambdaSelectorTextField;
    private JTextField ceacLambdaSelectorTextField;
    private SensitivityAnalysisModel sensitivityAnalysisModel;
    
    /**
     * Dialog with map analysis
     *
     * @param owner                    Owner window
     * @param probNet                  ProbNet
     * @param preResolutionEvidence    PreResolutionEvidence (included the scenario if applies)
     * @param sensitivityAnalysisModel Sensitivity model parameters
     */
    public CEProbabilisticDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence,
                                 SensitivityAnalysisModel sensitivityAnalysisModel) throws IncompatibleEvidenceException, NonProjectablePotentialException, NotSupportedOperationException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(owner);
        
        this.probNet = probNet;
        this.iterations = sensitivityAnalysisModel.getNumberOfIterationsSimulations();
        this.decisionVariable = sensitivityAnalysisModel.getDecisionVariable();
        this.selectedScenario = sensitivityAnalysisModel.getSelectedScenario();
        this.selectedStates = new boolean[decisionVariable.getNumStates()];
        this.sensitivityAnalysisModel = sensitivityAnalysisModel;
        
        // Run the task with uncertainty
        vecepsa = new VECEPSA(probNet);
        vecepsa.setPreResolutionEvidence(preResolutionEvidence);
        vecepsa.setDecisionVariable(sensitivityAnalysisModel.getDecisionVariable());
        vecepsa.setNumSimulations(sensitivityAnalysisModel.getNumberOfIterationsSimulations());
        vecepsa.setUseMultithreading(false);
        
        this.evaluationWTP = DEFAULT_LAMBDA;
        this.referenceWTP = DEFAULT_LAMBDA;
        this.psaResults = new ArrayList<>();
        
        // Get thresholds of each analysis
        List<Double> thresholds = new ArrayList<>();
        List<GTablePotential> cepPotentials = (List<GTablePotential>) vecepsa.getCEPPotentials();
        for (GTablePotential potential : cepPotentials) {
            
            if (!moreThanOneInterval) {
                for (int i = 0; i < potential.elementTable.size(); i++) {
                    CEP cep = (CEP) potential.elementTable.get(i);
                    if (cep.getNumIntervals() != 1) {
                        moreThanOneInterval = true;
                        break;
                    }
                }
            }
            
            psaResults.add(potential);
        }
        
        
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
     * Gets the content pane with the tabbed pane
     *
     * @return content pane
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
            tabbedPane.addTab(stringDatabase.getString("SensitivityAnalysis.Type.CEPlane"), null, getCEPlanePanel(),
                              null);
            tabbedPane.addTab(stringDatabase.getString("SensitivityAnalysis.Type.AcceptabilityCurve"), null,
                              getAcceptabilityCurvePanel(), null);
        }
        return tabbedPane;
    }
    
    public JPanel getCEPlanePanel() {
        cePlanePanel = new JPanel();
        cePlanePanel.setLayout(new BorderLayout());
        if (moreThanOneInterval) {
            cePlanePanel.add(getIntervalsPanel(AnalysisTab.CEPLANE), BorderLayout.WEST);
        }
        cePlanePanel.add(getAbsRelShowHidePanel(), BorderLayout.EAST);
        cePlanePanel.add(getCEPlaneChartPanel(), BorderLayout.CENTER);
        
        return cePlanePanel;
        
    }
    
    public JPanel getAcceptabilityCurvePanel() {
        ceacPanel = new JPanel();
        ceacPanel.setLayout(new BorderLayout());
        ceacPanel.add(getCEACChartPanel(), BorderLayout.CENTER);
        JScrollPane showHideScrollPane = getShowHidePanel();
        showHideScrollPane.setPreferredSize(new Dimension(150, 0));
        ceacPanel.add(showHideScrollPane, BorderLayout.EAST);
        ceacPanel.add(getIntervalsPanel(AnalysisTab.ACCEPTABILITY_CURVE), BorderLayout.WEST);
        //        ceacPanel.add(getWTPShowHidePanel(), BorderLayout.EAST);
        return ceacPanel;
    }
    
    /**
     * Get the intervals panel with all the compact intervals
     *
     * @return
     */
    public JPanel getIntervalsPanel(final AnalysisTab analysisTab) {
        
        JPanel intervalsPanel = new JPanel();
        intervalsPanel.setPreferredSize(new Dimension(120, 0));
        
        // TODO - Localize
        if (analysisTab == AnalysisTab.CEPLANE) {
            intervalsPanel.setBorder(BorderFactory.createTitledBorder("WTP for evaluation"));
        } else {
            intervalsPanel.setBorder(BorderFactory.createTitledBorder("WTP of reference"));
        }
        
        final ChangeListener sliderChangeLister = new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    if (analysisTab == AnalysisTab.CEPLANE) {
                        evaluationWTP = source.getValue();
                        cePlaneLambdaSelectorTextField.setText(String.valueOf(evaluationWTP));
                    } else if (analysisTab == AnalysisTab.ACCEPTABILITY_CURVE) {
                        referenceWTP = source.getValue();
                        ceacLambdaSelectorTextField.setText(String.valueOf(referenceWTP));
                    }
                    refreshChartPanels();
                }
            }
        };
        
        intervalsPanel.setLayout(new BorderLayout());
        JTextField lambdaSelectorTextField = new JTextField(10);
        lambdaSelectorTextField.setText(String.valueOf(DEFAULT_LAMBDA));
        lambdaSelectorTextField.setHorizontalAlignment(SwingConstants.CENTER);
        lambdaSelectorTextField.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != 'E') {
                    e.consume();
                }
            }
        });
        lambdaSelectorTextField.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JSlider lambdaSelector;
                if (analysisTab == AnalysisTab.CEPLANE) {
                    lambdaSelector = cePlaneLambdaSelector;
                    lambdaSelector.removeChangeListener(sliderChangeLister);
                    evaluationWTP = Double.parseDouble(cePlaneLambdaSelectorTextField.getText());
                    cePlaneLambdaSelector.setValue((int) evaluationWTP);
                    lambdaSelector.addChangeListener(sliderChangeLister);
                } else if (analysisTab == AnalysisTab.ACCEPTABILITY_CURVE) {
                    lambdaSelector = ceacLambdaSelector;
                    lambdaSelector.removeChangeListener(sliderChangeLister);
                    referenceWTP = Double.parseDouble(ceacLambdaSelectorTextField.getText());
                    ceacLambdaSelector.setValue((int) referenceWTP);
                    lambdaSelector.addChangeListener(sliderChangeLister);
                }
                refreshChartPanels();
            }
        });
        
        intervalsPanel.add(lambdaSelectorTextField, BorderLayout.NORTH);
        if (analysisTab == AnalysisTab.CEPLANE) {
            cePlaneLambdaSelectorTextField = lambdaSelectorTextField;
        } else if (analysisTab == AnalysisTab.ACCEPTABILITY_CURVE) {
            ceacLambdaSelectorTextField = lambdaSelectorTextField;
        }
        
        JSlider lambdaSelector = new JSlider();
        lambdaSelector.setOrientation(SwingConstants.VERTICAL);
        lambdaSelector.setMinimum(0);
        lambdaSelector.setMaximum(100000);
        lambdaSelector.setValue(DEFAULT_LAMBDA);
        intervalsPanel.add(lambdaSelector, BorderLayout.CENTER);
        
        lambdaSelector.addChangeListener(sliderChangeLister);
        
        if (analysisTab == AnalysisTab.CEPLANE) {
            cePlaneLambdaSelector = lambdaSelector;
        } else if (analysisTab == AnalysisTab.ACCEPTABILITY_CURVE) {
            ceacLambdaSelector = lambdaSelector;
        }
        
        return intervalsPanel;
    }
    
    /**
     * Build the right column with both panels
     *
     * @return
     */
    public JPanel getAbsRelShowHidePanel() {
        JPanel absRelShowHidePanel = new JPanel();
        absRelShowHidePanel.setPreferredSize(new Dimension(150, 0));
        absRelShowHidePanel.setLayout(new BorderLayout());
        absRelShowHidePanel.add(getAbsoluteRelativePanel(), BorderLayout.NORTH);
        absRelShowHidePanel.add(getShowHidePanel(), BorderLayout.SOUTH);
        
        return absRelShowHidePanel;
    }
    
    /**
     * Returns the scroll pane with the absolute/relative functionality
     *
     * @return
     */
    public JPanel getAbsoluteRelativePanel() {
        JPanel absoluteRelativePanel = new JPanel();
        // TODO - LOCALIZE
        absoluteRelativePanel.setBorder(new TitledBorder("Display:"));
        absoluteRelativePanel.setLayout(new BoxLayout(absoluteRelativePanel, BoxLayout.PAGE_AXIS));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        absoluteRadioButton = new JRadioButton("Absolute");
        relativeRadioButton = new JRadioButton("Relative to:");
        absoluteRadioButton.setSelected(true);
        absoluteRadioButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                relativeDecisionSelector.setEnabled(false);
                refreshChartPanels();
            }
        });
        
        relativeRadioButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                relativeDecisionSelector.setEnabled(true);
                refreshChartPanels();
            }
        });
        
        JPanel absoluteRadioButtonPanel = new JPanel();
        absoluteRadioButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        absoluteRadioButtonPanel.add(absoluteRadioButton);
        absoluteRelativePanel.add(absoluteRadioButtonPanel);
        
        JPanel relativeRadioButtonPanel = new JPanel();
        relativeRadioButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        relativeRadioButtonPanel.add(relativeRadioButton);
        absoluteRelativePanel.add(relativeRadioButtonPanel);
        
        buttonGroup.add(absoluteRadioButton);
        buttonGroup.add(relativeRadioButton);
        
        relativeDecisionSelector = new JComboBox<>();
        for (State state : decisionVariable.getStates()) {
            relativeDecisionSelector.addItem(state);
        }
        relativeDecisionSelector.setEnabled(false);
        relativeDecisionSelector.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                refreshChartPanels();
            }
        });
        
        absoluteRelativePanel.add(relativeDecisionSelector);
        absoluteRelativePanel.add(new JPanel());
        
        return absoluteRelativePanel;
    }
    
    /**
     * Returns the scroll pane with the show/hide functionality
     *
     * @return
     */
    public JScrollPane getShowHidePanel() {
        JPanel showHidePanel = new JPanel();
        // TODO - LOCALIZE
        showHidePanel.setBorder(new TitledBorder("Show/hide interventions"));
        showHidePanel.setLayout(new BoxLayout(showHidePanel, BoxLayout.PAGE_AXIS));
        
        List<JCheckBox> showHideCheckBoxes = new ArrayList();
        for (int stateIndex = 0; stateIndex < decisionVariable.getNumStates(); stateIndex++) {
            JCheckBox stateCheckbox = new JCheckBox(decisionVariable.getStateName(stateIndex));
            stateCheckbox.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBox checkBox = (JCheckBox) e.getSource();
                    // Update selected (show) or unselected (hide) states of the decision
                    for (int stateIndex = 0; stateIndex < decisionVariable.getNumStates(); stateIndex++) {
                        if (decisionVariable.getStateName(stateIndex).equals(checkBox.getText())) {
                            selectedStates[stateIndex] = checkBox.isSelected();
                        }
                    }
                    refreshChartPanels();
                }
            });
            stateCheckbox.setSelected(true);
            selectedStates[stateIndex] = true;
            showHideCheckBoxes.add(stateCheckbox);
            showHidePanel.add(stateCheckbox);
        }
        
        JScrollPane scrollPane = new JScrollPane(showHidePanel);
        scrollPane.setBorder(new EmptyBorder(2, 2, 2, 2));
        //        scrollPane.setPreferredSize(new Dimension(150,0));
        return scrollPane;
    }
    
    /**
     * Repaint and refresh the chart panel and its components
     */
    private void refreshChartPanels() {
        
        cePlanePanel.remove(ceChartPanel);
        cePlanePanel.add(getCEPlaneChartPanel(), BorderLayout.CENTER);
        
        ceacPanel.remove(ceacChartPanel);
        ceacPanel.add(getCEACChartPanel(), BorderLayout.CENTER);
        this.setVisible(false);
        this.setVisible(true);
    }
    
    /**
     * Get cost-effectiveness plane chart
     *
     * @return
     */
    public ChartPanel getCEPlaneChartPanel() {
        // JFreeChart attributes definition
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        double baseCost;
        double baseEffectiveness;
        
        // For each state of the decision
        for (int cepIndex = 0; cepIndex < decisionVariable.getNumStates(); cepIndex++) {
            
            // If the serie is hidden, skip it from JFreeChart
            if (!selectedStates[cepIndex]) {
                continue;
            }
            
            XYSeries series = new XYSeries(decisionVariable.getStateName(cepIndex));
            
            for (GTablePotential gTablePotential : psaResults) {
                //                CEP[] cepsForDecision = (CEP[]) gTablePotential.elementTable.toArray();
                ArrayList<CEP> cepsForDecision = (ArrayList<CEP>) gTablePotential.elementTable;
                
                // Relative
                if (relativeRadioButton.isSelected()) {
                    int indexSelected = relativeDecisionSelector.getSelectedIndex();
                    baseCost = cepsForDecision.get(indexSelected).getCost(evaluationWTP);
                    baseEffectiveness = cepsForDecision.get(indexSelected).getEffectiveness(evaluationWTP);
                    
                    // Absolute
                } else {
                    baseCost = 0;
                    baseEffectiveness = 0;
                }
                
                CEP cep = cepsForDecision.get(cepIndex);
                double cost = cep.getCost(evaluationWTP);
                cost -= baseCost;
                
                double effectiveness = cep.getEffectiveness(evaluationWTP);
                effectiveness -= baseEffectiveness;
                
                series.add(effectiveness, cost);
            }
            
            dataset.addSeries(series);
        }
        
        // Set the JFreeChart parameters call
        // TODO - LOCALIZE
        JFreeChart chart = ChartFactory.createScatterPlot("Cost-effectiveness plane", // Chart title
                                                          "Effectiveness",                                                    // X axis label
                                                          "Cost",                                               // Y axis label
                                                          dataset,                                            // data
                                                          PlotOrientation.VERTICAL,                            // Orientation
                                                          true,                                                // Legend
                                                          true,                                                // ToolTips
                                                          false                                                // Urls
        );
        
        // Set general aspects
        chart.getLegend().setPosition(RectangleEdge.RIGHT);
        
        ceChartPanel = new ChartPanel(chart);
        ceChartPanel.setAutoscrolls(true);
        ceChartPanel.setDisplayToolTips(true);
        ceChartPanel.setMouseZoomable(true);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        NumberFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", format, format);
        // TODO Manolo> When migrating to JFreeChart 1.5, I have replaced the call to the method setBaseToolTipGenerator by the new method setDefaultToolTipGenerator  
        //renderer.setBaseToolTipGenerator(generator);
        renderer.setDefaultToolTipGenerator(generator);
        
        
        return ceChartPanel;
    }
    
    public ChartPanel getCEACChartPanel() {
        // JFreeChart attributes definition
        XYDataset dataset = createCEACDataset();
        
        // Set the JFreeChart parameters call
        JFreeChart chart = ChartFactory
                .createXYLineChart(stringDatabase.getString("CostEffectivenessResults.AcceptabilityCurve"),
                                   stringDatabase.getString("CostEffectivenessResults.AcceptabilityCurve.Horizontal"),
                                   stringDatabase.getString("CostEffectivenessResults.AcceptabilityCurve.Vertical"), dataset,
                                   PlotOrientation.VERTICAL, true, true, true);
        
        ceacChartPanel = new ChartPanel(chart);
        XYPlot plot = (XYPlot) chart.getPlot();
        
        Marker marker = new ValueMarker(referenceWTP);
        marker.setLabel("WTP");
        marker.setPaint(GUIColors.General.TEXT.getColor());
        marker.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
        plot.addDomainMarker(marker);
        
        ceacChartPanel.setAutoscrolls(true);
        ceacChartPanel.setDisplayToolTips(true);
        ceacChartPanel.setMouseZoomable(true);
        
        XYItemRenderer renderer = plot.getRenderer();
        NumberFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", format, format);
        renderer.setDefaultToolTipGenerator(generator);
        
        // Set general aspects
        chart.getLegend().setPosition(RectangleEdge.RIGHT);
        
        return ceacChartPanel;
    }
    
    private XYDataset createCEACDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        boolean atLeastOneSerie = false;
        for (int stateIndex = 0; stateIndex < decisionVariable.getNumStates(); stateIndex++) {
            if (selectedStates[stateIndex]) {
                atLeastOneSerie = true;
                XYSeries series = new XYSeries(decisionVariable.getStateName(stateIndex));
                dataset.addSeries(series);
            }
        }
        
        if (!atLeastOneSerie) {
            return dataset;
        }
        
        double maxLambda = referenceWTP * 2;
        
        for (int i = 0; i < 1000; i++) {
            double lambda = maxLambda * i / 1000;
            
            double[] winnersForEachSerie = new double[dataset.getSeries().size()];
            
            for (GTablePotential gTablePotential : psaResults) {
                
                int bestSeriesIndex = -1;
                double maxNetMonetaryBenefit = Double.NEGATIVE_INFINITY;
                
                int seriesIndex = 0;
                for (int stateIndex = 0; stateIndex < decisionVariable.getNumStates(); stateIndex++) {
                    if (selectedStates[stateIndex]) {
                        double netMonetaryBenefit = ((CEP) gTablePotential.elementTable.get(stateIndex))
                                .getNetMonetaryBenefit(lambda);
                        
                        if (netMonetaryBenefit > maxNetMonetaryBenefit) {
                            maxNetMonetaryBenefit = netMonetaryBenefit;
                            bestSeriesIndex = seriesIndex;
                        }
                        seriesIndex++;
                    }
                }
                if (bestSeriesIndex != -1) {
                    winnersForEachSerie[bestSeriesIndex]++;
                }
            }
            
            for (int seriesIndex = 0; seriesIndex < dataset.getSeries().size(); seriesIndex++) {
                ((XYSeries) dataset.getSeries().get(seriesIndex)).add(lambda,
                                                                      winnersForEachSerie[seriesIndex] / sensitivityAnalysisModel.getNumberOfIterationsSimulations());
            }
            
        }
        return dataset;
    }
    
    private enum AnalysisTab {
        CEPLANE, ACCEPTABILITY_CURVE
    }
}
