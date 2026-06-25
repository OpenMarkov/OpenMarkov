/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.gui.action.NetworkAgentEdit;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Table panel for editing the list of agents defined in a network, supporting
 * add, remove, rename, and reorder operations.
 */
@SuppressWarnings("serial") public class NetworkAgentsTablePanel extends AdvancedPropertiesTablePanel {
    
    private final ProbNet probNet;
    /**
     * Each time an agent has been edited the corresponding edit would be stored
     */
    private final ArrayList<PNEdit> edits = new ArrayList<PNEdit>();
    
    public NetworkAgentsTablePanel(String[] newColumns, ProbNet probNet) {
        super(newColumns, new Object[0][0], "a");
        this.probNet = probNet;
        
    }
    
    @Override public void tableChanged(TableModelEvent tableEvent) {
        int column = tableEvent.getColumn();
        int row = tableEvent.getLastRow();
        if (tableEvent.getType() == TableModelEvent.UPDATE) {
            String agentName = (String) dataTable[row][0];
            String newName = (String) ((AdvancedPropertiesTableModel) tableEvent.getSource()).
                    getValueAt(row, column);
            dataTable[row][0] = newName;
            if (agentName != newName) {
                NetworkAgentEdit networkAgentEdit =
                        new NetworkAgentEdit(probNet, StateAction.RENAME, agentName, dataTable);
                try {
                    networkAgentEdit.executeEdit();
                    edits.add(networkAgentEdit);
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
                setData(dataTable);
                valuesTable.setRowSelectionInterval(row, row);
            }
        }
    }
    
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        
        String option = JOptionPane.showInputDialog(this, stringDatabase.getString("AddAgent.Text"),
                                                    stringDatabase.getString("AddAgent.Title"), JOptionPane.QUESTION_MESSAGE);
        
        if (option != null) {
            int newIndex = valuesTable.getRowCount();
            
            NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.ADD, option, null);
            //doEdit
            networkAgentEdit.executeEdit();
            edits.add(networkAgentEdit);

			
			/*getTableModel().insertRow(newIndex, new Object[] {getKeyString(newIndex), option });
			valuesTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);*/
            
            //StringsWithProperties agents = probNet.getAgents();
            //setDataFromNetworkAgents(agents);
            List<StringWithProperties> agents = probNet.getAgents();
            setDataFromAdvancedProperties(agents);
            // getTableModel().insertRow(newIndex, new Object[] {getKeyString(newIndex), option });
            valuesTable.setRowSelectionInterval(newIndex, newIndex);
            
            dataTable = new Object[valuesTable.getRowCount()][1];
            for (int i = 0; i < valuesTable.getRowCount(); i++) {
                dataTable[i][0] = valuesTable.getValueAt(i, 1, e.getSource());
            }
			/*getTableModel().insertRow(newIndex, new Object[] {getKeyString(newIndex), option });
			//valuesTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
			valuesTable.setValueAt(option, newIndex, 1);*/
        }
    }
    
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        String agentName = (String) valuesTable.getValueAt(selectedRow, 1, e.getSource());
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.REMOVE, agentName, null);
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        //StringsWithProperties agents = probNet.getAgents();
        List<StringWithProperties> agents = probNet.getAgents();
        setDataFromAdvancedProperties(agents);
        valuesTable.setRowSelectionInterval(selectedRow, selectedRow);
        //dataTable = new Object [agents.getNames().size()][1];
        if (agents != null) {
            dataTable = new Object[agents.size()][1];
            for (int i = 0; i < valuesTable.getRowCount(); i++) {
                dataTable[i][0] = valuesTable.getValueAt(i, 1, e.getSource());
            }
        }
    }
    
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        Object swap = dataTable[selectedRow][0];
        dataTable[selectedRow][0] = dataTable[selectedRow - 1][0];
        dataTable[selectedRow - 1][0] = swap;
        
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.UP, "", dataTable);
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        setData(dataTable);
			/*swap = valuesTable.getValueAt(selectedRow, 1);
			valuesTable.setValueAt(
				valuesTable.getValueAt(selectedRow - 1, 1), selectedRow, 1);
			valuesTable.setValueAt(swap, selectedRow - 1, 1);*/
        valuesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            dataTable[i][0] = valuesTable.getValueAt(i, 1, e.getSource());
        }
        
    }
    
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        Object swap = dataTable[selectedRow][0];
        dataTable[selectedRow][0] = dataTable[selectedRow + 1][0];
        dataTable[selectedRow + 1][0] = swap;
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.DOWN, "", dataTable);
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        setData(dataTable);
			/*swap = valuesTable.getValueAt(selectedRow, 1);
			valuesTable.setValueAt(
				valuesTable.getValueAt(selectedRow + 1, 1), selectedRow, 1);
			valuesTable.setValueAt(swap, selectedRow + 1, 1);*/
        valuesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            dataTable[i][0] = valuesTable.getValueAt(i, 1, e.getSource());
        }
    }
    
}
