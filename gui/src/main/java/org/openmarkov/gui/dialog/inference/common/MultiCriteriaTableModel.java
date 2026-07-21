/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import javax.swing.table.DefaultTableModel;

/**
 * Model for multicriteria table
 *
 * @author Jorge
 */
public class MultiCriteriaTableModel extends DefaultTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public MultiCriteriaTableModel() {
		super();
	}

	@Override public boolean isCellEditable(int row, int column) {

		// The user wouldn't be able to edit the criteria
        return column != InferenceOptionsDialog.CRITERION_COLUMN && row != 0;

	}

}
