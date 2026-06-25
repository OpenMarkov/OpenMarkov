/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.component.OMTableModel;
import org.openmarkov.gui.exception.ThereIsNoNodeInDataException;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * This panel contains a table whose first column represents a key data.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo Add support for i18N by having setName() property to
 * all components Change attributes to protected to allow extension
 */
public class KeyTablePanel extends JPanel implements ActionListener, ListSelectionListener {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 6257314234781632512L;
    
    /**
     * Panel to scroll the table.
     */
    protected JScrollPane valuesTableScrollPane = null;
    
    /**
     * Table where show the values.
     */
    protected KeyTable valuesTable = null;
    
    /**
     * Model table.
     */
    protected OMTableModel tableModel = null;
    
    /**
     * Panel of buttons.
     */
    protected JPanel buttonPanel = null;
    
    /**
     * Name of the columns of the table.
     */
    protected String[] columns = null;
    
    /**
     * Data of the cells.
     */
    protected Object[][] data = null;
    
    /**
     * This variable enables the buttons to reorder the elements of the table.
     */
    protected final boolean reorderable;
    /**
     * Button to bring one value up.
     */
    protected JButton upValueButton = null;
    /**
     * Button to bring one value down.
     */
    protected JButton downValueButton = null;
    /**
     * Button to add a new value.
     */
    protected JButton addValueButton = null;
    /**
     * Button to delete an existing value.
     */
    protected JButton removeValueButton = null;
    /**
     * String Database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * Indicates if the data of the table is modifiable.
     */
    private final boolean modifiable;
    /**
     * Indicates if the header is shown
     */
    private final boolean showHeader;
    
    /**
     * this is a default constructor with no construction parameters
     */
    public KeyTablePanel() {
        this(null, null, false, false, false);
    }
    
    /**
     * This is the default constructor
     *
     * @param columns     array of texts that appear in the header of the columns.
     * @param data        content of the cells.
     * @param reorderable if true, the elements of the table can be reordered.
     * @param modifiable  if true, the cells of the table (except the first) are
     *                    modifiable.
     */
    public KeyTablePanel(String[] columns, Object[][] data, boolean reorderable, boolean modifiable,
                         boolean showHeader) {
        this.columns = columns.clone();
        this.data = data.clone();
        this.reorderable = reorderable;
        this.modifiable = modifiable;
        this.showHeader = showHeader;
    }
    
    public KeyTablePanel(String[] columns, Object[][] data, boolean reorderable, boolean modifiable) {
        this(columns, data, reorderable, modifiable, false);
    }
    
    /**
     * This method initializes this instance.
     */
    protected void initialize() {
        
        setBorder(new LineBorder(UIManager.getColor("Table.dropLineColor"), 1, false));
        
        final GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup()
                           .addContainerGap()
                           .addComponent(getValuesTableScrollPane(), GroupLayout.PREFERRED_SIZE, 406,
                                         GroupLayout.PREFERRED_SIZE)
                           .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(getButtonPanel(), GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(
                                                        groupLayout.createSequentialGroup()
                                                                   .addComponent(getValuesTableScrollPane(), GroupLayout.DEFAULT_SIZE, /* 274 */
                                                                                 280, Short.MAX_VALUE)
                                                                   .addGap(24, 24, 24))
                                                .addGroup(GroupLayout.Alignment.LEADING,
                                                          groupLayout.createSequentialGroup()
                                                                     .addComponent(getButtonPanel(), GroupLayout.DEFAULT_SIZE, /* 262 */
                                                                                   270, Short.MAX_VALUE)
                                                                     .addContainerGap()));
        setLayout(groupLayout);
    }
    
    /**
     * This method initializes valuesTableScrollPane.
     *
     * @return a new values table scroll pane.
     */
    protected JScrollPane getValuesTableScrollPane() {
        
        if (valuesTableScrollPane == null) {
            valuesTableScrollPane = new JScrollPane(getValuesTable());
            valuesTableScrollPane.setName("KeyTablePanel.valuesTableScrollPane");
        }
        return valuesTableScrollPane;
    }
    
    /**
     * This method initializes valuesTable.
     *
     * @return a new values table.
     */
    public KeyTable getValuesTable() {
        if (valuesTable == null) {
            valuesTable = new KeyTable(getTableModel(), modifiable, true, showHeader);
            valuesTable.setName("KeyTablePanel.valuesTable");
            valuesTable.onTables(omjTable -> omjTable.setListSelectionListener(this));
        }
        return valuesTable;
    }
    
    /**
     * This method initializes tableModel.
     *
     * @return a new tableModel.
     */
    protected OMTableModel getTableModel() {
        if (tableModel == null) {
            tableModel = new OMTableModel(data, columns, true);
        }
        return tableModel;
    }
    
    /**
     * This method initializes buttonPanel.
     *
     * @return a new button panel.
     */
    protected JPanel getButtonPanel() {
        
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setName("KeyTablePanel.buttonPanel");
            final GroupLayout groupLayout = new GroupLayout(buttonPanel);
            groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
                    groupLayout.createSequentialGroup()
                               .addGroup(
                                       groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                  .addComponent(getAddValueButton(), GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                                  .addComponent(getDownValueButton(), GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                                  .addComponent(getUpValueButton(), GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                                  .addComponent(getRemoveValueButton(), GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE))
                               .addContainerGap()));
            groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                    groupLayout.createSequentialGroup().addComponent(getAddValueButton()).addGap(5, 5, 5)
                               .addComponent(getRemoveValueButton()).addGap(5, 5, 5).addComponent(getUpValueButton())
                               .addGap(5, 5, 5).addComponent(getDownValueButton()).addGap(88, 88, 88)));
            buttonPanel.setLayout(groupLayout);
        }
        return buttonPanel;
    }
    
    /**
     * This method initializes upValueButton.
     *
     * @return a new up value button.
     */
    protected JButton getUpValueButton() {
        
        if (upValueButton == null) {
            upValueButton = new JButton();
            upValueButton.setName("KeyTablePanel.upValueButton");
            upValueButton.setText(stringDatabase.getString("Up.Text"));
            upValueButton.setMnemonic(stringDatabase.getString("Up.Text.Mnemonic").charAt(0));
            upValueButton.setIcon(IconBind.ARROW_UP_ENABLED.icon());
            upValueButton.setVisible(reorderable);
            upValueButton.setEnabled(false);
            upValueButton.addActionListener(this);
        }
        return upValueButton;
    }
    
    /**
     * This method initializes downValueButton.
     *
     * @return a new down value button.
     */
    protected JButton getDownValueButton() {
        
        if (downValueButton == null) {
            downValueButton = new JButton();
            downValueButton.setName("KeyTablePanel.downValueButton");
            downValueButton.setText(stringDatabase.getString("Down.Text"));
            downValueButton.setMnemonic(stringDatabase.getString("Down.Text.Mnemonic").charAt(0));
            downValueButton.setIcon(IconBind.ARROW_DOWN_ENABLED.icon());
            downValueButton.setVisible(reorderable);
            downValueButton.setEnabled(false);
            downValueButton.addActionListener(this);
        }
        return downValueButton;
    }
    
    /**
     * This method initializes addValueButton.
     *
     * @return a new add value button.
     */
    protected JButton getAddValueButton() {
        
        if (addValueButton == null) {
            addValueButton = new JButton();
            addValueButton.setName("KeyTablePanel.addValueButton");
            addValueButton.setText(stringDatabase.getString("Add.Text"));
            addValueButton.setMnemonic(stringDatabase.getString("Add.Text.Mnemonic").charAt(0));
            addValueButton.setIcon(IconBind.PLUS_ENABLED.icon());
            addValueButton.addActionListener(this);
        }
        return addValueButton;
    }
    
    /**
     * Enables or disabled the AddValue button.
     *
     * @param enabled if true, it will be enabled; otherwise, disabled.
     */
    public void setEnabledAddValue(boolean enabled) {
        addValueButton.setEnabled(enabled);
    }
    
    public void setEnabledRemoveValue(boolean enabled) {
        removeValueButton.setEnabled(enabled);
    }
    
    public void setEnabledDownValue(boolean enabled) {
        downValueButton.setEnabled(enabled);
    }
    
    public void setEnabledUpValue(boolean enabled) {
        upValueButton.setEnabled(enabled);
    }
    
    public void setVisibleAddValue(boolean visible) {
        addValueButton.setVisible(visible);
    }
    
    public void setVisibleRemoveValue(boolean visible) {
        removeValueButton.setVisible(visible);
    }
    
    public void setVisibleDownValue(boolean visible) {
        downValueButton.setVisible(visible);
    }
    
    public void setVisibleUpValue(boolean visible) {
        upValueButton.setVisible(visible);
    }
    
    /**
     * This method initializes removeValueButton.
     *
     * @return a new delete value button.
     */
    protected JButton getRemoveValueButton() {
        
        if (removeValueButton == null) {
            removeValueButton = new JButton();
            removeValueButton.setName("KeyTablePanel.removeValueButton");
            removeValueButton.setText(stringDatabase.getString("Delete.Text"));
            removeValueButton.setMnemonic(stringDatabase.getString("Delete.Text.Mnemonic").charAt(0));
            removeValueButton.setIcon(IconBind.MINUS_ENABLED.icon());
            removeValueButton.setEnabled(false);
            removeValueButton.addActionListener(this);
        }
        return removeValueButton;
    }
    
    /**
     * Invoked when an action occurs.
     *
     * @param e event information.
     */
    @Override public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();
            if (source == null) {
                return;
            }
            if (source.equals(addValueButton)) {
                actionPerformedAddValue(e);
            } else if (source.equals(removeValueButton)) {
                actionPerformedRemoveValue(e);
            } else if (source.equals(upValueButton)) {
                actionPerformedUpValue(e);
            } else if (source.equals(downValueButton)) {
                actionPerformedDownValue(e);
            }
        } catch (DoEditException | ThereIsNoNodeInDataException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    /**
     * Invoked when the button 'add' is pressed.
     */
    protected void actionPerformedAddValue(ActionEvent e) throws DoEditException, ThereIsNoNodeInDataException {
        System.out.println();
    }
    
    /**
     * Invoked when the button 'remove' is pressed.
     */
    protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRowIndex = valuesTable.getSelectedRow();
        tableModel.removeRow(selectedRowIndex);
    }
    
    /**
     * Invoked when the button 'up' is pressed.
     */
    protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        int selectedRowIndex = valuesTable.getSelectedRow();
        tableModel.moveRow(selectedRowIndex, selectedRowIndex, selectedRowIndex - 1);
        valuesTable.setRowSelectionInterval(selectedRowIndex - 1, selectedRowIndex - 1);
    }
    
    /**
     * Invoked when the button 'down' is pressed.
     */
    protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        int selectedRowIndex = valuesTable.getSelectedRow();
        tableModel.moveRow(selectedRowIndex, selectedRowIndex, selectedRowIndex + 1);
        valuesTable.setRowSelectionInterval(selectedRowIndex + 1, selectedRowIndex + 1);
    }
    
    /**
     * Invoked when the row selection changes.
     *
     * @param e selection event information.
     */
    @Override public void valueChanged(ListSelectionEvent e) {
        
        int index = valuesTable.getSelectedRow();
        int rowCount = valuesTable.getRowCount();
        if ((rowCount == 0) || (index == -1)) {
            removeValueButton.setEnabled(false);
            upValueButton.setEnabled(false);
            downValueButton.setEnabled(false);
        } else {
            removeValueButton.setEnabled(true);
            if (index == 0) {
                upValueButton.setEnabled(false);
                downValueButton.setEnabled(index != (rowCount - 1));
            } else if (index == (valuesTable.getRowCount() - 1)) {
                downValueButton.setEnabled(false);
                upValueButton.setEnabled(true);
                upValueButton.setEnabled(true);
            } else {
                upValueButton.setEnabled(true);
                downValueButton.setEnabled(true);
            }
        }
        removeValueButton.setEnabled(rowCount > 2);
    }
    
    /**
     * Cancels the editing in any cell of the table, avoiding its new value to
     * be recorded.
     */
    public void cancelCellEditing() {
        valuesTable.cancelCellEditing();
    }
    
    /**
     * Stops the editing in any cell of the table, recording the new value.
     */
    public void stopCellEditing() {
        getValuesTable().stopCellEditing();
    }
    
    /**
     * Returns the content of the table.
     *
     * @return the content of the table.
     */
    @SuppressWarnings("rawtypes") public Object[][] getData() {
        
        DefaultTableModel model = (DefaultTableModel) valuesTable.getModel();
        int columnCount = model.getColumnCount();
        int rowCount = model.getRowCount();
        int i;
        int j;
        Object[][] datatmp = new Object[rowCount][columnCount];
        Vector vectorData = model.getDataVector();
        Vector vectorRow;
        
        for (i = 0; i < rowCount; i++) {
            vectorRow = (Vector) vectorData.get(i);
            for (j = 0; j < columnCount; j++) {
                datatmp[i][j] = vectorRow.get(j);
            }
        }
        return datatmp;
    }
    
    // ESCA-JAVA0173:
    
    /**
     * Sets a new table model with new data.
     *
     * @param newData new data for the table.
     */
    public void setData(Object[][] newData) {
        tableModel.setDataVector(newData, columns);
    }
    
}
