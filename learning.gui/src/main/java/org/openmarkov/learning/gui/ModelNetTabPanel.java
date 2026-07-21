/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.gui;

import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Panel for the "Model Network" tab in the Learning dialog.
 * Creates and lays out all widgets; action listeners are wired by LearningDialog.
 */
class ModelNetTabPanel extends JPanel {

    final JCheckBox addLinkModelNet;
    final JCheckBox deleteLinksModelNet;
    final JRadioButton fromFileRadioButton;
    final JCheckBox invertLinksModelNet;
    final JButton loadModelNetButton;
    final JTextPane modelNetTextPane;
    final JRadioButton noModelNetRadioButton;
    final JCheckBox useNodePositionsCheckBox;
    final JCheckBox startFromModelNetCheckBox;
    final JRadioButton fromOpenMarkovRadioButton;

    ModelNetTabPanel(StringDatabase stringDatabase) {
        super();

        // --- jPanel2: Choose Model Net ---
        JPanel jPanel2 = new JPanel();
        noModelNetRadioButton = new JRadioButton();
        fromFileRadioButton = new JRadioButton();
        fromOpenMarkovRadioButton = new JRadioButton();
        JScrollPane jScrollPane3 = new JScrollPane();
        modelNetTextPane = new JTextPane();
        JLabel jLabel2 = new JLabel();
        loadModelNetButton = new JButton();

        jPanel2.setBorder(
                BorderFactory.createTitledBorder(stringDatabase.getString("Learning.ChooseModelNet")));

        noModelNetRadioButton.setSelected(true);
        noModelNetRadioButton.setText(stringDatabase.getString("Learning.DontUseModelNet"));

        fromFileRadioButton.setText(stringDatabase.getString("Learning.LoadModelNetFromFile"));

        fromOpenMarkovRadioButton.setText(stringDatabase.getString("Learning.TakeOpenModelNet"));

        modelNetTextPane.setEditable(false);
        modelNetTextPane.setEnabled(false);
        jScrollPane3.setViewportView(modelNetTextPane);

        jLabel2.setText(stringDatabase.getString("Learning.ModelNet") + ":");

        loadModelNetButton.setText(stringDatabase.getString("Learning.Open"));
        loadModelNetButton.setMnemonic(KeyEvent.VK_N);
        loadModelNetButton.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                      .add(jPanel2Layout.createSequentialGroup().addContainerGap()
                                                                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                          .add(jPanel2Layout.createSequentialGroup()
                                                                                                            .add(jLabel2)
                                                                                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                                                                            .add(jScrollPane3)
                                                                                                            .add(22, 22, 22))
                                                                                          .add(jPanel2Layout.createSequentialGroup()
                                                                                                            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                              .add(jPanel2Layout.createSequentialGroup()
                                                                                                                                                .add(fromFileRadioButton)
                                                                                                                                                .add(50, 50, 50)
                                                                                                                                                .add(loadModelNetButton,
                                                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88,
                                                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                                                                              .add(noModelNetRadioButton)
                                                                                                                              .add(fromOpenMarkovRadioButton))
                                                                                                            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                                                             Short.MAX_VALUE)))));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jPanel2Layout.createSequentialGroup()
                                                                      .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                                                        .add(jPanel2Layout.createSequentialGroup()
                                                                                                          .add(noModelNetRadioButton)
                                                                                                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                          .add(fromOpenMarkovRadioButton)
                                                                                                          .add(2, 2, 2)
                                                                                                          .add(fromFileRadioButton))
                                                                                        .add(loadModelNetButton))
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                                      .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                                                        .add(jLabel2)
                                                                                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22,
                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                      .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        // --- jPanel4: Model Net Use ---
        JPanel jPanel4 = new JPanel();
        addLinkModelNet = new JCheckBox();
        deleteLinksModelNet = new JCheckBox();
        invertLinksModelNet = new JCheckBox();
        useNodePositionsCheckBox = new JCheckBox();
        startFromModelNetCheckBox = new JCheckBox();

        jPanel4.setBorder(
                BorderFactory.createTitledBorder(stringDatabase.getString("Learning.ModelNetUse")));

        addLinkModelNet.setText(stringDatabase.getString("Learning.AllowLinkAddition"));
        addLinkModelNet.setEnabled(false);

        deleteLinksModelNet.setText(stringDatabase.getString("Learning.AllowLinkRemoval"));
        deleteLinksModelNet.setEnabled(false);

        invertLinksModelNet.setText(stringDatabase.getString("Learning.AllowLinkInversion"));
        invertLinksModelNet.setEnabled(false);

        useNodePositionsCheckBox.setText(stringDatabase.getString("Learning.ModelNetUseOnlyPositions"));
        useNodePositionsCheckBox.setEnabled(false);

        startFromModelNetCheckBox.setText(stringDatabase.getString("Learning.StartFromModelNet"));
        startFromModelNetCheckBox.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                      .add(jPanel4Layout.createSequentialGroup()
                                                                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                          .add(jPanel4Layout.createSequentialGroup()
                                                                                                            .add(19, 19, 19)
                                                                                                            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                              .add(useNodePositionsCheckBox)
                                                                                                                              .add(startFromModelNetCheckBox,
                                                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 244,
                                                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                                                                          .add(jPanel4Layout.createSequentialGroup()
                                                                                                            .add(59, 59, 59)
                                                                                                            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                                                              .add(deleteLinksModelNet)
                                                                                                                              .add(invertLinksModelNet)
                                                                                                                              .add(addLinkModelNet))))
                                                                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jPanel4Layout.createSequentialGroup()
                                                                      .add(useNodePositionsCheckBox)
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(startFromModelNetCheckBox)
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(addLinkModelNet)
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(deleteLinksModelNet)
                                                                      .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                      .add(invertLinksModelNet)
                                                                      .addContainerGap(17, Short.MAX_VALUE)));

        deleteLinksModelNet.getAccessibleContext().setAccessibleName("deleteCheckBox");

        // --- ModelNetTabPanel layout ---
        org.jdesktop.layout.GroupLayout modelNetPanelLayout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(modelNetPanelLayout);
        modelNetPanelLayout.setHorizontalGroup(
                modelNetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                   .add(modelNetPanelLayout.createSequentialGroup().addContainerGap()
                                                           .add(modelNetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                                   .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                   .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                           .addContainerGap()));
        modelNetPanelLayout.setVerticalGroup(
                modelNetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                   .add(modelNetPanelLayout.createSequentialGroup().addContainerGap()
                                                           .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                           .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                           .add(16, 16, 16)));
    }
}
