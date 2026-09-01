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
import org.openmarkov.gui.exception.AlreadyExistingAgentException;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        if (tableEvent.getType() == TableModelEvent.UPDATE) {
            int column = tableEvent.getColumn();
            int row = tableEvent.getLastRow();
            if (column == -1 || row == -1) {
                return;
            }
            
            String originalAgentName = probNet.getAgents().get(row).string;
            String newName = (String) ((TableModel) tableEvent.getSource()).getValueAt(row, column);
            var agentsNames = new HashSet<>(probNet.getAgents()
                                                   .stream()
                                                   .map(StringWithProperties::getString)
                                                   .collect(Collectors.toSet()));
            agentsNames.remove(originalAgentName);
            if (agentsNames.contains(newName)) {
                throw new UnrecoverableException(new AlreadyExistingAgentException(newName));
            }
            Object[][] dataTable = agentsArray();
            NetworkAgentEdit networkAgentEdit =
                    new NetworkAgentEdit(probNet, StateAction.RENAME, originalAgentName, dataTable);
            try {
                networkAgentEdit.executeEdit();
                edits.add(networkAgentEdit);
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
            setDataFromAdvancedProperties(probNet.getAgents());
            try {
                valuesTable.setRowSelectionInterval(row, row);
            } catch (IllegalArgumentException _) {
            
            }
        }
    }
    
    private Object[][] agentsArray() {
        List<Object[]> arr = new ArrayList<>();
        for (var row : valuesTable.getModel().getDataVector()) {
            arr.add(new Object[]{row.get(1)});
        }
        return arr.stream().toArray(Object[][]::new);
    }
    
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        var existingAgents = probNet.getAgents().stream().map(agent -> agent.string).collect(Collectors.toSet());
        int prependedIndex = 1;
        while (existingAgents.contains("Agent " + prependedIndex)) {
            prependedIndex++;
        }
        String option = "Agent " + prependedIndex;
        
        int newIndex = valuesTable.getRowCount();
        
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.ADD, option, null);
        //doEdit
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        
        setDataFromAdvancedProperties(probNet.getAgents());
        valuesTable.setRowSelectionInterval(newIndex, newIndex);
    }
    
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        String agentName = (String) valuesTable.getValueAt(selectedRow, 1, e.getSource());
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.REMOVE, agentName, null);
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        //StringsWithProperties agents = probNet.getAgents();
        setDataFromAdvancedProperties(probNet.getAgents());
        valuesTable.setRowSelectionInterval(selectedRow, selectedRow);
    }
    
    
    public static <T> void swap(T[] arr, int index1, int index2) {
        T temp = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = temp;
    }
    
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        var agents = agentsArray();
        swap(agents, selectedRow, selectedRow - 1);
        
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.UP, "", agents);
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        setDataFromAdvancedProperties(probNet.getAgents());
        valuesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
    }
    
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        var agents = agentsArray();
        swap(agents, selectedRow, selectedRow + 1);
        
        NetworkAgentEdit networkAgentEdit = new NetworkAgentEdit(probNet, StateAction.DOWN, "", agents);
        networkAgentEdit.executeEdit();
        edits.add(networkAgentEdit);
        setDataFromAdvancedProperties(probNet.getAgents());
        valuesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
    }
    
}
