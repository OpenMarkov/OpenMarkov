/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.gui.component.OMTableModel;

/**
 * Table model for the advanced (user-defined) properties of a network,
 * storing property name-value pairs.
 */
@SuppressWarnings("serial") public class AdvancedPropertiesTableModel extends OMTableModel {
    
    
    /**
	 * constructor for the model
	 *
	 * @param data    - values to set in the table
	 * @param columns - name of the colums of the table
	 */
	public AdvancedPropertiesTableModel(Object[][] data, String[] columns) {
		super(data, columns, true);
	}
 
}
