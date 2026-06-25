/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.dialog;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.bnEvaluation.SplitSet;
import org.openmarkov.bnEvaluation.SplitSetManager;
import org.openmarkov.bnEvaluation.component.DBOpenerPanel;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.gui.component.NumericSpinner;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.io.DBWriterOMFileChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * This class is the GUI responsible for collecting the options to divide sets
 *
 * @author evillar
 */

public final class SplitDatasetDialog extends OkCancelDialog {
    
    private @Nullable SplitSet sets;
    private int numCases;
    
    /**
     * Swing components
     */
    private final Frame owner;
    private final JPanel mainPanel;
    
    /**
     * caseDatabasePanel Swing components
     */
    private final DBOpenerPanel dbOpenerPanel;
    
    /**
     * SplitSet Swing components
     */
    private final NumericSpinner<Float> testFractionSpinner;
    private final NumericSpinner<Integer> testNumberSpinner;
    private final NumericSpinner<Integer> trainNumberSpinner;
    private final JTextField totalNumberText;
    private final JRadioButton firstCasesButton;
    private final JRadioButton randomCasesButton;
    private final JRadioButton lastCasesButton;
    private final ButtonGroup typeSelectionButtonGroup;
    
    private final DBWriterOMFileChooser testWriter;
    private final DBWriterOMFileChooser trainWriter;
    
    public SplitDatasetDialog(Frame owner) {
        super(owner);
        this.owner = owner;
        //this.setTitle(Nls.bnevaluation.BNEvaluation.Dialog.Title.stringify());
        this.setTitle("Split Dataset");
        this.setMinimumSize(new Dimension(400, 300));
        this.setLocationRelativeTo(owner);
        sets = null;
        numCases = 0;
        this.testWriter = new DBWriterOMFileChooser(false);
        this.trainWriter = new DBWriterOMFileChooser(false);
        this.testWriter.setDialogTitle("Save test cases in...");
        this.trainWriter.setDialogTitle("Save training cases in...");
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        testFractionSpinner = new NumericSpinner<>(Float.class);
        testFractionSpinner.setMinimum(0.0f);
        testFractionSpinner.setMaximum(99.999f);
        testFractionSpinner.setCurrentValue(10.0f);
        testNumberSpinner = new NumericSpinner<>(Integer.class);
        testNumberSpinner.setMinimum(1);
        testNumberSpinner.setMaximum(0);
        trainNumberSpinner = new NumericSpinner<>(Integer.class);
        trainNumberSpinner.setMinimum(1);
        trainNumberSpinner.setMaximum(0);
        
        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(700, 400));
        // add components with a BoxLayout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        this.dbOpenerPanel = new DBOpenerPanel(this);
        mainPanel.add(this.dbOpenerPanel);
        dbOpenerPanel.onOpen((databaseFile, database) -> {
            numCases = database.getNumCases();
            Float oldFraction = this.testFractionSpinner.getCurrentValue();
            float floatNumCases = (float) numCases;
            this.testFractionSpinner.setMinimum(100.f * (1.0f / floatNumCases));
            this.testFractionSpinner.setMaximum(100.0f * ((floatNumCases - 1.0f) / floatNumCases));
            this.testNumberSpinner.setMaximum(numCases);
            this.trainNumberSpinner.setMaximum(numCases);
            this.testFractionSpinner.setCurrentValue(oldFraction);
            enableOptions();
        });
        
        JPanel splitSetPanel = new JPanel();
        splitSetPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        
        // create components
        JLabel testComboLabel = new JLabel("Test fraction (%)");
        JLabel testTextLabel = new JLabel("Testing cases");
        JLabel trainTextLabel = new JLabel("Training cases");
        JLabel totalTextLabel = new JLabel("Total cases");
        
        totalNumberText = new JTextField();
        JLabel typeSelectionLabel = new JLabel("Selection of testing");
        firstCasesButton = new JRadioButton("First");
        randomCasesButton = new JRadioButton("Random");
        lastCasesButton = new JRadioButton("Last");
        typeSelectionButtonGroup = new ButtonGroup();
        
        // component properties: all no enabled
        testFractionSpinner.setEnabled(false);
        testFractionSpinner.addChangeListener(e -> testFractionComboBoxActionPerformed());
        
        totalNumberText.setEnabled(false);
        testNumberSpinner.setEnabled(false);
        testNumberSpinner.addChangeListener(e -> testNumberTextActionPerformed());
        
        trainNumberSpinner.setEnabled(false);
        trainNumberSpinner.addChangeListener(e -> trainNumberTextActionPerformed());
        
        randomCasesButton.setSelected(true);
        firstCasesButton.setEnabled(false);
        lastCasesButton.setEnabled(false);
        randomCasesButton.setEnabled(false);
        typeSelectionButtonGroup.add(firstCasesButton);
        typeSelectionButtonGroup.add(randomCasesButton);
        typeSelectionButtonGroup.add(lastCasesButton);
        
        GroupLayout layout = new GroupLayout(splitSetPanel);
        splitSetPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                                        .addGroup(layout.createParallelGroup()
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addComponent(trainTextLabel, 100, 100, 100)
                                                                        .addGap(10)
                                                                        .addComponent(trainNumberSpinner, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(25, 25, 25)
                                                                        .addComponent(testTextLabel, 100, 100, 100)
                                                                        .addGap(10)
                                                                        .addComponent(testNumberSpinner, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                                                        .addContainerGap())
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addComponent(totalTextLabel, 100, 100, 100)
                                                                        .addGap(10)
                                                                        .addComponent(totalNumberText, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(25, 25, 25)
                                                                        .addComponent(testComboLabel, 100, 100, 100)
                                                                        .addGap(10)
                                                                        .addComponent(testFractionSpinner, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                                                        .addContainerGap())
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addGroup(layout.createParallelGroup()
                                                                                        .addComponent(typeSelectionLabel)
                                                                                        .addComponent(firstCasesButton)
                                                                                        .addComponent(randomCasesButton)
                                                                                        .addComponent(lastCasesButton)))));
        layout.setVerticalGroup(layout.createParallelGroup()
                                      .addGroup(layout.createSequentialGroup()
                                                      .addGap(5)
                                                      .addGroup(layout.createParallelGroup()
                                                                      .addComponent(totalTextLabel)
                                                                      .addComponent(totalNumberText, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                                                                      .addComponent(testComboLabel)
                                                                      .addComponent(testFractionSpinner, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
                                                      //.add(layout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                                      .addGap(5)
                                                      .addGroup(layout.createParallelGroup()
                                                                      .addComponent(testTextLabel)
                                                                      .addComponent(testNumberSpinner, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                                                                      .addComponent(trainTextLabel)
                                                                      .addComponent(trainNumberSpinner, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
                                                      .addGap(5)
                                                      .addGroup(layout.createSequentialGroup()
                                                                      .addComponent(typeSelectionLabel)
                                                                      .addComponent(firstCasesButton, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                                                                      .addComponent(randomCasesButton, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                                                                      .addComponent(lastCasesButton, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))));
        mainPanel.add(splitSetPanel);
        
        this.add(mainPanel);
        // ok_button disactived. To be enable its necesary select the database
        getOKButton().setEnabled(false);
        setSize(new Dimension(560, 330));
    }
    
    /**
     * This method change the values of foreground components when a casedatabase is loaded
     */
    private void enableOptions() {
        if (numCases != 0) {
            testFractionSpinner.setEnabled(true);
            double fraction = testFractionSpinner.getCurrentValue();
            double numTestDouble = (numCases * fraction) / 100.;
            int numTest = (int) (numTestDouble);
            int numTrain = numCases - numTest;
            testNumberSpinner.setCurrentValue(numTest);
            trainNumberSpinner.setCurrentValue(numTrain);
            totalNumberText.setText(((Integer) numCases).toString());
            testNumberSpinner.setEnabled(true);
            trainNumberSpinner.setEnabled(true);
            firstCasesButton.setEnabled(true);
            randomCasesButton.setEnabled(true);
            lastCasesButton.setEnabled(true);
            getOKButton().setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(null,
                                          "There are not cases in file",
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * this method is the testFractionComboBox Listener
     */
    private void testFractionComboBoxActionPerformed() {
        float fraction = testFractionSpinner.getCurrentValue();
        float numTestDouble = (numCases * fraction) / 100.0f;
        int numTest = (int) (numTestDouble);
        int numTrain = numCases - numTest;
        setNumOfTestsTrainsAndFraction(numTest, numTrain, fraction);
    }
    
    private void setNumOfTestsTrainsAndFraction(int numTest, int numTrain, float fraction) {
        testNumberSpinner.executeWithoutListeners(() -> testNumberSpinner.setCurrentValue(numTest));
        trainNumberSpinner.executeWithoutListeners(() -> trainNumberSpinner.setCurrentValue(numTrain));
        testFractionSpinner.executeWithoutListeners(() -> testFractionSpinner.setCurrentValue(fraction));
    }
    
    /**
     * this method is the trainNumber foreground Listener.
     */
    private void trainNumberTextActionPerformed() {
        int numTrain = trainNumberSpinner.getCurrentValue();
        int numTest = numCases - numTrain;
        float fraction = (100.0f * numTest / numCases);
        setNumOfTestsTrainsAndFraction(numTest, numTrain, fraction);
    }
    
    /**
     * this method is the testNumber Listener
     */
    private void testNumberTextActionPerformed() {
        int numTest = testNumberSpinner.getCurrentValue();
        int numTrain = numCases - numTest;
        float fraction = (100.0f * numTest / numCases);
        setNumOfTestsTrainsAndFraction(numTest, numTrain, fraction);
    }
    
    @Override protected boolean doOkClickBeforeHide() throws Exception {
        SplitSetManager splitSetManager = new SplitSetManager(this.dbOpenerPanel.getDatabase());
        int numTest = testNumberSpinner.getCurrentValue();
        int numTrain = trainNumberSpinner.getCurrentValue();
        double fraction = 100. * (numTest) / numCases;
        String title = "Division of the dataset " + this.dbOpenerPanel.getDatabaseFile()
                                                                  .getAbsolutePath() + " with " + numCases +
                " cases into " + numTrain + " training-cases and " + numTest + " test-cases\n" +
                "Test sampling fraction=" + String.format("%3.2f", fraction) + "%.\n";
        if (randomCasesButton.isSelected()) {
            sets = splitSetManager.generateRandomTestSet(numTest);
            sets.setTitle(title + "Random selected.\n");
        } else {
            if (firstCasesButton.isSelected()) {
                sets = splitSetManager.generateFirstTestSet(numTest);
                sets.setTitle(title + "First selected.\n");
            } else {
                sets = splitSetManager.generateLastTestSet(numTest);
                sets.setTitle(title + "Last selected.\n");
            }
        }
        CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
        
        
        String extension = FilenameUtils.getExtension(this.dbOpenerPanel.getDatabaseFile().getName());
        String name = FilenameUtils.getBaseName(this.dbOpenerPanel.getDatabaseFile().getName());
        File path = this.dbOpenerPanel.getDatabaseFile().getAbsoluteFile();
        
        this.testWriter.setSelectedFile(new File(path.getParentFile(), name + " - Test." + extension));
        if (this.testWriter.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        var testFile = this.testWriter.getSelectedFile();
        
        this.trainWriter.setSelectedFile(new File(path.getParentFile(), name + " - Train." + extension));
        if (this.trainWriter.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        var trainFile = this.trainWriter.getSelectedFile();
        
        caseDbManager.getWriter(FilenameUtils.getExtension(testFile.getName())).save(testFile, sets.getTestDatabase());
        caseDbManager.getWriter(FilenameUtils.getExtension(trainFile.getName()))
                     .save(trainFile, sets.getTrainDatabase());
        
        JOptionPane.showMessageDialog(null,
                                      "The train and test datasets have been saved.",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
        new ResultsDialog(owner, sets).setVisible(true);
        this.setVisible(false);
        return super.doOkClickBeforeHide();
    }
    
}