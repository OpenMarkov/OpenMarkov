/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel Component to manage a discretized values
 *
 * @author jlgozalo
 * @version 1.0
 */
public class DiscretizeTableModel extends OMTableModel {

	/**
	 * internal serial ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constant definition of the columns
	 */
	private static final int COLUMN_ID = 0;
	//private static final int COLUMN_INTERVAL_NAME = 1;
	private static final int COLUMN_LOWER_LIMIT_SYMBOL = 2;
	private static final int COLUMN_LOWER_LIMIT_VALUE = 3;
	private static final int COLUMN_SEPARATOR = 4;
	private static final int COLUMN_UPPER_LIMIT_VALUE = 5;
	private static final int COLUMN_UPPER_LIMIT_SYMBOL = 6;

	/**
	 * constructor for the model
	 *
	 * @param data    - values to set in the table
	 * @param columns - name of the colums of the table
	 */
	public DiscretizeTableModel(Object[][] data, String[] columns) {
		super(data, columns, true);
	}

	/**
	 * retrieve the type of the object in a particular column of the table
	 *
	 * @param columnId position of the column in the table
	 * @return class of the objects in the column
	 */
	@Override public Class<?> getColumnClass(int columnId) {
		Class<?> value = String.class;
		switch (columnId) {
            case COLUMN_LOWER_LIMIT_VALUE, COLUMN_UPPER_LIMIT_VALUE:
			value = Double.class;
			break;
            default:
			//value = String.class;
			break;
		}
		return value;
	}

	/**
	 * @param row    of the cell
	 * @param column of the cell
	 * @return true if the cell is editable
	 */
	@Override public boolean isCellEditable(int row, int column) {
        return column != COLUMN_ID && column != COLUMN_LOWER_LIMIT_SYMBOL && column != COLUMN_SEPARATOR
                && column != COLUMN_UPPER_LIMIT_SYMBOL;
    }

}