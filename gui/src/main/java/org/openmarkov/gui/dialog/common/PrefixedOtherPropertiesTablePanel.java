/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.action.OtherPropertyEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.component.OMTableModel;


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
 * @version 1.0 jmendoza
 */
public class PrefixedOtherPropertiesTablePanel extends KeyTablePanel implements TableModelListener {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 8550762264755243008L;
    /**
     * Key prefix.
     */
    private final String keyPrefix;
    
    private Node node = null;
    
    private ProbNet probNet = null;
    
    
    /**
     * this a default constructor with no construction parameters
     */
    public PrefixedOtherPropertiesTablePanel() {
        keyPrefix = "";
        initialize();
    }
    
    /**
     * This is the default constructor
     *
     * @param newColumns   array of texts that appear in the header of the columns.
     * @param noKeyData    content of the cells except the first column.
     * @param newKeyPrefix prefix of the keys of each row that appear in the first
     *                     column.
     */
    public PrefixedOtherPropertiesTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix,
                                             boolean firstColumnHidden) {// , ElementObservable notifier) {
        super(newColumns, new Object[0][0], true, true);// , notifier);
        keyPrefix = newKeyPrefix;
        initialize();
        getValuesTable().setFirstColumnHidden(firstColumnHidden);
        setData(noKeyData);
        tableModel.addTableModelListener(this);
        //valuesTable.getModel().addTableModelListener(this);
        //getValuesTable().getModel().addTableModelListener(this);
        //getTableModel().addTableModelListener(this);
    }
    
    /**
     * This is the constructor when called from a Node Properties Dialog
     *
     * @param newColumns   array of texts that appear in the header of the columns.
     * @param noKeyData    content of the cells except the first column.
     * @param newKeyPrefix prefix of the keys of each row that appear in the first
     *                     column.
     */
    public PrefixedOtherPropertiesTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix,
                                             boolean firstColumnHidden, Node node) {// , ElementObservable notifier) {
        super(newColumns, new Object[0][0], true, true);// , notifier);
        this.node = node;
        keyPrefix = newKeyPrefix;
        initialize();
        getValuesTable().setFirstColumnHidden(firstColumnHidden);
        setData(noKeyData);
        getTableModel().addTableModelListener(this);
        
        //tableModel.addTableModelListener(this);
        ////valuesTable.getModel().addTableModelListener(this);
        ////getValuesTable().getModel().addTableModelListener(this);
        ////getTableModel().addTableModelListener(this);
    }
    
    /**
     * This is the constructor when called from a Network Properties Dialog
     *
     * @param newColumns   array of texts that appear in the header of the columns.
     * @param noKeyData    content of the cells except the first column.
     * @param newKeyPrefix prefix of the keys of each row that appear in the first
     *                     column.
     */
    public PrefixedOtherPropertiesTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix,
                                             boolean firstColumnHidden, ProbNet probNet) {// , ElementObservable notifier) {
        super(newColumns, new Object[0][0], true, true);// , notifier);
        this.probNet = probNet;
        keyPrefix = newKeyPrefix;
        initialize();
        getValuesTable().setFirstColumnHidden(firstColumnHidden);
        setData(noKeyData);
        getTableModel().addTableModelListener(this);
        
        //tableModel.addTableModelListener(this);
        ////valuesTable.getModel().addTableModelListener(this);
        ////getValuesTable().getModel().addTableModelListener(this);
        ////getTableModel().addTableModelListener(this);
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
    public String getKeyString(int index) {
        return keyPrefix + index;
    }
    
    /**
     * Sets a new table model with new data.
     *
     * @param noKeyData new data for the table without the key column.
     */
    @Override public void setData(Object[][] noKeyData) {
        ///@Override public void setData(Object[][] newData) {
        data = fillDataKeys(noKeyData);
        tableModel = new OMTableModel(data, columns, true);
        valuesTable.setModel(tableModel);
        valuesTable.getModel().addTableModelListener(this);
        
        
        //tableModel = null;
        ////tableModel.setDataVector(data, columns);
        //valuesTable.setModel(getTableModel());
        //// valuesTable.getModel().addTableModelListener(this);
    }
    
    
    /**
     * Set the node additionalProperties in this panel with the provided ones
     */
    public void setProperties(Node node) {
        this.node = node;
        Map<String, String> otherProperties = node.getAdditionalProperties();
        setFieldsFromProperties(otherProperties);
    }
    
    /**
     * Set the network additionalProperties in this panel with the provided ones
     */
    public void setProperties(ProbNet probNet) {
        this.probNet = probNet;
        Map<String, String> otherProperties = probNet.getAdditionalProperties();
        setFieldsFromProperties(otherProperties);
    }
    
    public void setProperties() {
        if (node != null) {
            Map<String, String> otherProperties = node.getAdditionalProperties();
            setFieldsFromProperties(otherProperties);
        } else if (probNet != null) {
            Map<String, String> otherProperties = probNet.getAdditionalProperties();
            setFieldsFromProperties(otherProperties);
        }
    }
    
    public void setFieldsFromProperties(Map<String, String> otherProperties) {
        Object[] keys = otherProperties.keySet().toArray();
        Object[] values = otherProperties.values().toArray();
        int s = otherProperties.size();
        Object[][] data = new String[s][2];
        for (int i = 0; i < s; i++) {
            data[i][0] = keys[i].toString();
            data[i][1] = values[i].toString();
        }
        
        setData(data);
        // getOtherPropertiesTablePanel()
        // .setData( additionalProperties.getOtherProperties() );
    }
    
    /**
     * Invoked when the button 'add' is pressed.
     */
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        Set<String> existingKeys = IntStream.range(0, tableModel.getRowCount())
                                            .mapToObj(i -> tableModel.getValueAt(i, 1))
                                            .map(String.class::cast)
                                            .collect(Collectors.toSet());
        String propertyName;
        int propertyNumberName = 1;
        while (true) {
            propertyName = stringDatabase.getString("AddOtherProperty.DefaultName") + " " + propertyNumberName;
            if (!existingKeys.contains(propertyName)) {
                break;
            }
            propertyNumberName += 1;
        }
        
        String propertyValue = stringDatabase.getString("AddOtherProperty.DefaultValue");
        int newIndex = valuesTable.getRowCount();
        String propertyID = getKeyString(newIndex);
        int selectedRowIndex = valuesTable.getSelectedRow();
        int rowCount = valuesTable.getRowCount();
        String[] rowData = {propertyID, propertyName, propertyValue};
        
        tableModel.addRow(rowData);    // Add row to the end of the model
        //tableModel.moveRow(rowCount, rowCount, selectedRowIndex + 1);
        //valuesTable.setRowSelectionInterval(selectedRowIndex + 1, selectedRowIndex + 1);
        valuesTable.setRowSelectionInterval(rowCount, rowCount);
        String[] noIDrowData = {propertyName, propertyValue};
        if (node != null) {
            new OtherPropertyEdit(node, "ADD", selectedRowIndex, noIDrowData).executeEdit();
        } else if (probNet != null) {
            new OtherPropertyEdit(probNet, "ADD", selectedRowIndex, noIDrowData).executeEdit();
        }
    }
    
    /**
     * Invoked when the button 'remove' is pressed.
     */
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRowIndex = valuesTable.getSelectedRow();
        tableModel.removeRow(selectedRowIndex);
        //ProbNet probNet = null;
        if (node != null) {
            OtherPropertyEdit otherPropertyEdit = new OtherPropertyEdit(node, "REMOVE", selectedRowIndex, null);
            ProbNet probNet1 = node.getProbNet();
            otherPropertyEdit.executeEdit();
            //probNet = node.getProbNet();
            //probNet.doEdit(otherPropertyEdit);
        } else if (this.probNet != null) {
            OtherPropertyEdit otherPropertyEdit = new OtherPropertyEdit(probNet, "REMOVE", selectedRowIndex, null);
            //probNet = this.probNet;
            otherPropertyEdit.executeEdit();
        }
    }
    
    /**
     * Invoked when the button 'up' is pressed.
     */
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRowIndex = valuesTable.getSelectedRow();
        tableModel.moveRow(selectedRowIndex, selectedRowIndex, selectedRowIndex - 1);
        valuesTable.setRowSelectionInterval(selectedRowIndex - 1, selectedRowIndex - 1);
        if (node != null) {
            OtherPropertyEdit otherPropertyEdit = new OtherPropertyEdit(node, "UP", selectedRowIndex, null);
            ProbNet probNet1 = node.getProbNet();
            otherPropertyEdit.executeEdit();
        } else if (probNet != null) {
            OtherPropertyEdit otherPropertyEdit = new OtherPropertyEdit(probNet, "UP", selectedRowIndex, null);
            otherPropertyEdit.executeEdit();
        }
    }
    
    /**
     * Invoked when the button 'down' is pressed.
     */
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRowIndex = valuesTable.getSelectedRow();
        tableModel.moveRow(selectedRowIndex, selectedRowIndex, selectedRowIndex + 1);
        valuesTable.setRowSelectionInterval(selectedRowIndex + 1, selectedRowIndex + 1);
        if (node != null) {
            OtherPropertyEdit otherPropertyEdit = new OtherPropertyEdit(node, "DOWN", selectedRowIndex, null);
            ProbNet probNet1 = node.getProbNet();
            otherPropertyEdit.executeEdit();
        } else if (probNet != null) {
            OtherPropertyEdit otherPropertyEdit = new OtherPropertyEdit(probNet, "DOWN", selectedRowIndex, null);
            otherPropertyEdit.executeEdit();
        }
    }
    
    /**
     * Invoked when the row selection changes.
     *
     * @param e selection event information.
     */
    @Override public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        int rowCount = valuesTable.getRowCount();
        removeValueButton.setEnabled(rowCount >= 1);
    }
    
    @Override public void tableChanged(TableModelEvent e) {
        int row = e.getLastRow();
        if (e.getType() != TableModelEvent.UPDATE || getValuesTable().getEditorComponent(e.getSource()) == null) {
            return;
        }
        String newName = ((DefaultTableModel) e.getSource()).getValueAt(row, 1).toString();
        String newValue = ((DefaultTableModel) e.getSource()).getValueAt(row, 2).toString();
        String[] rowData = {newName, newValue};
        try {
            if (node != null) {
                new OtherPropertyEdit(node, "RENAME", row, rowData).executeEdit();
            } else if (probNet != null) {
                new OtherPropertyEdit(probNet, "RENAME", row, rowData).executeEdit();
            }
        } catch (DoEditException e1) {
            Map<String, String> additionalProperties = node != null ? node.getAdditionalProperties() : probNet.getAdditionalProperties();
            String oldName = new ArrayList<>(additionalProperties.keySet()).get(row);
            String oldValue = additionalProperties.get(oldName);
            valuesTable.setValueAt(oldName, row, 1, e.getSource());
            valuesTable.setValueAt(oldValue, row, 2, e.getSource());
            throw new UnrecoverableException(e1);
        }
    }
}
