/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

@SuppressWarnings("serial") public class LinkRestrictionValuesTableModel extends ValuesTableModel {

	/**
	 * constructor
	 */
	public LinkRestrictionValuesTableModel(Object[][] data, String[] columns, int firstEditableRow) {
		super(data, columns, firstEditableRow);

	}

	/**
	 * This method determines the default renderer/editor for each cell. First
	 * column is a String class type and the others are integer type.
	 */
	@Override public Class<?> getColumnClass(int c) {

		Integer integerExample = 0;
		String stringExample = "";

		if (c == 0) {
			return stringExample.getClass();
        }
        return integerExample.getClass();
    }

	@Override public boolean isCellEditable(int row, int col) {
		return false;
	}

}
