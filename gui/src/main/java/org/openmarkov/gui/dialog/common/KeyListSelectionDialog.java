/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.gui.component.OMTableModel;
import org.openmarkov.gui.exception.NoRowSelectedException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * This class implements a dialog box where the user can select various elements
 * from a table.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo not showing the id column
 */
public class KeyListSelectionDialog extends OkCancelDialog {
	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 1059576863283664256L;
	/**
	 * Elements of the table.
	 */
    private final Object[][] data;
	/**
	 * Columns of the table.
	 */
    private final String[] columns;
	/**
	 * Selected rows of the table.
	 */
	private Object[][] selectedRows = null;
	/**
	 * Panel that contains the scroll pane.
	 */
	private JPanel valuesTableScrollPane = null;
	/**
	 * Panel to scroll the table.
	 */
	private JScrollPane subValuesTableScrollPane = null;
	/**
	 * Table where show the values.
	 */
	private KeyTable valuesTable = null;
	/**
	 * Model table.
	 */
	private OMTableModel tableModel = null;

	/**
	 * Constructor that calls the superclass constructor and saves the objects.
	 *
	 * @param owner      window that owns this dialog box.
	 * @param title      title of the dialog box.
	 * @param newData    content of the rows of the table.
	 * @param newColumns titles of the columns of the table.
	 */
	public KeyListSelectionDialog(Window owner, String title, Object[][] newData, String[] newColumns) {
		super(owner);
		data = newData.clone();
		columns = newColumns.clone();
		initialize(title);
		setLocationRelativeTo(owner);
	}

	/**
	 * This method configures the dialog box.
	 *
	 * @param title title of the dialog box.
	 */
	private void initialize(String title) {
		setTitle(title);
		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		getComponentsPanel().add(getValuesTableScrollPane());
	}

	/**
	 * This method initialises valuesTableScrollPane.
	 *
	 * @return a new panel that contains the scroll pane.
	 */
	private JPanel getValuesTableScrollPane() {
		if (valuesTableScrollPane == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(5, 5, 0, 5);
			valuesTableScrollPane = new JPanel();
			valuesTableScrollPane.setLayout(new GridBagLayout());
			valuesTableScrollPane.add(getSubValuesTableScrollPane(), gridBagConstraints);
		}
		return valuesTableScrollPane;
	}

	/**
	 * This method initialises subValuesTableScrollPane.
	 *
	 * @return a new values table scroll pane.
	 */
	private JScrollPane getSubValuesTableScrollPane() {
		if (subValuesTableScrollPane == null) {
			subValuesTableScrollPane = new JScrollPane();
			subValuesTableScrollPane.setViewportView(getValuesTable());
			subValuesTableScrollPane.setMinimumSize(new Dimension(240, 180));
			subValuesTableScrollPane.setMaximumSize(new Dimension(240, 180));
			valuesTableScrollPane.setPreferredSize(new Dimension(240, 180));
		}
		return subValuesTableScrollPane;
	}

	/**
	 * This method initialises valuesTable.
	 *
	 * @return a new values table.
	 */
	private KeyTable getValuesTable() {
		if (valuesTable == null) {
			valuesTable = new KeyTable(getTableModel(), false, true, true);
			valuesTable.onTables(omjTable -> omjTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION));
		}
		return valuesTable;
	}

	/**
	 * This method initialises tableModel.
	 *
	 * @return a new tableModel.
	 */
	private OMTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new OMTableModel(data, columns, true);
		}
		return tableModel;
	}

	// ESCA-JAVA0025:

	/**
	 * Cancel the operation.
	 */
	@Override protected void doCancelClickBeforeHide() {
	}

	/**
	 * Checks if the user has selected any element.
	 *
	 * @return true if the dialog box must be closed; otherwise, false.
	 */
    @Override protected boolean doOkClickBeforeHide() throws NoRowSelectedException {
		fillSelectedRows();
		if (selectedRows == null) {
            throw new NoRowSelectedException();
		}
		return true;
	}

	/**
	 * Returns the selected elements of the table.
	 *
	 * @return an array that contains the selected elements of the table or null
	 * if nothing is selected.
	 */
	public Object[][] getSelectedRows() {
		return selectedRows.clone();
	}

	/**
	 * Fill the array of the selected rows.
	 */
	private void fillSelectedRows() {
		int selectedRowCount = valuesTable.getSelectedRowCount();
        int index;
        int[] selectedIndexes;
		if (selectedRowCount > 0) {
			selectedIndexes = valuesTable.getSelectedRows();
			selectedRows = new Object[selectedRowCount][columns.length];
			for (index = 0; index < selectedRowCount; index++) {
				selectedRows[index] = data[selectedIndexes[index]];
			}
		} else {
			selectedRows = null;
		}
	}

	/**
	 * This method shows the dialog and requests the user to select at least one
	 * row.
	 *
	 * @return OK_BUTTON if the user has pressed the 'Ok' button or CANCEL_BUTTON
	 * if the user has pressed the 'Cancel' button.
	 */
    public ChosenOption requestSelectRows() {
		setVisible(true);
        return getSelectedOption();
	}
}
