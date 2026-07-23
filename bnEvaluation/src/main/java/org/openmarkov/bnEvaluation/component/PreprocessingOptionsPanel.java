/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.component;

import org.openmarkov.bnEvaluation.DataPreprocessor;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.commonComponents.GeneralMode;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.FeatureSelection;
import org.openmarkov.learning.core.preprocess.MissingValues;
import org.openmarkov.learning.core.preprocess.Outliers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing panel that collects the preprocessing options for {@code DataPreprocessingDialog}: per
 * variable, whether to keep it and how to handle its missing values, discretization and number of
 * intervals; and, globally, the class variable, outlier handling and feature selection. Extracted
 * from the 788-line dialog so this view code lives in one focused component and the dialog stays
 * thin (model-view-controller separation, phase F6). It turns the current control state into a
 * {@link DataPreprocessor.Request} through {@link #buildRequest()}.
 *
 * @author Manuel Arias
 */
public final class PreprocessingOptionsPanel extends JPanel {

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

    /** The database whose variables are shown; set by {@link #updateForDatabase(CaseDatabase)}. */
    private CaseDatabase database;

    public PreprocessingOptionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel variablesPanel = new JPanel();

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

        // Two rows so the four global options fit without being cut off on the right.
        JPanel classAndOutliersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        classAndOutliersRow.add(new JLabel("Class variable (for MDLP / ChiMerge):"));
        classAndOutliersRow.add(classVariableComboBox);
        classAndOutliersRow.add(Box.createHorizontalStrut(20));
        classAndOutliersRow.add(new JLabel("Outliers:"));
        classAndOutliersRow.add(outliersComboBox);

        JPanel featureSelectionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        featureSelectionRow.add(new JLabel("Feature selection:"));
        featureSelectionRow.add(featureSelectionComboBox);
        featureSelectionRow.add(new JLabel("top-k:"));
        featureSelectionRow.add(featureSelectionTopKSpinner);

        JPanel globalOptionsPanel = new JPanel();
        globalOptionsPanel.setLayout(new BoxLayout(globalOptionsPanel, BoxLayout.PAGE_AXIS));
        classAndOutliersRow.setAlignmentX(LEFT_ALIGNMENT);
        featureSelectionRow.setAlignmentX(LEFT_ALIGNMENT);
        globalOptionsPanel.add(classAndOutliersRow);
        globalOptionsPanel.add(featureSelectionRow);

        globalOptionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        variablesPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(globalOptionsPanel);
        add(variablesPanel);
    }

    /**
     * Shows the variables of {@code database} and resets the per-variable rows for it. A
     * {@code null} database leaves the panel empty.
     */
    public void updateForDatabase(CaseDatabase database) {
        this.database = database;
        rebuildVariableRows();
    }

    /** Restores the default option selections and rebuilds the per-variable rows. */
    public void reset() {
        discretizeComboBox.setSelectedIndex(0);
        missingValuesComboBox.setSelectedIndex(0);
        outliersComboBox.setSelectedItem(Outliers.Option.NONE);
        featureSelectionComboBox.setSelectedItem(FeatureSelection.Method.NONE);
        featureSelectionTopKSpinner.setValue(5);
        numIntervalsCheckBox.setSelected(false);
        allVariablesRadioButton.setSelected(true);
        numIntervalsSpinner.setValue(2);
        numIntervalsSpinner.setEnabled(false);
        rebuildVariableRows();
    }

    /**
     * Reads the current state of the controls into a {@link DataPreprocessor.Request}, the sole
     * point where this view talks to the pure preprocessing service.
     */
    public DataPreprocessor.Request buildRequest() {
        Outliers.Option outliers = (Outliers.Option) outliersComboBox.getSelectedItem();
        if (outliers == null) outliers = Outliers.Option.NONE;
        FeatureSelection.Method fsMethod = (FeatureSelection.Method) featureSelectionComboBox.getSelectedItem();
        if (fsMethod == null) fsMethod = FeatureSelection.Method.NONE;
        int topK = (Integer) featureSelectionTopKSpinner.getValue();
        return new DataPreprocessor.Request(database, getSelectedVariables(),
                getSelectedMissingValuesOptions(), outliers, getSelectedDiscretizeOptions(),
                getSelectedNumIntervals(), getSelectedClassVariable(), fsMethod, topK);
    }

    /** Listener for the general missing-values combo box. */
    private void missingValuesComboBoxActionPerformed() {
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

    /** Listener for the general discretize combo box. */
    private void discretizeComboBoxActionPerformed() {
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

    /** Listener for the "same number of intervals" check box. */
    private void numIntervalsCheckBoxActionPerformed() {
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
    }

    /** Listener for the "all variables" radio button. */
    private void allVariablesRadioButtonActionPerformed() {
        selectDeselectCheckBox.setSelected(false);
        selectDeselectCheckBox.setEnabled(false);
        for (Component comp : varSelectionPanel.getComponents()) {
            ((JCheckBox) comp).setSelected(true);
            comp.setEnabled(false);
        }
    }

    /** Listener for the "selected variables" radio button. */
    private void selectedVariablesRadioButtonActionPerformed() {
        selectDeselectCheckBox.setEnabled(true);
        for (Component comp : varSelectionPanel.getComponents()) {
            comp.setEnabled(true);
        }
    }

    /** Listener for the "select/unselect all variables" check box. */
    private void selectDeselectCheckBoxActionPerformed() {
        boolean value = selectDeselectCheckBox.isSelected();
        for (Component comp : varSelectionPanel.getComponents()) {
            ((JCheckBox) comp).setSelected(value);
        }
    }

    /** Rebuilds one row per variable of {@link #database} across the four aligned panels. */
    private void rebuildVariableRows() {
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
            preprocessOptions.setSelectedIndex(0);
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
                variables.add(database.getVariable(((JCheckBox) comp).getText()));
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
        return database.getVariable((String) selected);
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
}
