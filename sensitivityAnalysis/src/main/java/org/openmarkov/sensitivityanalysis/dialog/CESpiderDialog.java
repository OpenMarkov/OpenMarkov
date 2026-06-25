/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 license
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleEdge;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.component.NumericSpinner;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECESensAnSpider;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Create the spider plot for cost-effectiveness sensitivity analysis.
 *
 * @author ahmed
 *
 */
public class CESpiderDialog extends JDialog {
    
    private static final long serialVersionUID = 2490213845932653160L;
    private static final int DEFAULT_LAMBDA = 30000;
    
    /**
     * Number of points/iterations in the sensitivity range of each parameter
     */
    private int iterations;
    /**
     * List of selected uncertain parameters
     */
    private List<UncertainParameter> uncertainParameters;
    /**
     * Variation of axis
     */
    private AxisVariation axisVariation;
    /**
     * Conditioned decision variable for the decision scope
     */
    private Variable decisionVariable;
    /**
     * Specific scenario based in previous evidence (decision nodes)
     */
    private EvidenceCase preResolutionEvidence;
    /**
     * ProbNet
     */
    private ProbNet probNet;
    /**
     * Task to perform the cost-effectiveness probability sensitivity analysis
     */
    /**
     * Panel for WTP selector
     */
    private JPanel WTPPanel;
    
    
    private VECESensAnSpider veCESensAnSpider = null;
    private HashMap<UncertainParameter, List<GTablePotential>> uncertainParametersPotentials;
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    private double evaluationWTP;
    
    /**
     * GUI controls for 'interventions' and WTP
     */
    private JComboBox<State> interventionDecisionSelector;
    private JComboBox<State> relativeDecisionSelector;
    private ButtonGroup valuesDecisionSelector;
    private JRadioButton absoluteRadioButton;
    private JRadioButton relativeToRadioButton;
    private ChartPanel chartPanel;
    private JPanel spiderPanel;
    private JSlider spiderLambdaSelector;
    private NumericSpinner<Double> spiderLambdaSelectorTextField;
    private JPanel decisionSelectorPanel;
    
    /**
     * // JFreeChart attributes definition
     */
    private XYSeriesCollection dataset;
    private XYLineAndShapeRenderer renderer;
    private XYPlot plot;
    private JFreeChart chart;
    
    /**
     * Dialog with spider cost-effectiveness analysis
     *
     * @param owner                    Owner window
     * @param probNet                  ProbNet
     * @param preResolutionEvidence    PreResolutionEvidence (included the scenario
     *                                 if applies)
     * @param sensitivityAnalysisModel Sensitivity model parameters
     */
    public CESpiderDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence,
                          SensitivityAnalysisModel sensitivityAnalysisModel) throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, ConstraintViolatedException {
        super(owner);
        
        this.probNet = probNet;
        this.iterations = sensitivityAnalysisModel.getNumberOfIterationsSimulations();
        this.decisionVariable = sensitivityAnalysisModel.getDecisionVariable();
        this.uncertainParameters = sensitivityAnalysisModel.getSelectedUncertainParametersXAxis();
        this.axisVariation = sensitivityAnalysisModel.getHorizontalAxisVariation();
        this.preResolutionEvidence = preResolutionEvidence;
        this.evaluationWTP = DEFAULT_LAMBDA;
        runSensitivityAnalysisTask();
        initializeDialog(owner);
    }
    
    /**
     * Run the sensitivity analysis for each parameter with uncertainty
     */
    
    private void runSensitivityAnalysisTask() throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {
        veCESensAnSpider = new VECESensAnSpider(probNet);
        veCESensAnSpider.setPreResolutionEvidence(preResolutionEvidence);
        veCESensAnSpider.setDecisionVariable(decisionVariable);
        veCESensAnSpider.setNumIterations(iterations);
        veCESensAnSpider.setUncertainParameters(uncertainParameters);
        veCESensAnSpider.setAxisVariation(axisVariation);
        
        this.uncertainParametersPotentials = veCESensAnSpider.getCEPotentials();
        System.out.println(uncertainParametersPotentials);
        
    }
    
    /**
     * Initializes the dialog (set the main panel title, icon, and content)
     *
     */
    private void initializeDialog(Window owner) {
        this.setTitle("OpenMarkov - " + StringDatabase.getUniqueInstance().getString("SensitivityAnalysis.Title")
                              + " - " + probNet.getName());
        this.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
        
        setContentPane(getCESpiderPanel());
        setDialogBounds(owner);
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(getWidth(), getHeight() / 2));
        setResizable(true);
        pack();
        this.setVisible(true);
        
    }
    
    /**
     * Sets the dialog bounds based on the owner window's size.
     */
    private void setDialogBounds(Window owner) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = owner.getBounds();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;
        int x = bounds.x / 2 - width / 2;
        int y = bounds.y / 2 - height / 2;
        this.setBounds(x, y, width, height);
        
    }
    
    /**
     * Gets the main panel with the spider chart and related controls
     *
     * @return content panel
     */
    public JPanel getCESpiderPanel() {
        spiderPanel = new JPanel();
        spiderPanel.setLayout(new BorderLayout());
        
        WTPPanel = getWTPpanel();
        spiderPanel.add(WTPPanel, BorderLayout.WEST);
        spiderPanel.add(getOptionsChartPanel(), BorderLayout.EAST);
        spiderPanel.add(getChartPanel(), BorderLayout.CENTER);
        
        // Initially set the visibility based on the radio button (that is initially 'relative to'
        WTPPanel.setVisible(relativeToRadioButton.isSelected());
        
        return spiderPanel;
    }
    
    /**
     * Get the WTP panel
     *
     * @return
     */
    public JPanel getWTPpanel() {
        JPanel WTPPanel = new JPanel(new BorderLayout());
        WTPPanel.setPreferredSize(new Dimension(120, 0));
        WTPPanel.setBorder(
                BorderFactory.createTitledBorder(stringDatabase.getString("SensitivityAnalysis.General.WTP")));
        
        WTPPanel.add(getWTPselectorTextField(), BorderLayout.NORTH);
        WTPPanel.add(getWTPslider(), BorderLayout.CENTER);
        
        return WTPPanel;
    }
    
    /**
     * Creates the number field for WTP selection.
     */
    private NumericSpinner getWTPselectorTextField() {
        spiderLambdaSelectorTextField = new NumericSpinner<Double>(Double.class);
        spiderLambdaSelectorTextField.setMinimum(1.0);
        spiderLambdaSelectorTextField.setValue(DEFAULT_LAMBDA);
        //spiderLambdaSelectorTextField.setHorizontalAlignment(SwingConstants.CENTER);
        spiderLambdaSelectorTextField.addChangeListener(e -> updateWTPselector());
        return spiderLambdaSelectorTextField;
    }
    
    /**
     * Updates the WTP slider based on the value in the foreground field.
     */
    private void updateWTPselector() {
        spiderLambdaSelector.removeChangeListener(sliderChangeListener);
        evaluationWTP = spiderLambdaSelectorTextField.getCurrentValue();
        spiderLambdaSelector.setValue((int) evaluationWTP);
        spiderLambdaSelector.addChangeListener(sliderChangeListener);
        refreshChartPanels();
    }
    
    /**
     * Creates the slider for WTP selection.
     */
    private JSlider getWTPslider() {
        spiderLambdaSelector = new JSlider(SwingConstants.VERTICAL, 1, 100000, DEFAULT_LAMBDA);
        spiderLambdaSelector.addChangeListener(sliderChangeListener);
        spiderLambdaSelector.setForeground(GUIColors.CostEffectiveness.WTP_SLOPE.getColor());
        return spiderLambdaSelector;
    }
    
    /**
     * Change listener for the WTP slider.
     */
    private final ChangeListener sliderChangeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (!spiderLambdaSelector.getValueIsAdjusting()) {
                evaluationWTP = spiderLambdaSelector.getValue();
                spiderLambdaSelectorTextField.setValue(evaluationWTP);
                refreshChartPanels();
            }
        }
    };
    
    /**
     * Builds the right panel for the chart, including decision selectors
     *
     * @return
     */
    public JPanel getOptionsChartPanel() {
        if(optionsChartPanel != null) {
            return optionsChartPanel;
        }
        optionsChartPanel = new JPanel();
        optionsChartPanel.setMinimumSize(new Dimension(140, 0));
        optionsChartPanel.setMaximumSize(new Dimension(240, Integer.MAX_VALUE));
        optionsChartPanel.setLayout(new VerticalLayout());
        
        // Panel for interventions an values
        decisionSelectorPanel = new JPanel();
        decisionSelectorPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        decisionSelectorPanel.setLayout(new BoxLayout(decisionSelectorPanel, BoxLayout.Y_AXIS));
        // Options for interventions and values
        if (interventionDecisionSelector == null) {
            interventionDecisionSelector = createComboBox(decisionVariable.getStates().length > 1 ? 1 : 0);
        }
        
        decisionSelectorPanel.add(createLabel("Decision"));
        JTextField decisionVariableName = new JTextField(this.decisionVariable.getName());
        decisionVariableName.setBackground(Color.WHITE);
        decisionVariableName.setEditable(false);
        decisionSelectorPanel.add(decisionVariableName);
        decisionSelectorPanel.add(generateSeparator(JSeparator.HORIZONTAL, 10));
        
        
        decisionSelectorPanel.add(createLabel("intervention"));
        
        decisionSelectorPanel.add(interventionDecisionSelector);
        
        decisionSelectorPanel.add(generateSeparator(JSeparator.HORIZONTAL, 20));
        decisionSelectorPanel.add(createLabel("values"));

        absoluteRadioButton = createRadioButton("absolute", false);
        relativeToRadioButton = createRadioButton("relativeTo", true);
        
        JPanel relativenessPanel = new JPanel(new VerticalLayout());
        relativenessPanel.add(absoluteRadioButton);
        relativenessPanel.add(relativeToRadioButton);
        
        decisionSelectorPanel.add(relativenessPanel);
        
        valuesDecisionSelector = new ButtonGroup();
        valuesDecisionSelector.add(absoluteRadioButton);
        valuesDecisionSelector.add(relativeToRadioButton);
        
        relativeDecisionSelector = createComboBox(0);
        decisionSelectorPanel.add(relativeDecisionSelector);
        
        optionsChartPanel.add(decisionSelectorPanel, BorderLayout.NORTH);
        return optionsChartPanel;
    }
    
    private static @NotNull JSeparator generateSeparator(int direction, int size) {
        JSeparator separator = new JSeparator();
        separator.setOrientation(direction);
        separator.setPreferredSize(new Dimension(size, size));
        separator.setBackground(GUIColors.General.TRANSPARENT.getColor());
        separator.setForeground(GUIColors.General.TRANSPARENT.getColor());
        return separator;
    }
    
    /**
     * Creates the main chart panel displaying the spider diagram.
     *
     * @return
     */
    public ChartPanel getChartPanel() {
        setChart();
        createDataset();
        return chartPanel;
    }
    
    private void setChart() {
        dataset = new XYSeriesCollection();
        renderer = new XYLineAndShapeRenderer();
        // Set the JFreeChart parameters call
        chart = ChartFactory.createScatterPlot(stringDatabase.getString("SensitivityAnalysis.Type.CESpiderDiagram"), // title
                                               stringDatabase.getString("SensitivityAnalysis.General.Effectiveness"), // X axis label
                                               stringDatabase.getString("SensitivityAnalysis.General.Cost"), // Y axis label
                                               dataset, PlotOrientation.VERTICAL, true, true, false);
        
        plot = (XYPlot) chart.getPlot();
        // Set general aspects
        chart.getLegend().setPosition(RectangleEdge.RIGHT);
        chart.setBackgroundPaint(GUIColors.CostEffectiveness.BACKGROUND.getColor());
        chartPanel = new ChartPanel(chart);
        chartPanel.setAutoscrolls(true);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseZoomable(true);
        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(GUIColors.CostEffectiveness.BACKGROUND.getColor());
        plot.setRenderer(renderer);
        NumberFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", format, format);
        renderer.setDefaultToolTipGenerator(generator);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        
        Marker horizontalLine = new ValueMarker(0.0);
        horizontalLine.setPaint(Color.BLACK);
        horizontalLine.setStroke(new BasicStroke(0.5f));
        
        Marker verticalLine = new ValueMarker(0.0);
        verticalLine.setPaint(Color.BLACK);
        verticalLine.setStroke(new BasicStroke(0.5f));
        
        
        plot.addRangeMarker(horizontalLine);
        plot.addDomainMarker(verticalLine);
        
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    }
    
    /**
     * Setting sensitivity analysis data in the chart
     */
    private void createDataset() {
        // Setting reference values for referenced chart
        double baseCost;
        double baseEffectiveness;
        double maxCost = 0;
        double costWTP = 0;
        
        // Index of intervention and reference states of the decision
        int relativeDecisionIndex = relativeDecisionSelector.getSelectedIndex();
        int interventionDecisionIndex = interventionDecisionSelector.getSelectedIndex();
        
        // For each parameter we get the cost and effectiveness in the uncertainty interval
        for (UncertainParameter uncertainParameter : uncertainParameters) {
            
            // Potentials for the uncertainty parameter
            List<GTablePotential> uncertParamPotentials = uncertainParametersPotentials.get(uncertainParameter);
            
            // For each state of the decision considered
            for (int cepIndex = 0; cepIndex < decisionVariable.getNumStates(); cepIndex++) {
                // No unselected state is displayed (intervention or reference)
                if (relativeDecisionIndex != cepIndex && interventionDecisionIndex != cepIndex)
                    continue;
                // No intervention matching reference decision is displayed
                if (cepIndex == relativeDecisionIndex)
                    continue;
                // One series for each parameter no considered
                XYSeries series = new XYSeries(uncertainParameter.getName());
                
                for (GTablePotential gTablePotential : uncertParamPotentials) {
                    ArrayList<CEP> cepsForDecision = (ArrayList<CEP>) gTablePotential.elementTable;
                    // Reference state potentials
                    if (relativeToRadioButton.isSelected()) {
                        baseCost = cepsForDecision.get(relativeDecisionIndex).getCost(evaluationWTP);
                        baseEffectiveness = cepsForDecision.get(relativeDecisionIndex).getEffectiveness(evaluationWTP);
                    } else {
                        baseCost = 0;
                        baseEffectiveness = 0;
                    }
                    // Potentials of the parameter considered
                    CEP cep = cepsForDecision.get(cepIndex);
                    double cost = cep.getCost(evaluationWTP);
                    cost -= baseCost;
                    // WTP line (setting the line to cover the highest value of the cost of all series)
                    if (relativeToRadioButton.isSelected()) {
                        maxCost = Math.max(Math.abs(cost), maxCost);
                        if (Math.abs(cost) == maxCost) {
                            costWTP = cost;
                        }
                    }
                    double effectiveness = cep.getEffectiveness(evaluationWTP);
                    effectiveness -= baseEffectiveness;
                    
                    series.add(effectiveness, cost);
                }
                dataset.addSeries(series);
            }
        }
        // Showing WTP line for 'relative' chart
        if (relativeToRadioButton.isSelected()) {
            XYSeries wtpSeries = new XYSeries("WTP");
            
            wtpSeries.add(0, 0);
            wtpSeries.add(costWTP / evaluationWTP, costWTP);
            
            // Set properties for WTP line
            dataset.addSeries(wtpSeries);
            int wtpSeriesIndex = dataset.getSeriesIndex("WTP");
            renderer.setSeriesShapesVisible(wtpSeriesIndex, false);
            renderer.setSeriesVisibleInLegend(wtpSeriesIndex, false);
            renderer.setSeriesPaint(wtpSeriesIndex, GUIColors.CostEffectiveness.WTP_SLOPE.getColor());
            renderer.setSeriesStroke(wtpSeriesIndex, new BasicStroke(
                    3.0f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[]{2.0f, 9.0f},
                    0.0f
            ));
            

            
            // Making origin visible
            XYSeries referenceSeries = new XYSeries(stringDatabase.getString("SensitivityAnalysis.General.Reference"));
            dataset.addSeries(referenceSeries);
            referenceSeries.add(0, 0);
            int referenceSeriesIndex = dataset.getSeriesIndex(stringDatabase.getString("SensitivityAnalysis.General.Reference"));
            renderer.setSeriesShapesVisible(referenceSeriesIndex, false);
            renderer.setSeriesVisibleInLegend(referenceSeriesIndex, false);
            renderer.setSeriesShapesVisible(referenceSeriesIndex, false);
            
            renderer.setSeriesShapesVisible(wtpSeriesIndex, true);
            renderer.setSeriesVisibleInLegend(wtpSeriesIndex, true);
            renderer.setSeriesShapesVisible(wtpSeriesIndex, false);
            

            
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            double lowerRangeBound = rangeAxis.getLowerBound();
            double upperRangeBound = rangeAxis.getUpperBound();
            if (lowerRangeBound == 0) {
                rangeAxis.setRange(lowerRangeBound - ((upperRangeBound - lowerRangeBound) * 0.02), upperRangeBound);
            }
            if (upperRangeBound == 0) {
                rangeAxis.setRange(lowerRangeBound, upperRangeBound + ((upperRangeBound - lowerRangeBound) * 0.02));
            }
        }
    }
    
    /**
     * Repaint and refresh the chart panels and its components
     */
    private void refreshChartPanels() {
        spiderPanel.remove(chartPanel);
        spiderPanel.add(getChartPanel(), BorderLayout.CENTER);
        //this.setVisible(false);
        //this.setVisible(true);
        this.revalidate();
    }
    
    /**
     * Creates a JPanel with a label (For the 'values' radioButton)
     *
     * @param label
     *
     * @return JPanel
     */
    private JPanel createLabel(String label) {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel(stringDatabase.getString("SensitivityAnalysis.General." + label)));
        return labelPanel;
    }
    
    /**
     * Creates a JPanel containing the 'values' of the RadioButton ('absolute' 'relative to').
     * When the selected radioButton is 'absolute', the WTPpanel is hidden
     *
     * @param label
     * @param selectedButton
     *
     * @return RadioButton
     */
    private JRadioButton createRadioButton(String label, boolean selectedButton) {
        JRadioButton radioButton = new JRadioButton(stringDatabase.getString("SensitivityAnalysis.General." + label),
                                                    selectedButton);
        
        radioButton.addActionListener(e -> {
            boolean isRelativeSelected = relativeToRadioButton.isSelected();
            relativeDecisionSelector.setEnabled(isRelativeSelected);
            // Enable or disable the WTP panel
            setPanelComponentsEnabled(WTPPanel, isRelativeSelected);
            
            refreshChartPanels();
        });
        return radioButton;
    }
    
    private static void setPanelComponentsEnabled(JPanel panel, boolean enabled) {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            component.setEnabled(enabled);
        }
    }
    
    private JComboBox<State> createComboBox(int selectedState) {
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
            JComboBox<State> other = (source == interventionDecisionSelector) ? relativeDecisionSelector
                    : interventionDecisionSelector;
            if (source.getSelectedIndex() == other.getSelectedIndex()) {
                other.setSelectedIndex((other.getSelectedIndex() + 1) % other.getItemCount());
            }
            refreshChartPanels();
        });
        return comboBox;
    }
    
    
    private JPanel optionsChartPanel;
}