/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.gui.action.RevelationIntervalEdit;

import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/******
 * This class implements a Discretize table for the edition of intervals. The intervals can be continuous or discontinuous.
 * @author caroline
 *
 */
public class RevelationArcDiscretizeTablePanel extends DiscretizeTablePanel {
    /****
     * Link for which the revelation conditions are stores.
     */
    private Link<Node> link;
    
    /**
     * default constructor
     *
     *
     */
    public RevelationArcDiscretizeTablePanel(String[] newColumns, Link<Node> link) {
        this(newColumns, new Object[0][0], "s", link.getFrom());
        this.link = link;
        
    }
    
    public RevelationArcDiscretizeTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix,
                                             Node node) {
        super(newColumns, noKeyData, newKeyPrefix, node);
        super.getDownValueButton().setVisible(false);
        super.getUpValueButton().setVisible(false);
        super.getNegativeInfinityButton().setVisible(false);
        super.getPositiveInfinityButton().setVisible(false);
        super.getStandardDomainButton().setVisible(false);
    }

    @Override public void setPartitionedInterval() {
        
        int subIntervals = 0;
        for (PartitionedInterval partitionInterval : link.getRevealingIntervals()) {
            subIntervals += partitionInterval.getNumSubintervals();
        }
        
        Object[][] allIntervalTable = new Object[subIntervals][6];
        int accumulatedIndex = 0;
        for (PartitionedInterval partitionInterval : link.getRevealingIntervals()) {
            Object[][] intervalTable = partitionInterval.convertToTableFormat();
            for (int i = 0; i < intervalTable.length; i++) {
                System.arraycopy(intervalTable[i], 0, allIntervalTable[i + accumulatedIndex], 0,
                                 intervalTable[0].length);
            }
            accumulatedIndex += intervalTable.length;
        }
        setData(allIntervalTable);
    }
    
    /**
     * Invoked when the button 'add' is pressed.
     */
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException {
        int rowCount = valuesTable.getRowCount();
        int newIndex = valuesTable.getRowCount();
        RevelationIntervalEdit revelationArcStateEdit =
                new RevelationIntervalEdit(link, StateAction.ADD, newIndex, 0, false);
        revelationArcStateEdit.executeEdit();
        setPartitionedInterval();
        valuesTable.setRowSelectionInterval(rowCount, rowCount);
    }
    
    /**
     * Invoked when the button 'remove' is pressed.
     */
    @Override protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        RevelationIntervalEdit revelationArcStateEdit =
                new RevelationIntervalEdit(link, StateAction.REMOVE, selectedRow, 0, false);
        revelationArcStateEdit.executeEdit();
        cancelCellEditing();
        setPartitionedInterval();
    }
    
    /****
     * Invoked when the table cells are edited
     */
    @Override public void tableChanged(TableModelEvent tableEvent) {
        int column = tableEvent.getColumn();
        int row = tableEvent.getLastRow();
        DiscretizeTableModel table = (DiscretizeTableModel) tableEvent.getSource();
        int numRows = table.getRowCount();
        boolean lower = (column - 1 == DiscretizeTablePanel.LOWER_BOUND_SYMBOL_COLUMN_INDEX);
        if (!(tableEvent.getType() == TableModelEvent.UPDATE && table
                .getValueAt(row, column) instanceof Double newValue)) {
            return;
        }
        if (lower) {
            double upperLimit = (Double) table.getValueAt(row, DiscretizeTablePanel.UPPER_BOUND_VALUE_COLUMN_INDEX);
            if (upperLimit < newValue) {
                throw new UnrecoverableException(new InvalidArgumentException(newValue, "New value", "it is greater than the upper limit (" + upperLimit + ")"));
            }
            if (row > 0) {
                double previousLimit = (Double) table.getValueAt(row - 1, DiscretizeTablePanel.UPPER_BOUND_VALUE_COLUMN_INDEX);
                if (previousLimit > newValue) {
                    throw new UnrecoverableException(new InvalidArgumentException(newValue, "New value", "it is lower than the previous bound's upper limit (" + previousLimit + ")"));
                }
            }
        } else {
            double lowerLimit = (Double) table.getValueAt(row, DiscretizeTablePanel.LOWER_BOUND_VALUE_COLUMN_INDEX);
            if (lowerLimit > newValue) {
                throw new UnrecoverableException(new InvalidArgumentException(newValue, "New value", "it is lower than the lower limit (" + lowerLimit + ")"));
            }
            if (row < numRows) {
                double nextLimit = (Double) table.getValueAt(row + 1, DiscretizeTablePanel.LOWER_BOUND_VALUE_COLUMN_INDEX);
                if (nextLimit < newValue) {
                    throw new UnrecoverableException(new InvalidArgumentException(newValue, "New value", "it is greater than the next bound's lower limit (" + nextLimit + ")"));
                }
            }
        }
        RevelationIntervalEdit nodePartitionedIntervalEdit
                = new RevelationIntervalEdit(link, StateAction.MODIFY_VALUE_INTERVAL, row, newValue, lower);
        try {
            nodePartitionedIntervalEdit.executeEdit();
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
        setPartitionedInterval();
    }
    
    @Override public void mouseClicked(MouseEvent e) {
        int row = valuesTable.rowAtPoint(e.getPoint(), e.getSource());
        int column = valuesTable.columnAtPoint(e.getPoint(), e.getSource());
        if (row == -1 && column == -1) {
            return;
        }
        try {
            changeIntervalDiscretize(row, column,e);
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        }
        
    }
    
    /****
     * Invoked when the interval delimiters are edited
     *  @param row the row
     * @param column the column
     * @param e
     */
    private void changeIntervalDiscretize(int row, int column, MouseEvent e) throws DoEditException {
        switch (column) {
            case LOWER_BOUND_VALUE_COLUMN_INDEX, UPPER_BOUND_VALUE_COLUMN_INDEX -> {
                double j = (Double) valuesTable.getValueAt(row, column, e.getSource());
                System.out.println(j);
                System.out.println("DiscretizeTablePanel.changeIntervalDiscretize");
                System.out.println(">> check here the values of the interval with the other intervals");
            }
            case LOWER_BOUND_SYMBOL_COLUMN_INDEX, UPPER_BOUND_SYMBOL_COLUMN_INDEX -> {
                boolean lower = column == LOWER_BOUND_SYMBOL_COLUMN_INDEX;
                String aux = (String) valuesTable.getValueAt(row, column, e.getSource());
                RevelationIntervalEdit relatedIntervalEdit = null;
                switch (column) {
                    case LOWER_BOUND_SYMBOL_COLUMN_INDEX -> {
                        if (aux.equals("(")) {
                            valuesTable.setValueAt("[", row, column, e.getSource());
                            
                            if (row > 0) {
                                Double lowerLimit = (Double) valuesTable.getValueAt(row, LOWER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                Double upperLimit = (Double) valuesTable.getValueAt(row - 1, UPPER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                if (lowerLimit.equals(upperLimit))
                                    valuesTable.setValueAt(")", row - 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX, e.getSource());
                                relatedIntervalEdit = new RevelationIntervalEdit(link, StateAction.MODIFY_DELIMITER_INTERVAL,
                                                                                 row - 1, 0, false);
                            }
                            // checkIntervalDiscretize("[", row, columna,upMonotony);
                        } else {
                            valuesTable.setValueAt("(", row, column, e.getSource());
                            if (row > 0) {
                                Double lowerLimit = (Double) valuesTable.getValueAt(row, LOWER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                Double upperLimit = (Double) valuesTable.getValueAt(row - 1, UPPER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                if (lowerLimit.equals(upperLimit))
                                    valuesTable.setValueAt("]", row - 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX, e.getSource());
                                relatedIntervalEdit = new RevelationIntervalEdit(link, StateAction.MODIFY_DELIMITER_INTERVAL,
                                                                                 row - 1, 0, false);
                            }
                            // checkIntervalDiscretize("(", row, columna,upMonotony);
                        }
                    }
                    case UPPER_BOUND_SYMBOL_COLUMN_INDEX -> {
                        if (aux.equals(")")) {
                            valuesTable.setValueAt("]", row, column, e.getSource());
                            if (row < valuesTable.getRowCount() - 1) {
                                Double lowerLimit = (Double) valuesTable.getValueAt(row, UPPER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                Double upperLimit = (Double) valuesTable.getValueAt(row + 1, LOWER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                if (lowerLimit.equals(upperLimit))
                                    valuesTable.setValueAt("(", row + 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX, e.getSource());
                                relatedIntervalEdit = new RevelationIntervalEdit(link, StateAction.MODIFY_DELIMITER_INTERVAL,
                                                                                 row + 1, 0, true);
                            }
                            // checkIntervalDiscretize("]", row, columna,upMonotony);
                        } else {
                            valuesTable.setValueAt(")", row, column, e.getSource());
                            if (row < valuesTable.getRowCount() - 1) {
                                Double lowerLimit = (Double) valuesTable.getValueAt(row, UPPER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                Double upperLimit = (Double) valuesTable.getValueAt(row + 1, LOWER_BOUND_VALUE_COLUMN_INDEX, e.getSource());
                                if (lowerLimit.equals(upperLimit))
                                    valuesTable.setValueAt("[", row + 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX, e.getSource());
                                relatedIntervalEdit = new RevelationIntervalEdit(link, StateAction.MODIFY_DELIMITER_INTERVAL,
                                                                                 row + 1, 0, true);
                            }
                            // checkIntervalDiscretize(")", row, columna,upMonotony);
                        }
                    }
                }
                
                RevelationIntervalEdit intervalEdit = new RevelationIntervalEdit(link, StateAction.MODIFY_DELIMITER_INTERVAL,
                                                                                 row, 0, lower);
                
                intervalEdit.executeEdit();
                if (relatedIntervalEdit != null) {
                    relatedIntervalEdit.executeEdit();
                }
            }
            
        }
        
    }
    
}
