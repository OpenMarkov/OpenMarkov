/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/*
 * LearningGUI.java
 *
 * Created on 5 de junio de 2008, 19:59
 */

package org.openmarkov.learning.gui;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.action.AutoArrangeEdit;
import org.openmarkov.gui.dialog.io.DBReaderOMFileChooser;
import org.openmarkov.gui.dialog.io.DBWriterOMFileChooser;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.dialog.io.NetworkOMFileChooser;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.layout.bayesian.StressLayout;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.java.function.PriorityComparator;
import org.openmarkov.learning.algorithm.naivebayes.NaiveBayesAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.fanb.ForestAugmentedNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.kdb.KDBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.snb.SelectiveNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.spnb.SuperParentNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.treeaugmentednb.TreeAugmentedNBAlgorithm;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmManager;
import org.openmarkov.learning.core.exception.EmptyModelNetException;
import org.openmarkov.learning.core.exception.UnobservedVariablesException;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.MissingValues;
import org.openmarkov.learning.core.util.ModelNetUse;
import org.openmarkov.learning.exception.FormatNotSupportedException;
import org.openmarkov.learning.exception.LearningException;
import org.openmarkov.learning.gui.interactive.InteractiveLearningDialog;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GUI to the learning option
 *
 * @author joliva
 * @author Manuel Arias
 * @author fjdiez
 * @author ibermejo
 * @version 1.1
 * @since OpenMarkov 1.0
 */
public class LearningDialog extends JDialog {

    private String databasePath = null;
    private String databaseName = null;
    private CaseDatabase database;
    private boolean[] isNumeric;
    private ProbNet modelNet;
    private AlgorithmParametersDialog optionsGUI;
    private Class<? extends LearningAlgorithm> selectedAlgorithm;
    private String selectedClassVariable;

    /**
     * Directories last used by the database / model-net file choosers. Static so
     * that they persist across {@code LearningDialog} instances within a single
     * program run (no on-disk persistence).
     */
    private static File lastDatabaseDirectory;
    private static File lastModelNetDirectory;

    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    private final JRadioButton allVariablesRadioButton;
    private final JFileChooser databaseFileChooser;
    private final JComboBox<String> discretizeComboBox;
    private final JPanel discretizePanel;
    private final JButton learnButton;
    private final JComboBox<String> missingValuesComboBox;
    private final JPanel missingValuesPanel;
    private final NetworkOMFileChooser modelNetFileChooser;
    private final JRadioButton modelNetVariablesRadioButton;
    private final JPanel numIntervalsPanel;
    private final JSpinner numIntervalsSpinner;
    private final JCheckBox selectDeselectCheckBox;
    private final JRadioButton selectedVariablesRadioButton;
    private final JPanel varSelectionPanel;
    private final JCheckBox numIntervalsCheckBox;

    private final List<JCheckBox> varSelectionCheckBoxes = new ArrayList<>();
    private final List<JComboBox<String>> missingValuesComboBoxes = new ArrayList<>();
    private final List<JComboBox<String>> discretizeComboBoxes = new ArrayList<>();
    private final List<JSpinner> numIntervalsSpinners = new ArrayList<>();


    private final JFrame parent;
    private final AlgorithmConfigurationManager algorithmConfigurationManager;
    private final CaseDatabaseManager caseDbManager;

    private final JTabbedPane generalTabbedPane;
    private final GeneralTabPanel generalTabPanel;
    private final ModelNetTabPanel modelNetTabPanel;

    public LearningDialog(JFrame parent) {
        super(parent, true);
        this.parent = parent;
        algorithmConfigurationManager = new AlgorithmConfigurationManager(parent);
        caseDbManager = new CaseDatabaseManager();

        databaseFileChooser = new DBReaderOMFileChooser(true); // COMMON_DB_READER_CHOOSER;
        modelNetFileChooser = new NetworkOMFileChooser(); // COMMON_NETWORK_FILE_CHOOSER;
        if (lastDatabaseDirectory != null) {
            databaseFileChooser.setCurrentDirectory(lastDatabaseDirectory);
        }
        if (lastModelNetDirectory != null) {
            modelNetFileChooser.setCurrentDirectory(lastModelNetDirectory);
        }
        generalTabbedPane = new JTabbedPane();

        // --- Build GeneralTabPanel ---
        generalTabPanel = new GeneralTabPanel(stringDatabase);

        // Wire listeners for generalTabPanel
        generalTabPanel.loadCaseFileButton.addActionListener(this::loadCaseFileButtonActionPerformed);

        generalTabPanel.generativeAlgorithmType.addActionListener(evt -> setAlgorithmDefaults());
        generalTabPanel.discriminativeAlgorithmType.addActionListener(evt -> setAlgorithmDefaults());

        setAlgorithmDefaults();
        generalTabPanel.algorithmComboBox.addActionListener(this::algorithmComboBoxActionPerformed);

        selectedAlgorithm = (Class<? extends LearningAlgorithm>) generalTabPanel.algorithmComboBox.getSelectedItem();
        optionsGUI = algorithmConfigurationManager.instanciateForAlgorithm(selectedAlgorithm);
        generalTabPanel.optionsButton.setEnabled(optionsGUI != null);
        generalTabPanel.optionsTextArea.setText((optionsGUI != null) ? optionsGUI.getDescription() : "");

        generalTabPanel.optionsButton.addActionListener(this::optionsButtonActionPerformed);

        generalTabbedPane.addTab(stringDatabase.getString("Learning.General"), generalTabPanel);

        // --- Build ModelNetTabPanel ---
        modelNetTabPanel = new ModelNetTabPanel(stringDatabase);

        // Wire listeners for modelNetTabPanel
        modelNetTabPanel.noModelNetRadioButton.addActionListener(this::noModelNetRadioButtonActionPerformed);
        modelNetTabPanel.fromFileRadioButton.addActionListener(this::fromFileRadioButtonActionPerformed);
        modelNetTabPanel.fromOpenMarkovRadioButton.addActionListener(this::fromOpenMarkovRadioButtonActionPerformed);
        modelNetTabPanel.loadModelNetButton.addActionListener(evt -> {
            try {
                loadModelNetButtonActionPerformed(evt);
            } catch (ProbNetParserException | IOException | NoReaderForFileException |
                     CorruptNetworkFile e) {
                throw new UnrecoverableException(e);
            }
        });
        modelNetTabPanel.startFromModelNetCheckBox.addActionListener(this::startFromModelNetCheckBoxActionPerformed);

        // Button groups for model net radio buttons
        ButtonGroup modelNetButtonGroup = new ButtonGroup();
        modelNetButtonGroup.add(modelNetTabPanel.fromFileRadioButton);
        modelNetButtonGroup.add(modelNetTabPanel.fromOpenMarkovRadioButton);
        modelNetButtonGroup.add(modelNetTabPanel.noModelNetRadioButton);

        modelNetTabPanel.fromOpenMarkovRadioButton.setEnabled(MainGUI.INSTANCE.mainPanel.
                                                     getMainPanelListenerAssistant().
                                                     getCurrentNetworkEditorPanel() != null);

        generalTabbedPane.addTab(stringDatabase.getString("Learning.ModelNet"), modelNetTabPanel);

        // --- Variables panel ---
        JPanel variablesPanel = new JPanel();
        JScrollPane jScrollPane4 = new JScrollPane();
        JPanel showVariablesPanel = new JPanel();
        missingValuesPanel = new JPanel();
        discretizePanel = new JPanel();
        numIntervalsPanel = new JPanel();
        varSelectionPanel = new JPanel();
        missingValuesComboBox = new JComboBox<>();
        JLabel jLabel6 = new JLabel();
        JLabel jLabel8 = new JLabel();
        discretizeComboBox = new JComboBox<>();
        JLabel jLabel3 = new JLabel();
        JLabel jLabel4 = new JLabel();
        JLabel jLabel5 = new JLabel();
        JLabel jLabel9 = new JLabel();
        numIntervalsSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
        JLabel jLabel10 = new JLabel();
        numIntervalsCheckBox = new JCheckBox();
        allVariablesRadioButton = new JRadioButton();
        selectedVariablesRadioButton = new JRadioButton();
        modelNetVariablesRadioButton = new JRadioButton();
        selectDeselectCheckBox = new JCheckBox();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(stringDatabase.getString("Learning.Title"));

        jScrollPane4.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        showVariablesPanel.setLayout(new GridBagLayout());

        org.jdesktop.layout.GroupLayout missingValuesPanelLayout = new org.jdesktop.layout.GroupLayout(
                missingValuesPanel);
        missingValuesPanel.setLayout(missingValuesPanelLayout);
        missingValuesPanelLayout.setHorizontalGroup(
                missingValuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(0, 200, Short.MAX_VALUE));
        missingValuesPanelLayout.setVerticalGroup(
                missingValuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(0, 232, Short.MAX_VALUE));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 8);
        showVariablesPanel.add(missingValuesPanel, gridBagConstraints);

        org.jdesktop.layout.GroupLayout discretizePanelLayout = new org.jdesktop.layout.GroupLayout(discretizePanel);
        discretizePanel.setLayout(discretizePanelLayout);
        discretizePanelLayout.setHorizontalGroup(
                discretizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(0, 150, Short.MAX_VALUE));
        discretizePanelLayout.setVerticalGroup(
                discretizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(0, 232, Short.MAX_VALUE));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 8);
        showVariablesPanel.add(discretizePanel, gridBagConstraints);

        org.jdesktop.layout.GroupLayout numIntervalsPanelLayout = new org.jdesktop.layout.GroupLayout(
                numIntervalsPanel);
        numIntervalsPanel.setLayout(numIntervalsPanelLayout);
        numIntervalsPanelLayout.setHorizontalGroup(
                numIntervalsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                       .add(0, 50, Short.MAX_VALUE));
        numIntervalsPanelLayout.setVerticalGroup(
                numIntervalsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                       .add(0, 232, Short.MAX_VALUE));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        showVariablesPanel.add(numIntervalsPanel, gridBagConstraints);

        org.jdesktop.layout.GroupLayout varSelectionPanelLayout = new org.jdesktop.layout.GroupLayout(
                varSelectionPanel);
        varSelectionPanel.setLayout(varSelectionPanelLayout);
        varSelectionPanelLayout.setHorizontalGroup(
                varSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                       .add(0, 150, Short.MAX_VALUE));
        varSelectionPanelLayout.setVerticalGroup(
                varSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                       .add(0, 232, Short.MAX_VALUE));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 0, 4, 8);
        showVariablesPanel.add(varSelectionPanel, gridBagConstraints);

        jScrollPane4.setViewportView(showVariablesPanel);

        //ausentValuesComboBox.setModel(new javax.swing.DefaultComboBoxModel(AbsentValues.getOptions()));
        missingValuesComboBox.addItem(stringDatabase.getString("Learning.DefaultOption"));
        missingValuesComboBox.addItem(stringDatabase.getString("Learning.MissingValues.KeepMissing"));
        missingValuesComboBox.addItem(stringDatabase.getString("Learning.MissingValues.Eliminate"));
        //ausentValuesComboBox.insertItemAt(AbsentValues.defaultOption, 0);
        missingValuesComboBox.setSelectedItem(0);
        missingValuesComboBox.setPreferredSize(new Dimension(27, 20));
        missingValuesComboBox.addActionListener(this::missingValuesComboBoxActionPerformed);

        jLabel6.setText(stringDatabase.getString("Learning.TreatAbsentValues"));

        jLabel8.setText(stringDatabase.getString("Learning.Discretize"));

        //discretizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(Discretization.getOptions()));
        discretizeComboBox.addItem(stringDatabase.getString("Learning.DefaultOption"));
        discretizeComboBox.addItem(stringDatabase.getString("Learning.Discretize.NoDiscretize"));
        discretizeComboBox.addItem(stringDatabase.getString("Learning.Discretize.SameFreq"));
        discretizeComboBox.addItem(stringDatabase.getString("Learning.Discretize.SameWidth"));
        discretizeComboBox.setPreferredSize(new Dimension(27, 20));
        discretizeComboBox.setSelectedItem(0);
        discretizeComboBox.addActionListener(this::discretizeComboBoxActionPerformed);

        jLabel3.setText(stringDatabase.getString("Learning.Preprocessing"));

        jLabel4.setText(stringDatabase.getString("Learning.AbsentValues"));
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);

        jLabel5.setText(stringDatabase.getString("Learning.Discretization"));
        jLabel5.setHorizontalAlignment(SwingConstants.CENTER);

        jLabel9.setText(stringDatabase.getString("Learning.IntervalCount"));
        jLabel9.setHorizontalAlignment(SwingConstants.CENTER);

        numIntervalsSpinner.setEnabled(false);
        numIntervalsSpinner.addChangeListener(this::numIntervalsSpinnerStateChanged);

        jLabel10.setText(stringDatabase.getString("Learning.Intervals"));

        numIntervalsCheckBox.setText(stringDatabase.getString("Learning.SameIntervalNumber"));
        numIntervalsCheckBox.setContentAreaFilled(false);
        numIntervalsCheckBox.setMargin(new Insets(2, 2, 2, 0));
        numIntervalsCheckBox.addActionListener(this::numIntervalsCheckBoxActionPerformed);

        allVariablesRadioButton.setSelected(true);
        allVariablesRadioButton.setText(stringDatabase.getString("Learning.UseAllVariables"));
        allVariablesRadioButton.setEnabled(false);
        allVariablesRadioButton.setPreferredSize(new Dimension(93, 20));
        allVariablesRadioButton.addActionListener(this::allVariablesRadioButtonActionPerformed);

        selectedVariablesRadioButton.setText(stringDatabase.getString("Learning.UseSelectedVariables"));
        selectedVariablesRadioButton.setEnabled(false);
        selectedVariablesRadioButton.setPreferredSize(new Dimension(93, 20));
        selectedVariablesRadioButton.addActionListener(this::selectedVariablesRadioButtonActionPerformed);

        modelNetVariablesRadioButton.setText(stringDatabase.getString("Learning.UseModelNetVariables"));
        modelNetVariablesRadioButton.setEnabled(false);
        modelNetVariablesRadioButton.addActionListener(this::modelNetVariablesRadioButtonActionPerformed);

        selectDeselectCheckBox.setText(stringDatabase.getString("Learning.SelectAllVariables"));
        selectDeselectCheckBox.setEnabled(false);
        selectDeselectCheckBox.addActionListener(this::selectDeselectCheckBoxActionPerformed);

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
                                                                                                                                                                                                       .add(jLabel8)
                                                                                                                                                                                                       .add(jLabel6))
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
                                                                                                                                                                                                        .add(jLabel10)
                                                                                                                                                                                                        .add(10, 10, 10)
                                                                                                                                                                                                        .add(numIntervalsSpinner,
                                                                                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                             48,
                                                                                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                                                                                                                            .add(variablesPanelLayout.createSequentialGroup()
                                                                                                                                                     .add(21, 21, 21)
                                                                                                                                                     .add(jLabel3,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          102,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(120, 120, 120)
                                                                                                                                                     .add(jLabel4,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          90,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(120, 120, 120)
                                                                                                                                                     .add(jLabel5,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          99,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(30, 30, 30)
                                                                                                                                                     .add(jLabel9,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                          121,
                                                                                                                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .add(21, 21, 21))
                                                                                                                            .add(22, 22, 22)))
                                                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
                                                                                           variablesPanelLayout.createSequentialGroup()
                                                                                                               .add(10, 10, 10)
                                                                                                               .add(modelNetVariablesRadioButton,
                                                                                                                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                    Short.MAX_VALUE))
                                                                                      .add(variablesPanelLayout.createSequentialGroup()
                                                                                                               .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 700,
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
                                                                                                                                                                      .add(jLabel8))
                                                                                                                                                         .add(4, 4, 4)
                                                                                                                                                         .add(variablesPanelLayout
                                                                                                                                                                      .createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                                                      .add(jLabel6)
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
                                                                                                                                                         .add(jLabel10)))
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                       .add(allVariablesRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                                                                       .add(modelNetVariablesRadioButton)
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                       .add(variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                .add(selectedVariablesRadioButton,
                                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .add(selectDeselectCheckBox))
                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                       .add(variablesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                .add(jLabel4)
                                                                                                                                .add(jLabel3)
                                                                                                                                .add(jLabel9)
                                                                                                                                .add(jLabel5))
                                                                                                       .add(5, 5, 5)
                                                                                                       .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180,
                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                       .add(52, 52, 52)));

        //generalTabbedPane.addTab("   " + stringDatabase.getString("Learning.Preprocessing") + "   ", variablesPanel);

        // --- Button panel ---
        learnButton = new JButton();
        JButton cancelButton = new JButton();
        JButton resetButton = new JButton();
        JPanel jPanel5 = new JPanel();

        learnButton.setText(stringDatabase.getString("Learning.LearnNet"));
        learnButton.setMnemonic(KeyEvent.VK_L);
        learnButton.setEnabled(false);
        learnButton.addActionListener(evt -> {
            try {
                learnButtonActionPerformed(evt);
            } catch (LearningException.NoDatabasePresent | LearningException.NoChosenVariables |
                     IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther | NonProjectablePotentialException |
                     NotEvaluableNetworkException.NotApplicableNetwork | ConstraintViolatedException e) {
                throw new UnrecoverableException(e);
            }
        });

        cancelButton.setText(stringDatabase.getString("Learning.Cancel"));
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        resetButton.setText(stringDatabase.getString("Learning.InitialValues"));
        resetButton.setMnemonic(KeyEvent.VK_R);
        resetButton.addActionListener(this::resetButtonActionPerformed);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                      .add(jPanel5Layout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .add(learnButton)
                                                                        .add(14, 14, 14)
                                                                        .add(resetButton)
                                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90,
                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                        .addContainerGap()));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                                                                                                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                                                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                                                                  .add(learnButton)
                                                                                                                                  .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                                                                       org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                                                                  .add(resetButton)
                                                                                                                )
                                                                                                                .addContainerGap()));

        // --- Main layout ---
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup().addContainerGap()
                                                   .add(generalTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE))
                                        .add(layout.createSequentialGroup()
                                                   .add(170, 170, 170)
                                                   .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                   .add(0, 0, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                      .add(layout.createSequentialGroup()
                                                 .add(generalTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                 .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                 .addContainerGap()));



        setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
        varSelectionPanel.setLayout(new GridLayout(0, 1, 0, 20));
        missingValuesPanel.setLayout(new GridLayout(0, 1, 0, 20));
        discretizePanel.setLayout(new GridLayout(0, 1, 0, 20));
        numIntervalsPanel.setLayout(new GridLayout(0, 1, 0, 20));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        ButtonGroup variablesButtonGroup = new ButtonGroup();
        variablesButtonGroup.add(allVariablesRadioButton);
        variablesButtonGroup.add(selectedVariablesRadioButton);
        variablesButtonGroup.add(modelNetVariablesRadioButton);

        modelNetVariablesRadioButton.setEnabled(modelNet != null);
        resetCaseDatabaseFile();
        pack();
        setMinimumSize(new Dimension(620, 522));

        // Initial focus on the "Open" button so the user can pick a database
        // immediately by pressing Enter or Space.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                generalTabPanel.loadCaseFileButton.requestFocusInWindow();
            }
        });
        /*
        if (databasePath != null) {
            try {
                caseFileTextPane.setText(databaseName);
                loadCaseFile(databasePath);
            } catch (IOException | EmptyDatabaseException | ParsingSourceException | NoWriterForExtensionException |
                     RuntimeException e) {
                //Ignored on purpose.
                //If you were to throw this exception, then the Learning tool wouldn't be able to load at all if
                //a previous corrupted database was open.
            }
        }
        */
    }


    private static final List<String> ALLOWED_NET_FORMATS = List.of("pgmx", "xml", "elv");

    /**
     * @return whether a file format is supported or not
     */
    private static boolean isSupportedNetFormat(String path) {
        var fileExtension = FilenameUtils.getExtension(path);
        return LearningDialog.ALLOWED_NET_FORMATS.contains(fileExtension);
    }

    //TODO: These FileChoosers can replace those in the initComponents method to improve perfomance,
    // but first, it is needed to study if they won't alter the current behaviour.
    final DBReaderOMFileChooser COMMON_DB_READER_CHOOSER = new DBReaderOMFileChooser(true);
    final DBWriterOMFileChooser COMMON_DB_WRITER_CHOOSER = new DBWriterOMFileChooser(false);
    final NetworkOMFileChooser COMMON_NETWORK_FILE_CHOOSER = new NetworkOMFileChooser();

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // </editor-fold>//GEN-END:initComponents

    private void loadCaseFileButtonActionPerformed(ActionEvent evt) {
        databaseFileChooser.setDialogTitle(stringDatabase.getString("Learning.OpenDatabase"));
        if (databaseFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        databasePath = databaseFileChooser.getSelectedFile().getAbsolutePath();
        databaseName = databaseFileChooser.getSelectedFile().getName();
        lastDatabaseDirectory = databaseFileChooser.getCurrentDirectory();
        generalTabPanel.caseFileTextPane.setText(databaseName);
        try {
            loadCaseFile(databaseFileChooser.getSelectedFile());
        } catch (ParsingSourceException | NoWriterForExtensionException | IOException | EmptyDatabaseException e) {
            throw new UnrecoverableException(e);
        } finally {
            updateVariableListCombobox();
        }
    }

    /**
     * Depending on the algorithm this method enables/disables the variable combobox
     * that is consumed by the bayesian derived models
     */
    private void updateVariableListCombobox() {
        boolean discriminativeAlgorithm = LearningAlgorithmManager.INSTANCE.getDiscriminativeAlgorithms()
                                                         .collect(Collectors.toSet())
                                                         .contains((Class<? extends LearningAlgorithm>) generalTabPanel.algorithmComboBox.getSelectedItem());
        //jScrollPane1.setVisible(!discriminativeAlgorithm);
        //optionsButton.setVisible(!discriminativeAlgorithm);
        generalTabPanel.variableListCombobox.setEnabled(discriminativeAlgorithm);
        generalTabPanel.variableListCombobox.setVisible(discriminativeAlgorithm);
        generalTabPanel.variableListCombobox.setModel(new DefaultComboBoxModel(getSelectedVariables().toArray()));
        //variableListCombobox.addActionListener(this::algorithmComboBoxActionPerformed);
        generalTabPanel.jPanel3.revalidate();
        generalTabPanel.variableListCombobox.revalidate();
    }


    private void missingValuesComboBoxActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_ausentValuesComboBoxActionPerformed
        String selected = (String) missingValuesComboBox.getSelectedItem();

        for (JComboBox<String> component : missingValuesComboBoxes) {
            if (missingValuesComboBox.getSelectedIndex() == 0)
                component.setEnabled(true);
            else {
                component.setSelectedItem(selected);
                component.setEnabled(false);
            }
        }
    }//GEN-LAST:event_ausentValuesComboBoxActionPerformed

    //GEN-LAST:event_loadCaseFileButtonActionPerformed

    private void loadCaseFile(File file) throws ParsingSourceException, IOException, NoWriterForExtensionException, EmptyDatabaseException {
        if (file == null) {
            learnButton.setEnabled(false);
            return;
        }
        if (databasePath == null) {
            return;
        }
        try {
            // Load the database
            CaseDatabaseReader reader = caseDbManager.getReader(FilenameUtils.getExtension(file.getName()));
            database = reader.load(file);
            updateVariableSelectionPanel();
            learnButton.setEnabled(true);
            numIntervalsCheckBox.setSelected(false);
            numIntervalsSpinner.setEnabled(false);
            modelNetVariablesRadioButton.setEnabled(modelNet != null);
            if (modelNet == null) {
                allVariablesRadioButton.setSelected(true);
            }
        } catch (NoWriterForExtensionException | IOException | ParsingSourceException | EmptyDatabaseException |
                 RuntimeException e) {
            resetCaseDatabaseFile();
            throw e;
        }
    }

    private void resetCaseDatabaseFile() {
        databaseFileChooser.setSelectedFile(null);
        generalTabPanel.caseFileTextPane.setText(null);
        learnButton.setEnabled(false);
        allVariablesRadioButton.setEnabled(false);
        selectedVariablesRadioButton.setEnabled(false);
        modelNetVariablesRadioButton.setEnabled(false);
        selectDeselectCheckBox.setEnabled(false);
    }
    
    private void loadModelNetButtonActionPerformed(ActionEvent evt) throws ProbNetParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        //GEN-FIRST:event_loadModelNetButtonActionPerformed
        if (!modelNetTabPanel.fromFileRadioButton.isSelected()) {
            return;
        }
        File modelNetFilePath = requestNetworkFileToOpen();
        if (modelNetFilePath == null) {
            return;
        }
        var filePath = modelNetFileChooser.getSelectedFile().getAbsolutePath();
        var fileName = modelNetFileChooser.getSelectedFile().getName();
        if (!LearningDialog.isSupportedNetFormat(fileName)) {
            throw new UnrecoverableException(new FormatNotSupportedException(FilenameUtils.getExtension(filePath), LearningDialog.ALLOWED_NET_FORMATS));
        }
        modelNetTabPanel.modelNetTextPane.setText(fileName);
        modelNet = NetsIO.openNetworkFile(filePath).probNet();
        modelNetSelected();
    }//GEN-LAST:event_loadModelNetButtonActionPerformed

    /**
     * It asks the user to choose a file by means of a open-file dialog box.
     *
     * @return complete path of the file, or null if the user selects cancel.
     */
    private File requestNetworkFileToOpen() {
        modelNetFileChooser.setDialogTitle(stringDatabase.getString("OpenNetwork.Title"));
        if (modelNetFileChooser.showOpenDialog(this.parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        lastModelNetDirectory = modelNetFileChooser.getCurrentDirectory();
        return modelNetFileChooser.getSelectedFile();
    }

    private void discretizeComboBoxActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_discretizeComboBoxActionPerformed
        String selected = (String) discretizeComboBox.getSelectedItem();

        for (int i = 0; i < discretizeComboBoxes.size(); i++) {
            JComboBox<String> variableDiscretizeComboBox = discretizeComboBoxes.get(i);
            variableDiscretizeComboBox.setEnabled(discretizeComboBox.getSelectedIndex() == 0);
            if (discretizeComboBox.getSelectedIndex() >= 0) {
                variableDiscretizeComboBox.setSelectedItem((isNumeric[i]) ? selected : 0);
                variableDiscretizeComboBox.setEnabled(isNumeric[i] && discretizeComboBox.getSelectedIndex() == 0);
            }
        }
    }//GEN-LAST:event_discretizeComboBoxActionPerformed

    // GEN-LAST:event_savePreprocessButtonActionPerformed

    private Map<String, Integer> getSelectedNumIntervals() {
        Map<String, Integer> selectedNumIntervals = new HashMap<>();

        for (int i = 0; i < varSelectionCheckBoxes.size(); ++i) {
            JCheckBox checkBox = varSelectionCheckBoxes.get(i);
            if (checkBox.isSelected()) {
                selectedNumIntervals.put(checkBox.getText(), (Integer) numIntervalsSpinners.get(i).getValue());
            }
        }
        return selectedNumIntervals;
    }

    private Map<String, Discretization.Option> getSelectedDiscretizeOptions() {
        Map<String, Discretization.Option> selectedDiscretizeOptions = new HashMap<>();

        for (int i = 0; i < varSelectionCheckBoxes.size(); ++i) {
            JCheckBox checkBox = varSelectionCheckBoxes.get(i);
            if (checkBox.isSelected()) {
                int selectedIndex = discretizeComboBoxes.get(i).getSelectedIndex();
                selectedDiscretizeOptions.put(checkBox.getText(), Discretization.Option.values()[selectedIndex]);
            }
        }
        return selectedDiscretizeOptions;
    }

    private Map<String, MissingValues.Option> getSelectedMissingValuesOptions() {
        Map<String, MissingValues.Option> selectedPreprocessOptions = new HashMap<>();

        for (int i = 0; i < varSelectionCheckBoxes.size(); ++i) {
            JCheckBox checkBox = varSelectionCheckBoxes.get(i);
            if (checkBox.isSelected()) {
                int selectedIndex = missingValuesComboBoxes.get(i).getSelectedIndex();
                selectedPreprocessOptions.put(checkBox.getText(), MissingValues.Option.values()[selectedIndex]);
            }
        }
        return selectedPreprocessOptions;
    }

    private void cancelButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void learnButtonActionPerformed(
            ActionEvent evt) throws LearningException.NoDatabasePresent, LearningException.NoChosenVariables, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {// GEN-FIRST:event_learnButtonActionPerformed
        if ((databasePath == null) || (databasePath.isEmpty())) {
            throw new LearningException.NoDatabasePresent();
        }
        List<Variable> selectedVariables = getSelectedVariables();
        if (selectedVariables.isEmpty()) {
            throw new LearningException.NoChosenVariables();
        }
        ModelNetUse modelNetUse = null;
        if (modelNet != null) {
            modelNetUse = new ModelNetUse(true, modelNetTabPanel.useNodePositionsCheckBox.isSelected(),
                                          modelNetTabPanel.startFromModelNetCheckBox.isSelected(), modelNetTabPanel.addLinkModelNet.isSelected(),
                                          modelNetTabPanel.deleteLinksModelNet.isSelected(), modelNetTabPanel.invertLinksModelNet.isSelected());
        }
        Variable selectedItem = (Variable) generalTabPanel.variableListCombobox.getSelectedItem();
        String classVariable = (selectedItem != null && !selectedItem.getName().isEmpty())
                ? selectedItem.getName() : "";
        @SuppressWarnings("unchecked")
        Class<? extends LearningAlgorithm> algorithm =
                (Class<? extends LearningAlgorithm>) generalTabPanel.algorithmComboBox.getSelectedItem();

        CaseDatabase preprocessedDb = LearningController.preprocessDatabase(
                database, selectedVariables,
                getSelectedMissingValuesOptions(), getSelectedDiscretizeOptions(), getSelectedNumIntervals(),
                modelNet);

        try {
            LearningManager learningManager = LearningController.initLearning(
                    preprocessedDb, algorithm, modelNet, modelNetUse, optionsGUI, classVariable);
            selectedClassVariable = classVariable;

            long start = System.currentTimeMillis();
            
            ProbNet probNet = learningManager.getLearnedNet();
            int dotIndex = databaseName.lastIndexOf('.');
            String baseName = dotIndex >= 0 ? databaseName.substring(0, dotIndex) : databaseName;
            probNet.setName(baseName + stringDatabase.getString("Learning.NetSuffix"));
            NetworkEditorPanel networkPanel = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant()
                                                                  .createNewFrame(probNet);
            networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsNetworkModified(false, false);
            if (generalTabPanel.automaticLearningRadioButton.isSelected()) {
                learningManager.learn();
                long elapsedTimeMillis = System.currentTimeMillis() - start;
                System.out.print(stringDatabase.getString("Learning.LearningFinished") +
                        LearningController.formatElapsedTime(elapsedTimeMillis) + "\n");
                if (modelNetUse == null || !modelNetUse.isUseModelNet()) {
                    ProbNet learnedNet = learningManager.getLearnedNet();
                    var positions = new StressLayout().compute(learnedNet);
                    if (!positions.isEmpty()) {
                        new AutoArrangeEdit(learnedNet, positions).executeEdit();
                        networkPanel.repaint();
                    }
                }
            } else {
                // INTERACTIVE LEARNING
                InteractiveLearningDialog interactiveLearningGUI = new InteractiveLearningDialog(
                        this.parent, false, learningManager, networkPanel);
                if ((modelNetUse == null) || (!modelNetUse.isUseNodePositions())) {
                    LearningController.placeNodesInCircle(learningManager.getLearnedNet(), selectedClassVariable);
                }
                interactiveLearningGUI.setVisible(true);
                // Ensure parametric learning has been called: if the user closed the dialog
                // without applying any edits (e.g. single-node network), nodes have no potentials.
                boolean needsParametricLearning = learningManager.getLearnedNet().getNodes()
                        .stream().anyMatch(node -> node.getNumPotentials() == 0);
                if (needsParametricLearning) {
                    learningManager.runParametricLearning();
                }
            }

            this.setVisible(false);
        } catch (OutOfMemoryError e1) {
            throw new UnreachableException(new NotEnoughMemoryException(e1));
        } catch (UnobservedVariablesException | EmptyModelNetException | CannotNormalizePotentialException
                 | DoEditException e) {
            throw new UnreachableException(e);
        }
    }// GEN-LAST:event_learnButtonActionPerformed


    private void numIntervalsCheckBoxActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_numIntervalsCheckBoxActionPerformed
        if (numIntervalsCheckBox.isSelected()) {
            numIntervalsSpinner.setEnabled(true);
            Integer selected = (Integer) numIntervalsSpinner.getValue();

            for (JSpinner spinner : numIntervalsSpinners) {
                spinner.setValue(selected);
                spinner.setEnabled(false);
            }
        } else {
            numIntervalsSpinner.setEnabled(false);
            for (int i = 0; i < numIntervalsSpinners.size(); i++) {
                numIntervalsSpinners.get(i).setEnabled(isNumeric[i]);
            }
        }
    }//GEN-LAST:event_numIntervalsCheckBoxActionPerformed

    private void startFromModelNetCheckBoxActionPerformed(ActionEvent evt) {
        if (modelNetTabPanel.startFromModelNetCheckBox.isSelected()) {
            modelNetTabPanel.addLinkModelNet.setEnabled(true);
            modelNetTabPanel.addLinkModelNet.setSelected(true);
            modelNetTabPanel.deleteLinksModelNet.setEnabled(true);
            modelNetTabPanel.invertLinksModelNet.setEnabled(true);
        } else {
            modelNetTabPanel.addLinkModelNet.setEnabled(false);
            modelNetTabPanel.addLinkModelNet.setSelected(false);
            modelNetTabPanel.deleteLinksModelNet.setEnabled(false);
            modelNetTabPanel.invertLinksModelNet.setEnabled(false);
        }
    }

    private void numIntervalsSpinnerStateChanged(
            ChangeEvent evt) {//GEN-FIRST:event_numIntervalsSpinnerStateChanged
        Integer selected = (Integer) numIntervalsSpinner.getValue();

        for (JSpinner spinner : numIntervalsSpinners) {
            spinner.setValue(selected);
        }
    }//GEN-LAST:event_numIntervalsSpinnerStateChanged

    private void resetButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        databasePath = null;
        databaseName = null;
        modelNet = null;
        generalTabPanel.caseFileTextPane.setText(null);
        modelNetTabPanel.modelNetTextPane.setText(null);
        modelNetTabPanel.noModelNetRadioButton.setEnabled(true);
        resetVariablePanel();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void noModelNetRadioButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_noModelNetRadioButtonActionPerformed
        modelNet = null;
        modelNetTabPanel.modelNetTextPane.setText(null);
        modelNetTabPanel.loadModelNetButton.setEnabled(false);
        modelNetTabPanel.useNodePositionsCheckBox.setSelected(false);
        modelNetTabPanel.useNodePositionsCheckBox.setEnabled(false);
        modelNetTabPanel.startFromModelNetCheckBox.setEnabled(false);
        modelNetTabPanel.startFromModelNetCheckBox.setSelected(false);
        modelNetTabPanel.addLinkModelNet.setSelected(false);
        modelNetTabPanel.deleteLinksModelNet.setSelected(false);
        modelNetTabPanel.invertLinksModelNet.setSelected(false);
        modelNetTabPanel.addLinkModelNet.setEnabled(false);
        modelNetTabPanel.deleteLinksModelNet.setEnabled(false);
        modelNetTabPanel.invertLinksModelNet.setEnabled(false);
        allVariablesRadioButton.setSelected(true);
    }//GEN-LAST:event_noModelNetRadioButtonActionPerformed

    private void fromOpenMarkovRadioButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_fromOpenMarkovRadioButtonActionPerformed
        modelNet = MainGUI.INSTANCE.mainPanel.
                getMainPanelListenerAssistant().
                getCurrentNetworkEditorPanel().getProbNet();
        modelNetTabPanel.modelNetTextPane.setText(null);
        modelNetTabPanel.loadModelNetButton.setEnabled(false);
        modelNetSelected();


    }//GEN-LAST:event_fromOpenMarkovRadioButtonActionPerformed

    private void modelNetSelected() {
        modelNetVariablesRadioButton.setSelected(true);
        modelNetTabPanel.useNodePositionsCheckBox.setEnabled(true);
        modelNetTabPanel.useNodePositionsCheckBox.setSelected(true);
        modelNetTabPanel.startFromModelNetCheckBox.setEnabled(true);

        updateVariableSelectionPanel();

        discretizeComboBox.addItem(stringDatabase.getString("Learning.Discretize.ModelNet"));
        discretizeComboBox.setSelectedItem(stringDatabase.getString("Learning.Discretize.ModelNet"));
    }

    private void fromFileRadioButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_fromFileRadioButtonActionPerformed
        modelNetTabPanel.loadModelNetButton.setEnabled(true);
    }//GEN-LAST:event_fromFileRadioButtonActionPerformed

    private void allVariablesRadioButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_allVariablesRadioButtonActionPerformed
        selectDeselectCheckBox.setSelected(false);
        selectDeselectCheckBox.setEnabled(false);
        for (JCheckBox checkBox : varSelectionCheckBoxes) {
            checkBox.setSelected(true);
            checkBox.setEnabled(false);
        }
    }//GEN-LAST:event_allVariablesRadioButtonActionPerformed

    private void selectedVariablesRadioButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_selectedVariablesRadioButtonActionPerformed
        selectDeselectCheckBox.setEnabled(true);
        for (JCheckBox checkBox : varSelectionCheckBoxes) {
            checkBox.setEnabled(true);
        }
    }//GEN-LAST:event_selectedVariablesRadioButtonActionPerformed

    private void modelNetVariablesRadioButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_modelNetVariablesRadioButtonActionPerformed
        selectDeselectCheckBox.setSelected(false);
        selectDeselectCheckBox.setEnabled(false);
        updateVariableSelectionPanel();
    }//GEN-LAST:event_modelNetVariablesRadioButtonActionPerformed

    private void selectDeselectCheckBoxActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_selectDeselectCheckBoxActionPerformed
        boolean value = selectDeselectCheckBox.isSelected();
        for (JCheckBox checkBox : varSelectionCheckBoxes) {
            checkBox.setSelected(value);
        }
    }//GEN-LAST:event_selectDeselectCheckBoxActionPerformed

    private void optionsButtonActionPerformed(
            ActionEvent evt) {//GEN-FIRST:event_optionsButtonActionPerformed
        optionsGUI.setVisible(true);
        generalTabPanel.optionsTextArea.setText(optionsGUI.getDescription());
    }//GEN-LAST:event_optionsButtonActionPerformed

    private void algorithmComboBoxActionPerformed(ActionEvent evt) {
        if (generalTabPanel.algorithmComboBox.getSelectedIndex() == -1) {
            return;
        }
        Class<? extends LearningAlgorithm> selectedAlgorithm = (Class<? extends LearningAlgorithm>) generalTabPanel.algorithmComboBox.getSelectedItem();
        optionsGUI = algorithmConfigurationManager.instanciateForAlgorithm(selectedAlgorithm);
        generalTabPanel.optionsButton.setEnabled(optionsGUI != null);
        generalTabPanel.optionsTextArea.setText((optionsGUI != null) ? optionsGUI.getDescription() : "");
        updateVariableListCombobox();
    }

    private void setAlgorithmDefaults() {
        setDefaultAlgorithmConfiguration(generalTabPanel.discriminativeAlgorithmType.isSelected());
        generalTabPanel.jPanel3.revalidate();
    }

    private void setDefaultAlgorithmConfiguration(boolean discriminative) {

        generalTabPanel.variableListCombobox.setEnabled(discriminative);
        generalTabPanel.variableListCombobox.setVisible(discriminative);
        generalTabPanel.classVariableLabel.setVisible(discriminative);
        generalTabPanel.algorithmComboBox.removeAllItems();
        
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
        algorithms.forEach(generalTabPanel.algorithmComboBox::addItem);

        generalTabPanel.variableListCombobox.setModel(new DefaultComboBoxModel(getSelectedVariables().toArray()));
        if (!discriminative) {
            generalTabPanel.variableListCombobox.setSelectedItem(null);
        }
        selectedAlgorithm = (Class<? extends LearningAlgorithm>) generalTabPanel.algorithmComboBox.getSelectedItem();
        optionsGUI = algorithmConfigurationManager.instanciateForAlgorithm(selectedAlgorithm);
        generalTabPanel.optionsButton.setEnabled(optionsGUI != null);
        generalTabPanel.optionsTextArea.setText((optionsGUI != null) ? optionsGUI.getDescription() : "");
    }


    private void resetVariablePanel() {
        discretizeComboBox.setSelectedIndex(0);
        missingValuesComboBox.setSelectedIndex(0);
        numIntervalsCheckBox.setSelected(false);
        allVariablesRadioButton.setSelected(true);
        numIntervalsSpinner.setValue(2);
        numIntervalsSpinner.setEnabled(false);
        varSelectionPanel.removeAll();
        missingValuesPanel.removeAll();
        discretizePanel.removeAll();
        numIntervalsPanel.removeAll();
        varSelectionCheckBoxes.clear();
        missingValuesComboBoxes.clear();
        discretizeComboBoxes.clear();
        numIntervalsSpinners.clear();
    }

    private void updateVariableSelectionPanel() {
        if (database == null) {
            return;
        }
        varSelectionPanel.removeAll();
        missingValuesPanel.removeAll();
        discretizePanel.removeAll();
        numIntervalsPanel.removeAll();
        varSelectionCheckBoxes.clear();
        missingValuesComboBoxes.clear();
        discretizeComboBoxes.clear();
        numIntervalsSpinners.clear();

        allVariablesRadioButton.setEnabled(true);
        selectedVariablesRadioButton.setEnabled(true);

        isNumeric = new boolean[database.getVariables().size()];

        int variableIndex = 0;
        for (Variable variable : database.getVariables()) {
            /*JComboBox preprocessOptions = new JComboBox(new javax.swing.
                    DefaultComboBoxModel(AbsentValues.getOptions()));*/
            JComboBox<String> preprocessOptions = new JComboBox<>();
            preprocessOptions.addItem(stringDatabase.getString("Learning.MissingValues.KeepMissing"));
            preprocessOptions.addItem(stringDatabase.getString("Learning.MissingValues.Eliminate"));
            preprocessOptions.setSelectedItem(0);
            preprocessOptions.setPreferredSize(new Dimension(225, 18));
            JCheckBox varSelect = new JCheckBox(variable.getName());
            varSelect.setPreferredSize(new Dimension(175, 18));
            varSelect.setEnabled(false);
            boolean select = true;
            if (modelNetVariablesRadioButton.isSelected()) {
                select = modelNet.containsVariable(variable.getName());
            }
            varSelect.setSelected(select);
            /*JComboBox discretizeOptions = new JComboBox(new javax.swing.
                    DefaultComboBoxModel(Discretization.getOptions()));*/
            JComboBox<String> discretizeOptions = new JComboBox<>(
                    new DefaultComboBoxModel<>());
            discretizeOptions.addItem(stringDatabase.getString("Learning.Discretize.NoDiscretize"));
            discretizeOptions.addItem(stringDatabase.getString("Learning.Discretize.SameFreq"));
            discretizeOptions.addItem(stringDatabase.getString("Learning.Discretize.SameWidth"));
            if (modelNet != null) {
                discretizeOptions.addItem(stringDatabase.getString("Learning.Discretize.ModelNet"));
            }
            discretizeOptions.setSelectedItem(0);
            discretizeOptions.setPreferredSize(new Dimension(175, 18));
            discretizeOptions.addItemListener(e -> {
                for (int i = 0; i < discretizeComboBoxes.size(); i++) {
                    if (discretizeComboBoxes.get(i).equals(e.getSource())) {
                        numIntervalsSpinners.get(i).setEnabled(
                                isNumeric[i] && discretizeComboBoxes.get(i).getSelectedIndex() > 0
                                        && !numIntervalsCheckBox.isSelected());
                        break;
                    }
                }
            });
            JSpinner numIntervals = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
            numIntervals.setPreferredSize(new Dimension(50, 18));
            isNumeric[variableIndex] = Discretization.isNumeric(variable);
            discretizeOptions.setEnabled(isNumeric[variableIndex] && discretizeComboBox.getSelectedIndex() == 0);
            /* If there is a model net and the variable is in the model net
             * and is discretized, then the default option should be 'As in model'*/
            if (modelNet != null) {
                VariableType variableTypeInModel = modelNet.getNode(variable.getName()).getVariable()
                                                           .getVariableType();
                if (variableTypeInModel == VariableType.DISCRETIZED) {
                    discretizeOptions.setSelectedItem(stringDatabase.getString("Learning.Discretize.ModelNet"));
                }
            }
            numIntervals.setEnabled(isNumeric[variableIndex] && discretizeOptions.getSelectedIndex() > 1);

            varSelectionPanel.add(varSelect);
            missingValuesPanel.add(preprocessOptions);
            discretizePanel.add(discretizeOptions);
            numIntervalsPanel.add(numIntervals);
            varSelectionCheckBoxes.add(varSelect);
            missingValuesComboBoxes.add(preprocessOptions);
            discretizeComboBoxes.add(discretizeOptions);
            numIntervalsSpinners.add(numIntervals);
            variableIndex++;
        }

        varSelectionPanel.revalidate();
        missingValuesPanel.revalidate();
        discretizePanel.revalidate();
        numIntervalsPanel.revalidate();
    }

    private List<Variable> getSelectedVariables() {
        return varSelectionCheckBoxes.stream()
                                     .filter(JCheckBox::isSelected)
                                     .map(cb -> database.getVariable(cb.getText()))
                                     .collect(Collectors.toList());
    }

    // End of variables declaration//GEN-END:variables

}


