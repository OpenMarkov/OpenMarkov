/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.localize.StringDatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * This class implements a potentials table with the following features:
 * <ul>
 * <li>Its elements, except the first column, are modifiable.</li>
 * <li>New elements can be added, creating a new key row with empty data.</li>
 * <li>The key data (first column) consist of a key string following of the
 * index of the row and it is used for internal purposes only.</li>
 * <li>The key data is hidden.</li>
 * <li>The information of a row (except the first column) can not be taken up or
 * down.</li>
 * <li>The rows can not be removed.</li>
 * <li>The first editable row is the one that has the values of the potentials.</li>
 * <li>The rows between 0 and the first editable row are ocuppied by the values
 * of the states of the parents of the variable.</li>
 * <li>The header of columns is hidden.</li>
 * </ul>
 * This class is based upon KeyTablePanel without buttons
 *
 * @author jlgozalo
 * @author maryebra
 * @version 1.0 jlgozalo
 */
public abstract class ProbabilityTablePanel extends PotentialPanel implements ActionListener {

	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 6257314234781632512L;
	/**
	 * Name of the columns of the table.
	 */
    protected String[] columns;

	/**
	 * Data of the cells.
	 */
    protected Object[][] data;
	/**
	 * number of positions in this table
	 */
    protected long position = -1;
	/**
	 * list of variables that are shown in this table
	 */
	protected List<Variable> variables = null;
	
	/**
	 * first editable row (only for temporal storage)
	 */
	protected int firstEditableRow = -1;
	/**
	 * last editable row (only for temporal storage)
	 */
	protected int lastEditableRow = -1;
	/**
	 * Properties for options to display in the table
	 */
	protected boolean showAllParameters = true;
	protected boolean showNetValues = true;
	
	// private Node node;
	protected EvidenceCase evidenceCase;
	/**
	 * index of the column selected in valuesTable
	 */
	protected int selectedColumn = -1;
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	
	/**
	 * this is a default constructor with no construction parameters
	 *
	 *
	 */
	public ProbabilityTablePanel() {
		this(new String[] { "id", "states", "values" }, new Object[][] { new Object[] { 0, null, 0 } }); // default init
	}

	/**
	 * This is the default constructor
	 *
	 * @param newColumns array of texts that appear in the header of the columns.
	 * @param newData    content of the cells.
	 */
	public ProbabilityTablePanel(String[] newColumns, Object[][] newData) {
		columns = newColumns.clone();
		data = newData.clone();
		repaint();
	}
	
	/**
	 * gets the first row on edition
	 *
	 * @return first row for edition
	 */
	protected int getFirstEditableRow() {
		return this.firstEditableRow;
	}

	/**
	 * sets the first row for edition
	 *
	 * @param firstEditableRow position of the first row that is available for edition
	 */
	protected void setFirstEditableRow(int firstEditableRow) {
		this.firstEditableRow = firstEditableRow;
	}

	/**
	 * @return the lastEditableRow
	 */
	protected int getLastEditableRow() {
		return lastEditableRow;
	}

	/**
	 * @param lastEditableRow the lastEditableRow to set
	 */
	protected void setLastEditableRow(int lastEditableRow) {

		this.lastEditableRow = lastEditableRow;
	}
	
	/**
	 * @param position the position to set
	 */
    protected void setPosition(long position) {
		this.position = position;
	}

	/**
	 * @return the variables
	 */
	protected List<Variable> getVariables() {
		return variables;
	}
	
	/**
	 * Set the Base index for the coordinates in the table related to the
	 * Potential of the variable of this node
	 *
	 * @param value - the new base index for coordinates in the table
	 */
	protected void setBaseIndexForCoordinates(int value) {
    }
	
	// @Override
	@Override public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public Object[][] getData() {
		return this.data;
	}

}
