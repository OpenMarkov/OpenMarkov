/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.link;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.component.LinkRestrictionCellRenderer;
import org.openmarkov.gui.component.LinkRestrictionValuesTable;
import org.openmarkov.gui.component.LinkRestrictionValuesTableModel;
import org.openmarkov.gui.component.ValuesTable;
import org.openmarkov.gui.component.ValuesTableModel;
import org.openmarkov.gui.dialog.common.ProbabilityTablePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*****
 * This class implements a TablePotential table for a link restriction. This
 * class is based upon ProbabilityTablePanel, reading the potential from the
 * link.
 *
 * @author ckonig
 *
 */
public class LinkRestrictionPanel extends ProbabilityTablePanel {

	/**
	 * JTable where show the values.
	 */
	protected ValuesTable valuesTable = null;
	/**
	 * Panel to scroll the table.
	 */
	protected JScrollPane valuesTableScrollPane = null;
	/***
	 * Node of the parent node of the link
	 */
	protected final Node node1;
	/****
	 * Node of the child node of the link
	 */
	protected final Node node2;
	/***
	 * The link which has the link restriction
	 */
	protected final Link<Node> link;
	/**
	 * Indicates if the data of the table is modifiable.
	 */
	private final boolean modifiable;

	public LinkRestrictionPanel(Link<Node> link) {
		this.link = link;
        node1 = link.getFrom();
        node2 = link.getTo();
		modifiable = true;
		setData(node1, node2);
		setLayout(new BorderLayout());
		add(this.getValuesTableScrollPane(), BorderLayout.CENTER);
	}

	/**
	 * special method to show/hide the values table
	 */
	public void showValuesTable(final boolean visible) {
		getLinkRestrictionValuesTable().setVisible(visible);
	}

	/**
	 * This method initializes valuesTable and defines that first column and
	 * first row are not selectable.
	 *
	 * @return a new values table.
	 */
	protected ValuesTable getLinkRestrictionValuesTable() {
		if (valuesTable == null) {
			valuesTable = new LinkRestrictionValuesTable(link, getTableModel(), modifiable);
			valuesTable.setName("LinkRestrictionPanel.valuesTable");
		}
		return valuesTable;
	}

	/**
	 * This method initializes tableModel.
	 *
	 * @return a new tableModel.
	 */
	protected ValuesTableModel getTableModel() {
        if (valuesTable == null || valuesTable.getTableModel() == null) {
            return new LinkRestrictionValuesTableModel(data, columns, firstEditableRow);
		}
        return (ValuesTableModel) valuesTable.getModel();
    }

	/**
	 * This method initializes valuesTableScrollPane.
	 *
	 * @return a new values table scroll pane.
	 */
	protected JScrollPane getValuesTableScrollPane() {
		if (valuesTableScrollPane == null) {
			valuesTableScrollPane = new JScrollPane();
			valuesTableScrollPane.setName("LinkRestrictionPanel.valuesTableScrollPane");
			valuesTableScrollPane.setViewportView(getLinkRestrictionValuesTable());
		}
		return valuesTableScrollPane;
	}

	/*****
	 * This method gets the information of the link potential from the two nodes
	 * of the link and transforms the tablePotential of the link restriction to
	 * a format, which can be displayed in a table.
	 *
	 * @param node1 the node1
	 *            Parent node of the link.
	 * @param node2 the node2
	 *            Child node of the link.
	 */
	public void setData(Node node1, Node node2) {
        
        String[] newColumns = ValuesTable.getColumnsIdsSpreadSheetStyle(this.node1.getVariable().getNumStates() + 1);
		setFirstEditableRow(1);
		setLastEditableRow(node2.getVariable().getNumStates());
        Object[][] tableData = convertListPotentialsToTableFormat(node1, node2);
		this.data = tableData;
		this.columns = newColumns;

		setData(tableData, newColumns, firstEditableRow, lastEditableRow, node2.getNodeType());
		setCellRenderers();
		valuesTable.addMouseListener(new java.awt.event.MouseAdapter() {

			@Override public void mouseClicked(java.awt.event.MouseEvent e) {

				int row = valuesTable.rowAtPoint(e.getPoint(), e.getSource());
				int column = valuesTable.columnAtPoint(e.getPoint(), e.getSource());
				if ((row > 0) && (column > 0)) {
					Integer value = (Integer) valuesTable.getValueAt(row, column, e.getSource());
					Integer newValue = (value.equals(1) ? 0 : 1);
					valuesTable.setValueAt(newValue, row, column, e.getSource());
				}

			}

		});

	}

	/*****
	 * Prepare the table data from the two nodes of the link.
	 *
	 * @param node1 the node1
	 *            Parent node of the link
	 * @param node2 the node2
	 *            Child node of the link
	 * @return he table data to be set
	 */
	protected Object[][] convertListPotentialsToTableFormat(Node node1, Node node2) {
        Object[][] values = setValuesTableSize();
		values = setParentsNameInUpperLeftCornerArea(values);
		values = setParentsStatesInTopArea(values);
		values = setNodeStatesInLeftArea(values);
		values = setPotentialDataInCentreArea(values);
		return values;
	}

	/**
	 * Assigns the value of the tablePotential of the link restriction to the
	 * central positions of the table data.
	 *
	 * @param oldValues - the table data.
	 * @return the table data having assigned the values of the link restriction
	 * potential to the corresponding positions of the table.
	 */

	private Object[][] setPotentialDataInCentreArea(Object[][] oldValues) {

        TablePotential tablePotential = link.getRestrictionsPotential();

		int numStates2 = node2.getVariable().getNumStates();
		int numStates1 = node1.getVariable().getNumStates();
		for (int i = 0; i < numStates2; i++) {
			for (int j = 1; j <= numStates1; j++) {
				int[] statesIndices = new int[] { j - 1, i };
				int value = (int) tablePotential.getValue(variables, statesIndices);
				oldValues[numStates2 - i][j] = value;
			}
		}

		return oldValues;
	}

	/**
	 * This method sets the first column with the values of the states of the
	 * childe node (node2).
	 *
	 * @param oldValues - the table that is being modified
	 * @return the table data having assigned the values of the states of node2.
	 */
	private Object[][] setNodeStatesInLeftArea(Object[][] oldValues) {

        NodeType type = node2.getNodeType();
        Variable variable = node2.getVariable();
        State[] states = variable.getStates();
        for (int i = variable.getNumStates(); i > 0; i--) {
			if (type != NodeType.UTILITY) {
                oldValues[i][0] = states[variable.getNumStates() - i].getName();
			} else {
				oldValues[i][0] = "";
			}
		}
		return oldValues;

	}

	/**
	 * This method assigns the states of the parent node to the first row of the
	 * table data.
	 *
	 * @param oldValues - the table that is being modified
	 * @return the table data having assigned the values of the states of node1.
	 */
	private Object[][] setParentsStatesInTopArea(Object[][] oldValues) {

		Object[][] values = oldValues;
        
        Variable variable = node1.getVariable();
        State[] states = variable.getStates();
		for (int i = 1; i <= node1.getVariable().getNumStates(); i++) {
			values[0][i] = states[i - 1].getName();
		}
		return values;
	}

	/**
	 * This methods fills the Upper Left corner of the table with the name of
	 * the nodes of the potential.
	 *
	 * @param oldValues - the table that is being modified
	 * @return the table data having assigned the names of the two nodes to the
	 * upper left position.
	 */

	private Object[][] setParentsNameInUpperLeftCornerArea(Object[][] oldValues) {

        oldValues[0][0] = node1.getVariable();
		return oldValues;
	}

	/**
	 * Set values table size for the link restriction potential.
	 *
	 * @return the table data having the correct size to displau the link
	 * restriction potential.
	 */
    
    private Object[][] setValuesTableSize() {
        setBaseIndexForCoordinates(1);
		setFirstEditableRow(1);
		variables = link.getRestrictionsPotential().getVariables();
		// create the array of arrays
        return new Object[node2.getVariable().getNumStates() + 1][node1.getVariable().getNumStates() + 1];
	}

	/**
	 * Sets a new table model with new data.
	 *
	 * @param newData new data for the table.
	 */
	public void setData(Object[][] newData) {
		setData(newData, columns, 0, 0, NodeType.CHANCE);
	}

	/**
	 * Sets a new table model with new data and new columns
	 *
	 * @param newData    new data for the table
	 * @param newColumns new columns for the table
	 */
	public void setData(Object[][] newData, String[] newColumns, int firstEditableRow, int lastEditableRow,
			NodeType nodeType) {
		showValuesTable(true);
		data = newData.clone();
		columns = newColumns.clone();
		this.firstEditableRow = firstEditableRow;
		this.lastEditableRow = lastEditableRow;
		valuesTable.resetModel();
		valuesTable.setModel(getTableModel());
		valuesTable.initializeDataModified(false);
		((ValuesTableModel) valuesTable.getModel()).setFirstEditableRow(firstEditableRow);
		valuesTable.setLastEditableRow(lastEditableRow);
	}

	/**
	 * Set renders for the cells in the table. Only has to be called when set
	 * data.
	 */
	protected void setCellRenderers() {
		int size = valuesTable.getColumnCount();
		boolean[] aux = new boolean[size - 1];
		valuesTable.onTables(omjTable -> {
			omjTable.setDefaultRenderer(Double.class, new LinkRestrictionCellRenderer(valuesTable, getFirstEditableRow(), aux));
			omjTable.setDefaultRenderer(String.class, new LinkRestrictionCellRenderer(valuesTable, getFirstEditableRow(), aux));
			omjTable.setDefaultRenderer(Integer.class, new LinkRestrictionCellRenderer(valuesTable, getFirstEditableRow(), aux));
		});
	}

	@Override public void setData(Node node) {
		// TODO Auto-generated method stub
	}

	@Override public void close() {

	}

	private void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
	
	@Override protected void setFirstEditableRow(int firstEditableRow) {
		super.setFirstEditableRow(firstEditableRow);
		this.valuesTable.setFirstEditableRow(firstEditableRow);
	}
}
