/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.ExceptionDialog;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.sensitivityanalysis.exceptions.UncertainParameterException;
import org.openmarkov.sensitivityanalysis.model.AnalysisType;
import org.openmarkov.sensitivityanalysis.model.ParameterType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisConfiguration;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

/**
 * Main Dialog for Sensitivity Analysis
 *
 * @author gobispo
 * @author jperez-martin
 */
public class SensitivityAnalysisDialog extends OkCancelDialog implements Observer {
    
    /**
     * Sensitivity analysis controller
     */
    private SensitivityAnalysisController controller;
    
    /**
     * GUI Label for the number of iterations
     */
    private JLabel numberOfIterationsLabel;
    
    /**
     * GUI Label for the number of simulations
     */
    private JLabel iterationsOrSimulationsLabel;
    
    /**
     * Main panel of JFrame
     */
    private JPanel mainPanel;
    
    /**
     * Network name panel
     */
    private JPanel networkNamePanel;
    
    /**
     * Font of OpenMarkov
     */
    private Font openMarkovFont;
    
    /**
     * Analysis type selector
     */
    private JComboBox<String> analysisTypeSelector;
    
    /**
     * Panel with the number of iterations/simulations controls
     */
    private JPanel iterationSimulationsPanel;
    
    private JTextField numSimulationsTextField;
    
    private JCheckBox chkUseMultiThreading;
    
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * Constructor. initialises the instance.
     *
     * @param owner window that owns the dialog.
     */
    public SensitivityAnalysisDialog(Window owner, SensitivityAnalysisController controller) {
        super(owner);
        this.setModal(false);
        this.setResizable(true);
        this.controller = controller;
        controller.getSensitivityAnalysisModel().addObserver(this);
        
        setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
        
        for (ActionListener actionListener : getOKButton().getActionListeners()) {
            getOKButton().removeActionListener(actionListener);
        }
        getOKButton().addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    doOkClick();
                } catch (NonProjectablePotentialException | IncompatibleEvidenceException |
                         NotSupportedOperationException | NotEvaluableNetworkException.NotApplicableNetwork |
                         NotEvaluableNetworkException.UnsatisfiedConstraints | ConstraintViolatedException ex) {
                    throw new UnrecoverableException(ex);
                }
            }
        });
        
        // Set locale "." as decimal separator
        Locale.setDefault(new Locale("en", "US"));
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        /** Scrolling panel containing main panel */
        JScrollPane jScrollPane = new JScrollPane(mainPanel);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Panel show
        getComponentsPanel().add(jScrollPane);
        
        openMarkovFont = MainGUI.INSTANCE.mainPanel.getFont();
        
        if (controller.getProbNet() != null) {
            getContentFrame();
        }
        
        // Center dialog
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);
    }
    
    /**
     * This will format the GUI at execution time
     */
    public void getContentFrame() {
        mainPanel.setVisible(false);
        mainPanel.removeAll();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(getNetworkNamePanel());
        mainPanel.add(getAnalysisTypePanel());
        
        if (controller.getConfiguration()
                      .getParameterType() != ParameterType.NO_PARAMETER && controller.getUncertainParameters()
                                                                                     .isEmpty()) {
            ExceptionDialog.show(new UncertainParameterException.FewUncertainParameters(1, controller.getUncertainParameters()
                                                                                                     .size()));
            getOKButton().setEnabled(false);
        } else if (controller.getConfiguration()
                             .getParameterType() != ParameterType.NO_PARAMETER && controller.getConfiguration()
                                                                                            .isBiaxial() && controller.getUncertainParameters()
                                                                                                                      .size() < 2) {
            ExceptionDialog.show(new UncertainParameterException.FewUncertainParameters(2, controller.getUncertainParameters()
                                                                                                     .size()));
            getOKButton().setEnabled(false);
        } else {
            ParametersPanel parametersPanel = new ParametersPanel(controller);
            mainPanel.add(parametersPanel);
            mainPanel.add(getAxisVariationPanel());
            mainPanel.add(getScopePanel());
            mainPanel.add(getProbabilityAbovePanel());
            mainPanel.add(getIterationsSimulationsRequiredPanel());
            getOKButton().setEnabled(true);
        }
        mainPanel.setVisible(true);
        this.pack();
    }
    
    /**
     * Gets/builds the panel with the network name
     *
     * @return network name panel
     */
    public JPanel getNetworkNamePanel() {
        if (networkNamePanel == null) {
            networkNamePanel = new JPanel();
            networkNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel networkLabel = new JLabel(stringDatabase.getString("SensitivityAnalysis.General.Network"));
            networkLabel.setFont(openMarkovFont.deriveFont(openMarkovFont.getStyle() | Font.BOLD));
            JLabel networkName = new JLabel(controller.getProbNet().getName());
            networkName.setFont(openMarkovFont.deriveFont(openMarkovFont.getStyle() | Font.BOLD));
            networkNamePanel.add(networkLabel);
            networkNamePanel.add(networkName);
        }
        return networkNamePanel;
    }
    
    /**
     * Gets/builds the panel with the analysis type controls
     *
     * @return analysis type panel
     */
    public JPanel getAnalysisTypePanel() {
        
        AnalysisType analysisType = controller.getSensitivityAnalysisModel().getAnalysisType();
        JPanel analysisTypePanel = new JPanel();
        analysisTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel analysisTypeLabel = new JLabel(stringDatabase.getString("SensitivityAnalysis.Type.Title"));
        analysisTypePanel.add(analysisTypeLabel);
        
        analysisTypeSelector = new JComboBox<>();
        List<String> filteredTypes = getFilteredTypes();
        for (String type : filteredTypes) {
            analysisTypeSelector.addItem(type);
        }
        if (analysisType != null) {
            analysisTypeSelector.setSelectedItem(stringDatabase.getString(analysisType.toString()));
            updateFlags();
        } else {
            analysisTypeSelector.setSelectedIndex(0);
            updateFlags();
            reloadFrame();
        }
        
        analysisTypeSelector.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                AnalysisType analysisType = controller.getSensitivityAnalysisModel().getAnalysisType();
                if (!analysisTypeSelector.getSelectedItem().toString().equals(analysisType.toString())) {
                    updateFlags();
                    reloadFrame();
                }
            }
        });
        
        analysisTypePanel.add(analysisTypeSelector);
        
        if (controller.getConfiguration().isDeterministic()) {
            JLabel analysisPointsPerParameterLabel = new JLabel(
                    stringDatabase.getString("SensitivityAnalysis.General.PointsPerParameter"));
            analysisTypePanel.add(analysisPointsPerParameterLabel);
            
            JComboBox<Integer> pointPerParameterSelector = new JComboBox<>();
            pointPerParameterSelector.addItem(10);
            pointPerParameterSelector.addItem(20);
            pointPerParameterSelector.addItem(50);
            pointPerParameterSelector.addItem(100);
            pointPerParameterSelector.addActionListener(new ActionListener() {
                @Override @SuppressWarnings("unchecked") public void actionPerformed(ActionEvent e) {
                    JComboBox<Integer> pointPerParameterSelector = (JComboBox<Integer>) e.getSource();
                    controller.getSensitivityAnalysisModel()
                              .setNumberOfIterationsSimulations((Integer) pointPerParameterSelector.getSelectedItem());
                }
            });
            pointPerParameterSelector.setSelectedItem(50);
            pointPerParameterSelector.setBackground(GUIColors.SensitivityAnalysis.POINT_PER_PARAMETER_BACKGROUND.getColor());
            pointPerParameterSelector.setEditable(true);
            
            analysisTypePanel.add(pointPerParameterSelector);
        } else {
            JLabel numSimulationsLabel = new JLabel(
                    stringDatabase.getString("SensitivityAnalysis.General.Simulations"));
            analysisTypePanel.add(numSimulationsLabel);
            
            numSimulationsTextField = new JTextField(8);
            numSimulationsTextField.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JTextField numSimulationsTextField = (JTextField) e.getSource();
                    controller.getSensitivityAnalysisModel()
                              .setNumberOfIterationsSimulations(Integer.parseInt(numSimulationsTextField.getText()));
                }
            });
            
            numSimulationsTextField.addKeyListener(new KeyAdapter() {
                @Override public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) {
                        e.consume();
                    }
                }
            });
            
            numSimulationsTextField.setText("1000");
            controller.getSensitivityAnalysisModel()
                      .setNumberOfIterationsSimulations(Integer.parseInt(numSimulationsTextField.getText()));
            
            analysisTypePanel.add(numSimulationsTextField);
            
            chkUseMultiThreading = new JCheckBox(
                    stringDatabase.getString("SensitivityAnalysis.General.Multithreading"));
            chkUseMultiThreading.setSelected(true);
            controller.getSensitivityAnalysisModel()
                      .setMultithreading(chkUseMultiThreading.isSelected());
            
            analysisTypePanel.add(chkUseMultiThreading);
        }
        
        return analysisTypePanel;
    }
    
    /**
     * Updates the frame
     */
    private void reloadFrame() {
        // Reload the frame
        getContentFrame();
    }
    
    /**
     * Update the configuration flags of the selected analysis type
     */
    private void updateFlags() {
        AnalysisType analysisType = controller.getSensitivityAnalysisModel().getAnalysisType();
        SensitivityAnalysisConfiguration config = controller.getConfiguration();
        String selectedItem = analysisTypeSelector.getSelectedItem().toString();
        
        if (selectedItem.equals(stringDatabase.getString(AnalysisType.TORNADO_SPIDER.toString()))) {
            analysisType = AnalysisType.TORNADO_SPIDER;
            config.setIsDeterministic(true);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.MULTI_PARAMETER);
            config.setCanBeGlobal(true);
            config.setCanBeDecision(true);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.PLOT.toString()))) {
            analysisType = AnalysisType.PLOT;
            config.setIsDeterministic(true);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.ONE_PARAMETER);
            config.setCanBeGlobal(true);
            config.setCanBeDecision(true);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.MAP.toString()))) {
            analysisType = AnalysisType.MAP;
            config.setIsDeterministic(true);
            config.setIsBiaxial(true);
            config.setParameterType(ParameterType.ONE_PARAMETER);
            config.setCanBeGlobal(true);
            config.setCanBeDecision(true);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.ACCEPTABILITY.toString()))) {
            analysisType = AnalysisType.ACCEPTABILITY;
            config.setIsDeterministic(false);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.ONE_PARAMETER);
            config.setCanBeGlobal(false);
            config.setCanBeDecision(true);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.EVPI.toString()))) {
            analysisType = AnalysisType.EVPI;
            config.setIsDeterministic(false);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.ONE_PARAMETER);
            config.setCanBeGlobal(true);
            config.setCanBeDecision(false);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.SPIDER_CE.toString()))) {
            analysisType = AnalysisType.SPIDER_CE;
            config.setIsDeterministic(true);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.MULTI_PARAMETER);
            config.setCanBeGlobal(false);
            config.setCanBeDecision(true);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.ACCEPTABILITY_CURVE.toString()))) {
            analysisType = AnalysisType.ACCEPTABILITY_CURVE;
            config.setIsDeterministic(false);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.NO_PARAMETER);
            config.setCanBeGlobal(false);
            config.setCanBeDecision(true);
        } else if (selectedItem.equals(stringDatabase.getString(AnalysisType.CEPLANE.toString()))) {
            analysisType = AnalysisType.CEPLANE;
            config.setIsDeterministic(false);
            config.setIsBiaxial(false);
            config.setParameterType(ParameterType.NO_PARAMETER);
            config.setCanBeGlobal(false);
            config.setCanBeDecision(true);
        }
        
        controller.getSensitivityAnalysisModel().setAnalysisType(analysisType);
    }
    
    /**
     * Gets the filtered analysis types for the unicriteria/bicriteria analysis
     * z
     *
     * @return Strings of filtered types
     */
    public List<String> getFilteredTypes() {
        List<String> filteredTypes = new ArrayList<>();
        if (controller.getConfiguration().isUnicriterion()) {
            filteredTypes.add(stringDatabase.getString(AnalysisType.TORNADO_SPIDER.toString()));
            filteredTypes.add(stringDatabase.getString(AnalysisType.PLOT.toString()));
            filteredTypes.add(stringDatabase.getString(AnalysisType.MAP.toString()));
            //filteredTypes.add(stringDatabase.getValuesInAString(AnalysisType.ACCEPTABILITY.toString()));
            //filteredTypes.add(stringDatabase.getValuesInAString(AnalysisType.EVPI.toString()));
        } else {
            filteredTypes.add(stringDatabase.getString(AnalysisType.SPIDER_CE.toString()));
            filteredTypes.add(stringDatabase.getString(AnalysisType.CEPLANE.toString()));
            //filteredTypes.add(stringDatabase.getValuesInAString(AnalysisType.EVPI.toString()));
        }
        
        return filteredTypes;
    }
    
    /**
     * Builds the axis variation panel
     *
     * @return axis variation panel
     */
    public AxisVariationPanel getAxisVariationPanel() {
        return new AxisVariationPanel(controller);
    }
    
    /**
     * Builds the scope panel
     *
     * @return scope panel
     */
    public ScopePanel getScopePanel() {
        return new ScopePanel(controller);
    }
    
    /**
     * Builds the panel with the "ProbabilityAboveOne" control
     *
     * @return ProbabilityAbove panel
     */
    public JPanel getProbabilityAbovePanel() {
        JPanel probabilityAbovePanel = new JPanel();
        probabilityAbovePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        probabilityAbovePanel
                .add(new JLabel(stringDatabase.getString("SensitivityAnalysis.General.ProbabilityAboveOne")));
        
        JComboBox<String> probabilityAboveOneSelector = new JComboBox<>();
        probabilityAboveOneSelector.addItem(stringDatabase.getString("SensitivityAnalysis.General.Ignore"));
        probabilityAboveOneSelector.addItem(stringDatabase.getString("SensitivityAnalysis.General.ThrowError"));
        probabilityAboveOneSelector.addActionListener(new ActionListener() {
            @Override @SuppressWarnings("unchecked") public void actionPerformed(ActionEvent e) {
                JComboBox<String> selector = (JComboBox<String>) e.getSource();
                controller.getSensitivityAnalysisModel()
                          .setThrowErrorMessageIfProbAboveOne(
                                  selector.getSelectedItem()
                                          .equals(stringDatabase.getString("SensitivityAnalysis.General.ThrowError")));
            }
        });
        probabilityAbovePanel.add(probabilityAboveOneSelector);
        
        return probabilityAbovePanel;
    }
    
    /**
     * Builds the iterations/simulations required panel
     *
     * @return iterations/simulations panel
     */
    public JPanel getIterationsSimulationsRequiredPanel() {
        iterationSimulationsPanel = new JPanel();
        iterationSimulationsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        numberOfIterationsLabel = new JLabel(String.valueOf(0));
        iterationSimulationsPanel.add(numberOfIterationsLabel);
        
        iterationsOrSimulationsLabel = new JLabel(
                stringDatabase.getString("SensitivityAnalysis.General.IterationsRequired"));
        iterationSimulationsPanel.add(iterationsOrSimulationsLabel);
        
        updateIterationsSimulations();
        
        return iterationSimulationsPanel;
    }
    
    /**
     * Update the number of iterations/simulations given the number of parameters and the selected number of iterations/simulations
     */
    private void updateIterationsSimulations() {
        // Set number of iterations
        if (controller.getConfiguration().isDeterministic()) {
            int numIterations = 0;
            
            int numSelectedParametersXAxis;
            if (controller.getSensitivityAnalysisModel().getSelectedUncertainParametersXAxis() != null) {
                numSelectedParametersXAxis = controller.getSensitivityAnalysisModel()
                                                       .getSelectedUncertainParametersXAxis().size();
                numIterations = numSelectedParametersXAxis * controller.getSensitivityAnalysisModel()
                                                                       .getNumberOfIterationsSimulations();
            }
            
            if (controller.getConfiguration().isBiaxial()) {
                int numSelectedParametersYAxis = 1;
                if (controller.getSensitivityAnalysisModel().getSelectedUncertainParametersYAxis() != null) {
                    numSelectedParametersYAxis = controller.getSensitivityAnalysisModel()
                                                           .getSelectedUncertainParametersYAxis().size();
                    numSelectedParametersYAxis *= controller.getSensitivityAnalysisModel()
                                                            .getNumberOfIterationsSimulations();
                }
                
                numIterations *= numSelectedParametersYAxis;
            }
            
            numberOfIterationsLabel.setText(String.valueOf(numIterations));
            iterationsOrSimulationsLabel
                    .setText(stringDatabase.getString("SensitivityAnalysis.General.IterationsRequired"));
        } else {
            // Set number of simulations
            int numSimulations = controller.getSensitivityAnalysisModel().getNumberOfIterationsSimulations();
            numberOfIterationsLabel.setText(String.valueOf(numSimulations));
            iterationsOrSimulationsLabel
                    .setText(stringDatabase.getString("SensitivityAnalysis.General.SimulationsRequired"));
        }
    }
    
    protected void doOkClick() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotSupportedOperationException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, ConstraintViolatedException {
        if (!controller.getConfiguration().isDeterministic()) {
            controller.getSensitivityAnalysisModel()
                      .setNumberOfIterationsSimulations(Integer.parseInt(numSimulationsTextField.getText()));
            controller.getSensitivityAnalysisModel()
                      .setMultithreading(chkUseMultiThreading.isSelected());
            
        }
        if (this.controller.getSensitivityAnalysisModel().getDecisionVariable() != null
                && this.controller.getSensitivityAnalysisModel().getSelectedScenario() != null && !this.controller
                .getSensitivityAnalysisModel().getSelectedScenario().isEmpty()) {
            
            EvidenceCase newPreResolutionEvidence = new EvidenceCase(controller.getPreResolutionEvidence());
            for (Finding finding : controller.getSensitivityAnalysisModel().getSelectedScenario()) {
                newPreResolutionEvidence.addFinding(finding);
            }
            controller.setPreResolutionEvidence(newPreResolutionEvidence);
            
        }
        controller.runAnalysis();
    }
    
    @Override protected void doCancelClickBeforeHide() {
        controller.closeAllPlots();
    }
    
    @Override
    /**
     * Update the GUI when the model has been modified
     */ public void update(Observable o, Object arg) {
        if (iterationSimulationsPanel != null) {
            updateIterationsSimulations();
        }
    }
}
