/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.pc.gui;

import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.pc.PCAlgorithm;
import org.openmarkov.learning.algorithm.pc.independencetester.ANMCausalDirectionTester;
import org.openmarkov.learning.algorithm.pc.independencetester.AdaptativeTester;
import org.openmarkov.learning.algorithm.pc.independencetester.CausalDirectionTester;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.algorithm.pc.independencetester.G2IndependenceTester;
import org.openmarkov.learning.algorithm.pc.independencetester.IndependenceTester;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

/**
 * Dialog showing the options and parameters of the PC algorithm.
 *
 * @author joliva
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = PCAlgorithm.class)
public class PCParametersDialog extends AlgorithmParametersDialog {

    /** Localization key suffixes for each tester, in display order (first = default). */
    public static final String[] independenceTesters = {"CrossEntropy", "G2", "Adaptive"};
    private String independenceTester = independenceTesters[0];
    private String significanceLevel = "0.05";
    private boolean useANM = true;
    private final JTextField significanceLevelText;
    private final JComboBox<String> independenceTesterComboBox;
    private final JCheckBox useANMCheckBox;

    public PCParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);

        JLabel testerLabel = new JLabel(stringDatabase.getString("Learning.PC.IndependenceTest"));
        independenceTesterComboBox = new JComboBox<>();
        for (String testerKey : independenceTesters) {
            independenceTesterComboBox.addItem(stringDatabase.getString("Learning.PC." + testerKey));
        }

        JLabel sigLabel = new JLabel(stringDatabase.getString("Learning.PC.SignificanceLevel") + ":");
        sigLabel.setToolTipText(stringDatabase.getString("Learning.PC.SignificanceLevel.Tooltip"));
        significanceLevelText = new JTextField(significanceLevel, 5);

        useANMCheckBox = new JCheckBox("Use ANM causal direction test (phase 3)");
        useANMCheckBox.setSelected(true);
        useANMCheckBox.setToolTipText("Orient remaining undirected links using the Additive Noise Model instead of an arbitrary acyclic order");

        initStandardLayout("Learning.PC.Title",
                new FieldRow(testerLabel, independenceTesterComboBox),
                new FieldRow(sigLabel, significanceLevelText));

        // Add ANM checkbox below the main panel
        GroupLayout layout = (GroupLayout) getContentPane().getLayout();
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addComponent(mainPanel, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addComponent(useANMCheckBox)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addComponent(mainPanel, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(useANMCheckBox)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    @Override
    protected void acceptCustomFields() {
        Double degreeAcc;
        try {
            degreeAcc = Double.parseDouble(significanceLevelText.getText());
        } catch (NumberFormatException e) {
            degreeAcc = null;
        }
        if (degreeAcc == null || degreeAcc < 0 || degreeAcc > 1) {
            throw new InvalidArgumentException(degreeAcc, "degreeAcc", "must between 0 and 1");
        }
        independenceTester = independenceTesterComboBox.getSelectedItem().toString();
        significanceLevel = significanceLevelText.getText();
        useANM = useANMCheckBox.isSelected();
    }

    @Override public String getDescription() {
        return stringDatabase.getString("Learning.PC.IndependenceTest.Short") + ": " + independenceTesterComboBox
                .getSelectedItem() + "\r\n" + stringDatabase.getString("Learning.PC.SignificanceLevel") + ": "
                + significanceLevel + "\r\n" + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter
                + "\r\nANM causal direction: " + useANM;
    }

    public String getIndependenceTester() {
        return independenceTester;
    }

    public String getDegreeOfAccuracy() {
        return significanceLevel;
    }

    @Override public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        CausalDirectionTester directionTester = useANM ? new ANMCausalDirectionTester() : null;
        return new PCAlgorithm(probNet, database, Double.parseDouble(alphaParameter),
                               buildSelectedTester(), Double.parseDouble(significanceLevel), directionTester);
    }

    private IndependenceTester buildSelectedTester() {
        return switch (independenceTesterComboBox.getSelectedIndex()) {
            case 1 -> new G2IndependenceTester();
            case 2 -> new AdaptativeTester();
            default -> new CrossEntropyIndependenceTester();
        };
    }
}
