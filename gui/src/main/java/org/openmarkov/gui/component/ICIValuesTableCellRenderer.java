/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.component;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.table.TableModel;
import java.awt.Color;
import java.util.List;

/**
 * This class is used for painting and coloring the ICItable and the headers
 *
 * @author jlgozalo
 * @version 1.0 15/08/2009
 */
@SuppressWarnings("serial") public class ICIValuesTableCellRenderer extends ValuesTableCellRenderer {
    private final List<Variable> variables;
    private final int[] numColumnsParents;
    private final int[] acummulativeColumns;
    
    public ICIValuesTableCellRenderer(ICIValuesTable iciValuesTable, int firstEditableRow, boolean[] uncertaintyInColumns, ICIPotential iciPotential) {
        super(iciValuesTable, firstEditableRow, uncertaintyInColumns);
        this.variables = iciPotential.getVariables();
        this.numColumnsParents = new int[variables.size()];
        for (int i = 1; i < variables.size(); ++i) {
            numColumnsParents[i - 1] = variables.get(i).getNumStates();
        }
        numColumnsParents[variables.size() - 1] = 1;
        acummulativeColumns = new int[variables.size()];
        acummulativeColumns[0] = numColumnsParents[0];
        for (int i = 1; i < numColumnsParents.length; ++i) {
            acummulativeColumns[i] = numColumnsParents[i] + acummulativeColumns[i - 1];
        }
    }
    
    @Override
    protected SetColor getCellColors(TableModel model, Object value, boolean isSelected, boolean hasFocus, int row,
                                     int column) {
        Color background = null;
        Color foreground = null;
        
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) { // PARENTS CELLS
            // set alternate colors
            // column = 0 row = 0 o 1
            background = GUIColors.Tables.HEADER_BACKGROUND.getColor();
            foreground = GUIColors.Tables.FROZEN_CELL_FOREGROUND.getColor();
        }
        // NEW
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) { // NODE STATES CELLS
            background = GUIColors.Tables.FROZEN_CELL_BACKGROUND.getColor();
            foreground = GUIColors.Tables.FROZEN_CELL_FOREGROUND.getColor();
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) { // headers cells
            if (row == 0) {// FIRST ROW
                for (int i = 0; i < acummulativeColumns.length; i++) {
                    if (i == 0) {
                        if (column <= acummulativeColumns[i]) {
                            background = GUIColors.Tables.HEADER_BACKGROUND.getColor();
                            foreground = GUIColors.Tables.HEADER_FOREGROUND_COLORS.getFirst().getColor();
                            break;
                        }
                    } else if (acummulativeColumns[i - 1] < column && column <= acummulativeColumns[i]) {
                        background = GUIColors.Tables.HEADER_BACKGROUND.getColor();
                        foreground = GUIColors.Tables.HEADER_FOREGROUND_COLORS.get(i % GUIColors.Tables.HEADER_FOREGROUND_COLORS.size())
                                                                              .getColor();
                        break;
                    }
                }
            }
            if (row == 1) {// SECOND ROW
                // setBackground( new Color(220,220,220));
                background = GUIColors.Tables.HEADER_BACKGROUND.getColor();
                foreground = GUIColors.Tables.HEADER_FOREGROUND_COLORS.get(column % GUIColors.Tables.HEADER_FOREGROUND_COLORS.size())
                                                                      .getColor();
            }
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && firstEditableRow >= 0 && (row >= firstEditableRow)) {
            int editableRowIndex = row-firstEditableRow;
            int editableColumnIndex = column-ValuesTable.FIRST_EDITABLE_COLUMN;
            var colors = GUIColors.Tables.EDITABLE_CELL_COLOR.getEditableCellColor(isSelected, editableRowIndex, editableColumnIndex);
            if (colors.background!=null){
                background = colors.background.getColor();
            }
            if(colors.foreground!=null) {
                foreground = colors.foreground.getColor();
            }
            return new SetColor(foreground, background, true);
        }
        return new SetColor(foreground, background, false);
    }
}
