package org.openmarkov.gui.component;

import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class is used for painting and coloring the table and the headers
 * To be mixed to ValuesTableCellRenderer when Tables change
 * @author cmyago
 * @version 1.0  01/09/2019
 * @version 2.0 - 29/08/2023 - cmyago; refactored to ValuesTableWithEventsCellRenderer (from EventValuesTableCellRenderer):impossible configurations commented
 */
public class TableWithEventsCellRenderer extends DefaultTableCellRenderer {

	/**
	 * default serial ID
	 */
	private static final long serialVersionUID = 1L;
	private static final DecimalFormat formatter = new DecimalFormat("0.######", new DecimalFormatSymbols(Locale.US));
	/**
	 * to define the first editable row of the table
	 */
	protected int firstEditableRow;
	private boolean[] impossibleColumns = null;
	private JLabel jImpossibleColumnIcon;

//	/**
//	 * constructor for the renderer
//	 *
//	 * @param firstEditableRow     value of the first editable row
//	 * @param impossibleColumns boolean array with the columns with (1)/without (0) mark. The
//	 *                             array only has to contain indexes for the editables columns
//	 */
//	public TableWithEventsCellRenderer(int firstEditableRow, boolean[] impossibleColumns) {
//		this.impossibleColumns = impossibleColumns;
//		this.firstEditableRow = firstEditableRow;
//	}

	public TableWithEventsCellRenderer(int firstEditableRow) {
		this.firstEditableRow = firstEditableRow;;
	}

//	/**
//	 * headers rows are displayed in a gray background color with red and blue
//	 * foreground alternatively non headers rows are displayed in an alternative
//	 * cyan and light gray background color with black foreground color the
//	 * first two column are in gray
//	 */
//	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
//			int row, int column) {
//		setHorizontalAlignment(SwingConstants.CENTER);
//		setCellFonts(table, value, isSelected, hasFocus, row, column);
//		setCellColors(table, value, isSelected, hasFocus, row, column);
//		setCellBorders(table, value, isSelected, hasFocus, row, column);
//		setMinimumSize(table, value, isSelected, hasFocus, row, column);
//		if (value instanceof Double) {
//			value = formatter.format((Double) value);
//		}
//		if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && (row >= firstEditableRow) && impossibleColumns != null
//				&& impossibleColumns[column - 1]) {
//			setImpossibleColumn().setText(value.toString());
//			return setImpossibleColumn();
//		} else {
//			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//		}
//	}


	/**
	 * headers rows are displayed in a gray background color with red and blue
	 * foreground alternatively non headers rows are displayed in an alternative
	 * cyan and light gray background color with black foreground color the
	 * first two column are in gray
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
												   int row, int column) {
		setHorizontalAlignment(SwingConstants.CENTER);
		setCellFonts(table, value, isSelected, hasFocus, row, column);
		setCellColors(table, value, isSelected, hasFocus, row, column);
		setCellBorders(table, value, isSelected, hasFocus, row, column);
		setMinimumSize(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Double) {
			value = formatter.format(value);
		}
//		if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && (row >= firstEditableRow) && impossibleColumns != null
//				&& impossibleColumns[column - 1]) {
//			setImpossibleColumn().setText(value.toString());
//			return setImpossibleColumn();
//		} else {
//			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}



	private void setMinimumSize(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	}

	/**
	 * set cell fonts
	 *
	 * @param table      - table where the cell is located
	 * @param value      - the value of the cell in edition
	 * @param isSelected - true if the cell is selected by the user
	 * @param hasFocus   - true if the cell has the focus by the user
	 * @param row        - row of the cell
	 * @param column     - column of the cell
	 */
	private void setCellFonts(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
	 * @param table      - table where the cell is located
	 * @param value      - the value of the cell in edition
	 * @param isSelected - true if the cell is selected by the user
	 * @param hasFocus   - true if the cell has the focus by the user
	 * @param row        - row of the cell
	 * @param column     - column of the cell
	 */
	protected void setCellColors(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
			// PARENTS CELLS set alternate colors
			setBackground(GUIColors.Tables.HEADER_BACKGROUND.getColor());
			setForeground(GUIColors.Tables.HEADER_FOREGROUND_COLORS.get(row%GUIColors.Tables.HEADER_FOREGROUND_COLORS.size()).getColor());
		}
		if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
			// NODE STATES CELLS
			setBackground(GUIColors.Tables.HEADER_BACKGROUND.getColor());
			setForeground(GUIColors.Tables.FROZEN_CELL_FOREGROUND.getColor());
		}
		if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
			// HEADER CELLS
			setBackground(GUIColors.Tables.HEADER_BACKGROUND.getColor());
			setForeground(GUIColors.Tables.HEADER_FOREGROUND_COLORS.get(row%GUIColors.Tables.HEADER_FOREGROUND_COLORS.size()).getColor());
		}
		if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && firstEditableRow >= 0 && (row >= firstEditableRow)) {
			ValuesTableCellRenderer.CellColor editableCellColor = GUIColors.Tables.EDITABLE_CELL_COLOR.getEditableCellColor(hasFocus && table.isCellEditable(row, column), row, column);
			setBackground(editableCellColor.background.getColor());
			setForeground(editableCellColor.foreground.getColor());
		}
	}

	// ESCA-JAVA0173: not considering unused parameters for the method.

//	/**
//	 * set cell borders
//	 *
//	 * @param table      - table where the cell is located
//	 * @param value      - the value of the cell in edition
//	 * @param isSelected - true if the cell is selected by the user
//	 * @param hasFocus   - true if the cell has the focus by the user
//	 * @param row        - row of the cell
//	 * @param column     - column of the cell
//	 */
//	private void setCellBorders(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//		setBorder(new LineBorder(Color.BLACK, 5));
//		if (hasFocus) {
//			if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
//				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
//				setImpossibleColumn().setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
//			} else {
//				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
//				setImpossibleColumn().setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
//			}
//		} else {
//			setImpossibleColumn().setBorder(noFocusBorder);
//		}
//	}


	/**
	 * set cell borders
	 *
	 * @param table      - table where the cell is located
	 * @param value      - the value of the cell in edition
	 * @param isSelected - true if the cell is selected by the user
	 * @param hasFocus   - true if the cell has the focus by the user
	 * @param row        - row of the cell
	 * @param column     - column of the cell
	 */
	private void setCellBorders(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setBorder(new LineBorder(Color.BLACK, 5));
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
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

//	/**
//	 * @param column index of the column to mark
//	 */
//	public void setMark(int column) {
//		if (column < impossibleColumns.length) {
//			impossibleColumns[column] = true;
//		}
//	}

//	/**
//	 * @param column index of the column to unmark
//	 */
//	public void setUnMark(int column) {
//		if (column < impossibleColumns.length) {
//			impossibleColumns[column] = false;
//		}
//	}

//	protected JLabel setImpossibleColumn() {
//		if (jImpossibleColumnIcon == null) {
//			iconLoader = new IconLoader();
//			jImpossibleColumnIcon = new JLabel();
//			jImpossibleColumnIcon.setName("jUncertaintyIcon");
//			jImpossibleColumnIcon.setOpaque(true);
//			jImpossibleColumnIcon.setIcon(iconLoader.load(IconLoader.ICON_UNCERTAINTY));
//			jImpossibleColumnIcon.setText("Impossible Configuration");
//			jImpossibleColumnIcon.setHorizontalAlignment(SwingConstants.RIGHT);
//			jImpossibleColumnIcon.setHorizontalTextPosition(SwingConstants.LEFT);
//			jImpossibleColumnIcon.setIconTextGap(0);
//			jImpossibleColumnIcon.setBackground(Color.PINK);
//		}
//		return jImpossibleColumnIcon;
//	}




}
