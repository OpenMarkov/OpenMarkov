/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.dialog;

import org.openmarkov.bnEvaluation.Coherence;
import org.openmarkov.bnEvaluation.LearningEvaluator;
import org.openmarkov.bnEvaluation.SplitSet;
import org.openmarkov.bnEvaluation.SplitSetManager;
import org.openmarkov.bnEvaluation.component.DBOpenerPanel;
import org.openmarkov.bnEvaluation.component.MeasuresPanel;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.component.NumericSpinner;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.java.function.PriorityComparator;
import org.openmarkov.learning.algorithm.naivebayes.NaiveBayesAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.fanb.ForestAugmentedNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.kdb.KDBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.snb.SelectiveNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.spnb.SuperParentNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.treeaugmentednb.TreeAugmentedNBAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmManager;
import org.openmarkov.learning.gui.AlgorithmConfigurationManager;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is the GUI responsible for collecting the information for the class
 * that will evaluate the learning.
 *
 * @author evillar
 */
public final class CrossValidationDialog extends OkCancelDialog {
    
    private final AlgorithmConfigurationManager algorithmConfigurationManager;
    
    // caseDatabasePanel components
    private final DBOpenerPanel dbOpenerPanel;
    
    //  algorithmPanel components
    private AlgorithmParametersDialog optionsGUI;
    private JButton optionsButton;
    private JTextArea optionsTextArea;
    private JComboBox<Class<? extends LearningAlgorithm>> algorithmComboBox;
    private JRadioButton generativeAlgorithmButton;
    private JRadioButton discriminativeAlgorithmButton;
    private JComboBox<Variable> variableCombobox;
    // optionsPanel components
    private JRadioButton crossValidationRadioButton;
    private JRadioButton multipleSamplesRadioButton;
    private NumericSpinner<Integer> kFolderTextField;
    private NumericSpinner<Integer> numberSamplesTextField;
    private NumericSpinner<Integer> sampleSizeTextField;
    
    private MeasuresPanel measuresPanel;
    
    private final Frame owner;
    
    public CrossValidationDialog(Frame owner) {
        super(owner);
        this.owner = owner;
        this.setTitle("Cross validation");
        this.setMinimumSize(new Dimension(400, 750));
        this.setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        algorithmConfigurationManager = new AlgorithmConfigurationManager((JFrame) this.owner);
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(700, 400));
        // add components with a BoxLayout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        this.dbOpenerPanel = new DBOpenerPanel(this);
        this.dbOpenerPanel.onOpen((databaseFile, database) -> {
            setEnableComponents();
            kFolderTextField.setMaximum(database.getNumCases());
            sampleSizeTextField.setMaximum(database.getNumCases());
            int sampleSize = (int) (0.25 * database.getNumCases());
            sampleSizeTextField.setValue(sampleSize);
            this.enableSpinnersForValidation();
        });
        mainPanel.add(getCaseDatabasePanel());
        mainPanel.add(getAlgorithmPanel());
        mainPanel.add(getOptionsPanel());
        mainPanel.add(getMeasuresPanel());
        this.add(mainPanel);
        setEnableComponents();
        this.pack();
        setSize(new Dimension(720, 700));
    }
    
    /**
     * Gets the Panel with the components to open the case database
     *
     * @return: JPanel
     */
    private JPanel getCaseDatabasePanel() {
        return this.dbOpenerPanel;
    }
    
    /**
     * Gets the Panel with the components to select learning algorithm
     *
     * @return: JPanel
     */
    private JPanel getAlgorithmPanel() {
        JPanel algorithmPanel = new JPanel();
        algorithmPanel.setBorder(BorderFactory.createTitledBorder("Learning algorithm"));
        
        // create components
        JLabel algorithmLabel = new JLabel("Learning algorithm");
        generativeAlgorithmButton = new JRadioButton();
        discriminativeAlgorithmButton = new JRadioButton();
        ButtonGroup algorithmTypeGroup = new ButtonGroup();
        generativeAlgorithmButton.setText("Generative");
        generativeAlgorithmButton.addActionListener(e -> algorithmButtonActionPerformed(e));
        discriminativeAlgorithmButton.setText("Discriminative");
        discriminativeAlgorithmButton.addActionListener(e -> algorithmButtonActionPerformed(e));
        // default options
        generativeAlgorithmButton.setSelected(true);
        generativeAlgorithmButton.setEnabled(false);
        discriminativeAlgorithmButton.setEnabled(false);
        
        algorithmTypeGroup.add(generativeAlgorithmButton);
        algorithmTypeGroup.add(discriminativeAlgorithmButton);
        
        // algorithm ComboBox
        algorithmComboBox = new JComboBox<>();
        algorithmComboBox.setRenderer(new JComboBoxFunctionRender<Class<? extends LearningAlgorithm>>
                                              (item-> LearningAlgorithmManager.info(item).name()));
        optionsButton = new JButton();
        optionsTextArea = new JTextArea();
        JScrollPane optionsTextScrollPane = new JScrollPane();
        algorithmComboBox.addActionListener(e -> algorithmComboBoxActionPerformed());
        algorithmComboBox.removeAllItems();
        LearningAlgorithmManager.INSTANCE.getGenerativeAlgorithms().forEach(algorithmComboBox::addItem);
        
        
        variableCombobox = new JComboBox<Variable>();
        variableCombobox.setRenderer(new JComboBoxFunctionRender<>(Variable::getName));
        variableCombobox.setEnabled(false);
        
        JLabel classVariableLabel = new JLabel("Class variable:");
        classVariableLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        
        Class<? extends LearningAlgorithm> selectedAlgorithm = (Class<? extends LearningAlgorithm>) algorithmComboBox.getSelectedItem();
        optionsGUI = algorithmConfigurationManager.instanciateForAlgorithm(selectedAlgorithm);
        optionsButton.setText("Learning options");
        optionsButton.addActionListener(e -> optionsButtonActionPerformed());
        optionsTextArea.setText((optionsGUI != null) ? optionsGUI.getDescription() : "");
        
        optionsTextArea.setColumns(20);
        optionsTextArea.setRows(3);
        optionsTextArea.setEnabled(false);
        optionsTextScrollPane.setViewportView(optionsTextArea);
        
        GroupLayout layout = new GroupLayout(algorithmPanel);
        algorithmPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                                        .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(layout.createParallelGroup()
                                                                        .addComponent(discriminativeAlgorithmButton)
                                                                        .addComponent(generativeAlgorithmButton)
                                                                        .addComponent(algorithmLabel))
                                                        .addGap(20)
                                                        .addGroup(layout.createParallelGroup()
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                        .addComponent(classVariableLabel)
                                                                                        .addGap(20)
                                                                                        .addComponent(variableCombobox))
                                                                        .addComponent(algorithmComboBox))
                                                        .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(optionsTextScrollPane)
                                                        .addGap(20)
                                                        .addComponent(optionsButton)
                                                        .addContainerGap()));
        layout.setVerticalGroup(layout.createSequentialGroup()
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                      .addComponent(discriminativeAlgorithmButton)
                                                      .addComponent(classVariableLabel)
                                                      .addComponent(variableCombobox))
                                      .addGap(5)
                                      .addComponent(generativeAlgorithmButton)
                                      .addGap(5)
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                      .addComponent(algorithmLabel)
                                                      .addComponent(algorithmComboBox))
                                      .addGap(5)
                                      .addGroup(layout.createParallelGroup()
                                                      .addComponent(optionsTextScrollPane, GroupLayout.PREFERRED_SIZE,
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    GroupLayout.PREFERRED_SIZE)
                                                      .addComponent(optionsButton))
                                      .addContainerGap());
        return algorithmPanel;
    }
    
    /**
     * Gets the Panel with the components to select Evaluation options
     *
     * @return: JPanel
     */
    private JPanel getOptionsPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Evaluation options"));
        // create components
        crossValidationRadioButton = new JRadioButton("Cross validation");
        crossValidationRadioButton.addActionListener(e -> this.enableSpinnersForValidation());
        multipleSamplesRadioButton = new JRadioButton("Multiple samples");
        multipleSamplesRadioButton.addActionListener(e -> this.enableSpinnersForValidation());
        ButtonGroup optionsButtons = new ButtonGroup();
        optionsButtons.add(crossValidationRadioButton);
        optionsButtons.add(multipleSamplesRadioButton);
        JLabel kFolderLabel = new JLabel("Folds");
        JLabel numberSamplesLabel = new JLabel("Number of samples");
        JLabel sampleSizeLabel = new JLabel("Sample size");
        kFolderTextField = new NumericSpinner<>(Integer.class);
        kFolderTextField.setMinimum(2);
        
        numberSamplesTextField = new NumericSpinner<>(Integer.class);
        numberSamplesTextField.setMinimum(1);
        numberSamplesTextField.setMaximum(100);
        numberSamplesTextField.setValue(10);
        
        sampleSizeTextField = new NumericSpinner<>(Integer.class);
        sampleSizeTextField.setMinimum(1);
        
        // default properties
        crossValidationRadioButton.setSelected(true);
        kFolderTextField.setValue(10);
        
        //layout panel
        GroupLayout layout = new GroupLayout(optionsPanel);
        optionsPanel.setLayout(layout);
        //automatic gaps
        layout.setHorizontalGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(layout.createParallelGroup()
                                                        .addComponent(crossValidationRadioButton)
                                                        .addComponent(multipleSamplesRadioButton))
                                        .addGroup(layout.createParallelGroup()
                                                        .addComponent(kFolderLabel)
                                                        .addComponent(numberSamplesLabel)
                                                        .addComponent(sampleSizeLabel))
                                        .addGap(20)
                                        .addGroup(layout.createParallelGroup()
                                                        .addComponent(kFolderTextField)
                                                        .addComponent(numberSamplesTextField)
                                                        .addComponent(sampleSizeTextField))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                                         GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addContainerGap());
        layout.setVerticalGroup(layout.createSequentialGroup()
                                      .addComponent(crossValidationRadioButton)
                                      .addGap(5)
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                      .addComponent(kFolderLabel)
                                                      .addComponent(kFolderTextField))
                                      .addGap(5)
                                      .addComponent(multipleSamplesRadioButton)
                                      .addGap(5)
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                      .addComponent(numberSamplesLabel)
                                                      .addComponent(numberSamplesTextField))
                                      .addGap(5)
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                      .addComponent(sampleSizeLabel)
                                                      .addComponent(sampleSizeTextField))
                                      .addContainerGap());
        return optionsPanel;
    }
    
    /**
     * Gets the Panel with the options to select the measures (classification variable...)
     *
     * @return JPanel
     */
    private MeasuresPanel getMeasuresPanel() {
        if (this.measuresPanel == null) {
            this.measuresPanel = new MeasuresPanel();
            this.measuresPanel.setEnabled(false);
            measuresPanel.allMeasureRelatedCheckboxes()
                          .forEach(metricCheckbox -> metricCheckbox.addChangeListener(e -> checkOkButton()));
        }
        return this.measuresPanel;
    }
    
    /**
     * This method manages the enabling of the different components
     */
    private void setEnableComponents() {
        // Gui components are disable. To be enable its necesary select the database.
        CaseDatabase database = this.dbOpenerPanel.getDatabase();
        boolean enabled = database != null;
        getOKButton().setEnabled(enabled);
        discriminativeAlgorithmButton.setEnabled(enabled);
        generativeAlgorithmButton.setEnabled(enabled);
        algorithmComboBox.setEnabled(enabled);
        optionsButton.setEnabled(enabled);
        crossValidationRadioButton.setEnabled(enabled);
        multipleSamplesRadioButton.setEnabled(enabled);
        kFolderTextField.setEnabled(enabled);
        numberSamplesTextField.setEnabled(enabled);
        sampleSizeTextField.setEnabled(enabled);
        this.measuresPanel.setEnabled(enabled);
        if (enabled) {
            List<Variable> variables = database.getVariables();
            if (!discriminativeAlgorithmButton.isSelected()) {
                variableCombobox.setSelectedItem(null);
                variableCombobox.setEnabled(false);
            } else {
                variableCombobox.removeAllItems();
                for (Variable variable : variables) {
                    variableCombobox.addItem(variable);
                }
                variableCombobox.setEnabled(true);
            }
            this.measuresPanel.changeVariables(variables.stream().map(Variable::getName).toList());
        }
        
    }
    
    /**
     * this method manages the GUI components according to the type of model
     * Is the listener of discriminativeAlgorithmButton and generativeAlgorithmButton
     */
    private void algorithmButtonActionPerformed(ActionEvent e) {
        Object source = e.getSource();
        boolean discriminative = false;
        if (source == discriminativeAlgorithmButton) {
            discriminative = true;
        }
        variableCombobox.setEnabled(discriminative);
        algorithmComboBox.removeAllItems();
        
        Stream<Class<? extends LearningAlgorithm>> algorithms;
        if (discriminative) {
            algorithms = LearningAlgorithmManager.INSTANCE.getDiscriminativeAlgorithms()
                                        .sorted(new PriorityComparator<>(
                                                NaiveBayesAlgorithm.class,
                                                SelectiveNBAlgorithm.class,
                                                KDBAlgorithm.class,
                                                TreeAugmentedNBAlgorithm.class,
                                                ForestAugmentedNBAlgorithm.class,
                                                SuperParentNBAlgorithm.class
                                        ));
        } else {
            algorithms = LearningAlgorithmManager.INSTANCE.getGenerativeAlgorithms();
        }
        algorithms.forEach(algorithmComboBox::addItem);
        
        if (!discriminative) {
            variableCombobox.setSelectedItem(null);
        } else {
            if (this.dbOpenerPanel.getDatabase() != null) {
                variableCombobox.removeAllItems();
                List<Variable> variables = this.dbOpenerPanel.getDatabase().getVariables();
                for (Variable variable : variables) {
                    variableCombobox.addItem(variable);
                }
            }
        }
        Class<? extends LearningAlgorithm> selectedAlgorithm = (Class<? extends LearningAlgorithm>) algorithmComboBox.getSelectedItem();
        optionsGUI = algorithmConfigurationManager.instanciateForAlgorithm(selectedAlgorithm);
        optionsButton.setEnabled(optionsGUI != null);
        optionsTextArea.setText((optionsGUI != null) ? optionsGUI.getDescription() : "");
    }
    
    private void enableSpinnersForValidation() {
        kFolderTextField.setEnabled(crossValidationRadioButton.isSelected());
        numberSamplesTextField.setEnabled(!crossValidationRadioButton.isSelected());
        sampleSizeTextField.setEnabled(!crossValidationRadioButton.isSelected());
    }
    
    /**
     * This method is the listener of algorithmComboBox( for select the learning Algorithm)
     */
    private void algorithmComboBoxActionPerformed() {
        Class<? extends LearningAlgorithm> selectedAlgorithm = (Class<? extends LearningAlgorithm>) algorithmComboBox.getSelectedItem();
        if(selectedAlgorithm==null){
            return;
        }
        optionsGUI = algorithmConfigurationManager.instanciateForAlgorithm(selectedAlgorithm);
        //optionsButton.setEnabled(optionsGUI != null);
        optionsTextArea.setText((optionsGUI != null) ? optionsGUI.getDescription() : "");
    }
    
    /**
     * This method is the listener of optionsButton (for select the options of the learning Algorithm)
     * make visible the window to select options (optionsGui)
     */
    private void optionsButtonActionPerformed() {
        optionsGUI.setVisible(true);
        optionsTextArea.setText(optionsGUI.getDescription());
    }
    
    /**
     * Check that at least one measure is selected
     *
     * @return: boolean
     */
    private boolean checkAnyMeasures() {
        return this.measuresPanel.allMeasureRelatedCheckboxes().anyMatch(JCheckBox::isSelected);
    }
    
    private void checkOkButton() {
        getOKButton().setEnabled(checkAnyMeasures() && this.dbOpenerPanel.getDatabase() != null);
    }
    
    @Override
    protected boolean doOkClickBeforeHide() throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        Class<? extends LearningAlgorithm> algorithmType = (Class<? extends LearningAlgorithm>) algorithmComboBox.getSelectedItem();
        String algorithmName = (String) LearningAlgorithmManager.info( algorithmType).name();
        // options
        @ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG, reasonDescription = "This options variable was used to" +
                " be taken out from optionsGUI, and that method has been removed")
        ArrayList<Object> options = new ArrayList<>();
        //ArrayList<Object> options = optionsGUI.getOptions();
        String title = "Evaluation of the " + algorithmName + " algorithm. Options: " +
                optionsGUI.getDescription() + ".\n";
        double fraccion;
        CaseDatabase database = this.dbOpenerPanel.getDatabase();
        if (crossValidationRadioButton.isSelected()) {
            title = title + "Cross validation with k=" + kFolderTextField.getValue() + " folders.\n";
            fraccion = (Double.valueOf(kFolderTextField.getCurrentValue()) - 1.0) / Double.valueOf(kFolderTextField.getCurrentValue());
        } else {
            title = title + "Multiples samples: " + numberSamplesTextField.getCurrentValue() +
                    " samples of " + sampleSizeTextField.getCurrentValue() + " cases each.\n";
            fraccion = ((double) database.getNumCases() - Double.valueOf(sampleSizeTextField.getCurrentValue())) /
                    database.getNumCases();
        }
        int numCasesRed = (int) (database.getNumCases() * fraccion);
        title = title + "In each iteration, the network is learned with " + numCasesRed +
                " cases.";
        MeasuresSet measuresSet = this.getMeasuresPanel().measuresSet(title, Coherence.WEAK, database);
        SplitSetManager splitSetManager = new SplitSetManager(database);
        SplitSet[] sets;
        if (crossValidationRadioButton.isSelected()) {
            sets = splitSetManager.crossValidation(kFolderTextField.getCurrentValue());
        } else {
            sets = splitSetManager.multipleSamples(numberSamplesTextField.getCurrentValue(),
                                                   sampleSizeTextField.getCurrentValue());
        }
        LearningEvaluator evaluator = new LearningEvaluator(algorithmType, options,
                                                            sets, measuresSet);
        if (discriminativeAlgorithmButton.isSelected()) {
            evaluator.setVariable(((Variable)variableCombobox.getSelectedItem()).getName());
        }
        ResultsDialog resultsDialog = new ResultsDialog(owner, evaluator.runEvaluator());
        this.setVisible(false);
        resultsDialog.setVisible(true);
        return true;
    }
    
}
