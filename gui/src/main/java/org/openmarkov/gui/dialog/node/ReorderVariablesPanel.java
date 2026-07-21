
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.component.OMTableModel;
import org.openmarkov.gui.dialog.common.KeyTablePanel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Table panel listing the parent variables of a node, allowing the user
 * to reorder them using up/down buttons.
 */
@SuppressWarnings("serial") public class ReorderVariablesPanel extends KeyTablePanel
        implements TableModelListener, PNEditListener {
	private final Node node;
	private final List<PNEdit> edits = new ArrayList<PNEdit>();

	public ReorderVariablesPanel(Node node) {
		super(new String[] { "Variable name" }, getData(node), true, false);
		initialize();
		getAddValueButton().setVisible(false);
		getRemoveValueButton().setVisible(false);
		this.node = node;
		// dataTable = newData;
		tableModel = new OMTableModel(data, columns, true);
		// valuesTable.setModel(tableModel);
		valuesTable.setModifiable(false);
		valuesTable.setModel(tableModel);
		tableModel.addTableModelListener(this);
		defineTableLookAndFeel();
		setData(data);
		// define specific listeners
		// defineTableSpecificListeners();
		// getTableModel().addTableModelListener(this);
	}

	/**
	 * Sets a new table model with new data.
	 *
	 * @param node The node to extract the data from.
	 */
	private static Object[][] getData(Node node) {
		Potential nodePotential = node.getPotentials().get(0);
        //CMI
        Potential potentialForTakingVariables = switch (nodePotential) {
            case UnivariateDistrPotential univariateDistrPotential -> univariateDistrPotential.getAugmentedProbTable();
            case AugmentedProbTablePotential AugmentedProbTablePotential -> AugmentedProbTablePotential.getAugmentedProbTable();
            case ExactDistrPotential exactDistrPotential -> exactDistrPotential.getTablePotential();
            case null, default -> nodePotential;
        };
        List<Variable> variables = potentialForTakingVariables.getVariables();
			
		//CMF
		// 26/11/2014
		// Added node.hasPolicy() to the condition of the if clause when allowing to reorder variables
		// when imposing a policy in a decision node
		if (isConditionalProbabilityNotExactDistrPotential(nodePotential) 
				|| node.hasPolicy()) {
			variables.remove(0);
		}
		Object[][] data = new Object[variables.size()][1];
		for (int i = 0; i < variables.size(); i++) {
			//data[variables.size () - i - 1][0] = variables.get (i).getName ();
			data[i][0] = variables.get(i).getName();
		}
		return data;
	}

	private static boolean isConditionalProbabilityNotExactDistrPotential(Potential potential) {
		return potential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY && !(potential instanceof ExactDistrPotential);
	}

	protected void defineTableLookAndFeel() {
		// center the data in all columns
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
		tcr.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer statesRender = new DefaultTableCellRenderer();
		statesRender.setHorizontalAlignment(SwingConstants.LEFT);
		valuesTable.onTables(omjTable -> {
			int maxColumn = omjTable.getColumnModel().getColumnCount();
			for (int i = 1; i < maxColumn; i++) {
				TableColumn aColumn = omjTable.getColumnModel().getColumn(i);
				aColumn.setCellRenderer(tcr);
				omjTable.getTableHeader().getColumnModel().getColumn(i).setCellRenderer(tcr);
			}
		});
	}

	@Override protected void actionPerformedUpValue(ActionEvent e) {
		int selectedRow = valuesTable.getSelectedRow();
		Object swap = data[selectedRow][0];
		data[selectedRow][0] = data[selectedRow - 1][0];
		data[selectedRow - 1][0] = swap;
		setData(data);
		valuesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
	}

	@Override protected void actionPerformedDownValue(ActionEvent e) {
		int selectedRow = valuesTable.getSelectedRow();
		Object swap = data[selectedRow][0];
		data[selectedRow][0] = data[selectedRow + 1][0];
		data[selectedRow + 1][0] = swap;
		setData(data);
		valuesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
	}

	public List<Variable> getVariables() {
		Potential potential = node.getPotentials().get(0);
		List<Variable> potentialVariables = potential.getVariables();
		List<Variable> newVariables = new ArrayList<Variable>();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < potentialVariables.size(); j++) {
                if (data[i][0].equals(potentialVariables.get(j).getName())) {
					newVariables.add(potentialVariables.get(j));
				}
			}
		}
		//Collections.reverse(newVariables);
		// 26/11/2014
		// Added node.hasPolicy() to the condition of the if clause when allowing to reorder variables
		// when imposing a policy in a decision node
		if (isConditionalProbabilityNotExactDistrPotential(potential) || node.hasPolicy()) {
			newVariables.add(0, potential.getVariables().get(0));
		}
		return newVariables;
	}

	/**
	 * @return The lisf of edits
	 */
	public List<PNEdit> getEdits() {
		return edits;
	}

	@Override public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
	}
}
