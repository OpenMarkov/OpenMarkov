/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.gui.dialog.common.KeyTablePanel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.List;

/**
 * Table panel for editing the advanced (user-defined) properties of network
 * elements such as agents and decision criteria.
 */
@SuppressWarnings("serial") public class AdvancedPropertiesTablePanel extends KeyTablePanel
        implements TableModelListener, PNEditListener {
    
    protected Object[][] dataTable;
	private String keyPrefix;
	private AdvancedPropertiesTableModel advancedPropertiestableModel;
	/**
	 * Each time an agent has been edited the corresponding edit would be stored
	 */
	private final List<PNEdit> edits = new ArrayList<PNEdit>();

	public AdvancedPropertiesTablePanel(String[] newColumns, ProbNet probNet) {
		this(newColumns, new Object[0][0], "a");
	}

	public AdvancedPropertiesTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix) {
		super(newColumns, new Object[0][0], true, true, true);
		initialize();
		setData(noKeyData);
		defineTableLookAndFeel();            // define specific listeners
		//defineTableSpecificListeners();
		//getTableModel().addTableModelListener(this);
	}

	/**
	 * Sets a new table model with new data.
	 *
	 * @param newData new data for the table without the key column.
	 */
	@Override public void setData(Object[][] newData) {

		if (newData != null) {
			//dataTable = newData;
			data = fillDataKeys(newData);
			//tableModel = new DefaultTableModel(data, columns);
			advancedPropertiestableModel = new AdvancedPropertiesTableModel(data, columns);
			//valuesTable.setModel(tableModel);
			valuesTable.setModel(advancedPropertiestableModel);
			valuesTable.getModel().addTableModelListener(this);
			this.defineTableLookAndFeel();
		}
	}

	public void setDataTable(Object[][] dataTable) {
		this.dataTable = dataTable;
	}

	protected void defineTableLookAndFeel() {

		// center the data in all columns
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
		tcr.setHorizontalAlignment(SwingConstants.LEFT);

		DefaultTableCellRenderer statesRender = new DefaultTableCellRenderer();
		statesRender.setHorizontalAlignment(SwingConstants.LEFT);

		this.valuesTable.onTables(omjTable -> {
			int maxColumn = omjTable.getColumnModel().getColumnCount();
			
			for (int i = 1; i < maxColumn; i++) {
				TableColumn aColumn = omjTable.getColumnModel().getColumn(i);
				aColumn.setCellRenderer(tcr);
				omjTable.getTableHeader().getColumnModel().getColumn(i).setCellRenderer(tcr);
			}
		});
	}

	/**
	 * This method takes a data object and creates a new column that content a
	 * row key. This key begins with the key prefix following a number that
	 * starts at 0.
	 *
	 * @param oldData data to add a key column.
	 * @return a data object with one more column that contains the keys.
	 */
	protected Object[][] fillDataKeys(Object[][] oldData) {
        
        Object[][] newData;
        int i1; // aux int
        int i2; // aux int
        int l2; // num of columns
        
        // num of rows
        int l1 = oldData.length;
		if (l1 > 0) {
			l2 = oldData[0].length + 1;
			newData = new Object[l1][l2];
			for (i1 = 0; i1 < l1; i1++) {
				newData[i1][0] = getKeyString(i1);
				for (i2 = 1; i2 < l2; i2++) {
					newData[i1][i2] = oldData[i1][i2 - 1];
				}
			}
			return newData;
		}
		return new Object[0][0];
	}

	/**
	 * Returns a key represented by an index.
	 *
	 * @param index index of the key which will be returned
	 * @return the string that content the key.
	 */
	protected String getKeyString(int index) {

		return keyPrefix + index;

	}

	protected void setDataFromAdvancedProperties(List<StringWithProperties> advancedProperties) {
        Object[][] tableData;
        if (advancedProperties == null) {
            tableData = new Object[0][0];
        } else {
            tableData = new Object[advancedProperties.size()][1];
			for (int i = 0; i < advancedProperties.size(); i++) {
				tableData[i][0] = advancedProperties.get(i).getString();
			}
        }
        setData(tableData);
    }

	protected void setDataFromCriteria(List<Criterion> criteria) {
        Object[][] tableData;
        if (criteria != null) {
            tableData = new Object[criteria.size()][2];
			for (int i = 0; i < criteria.size(); i++) {
				tableData[i][0] = criteria.get(i).getCriterionName();
				tableData[i][1] = criteria.get(i).getCriterionUnit();
			}
        } else {
            tableData = new Object[0][0];
        }
        setData(tableData);
        
    }

	/**
	 * @return The edits
	 */
	public List<PNEdit> getEdits() {
		return edits;
	}

	@Override public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
	}

}
