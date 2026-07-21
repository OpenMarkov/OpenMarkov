/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Read-only panel for displaying the contents of a {@link GTablePotential}.
 * <p>
 * {@code GTablePotential} stores a {@code List<E>} of generic elements (typically
 * {@code CEP} or {@code Choice}) indexed by the same variable configurations as a
 * {@code TablePotential}. This panel displays each configuration alongside the
 * {@code toString()} representation of the corresponding element.
 * <p>
 * This potential is created internally during cost-effectiveness inference and is not
 * user-editable, so the panel is always read-only.
 */
@SuppressWarnings("serial")
@PotentialPanelPlugin(potentialClasses = {GTablePotential.class})
public class GTablePotentialPanel extends PotentialPanel {

	private JTable table;

	public GTablePotentialPanel(Node node) {
		setLayout(new BorderLayout());
		try {
			setData(node);
		} catch (ThereIsNoPotentialsInNodeException e) {
			add(new JLabel("No potential available."), BorderLayout.CENTER);
		}
	}

	@Override
	public void setData(Node node) throws ThereIsNoPotentialsInNodeException {
		removeAll();

		Potential potential = node.getFirstPotential();
		if (!(potential instanceof GTablePotential<?> gTablePotential)) {
			add(new JLabel("Not a GTablePotential."), BorderLayout.CENTER);
			return;
		}

		List<Variable> variables = gTablePotential.getVariables();
		List<?> elementTable = gTablePotential.elementTable;
		int numVariables = variables.size();
		int numElements = elementTable.size();

		// Column names: one per variable + "Value"
		String[] columnNames = new String[numVariables + 1];
		for (int i = 0; i < numVariables; i++) {
			columnNames[i] = variables.get(i).getName();
		}
		columnNames[numVariables] = "Value";

		// Build table data
		Object[][] data = new Object[numElements][numVariables + 1];
		for (int i = 0; i < numElements; i++) {
			if (numVariables > 0) {
				int[] configuration = gTablePotential.getConfiguration(i);
				for (int j = 0; j < numVariables; j++) {
					Variable variable = variables.get(j);
					data[i][j] = variable.getStateName(configuration[j]);
				}
			}
			Object element = elementTable.get(i);
			data[i][numVariables] = element != null ? element.toString() : "";
		}

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		// Info label at the top
		String typeInfo = numElements > 0
				? elementTable.get(0).getClass().getSimpleName()
				: "empty";
		JLabel infoLabel = new JLabel(
				"  GTablePotential<%s>  —  %d configurations  (read-only)".formatted(typeInfo, numElements));
		infoLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		add(infoLabel, BorderLayout.NORTH);

		revalidate();
		repaint();
	}

	@Override
	public void close() {
		// Nothing to clean up — this panel is read-only
	}
}
