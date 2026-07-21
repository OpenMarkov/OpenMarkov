/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.DecisionCriteriaEdit;
import org.openmarkov.core.action.core.DecisionCriterionUnitEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
        PNEdit criteriaEdit = null;
        String oldCriterionName = null;
        String oldUnitName = null;
        switch (column) {
            case 1 -> {
                oldCriterionName = (String) dataTable[row][column - 1];
                oldUnitName = (String) dataTable[row][column];
                String newCriterionName = (String) ((AdvancedPropertiesTableModel) tableEvent.getSource())
                        .getValueAt(row, column);
                dataTable[row][column - 1] = newCriterionName;
                if (oldCriterionName != newCriterionName) {
                    criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.RENAME,
                                                            probNet.getDecisionCriteria().get(row), newCriterionName);
                }
            }
            case 2 -> {
                oldCriterionName = (String) dataTable[row][column - 2];
                oldUnitName = (String) dataTable[row][column - 1];
                String newUnitName = (String) ((AdvancedPropertiesTableModel) tableEvent.getSource())
                        .getValueAt(row, column);
                dataTable[row][column - 1] = newUnitName;
                if (oldUnitName != newUnitName) {
                    criteriaEdit = new DecisionCriterionUnitEdit(probNet, oldCriterionName, newUnitName);
                }
            }
            default -> {
            }
        }
        if (criteriaEdit != null) {
            try {
                criteriaEdit.executeEdit();
            } catch (DoEditException e) {
                switch (column) {
                    case 1 -> {
                        dataTable[row][column - 1] = oldCriterionName;
                        this.valuesTable.setValueAt(oldCriterionName, row, column - 1, tableEvent.getSource());
                    }
                    case 2 -> {
                        dataTable[row][column - 1] = oldUnitName;
                        this.valuesTable.setValueAt(oldUnitName, row, column - 1, tableEvent.getSource());
                    }
                }
                throw new UnrecoverableException(e);
            }
        }
        setData(dataTable);
        valuesTable.setRowSelectionInterval(row, row);
    }
    
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        String option = JOptionPane.showInputDialog(this, stringDatabase.getString("AddCriterion.Text"),
                                                    stringDatabase.getString("AddCriterion.Title"), JOptionPane.QUESTION_MESSAGE);
        if (option == null) {
            return;
        }
        int newIndex = valuesTable.getRowCount();
        DecisionCriteriaEdit criteriaEdit
                = new DecisionCriteriaEdit(probNet, StateAction.ADD, new Criterion(option), null);
        criteriaEdit.executeEdit();
        //edits.add(criteriaEdit);
        
        /*
         * getTableModel().insertRow(newIndex, new Object[]
         * {getKeyString(newIndex), option });
         * valuesTable.getSelectionModel().setSelectionInterval(newIndex,
         * newIndex);
         */
        
        // StringsWithProperties agents = probNet.getAgents();
        // setDataFromNetworkAgents(agents);
        List<Criterion> criteria = probNet.getDecisionCriteria();
        setDataFromCriteria(criteria);
        // getTableModel().insertRow(newIndex, new Object[]
        // {getKeyString(newIndex), option });
        valuesTable.setRowSelectionInterval(newIndex, newIndex);
        
        dataTable = new Object[valuesTable.getRowCount()][2];
        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            dataTable[i][0] = valuesTable.getValueAt(i, 1, null);
            dataTable[i][1] = valuesTable.getValueAt(i, 2, null);
        }
        /*
         * getTableModel().insertRow(newIndex, new Object[]
         * {getKeyString(newIndex), option });
         * //valuesTable.getSelectionModel().setSelectionInterval(newIndex,
         * newIndex); valuesTable.setValueAt(option, newIndex, 1);
         */
    }
    
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        String criteriaName = (String) valuesTable.getValueAt(selectedRow, 1, null);
        
        DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.REMOVE,
                                                                     probNet.getDecisionCriteria()
                                                                            .get(selectedRow), null);
        criteriaEdit.executeEdit();
        //edits.add(criteriaEdit);
        // StringsWithProperties agents = probNet.getAgents();
        List<Criterion> criterias = probNet.getDecisionCriteria();
        setDataFromCriteria(criterias);
        valuesTable.setRowSelectionInterval(selectedRow, selectedRow);
        // dataTable = new Object [agents.getNames().size()][1];
        if (criterias != null) {
            dataTable = new Object[criterias.size()][2];
            for (int i = 0; i < valuesTable.getRowCount(); i++) {
                dataTable[i][0] = valuesTable.getValueAt(i, 1, null);
                dataTable[i][1] = valuesTable.getValueAt(i, 2, null);
            }
        }
    }
    
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        Object swapName = dataTable[selectedRow][0];
        dataTable[selectedRow][0] = dataTable[selectedRow - 1][0];
        dataTable[selectedRow - 1][0] = swapName;
        
        Object swapUnit = dataTable[selectedRow][1];
        dataTable[selectedRow][1] = dataTable[selectedRow - 1][1];
        dataTable[selectedRow - 1][1] = swapUnit;
        
        DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.UP,
                                                                     probNet.getDecisionCriteria()
                                                                            .get(selectedRow), null);
        criteriaEdit.executeEdit();
        //edits.add(criteriaEdit);
        setData(dataTable);
        /*
         * swap = valuesTable.getValueAt(selectedRow, 1);
         * valuesTable.setValueAt( valuesTable.getValueAt(selectedRow - 1,
         * 1), selectedRow, 1); valuesTable.setValueAt(swap, selectedRow -
         * 1, 1);
         */
        valuesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            dataTable[i][0] = valuesTable.getValueAt(i, 1, null);
            dataTable[i][1] = valuesTable.getValueAt(i, 2, null);
        }
        
    }
    
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        Object swapName = dataTable[selectedRow][0];
        dataTable[selectedRow][0] = dataTable[selectedRow + 1][0];
        dataTable[selectedRow + 1][0] = swapName;
        Object swapUnit = dataTable[selectedRow][1];
        dataTable[selectedRow][1] = dataTable[selectedRow + 1][1];
        dataTable[selectedRow + 1][1] = swapUnit;
        DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.DOWN,
                                                                     probNet.getDecisionCriteria()
                                                                            .get(selectedRow), null);
        criteriaEdit.executeEdit();
        //edits.add(criteriaEdit);
        setData(dataTable);
        /*
         * swap = valuesTable.getValueAt(selectedRow, 1);
         * valuesTable.setValueAt( valuesTable.getValueAt(selectedRow + 1,
         * 1), selectedRow, 1); valuesTable.setValueAt(swap, selectedRow +
         * 1, 1);
         */
        valuesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            dataTable[i][0] = valuesTable.getValueAt(i, 1, null);
            dataTable[i][1] = valuesTable.getValueAt(i, 2, null);
        }
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
