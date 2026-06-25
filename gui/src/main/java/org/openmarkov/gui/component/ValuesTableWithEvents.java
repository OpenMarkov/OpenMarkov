/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.component;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TableWithEvents;
import org.openmarkov.core.model.network.potential.TableWithFunctions;
import org.openmarkov.gui.action.EventTablePotentialValueEdit;
import org.openmarkov.gui.dialog.common.KeyTable;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoableEdit;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of <code>EventTablePotential</code>.
 * <p>
 * Transition class to be merged with the new structure of tables
 *
 * @author cyago
 * @version 2.0 - 29/08/2023 - cmyago; refactored to ValuesTableWithEvents (from EventValuesTable); impossible configurations commented
 *
 */
public class ValuesTableWithEvents extends ValuesTable {
    /**
     * first editable Column
     */
    public static final int FIRST_EDITABLE_COLUMN = 1;
    /**
     * default serial ID
     */
    private static final long serialVersionUID = 1L;
    /**
     * number of decimals positions to be used for calculations and display
     */
    protected static int decimalPositions = 2;                                  // by
    /**
     * table model
     */
    protected TableWithEventsModel tableWithEventsModel;
    /**
     * Boolean array with the rows and columns of the tableWithEventsModel.
     * Each cell of the array is true if the data has been modified
     * boolean data model (to know if a value has been changed)
     */
    protected boolean[][] dataModified = null;
    /**
     * Table Row Sorter/Filter
     */
    protected TableRowSorter<TableWithEventsModel> tableRowSorter = null;
    /**
     * type of node for this variable
     */
    protected NodeType nodeType = null;
    // default;
    /**
     * last editable row. By default, it is zero until runtime initialisation
     */
    protected int lastEditableRow = 0;
    /**
     * define if the table is using General or Canonical Potentials
     * <ul>
     * <li>if index = 0 then Using General Potential</li>
     * <li>if index = 1,2,3 then Using Canonical Potential (family OR)</li>
     * <li>if index = 4,5,6 then Using Canonical Potential (famili AND)</li>
     * </ul>
     */
    protected int indexPotential = 0;                                  // General
    /**
     * define if the table shows all parameters or only independent parameters
     */
    protected boolean showingAllParameters = false;
    // Potential
    // by
    // default
    /**
     * define if the table shows probabilities values or state name
     */
    
    protected boolean showingProbabilitiesValues = false;
    
    /**
     * String database
     */
    protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    protected Node node;
    /**
     * First eventTablePotential of node
     *
     */
    protected TableWithEvents tableWithEvents = null;
    protected TablePotential tablePotential = null;
    protected TableWithFunctions tableWithFunctions = null;

//	private ArrayList<Configuration> impossibleConfigurations;
    
    
    protected ProbNet probNet;
    /**
     * Define the last column of the table that was modified
     */
    
    protected int lastCol = -1;
    /**
     * Define the priority list when eventTablePotential values are edited
     */
    
    protected List<Integer> priorityList = new LinkedList<Integer>();
    protected boolean isSelectAllForMouseEvent = true;
    protected boolean isSelectAllForActionEvent = false;
    protected boolean isSelectAllForKeyEvent = false;
    /**
     * first editable row. By default, it is zero until runtime initialisation
     */
    private int firstEditableRow = 0;
    
    /**
     * Default constructor
     *
     * @param node                 - the node with the EvemtTablePotential
     * @param tableWithEventsModel - the model of the EventTablePotential
     * @param modifiable           - true if the table can be edited and modified
     */
    public ValuesTableWithEvents(Node node, TableWithEventsModel tableWithEventsModel, final boolean modifiable) {
        super(node, tableWithEventsModel, true);
        node.getProbNet().getPNESupport().addListener(this);
        this.tableWithEventsModel = tableWithEventsModel;
        this.node = node;
        this.probNet = node.getProbNet();
        try {
            this.tableWithEvents = (TableWithEvents) node.getPotentials().get(0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
                                          stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
        }
        //Adding the initialisation of getEventTablePotential
        
        tablePotential = tableWithEvents.getTablePotential();
        tableWithFunctions = tableWithEvents.getTableWithFunctions();
//		setImpossibleConfigurations(tableWithEvents.getImpossibleConfigurations());
        //
        if (modifiable) {
            int numRowsModel = tableWithEventsModel.getRowCount();
            int numColumsModel = tableWithEventsModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
            initializeDataModified(false);
        }
    }
    
    
    /**
     * Default constructor
     *
     * @param node                 - the node with the EventTablePotential
     * @param tableWithEventsModel - the model of the EventTablePotential
     * @param modifiable           - true if the table can be edited and modified
     */
    public ValuesTableWithEvents(Node node, TableWithEvents tableWithEvents, TableWithEventsModel tableWithEventsModel, final boolean modifiable) {
        super(node, tableWithEventsModel, modifiable);
        node.getProbNet().getPNESupport().addListener(this);
        this.tableWithEventsModel = tableWithEventsModel;
        this.node = node;
        this.probNet = node.getProbNet();
        try {
            this.tableWithEvents = tableWithEvents;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
                                          stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
        }
        //Adding the initialisation of getEventTablePotential
        
        tablePotential = this.tableWithEvents.getTablePotential();
//		setImpossibleConfigurations(this.tableWithEvents.getImpossibleConfigurations());
        //
        if (modifiable) {
            int numRowsModel = tableWithEventsModel.getRowCount();
            int numColumsModel = tableWithEventsModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
            initializeDataModified(false);
        }
    }
    
    
    /**
     * Constructor for ValuesTable
     */
    public ValuesTableWithEvents(TableWithEventsModel tableWithEventsModel, final boolean modifiable) {
        super(null, tableWithEventsModel, modifiable);
        this.tableWithEventsModel = tableWithEventsModel;
        if (modifiable) {
            int numRowsModel = tableWithEventsModel.getRowCount();
            int numColumsModel = tableWithEventsModel.getColumnCount();
            this.dataModified = new boolean[numRowsModel][numColumsModel];
            initializeDataModified(false);
        }
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
            int firstLetterPosition = columnPosition % 26;
            int secondLetterPosition = columnPosition / 26 - 1;
            if (columnPosition >= (26 * 27)) {
            } else if (columnPosition >= 26) {
                columnId = columnId + ALPHABET.substring(secondLetterPosition, secondLetterPosition + 1) + ALPHABET
                        .substring(firstLetterPosition, firstLetterPosition + 1);
            } else {
                columnId = columnId + ALPHABET.substring(firstLetterPosition, firstLetterPosition + 1);
            }
            columnsId[columnPosition] = columnId;
        }
        return columnsId;
    }
    
    /**
     * @return the decimalPositions
     */
    protected static int getDecimalPositions() {
        return decimalPositions;
    }
    
    /**
     * @param newDecimalPositions the decimalPositions to set
     */
    protected static void setDecimalPositions(int newDecimalPositions) {
        decimalPositions = newDecimalPositions;
    }
    
    /**
     * This method returns the dataModified variable. If dataModified doesn't exist it is created
     *
     * @return dataModified
     *
     * @see #dataModified
     * revised--> not changed
     */
    public boolean[][] getDataModified() {
        if (dataModified == null) {
            int numRowsModel = tableWithEventsModel.getRowCount();
            int numColumsModel = tableWithEventsModel.getColumnCount();
            dataModified = new boolean[numRowsModel][numColumsModel];
        }
        return dataModified;
    }
    
    /**
     * This method initialises all the cells of dataModified to the boolean value given by isModified
     *
     * @param isModified - initial value for the cells is dataModified
     *
     * @see #dataModified
     * revised--> not changed
     */
    public void initializeDataModified(boolean isModified) {
        if (tableWithEventsModel != null) {
            if (dataModified == null) {
                getDataModified();
            }
            for (int i = 0; i < tableWithEventsModel.getRowCount(); i++) {
                for (int j = 0; j < tableWithEventsModel.getColumnCount(); j++) {
                    dataModified[i][j] = isModified;
                }
            }
        }
    }
    
    /**
     * Default display configuration for this table
     */
    @Override protected void defaultConfiguration() {
        super.defaultConfiguration();
        setFirstColumnHidden(false); // key prefix column is hidden
        setShowColumnHeader(false); // no column header here
        onTables(omjTable -> {
            omjTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            omjTable.setRowSelectionAllowed(true);
            omjTable.setColumnSelectionAllowed(true);
            omjTable.setGridColor(Color.DARK_GRAY);
            omjTable.setDefaultRenderer(Double.class, new ValuesTableCellRenderer(this,firstEditableRow, null));
            omjTable.setDefaultRenderer(String.class, new ValuesTableCellRenderer(this,firstEditableRow, null));
            // next two lines is a cool trick to enhance table performance
            ToolTipManager.sharedInstance().unregisterComponent(omjTable);
            ToolTipManager.sharedInstance().unregisterComponent(omjTable.getTableHeader());
        });

    }
    
    /**
     * Resets the model in use
     * revised-> not changed
     */
    public void resetModel() {
        tableWithEventsModel = null;
        dataModified = null;
    }
    
    /**
     * Gets the tableWithEventsModel attribute
     * revised-->not changed
     */
    public TableWithEventsModel getEventValuesTableModel() {
        return this.tableWithEventsModel;
    }
    
    /**
     * Sets the data model for this table to newModel and registers with it for
     * listener notifications from the new data model.
     *
     * @param newDataModel the new data source for this table.
     *
     * @throws IllegalArgumentException if newModel is null.
     *                                  <p>
     *                                  revised-->not changed
     */
    public void setModel(TableWithEventsModel newDataModel) throws IllegalArgumentException {
        super.setModel(newDataModel);
        this.tableWithEventsModel = newDataModel;
        tableRowSorter = new TableRowSorter<TableWithEventsModel>(((TableWithEventsModel) getModel()));
        // not display the last row where the cells has states and not values
        // and it is only required when displaying states values
    }
    
    @Override public void setValueAt(Object newValue, int row, int column, Object source) {
        Object oldValue = getValueAt(row, column, source);
        // Not clear if I have to use equals
        if (oldValue.equals(newValue))
            return;
        
        //18/05/2022 - Changed to be used with numeric variables; currently discretized variables not considered
//			if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION) {
        VariableType variableType = node.getVariable().getVariableType();
        if (variableType == VariableType.FINITE_STATES) {
            if (((Double) newValue) < 0) {
                newValue = oldValue;
                JOptionPane.showMessageDialog(this.getParent(), "Introduced value cannot be negative");
            }
            //if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION)
            if (lastCol != column) {
                priorityList.clear();
                lastCol = column;
            }
        }
        
        
        // Chance, decision and utility
        
        try {
            new EventTablePotentialValueEdit(node, tableWithEvents, newValue, row, column, priorityList)
                    .executeEdit();
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
        
    }
    
    /**
     * show a error window message to the user with a specific msg
     *
     * @param msg - the error message to show to user
     *            revised-->not changed
     */
    protected void showNodePotentialTableErrorMsg(String msg) {
        JOptionPane.showMessageDialog(this, stringDatabase.getString(msg + ".Text"),
                                      stringDatabase.getString(msg + ".Title"), JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * @return the variable
     */
    public Variable getVariable() {
        return node.getVariable();
    }
    
    /**
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return nodeType;
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
        return lastEditableRow;
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
        return firstEditableRow;
    }
    
    /**
     * @param firstEditableRow the firstEditableRow to set
     */
    public void setFirstEditableRow(int firstEditableRow) {
        this.firstEditableRow = firstEditableRow;
    }
    
    /**
     * @return the usingGeneralPotential
     */
    public boolean isUsingGeneralPotential() {
        return (indexPotential == 0 ? true : false);
    }
    
    /**
     * @return the showingAllParameters
     */
    public boolean isShowingAllParameters() {
        return showingAllParameters;
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
     *                             revised-->  minor changes
     */
    public void setShowingAllParameters(boolean showingAllParameters) {
        this.showingAllParameters = showingAllParameters;
        tableRowSorter = new TableRowSorter<TableWithEventsModel>(((TableWithEventsModel) getModel()));
        onTables(omjTable -> {
            if (showingAllParameters) {
                omjTable.setRowSorter(null);
            } else {
                int lastRow = getModel().getRowCount() - 1 - 1;
                lastRow = (lastRow < 0 ? 0 : lastRow);
                LinkedList<RowFilter<Object, Object>> list = new LinkedList<RowFilter<Object, Object>>();
                list.add(RowFilter.notFilter(RowFilter.regexFilter((String) getModel().getValueAt(lastRow, 0), 0)));
                list.add(RowFilter.notFilter(RowFilter.regexFilter(getVariable().getName(), 0)));
                tableRowSorter.setRowFilter(RowFilter.andFilter(list));
                omjTable.setRowSorter(tableRowSorter);
            }
        });
    }
    
    /**
     * Gets the regular expression for the temporal node
     *
     * @param name the name of the node
     *
     * @return the regular expression of the name of node. It returns namenode\\[number\\]
     * <p>
     * revised--> not changed
     */
    protected String getRegExp(String name) {
        int cont1 = name.indexOf("[");
        String s1 = name.substring(0, cont1);
        int cont2 = name.indexOf("]");
        String s2 = name.substring(cont1, cont2);
        String s3 = name.substring(cont2, name.length());
        return s1 + "\\" + s2 + "\\" + s3;
    }
    
    /**
     * Gets the regular expression for node names with parenthesis
     *
     * @param name the name of the node
     *
     * @return the regular expression of the name of node. This method returns
     * the same name but substituting '(' and ')' by '\\(' and '\\)'
     */
    protected String getRegExpParenthesis(String name) {
        if (name.contains("(")) {
            name = name.replace("(", "\\(");
        }
        if (name.contains(")")) {
            name = name.replace(")", "\\)");
        }
        return name;
    }
    
    /**
     * @return the showingProbabilitiesValues
     */
    protected boolean isShowingProbabilitiesValues() {
        return showingProbabilitiesValues;
    }
    
    
    /**
     * print the NodePotentialTable
     *
     * @cyago minor changes
     */
    public void printTable() {
        System.out.println("NodePotentialTable: ");
        if (getVariable() != null) {
            System.out.println("    variable = " + getVariable().getName());
        } else {
            System.out.println("    variable = not defined yet");
        }
        if (tableWithEventsModel != null) {
            System.out.println("    tableWithEventsModel.firstEditableRow = " + tableWithEventsModel.getFirstEditableRow());
            System.out.println("    tableWithEventsModel.rowCount = " + tableWithEventsModel.getRowCount());
            System.out.println("    tableWithEventsModel.columnCount = " + tableWithEventsModel.getColumnCount());
        } else {
            System.out.println("    tableWithEventsModel.firstEditableRow = not tableWithEventsModel yet");
        }
        System.out.println("    lastEditableRow = " + lastEditableRow);
        System.out.println("    usingGeneralPotencial = " + isUsingGeneralPotential());
        System.out.println("    showingAllParameters = " + isShowingAllParameters());
        System.out.println("    showingProbabilitiesValues = " + isShowingProbabilitiesValues());
        
    }
    
    public void undoableEditHappened(UndoableEditEvent event) {
        UndoableEdit edit = event.getEdit();
        if (edit instanceof EventTablePotentialValueEdit) {
            eventTablePotentialValueEditHappened((EventTablePotentialValueEdit) edit);
            
        }
    }
    
    
    public void eventTablePotentialValueEditHappened(EventTablePotentialValueEdit edit) {
        // postition in the table of TablePotential
        int position = 0;
        int rowPosition = edit.getRowPosition(position);
        int columnPosition = edit.getColumnPosition();
        TablePotential tablePotential = edit.getTablePotential();
        TableWithFunctions tableWithFunctions = edit.getTableWithEvents().getTableWithFunctions();
        //12/06/2024 DistributionTablePotential can be associated with any Event or Numeric variable
//		if (
//				(getNodeType()==NodeType.EVENT) || (getNodeType()==NodeType.UTILITY)){
        if (getVariable().getVariableType() == VariableType.EVENT
                || getVariable().getVariableType() == VariableType.NUMERIC) {
            int potentialSelected = edit.getPotentialSelected();
            rowPosition = edit.getRowPosition(position);
            columnPosition = edit.getColumnPosition();
            try {
                if (tableWithFunctions == null) {
                    getModel()
                            .setValueAt(tablePotential.getValues()[potentialSelected], edit.getRowPosition(), edit.getColumnPosition());
                } else {
                    getModel()
                            .setValueAt(tableWithFunctions.getFunctionValues()[potentialSelected], edit.getRowPosition(), edit.getColumnPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            priorityList = edit.getPriorityList();
            ListIterator<Integer> listIterator = priorityList.listIterator();
            double[] values = tablePotential.getValues();
            while (listIterator.hasNext()) {
                position = (Integer) listIterator.next();
                rowPosition = edit.getRowPosition(position);
                columnPosition = edit.getColumnPosition();
                super.getModel().setValueAt(values[position], rowPosition, columnPosition);
            }
        }
    }
    
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (edit instanceof EventTablePotentialValueEdit eventTablePotentialValueEdit) {
            TablePotential editPotential = eventTablePotentialValueEdit.getTablePotential();
            priorityList = eventTablePotentialValueEdit.getPriorityList();
            for (Integer position : priorityList) {
                super.getModel().setValueAt(editPotential.getValues()[position], eventTablePotentialValueEdit.getRowPosition(position),
                                            eventTablePotentialValueEdit.getColumnPosition());
            }
        }
    }
    

    
    /**
     * This method edits the cell at row, column.
     * If isSelectAllForMouseEvent,isSelectAllForActionEvent, or isSelectAllForKeyEvent the entire cell is selected
     * UNCLEAR isSelectAllForMouseEvent,isSelectAllForActionEvent, or isSelectAllForKeyEvent values never change
     * Overrided to provide Select All editing functionality
     *
     * @param row    - the row of the edited cell
     * @param column - the column of the edited cell
     * @param e      - event to pass into shouldSelectCell;
     */
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        if (isSelectAllForMouseEvent || isSelectAllForActionEvent || isSelectAllForKeyEvent) {
            selectAll(e);
        }
        return result;
        
    }
    
    /**
     * If the editor that is handling the editing session is not a JTextComponent, the method does nothing
     * If the editor is a JTextComponent then:
     * If e is and instance of KeyEvent, ActionEvent or MouseEvent, the method select all the text of the cell
     *
     * @param e: event which provoked the edition and selection
     */
    private void selectAll(EventObject e) {
        // Returns the component that is handling the editing session.
        final Component editor = getEditorComponent(e.getSource());
        if (editor == null || !(editor instanceof JTextComponent))
            return;
        if (e == null) {
            ((JTextComponent) editor).selectAll();
            return;
        }
        // Typing in the cell was used to activate the editor
        if (e instanceof KeyEvent && isSelectAllForKeyEvent) {
            ((JTextComponent) editor).selectAll();
            return;
        }
        // F2 was used to activate the editor
        if (e instanceof ActionEvent && isSelectAllForActionEvent) {
            ((JTextComponent) editor).selectAll();
            return;
        }
        // A mouse click was used to activate the editor.
        // Generally this is a double click and the second mouse click is
        // passed to the editor which would remove the text selection unless
        // we use the invokeLater()
        if (e instanceof MouseEvent && isSelectAllForMouseEvent) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ((JTextComponent) editor).selectAll();
                }
            });
        }
    }
    
    /**
     * This method sets the variable probNet to the node probNet
     *
     * @param node: the node whose eventTablePotential is being displayed
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
        probNet.getPNESupport().removeListener(this);
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

//	public ArrayList<Configuration> getImpossibleConfigurations() {
//		return impossibleConfigurations;
//	}
//
//	public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations) {
//		this.impossibleConfigurations = impossibleConfigurations;
//	}
}