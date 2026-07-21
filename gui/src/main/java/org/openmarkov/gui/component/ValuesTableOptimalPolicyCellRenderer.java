/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.table.TableModel;
import java.awt.*;

@SuppressWarnings("serial") public class ValuesTableOptimalPolicyCellRenderer extends ValuesTableCellRenderer {
    
    public ValuesTableOptimalPolicyCellRenderer(ValuesTable valuesTable, int firstEditableRow, boolean[] uncertaintyInColumns) {
		super(valuesTable, firstEditableRow, uncertaintyInColumns);
    }
	
	@Override
	protected SetColor getCellColors(TableModel model, Object value, boolean isSelected, boolean hasFocus, int row,
	                                 int column) {
		var colors = super.getCellColors(model, value, isSelected, hasFocus, row, column);
        Color color = GUIColors.Tables.ValuesTable.OPTIMAL_POLICY.getColor(); //new java.awt.Color (255, 72, 72);
		if (column >= ValuesTable.FIRST_EDITABLE_COLUMN && ValuesTable.FIRST_EDITABLE_COLUMN >= 0
				&& row >= firstEditableRow && value instanceof Double) {
			boolean isMax = true;
			double doubleValue = (double) value;
			// Change color if this cell contains optimal policy, i.e. max value
			for (int i = firstEditableRow; i < model.getRowCount(); i++) {
				if (i != row) {
					isMax &= doubleValue > (double) model.getValueAt(i, column);
				}
			}
			if (isMax) {
				colors.background = color;
			}
		}
		return colors;
	}
}
