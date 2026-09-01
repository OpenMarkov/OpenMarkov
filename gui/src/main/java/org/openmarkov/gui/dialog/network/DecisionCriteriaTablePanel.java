/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.DecisionCriteriaEdit;
import org.openmarkov.core.action.core.DecisionCriterionUnitEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.component.OMTableModel;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Table panel for editing the decision criteria (e.g. cost, effectiveness) of a network,
 * with support for standard criteria selection and per-criterion unit editing.
 */
@SuppressWarnings("serial") public class DecisionCriteriaTablePanel extends AdvancedPropertiesTablePanel {
    
    JButton standardCriteriaButton;
    private final ProbNet probNet;
    private final Window owner;
    /**
     * Each time an agent has been edited the corresponding edit would be stored
     */
    //private List<PNEdit> edits = new ArrayList<PNEdit>();
    
    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    public DecisionCriteriaTablePanel(String[] newColumns, ProbNet probNet, Window owner) {
        super(newColumns, new Object[0][0], StringDatabase.getUniqueInstance().
                                                          getString("NetworkAdvancedPanel.DecisionCriteria.ValuesTable.Columns.Id.Prefix"));
        
        this.probNet = probNet;
        this.owner = owner;
    }
    
    @Override public void tableChanged(TableModelEvent tableEvent) {
        int column = tableEvent.getColumn();
        int row = tableEvent.getLastRow();
        if (tableEvent.getType() != TableModelEvent.UPDATE) {
            return;
        }
        try {
            switch (column) {
                case 1 -> {
                    String newCriterionName = (String) ((OMTableModel) tableEvent.getSource())
                            .getValueAt(row, column);
                    new DecisionCriteriaEdit(probNet, StateAction.RENAME,
                                             probNet.getDecisionCriteria().get(row), newCriterionName).executeEdit();
                    
                }
                case 2 -> {
                    String oldCriterionName = (String) ((OMTableModel) tableEvent.getSource())
                            .getValueAt(row, column - 1);
                    String newUnitName = (String) ((OMTableModel) tableEvent.getSource())
                            .getValueAt(row, column);
                    new DecisionCriterionUnitEdit(probNet, oldCriterionName, newUnitName).executeEdit();
                }
                default -> {
                }
            }
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        String option = JOptionPane.showInputDialog(this, stringDatabase.getString("AddCriterion.Text"),
                                                    stringDatabase.getString("AddCriterion.Title"), JOptionPane.QUESTION_MESSAGE);
        if (option == null) {
            return;
        }
        DecisionCriteriaEdit criteriaEdit
                = new DecisionCriteriaEdit(probNet, StateAction.ADD, new Criterion(option), null);
        criteriaEdit.executeEdit();
        
        setDataFromCriteria(probNet.getDecisionCriteria());
        int newIndex = valuesTable.getRowCount() - 1;
        valuesTable.setRowSelectionInterval(newIndex, newIndex);
    }
    
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.REMOVE,
                                                                     probNet.getDecisionCriteria()
                                                                            .get(selectedRow), null);
        criteriaEdit.executeEdit();
        setDataFromCriteria(probNet.getDecisionCriteria());
    }
    
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.UP,
                                                                     probNet.getDecisionCriteria()
                                                                            .get(selectedRow), null);
        criteriaEdit.executeEdit();
        setDataFromCriteria(probNet.getDecisionCriteria());
    }
    
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.DOWN,
                                                                     probNet.getDecisionCriteria()
                                                                            .get(selectedRow), null);
        criteriaEdit.executeEdit();
        setDataFromCriteria(probNet.getDecisionCriteria());
    }
    
    /*
    Fixing issue https://bitbucket.org/cisiad/org.openmarkov.issues/issue/221/button-delete-in-node-properties-parents
    The remove button was always set to disabled, unless more than two parents were present
    We need to override the method from KeyTablePanel
    as in it we are not able to determine in which panel we are located and thus
    if the button needs to be enabled or not.
     */
    @Override public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        
        // If there are two criteria, one can be deleted
        if (valuesTable.getRowCount() == 2) {
            removeValueButton.setEnabled(true);
        }
    }
    
    /**
     * This method initializes buttonPanel.
     *
     * @return a new button panel.
     */
    @Override protected JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setName("DiscretizeTablePanel.buttonPanel");
            final GroupLayout groupLayout = new GroupLayout(buttonPanel);
            groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
                    groupLayout.createSequentialGroup()
                               .addGroup(
                                       groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                  .addComponent(getStandardCriteriaButton(), GroupLayout.DEFAULT_SIZE, 55,
                                                                Short.MAX_VALUE)
                                                  .addComponent(getAddValueButton(), GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                                                  .addComponent(getDownValueButton(), GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                                                  .addComponent(getUpValueButton(), GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                                                  .addComponent(getRemoveValueButton(), GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                               .addContainerGap()));
            groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                    groupLayout.createSequentialGroup().addComponent(getStandardCriteriaButton()).addGap(5, 5, 5)
                               .addComponent(getAddValueButton()).addGap(5, 5, 5).addComponent(getRemoveValueButton())
                               .addGap(5, 5, 5).addComponent(getUpValueButton()).addGap(5, 5, 5)
                               .addComponent(getDownValueButton()).addGap(5, 5, 5).addGap(48, 48, 48)));
            buttonPanel.setLayout(groupLayout);
        }
        return buttonPanel;
    }
    
    private Component getStandardCriteriaButton() {
        if (standardCriteriaButton == null) {
            standardCriteriaButton = new JButton();
            standardCriteriaButton.setName("KeyTablePanel.standardDomainButton");
            standardCriteriaButton.setText(StringDatabase.getUniqueInstance().getString("StandardCriteria.Text"));
            standardCriteriaButton.setVisible(true);
            standardCriteriaButton.setEnabled(true);
            standardCriteriaButton.addActionListener(new ActionListener() {
                
                @Override public void actionPerformed(ActionEvent e) {
                    StandardCriteriaDialog dialog = new StandardCriteriaDialog(owner, probNet);
                    if (dialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
                        setDataFromCriteria(probNet.getDecisionCriteria());
                    }
                    
                }
            });
        }
        return standardCriteriaButton;
    }
    
}
