/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.dialog;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.bnEvaluation.component.DBOpenerPanel;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoReaderForExtension;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.commonComponents.GeneralMode;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.dialog.common.BottomPanelButtonDialog;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.gui.dialog.io.DBWriterOMFileChooser;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.FeatureSelection;
import org.openmarkov.learning.core.preprocess.FilterDatabase;
import org.openmarkov.learning.core.preprocess.MissingValues;
import org.openmarkov.learning.core.preprocess.Outliers;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the GUI responsible for collecting the options to divide sets
 *
 * @author evillar
 */

public final class DataPreprocessingDialog extends BottomPanelButtonDialog {
    
    /**
     * caseDatabasePanel Swing components
     */
    private DBOpenerPanel dbOpenerPanel;
    
    
    private JComboBox<String> missingValuesComboBox;
    private JPanel missingValuesPanel;
    private JComboBox<GeneralMode<Discretization.Option>> discretizeComboBox;
    private JComboBox<String> classVariableComboBox;
    private JComboBox<Outliers.Option> outliersComboBox;
    private JComboBox<FeatureSelection.Method> featureSelectionComboBox;
    private JSpinner featureSelectionTopKSpinner;
    private JPanel discretizePanel;
    private boolean[] isNumeric;
    private JPanel numIntervalsPanel;
    private JSpinner numIntervalsSpinner;
    private JCheckBox numIntervalsCheckBox;
    private JCheckBox selectDeselectCheckBox;
    private JPanel varSelectionPanel;
    private JRadioButton allVariablesRadioButton;
    private JRadioButton selectedVariablesRadioButton;
    
    /**
     * Acept and cancel buttons
     */
    private JButton resetButton;
    private JButton savePreprocessSetButton;
    private JButton cancelButton;
    private final DBWriterOMFileChooser saveWritter;
    
    public DataPreprocessingDialog(Frame owner) {
        super(owner);
        this.setTitle("Data Preprocessing");
        this.setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        saveWritter = new DBWriterOMFileChooser(false);
        saveWritter.setDialogTitle("Save preprocessed dataset in...");
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(750, 500));
        // add components with a BoxLayout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        this.dbOpenerPanel = new DBOpenerPanel(this);
        mainPanel.add(this.dbOpenerPanel);
        this.dbOpenerPanel.onOpen((databaseFile, database) -> {
            database.getNumCases();
            // preprocessing variables
            isNumeric = new boolean[database.getVariables().size()];
            updateVariableSelectionPanel();
            resetButton.setEnabled(true);
            savePreprocessSetButton.setEnabled(true);
        });
        // tabbedPane whit the processing variables and the splitst
        JTabbedPane tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane);
        
        JPanel variablesPanel = new JPanel();
        this.dbOpenerPanel.setPreferredSize(new Dimension(750, 500));
        
        // general components (for all variables)
        discretizeComboBox = new JComboBox<GeneralMode<Discretization.Option>>();
        discretizeComboBox.setRenderer(new JComboBoxFunctionRender<GeneralMode<Discretization.Option>>(GeneralMode::toString));
        // add items to combo box
        discretizeComboBox.addItem(GeneralMode.manuallySpecifyingEach("Specify for each variable"));
        discretizeComboBox.addItem(GeneralMode.asSetAllTo(Discretization.Option.NONE));
        discretizeComboBox.addItem(GeneralMode.asSetAllTo(Discretization.Option.EQUAL_FREQ));
        discretizeComboBox.addItem(GeneralMode.asSetAllTo(Discretization.Option.EQUAL_WIDTH));
        discretizeComboBox.addItem(GeneralMode.asSetAllTo(Discretization.Option.MDLP));
        discretizeComboBox.addItem(GeneralMode.asSetAllTo(Discretization.Option.CHIMERGE));
        discretizeComboBox.addItem(GeneralMode.asSetAllTo(Discretization.Option.KMEANS));
        discretizeComboBox.setSelectedIndex(0);
        discretizeComboBox.addActionListener(e -> discretizeComboBoxActionPerformed());

        classVariableComboBox = new JComboBox<>();
        classVariableComboBox.setEnabled(false);

        outliersComboBox = new JComboBox<>();
        outliersComboBox.setRenderer(new JComboBoxFunctionRender<Outliers.Option>(Localizable::localize));
        for (Outliers.Option opt : Outliers.Option.values()) outliersComboBox.addItem(opt);
        outliersComboBox.setSelectedItem(Outliers.Option.NONE);

        featureSelectionComboBox = new JComboBox<>();
        featureSelectionComboBox.setRenderer(new JComboBoxFunctionRender<FeatureSelection.Method>(Localizable::localize));
        for (FeatureSelection.Method m : FeatureSelection.Method.values()) featureSelectionComboBox.addItem(m);
        featureSelectionComboBox.setSelectedItem(FeatureSelection.Method.NONE);
        featureSelectionTopKSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        
        missingValuesComboBox = new JComboBox<String>();
        missingValuesComboBox.addItem("Specify for each variable");
        missingValuesComboBox.addItem("Keep records with missing values");
        missingValuesComboBox.addItem("Erase records with missing values");
        missingValuesComboBox.addItem("Impute missing values with the mode (most frequent state)");
        missingValuesComboBox.addItem("Impute missing values with the mean (numeric variables)");
        missingValuesComboBox.addItem("Impute missing values with the median (numeric variables)");
        missingValuesComboBox.addItem("Impute missing values with k-Nearest Neighbours (k=5)");
        missingValuesComboBox.setSelectedIndex(0);
        missingValuesComboBox.addActionListener(e -> missingValuesComboBoxActionPerformed());
        
        numIntervalsCheckBox = new JCheckBox("Same number of intervals");
        numIntervalsSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
        numIntervalsCheckBox.setContentAreaFilled(false);
        numIntervalsCheckBox.setMargin(new Insets(2, 2, 2, 0));
        numIntervalsCheckBox.addActionListener(e -> numIntervalsCheckBoxActionPerformed());
        
        numIntervalsSpinner.setEnabled(false);
        numIntervalsSpinner.addChangeListener(evt -> {
            //GEN-FIRST:event_numIntervalsSpinnerStateChanged
            Integer selected = (Integer) numIntervalsSpinner.getValue();
            for (Component component : numIntervalsPanel.getComponents()) {
                ((JSpinner) component).setValue(selected);
            }
        });
        selectedVariablesRadioButton = new JRadioButton("Selected variables");
        allVariablesRadioButton = new JRadioButton("All variables");
        selectDeselectCheckBox = new JCheckBox("Select/unselect all variables");
        ButtonGroup variablesButtonGroup = new ButtonGroup();
        variablesButtonGroup.add(allVariablesRadioButton);
        variablesButtonGroup.add(selectedVariablesRadioButton);
        allVariablesRadioButton.setSelected(true);
        allVariablesRadioButton.setEnabled(false);
        allVariablesRadioButton.addActionListener(e -> allVariablesRadioButtonActionPerformed());
        selectedVariablesRadioButton.setEnabled(false);
        selectedVariablesRadioButton.addActionListener(e -> selectedVariablesRadioButtonActionPerformed());
        selectDeselectCheckBox.setEnabled(false);
        selectDeselectCheckBox.addActionListener(e -> selectDeselectCheckBoxActionPerformed());
        
        JLabel missingValuesLabel = new JLabel("Missing values");
        JLabel discretizeLabel = new JLabel("Discretize");
        JLabel intervalsLabel = new JLabel("Number of intervals:");
        
        // create the panel ShowVariablesPanel and its components
        JPanel showVariablesPanel = new JPanel();
        JScrollPane jScrollVariables = new JScrollPane();
        
        varSelectionPanel = new JPanel();
        missingValuesPanel = new JPanel();
        discretizePanel = new JPanel();
        numIntervalsPanel = new JPanel();
        
        // labels for the variables
        JLabel preprocessVariablesLabel = new JLabel("Preprocessing");
        JLabel missingValuesVariableLabel = new JLabel("Missing values");
        JLabel discretizeVariablesLabel = new JLabel("Discretize");
        JLabel intervalVariablesLabel = new JLabel("Number of intervals:");
        
        showVariablesPanel.setLayout(new GridBagLayout());
        // selection variables
        varSelectionPanel.setLayout(new GridLayout(0, 1, 0, 20));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 0, 4, 8);
        showVariablesPanel.add(varSelectionPanel, gridBagConstraints);
        // missing values
        missingValuesPanel.setLayout(new GridLayout(0, 1, 0, 20));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 8);
        showVariablesPanel.add(missingValuesPanel, gridBagConstraints);
        // discretizePanel
        discretizePanel.setLayout(new GridLayout(0, 1, 0, 20));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 8);
        showVariablesPanel.add(discretizePanel, gridBagConstraints);
        // numIntervalsPanel
        numIntervalsPanel.setLayout(new GridLayout(0, 1, 0, 20));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        showVariablesPanel.add(numIntervalsPanel, gridBagConstraints);
        jScrollVariables.setViewportView(showVariablesPanel);
        // layout all components
        org.jdesktop.layout.GroupLayout variablesPanelLayout = new org.jdesktop.layout.GroupLayout(variablesPanel);
        variablesPanel.setLayout(variablesPanelLayout);
        variablesPanelLayout.setHorizontalGroup(
                variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(variablesPanelLayout.createSequentialGroup()
                                                             .add(variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                      .add(variablesPanelLayout.createSequentialGroup()
                                                                                                               .addContainerGap()
                                                                                                               .add(variablesPanelLayout
                                                                                                                            .createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                            .add(allVariablesRadioButton,
                                                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140,
                                                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                            .add(variablesPanelLayout.createSequentialGroup()
                                                                                                                                                     .add(selectedVariablesRadioButton,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          192,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(28, 28, 28)
                                                                                                                                                     .add(selectDeselectCheckBox,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          251,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                                                                            .add(variablesPanelLayout.createParallelGroup(
                                                                                                                                                             org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                                                     .add(variablesPanelLayout.createSequentialGroup()
                                                                                                                                                                              .add(variablesPanelLayout.createParallelGroup(
                                                                                                                                                                                                               org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                                                                                                       .add(discretizeLabel)
                                                                                                                                                                                                       .add(missingValuesLabel))
                                                                                                                                                                              .add(20, 20, 20)
                                                                                                                                                                              .add(variablesPanelLayout
                                                                                                                                                                                           .createParallelGroup(
                                                                                                                                                                                                   org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                                                                                           .add(discretizeComboBox,
                                                                                                                                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                278,
                                                                                                                                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                           .add(missingValuesComboBox,
                                                                                                                                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                278,
                                                                                                                                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                                                                                                                              .add(50, 50, 50)
                                                                                                                                                                              .add(variablesPanelLayout
                                                                                                                                                                                           .createParallelGroup(
                                                                                                                                                                                                   org.jdesktop.layout.GroupLayout.LEADING,
                                                                                                                                                                                                   false)
                                                                                                                                                                                           .add(numIntervalsCheckBox)
                                                                                                                                                                                           .add(variablesPanelLayout
                                                                                                                                                                                                        .createSequentialGroup()
                                                                                                                                                                                                        .add(5, 5, 5)
                                                                                                                                                                                                        .add(intervalsLabel)
                                                                                                                                                                                                        .add(10, 10, 10)
                                                                                                                                                                                                        .add(numIntervalsSpinner,
                                                                                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                             48,
                                                                                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                                                                                                                            .add(variablesPanelLayout.createSequentialGroup()
                                                                                                                                                     .add(20, 20, 20)
                                                                                                                                                     .add(preprocessVariablesLabel,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          102,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(120, 120, 120)
                                                                                                                                                     .add(missingValuesVariableLabel,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          90,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(120, 120, 120)
                                                                                                                                                     .add(discretizeVariablesLabel,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          99,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(20, 20, 20)
                                                                                                                                                     .add(intervalVariablesLabel,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          121,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(21, 21, 21))
                                                                                                                            .add(22, 22, 22)))
                                                                                      .add(variablesPanelLayout.createSequentialGroup()
                                                                                                               .add(jScrollVariables, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 700,
                                                                                                                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                               .add(0, 0, Short.MAX_VALUE)))
                                                             .addContainerGap()));
        variablesPanelLayout.setVerticalGroup(
                variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, variablesPanelLayout.createSequentialGroup()
                                                                                                       .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                                       .add(variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                                .add(org.jdesktop.layout.GroupLayout.TRAILING,
                                                                                                                                     variablesPanelLayout.createSequentialGroup()
                                                                                                                                                         .add(variablesPanelLayout
                                                                                                                                                                      .createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                                                      .add(discretizeComboBox,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                      .add(discretizeLabel))
                                                                                                                                                         
                                                                                                                                                         .add(variablesPanelLayout
                                                                                                                                                                      .createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                                                      .add(missingValuesLabel)
                                                                                                                                                                      .add(missingValuesComboBox,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                      .add(numIntervalsSpinner,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                                                                                                                .add(org.jdesktop.layout.GroupLayout.TRAILING,
                                                                                                                                     variablesPanelLayout.createSequentialGroup()
                                                                                                                                                         .add(numIntervalsCheckBox)
                                                                                                                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                                                                         .add(intervalsLabel)))
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                       .add(allVariablesRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                       .add(variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                .add(selectedVariablesRadioButton,
                                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .add(selectDeselectCheckBox))
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                       .add(variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                .add(missingValuesVariableLabel)
                                                                                                                                .add(preprocessVariablesLabel)
                                                                                                                                .add(intervalVariablesLabel)
                                                                                                                                .add(discretizeVariablesLabel))
                                                                                                       .add(5, 5, 5)
                                                                                                       .add(jScrollVariables, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180,
                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                       .add(52, 52, 52)));
        // panel whit resset and save button
        resetButton = new JButton("Reset values");
        resetButton.addActionListener(e -> resetButtonActionPerformed());
        resetButton.setEnabled(false);
        
        savePreprocessSetButton = new JButton("Save dataset");
        savePreprocessSetButton.setEnabled(false);
        savePreprocessSetButton.addActionListener(e -> {
            try {
                savePreprocessSetButtonActionPerformed();
            } catch (IOException | NoReaderForExtension ex) {
                throw new UnrecoverableException(ex);
            }
        });
        
        //tabbedPane.addTab("Preprocess variables", variablesPanel);

        JPanel classVariablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        classVariablePanel.add(new JLabel("Class variable (for MDLP / ChiMerge):"));
        classVariablePanel.add(classVariableComboBox);
        classVariablePanel.add(Box.createHorizontalStrut(20));
        classVariablePanel.add(new JLabel("Outliers:"));
        classVariablePanel.add(outliersComboBox);
        classVariablePanel.add(Box.createHorizontalStrut(20));
        classVariablePanel.add(new JLabel("Feature selection:"));
        classVariablePanel.add(featureSelectionComboBox);
        classVariablePanel.add(new JLabel("top-k:"));
        classVariablePanel.add(featureSelectionTopKSpinner);
        mainPanel.add(classVariablePanel);

        mainPanel.add(variablesPanel);
        
        cancelButton = DialogBase.generateGenericCancelButton();
        cancelButton.setText("Close");
        this.add(mainPanel);
        
        // ok_button disactived. To be enable its necesary select the database
        resetButton.setEnabled(false);
        addButtonToButtonsPanel(savePreprocessSetButton);
        addButtonToButtonsPanel(resetButton);
        setCancelButton(cancelButton);
        
        this.pack();
        setSize(new Dimension(740, 510));
    }
    
    /**
     * Listener for que missingValues ComboBox
     */
    private void missingValuesComboBoxActionPerformed() {
        //GEN-FIRST:event_ausentValuesComboBoxActionPerformed
        String selected = (String) missingValuesComboBox.getSelectedItem();
        
        for (Component component : missingValuesPanel.getComponents()) {
            if (missingValuesComboBox.getSelectedIndex() == 0)
                component.setEnabled(true);
            else {
                ((JComboBox<String>) component).setSelectedItem(selected);
                component.setEnabled(false);
            }
        }
    }
    
    /**
     * Listener for que discretize ComboBox
     */
    private void discretizeComboBoxActionPerformed() {
        //GEN-FIRST:event_discretizeComboBoxActionPerformed
        if (discretizeComboBox.getSelectedIndex() == -1) {
            return;
        }
        
        GeneralMode<Discretization.Option> selected = (GeneralMode<Discretization.Option>) discretizeComboBox.getSelectedItem();
        
        int i = 0;
        for (Component component : discretizePanel.getComponents()) {
            JComboBox<GeneralMode<Discretization.Option>> variableDiscretizeComboBox = (JComboBox<GeneralMode<Discretization.Option>>) component;
            variableDiscretizeComboBox.setEnabled(selected.isSpecifyEach());
            if (!selected.isSpecifyEach()) {
                Discretization.Option item = (isNumeric[i]) ? selected.commonValueToSet() : Discretization.Option.NONE;
                variableDiscretizeComboBox.setSelectedItem(item);
            }
            variableDiscretizeComboBox.setEnabled(isNumeric[i] && selected.isSpecifyEach());
            ++i;
        }
    }
    
    /**
     * Listener for que numIntervalsCheckBox
     */
    private void numIntervalsCheckBoxActionPerformed() {//GEN-FIRST:event_numIntervalsCheckBoxActionPerformed
        int i = 0;
        
        if (numIntervalsCheckBox.isSelected()) {
            numIntervalsSpinner.setEnabled(true);
            Integer selected = (Integer) numIntervalsSpinner.getValue();
            
            for (Component component : numIntervalsPanel.getComponents()) {
                ((JSpinner) component).setValue(selected);
                component.setEnabled(false);
            }
        } else {
            numIntervalsSpinner.setEnabled(false);
            for (Component component : numIntervalsPanel.getComponents()) {
                if (isNumeric[i])
                    component.setEnabled(true);
                i++;
            }
        }
    }//GEN-LAST:event_numIntervalsCheckBoxActionPerformed
    
    /**
     * Listener for que numIntervalsSpinner
     */
    private void numIntervalsSpinnerStateChanged(
            ChangeEvent evt) {//GEN-FIRST:event_numIntervalsSpinnerStateChanged
        Integer selected = (Integer) numIntervalsSpinner.getValue();
        
        for (Component component : numIntervalsPanel.getComponents()) {
            ((JSpinner) component).setValue(selected);
        }
    }//GEN-LAST:event_numIntervalsSpinnerStateChanged
    
    /**
     * Listener for que allVariablesRadioButton
     */
    private void allVariablesRadioButtonActionPerformed() {//GEN-FIRST:event_allVariablesRadioButtonActionPerformed
        selectDeselectCheckBox.setSelected(false);
        selectDeselectCheckBox.setEnabled(false);
        for (Component comp : varSelectionPanel.getComponents()) {
            ((JCheckBox) comp).setSelected(true);
            comp.setEnabled(false);
        }
    }//GEN-LAST:event_allVariablesRadioButtonActionPerformed
    
    /**
     * Listener for que selectedVariablesRadioButton
     */
    private void selectedVariablesRadioButtonActionPerformed() {//GEN-FIRST:event_selectedVariablesRadioButtonActionPerformed
        selectDeselectCheckBox.setEnabled(true);
        for (Component comp : varSelectionPanel.getComponents()) {
            comp.setEnabled(true);
        }
    }//GEN-LAST:event_selectedVariablesRadioButtonActionPerformed
    
    /**
     * Listener for que selectDeselectCheckBox
     */
    private void selectDeselectCheckBoxActionPerformed() {//GEN-FIRST:event_selectDeselectCheckBoxActionPerformed
        boolean value = selectDeselectCheckBox.isSelected();
        for (Component comp : varSelectionPanel.getComponents()) {
            ((JCheckBox) comp).setSelected(value);
        }
    }//GEN-LAST:event_selectDeselectCheckBoxActionPerformed
    
    /**
     * this method save the preprocess complete set in the same
     * path of the caseDatabase file. Its the listener savePreprocessSetButton
     */
    private void savePreprocessSetButtonActionPerformed() throws IOException, NoReaderForExtension {
        if (!validateBeforeSave()) return;
        CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
        String extension = FilenameUtils.getExtension(this.dbOpenerPanel.getDatabaseFile().getName());
        String name = FilenameUtils.getBaseName(this.dbOpenerPanel.getDatabaseFile().getName());
        
        this.saveWritter.setSelectedFile(new File(this.dbOpenerPanel.getDatabaseFile()
                                                                    .getParentFile(), name + " - Preprocessed." + extension));
        if (this.saveWritter.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        var preprocessedDBFile = this.saveWritter.getSelectedFile();
        caseDbManager.getWriter(FilenameUtils.getExtension(preprocessedDBFile.getName()))
                     .save(preprocessedDBFile, getPreprocessDataBase());
        
        // save the the preprocesed datapath
        JOptionPane.showMessageDialog(null,
                                      "The preprocessed dataset have been saved.",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Listener for que resetButton
     */
    private void resetButtonActionPerformed() {
        discretizeComboBox.setSelectedIndex(0);
        missingValuesComboBox.setSelectedIndex(0);
        outliersComboBox.setSelectedItem(Outliers.Option.NONE);
        featureSelectionComboBox.setSelectedItem(FeatureSelection.Method.NONE);
        featureSelectionTopKSpinner.setValue(5);
        numIntervalsCheckBox.setSelected(false);
        allVariablesRadioButton.setSelected(true);
        numIntervalsSpinner.setValue(2);
        numIntervalsSpinner.setEnabled(false);
        updateVariableSelectionPanel();
    }
    
    /**
     * Listener for que selectDeselectCheckBox
     */
    private void updateVariableSelectionPanel() {
        CaseDatabase database = this.dbOpenerPanel.getDatabase();
        if (database == null) {
            return;
        }
        varSelectionPanel.removeAll();
        missingValuesPanel.removeAll();
        discretizePanel.removeAll();
        numIntervalsPanel.removeAll();
        classVariableComboBox.removeAllItems();
        classVariableComboBox.addItem("(none)");
        for (Variable v : database.getVariables()) {
            classVariableComboBox.addItem(v.getName());
        }
        classVariableComboBox.setEnabled(true);
        int i = 0;
        allVariablesRadioButton.setEnabled(true);
        selectedVariablesRadioButton.setEnabled(true);
        isNumeric = new boolean[database.getVariables().size()];
        for (Variable variable : database.getVariables()) {
            JComboBox<String> preprocessOptions = new JComboBox<String>();
            preprocessOptions.addItem("Keep records with missing values");
            preprocessOptions.addItem("Erase records with missing values");
            preprocessOptions.addItem("Impute missing values with the mode (most frequent state)");
            preprocessOptions.addItem("Impute missing values with the mean (numeric variables)");
            preprocessOptions.addItem("Impute missing values with the median (numeric variables)");
            preprocessOptions.addItem("Impute missing values with k-Nearest Neighbours (k=5)");
            preprocessOptions.setSelectedItem(0);
            preprocessOptions.setPreferredSize(new Dimension(225, 18));
            JCheckBox varSelect = new JCheckBox(variable.getName());
            varSelect.setPreferredSize(new Dimension(175, 18));
            varSelect.setEnabled(false);
            boolean select = true;
            varSelect.setSelected(select);
            JComboBox<Discretization.Option> discretizeOptions = new JComboBox<>(new DefaultComboBoxModel<>());
            discretizeOptions.setRenderer(new JComboBoxFunctionRender<Discretization.Option>(Localizable::localize));
            discretizeOptions.addItem(Discretization.Option.NONE);
            discretizeOptions.addItem(Discretization.Option.EQUAL_FREQ);
            discretizeOptions.addItem(Discretization.Option.EQUAL_WIDTH);
            discretizeOptions.addItem(Discretization.Option.MDLP);
            discretizeOptions.addItem(Discretization.Option.CHIMERGE);
            discretizeOptions.addItem(Discretization.Option.KMEANS);

            discretizeOptions.setSelectedIndex(0);
            discretizeOptions.setPreferredSize(new Dimension(175, 18));
            discretizeOptions.addItemListener(new ItemListener() {
                @SuppressWarnings("unchecked") @Override public void itemStateChanged(ItemEvent arg0) {
                    int i = 0;
                    for (Component comboBox : discretizePanel.getComponents()) {
                        if (comboBox.equals(arg0.getSource())) {
                            numIntervalsPanel.getComponent(i).setEnabled(
                                    isNumeric[i] && ((JComboBox<String>) comboBox).getSelectedIndex() > 0
                                            && !numIntervalsCheckBox.isSelected());
                        }
                        ++i;
                    }
                }
            });
            JSpinner numIntervals = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
            numIntervals.setPreferredSize(new Dimension(50, 18));
            isNumeric[i] = Discretization.isNumeric(variable);
            discretizeOptions.setEnabled(isNumeric[i] && ((GeneralMode<Discretization.Option>) discretizeComboBox.getSelectedItem()).isSpecifyEach());
            
            numIntervals.setEnabled(isNumeric[i] && discretizeOptions.getSelectedIndex() > 1);
            
            varSelectionPanel.add(varSelect);
            missingValuesPanel.add(preprocessOptions);
            discretizePanel.add(discretizeOptions);
            numIntervalsPanel.add(numIntervals);
            i++;
        }
        
        varSelectionPanel.revalidate();
        missingValuesPanel.revalidate();
        discretizePanel.revalidate();
        numIntervalsPanel.revalidate();
    }
    
    private List<Variable> getSelectedVariables() {
        List<Variable> variables = new ArrayList<>();
        
        for (Component comp : varSelectionPanel.getComponents()) {
            if (((JCheckBox) comp).isSelected()) {
                variables.add(this.dbOpenerPanel.getDatabase().getVariable(((JCheckBox) comp).getText()));
            }
        }
        return variables;
    }
    
    private Map<String, Integer> getSelectedNumIntervals() {
        Map<String, Integer> selectedNumIntervals = new HashMap<>();
        
        for (int i = 0; i < varSelectionPanel.getComponents().length; ++i) {
            Component comp = varSelectionPanel.getComponents()[i];
            if (((JCheckBox) comp).isSelected()) {
                String variableName = ((JCheckBox) comp).getText();
                int numIntervals = (Integer) ((JSpinner) numIntervalsPanel.getComponents()[i]).getValue();
                selectedNumIntervals.put(variableName, numIntervals);
            }
        }
        return selectedNumIntervals;
    }
    
    private Map<String, Discretization.Option> getSelectedDiscretizeOptions() {

        Map<String, Discretization.Option> selectedDiscretizeOptions = new HashMap<>();
        for (int i = 0; i < varSelectionPanel.getComponents().length; ++i) {
            Component comp = varSelectionPanel.getComponents()[i];
            if (((JCheckBox) comp).isSelected()) {
                String variableName = ((JCheckBox) comp).getText();
                @SuppressWarnings("unchecked")
                Discretization.Option discretizationOption = (Discretization.Option) (
                        (JComboBox<Discretization.Option>) discretizePanel.getComponents()[i]
                ).getSelectedItem();
                selectedDiscretizeOptions.put(variableName, discretizationOption);
            }
        }
        return selectedDiscretizeOptions;
    }

    private Variable getSelectedClassVariable() {
        Object selected = classVariableComboBox.getSelectedItem();
        if (selected == null || "(none)".equals(selected)) return null;
        return this.dbOpenerPanel.getDatabase().getVariable((String) selected);
    }
    
    private Map<String, MissingValues.Option> getSelectedMissingValuesOptions() {
        Map<String, MissingValues.Option> selectedPreprocessOptions = new HashMap<>();
        for (int i = 0; i < varSelectionPanel.getComponents().length; ++i) {
            Component comp = varSelectionPanel.getComponents()[i];
            if (((JCheckBox) comp).isSelected()) {
                String variableName = ((JCheckBox) comp).getText();
                @SuppressWarnings("unchecked") int selectedIndex = (
                        (JComboBox<String>) missingValuesPanel.getComponents()[i]
                ).getSelectedIndex();
                MissingValues.Option missingValuesOption = MissingValues.Option.values()[selectedIndex];
                
                selectedPreprocessOptions.put(variableName, missingValuesOption);
            }
        }
        return selectedPreprocessOptions;
    }
    
    private CaseDatabase getPreprocessDataBase() {
        Map<String, Discretization.Option> discretizeOpts = getSelectedDiscretizeOptions();
        Variable classVariable = getSelectedClassVariable();
        CaseDatabase preprocessedDatabase = FilterDatabase.filter(this.dbOpenerPanel.getDatabase(), getSelectedVariables());
        preprocessedDatabase = Outliers.process(preprocessedDatabase, getSelectedOutliersOptions(preprocessedDatabase));
        preprocessedDatabase = MissingValues.process(preprocessedDatabase, getSelectedMissingValuesOptions());
        preprocessedDatabase = Discretization.process(preprocessedDatabase, discretizeOpts,
                getSelectedNumIntervals(), null, classVariable);
        FeatureSelection.Method fsMethod = (FeatureSelection.Method) featureSelectionComboBox.getSelectedItem();
        if (fsMethod != null && fsMethod != FeatureSelection.Method.NONE) {
            Variable fsClass = (classVariable != null)
                    ? preprocessedDatabase.getVariable(classVariable.getName())
                    : null;
            int topK = (Integer) featureSelectionTopKSpinner.getValue();
            preprocessedDatabase = FeatureSelection.select(preprocessedDatabase, fsClass, fsMethod, topK);
        }
        return preprocessedDatabase;
    }

    private Map<String, Outliers.Option> getSelectedOutliersOptions(CaseDatabase database) {
        Outliers.Option selected = (Outliers.Option) outliersComboBox.getSelectedItem();
        if (selected == null) selected = Outliers.Option.NONE;
        Map<String, Outliers.Option> map = new HashMap<>();
        for (Variable v : database.getVariables()) {
            map.put(v.getName(), selected);
        }
        return map;
    }

    private boolean validateBeforeSave() {
        Map<String, Discretization.Option> opts = getSelectedDiscretizeOptions();
        boolean supervisedUsed = opts.values().stream().anyMatch(
                o -> o == Discretization.Option.MDLP || o == Discretization.Option.CHIMERGE);
        FeatureSelection.Method fsMethod = (FeatureSelection.Method) featureSelectionComboBox.getSelectedItem();
        boolean fsUsed = fsMethod != null && fsMethod != FeatureSelection.Method.NONE;
        if (!supervisedUsed && !fsUsed) return true;
        Variable classVar = getSelectedClassVariable();
        if (classVar == null) {
            JOptionPane.showMessageDialog(this,
                    "Supervised discretization (MDLP / ChiMerge) and feature selection require selecting a class variable.",
                    "Class variable required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Discretization.Option classVarOpt = opts.get(classVar.getName());
        if (classVarOpt == Discretization.Option.MDLP || classVarOpt == Discretization.Option.CHIMERGE) {
            JOptionPane.showMessageDialog(this,
                    "The class variable cannot itself be discretized with a supervised method.",
                    "Invalid class variable", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Gets the Panel with the buttons OK and Cancel
     *
     * @return: JPanel
     */
    private JPanel getOkCancelPanel() {
        JPanel okCancelPanel = new JPanel();
        
        cancelButton = new JButton();
        
        
        // add components to okCancelPanel with a GroupLayout
        GroupLayout layout = new GroupLayout(okCancelPanel);
        okCancelPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(cancelButton, 90, 90, 90)
                                        .addContainerGap());
        layout.setVerticalGroup(layout.createParallelGroup()
                                      .addComponent(cancelButton));
        return okCancelPanel;
    }
    
}