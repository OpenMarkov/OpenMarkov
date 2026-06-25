/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

/**
 * TableWithEventsModel defines the basic behavior of the Table Model for EventTablePotential
 * * Transition class to be merged with the new structure of tables
 * @author cyago
 * @version 1.0 - 24/03/2019
 * @version 2.0 - 29/08/2023 - cmyago; refactored to TableWithEventsModel (from EventValuesTableModel)
 */
public class TableWithEventsModel extends ValuesTableModel {
	/**
	 * calculated serial ID
	 */
	private static final long serialVersionUID = 7010730473355625101L;
	/***
	 * List of additional not editable positions
	 */
	protected Object[][] notEditablePositions;
	/**
	 * first editable row. By default, all rows are editable (first 0)
	 */
	int firstEditableRow = 0;
	
	/**
	 * constructor
	 */
	public TableWithEventsModel(Object[][] data, String[] columns, int firstEditableRow) {
		super(data, columns, firstEditableRow);
		this.firstEditableRow = firstEditableRow;
		this.notEditablePositions = new Object[0][0];
	}

	/**
	 * This method determines the default renderer/editor for each cell. First
	 * column is a String class type and the others are double type.
	 */
	public Class<?> getColumnClass(int c) {
		return (c == 0) ? String.class : Double.class;
	}

	/**
	 * This method determines if the cell is editable or not, considering :<ul>
	 * <li>all rows in the header are not editable</li> <li>column with the name of
	 * the parents and the values are not editable</li></ul>
	 */
	public boolean isCellEditable(int row, int col) {
		if (row < firstEditableRow) {
			return false;
		}
		if (col < ValuesTable.FIRST_EDITABLE_COLUMN) {
			// states names are not editable
			return false;
		}
		if (notEditablePositions.length > row && notEditablePositions[0].length > col) {
			if ((notEditablePositions[row][col]) != null) {
				return false;
			}
		}
		return true; // all other cells are editable
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

	public Object[][] getNotEditablePositions() {
		return notEditablePositions.clone();
	}

	public void setNotEditablePositions(Object[][] notEditablePositions) {
		this.notEditablePositions = notEditablePositions;
	}
}
