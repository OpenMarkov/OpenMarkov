/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.gui.component.OMTableModel;

import javax.swing.table.DefaultTableModel;

/**
 * Table model for the advanced (user-defined) properties of a network,
 * storing property name-value pairs.
 */
@SuppressWarnings("serial") public class AdvancedPropertiesTableModel extends OMTableModel {

	private static final int COLUMN_AGENT_NAME = 1;
	//private static final int COLUMN_AGENT_ADITIONAL_PROPERTIES = 2;

	/**
	 * constructor for the model
	 *
	 * @param data    - values to set in the table
	 * @param columns - name of the colums of the table
	 */
	public AdvancedPropertiesTableModel(Object[][] data, String[] columns) {
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
		case COLUMN_AGENT_NAME:
			value = String.class;
			break;
		/*case COLUMN_AGENT_ADITIONAL_PROPERTIES:
			value = String.class;
			break;*/
		default:
			//value = String.class;
			break;
		}
		return value;
	}

}
