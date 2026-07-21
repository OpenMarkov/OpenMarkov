/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.component;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.configuration.GUIColor;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.IconBind;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.stream.IntStream;

import static org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog.CRITERION_COLUMN;

/**
 * This class is used for painting and coloring the table and the headers
 *
 * @author jlgozalo
 * @version 1.0 15/08/2009
 */
public class ValuesTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * default serial ID
     */
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat formatter = new DecimalFormat("0.######", new DecimalFormatSymbols(Locale.US));
    /**
     * to define the first editable row of the table
     */
    protected int firstEditableRow;
    private final Object table;
    private final boolean[] uncertaintyInColumns;
    private JLabel jUncertaintyIcon;
    
    /**
     * constructor for the renderer
     *
     * @param firstEditableRow     value of the first editable row
     * @param uncertaintyInColumns boolean array with the columns with (1)/without (0) mark. The
     *                             array only has to contain indexes for the editables columns
     */
    public ValuesTableCellRenderer(Object table, int firstEditableRow, boolean[] uncertaintyInColumns) {
        this.table = table;
        this.uncertaintyInColumns = uncertaintyInColumns;
        this.firstEditableRow = firstEditableRow;
    }
    
    public static class SetColor {
        public Color foreground;
        public Color background;
        public boolean forceOverride;
        
        public SetColor(Color foreground, Color background, boolean forceOverride) {
            this.foreground = foreground;
            this.background = background;
            this.forceOverride = forceOverride;
        }
    }
    
    /**
     * headers rows are displayed in a gray background color with red and blue
     * foreground alternatively non headers rows are displayed in an alternative
     * cyan and light gray background color with black foreground color the
     * first two column are in gray
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TableModel model = switch (this.table){
            case JTable jTable->jTable.getModel();
            case ValuesTable valuesTable-> {
                column=valuesTable.resolveMainColumnIndex(table, column);
                this.firstEditableRow=valuesTable.getFirstEditableRow();
                yield valuesTable.getModel();
            }
            case StickyColumnsTablePane stickyColumnsTablePane -> {
                column=stickyColumnsTablePane.resolveMainColumnIndex(table, column);
                yield stickyColumnsTablePane.getModel();
            }
            default -> throw new IllegalStateException("Unexpected value: " + this.table);
        };
        if (column == CRITERION_COLUMN) {
            setHorizontalAlignment(SwingConstants.LEFT);
        } else {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        setCellFonts(row, column);
        setCellBorders(hasFocus, row, column);
        SetColor setColor = getCellColors(model, value, isSelected, hasFocus, row, column);
        if (setColor.background != null) {
            this.setBackground(setColor.background);
        }
        if (setColor.foreground != null) {
            this.setForeground(setColor.foreground);
        }
        if (value instanceof Double) {
            value = formatter.format(value);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && (row >= firstEditableRow) && uncertaintyInColumns != null
                && uncertaintyInColumns[column - 1]) {
            getUncertaintyIcon().setText(value.toString());
            return getUncertaintyIcon();
        }
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (setColor.forceOverride && setColor.background != null) {
            this.setBackground(setColor.background);
        }
        if (setColor.forceOverride && setColor.foreground != null) {
            this.setForeground(setColor.foreground);
        }
        return component;
    }
    
    /**
     * set cell fonts
     *
     * @param row        - row of the cell
     * @param column     - column of the cell
     */
    private void setCellFonts(int row, int column) {
        Font sansboldFont = new Font("SansSerif", Font.BOLD, 30);
        Font sansFont = new Font("SansSerif", Font.PLAIN, 14);
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // PARENTS CELLS
            setFont(sansboldFont);
        }
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
            // NODE STATES CELLS
            setFont(sansboldFont);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // HEADER CELLS
            setFont(sansboldFont);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
            // DATA CELLS
            setFont(sansFont);
        }
    }
    
    /**
     * set cell colors
     *
     * @param model      - table where the cell is located
     * @param value      - the value of the cell in edition
     * @param isSelected - true if the cell is selected by the user
     * @param hasFocus   - true if the cell has the focus by the user
     * @param row        - row of the cell
     * @param column     - column of the cell
     */
    protected SetColor getCellColors(TableModel model, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Color background = null;
        Color foreground = null;
        
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // PARENTS CELLS set alternate colors
            background = GUIColors.Tables.HEADER_BACKGROUND.getColor();
            foreground = GUIColors.Tables.FROZEN_CELL_FOREGROUND.getColor();
        }
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
            // NODE STATES CELLS
            background = GUIColors.Tables.FROZEN_CELL_BACKGROUND.getColor();
            foreground = GUIColors.Tables.FROZEN_CELL_FOREGROUND.getColor();
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // HEADER CELLS
            switch (row % 3) {
                case 0, 1, 2:
                    background = GUIColors.Tables.HEADER_BACKGROUND.getColor();
                    break;
                default:
                    break;
            }
            var valuesOfRow = IntStream.range(0, model.getColumnCount())
                                       .mapToObj(columnIndex -> model.getValueAt(row, columnIndex))
                                       .distinct().toList();
            foreground = GUIColors.Tables.HEADER_FOREGROUND_COLORS.get(valuesOfRow.indexOf(value) % GUIColors.Tables.HEADER_FOREGROUND_COLORS.size())
                                                                  .getColor();
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
    
    enum ValueCellKind{
        PARENT_VARIABLE_NAME,
        PARENT_VARIABLE_STATE,
        THIS_VARIABLE_STATE,
        STATE_VALUE,
    }
    
    @FunctionalInterface
    public interface EditableCellColor{
        CellColor getEditableCellColor(boolean isSelected, int rowIndex, int columnIndex);
    }
    
    public static class CellColor{
        public @Nullable GUIColor foreground;
        public @Nullable GUIColor background;
    }
    
    
    
    // ESCA-JAVA0173: not considering unused parameters for the method.
    
    /**
     * set cell borders
     *
     * @param hasFocus   - true if the cell has the focus by the user
     * @param row        - row of the cell
     * @param column     - column of the cell
     */
    private void setCellBorders(boolean hasFocus, int row, int column) {
        if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            getUncertaintyIcon().setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            setBorder(null);
            getUncertaintyIcon().setBorder(null);
        }
    }
    
    /**
     * @return the firstEditableRow
     */
    public int getFirstEditableRow() {
        return firstEditableRow;
    }
    
    /**
     * @param firstEditableRow the firstEditableRow to set
     */
    public void setFirstEditableRow(int firstEditableRow) {
        this.firstEditableRow = firstEditableRow;
    }
    
    /**
     * @param column index of the column to mark
     */
    public void setMark(int column) {
        if (column < uncertaintyInColumns.length) {
            uncertaintyInColumns[column] = true;
        }
    }
    
    /**
     * @param column index of the column to unmark
     */
    public void unMark(int column) {
        if (column < uncertaintyInColumns.length) {
            uncertaintyInColumns[column] = false;
        }
    }
    
    protected JLabel getUncertaintyIcon() {
        if (jUncertaintyIcon == null) {
            jUncertaintyIcon = new JLabel();
            jUncertaintyIcon.setName("jUncertaintyIcon");
            jUncertaintyIcon.setOpaque(true);
            jUncertaintyIcon.setIcon(IconBind.UNCERTAINTY.icon());
            jUncertaintyIcon.setText("Uncertainty");
            jUncertaintyIcon.setHorizontalAlignment(SwingConstants.RIGHT);
            jUncertaintyIcon.setHorizontalTextPosition(SwingConstants.LEFT);
            jUncertaintyIcon.setIconTextGap(0);
            jUncertaintyIcon.setBackground(GUIColors.Tables.ValuesTable.UNCERTAINTY_BACKGROUND.getColor());
        }
        return jUncertaintyIcon;
    }
    
}
