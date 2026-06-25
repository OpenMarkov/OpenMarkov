/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.component.ValuesTable;

/**
 * This class extends from {@code TablePotentialPanel}, is a panel used by
 * {@code ICIOptionListenerAssistant} to show the complete parameters
 * table. It is similar to {@code TablePotentialPanel} with the peculiarity
 * that can not be edited cells
 *
 * @author myebra
 */
@SuppressWarnings("serial") public class CPTablePanel extends TablePotentialPanel {
	/**
	 * Indicates if the data of the table is modifiable.
	 */
	private boolean modifiable;
    
    public CPTablePanel(Node node) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException {
		super(node);
		modifiable = false;
	}

	/**
	 * This method initializes valuesTable and defines that first two columns
	 * are not selectable
	 *
	 * @return a new values table.
	 */
	@Override public ValuesTable getValuesTable() {
		if (valuesTable == null) {
			valuesTable = new ValuesTable(node, getTableModel(), modifiable);
			valuesTable.setName("PotentialsTablePanel.valuesTable");
		}
		return valuesTable;
	}
	
	@Override public void close() {
		getValuesTable().close();
	}
}
