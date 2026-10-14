/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel with checkboxes for selecting which variables to add to a TreeADD subtree.
 */
@SuppressWarnings("serial") public class AddVariablesCheckBoxPanel extends JPanel {

	private final ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	private final List<Variable> variables = new ArrayList<>();
	private final TreeADDBranch branch;
	private final TreeADDPotential treeADD;

	public AddVariablesCheckBoxPanel(TreeADDBranch branch, TreeADDPotential treeADD) {
		// super();
		this.branch = branch;
		this.treeADD = treeADD;
		initialize();
		repaint();
	}

	public void initialize() {
		// setLayout(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		List<Variable> possibleVariables = branch.getAddableVariables();
		for (Variable variable : possibleVariables) {
			JCheckBox checkBox = new JCheckBox(variable.getName());
			checkBoxes.add(checkBox);
			variables.add(variable);
			add(checkBox, CENTER_ALIGNMENT);
		}
	}

	/** The variables whose boxes are checked. */
	public List<Variable> getSelectedVariables() {
		List<Variable> selected = new ArrayList<>();
		for (int i = 0; i < checkBoxes.size(); i++) {
			if (checkBoxes.get(i).isSelected()) {
				selected.add(variables.get(i));
			}
		}
		return selected;
	}

	public TreeADDBranch getBranch() {
		return this.branch;
	}

	public TreeADDPotential getTreeADDPotential() {
		return this.treeADD;
	}

	public ArrayList<JCheckBox> getCheckBoxes() {
		return this.checkBoxes;
	}
}
