/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.dialog;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.bnEvaluation.Coherence;
import org.openmarkov.bnEvaluation.NetEvaluator;
import org.openmarkov.bnEvaluation.component.DBOpenerPanel;
import org.openmarkov.bnEvaluation.component.MeasuresPanel;
import org.openmarkov.bnEvaluation.exceptions.NetworkIsNotEvaluable;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.dialog.io.NetworkOMFileChooser;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the GUI responsible for collecting the information for the class
 * that will evaluate the Bayesian network.
 *
 * @author evillar
 */
public final class BNEvaluationDialog extends OkCancelDialog {
    
    private @Nullable ProbNet probNet = null;
    private CaseDatabase netdatabase = null;
    private Coherence coherence = null;
    
    private JTextPane modelNetTextPane;
    private JButton loadModelNetButton;
    private JRadioButton fromFileRadioButton;
    private JRadioButton fromOpenMarkovRadioButton;
    
    private final DBOpenerPanel dbOpenerPanel;
    private final MeasuresPanel measuresPanel;
    
    private boolean currentlyOpenNetIsElegible;
    private final Frame owner;
    
    public BNEvaluationDialog(Frame owner) {
        super(owner);
        this.owner = owner;
        this.setTitle("Bayesian network evaluation");
        this.setMinimumSize(new Dimension(480, 390));
        this.setLocationRelativeTo(owner);
        try {
            BNEvaluationDialog.checkNetworkEvaluability(MainPanel.getCurrentProbNet());
            this.currentlyOpenNetIsElegible = true;
        } catch (NetworkIsNotEvaluable ignored) {
            this.currentlyOpenNetIsElegible = false;
        }
        
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(840, 520));
        // add components with a BoxLayout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(this.getProbNetPanel());
        this.dbOpenerPanel = new DBOpenerPanel(this);
        mainPanel.add(this.dbOpenerPanel);
        this.dbOpenerPanel.onOpen((databaseFile, database) -> this.checkMeasuresComponents(EnableMeasuresTriger.FromSettingDatabase));
        this.measuresPanel = new MeasuresPanel();
        this.measuresPanel.setEnabled(false);
        this.measuresPanel.allMeasureRelatedCheckboxes()
                           .forEach(metricCheckbox -> metricCheckbox.addChangeListener(e -> this.checkOkButton()));
        mainPanel.add(this.measuresPanel);
        this.add(mainPanel);
        JRadioButton buttonToClick = currentlyOpenNetIsElegible ? this.fromOpenMarkovRadioButton : this.fromFileRadioButton;
        buttonToClick.setSelected(true);
        this.pack();
        setSize(new Dimension(864, 585));
    }
    
    public void reload() {
        if (MainPanel.getCurrentProbNet() == this.probNet) {
            return;
        }
        try {
            BNEvaluationDialog.checkNetworkEvaluability(MainPanel.getCurrentProbNet());
            this.currentlyOpenNetIsElegible = true;
        } catch (NetworkIsNotEvaluable ignored) {
            this.currentlyOpenNetIsElegible = false;
        }
        this.fromOpenMarkovRadioButton.setEnabled(this.currentlyOpenNetIsElegible);
        if (this.fromOpenMarkovRadioButton.isSelected() || this.probNet == null) {
            if (currentlyOpenNetIsElegible) {
                this.fromOpenMarkovRadioButton.setSelected(true);
                setProbnetToMainPanelProbNet();
            } else {
                this.fromFileRadioButton.setSelected(true);
            }
        }
    }
    
    /**
     * Gets the Panel to select the probNet to evaluate
     *
     * @return JPanel
     */
    private JPanel getProbNetPanel() {
        JPanel probNetPanel = new JPanel();
        probNetPanel.setBorder(BorderFactory.createTitledBorder("Choose Net"));
        probNetPanel.setPreferredSize(new Dimension(840, 100));
        //create the components
        this.fromFileRadioButton = new JRadioButton();
        this.fromOpenMarkovRadioButton = new JRadioButton();
        ButtonGroup modelNetButtonGroup = new ButtonGroup();
        modelNetButtonGroup.add(this.fromFileRadioButton);
        modelNetButtonGroup.add(this.fromOpenMarkovRadioButton);
        this.modelNetTextPane = new JTextPane();
        JScrollPane jScrollProbNetTextPane = new JScrollPane();
        this.loadModelNetButton = new JButton();
        
        // component properties
        this.fromFileRadioButton.setText("Load net from file");
        this.fromFileRadioButton.addItemListener(this::fromFileRadioButtonActionPerformed);
        this.fromFileRadioButton.setEnabled(true);
        this.fromOpenMarkovRadioButton.setText("Network already open");
        this.fromOpenMarkovRadioButton.addItemListener(this::fromOpenMarkovRadioButtonActionPerformed);
        this.fromOpenMarkovRadioButton.setEnabled(this.currentlyOpenNetIsElegible);
        
        this.modelNetTextPane.setEditable(false);
        this.modelNetTextPane.setEnabled(false);
        jScrollProbNetTextPane.setViewportView(this.modelNetTextPane);
        
        this.loadModelNetButton.setText("Open");
        this.loadModelNetButton.setEnabled(false);
        this.loadModelNetButton.addActionListener(e -> {
            try {
                this.loadModelNetButtonActionPerformed();
            } catch (NoReaderForFileException | ParserException | IOException | CorruptNetworkFile |
                     NetworkIsNotEvaluable ex) {
                throw new UnrecoverableException(ex);
            }
        });
        
        // add components to Panel with a GroupLayout
        GroupLayout layout = new GroupLayout(probNetPanel);
        probNetPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                                        .addGroup(layout.createSequentialGroup()
                                                        .addGap(10)
                                                        .addComponent(this.fromOpenMarkovRadioButton))
                                        .addGroup(layout.createSequentialGroup()
                                                        .addGap(10)
                                                        .addComponent(this.fromFileRadioButton)
                                                        .addGap(10)
                                                        .addComponent(this.loadModelNetButton,
                                                                      GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                        .addGap(10)
                                                        .addComponent(jScrollProbNetTextPane)
                                                        .addGap(10)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                                      .addComponent(this.fromOpenMarkovRadioButton)
                                      .addGap(5)
                                      .addGroup(layout.createParallelGroup()
                                                      .addComponent(this.fromFileRadioButton)
                                                      .addComponent(this.loadModelNetButton))
                                      .addGap(5)
                                      .addComponent(jScrollProbNetTextPane, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                                      .addGap(5));
        return probNetPanel;
    }
    
    /**
     * This method take the open network in the main panel
     * of OpenMarkov
     * Is the listener of fromOpenMarkovRadioButton
     */
    private void fromOpenMarkovRadioButtonActionPerformed(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        setProbnetToMainPanelProbNet();
    }
    
    private void setProbnetToMainPanelProbNet() {
        this.probNet = MainPanel.getCurrentProbNet();
        this.checkMeasuresComponents(EnableMeasuresTriger.FromSettingProbNet);
    }
    
    /**
     * This method enables the button to load the file network
     * Is the listener of fromFileRadioButton
     */
    private void fromFileRadioButtonActionPerformed(ItemEvent e) {
        this.modelNetTextPane.setText(null);
        this.loadModelNetButton.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        this.probNet = null;
        this.checkMeasuresComponents(EnableMeasuresTriger.FromSettingProbNet);
    }
    
    /**
     * This method open a network. Is the listener of loadModelNetButton.
     * Then, method enableMeasuresComponents is called for check
     * consistency between the network and data and activate the measurement components
     */
    private void loadModelNetButtonActionPerformed() throws NoReaderForFileException, ParserException, IOException, CorruptNetworkFile, NetworkIsNotEvaluable {
        NetworkOMFileChooser modelNetFileChooser = new NetworkOMFileChooser(); // COMMON_NETWORK_FILE_CHOOSER;
        //modelNetFileChooser.setDialogTitle(stringDatabase.getString("OpenNetwork.Title));
        if (modelNetFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String filePath = modelNetFileChooser.getSelectedFile().getAbsolutePath();
        ProbNet newNetwork = NetsIO.openNetworkFile(filePath).probNet();
        BNEvaluationDialog.checkNetworkEvaluability(newNetwork);
        this.probNet = newNetwork;
        this.modelNetTextPane.setText(this.probNet.getName());
        this.checkMeasuresComponents(EnableMeasuresTriger.FromSettingProbNet);
    }
    
    /**
     * This method checks that the ProbNet is a Bayesian network and it has defined potentials
     *
     * @param networkToEvaluate : probNet to check
     */
    private static void checkNetworkEvaluability(ProbNet networkToEvaluate) throws NetworkIsNotEvaluable {
        //check that it is a Bayesian network and that it has defined potentials
        if (networkToEvaluate == null || !(networkToEvaluate.getNetworkType() instanceof BayesianNetworkType)) {
            throw new NetworkIsNotEvaluable(networkToEvaluate);
        }
        @ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG, reasonDescription =
                "getPotentials always give a not null value. Did this mean to check whether the potentials list" +
                        "has a size superior to 0?")
        List<Potential> tabla = networkToEvaluate.getPotentials();
        if (tabla == null) {
            throw new NetworkIsNotEvaluable(networkToEvaluate);
        }
    }
    
    enum EnableMeasuresTriger {
        FromSettingProbNet,
        FromSettingDatabase,
    }
    
    /**
     * this method manages the components when database and the probNet are loaded
     * when (they) are loaded enable the ok-button and the measures check-boxes
     *
     * @param trigger: i=0 when is called from probnet and i=1 when database
     */
    private void checkMeasuresComponents(EnableMeasuresTriger trigger) {
        if (this.dbOpenerPanel.getDatabase() != null && this.probNet != null) {
            this.coherence = this.isConsistentProbNetCase();
            switch (this.coherence) {
                case STRONG -> this.measuresPanel.setEnabled(true);
                case WEAK -> {
                    JOptionPane.showMessageDialog(null,
                                                  "Week coherence between network and dataset variables",
                                                  "Warning", JOptionPane.WARNING_MESSAGE);
                    this.measuresPanel.setEnabled(true);
                }
                case ZERO -> {
                    JOptionPane.showMessageDialog(null,
                                                  "There are not enougth variables in database",
                                                  "Warning", JOptionPane.WARNING_MESSAGE);
                    switch (trigger) {
                        case FromSettingProbNet -> {
                            this.probNet = null;
                            this.modelNetTextPane.setText(null);
                        }
                        case FromSettingDatabase -> this.dbOpenerPanel.removeCurrentDB();
                    }
                }
            }
            this.checkOkButton();
            return;
        }
        this.measuresPanel.setEnabled(false);
        this.getOKButton().setEnabled(false);
    }
    
    private void checkOkButton() {
        boolean aMeasureIsSelected = this.measuresPanel.allMeasureRelatedCheckboxes()
                                                        .anyMatch(AbstractButton::isSelected);
        this.getOKButton()
            .setEnabled(aMeasureIsSelected && this.dbOpenerPanel.getDatabase() != null && this.probNet != null);
    }
    
    /**
     * This method checks the coherence between the network variables and
     * database:
     * select the database variables included in probNet and check that
     * all the states (in this variables) are contained in probNet-variable states
     * Look out!! A new translated database is created (netcasedatabase)
     *
     * @return Coherence between the variables
     */
    private Coherence isConsistentProbNetCase() {
        // when the coherence is weak; varNotIncluded contains the list of variables not included
        // number of probNet variables not included in database
        
        // variables from the probNet
        List<Variable> netVariables = this.probNet.getVariables();
        // variablesCount is the number of database-variables included in probNet
        // caseVariables: probNet-variables corresponding to the database-variables
        // included in probNet
        ArrayList<int[]> casesArray = new ArrayList<>();
        ArrayList<Variable> caseVariables = new ArrayList<>();
        ArrayList<String> existingVariables = new ArrayList<>();
        int variablesCount = 0;
        List<Variable> notIncludedVariables = new ArrayList<>();
        CaseDatabase database = this.dbOpenerPanel.getDatabase();
        for (Variable netVariable : netVariables) {
            // search for the network variable in the database variables
            Variable caseVariable = database.getVariable(netVariable.getName());
            if (caseVariable == null) {
                notIncludedVariables.add(netVariable);
                continue;
            }
            int[] casesCaseVariable = database.getCases(caseVariable);
            if (this.containsStates(netVariable, caseVariable, casesCaseVariable)) {
                variablesCount = variablesCount + 1;
                caseVariables.add(netVariable);
                casesArray.add(casesCaseVariable);
                existingVariables.add(netVariable.getName());
            } else {
                this.measuresPanel.changeVariables(Collections.emptyList());
                JOptionPane.showMessageDialog(null,
                                              "Dataset variable: " + caseVariable.getName() +
                                                      " contains no-defined states in the network variable",
                                              "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        this.measuresPanel.changeVariables(existingVariables);
        if (!notIncludedVariables.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                                          "Network variable/s: " + notIncludedVariables.stream()
                                                                                       .map(Variable::getName)
                                                                                       .collect(Collectors.joining(", ")) +
                                                  " are not included in dataset",
                                          "Information", JOptionPane.INFORMATION_MESSAGE);
        }
        if (variablesCount > 1) {
            // coherence strong or week
            int[][] cases = new int[database.getNumCases()][variablesCount];
            for (int v = 0; v < variablesCount; v++) {
                int[] casesV = casesArray.get(v);
                for (int i = 0; i < database.getNumCases(); i++) {
                    cases[i][v] = casesV[i];
                }
            }
            this.netdatabase = new CaseDatabase(caseVariables, cases);
            if (variablesCount == netVariables.size()) {
                return Coherence.STRONG;
            }
            return Coherence.WEAK;
        }
        this.measuresPanel.changeVariables(Collections.emptyList());
        return Coherence.ZERO;
    }
    
    /**
     * This method check that the varContainer (probNet variable) contains all the
     * varIncluded (caseDatabase variable) states and performs the corresponding
     * translation to cases[] changing the StateIndex to the StateIndex in the
     * probnet-variable
     *
     * @param varContainer (probNet variable)
     * @param varIncluded  (caseDatabase variable)
     *
     * @return true when all the states of varIncluded are in varContainer,
     * false in other case
     */
    private boolean containsStates(Variable varContainer, Variable varIncluded, int[] cases) {
        State[] includedStates = varIncluded.getStates();
        // indexStateInNet[i] index of State i in the probNet-variable
        int[] indexStateInNet = new int[includedStates.length];
        for (int i = 0; i < includedStates.length; i++) {
            State state = varContainer.getState(includedStates[i].getName());
            //This case does not happen, but just in case
            if (state == null) {
                return false;
            }
            indexStateInNet[i] = varContainer.getStateIndex(state);
        }
        //traslate the cases
        for (int i = 0; i < cases.length; i++) {
            cases[i] = indexStateInNet[cases[i]];
        }
        return true;
    }
    
    @Override
    protected boolean doOkClickBeforeHide() throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        boolean aMeasureIsSeleteced = this.measuresPanel.allMeasureRelatedCheckboxes()
                                                         .anyMatch(AbstractButton::isSelected);
        if (!aMeasureIsSeleteced) {
            JOptionPane.showMessageDialog(null,
                                          "There arent measures selected",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String title = "Evaluation of the " + this.probNet.getName() + " network";
        MeasuresSet measuresSet = this.measuresPanel.measuresSet(title, this.coherence, this.netdatabase);
        NetEvaluator evaluator = new NetEvaluator(this.probNet, this.netdatabase, measuresSet);
        ResultsDialog resultsDialog = new ResultsDialog(this.owner, evaluator.runEvaluator());
        this.setVisible(false);
        resultsDialog.setVisible(true);
        return true;
    }
}