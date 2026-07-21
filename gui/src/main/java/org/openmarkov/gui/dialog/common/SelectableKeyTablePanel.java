/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.core.RevelationStateEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.gui.component.OMTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * This class implements a key table with a table model which renders the cells
 * according to the class type.
 ***/
public class SelectableKeyTablePanel extends PrefixedKeyTablePanel implements TableModelListener {
    /***
     * Preferred column width
     */
    private static final int CHECKBOX_COLUMN_WIDTH = 60;
    private static final int STATENAME_COLUMN_WIDTH = 440;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /***
     * Link containing the revelation conditions
     */
    private final Link<Node> link;
    /****
     * Node whose values are revealing
     */
    private final Node node;
    
    public SelectableKeyTablePanel(String[] newColumns, String newKeyPrefix,
                                   boolean firstColumnHidden, Link<Node> link) {
        super(newColumns, new Object[0][0], newKeyPrefix, firstColumnHidden);
        this.link = link;
        this.node = link.getFrom();
        super.getAddValueButton().setVisible(false);
        super.getRemoveValueButton().setVisible(false);
        super.getDownValueButton().setVisible(false);
        super.getUpValueButton().setVisible(false);
        
    }
    
    /****
     * Adjusts the column width. The checkbox column is thinner than the node's state column
     */
    public void adjustColumnSize() {
        getValuesTable().getColumn(1).setMaxWidth(CHECKBOX_COLUMN_WIDTH);
        getValuesTable().getColumn(1).setPreferredWidth(STATENAME_COLUMN_WIDTH);
    }
    
    /**
     * This method initializes tableModel.
     *
     * @return a new tableModel.
     */
    @Override protected OMTableModel getTableModel() {
        if (tableModel == null) {
            tableModel = new SelectableTableModel(data, columns);
        }
        return tableModel;
    }
    
    /**
     * Invoked when the row selection changes.
     *
     * @param e selection event information.
     */
    @Override public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        Object data = model.getValueAt(row, column);
        State[] states = node.getVariable().getStates();
        if (states.length > 0) {
            State selectedState = states[states.length - row - 1];
            RevelationStateEdit arcEdit = new RevelationStateEdit(link, selectedState, (Boolean) data);
            try {
                arcEdit.executeEdit();
            } catch (DoEditException ex) {
                throw new UnrecoverableException(ex);
            }
        }
    }
    
    static class SelectableTableModel extends OMTableModel {
        
        private static final long serialVersionUID = 4478294244055128574L;
        
        public SelectableTableModel(Object[][] data, String[] columns) {
            super(data, columns, true);
            
        }
        
        @Override public boolean isCellEditable(int row, int col) {
            return col == 1;
        }
        
        @Override public Class<?> getColumnClass(int c) {
            if (getRowCount() <= 0) {
                return Object.class;
            }
            return getValueAt(0, c).getClass();
        }
        
    }
}
