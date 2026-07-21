/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.component;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.UncertainValuesEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.action.TablePotentialValueEdit;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.common.KeyTable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.exception.MismatchedValueException;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.Serial;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Node Potentials (either in a general family or in a
 * canonical family potential). This table also shows the data in several ways,
 * depending upon the type of user selection:
 * <ul>
 * <Li>Probabilities or states values</li>
 * <li>Probabilistic or Deterministic values allowed</li>
 * <li>All parameters or Only independent parameters</li>
 * <li>TPC or canonical parameters(for the Canonical families)</li>
 * <li>Net or Compound values (for the Canonical families)</li>
 * </ul>
 *
 * @author jlgozalo
 * @author mpalacios
 * @version 3.0 - cmyago - May 2016 - eliminates the different treatment for the Utility nodes.
 * - eliminates the deterministic values
 */
public class ValuesTable extends KeyTable implements PNEditListener {
    
    /**
     * first editable Column
     */
    public static final int FIRST_EDITABLE_COLUMN = 1;
    /**
     * default serial ID
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * table model
     */
    private ValuesTableModel tableModel;
    /**
     * Boolean array with the rows and columns of the tableModel.
     * Each cell of the array is true if the data has been modified
     * boolean data model (to know if a value has been changed)
     */
    private boolean[][] dataModified = null;
    /**
     * Table Row Sorter/Filter
     */
    private TableRowSorter<ValuesTableModel> tableRowSorter = null;
    /**
     * type of node for this variable
     */
    protected NodeType nodeType = null;
    // default;
    /**
     * last editable row. By default, it is -1 until runtime initialisation
     */
    protected int lastEditableRow = -1;
    // Potential
    // by
    // default
    /**
     * String database
     */
    protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    protected Node node;
    /**
     * First potential of node
     */
    protected Potential potential = null;
    /**
     * True if the class of potential is ExactDistrPotential
     */
    private boolean isExactDistrPotential = false;
    /**
     * if getExactDistrPotential tablePotential=potential.getTablePotential, if !getExactDistrPotential tablePotential= (tablePotential)potential
     */
    protected TablePotential tablePotential = null;
    private ProbNet probNet;
    /**
     * Define the last column of the table that was modified
     */
    
    protected int lastCol = -1;
    /**
     * Define the priority list when potential values are edited
     */
    
    protected List<Integer> priorityList = new LinkedList<>();
    /**
     * first editable row. By default, it is zero until runtime initialisation
     */
    private int firstEditableRow = 0;
    
    
    /**
     * Default constructor
     *
     * @param node       - the node with the TablePotential or ExactDistrPotential
     * @param tableModel - the model of the TablePotential or ExactDistrPotential
     * @param modifiable - true if the table can be edited and modified
     */
    public ValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
        super(tableModel, modifiable, false, true);
        this.tableModel = tableModel;
        if (node != null) {
            node.getProbNet().getPNESupport().addListener(this);
            this.node = node;
            this.probNet = node.getProbNet();
            this.potential = node.getPotentials().getFirst();
            //Adding the initialisation of getExactDistrPotential
            
            this.isExactDistrPotential = (node.getPotentials().getFirst() instanceof ExactDistrPotential);
            if (this.potential instanceof ExactDistrPotential exactDistr) {
                this.tablePotential = exactDistr.getTablePotential();
            } else if (this.potential instanceof TablePotential table) {
                this.tablePotential = table;
            }
        }
        //
        if (modifiable) {
            int numRowsModel = tableModel.getRowCount();
            int numColumsModel = tableModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
            this.initializeDataModified(false);
        }
        this.canGenerateEditorWhen((row, column)-> row >= getFirstEditableRow() && (row <= getLastEditableRow() || getLastEditableRow() == -1) && column >= FIRST_EDITABLE_COLUMN);
        
    }
    
    @Override public void setValueAt(Object newValue, int row, int column, Object source) {
        Object oldValue = this.getValueAt(row, column, source);
        // The new value has to be transformed to double
        newValue = ValuesTable.resolveNewDouble(newValue, oldValue);
        if (oldValue == newValue || oldValue.equals(newValue)) {
            return;
        }
        // When is tablePotential, the value cannot be negative
        if (((Double) newValue) < 0 && !this.isExactDistrPotential) {
            throw new UnrecoverableException(new MismatchedValueException("a positive number", newValue));
        }
        //if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION)
        if (!this.isExactDistrPotential) {
            if (this.lastCol != column) {
                this.priorityList.clear();
                this.lastCol = column;
            }
            
        }
        
        // Chance, decision and utility
        
        try {
            new TablePotentialValueEdit(this.node, (Double) newValue, row, column, this.priorityList,
                                        this.getTableModel().getNotEditablePositions())
                    .executeEdit();
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        } catch (ThereIsNoPotentialsInNodeException e) {
            throw new UnreachableException(e);
        }
        
        // UNCLEAR Should it be here?
        // Sets the value in case of ExactDistrPotential
        if (this.isExactDistrPotential) {
            super.setValueAt(newValue, row, column, source);
        }
    }
    
    static Object resolveNewDouble(Object newValue, Object oldValue) {
        return switch (newValue) {
            case String newValueString when newValueString.isBlank() -> oldValue;
            case String newValueString -> {
                try {
                    yield Double.parseDouble(newValueString);
                } catch (NumberFormatException ex) {
                    throw new UnrecoverableException(new MismatchedValueException("a number", newValue));
                }
            }
            case Double newValueDouble -> newValueDouble;
            case null, default -> throw new UnrecoverableException(new MismatchedValueException("a number", newValue));
        };
    }
    
    /**
     * Sets a default id for the columns (Excel format)
     *
     * @param howManyColumns - number of columns of the table
     */
    public static String[] getColumnsIdsSpreadSheetStyle(int howManyColumns) {
        String[] columnsId = new String[howManyColumns];
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int columnPosition = 0; columnPosition < howManyColumns; columnPosition++) {
            String columnId = "";
            int firstLetterPosition = columnPosition % ALPHABET.length();
            int secondLetterPosition = columnPosition / ALPHABET.length() - 1;
            if (columnPosition >= (ALPHABET.length() * (ALPHABET.length() + 1))) {
            } else if (columnPosition >= ALPHABET.length()) {
                columnId = columnId + ALPHABET.charAt(secondLetterPosition) + ALPHABET
                        .charAt(firstLetterPosition);
            } else {
                columnId = columnId + ALPHABET.charAt(firstLetterPosition);
            }
            columnsId[columnPosition] = columnId;
        }
        return columnsId;
    }
    
    /**
     * This method returns the dataModified variable. If dataModified doesn't exist it is created
     *
     * @return dataModified
     *
     * @see #dataModified
     * revised--&gt; not changed
     */
    private boolean[][] getDataModified() {
        if (this.dataModified == null) {
            int numRowsModel = this.tableModel.getRowCount();
            int numColumsModel = this.tableModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
        }
        return this.dataModified;
    }
    
    /**
     * This method initialises all the cells of dataModified to the boolean value given by isModified
     *
     * @param isModified - initial value for the cells is dataModified
     *
     * @see #dataModified
     * revised--&gt; not changed
     */
    public void initializeDataModified(boolean isModified) {
        if (this.tableModel != null) {
            if (this.dataModified == null) {
                this.getDataModified();
            }
            for (int i = 0; i < this.tableModel.getRowCount(); i++) {
                for (int j = 0; j < this.tableModel.getColumnCount(); j++) {
                    this.dataModified[i][j] = isModified;
                }
            }
        }
    }
    
    /**
     * Default display configuration for this table
     */
    @Override protected void defaultConfiguration() {
        super.defaultConfiguration();
        this.setFirstColumnHidden(false); // key prefix column is hidden
        this.setShowColumnHeader(false); // no column header here
        
        this.onTables(omjTable -> {
            omjTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            omjTable.setRowSelectionAllowed(true);
            omjTable.setColumnSelectionAllowed(true);
            omjTable.setGridColor(GUIColors.Tables.ValuesTable.GRID_COLOR.getColor());
            omjTable.setDefaultRenderer(Double.class, new ValuesTableCellRenderer(this, firstEditableRow, null));
            omjTable.setDefaultRenderer(String.class, new ValuesTableCellRenderer(this, firstEditableRow, null));
            omjTable.setDefaultRenderer(Object.class, new ValuesTableCellRenderer(this, firstEditableRow, null));
            // next two lines is a cool trick to enhance table performance
            ToolTipManager.sharedInstance().unregisterComponent(omjTable);
            ToolTipManager.sharedInstance().unregisterComponent(omjTable.getTableHeader());
        });
        
    }
    
    /**
     * Resets the model in use
     * revised-&gt; not changed
     */
    public void resetModel() {
        this.tableModel = null;
        this.dataModified = null;
    }
    
    /**
     * Gets the tableModel attribute
     * revised--&gt;not changed
     */
    public ValuesTableModel getTableModel() {
        return this.tableModel;
    }
    
    /**
     * Sets the data model for this table to newModel and registers with it for
     * listener notifications from the new data model.
     *
     * @param newDataModel the new data source for this table.
     *
     * @throws IllegalArgumentException if newModel is null.
     *                                  <p>
     *                                  revised--&gt;not changed
     */
    public void setModel(ValuesTableModel newDataModel) throws IllegalArgumentException {
        super.setModel(newDataModel);
        this.tableModel = newDataModel;
        this.tableRowSorter = new TableRowSorter<>(((ValuesTableModel) this.getModel()));
        // not display the last row where the cells has states and not values
        // and it is only required when displaying states values
    }
    
    /**
     * @return the variable
     */
    private Variable getVariable() {
        return this.node.getVariable();
    }
    
    /**
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return this.nodeType;
    }
    
    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
    
    /**
     * @return the lastEditableRow
     */
    public int getLastEditableRow() {
        return this.lastEditableRow;
    }
    
    /**
     * @param lastEditableRow the lastEditableRow to set
     */
    public void setLastEditableRow(int lastEditableRow) {
        this.lastEditableRow = lastEditableRow;
    }
    
    /**
     * @return the firstEditableRow
     */
    public int getFirstEditableRow() {
        return this.firstEditableRow;
    }
    
    /**
     * @param firstEditableRow the firstEditableRow to set
     */
    public void setFirstEditableRow(int firstEditableRow) {
        this.firstEditableRow = firstEditableRow;
    }
    
    /**
     * Method to show/hide rows based on the showingAllParameters attribute
     * using a RowFilter mechanism.
     * <ul>
     * <li>If true, the table is shown completely with probabilities values
     * which means that there is no active row filter</li>
     * <li>If not, the row filter is set to show all rows except the one that
     * has the state name equals to the last state name.</li>
     * </ul>
     *
     * @param showingAllParameters if true, show all; if false, show only
     *                             independent parameters
     *                             <p>
     *                             revised--&gt;  minor changes
     */
    public void setShowingAllParameters(boolean showingAllParameters) {
        this.tableRowSorter = new TableRowSorter<>(((ValuesTableModel) this.getModel()));
        if (showingAllParameters) {
            this.onTables(omjTable -> omjTable.setRowSorter(null));
        } else {
            int lastRow = this.getModel().getRowCount() - 1 - 1;
            lastRow = (Math.max(lastRow, 0));
            Deque<RowFilter<Object, Object>> list = new LinkedList<>();
            list.add(RowFilter.notFilter(RowFilter.regexFilter((String) this.getModel().getValueAt(lastRow, 0), 0)));
            list.add(RowFilter.notFilter(RowFilter.regexFilter(this.getVariable().getName(), 0)));
            this.tableRowSorter.setRowFilter(RowFilter.andFilter(list));
            this.onTables(omjTable -> omjTable.setRowSorter(this.tableRowSorter));
        }
    }
    
    /**
     * print the NodePotentialTable
     */
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        if (edit instanceof TablePotentialValueEdit tpEdit) {
            this.tablePotentialValueEditHappened(tpEdit);
        } else if (edit instanceof UncertainValuesEdit uvEdit) {
            this.uncertainValuesEditHappened(uvEdit);
        }
    }
    
    /**
     * Updates the table if the edited column has uncertainty
     *
     * @param edit the edit
     */
    private void uncertainValuesEditHappened(UncertainValuesEdit edit) {
        int row;
        int positionInValues;
        boolean isChance = edit.isChanceVariable();
        List<Variable> varsPotential = this.tablePotential.getVariables();
        int numVarsPotential = varsPotential.size();
        int numParents = numVarsPotential - (isChance ? 1 : 0);
        int col = edit.getSelectedColumn();
        TableModel superModel = super.getModel();
        double[] values = this.tablePotential.getValues();
        int basePosition = edit.getBasePosition();
        if (isChance) {
            int numStates = varsPotential.getFirst().getNumStates();
            int startRow = numParents + (numStates - 1);
            for (int i = 0; i < numStates; i++) {
                row = startRow - i;
                positionInValues = basePosition + i;
                superModel.setValueAt(values[positionInValues], row, col);
            }
        } else {
            row = numParents;
            //positionInValues = col - 1;
            positionInValues = basePosition;
            superModel.setValueAt(values[positionInValues], row, col);
        }
    }
    
    /**
     * Sets the values in the edited column
     *
     * @param edit - context for changing the
     */
    private void tablePotentialValueEditHappened(TablePotentialValueEdit edit) {
        int position;
        TablePotential editPotential = edit.getPotential();
        if (!edit.getExactDistrPotential()) {
            this.priorityList = edit.getPriorityList();
            ListIterator<Integer> listIterator = this.priorityList.listIterator();
            double[] values = editPotential.getValues();
            while (listIterator.hasNext()) {
                position = listIterator.next();
                int rowPosition = edit.getRowPosition(position);
                int columnPosition = edit.getColumnPosition();
                super.getModel().setValueAt(values[position], rowPosition, columnPosition);
            }
        } else {
            position = edit.getColumnPosition() - 1;
            super.getModel()
                 .setValueAt(editPotential.getValues()[position], edit.getRowPosition(), edit.getColumnPosition());
        }
    }
    
    /**
     *
     */
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (edit instanceof TablePotentialValueEdit tpEdit) {
            TablePotential editPotential = tpEdit.getPotential();
            if (!tpEdit.getExactDistrPotential()) {
                this.priorityList = tpEdit.getPriorityList();
                for (Integer position : this.priorityList) {
                    super.getModel().setValueAt(editPotential.getValues()[position], tpEdit.getRowPosition(position),
                                                tpEdit.getColumnPosition());
                }
            } else {
                int position = tpEdit.getColumnPosition() - 1;
                super.getModel()
                     .setValueAt(editPotential.getValues()[position], tpEdit.getRowPosition(), tpEdit.getColumnPosition());
            }
        }
    }
    
    
    /**
     * This method sets the variable probNet to the node probNet
     *
     * @param node the node whose potential is being displayed
     */
    public void setData(Node node) {
        if (this.probNet.getPNESupport() != node.getProbNet().getPNESupport()) {
            this.probNet.getPNESupport().removeListener(this);
            node.getProbNet().getPNESupport().addListener(this);
        }
        this.probNet = node.getProbNet();
    }
    
    /**
     * Closes this object and prepare it for disposal
     */
    public void close() {
        this.probNet.getPNESupport().removeListener(this);
    }
    
    /**
     * Adjusts columns width to its content
     */
    public void fitColumnsWidthToContent() {
        this.onTables(omjTable -> {
            JTableHeader header = omjTable.getTableHeader();
            
            TableCellRenderer headerRenderer = null;
            
            if (header != null) {
                headerRenderer = header.getDefaultRenderer();
            }
            
            TableColumnModel columns = omjTable.getColumnModel();
            TableModel tableModel = omjTable.getModel();
            int margin = columns.getColumnMargin();
            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();
            
            for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                TableColumn column = columns.getColumn(columnIndex);
                column.setMinWidth(60);
                int width = -1;
                
                TableCellRenderer tableCellRenderer = column.getHeaderRenderer();
                
                if (tableCellRenderer == null) {
                    tableCellRenderer = headerRenderer;
                }
                
                if (tableCellRenderer != null) {
                    Component component = tableCellRenderer
                            .getTableCellRendererComponent(omjTable, column.getHeaderValue(), false, false, -1, columnIndex);
                    
                    width = component.getPreferredSize().width;
                }
                
                for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
                    TableCellRenderer cellRenderer = omjTable.getCellRenderer(rowIndex, columnIndex);
                    
                    Component c = cellRenderer
                            .getTableCellRendererComponent(omjTable, tableModel.getValueAt(rowIndex, columnIndex), false, false,
                                                           rowIndex, columnIndex);
                    
                    width = Math.max(width, c.getPreferredSize().width);
                }
                
                if (width >= 0) {
                    column.setMinWidth(width + margin);
                }
            }
        });
        
    }
    
}