/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
//import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.TemporalEvolution;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.gui.loader.element.ImageLoader;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.MIDTemporalEvolution;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VETemporalEvolution;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Plot of temporal evolution of variables in CEA
 *
 * @author myebra
 * @version 2 cmyago   16/11/2022; progress bar; reworked constructors
 */
public class TraceTemporalEvolutionDialog extends JDialog {
    private static final String CRITERION = "Criterion";
    private static final String NODEC = "No decision nodes";
    private final Dimension legendsDimension = new Dimension(200, 450);
    
    //23-24/10/2022 javadoc; added fields: temporalEvolutionByCriterion, temporalEvolutionWithDiscount, originalProbnet, isByCriterion,
    /**
     * Atemporal values (corresponding to upfront values in CEA) for @link{Criterion} of @code{#originalProbNet}.
     * This values are not discounted because they are allocated in time=0.
     */
    private final TreeMap<String, TablePotential> upfrontEvolutionByCriterion = new TreeMap<>();
    
    /**
     * Temporal evolution of each @link{Criterion} of @code{#originalProbNet} without discount.
     */
    private final TreeMap<String, List<TablePotential>> temporalEvolutionByCriterion = new TreeMap<>();
    
    
    /**
     * Discounted temporal evolution of each @link{Criterion} of @code{#originalProbNet}.
     */
    private final TreeMap<String, List<TablePotential>> temporalEvolutionByCriterionDiscount = new TreeMap<>();
    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * MID before expansion (from which temporal evolution of one of its elements is displayed)
     */
    private final ProbNet originalProbNet;
    /**
     * True if node(s) whose temporal evolution is displayed are utility nodes; false otherwise
     */
    private boolean isUtility;
    /**
     * List of conditioning variables for performing variable elimination algorithm; for temporal evolution there is only zero/one decision variable
     */
    private List<Variable> conditioningVariables;
    /**
     * Conditioning decision for displaying temporal evolution; if there is one it corresponds
     * to the first element of@code{#conditioningVariables}
     * TODO remove? At least To be used for displaying purposes when there is not decision
     */
    private Variable decisionSelected;
    /**
     * True if temporal evolution by criterion is displayed
     */
    private boolean isByCriterion = false;
    private Map<Variable, TablePotential> temporalEvolutionResults;
    /**
     * Temporal evolution of a node or set of nodes
     */
    private Map<Variable, TablePotential> temporalEvolutionDiscount;
    //end
    
    private ChartPanel chartPanel;
    private TemporalEvolutionTablePane tablePane;
    //31/10/2022; isUtility, conditioningVariables, stringDatabase, conditiningVariables made final; javadoc added; 07/11/2022; fields added: arrayXYSeriesUpfront, arrayXYSeriesDiscount, jCheckBoxUpfrontValues,jCheckBoxDiscounted,markedCheckBoxes
    private JTabbedPane tabbedPane;
    private Variable variableOfInterest;
    private ProbNet expandedNetwork;
    /**
     * True if temporal evolution is a cumulative graph and table
     */
    private boolean isCumulative;
    /**
     * Number of cycles
     */
    private int numSlices;
    /**
     * Panel with the checkboxes corresponding to the states (probabilistic variables) or criteria to be displayed
     */
    private JScrollPane checkBoxPanel;
    /**
     * List of XYSeries containing the data to be displayed; each XYSeries corresponds to a data line
     */
    private List<XYSeries> arrayXYSeries;
    
    /**
     * List of XYSeries containing the timeless utility data to be displayed; each XYSeries corresponds to a data line
     */
    private List<XYSeries> arrayXYSeriesUpfront;
    
    /**
     * List of XYSeries containing the discounted utility data to be displayed; each XYSeries corresponds to a data line
     */
    private List<XYSeries> arrayXYSeriesDiscount;
    
    
    /**
     * List of JCheckBox displayed in checkBoxPanel corresponding to the states (probabilistic variables) or criteria
     */
    private List<JCheckBox> jcheckBoxList;
    
    /**
     * JPanel containing:
     * -Left: a panel created by @code{#getChartOptionsPanel()} with the instantaneous/sum/cumulative/ radiobuttons
     * (created by @code{#getDisplayTypePanel()}), @code{#checkBoxPanel},
     * -Center: chartPanel.
     * -Right: legendPanel.
     */
    private JPanel chartPanelWithCheckBox;
    /**
     * Number of data lines in the graph
     */
    private int numberOfCombinations;
    /**
     * Radiobutton; when checked individual data is displayed
     */
    private JRadioButton radioIndividual;
    /**
     * Radiobutton; when aggregated data is displayed; the individual series corresponding to the stated selected in checkBoxPanel are added
     */
    private JRadioButton radioSummatory;
    
    /**
     * Radiobutton for utility nodes; when checked an instantaneous data line is displayed
     */
    private JRadioButton radioInstantaneus;
    /**
     * Radiobutton for utility nodes; when checked a cumulative data line is displayed
     */
    private JRadioButton radioAccumulate;
    /**
     * True if individual or instantaneous data is displayed
     */
    private boolean isIndividual;
    /**
     * JFreeChart when data is shown
     */
    private JFreeChart chart;
    //end
    
    
    /**
     * List of JLabel containing the legend for chart
     */
    private List<JLabel> legendLabels;
    /**
     * JScrollPane containing the customized legends for chart. chart own legend is disabled and used this panel instead
     */
    private JScrollPane legendPanel;
    
    
    // 07/11/2022 - checkbox for displaying upfront values
    /**
     * When checked upfront values are shown for criteria
     */
    private JCheckBox jCheckBoxUpfrontValues = null;
    
    //- checkbox for discounted/non-discounted values
    /**
     * When checked discounted values are shown for utility variables and criteria
     */
    private JCheckBox jCheckBoxDiscounted = null;
    
    //variable  with the value of the state check boxes; made field to be available for the table
    /**
     * Reflect the states to be displayed
     */
    private boolean[] markedCheckBoxes;
    
    //  end
    
    //15/11/2022 - progress bar
    private ProgressMonitor progressMonitor;
    //end
    
    //15/11/2022  constructors reworked
    private TraceTemporalEvolutionDialog(Window owner, ProbNet probNet, Variable decisionSelected) throws NotAllNodesHavePoliciesException {
        super(owner);
        this.originalProbNet = probNet;
        //10/11/2022 error message when there is more than one node without policy
        MIDTemporalEvolution.checkDecision(this.originalProbNet, this.originalProbNet.getNode(decisionSelected));
        //end
        this.numSlices = originalProbNet.getInferenceOptions().getTemporalOptions().getHorizon();
        conditioningVariables = new ArrayList<>();
        if (decisionSelected != null) {
            this.decisionSelected = decisionSelected;
            conditioningVariables.add(decisionSelected);
        } else {
            this.decisionSelected = new Variable(NODEC, NODEC);
        }
        isIndividual = true;
        //Select language; otherwise takes VM language; changing "default locales" does not work.
        UIManager.put("ProgressMonitor.progressText", stringDatabase.getString("ProgressMonitor.ProgressText"));
        UIManager.put("OptionPane.cancelButtonText", stringDatabase.getString("Cancel.Text"));
        this.progressMonitor = new ProgressMonitor(owner, stringDatabase.getString("ProgressMonitor.Message.TemporalEvolution"), stringDatabase.getString("ProgressMonitor.Note.TemporalEvolution"), 0, 100);
        
        
    }
    
    
    /**
     * Constructor for displaying a temporal evolution dialog of one chance or utility node
     *
     * @param owner            dialog window owner
     * @param node             node whose temporal evolution is displayed
     * @param evidence         MID current evidence
     * @param decisionSelected conditioning decision for which temporal evolution by criterion is displayed
     */
    public TraceTemporalEvolutionDialog(Window owner, Node node, EvidenceCase evidence, Variable decisionSelected) throws NotAllNodesHavePoliciesException {
        this(owner, node.getProbNet(), decisionSelected);
        this.isUtility = node.getNodeType() == NodeType.UTILITY;
        progressMonitor.setMaximum(numSlices);
        Thread evaluationThread = new Thread(() -> {
            try {
                this.variableOfInterest = node.getVariable();
//			TemporalEvolution temporalEvolution = new VETemporalEvolution(probNet, node.getVariable());
                TemporalEvolution temporalEvolution = new MIDTemporalEvolution(originalProbNet, variableOfInterest);
                temporalEvolution.setPreResolutionEvidence(evidence);
                temporalEvolution.setDecisionVariable(decisionSelected);
                //no discounted or probabilistic elements
                this.temporalEvolutionResults = temporalEvolution.getTemporalEvolution();
                this.expandedNetwork = temporalEvolution.getExpandedNetwork();
                //discounted elements for utility nodes
                if (isUtility) {
                    this.temporalEvolutionDiscount = temporalEvolution.getTemporalEvolutionWithDiscount();
                }
                // end
                initialize(owner);
            } catch (IncompatibleEvidenceException | NonProjectablePotentialException | ConstraintViolatedException |
                     CannotNormalizePotentialException | NotEvaluableNetworkException.NotApplicableNetwork |
                     NotEvaluableNetworkException.VariableIsNotTemporal e) {
                throw new UnrecoverableException(e);
            } catch (IndexOutOfBoundsException ignore) {
                //When pressing "Cancel" in progressMonitor
            }
        });
        evaluationThread.start();
        progressMonitorThread(evaluationThread).start();
    }
    
    // 02/11/2022
    
    /**
     * Constructor for displaying a temporal evolution dialog by criterion
     *
     * @param owner            dialog window owner
     * @param probNet          MID whose temporal evolution by criterion is displayed
     * @param evidence         MID current evidence
     * @param decisionSelected conditioning decision for which temporal evolution by criterion is displayed
     */
    public TraceTemporalEvolutionDialog(Window owner, ProbNet probNet, EvidenceCase evidence, Variable decisionSelected) throws NotAllNodesHavePoliciesException {
        this(owner, probNet, decisionSelected);
        this.isByCriterion = true;
        this.isUtility = true;
        List<Criterion> criteria = probNet.getDecisionCriteria();
        progressMonitor.setMaximum(numSlices * criteria.size());
        this.markedCheckBoxes = new boolean[criteria.size()];
        Arrays.fill(markedCheckBoxes, true);
        Thread evaluationThread = new Thread(() -> {
            try {
                for (Criterion criterion : criteria) {
                    String criterionName = criterion.getCriterionName();
                    List<Node> criterionNodes = probNet.getNodes(NodeType.UTILITY)
                                                       .stream()
                                                       .filter(node -> node.getVariable()
                                                                           .getDecisionCriterion()
                                                                           .getCriterionName()
                                                                           .equals(criterionName))
                                                       .collect(Collectors.toList());
                    
                    MIDTemporalEvolution temporalEvolutionCriterion = new MIDTemporalEvolution(probNet, criterionNodes);
                    temporalEvolutionCriterion.setPreResolutionEvidence(evidence);
                    temporalEvolutionCriterion.setDecisionVariable(decisionSelected);
                    temporalEvolutionCriterion.forceUnicriterion();
                    this.temporalEvolutionResults = temporalEvolutionCriterion.getTemporalEvolution();
                    //Upfront values
                    this.upfrontEvolutionByCriterion.put(criterionName, temporalEvolutionCriterion.getAtemporalUtility());
                    //No discount
                    List<TablePotential> tablePotentialSequence = this.temporalEvolutionResults.entrySet()
                                                                                               .stream()
                                                                                               .sorted((Comparator.comparing(v -> v.getKey()
                                                                                                                                   .getTimeSlice())))
                                                                                               .map(Map.Entry::getValue)
                                                                                               .collect(Collectors.toList());
                    temporalEvolutionByCriterion.put(criterionName, tablePotentialSequence);
                    
                    //Discounted
                    this.temporalEvolutionDiscount = temporalEvolutionCriterion.getTemporalEvolutionWithDiscount(criterion);
                    tablePotentialSequence = this.temporalEvolutionDiscount.entrySet()
                                                                           .stream()
                                                                           .sorted((Comparator.comparing(v -> v.getKey()
                                                                                                               .getTimeSlice())))
                                                                           .map(Map.Entry::getValue)
                                                                           .collect(Collectors.toList());
                    temporalEvolutionByCriterionDiscount.put(criterionName, tablePotentialSequence);
                    
                }
                
                //For representation purposes
                variableOfInterest = new Variable(CRITERION, temporalEvolutionByCriterion.keySet()
                                                                                         .toArray(new String[0]));
                initialize(owner);
            } catch (IndexOutOfBoundsException ignore) {
                //When pressing "Cancel" in progressMonitor
            } catch (NotEvaluableNetworkException.NotApplicableNetwork | IncompatibleEvidenceException |
                     NotEvaluableNetworkException.VariableIsNotTemporal | NonProjectablePotentialException |
                     ConstraintViolatedException e) {
                throw new UnrecoverableException(e);
            }
        });
        evaluationThread.start();
        progressMonitorThread(evaluationThread).start();
        
    }
    
    private Thread progressMonitorThread(Thread evaluationThread) {
        return new Thread(() -> {
            int completed = 0;
            while (completed < progressMonitor.getMaximum()) {
                synchronized (MIDTemporalEvolution.class) {
                    try {
                        MIDTemporalEvolution.class.wait();
                    } catch (InterruptedException ignored) {
                    }
                    progressMonitor.setProgress(++completed);
                    if (progressMonitor.isCanceled()) {
                        evaluationThread.interrupt();
                        return;
                    }
                }
            }
        });
    }
    
    /**
     * Selects and prepares the series to be displayed
     *
     * @param isDiscounted if true, discounted series are shown
     * @param showUpfront  if true, upfront values are added to time 0
     *
     * @return List of @link(XYSeries) ready to be displayed
     */
    private List<XYSeries> displaySeries(boolean isDiscounted, boolean showUpfront) {
        List<XYSeries> display;
        if (isDiscounted) {
            display = new ArrayList<>(arrayXYSeriesDiscount);
        } else {
            display = new ArrayList<>(arrayXYSeries);
        }
        if (showUpfront) {
            for (int seriesNumber = 0; seriesNumber < display.size(); seriesNumber++) {
                //Cannot change values from arrayXYSeriesDiscount and
                XYSeries xySeriesWithUpfront;
                try {
                    xySeriesWithUpfront = (XYSeries) (display.remove(seriesNumber)).clone();
                } catch (CloneNotSupportedException e) {
                    throw new UnreachableException(e);
                }
                XYDataItem firstDataItem = xySeriesWithUpfront.remove(0);
                //adding atemporal data
                firstDataItem.setY(firstDataItem.getYValue() + arrayXYSeriesUpfront.get(seriesNumber)
                                                                                   .getY(0)
                                                                                   .doubleValue());
                xySeriesWithUpfront.add(firstDataItem);
                display.add(seriesNumber, xySeriesWithUpfront);
                
            }
            
        }
        return display;
    }
    
    /**
     * Creates the XYSeries and displays them in chartPanel ChartPanel according to markedCheckBoxes and
     * whether there is a cumulative or individual display.
     * For criteria temporal evolution it is necessary to show Display=(individual, cumulative), Checkboxes (criteria)
     * Based on this#showChartSeriesWithFilter.
     *
     * @param markedCheckBoxes array of boolean representing the MID criteria; when an item is true its temporal evolution is displayed.
     * @param isDiscounted     if true, discounted series are shown
     * @param showUpfront      if true, upfront values are added to time 0
     */
    private void showByCriterionSeries(boolean[] markedCheckBoxes, boolean isDiscounted, boolean showUpfront) throws UnexpectedInferenceException.ThereIsMoreThanOneConditioningVariable {
        
        List<XYSeries> result = new ArrayList<>();
        //At least there is one element marked
        boolean someCheckBoxMarked = IntStream.range(0, markedCheckBoxes.length).anyMatch(i -> markedCheckBoxes[i]);
        if (arrayXYSeries == null) {
            createByCriterionSeries();
        }
        
        
        if (someCheckBoxMarked) {
            if (isCumulative) {
                for (int i = 0; i < numberOfCombinations / variableOfInterest.getNumStates(); i++) {
                    for (int j = 0; j < markedCheckBoxes.length; j++) {
                        if (markedCheckBoxes[j]) {
                            XYSeries individualSeries = displaySeries(isDiscounted, showUpfront).get(i * variableOfInterest.getNumStates() + j);
                            XYSeries cumulatedSeries = new XYSeries(individualSeries.getKey());
                            double value = 0.0;
                            double slice;
                            for (int k = 0; k < individualSeries.getItemCount(); k++) {
                                value += (Double) individualSeries.getY(k);
                                slice = (Double) individualSeries.getX(k);
                                cumulatedSeries.add(slice, value);
                            }
                            result.add(cumulatedSeries);
                        }
                    }
                }
            } else {
                for (int i = 0; i < numberOfCombinations / variableOfInterest.getNumStates(); i++) {
                    for (int j = 0; j < markedCheckBoxes.length; j++) {
                        if (markedCheckBoxes[j]) {
                            result.add(displaySeries(isDiscounted, showUpfront).get(i * variableOfInterest.getNumStates() + j));
                            
                        }
                        
                    }
                }
            }
        }
        
        
        // Remove the old Chart, calculate the new Chart and Add the new Chart. Then repaint to update the GUI
        chartPanelWithCheckBox.remove(chartPanel);
        chartPanel = getChartsByCriterionPanel(result, markedCheckBoxes);
        chartPanelWithCheckBox.add(chartPanel, BorderLayout.CENTER);
        this.repaint();
        
    }
    
    
    /**
     * Create the series to be showed for temporal evolution by criterion and stores it in field arrayXYSeries
     * Adapted from createSeries(). Deeply changed.
     * This method only will be launched at the first time. In later
     * modifications and filters the established series are used to get other combined data
     */
    private void createByCriterionSeries() throws UnexpectedInferenceException.ThereIsMoreThanOneConditioningVariable {
        //Only one decision variable; conditioningVariables.size() =1
        arrayXYSeriesUpfront = new ArrayList<>();
        arrayXYSeries = new ArrayList<>();
        arrayXYSeriesDiscount = new ArrayList<>();
        
        List<String> decisionCriteria = new ArrayList<>(temporalEvolutionByCriterion.keySet());
        numberOfCombinations = decisionCriteria.size() * decisionSelected.getNumStates();
        
        for (State decisionState : decisionSelected.getStates()) {
            for (String criterionName : decisionCriteria) {
                //Adapted form nameOfSerie in createSeries() lines 1032-1051 for probabilistic variables
                String nameOfSeries = criterionName + " [" + decisionSelected.getName() + " = " + decisionState.getName() + "]";
                //probNet is sometimes copied; careful regarding comparing objects
                XYSeries xySeries = new XYSeries(nameOfSeries);
                XYSeries xySeriesDiscount = new XYSeries(nameOfSeries);
                XYSeries xySeriesUpfront = new XYSeries(nameOfSeries);
                TablePotential upfrontTablePotential = upfrontEvolutionByCriterion.get(criterionName);
                int numVariables = upfrontTablePotential.getNumVariables();
                switch (numVariables) {
                    case 0 -> {
                        xySeriesUpfront.add(0, upfrontTablePotential.getValues()[0]);
                    }
                    case 1 -> {
                        int decisionStateIndex = upfrontTablePotential.getVariables()
                                                                      .get(0)
                                                                      .getStateIndex(decisionState);
                        xySeriesUpfront.add(0, upfrontTablePotential.getValue(upfrontTablePotential.getVariables(), new int[]{decisionStateIndex}));
                    }
                    default ->
                            throw new UnexpectedInferenceException.ThereIsMoreThanOneConditioningVariable(upfrontTablePotential);
                }
                
                
                for (int slice = 0; slice <= numSlices; slice++) {
                    TablePotential tablePotential = temporalEvolutionByCriterion.get(criterionName).get(slice);
                    TablePotential tablePotentialDiscount = temporalEvolutionByCriterionDiscount.get(criterionName)
                                                                                                .get(slice);
                    double value;
                    double valueDiscount;
                    numVariables = tablePotential.getNumVariables();
                    switch (numVariables) {
                        case 0 -> {
                            value = tablePotential.getValues()[0];
                            valueDiscount = tablePotentialDiscount.getValues()[0];
                        }
                        case 1 -> {
                            int decisionStateIndex = tablePotential.getVariables().get(0).getStateIndex(decisionState);
                            value = tablePotential.getValue(tablePotential.getVariables(), new int[]{decisionStateIndex});
                            valueDiscount = tablePotentialDiscount.getValue(tablePotential.getVariables(), new int[]{decisionStateIndex});
                        }
                        default ->
                                throw new UnexpectedInferenceException.ThereIsMoreThanOneConditioningVariable(tablePotential);
                    }//if it is greater than two -->exception
                    xySeries.add(slice, value);
                    xySeriesDiscount.add(slice, valueDiscount);
                }
                arrayXYSeries.add(xySeries);
                arrayXYSeriesDiscount.add(xySeriesDiscount);
                arrayXYSeriesUpfront.add(xySeriesUpfront);
                
            }
            
        }
        
    }
    
    
    /**
     * Gets the Charts Panel with the JFreeChart for displaying temporal evolution by criterion where each criterion has its own axis
     *
     * @param xySeriesToDisplay List of XYSeries any series where each element contains one data line
     * @param markedCheckBoxes  array indicating which criteria are to be displayed
     *
     * @return a ChartPanel displaying temporal evolution by criterion according to xySeriesToDisplay
     */
    private ChartPanel getChartsByCriterionPanel(List<XYSeries> xySeriesToDisplay, boolean[] markedCheckBoxes) {
        //We can use one dataset per axis (as done by examples in "Developer guide" or add the axis using computing the range)
        //First line associated to chart primary index
        XYSeriesCollection dataset = new XYSeriesCollection();
        //GetNumCriteria returns 0
//		int numCriteria= originalProbNet.getNumCriteria();
//        int numCriteria = temporalEvolutionByCriterion.size();
        List<Criterion> criteria = originalProbNet.getDecisionCriteria();
        int numCriteria = criteria.size();
        //Displayed criterion according to markedCheckBoxes
        int numDisplayedCriteria = 0;
        List<String> yAxisLabels = new ArrayList<>();
        for (int i = 0; i < markedCheckBoxes.length; i++) {
            if (markedCheckBoxes[i]) {
                numDisplayedCriteria++;
                String criterionName = (String) temporalEvolutionByCriterion.keySet().toArray()[i];
                String criterionUnit = criteria.stream()
                                               .filter(criterion -> criterion.getCriterionName().equals(criterionName))
                                               .findFirst()
                                               .get()
                                               .getCriterionUnit();
                yAxisLabels.add(String.format("%s (%s)", criterionName, criterionUnit));
            }
        }
        
        //First dataset matched to main axis; it contains first criterion data;
        for (int i = 0; i < xySeriesToDisplay.size(); i = i + numDisplayedCriteria) {
            dataset.addSeries(xySeriesToDisplay.get(i));
        }
        chart = ChartFactory.createXYLineChart("", "t", yAxisLabels.get(0), dataset, PlotOrientation.VERTICAL, true, true, true);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        XYPlot plot = chart.getXYPlot();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", new DecimalFormat("0.00"),
                                                                      new DecimalFormat("0.00"));
        // TODO Manolo> When migrating to JFreeChart 1.5, I have replaced the call to the method setBaseToolTipGenerator by the new method setDefaultToolTipGenerator  
        //renderer.setBaseToolTipGenerator(generator);
        renderer.setDefaultToolTipGenerator(generator);
        plot.setRenderer(renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(GUIColors.TemporalEvoluation.DOMAIN.getColor());
        plot.setRangeGridlinePaint(GUIColors.TemporalEvoluation.DOMAIN.getColor());
//        ValueAxis axis1 =plot.getRangeAxis();
//        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        
        //One axis per criterion; next axis
        for (int numDisplayedCriterion = 1; numDisplayedCriterion < numDisplayedCriteria; numDisplayedCriterion++) {
            NumberAxis axis = new NumberAxis(yAxisLabels.get(numDisplayedCriterion));
            axis.setLabelFont(plot.getRangeAxis().getLabelFont());
            
            plot.setRangeAxis(numDisplayedCriterion, axis);
            dataset = new XYSeriesCollection();
            for (int i = numDisplayedCriterion; i < xySeriesToDisplay.size(); i = i + numDisplayedCriteria) {
                dataset.addSeries(xySeriesToDisplay.get(i));
            }
            plot.setDataset(numDisplayedCriterion, dataset);
            plot.mapDatasetToRangeAxis(numDisplayedCriterion, numDisplayedCriterion);
            //for left side axis
            renderer = new XYLineAndShapeRenderer();
            
            // TODO Manolo> When migrating to JFreeChart 1.5, I have replaced the call to the method setBaseToolTipGenerator by the new method setDefaultToolTipGenerator
            //renderer.setBaseToolTipGenerator(generator);
            renderer.setDefaultToolTipGenerator(generator);
            plot.setRenderer(numDisplayedCriterion, renderer);
            
        }


//        //Reorder labels; getLegendTitle() consider legend items are ordered by policy; it also consider there is only one rendere
//        //TODO consider recoding getLegendTitle
//        final Pattern pattern = Pattern.compile("(.+)(\\[.+])");
//        Iterable<LegendItem> iterable = () -> plot.getLegendItems().iterator();
//        List<LegendItem> sortedItems = StreamSupport.stream(
//                        iterable.spliterator(), false)
//                .sorted(Comparator.comparing(legendItem -> {
//                    Matcher matcher = pattern.matcher(legendItem.getLabel());
//                    matcher.matches();
//                    return matcher.group(2) + matcher.group(1);
//                }))
//                .collect(Collectors.toList());
//        LegendItemCollection sortedLegendCollection = new LegendItemCollection();
//        sortedItems.forEach(sortedLegendCollection::add);
//        plot.setFixedLegendItems(sortedLegendCollection);
        
        
        // Create the custom legend in an adjoined panel
        getCriteriaLegendTitle(numDisplayedCriteria);
        chart.removeLegend();
        chartPanel = new ChartPanel(chart);
        chartPanel.setAutoscrolls(true);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseZoomable(true);
        return chartPanel;
    }
    
    // end 02/11/2022
    
    
    private void createExcel(ProbNet probNet, EvidenceCase evidence, Variable decisionSelected) throws IOException, NotEvaluableNetworkException, NonProjectablePotentialException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        OMFileChooser omFileChooser = new OMFileChooser();
        String netName = probNet.getName();
        omFileChooser.setSelectedFile(new File(netName + "-temporal_evolution.xlsx"));
        if (omFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String targetFilename = omFileChooser.getSelectedFile().getAbsolutePath().endsWith(".xlsx") ?
                omFileChooser.getSelectedFile().getAbsolutePath() :
                omFileChooser.getSelectedFile().getAbsolutePath() + ".xlsx";
        List<Variable> temporalVariables = new ArrayList<>();
        for (Variable variable : probNet.getVariables()) {
            if (variable.isTemporal()) {
                if (variable.getVariableType() == VariableType.NUMERIC
                        && probNet.getNode(variable).getNodeType() != NodeType.UTILITY) {
                    continue;
                }
                boolean addedOtherSlice = temporalVariables
                        .stream()
                        .anyMatch(temporalVariable -> temporalVariable.getBaseName().equals(variable.getBaseName()));
                if (!addedOtherSlice) {
                    temporalVariables.add(variable);
                }
            }
        }
        
        HashMap<Variable, JTable> datasheet = new HashMap<>();
        for (Variable temporalVariable : temporalVariables) {
            System.out.println(temporalVariable.getBaseName());
            TemporalEvolution veTemporalEvolution = new VETemporalEvolution(probNet, temporalVariable);
            veTemporalEvolution.setPreResolutionEvidence(evidence);
            veTemporalEvolution.setDecisionVariable(decisionSelected);
            HashMap<Variable, TablePotential> result = veTemporalEvolution.getTemporalEvolution();
            JTable table = createJTable(temporalVariable, result);
            TemporalEvolutionReport.write(
                    targetFilename.substring(0, targetFilename.length() - 5) + temporalVariable.getBaseName()
                            + ".xlsx", table);
            datasheet.put(temporalVariable, table);
        }
        
        XSSFWorkbook hwb = new XSSFWorkbook();
        for (Variable tabVariable : datasheet.keySet()) {
            JTable jtable = datasheet.get(tabVariable);
            XSSFSheet sheetTable = hwb.createSheet(tabVariable.getBaseName());
            // first row, column names
            Row rowIndexes = sheetTable.createRow(0);
            rowIndexes.createCell(0).setCellValue("");
            
            for (int i = 1; i < jtable.getColumnCount(); i++) {
                rowIndexes.createCell(i + 1)
                          .setCellValue(jtable.getColumnModel().getColumn(i).getHeaderValue().toString());
            }
            // fill data
            for (int i = 0; i < jtable.getRowCount(); i++) {
                Row row = sheetTable.createRow(i + 1);
                for (int j = 0; j < jtable.getColumnCount(); j++) {
                    if (jtable.getValueAt(i, j) instanceof String) {
                        row.createCell(j).setCellValue((String) jtable.getValueAt(i, j));
                    } else if (jtable.getValueAt(i, j) instanceof Integer) {
                        row.createCell(j).setCellValue((Integer) jtable.getValueAt(i, j));
                    } else {
                        row.createCell(j).setCellValue((Double) jtable.getValueAt(i, j));
                    }
                }
                
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(targetFilename)) {
            hwb.write(fileOut);
        }
    }
    
    private JTable createJTable(Variable temporalVariable, HashMap<Variable, TablePotential> result) {
        int numRows = this.numSlices + conditioningVariables.size() + 2;
        int numColumns = temporalVariable.getNumStates();
        for (Variable conditioningVariable : conditioningVariables) {
            numColumns *= conditioningVariable.getNumStates();
        }
        numColumns += 1;
        
        JTable jtable = new JTable(numRows, numColumns);
        
        int row;
        // Build conditioning variables names
        for (row = 0; row < conditioningVariables.size(); row++) {
            jtable.setValueAt(conditioningVariables.get(row).getBaseName(), row, 0);
        }
        
        //Build states
        jtable.setValueAt("States", row, 0);
        row++;
        
        // Build slices column
        for (int slice = 0; row < numRows; row++) {
            jtable.setValueAt(slice, row, 0);
            slice++;
        }
        
        // Fill conditioning variables states
        row = 0;
        for (Variable conditioningVariable : conditioningVariables) {
            // Build headers
            for (int column = 1; column < numColumns; column++) {
                String stateName = conditioningVariable.getStateName((column - 1) / temporalVariable.getNumStates());
                jtable.setValueAt(stateName, row, column);
            }
            row++;
        }
        
        // Fill states of the temporal variable
        for (int column = 1; column < numColumns; column++) {
            String stateName = temporalVariable.getStateName((column - 1) % temporalVariable.getNumStates());
            jtable.setValueAt(stateName, row, column);
        }
        
        // Fill table potentials
        int slice0row = conditioningVariables.size() + 1;
        
        for (Variable variable : result.keySet()) {
            TablePotential tablePotential = result.get(variable);
            int rowVariable = variable.getTimeSlice() + slice0row;
            for (int column = 1; column < numColumns; column++) {
                jtable.setValueAt(tablePotential.getValues()[(column - 1) % temporalVariable.getNumStates()],
                                  rowVariable, column);
            }
        }
        
        return jtable;
    }
    
    // 31/10/2022 -refactored to avoid code repetition
    
    /**
     * Initializes TraceTemporalEvolutionDialog
     *
     * @param owner dialog window owner
     */
    private void initialize(Window owner) {
        // 24/10/2022
        if (isByCriterion) {
            setTitle(stringDatabase.getString("TemporalEvolutionResultDialog.ByCriterionTitle"));
        } else {
            setTitle(stringDatabase.getString("TemporalEvolutionResultDialog.Title") + " " + variableOfInterest
                    .getBaseName());
        }
        setContentPane(getJContentPane());
        pack();
        
        // 31/10/2022 -moved from constructor to avoid code repetition
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle bounds = owner.getBounds();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;
        // center point of the owner window
        int x = bounds.x / 2 - width / 2;
        int y = bounds.y / 2 - height / 2;
        this.setBounds(x, y, width, height);
        setMinimumSize(new Dimension(width, height / 2));
        setLocationRelativeTo(owner);
        setResizable(true);
        repaint();
        pack();
        setVisible(true);
        // end
    }
    
    /**
     * This method initialises jContentPane.
     *
     * @return a new content panel.
     */
    private JPanel getJContentPane() {
        JPanel jContentPane = new JPanel();
        jContentPane.setLayout(new BorderLayout());
        jContentPane.add(getComponentsPanel(), BorderLayout.CENTER);
        jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
        return jContentPane;
    }
    
    /**
     * Get the bottom panel with buttons
     *
     * @return The bottom panel
     */
    private JPanel getBottomPanel() {
        JPanel buttonsPanel = new JPanel();
        JButton jButtonSaveReport = new JButton();
        jButtonSaveReport.setName("jButtonSaveReport");
        jButtonSaveReport.setText(stringDatabase.getString("Dialog.SaveReport"));
        jButtonSaveReport.addActionListener(e -> {
            try {
                saveReport();
            } catch (IOException ex) {
                throw new UnrecoverableException(ex);
            }
        });
        buttonsPanel.add(jButtonSaveReport);
        JButton jButtonClose = new JButton();
        jButtonClose.setName("jButtonClose");
        jButtonClose.setText(stringDatabase.getString("Dialog.Close"));
        jButtonClose.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        buttonsPanel.add(jButtonClose);
        return buttonsPanel;
    }
    
    /**
     * Gets the components panel
     *
     * @return The components panel
     */
    private Component getComponentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setMaximumSize(new Dimension(180, 40));
        panel.add(getTabbedPane());
        pack();
        return panel;
    }
    
    /**
     * This method initialises tabbedPane.
     *
     * @return a new tabbed pane.
     */
    protected JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            tabbedPane.setName("TraceTemporalEvolutionTabbedPane");
            tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionChart.Title"), null,
                              getChartsPanelWithCheckBoxes(), null);
            tabbedPane
                    .addTab(stringDatabase.getString("TemporalEvolutionTable.Title"), null, getTablePane(), null);
            
        }
        return tabbedPane;
    }
    
    /**
     * Gets the ChartPanel with the control area
     *
     * @return JPanel with the ChartPanel and the Control Area
     */
    private JPanel getChartsPanelWithCheckBoxes() {
        if (chartPanelWithCheckBox == null) {
            chartPanelWithCheckBox = new JPanel();
            chartPanelWithCheckBox.setLayout(new BorderLayout());
            // 28/10/2022 - new method for byCriterion in order to address several indexes
            if (isByCriterion) {
                try {
                    createByCriterionSeries();
                } catch (UnexpectedInferenceException.ThereIsMoreThanOneConditioningVariable e) {
                    throw new UnreachableException(e);
                }
                chartPanelWithCheckBox.add(getChartsByCriterionPanel(displaySeries(true, true), markedCheckBoxes), BorderLayout.CENTER);
            } else
                // end
                chartPanelWithCheckBox.add(getChartsPanel(createDataset()), BorderLayout.CENTER);
            chartPanelWithCheckBox.add(getChartOptionsPanel(), BorderLayout.LINE_START);
            getLegendsPanel();
        }
        return chartPanelWithCheckBox;
    }
    
    /**
     * Gets the Legends Panel and set it to the chartPanelWithCheckBoxes
     */
    private void getLegendsPanel() {
        if (legendPanel != null) {
            chartPanelWithCheckBox.remove(legendPanel);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        
        TitledBorder border = new TitledBorder(
                stringDatabase.getString("TemporalEvolutionResultDialog.Legend.Title"));
        
        panel.setBorder(border);
        panel.setBackground(GUIColors.TemporalEvoluation.BACKGROUND.getColor());
        
        for (JLabel legend : legendLabels) {
            panel.add(legend);
        }
        
        legendPanel = new JScrollPane(panel);
        legendPanel.setPreferredSize(legendsDimension);
        legendPanel.setMinimumSize(legendsDimension);
        
        legendPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        legendPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        if ((conditioningVariables != null && !conditioningVariables.isEmpty()) || (!isUtility && isIndividual)) {
            chartPanelWithCheckBox.add(legendPanel, BorderLayout.LINE_END);
        }
        
    }
    
    /**
     * Gets the Charts Panel with the JFreeChart
     *
     * @param dataset Dataset for the JFreeChart
     *
     * @return ChartsPanel
     */
    private ChartPanel getChartsPanel(XYDataset dataset) {
        chart = ChartFactory.createXYLineChart("", "t", "value", dataset, PlotOrientation.VERTICAL, true, true, true);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }
        
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().setDomainGridlinesVisible(true);
        chart.getXYPlot().setRangeGridlinesVisible(true);
        chart.getXYPlot().setDomainGridlinePaint(GUIColors.TemporalEvoluation.DOMAIN.getColor());
        chart.getXYPlot().setRangeGridlinePaint(GUIColors.TemporalEvoluation.DOMAIN.getColor());
        
        // Create the custom legend in an adjoined panel
        getLegendTitle();
        
        // Hide the default legend
        for (int i = 0; i < chart.getSubtitleCount(); i++) {
            chart.getSubtitle(i).setVisible(false);
        }
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setAutoscrolls(true);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseZoomable(true);
        
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", new DecimalFormat("0.00"),
                                                                      new DecimalFormat("0.00"));
        // TODO Manolo> When migrating to JFreeChart 1.5, I have replaced the call to the method setBaseToolTipGenerator by the new method setDefaultToolTipGenerator
        //renderer.setBaseToolTipGenerator(generator);
        renderer.setDefaultToolTipGenerator(generator);
        
        return chartPanel;
    }
    
    private JPanel getChartOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(getDisplayTypePanel(), BorderLayout.PAGE_START);
        panel.add(getCheckBoxesPanel(), BorderLayout.CENTER);
        if ((!isByCriterion) && (variableOfInterest.getNumStates() <= 1 || isUtility)) {
            getCheckBoxesPanel().setVisible(false);
        }
        panel.setPreferredSize(new Dimension(150, 450));
        return panel;
    }
    
    /**
     * Control panel with checkBox controls
     *
     * @return The check boxes panel
     */
    private JScrollPane getCheckBoxesPanel() {
        if (checkBoxPanel == null) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            
            jcheckBoxList = new ArrayList<JCheckBox>();
            
            // Add a checkbox for each state in the variable
            for (State state : variableOfInterest.getStates()) {
                JCheckBox checkBox = new JCheckBox(state.getName());
                checkBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkBoxChanged();
                    }
                });
                checkBox.setSelected(true);
                jcheckBoxList.add(checkBox);
                panel.add(checkBox);
            }
            if (isByCriterion) {
                panel.setBorder(new TitledBorder(stringDatabase.getString("TemporalEvolutionResultDialog.Criteria")));
            } else {
                panel.setBorder(new TitledBorder(stringDatabase.getString("TemporalEvolutionResultDialog.States")));
            }
            
            checkBoxPanel = new JScrollPane(panel);
            checkBoxPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            checkBoxPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            checkBoxPanel.setPreferredSize(new Dimension(150, 250));
            checkBoxPanel.setBorder(null);
        }
        return checkBoxPanel;
    }
    
    /**
     * Panel with the radio button control that changes the display type
     *
     * @return The display type panel
     */
    private JPanel getDisplayTypePanel() {
        JPanel displayType = new JPanel();
        displayType
                .setBorder(new TitledBorder(stringDatabase.getString("TemporalEvolutionResultDialog.Display")));
        displayType.setPreferredSize(new Dimension(150, 150));
        displayType.setLayout(new BoxLayout(displayType, BoxLayout.PAGE_AXIS));
        
        // Create the custom radio button group. If we have an utility node
        // we can choose between "instaneus/accumulate" in other case
        // we can choose between "individual/summatory".
        ButtonGroup radioButtonGroup = new ButtonGroup();
        if (isUtility) {
            radioInstantaneus = new JRadioButton(
                    stringDatabase.getString("TemporalEvolutionResultDialog.Display.Instantaneus"));
            radioInstantaneus.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioButtonChanged(e);
                }
            });
            
            radioAccumulate = new JRadioButton(
                    stringDatabase.getString("TemporalEvolutionResultDialog.Display.Accumulate"));
            radioAccumulate.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioButtonChanged(e);
                }
            });
            
            radioButtonGroup.add(radioInstantaneus);
            radioButtonGroup.add(radioAccumulate);
            radioInstantaneus.setSelected(true);
            isCumulative = false;
            
            displayType.add(radioInstantaneus);
            displayType.add(radioAccumulate);
        } else {
            
            radioIndividual = new JRadioButton(
                    stringDatabase.getString("TemporalEvolutionResultDialog.Display.Individual"));
            radioIndividual.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioButtonChanged(e);
                }
            });
            
            radioSummatory = new JRadioButton(
                    stringDatabase.getString("TemporalEvolutionResultDialog.Display.Summatory"));
            radioSummatory.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioButtonChanged(e);
                }
            });
            
            radioButtonGroup.add(radioIndividual);
            radioButtonGroup.add(radioSummatory);
            radioIndividual.setSelected(true);
            isIndividual = true;
            
            displayType.add(radioIndividual);
            displayType.add(radioSummatory);
        }
        if (isByCriterion) {
            //TODO add StringDatabase
            jCheckBoxUpfrontValues = new JCheckBox(stringDatabase.getString("TemporalEvolutionResultDialog.Display.Upfront"));//new JCheckBox("Upfront values");
            jCheckBoxUpfrontValues.addActionListener(e -> checkBoxChanged());
            jCheckBoxUpfrontValues.setSelected(true);
            displayType.add(jCheckBoxUpfrontValues);
        }
        if (isUtility) {
            jCheckBoxDiscounted = new JCheckBox(stringDatabase.getString("TemporalEvolutionResultDialog.Display.Discount"));
            jCheckBoxDiscounted.addActionListener(e -> checkBoxChanged());
            jCheckBoxDiscounted.setSelected(true);
            displayType.add(jCheckBoxDiscounted);
        }
        
        return displayType;
    }
    
    
    /**
     * Action that trigger a change in a radio button.
     *
     * @param e the e
     */
    private void radioButtonChanged(ActionEvent e) {
        if (e.getSource().equals(radioIndividual)) {
            if (!isIndividual) {
                isIndividual = true;
                // Uses the same method as check box action performed.
                checkBoxChanged();
            }
        } else if (e.getSource().equals(radioSummatory)) {
            if (isIndividual) {
                isIndividual = false;
                checkBoxChanged();
            }
        } else if (e.getSource().equals(radioInstantaneus)) {
            if (isCumulative) {
                isCumulative = false;
                tabbedPane.removeTabAt(1);
                tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionTable.Title"), null, getTablePane(),
                                  null);
                checkBoxChanged();
            }
        } else if (e.getSource().equals(radioAccumulate)) {
            if (!isCumulative) {
                isCumulative = true;
                tabbedPane.removeTabAt(1);
                tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionTable.Title"), null, getTablePane(),
                                  null);
                checkBoxChanged();
            }
        }
    }
    
    /**
     * Method triggered by a change in a checkbox.
     */
    protected void checkBoxChanged() {
        // 09/11/2022 - made field to be available for the  table
//        boolean[] markedCheckBoxes = new boolean[jcheckBoxList.size()];
        markedCheckBoxes = new boolean[jcheckBoxList.size()];
        // end
        for (int i = 0; i < jcheckBoxList.size(); i++) {
            markedCheckBoxes[i] = jcheckBoxList.get(i).isSelected();
        }
        // Update the visual info and repaint
        // 24/10/2022
        if (isByCriterion) {
            try {
                showByCriterionSeries(markedCheckBoxes, jCheckBoxDiscounted.isSelected(), jCheckBoxUpfrontValues.isSelected());
            } catch (UnexpectedInferenceException.ThereIsMoreThanOneConditioningVariable e) {
                throw new UnrecoverableException(e);
            }
            tabbedPane.removeTabAt(1);
            tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionTable.Title"), null, getTablePane(),
                              null);
        } else
            // end
            if (isUtility) {
                showUtilitySeries(jCheckBoxDiscounted.isSelected());
                //
                tabbedPane.removeTabAt(1);
                tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionTable.Title"), null, getTablePane(),
                                  null);
                //
            } else {
                showChartSeriesWithFilter(markedCheckBoxes);
            }
    }
    
    
    /**
     * Allows to update the info in the chart. With the filter, the painted
     * series will change
     *
     * @param markedCheckBoxes the marked check boxes
     */
    private void showChartSeriesWithFilter(boolean[] markedCheckBoxes) {
        XYSeriesCollection result = new XYSeriesCollection();
        
        boolean someCheckBoxMarked = false;
        for (int j = 0; j < markedCheckBoxes.length; j++) {
            if (markedCheckBoxes[j]) {
                someCheckBoxMarked = true;
                break;
            }
        }
        
        if (arrayXYSeries == null) {
            createSeries();
        }
        
        if (isIndividual) {
            if (someCheckBoxMarked) {
                for (int i = 0; i < numberOfCombinations / variableOfInterest.getNumStates(); i++) {
                    for (int j = 0; j < markedCheckBoxes.length; j++) {
                        if (markedCheckBoxes[j]) {
                            result.addSeries(arrayXYSeries.get(i * variableOfInterest.getNumStates() + j));
                        }
                    }
                }
            }
        } else {
            if (someCheckBoxMarked) {
                for (int i = 0; i < numberOfCombinations / variableOfInterest.getNumStates(); i++) {
                    ArrayList<XYSeries> seriesToBeAdded = new ArrayList<XYSeries>();
                    for (int j = 0; j < markedCheckBoxes.length; j++) {
                        if (markedCheckBoxes[j]) {
                            seriesToBeAdded.add(arrayXYSeries.get(i * variableOfInterest.getNumStates() + j));
                        }
                    }
                    result.addSeries(sumSeries(seriesToBeAdded));
                }
            }
        }
        
        // Remove the old Chart, calculate the new Chart and Add the new Chart. Then repaint to update the GUI
        chartPanelWithCheckBox.remove(chartPanel);
        chartPanel = getChartsPanel(result);
        chartPanelWithCheckBox.add(chartPanel, BorderLayout.CENTER);
        this.repaint();
        
    }
    
    
    // 07/11/2022 showUtilitySeries() refactored to include discount/no discount option
    
    /**
     * Method that updates utility series. Really similar to "showChartSeriesWithFilter", but in this
     * case, the checkboxes are irrelevant and we must know if the display is cumulative or individual
     *
     * @param isDiscounted true if discounted results are displayed
     */
    private void showUtilitySeries(boolean isDiscounted) {
        XYSeriesCollection result = new XYSeriesCollection();
        
        if (arrayXYSeries == null) {
            createSeries();
        }
        List<XYSeries> displaySeries;
        if (isDiscounted) {
            displaySeries = arrayXYSeriesDiscount;
        } else {
            displaySeries = arrayXYSeries;
        }
        
        if (isCumulative) {
            for (int i = 0; i < displaySeries.size(); i++) {
                String nameOfSerie = (String) displaySeries.get(i).getKey();
                // end
                XYSeries serie = new XYSeries(nameOfSerie);
                double value = 0.0;
                double slice;
                for (int j = 0; j < displaySeries.get(i).getItemCount(); j++) {
                    value += (Double) displaySeries.get(i).getY(j);
                    slice = (Double) displaySeries.get(i).getX(j);
                    
                    serie.add(slice, value);
                }
                result.addSeries(serie);
            }
        } else {
            for (int i = 0; i < displaySeries.size(); i++) {
                String nameOfSerie = (String) displaySeries.get(i).getKey();
                // end
                XYSeries serie = null;
                try {
                    serie = (XYSeries) displaySeries.get(i).clone();
                } catch (CloneNotSupportedException e) {
                    throw new UnreachableException(e);
                }
                serie.setKey(nameOfSerie);
                result.addSeries(serie);
            }
        }
        
        chartPanelWithCheckBox.remove(chartPanel);
        chartPanel = getChartsPanel(result);
        chartPanelWithCheckBox.add(chartPanel, BorderLayout.CENTER);
        this.repaint();
    }
    
    
    /**
     * Sum a list of series to get a new series
     *
     * @param arraySeries the array series
     *
     * @return The XY series
     */
    private XYSeries sumSeries(ArrayList<XYSeries> arraySeries) {
        String seriesName = (String) arraySeries.get(0).getKey();
        if (!conditioningVariables.isEmpty()) {
            seriesName = seriesName.substring(seriesName.indexOf('[') + 1, seriesName.indexOf(']'));
        }
        
        XYSeries series = new XYSeries(seriesName);
        for (int i = 0; i < arraySeries.get(0).getItemCount(); i++) {
            double yCoordinate = 0;
            for (int j = 0; j < arraySeries.size(); j++) {
                yCoordinate += arraySeries.get(j).getY(i).doubleValue();
            }
            series.add(i, yCoordinate);
        }
        return series;
    }
    
    /**
     * Create the series to be showed. This method only will be launched at the first time. In later
     * modifications and filters the established series are used to get other combined data
     */
    private void createSeries() {
        numberOfCombinations = variableOfInterest.getNumStates();
        
        if (conditioningVariables != null && !conditioningVariables.isEmpty()) {
            for (int i = 0; i < conditioningVariables.size(); i++) {
                numberOfCombinations *= conditioningVariables.get(i).getNumStates();
            }
        }
        
        List<TablePotential> listOfPotentials = new ArrayList<>();
        List<TablePotential> listOfPotentialsDiscount = new ArrayList<>();
        for (int slice = 0; slice <= numSlices; slice++) {
            String basename = variableOfInterest.getBaseName();
            
            Variable variableInSliceJ = expandedNetwork.getVariable(basename, slice);
            
            
            TablePotential tablePotential = temporalEvolutionResults.get(variableInSliceJ);
            TablePotential tablePotentialDiscount = null;
            if (isUtility) {
                tablePotentialDiscount = temporalEvolutionDiscount.get(variableInSliceJ);
            }
            
            if (tablePotential.getValues().length < numberOfCombinations) {
                double[] values = new double[numberOfCombinations];
                double[] valuesDiscount = new double[numberOfCombinations];
                for (int z = 0; z < numberOfCombinations; z++) {
                    values[z] = tablePotential.getValues()[z % temporalEvolutionResults.get(variableInSliceJ)
                                                                                       .getValues().length];///(numberOfCombinations/variableOfInterest.getNumStates());
                    if (isUtility) {
                        valuesDiscount[z] = tablePotentialDiscount.getValues()[z % temporalEvolutionDiscount.get(variableInSliceJ)
                                                                                                            .getValues().length];///(numberOfCombinations/variableOfInterest.getNumStates());
                    }
                    
                }
                
                tablePotential.setValues(values);
                if (isUtility) {
                    tablePotentialDiscount.setValues(valuesDiscount);
                }
            }
            listOfPotentials.add(tablePotential);
            if (isUtility) {
                listOfPotentialsDiscount.add(tablePotentialDiscount);
            }
        }
        
        arrayXYSeries = new ArrayList<XYSeries>();
        arrayXYSeriesDiscount = new ArrayList<>();
        
        double value;
        for (int i = 0; i < numberOfCombinations; i++) {
            XYSeries series;
            XYSeries seriesDiscount = null;
            if (isUtility) {
                String nameOfSerie = "";
                if (conditioningVariables != null && !conditioningVariables.isEmpty()) {
                    int positionSelector = variableOfInterest.getNumStates();
                    for (int j = conditioningVariables.size() - 1; j >= 0; j--) {
                        String nameOfConditionalVariable = conditioningVariables.get(j).getName();
                        String stateOfConditionalVariable = conditioningVariables.get(j)
                                                                                 .getStateName((i / positionSelector) % conditioningVariables.get(j)
                                                                                                                                             .getNumStates());
                        positionSelector *= conditioningVariables.get(j).getNumStates();
                        nameOfSerie += nameOfConditionalVariable + " = " + stateOfConditionalVariable + " ; ";
                    }
                    if (!conditioningVariables.isEmpty()) {
                        nameOfSerie = nameOfSerie.substring(0, nameOfSerie.length() - 3);
                    }
                } else {
                    nameOfSerie = variableOfInterest.getStateName(i);
                }
                
                series = new XYSeries(nameOfSerie);
                seriesDiscount = new XYSeries(nameOfSerie);
            } else {
                String nameOfSerie;
                if (conditioningVariables != null && !conditioningVariables.isEmpty()) {
                    int stateIndex = i % variableOfInterest.getNumStates();
                    nameOfSerie = variableOfInterest.getStateName(stateIndex) + " [";
                    
                    int positionSelector = variableOfInterest.getNumStates();
                    for (int j = conditioningVariables.size() - 1; j >= 0; j--) {
                        String nameOfConditionalVariable = conditioningVariables.get(j).getName();
                        String stateOfConditionalVariable = conditioningVariables.get(j)
                                                                                 .getStateName((i / positionSelector) % conditioningVariables.get(j)
                                                                                                                                             .getNumStates());
                        positionSelector *= conditioningVariables.get(j).getNumStates();
                        nameOfSerie += nameOfConditionalVariable + " = " + stateOfConditionalVariable + " ; ";
                    }
                    if (!conditioningVariables.isEmpty()) {
                        nameOfSerie = nameOfSerie.substring(0, nameOfSerie.length() - 3) + "]";
                    } else {
                        nameOfSerie = nameOfSerie.substring(0, nameOfSerie.length() - 2);
                    }
                } else {
                    nameOfSerie = variableOfInterest.getStateName(i);
                }
                
                series = new XYSeries(nameOfSerie);
            }
            
            for (int j = 0; j <= numSlices; j++) {
                
                if (listOfPotentials.get(j) != null) {
                    value = listOfPotentials.get(j).getValues()[i];
                    int time = j;
                    series.add(time, value);
                    if (isUtility) {
                        value = listOfPotentialsDiscount.get(j).getValues()[i];
                        seriesDiscount.add(time, value);
                    }
                }
                
            }
            arrayXYSeries.add(series);
            if (isUtility) {
                arrayXYSeriesDiscount.add(seriesDiscount);
            }
            
        }
    }
    
    // 24/11/2022 temporal evolution by criterion extracted to getCriteriaLegendTitle method
    
    /**
     * Sorts and formats the temporal evolution by criterion chart legends (stored in {@code legendLabels}) and updates the legend panel.
     *
     * @param numDisplayedCriteria number of criteria to display (selected in the checkboxes)
     */
    private void getCriteriaLegendTitle(int numDisplayedCriteria) {
        LegendItemCollection legendItems = chart.getPlot().getLegendItems();
        
        JLabel[] legendLabelsArray = new JLabel[numDisplayedCriteria * decisionSelected.getNumStates()];
        Iterator<?> iterator = legendItems.iterator();
        int decisionIndex = 0;
        int criterionIndex = 0;
        //Reordering and formatting labels
        while (iterator.hasNext()) {
            LegendItem item = (LegendItem) iterator.next();
            String legendTitle = item.getLabel().substring(0, item.getLabel().indexOf('[') - 1);
            JLabel itemLegendLabel = makeLegendLabel(legendTitle,
                                                     makeImage(item, (Color) chart.getXYPlot()
                                                                                  .getRenderer(item.getDatasetIndex())
                                                                                  .getSeriesPaint(item.getSeriesIndex())));
            // get metrics from the graphics
            FontMetrics metrics = itemLegendLabel.getFontMetrics(itemLegendLabel.getFont());
            // get the height of a line of foreground in this font and render context
            int hgt = metrics.getHeight();
            // get the advance of my foreground in this font and render context
            int adv = metrics.stringWidth(itemLegendLabel.getText());
            // calculate the size of a box to hold the foreground with some padding.
            Dimension size = new Dimension(adv + 30, hgt + 10);
            itemLegendLabel.setMinimumSize(size);
            itemLegendLabel.setMaximumSize(size);
            itemLegendLabel.setPreferredSize(size);
            itemLegendLabel.setToolTipText(itemLegendLabel.getText());
            legendLabelsArray[criterionIndex + decisionIndex] = itemLegendLabel;
            decisionIndex += numDisplayedCriteria;
            if (decisionIndex >= (numDisplayedCriteria * decisionSelected.getNumStates())) {
                decisionIndex = 0;
                criterionIndex++;
            }
        }
        legendLabels = new ArrayList<>(Arrays.asList(legendLabelsArray));
        //Add decision titles
        for (int i = 0; i < decisionSelected.getNumStates(); i++) {
            JLabel groupLabel = new JLabel(decisionSelected.getName() + " = " + decisionSelected.getStateName(i));
            groupLabel.setFont(new Font(groupLabel.getFont().getName(), Font.BOLD, groupLabel.getFont().getSize()));
            // get metrics from the graphics
            FontMetrics metrics = groupLabel.getFontMetrics(groupLabel.getFont());
            // get the height of a line of foreground in this font and render context
            int hgt = metrics.getHeight();
            // get the advance of my foreground in this font and render context
            int adv = metrics.stringWidth(groupLabel.getText());
            // calculate the size of a box to hold the foreground with some padding.
            Dimension size = new Dimension(adv + 5, hgt + 10);
            groupLabel.setMinimumSize(size);
            groupLabel.setMaximumSize(size);
            groupLabel.setPreferredSize(size);
            groupLabel.setToolTipText(groupLabel.getText());
            legendLabels.add(i * (numDisplayedCriteria + 1), groupLabel);
        }
        
        getLegendsPanel();
    }
    //end
    
    /**
     * Gets the LegendTitles and updates the LegendPanel at the end
     */
    private void getLegendTitle() {
        legendLabels = new ArrayList<>();
        // They are sorted by policy
        LegendItemCollection legendItemsOld = chart.getPlot().getLegendItems();
        int listPointer = 0;
        while (listPointer < legendItemsOld.getItemCount()) {
            
            String subListTitle = legendItemsOld.get(listPointer).getLabel();
            if (subListTitle.indexOf('[') != -1) {
                subListTitle = subListTitle.substring(subListTitle.indexOf('[') + 1, subListTitle.indexOf(']'));
            }
            
            boolean isSamePolicy = true;
            //24/11/2022 temporal evolution by criterion extracted to getCriteriaLegendTitle method
            if (isIndividual && !isUtility && conditioningVariables != null && !conditioningVariables.isEmpty()) {
                // end
                JLabel groupLabel = new JLabel(subListTitle);
                Font font = groupLabel.getFont();
                groupLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                
                // get metrics from the graphics
                FontMetrics metrics = groupLabel.getFontMetrics(groupLabel.getFont());
                // get the height of a line of foreground in this font and render context
                int hgt = metrics.getHeight();
                // get the advance of my foreground in this font and render context
                int adv = metrics.stringWidth(subListTitle);
                // calculate the size of a box to hold the foreground with some padding.
                Dimension size = new Dimension(adv + 5, hgt + 10);
                
                groupLabel.setMinimumSize(size);
                groupLabel.setMaximumSize(size);
                groupLabel.setPreferredSize(size);
                
                groupLabel.setToolTipText(subListTitle);
                
                legendLabels.add(groupLabel);
            }
            
            while (isSamePolicy && listPointer < legendItemsOld.getItemCount()) {
                String subListTitle2 = legendItemsOld.get(listPointer).getLabel();
                if (subListTitle2.indexOf('[') != -1) {
                    subListTitle2 = subListTitle2.substring(subListTitle2.indexOf('[') + 1, subListTitle2.indexOf(']'));
                }
                
                if (!subListTitle.equals(subListTitle2)) {
                    isSamePolicy = false;
                } else {
                    LegendItem item = legendItemsOld.get(listPointer);
                    String nameItem2 = item.getLabel();
                    if (nameItem2.indexOf('[') != -1) {
                        nameItem2 = nameItem2.substring(0, nameItem2.indexOf('[') - 1);
                    }
                    Image img = makeImage(item, (Color) chart.getXYPlot().getRenderer().getSeriesPaint(listPointer));
                    JLabel itemLegendLabel = makeLegendLabel(nameItem2, img);
                    // get metrics from the graphics
                    FontMetrics metrics = itemLegendLabel.getFontMetrics(itemLegendLabel.getFont());
                    // get the height of a line of foreground in this font and render context
                    int hgt = metrics.getHeight();
                    // get the advance of my foreground in this font and render context
                    int adv = metrics.stringWidth(nameItem2);
                    // calculate the size of a box to hold the foreground with some padding.
                    
                    Dimension size = new Dimension(adv + 30, hgt + 10);
                    itemLegendLabel.setMinimumSize(size);
                    itemLegendLabel.setMaximumSize(size);
                    itemLegendLabel.setPreferredSize(size);
                    itemLegendLabel.setToolTipText(nameItem2);
                    
                    legendLabels.add(itemLegendLabel);
                    listPointer++;
                    
                }
            }
        }
        getLegendsPanel();
    }
    
    /**
     * Create the data set using the result given by createSeries method*
     *
     * @return The data set created
     */
    private XYDataset createDataset() {
        XYSeriesCollection result = new XYSeriesCollection();
        createSeries();
        if (isUtility) {
            arrayXYSeriesDiscount.forEach(result::addSeries);
        } else {
            arrayXYSeries.forEach(result::addSeries);
        }
        return result;
    }
    
    /**
     * Get the TablePane constructed by the data in temporalEvolution
     *
     * @return The TablePane
     */
    private JScrollPane getTablePane() {
        if (isByCriterion) {
            tablePane = new TemporalEvolutionTablePane(decisionSelected, markedCheckBoxes, isCumulative, new ArrayList<>(temporalEvolutionByCriterion.keySet()), displaySeries(jCheckBoxDiscounted.isSelected(), jCheckBoxUpfrontValues.isSelected()), numSlices);
        } else if (isUtility) {
            tablePane = new TemporalEvolutionTablePane(decisionSelected, new boolean[]{true}, isCumulative, new ArrayList<>(Collections.singletonList(variableOfInterest.getDecisionCriterion()
                                                                                                                                                                        .getCriterionName())), displaySeries(jCheckBoxDiscounted.isSelected(), false), numSlices);
        } else {
            tablePane = new TemporalEvolutionTablePane(temporalEvolutionResults, expandedNetwork, variableOfInterest,
                                                       conditioningVariables, numSlices, isUtility, isCumulative);
        }
        return tablePane;
    }
    
    /**
     * Allows to save a file with the excel or the png of the information showed in the screem
     */
    private void saveReport() throws IOException {
        Preferences prefs = Preferences.userRoot().node(getClass().getSimpleName());
        OMFileChooser omFileChooser = new OMFileChooser();
        omFileChooser.setCurrentDirectory(new File(prefs.get("LAST_FOLDER_TEMPEVO", ".")));
        String netName = FilenameUtils.getBaseName(expandedNetwork.getName());
        if (tabbedPane.getSelectedIndex() == 0) {
            omFileChooser.setSelectedFile(
                    new File(netName + "-" + variableOfInterest.getBaseName() + "-temporal_evolution.png"));
        } else {
            omFileChooser.setSelectedFile(
                    new File(netName + "-" + variableOfInterest.getBaseName() + "-temporal_evolution.xlsx"));
        }
        if (omFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String filename = omFileChooser.getSelectedFile().getAbsolutePath();
        if (omFileChooser.getSelectedFile().exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                                                       stringDatabase.getString("OverwriteFile.Text"),
                                                       stringDatabase.getString("OverwriteFile.Title"),
                                                       JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        prefs.put("LAST_FOLDER_TEMPEVO", omFileChooser.getSelectedFile().getParent());
        if (tabbedPane.getSelectedIndex() != 0) {
            createExcel(filename);
            return;
        }
        // Shows the default subtitles, save the png and then hide again the default subtitles
        for (int i = 0; i < chart.getSubtitleCount(); i++) {
            chart.getSubtitle(0).setVisible(true);
        }
        ChartUtils.saveChartAsPNG(new File(filename), chart, 1024, 768);
        for (int i = 0; i < chart.getSubtitleCount(); i++) {
            chart.getSubtitle(0).setVisible(false);
        }
    }
    
    /**
     * Allows to create an excel of a report
     *
     * @param filename the filename
     *
     * @throws IOException if an I/O error occurs
     */
    private void createExcel(String filename) throws IOException {
        TemporalEvolutionReport.write(filename, tablePane.getTable());
    }
    
    /**
     * Auxiliary method to make a legend JLabel with a title and an image
     *
     * @param title the title
     * @param image the image
     *
     * @return The legend label made
     */
    public static JLabel makeLegendLabel(String title, Image image) {
        Icon icon = ImageLoader.of(image);
        JLabel label = new JLabel(title);
        label.setIcon(icon);
        return label;
    }
    
    /**
     * Auxiliary method to obtain an Image from an LegendItem of JFreeChart and a color
     *
     * @param item the item
     * @param color the color
     *
     * @return The image made
     */
    public static Image makeImage(LegendItem item, Color color) {
        final int imgScale = 3;
        final int shapeScale = 2;
        Rectangle r = item.getShape().getBounds();
        BufferedImage image = new BufferedImage(r.width * imgScale, r.height * imgScale,
                                                BufferedImage.TYPE_BYTE_INDEXED);
        
        Graphics2D gr = image.createGraphics();
        gr.scale(shapeScale, shapeScale);
        gr.setBackground(GUIColors.TemporalEvoluation.BACKGROUND.getColor());
        gr.clearRect(0, 0, image.getWidth(), image.getHeight());
        gr.setColor(color);
        
        // move the shape in the region of the image
        gr.translate(-r.x * 1.5, -r.y * 1.5);
        gr.fill(item.getShape());
        gr.drawLine(-r.width, 0, r.width, 0);
        
        gr.dispose();
        
        return image;
    }
    
}
