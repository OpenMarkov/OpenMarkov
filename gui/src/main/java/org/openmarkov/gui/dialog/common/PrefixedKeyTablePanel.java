/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * This class implements a key table with the following features:
 * <ul>
 * <li>Its elements, except the first column, are modifiable.</li>
 * <li>New elements can be added, creating a new key row with empty data.</li>
 * <li>The key data (first column) consist of a key string following of the
 * index of the row.</li>
 * <li>The information of a row (except the first column) can be taken up or
 * down.</li>
 * <li>The rows can be removed.</li>
 * </ul>
 *
 * @author jmendoza
 * @author myebra
 * @version 1.0 jmendoza
 */
public class PrefixedKeyTablePanel extends KeyTablePanel implements TableModelListener {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 8550762264755243008L;
    
    /**
     * Key prefix.
     */
    private final String keyPrefix;
    
    private Node node;
    
    private boolean renameAction = true;
    
    /*
     * this a default constructor with no construction parameters
     */
    public PrefixedKeyTablePanel() {
        keyPrefix = "";
        initialize();
        getValuesTable().getModel().addTableModelListener(this);
    }
    
    /**
     * This is the default constructor
     *
     * @param newColumns   array of texts that appear in the header of the columns.
     * @param noKeyData    content of the cells except the first column.
     * @param newKeyPrefix prefix of the keys of each row that appear in the first
     *                     column.
     */
    public PrefixedKeyTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix,
                                 boolean firstColumnHidden) {// , ElementObservable notifier) {
        super(newColumns, new Object[0][0], true, true);// , notifier);
        keyPrefix = newKeyPrefix;
        initialize();
        getValuesTable().getModel().addTableModelListener(this);
        getValuesTable().setFirstColumnHidden(firstColumnHidden);
        setData(noKeyData);
    }
    
    public PrefixedKeyTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix,
                                 boolean firstColumnHidden, Node node) {
        super(newColumns, new Object[0][0], true, true);// , notifier);
        this.node = node;
        keyPrefix = newKeyPrefix;
        initialize();
        getValuesTable().setFirstColumnHidden(firstColumnHidden);
        setData(noKeyData);
    }
    
    /**
     * This method takes a data object and creates a new column that content a
     * row key. This key begins with the key prefix following a number that
     * starts at 0.
     *
     * @param oldData data to add a key column.
     *
     * @return a data object with one more column that contains the keys.
     */
    private Object[][] fillDataKeys(Object[][] oldData) {
        
        Object[][] newData;
        int i1;
        int i2;
        int l2;
        
        int l1 = oldData.length;
        if (l1 > 0) {
            l2 = oldData[0].length + 1;
            newData = new Object[l1][l2];
            for (i1 = 0; i1 < l1; i1++) {
                newData[i1][0] = getKeyString(i1);
                for (i2 = 1; i2 < l2; i2++) {
                    newData[i1][i2] = oldData[i1][i2 - 1];
                }
            }
            
            return newData;
        }
        
        return new Object[0][0];
        
    }
    
    /**
     * Returns a key represented by an index.
     *
     * @param index index of the key which will be returned
     *
     * @return the string that content the key.
     */
    private String getKeyString(int index) {
        
        return keyPrefix + index;
        
    }
    
    /**
     * Invoked when the button 'add' is pressed.
     */
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        // TODO warning esto afecta a la tabla de propiedades adicionales
        String option = JOptionPane.showInputDialog(this, stringDatabase.getString("AddState.Message"),
                                                    stringDatabase.getString("AddState.Title"), JOptionPane.QUESTION_MESSAGE);
        if (option == null) {
            return;
        }
        int newIndex = valuesTable.getRowCount();
        NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.ADD, newIndex, option);
        ProbNet probNet = node.getProbNet();
        nodeStateEdit.executeEdit();
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        propagateNodeStateEditRelatedVariables(StateAction.ADD, newIndex, option);
        //
        renameAction = false;
        tableModel.insertRow(0, new Object[]{getKeyString(newIndex), option});
        valuesTable.setRowSelectionInterval(0, 0);
        renameAction = false;
    }
    
    /**
     * Invoked when the button 'remove' is pressed.
     */
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        int rowCount;
        NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.REMOVE, selectedRow, "");
        ProbNet probNet = node.getProbNet();
        nodeStateEdit.executeEdit();
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        propagateNodeStateEditRelatedVariables(StateAction.REMOVE, selectedRow, "");
        //
        cancelCellEditing();
        renameAction = false;
        tableModel.removeRow(selectedRow);
        rowCount = valuesTable.getRowCount();
        if (rowCount > 0) {
            if (selectedRow < rowCount) {
                valuesTable.setRowSelectionInterval(selectedRow, selectedRow);
                while (selectedRow < rowCount) {
                    renameAction = false;
                    tableModel.setValueAt(getKeyString(selectedRow), selectedRow, 0);
                    selectedRow++;
                }
            } else {
                valuesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            }
        }
        renameAction = false;
    }
    
    /**
     * Invoked when the button 'up' is pressed.
     */
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        Object swap;
        NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.UP, selectedRow, "");
        ProbNet probNet = node.getProbNet();
        nodeStateEdit.executeEdit();
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        propagateNodeStateEditRelatedVariables(StateAction.UP, selectedRow, "");
        //
        stopCellEditing();
        swap = valuesTable.getValueAt(selectedRow, 1, e.getSource());
        renameAction = false;
        valuesTable.setValueAt(valuesTable.getValueAt(selectedRow - 1, 1, e.getSource()), selectedRow, 1, e.getSource());
        renameAction = false;
        valuesTable.setValueAt(swap, selectedRow - 1, 1, e.getSource());
        valuesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        renameAction = false;
    }
    
    /**
     * Invoked when the button 'down' is pressed.
     */
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        Object swap;
        NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.DOWN, selectedRow, "");
        ProbNet probNet = node.getProbNet();
        nodeStateEdit.executeEdit();
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        propagateNodeStateEditRelatedVariables(StateAction.DOWN, selectedRow, "");
        //
        stopCellEditing();
        swap = valuesTable.getValueAt(selectedRow, 1, e.getSource());
        renameAction = false;
        valuesTable.setValueAt(valuesTable.getValueAt(selectedRow + 1, 1, e.getSource()), selectedRow, 1, e.getSource());
        renameAction = false;
        valuesTable.setValueAt(swap, selectedRow + 1, 1, e.getSource());
        valuesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        renameAction = false;
    }
    
    /**
     * Returns the content of the table except the first column. This column
     * contains the keys generated automatically by this class and is only used
     * to display it not to manage it.
     *
     * @return the content of the table except the first column.
     */
    @Override public Object[][] getData() {
        
        Object[][] content = super.getData();
        Object[][] result;
        int rowCount = content.length;
        int columnCount;
        int i;
        int j;
        
        if (rowCount > 0) {
            columnCount = content[0].length;
            result = new Object[rowCount][columnCount - 1];
            for (i = 0; i < rowCount; i++) {
                for (j = 1; j < columnCount; j++) {
                    result[i][j - 1] = content[i][j];
                }
            }
        } else {
            result = new Object[0][0];
        }
        
        return result;
        
    }
    
    /**
     * Sets a new table model with new data.
     *
     * @param newData new data for the table without the key column.
     */
    @Override public void setData(Object[][] newData) {
        
        data = fillDataKeys(newData);
        tableModel = null;
        valuesTable.setModel(getTableModel());
        tableModel.addTableModelListener(this);
        
    }
    
    @Override public void tableChanged(TableModelEvent e) {
        int row = e.getLastRow();
        
        if (e.getType() != TableModelEvent.DELETE && e.getType() != TableModelEvent.INSERT && renameAction) {
            Object value = ((DefaultTableModel) e.getSource()).getValueAt(row, e.getColumn());
            String newName = value.toString();
            
            NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.RENAME, row, newName);
            ProbNet probNet = node.getProbNet();
            try {
                nodeStateEdit.executeEdit();
            } catch (DoEditException ex) {
                throw new UnrecoverableException(ex);
            }
            // @ 2014/11/18. Issue 145.
            // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
            // Propagation of the domain in related variables in temporal models
            try {
                propagateNodeStateEditRelatedVariables(StateAction.RENAME, row, newName);
            } catch (DoEditException ex) {
                throw new UnrecoverableException(ex);
            }
            //
        }
        renameAction = true;
    }
    
    // @ 2014/11/18. Issue 145.
    // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
    // Propagation of the domain in related variables in temporal models
    private void propagateNodeStateEditRelatedVariables(StateAction stateAction, int selectedRow, String option) throws DoEditException {
        // First we get the nodes in the same time slide as the node currently being edited
        List<Node> nodeRelatedNodes = TemporalNetOperations.getRelatedNodesOtherTimeSlices(node);
        // We create a variable to store the edit of the related node
        NodeStateEdit nodeStateEdit;
        // We iterate the related nodes, if any
        if (nodeRelatedNodes == null || nodeRelatedNodes.isEmpty()) {
            return;
        }
        for (Node relatedNode : nodeRelatedNodes) {
            // we create the edit for the realted node
            nodeStateEdit = new NodeStateEdit(relatedNode, stateAction, selectedRow, option);
            // and we perform the edit
            ProbNet probNet = relatedNode.getProbNet();
            nodeStateEdit.executeEdit();
        }
    }
}
