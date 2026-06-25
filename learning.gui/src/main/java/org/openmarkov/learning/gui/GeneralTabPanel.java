/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.gui;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Panel for the "General" tab in the Learning dialog.
 * Creates and lays out all widgets; action listeners are wired by LearningDialog.
 */
class GeneralTabPanel extends JPanel {

    final JTextPane caseFileTextPane;
    final JLabel classVariableLabel;
    final JComboBox<Variable> variableListCombobox;
    final JComboBox<Class<? extends LearningAlgorithm>> algorithmComboBox;
    final ButtonGroup algorithmTypeGroup;
    final JRadioButton generativeAlgorithmType;
    final JRadioButton discriminativeAlgorithmType;
    final JPanel jPanel3;
    final JTextArea optionsTextArea;
    final JButton optionsButton;
    final JRadioButton interactiveLearningRadioButton;
    final JRadioButton automaticLearningRadioButton;
    final ButtonGroup learningTypeButtonGroup;
    final JButton loadCaseFileButton;

    GeneralTabPanel(StringDatabase stringDatabase) {
        super();

        // --- jPanel1: Case File ---
        JPanel jPanel1 = new JPanel();
        JScrollPane jScrollPane2 = new JScrollPane();
        caseFileTextPane = new JTextPane();
        loadCaseFileButton = new JButton();
        JLabel jLabel1 = new JLabel();

        jPanel1.setBorder(BorderFactory.createTitledBorder(stringDatabase.getString("Learning.CaseFile")));
        caseFileTextPane.setEditable(false);
        caseFileTextPane.setEnabled(false);
        jScrollPane2.setViewportView(caseFileTextPane);
        loadCaseFileButton.setText(stringDatabase.getString("Learning.Open"));
        loadCaseFileButton.setMnemonic(KeyEvent.VK_O);

        jLabel1.setText(stringDatabase.getString("Learning.CaseFile"));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);

        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                      .add(jPanel1Layout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400,
                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                        .add(25, 25, 25)
                                                                        .add(loadCaseFileButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90,
                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jPanel1Layout.createSequentialGroup().addContainerGap()
                                                                      .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                        .add(jLabel1)
                                                                                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22,
                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                        .add(25, 25, 25)
                                                                                        .add(loadCaseFileButton))
                                                                      .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        // --- jPanel3: Algorithm Selection ---
        jPanel3 = new JPanel();
        classVariableLabel = new JLabel();
        variableListCombobox = new JComboBox<>();
        variableListCombobox.setRenderer(new JComboBoxFunctionRender<>(Variable::getName));
        algorithmComboBox = new JComboBox<>();
        algorithmComboBox.setRenderer(new JComboBoxFunctionRender<Class<? extends LearningAlgorithm>>(
                algorithmClass -> LearningAlgorithmManager.info(algorithmClass).name()));
        algorithmTypeGroup = new ButtonGroup();
        generativeAlgorithmType = new JRadioButton();
        discriminativeAlgorithmType = new JRadioButton();
        JPanel jPanel6 = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        optionsTextArea = new JTextArea();
        optionsButton = new JButton();

        optionsTextArea.setEditable(false);

        jPanel3.setBorder(BorderFactory.createTitledBorder(stringDatabase.getString("Learning.Algorithm")));

        generativeAlgorithmType.setText(stringDatabase.getString("Learning.Algorithm.Type.Generative"));
        discriminativeAlgorithmType.setText(stringDatabase.getString("Learning.Algorithm.Type.Discriminative"));
        generativeAlgorithmType.setSelected(true);
        algorithmTypeGroup.add(generativeAlgorithmType);
        algorithmTypeGroup.add(discriminativeAlgorithmType);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 0, Short.MAX_VALUE));
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 0, Short.MAX_VALUE));

        optionsTextArea.setColumns(20);
        optionsTextArea.setRows(4);
        jScrollPane1.setViewportView(optionsTextArea);

        optionsButton.setText(stringDatabase.getString("Learning.Options"));
        optionsButton.setMnemonic(KeyEvent.VK_P);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);

        variableListCombobox.setVisible(false);
        classVariableLabel.setText(stringDatabase.getString("Learning.Variable.Class"));
        classVariableLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jPanel3Layout.createSequentialGroup().addContainerGap()
                                               .add(jPanel6, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

                             .add(jPanel3Layout.createSequentialGroup().add(33, 33, 33)
                                               .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                 .add(jPanel3Layout.createSequentialGroup()
                                                                                   .add(generativeAlgorithmType)
                                                                                   .add(discriminativeAlgorithmType)
                                                                                   .add(40, 40, 40)

                                                                                   .add(0, 0, Short.MAX_VALUE))
                                                                 .add(jScrollPane1)).add(18, 18, 18)
                                               .add(optionsButton)
                                               .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                             )
                             .add(jPanel3Layout.createSequentialGroup().add(33, 33, 33)
                                               .add(algorithmComboBox, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                               .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                             )
                             .add(jPanel3Layout.createSequentialGroup().add(33, 33, 33)
                                               .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                 .add(jPanel3Layout.createSequentialGroup()
                                                                                   .add(classVariableLabel)
                                                                                   .add(10, 10, 10)
                                                                                   .add(variableListCombobox, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                                                                   .add(0, 0, Short.MAX_VALUE))))
        );


        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jPanel3Layout.createSequentialGroup()
                                                                      .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                        .add(generativeAlgorithmType)
                                                                                        .add(discriminativeAlgorithmType)
                                                                      )
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(algorithmComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                        .add(classVariableLabel)
                                                                                        .add(variableListCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                                      .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                        .add(optionsButton)
                                                                                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                      .add(22, 22, 22)
                                                                      .add(jPanel6, GroupLayout.PREFERRED_SIZE,
                                                                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                                                                      .addContainerGap()
                                                    ));

        jPanel3.setLayout(jPanel3Layout);

        // --- jPanel7: Learning Type ---
        JPanel jPanel7 = new JPanel();
        learningTypeButtonGroup = new ButtonGroup();
        interactiveLearningRadioButton = new JRadioButton();
        automaticLearningRadioButton = new JRadioButton();

        jPanel7.setBorder(
                BorderFactory.createTitledBorder(stringDatabase.getString("Learning.LearningType")));

        learningTypeButtonGroup.add(interactiveLearningRadioButton);
        interactiveLearningRadioButton.setText(stringDatabase.getString("Learning.Interactive"));

        learningTypeButtonGroup.add(automaticLearningRadioButton);
        automaticLearningRadioButton.setText(stringDatabase.getString("Learning.Automatic"));
        interactiveLearningRadioButton.setSelected(true);

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                      .add(jPanel7Layout.createSequentialGroup().add(33, 33, 33)
                                                                        .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                          .add(automaticLearningRadioButton)
                                                                                          .add(interactiveLearningRadioButton))
                                                                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jPanel7Layout.createSequentialGroup()
                                                                      .add(interactiveLearningRadioButton)
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(automaticLearningRadioButton)
                                                                      .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        // --- GeneralTabPanel layout ---
        org.jdesktop.layout.GroupLayout generalPanelLayout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
                generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                  .add(generalPanelLayout.createSequentialGroup()
                                                         .addContainerGap()
                                                         .add(generalPanelLayout
                                                                      .createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                      .add(jPanel3, GroupLayout.PREFERRED_SIZE,
                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                      .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                      .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                         .addContainerGap()));
        generalPanelLayout.setVerticalGroup(
                generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                  .add(generalPanelLayout.createSequentialGroup()
                                                         .addContainerGap()
                                                         .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                         .add(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                         .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                         .add(50, 50, 50)));
    }
}
