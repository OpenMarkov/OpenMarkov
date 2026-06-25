/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

/**
 * ValuesTableModel defines the basic behaviour of the Augmented Table Model
 *
 * @author carmenyago
 * @version 1.0 Apr 2017
 */
public class AugmentedValuesTableModel extends ValuesTableModel {
	/**
	 * calculated serial ID
	 */
	private static final long serialVersionUID = 7010730473355625101L;
	
	/**
	 * constructor
	 */
	public AugmentedValuesTableModel(Object[][] data, String[] columns, int firstEditableRow) {
		super(data, columns, firstEditableRow);
	}

	/**
	 * This method determines the default renderer/editor for each cell. First
	 * column is a String class type and the others are double type.
	 */
	@Override public Class<?> getColumnClass(int c) {
		return String.class;
	}

	@Override public boolean isCellEditable(int row, int col) {
		return false;
	}
}
