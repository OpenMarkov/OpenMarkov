/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.configuration;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;

/**
 * ColorTableCellEditor handles the edition for colors in the Preferences Editor
 *
 * @author jlgozalo
 * @version 1.0 - 11 Mar 2010 - initial version
 */

@SuppressWarnings("serial") public class ColorTableCellEditor extends AbstractCellEditor implements TableCellEditor {

	/**
	 * colorChooser for the panel
	 */
	private final JColorChooser colorChooser;
	/**
	 * dialog to implement the Color Chooser
	 */
	private final JDialog colorDialog;
	/**
	 * panel to include the dialog
	 */
	private final JPanel panel;

	public ColorTableCellEditor() {
		panel = new JPanel();
		// prepare color dialog
		colorChooser = new JColorChooser();
		colorDialog = JColorChooser.createDialog(null, "OpenMarkov colors", false, colorChooser,
				new ActionListener() { //OK button listener
					@Override public void actionPerformed(ActionEvent event) {
						stopCellEditing();
					}
				}, new ActionListener() { //Cancel button listener
					@Override public void actionPerformed(ActionEvent event) {
						cancelCellEditing();
					}
				});
		colorDialog.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent event) {
				cancelCellEditing();
			}
		});
	}

	/**
	 * This is where we get the current Color value. We store it in the dialog
	 * in case the user starts editing
	 *
	 * @see TableCellEditor#getTableCellEditorComponent(JTable, Object, boolean, int, int)
	 */
	@Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		colorChooser.setColor((Color) value);
		return panel;
	}

	/**
	 * @param anEvent event of the table
	 * @return true if the cell should selected
	 */
	public boolean shouldSelectedCell(EventObject anEvent) {
		//starts editing
		colorDialog.setVisible(true);
		//tell caller it is ok to select this cell
		return true;
	}

	@Override public void cancelCellEditing() {
		//editing is canceled-hide dialog
		colorDialog.setVisible(false);
		super.cancelCellEditing();
	}

	@Override public boolean stopCellEditing() {
		// editing is complete-hide dialog
		colorDialog.setVisible(false);
		super.stopCellEditing();
		// tell caller if is ok to use color value
		return true;
	}

	@Override public Object getCellEditorValue() {
		return colorChooser.getColor();
	}

}
