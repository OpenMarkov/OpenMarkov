/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.table.TableModel;

@SuppressWarnings("serial") public class ValuesTableWithLinkRestrictionCellRenderer extends ValuesTableCellRenderer {
    
    public ValuesTableWithLinkRestrictionCellRenderer(ValuesTable valuesTable, int firstEditableRow, boolean[] uncertaintyInColumns) {
		super(valuesTable, firstEditableRow, uncertaintyInColumns);
	}
	
	@Override
	protected SetColor getCellColors(TableModel model, Object value, boolean isSelected, boolean hasFocus, int row,
	                                 int column) {
		var colors = super.getCellColors(model, value, isSelected, hasFocus, row, column);
		if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && firstEditableRow >= 0 && (row >= firstEditableRow)) {
			try {
				if (!model.isCellEditable(row, column)) {
					colors.background = GUIColors.Network.Link.LinkRestriction.INCOMPATIBILITY_BACKGROUND.getColor();
				}
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
		return colors;
	}

}
