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
 * Panel with checkboxes for selecting which variables to remove from a TreeADD branch.
 */
@SuppressWarnings("serial") public class RemoveVariablesCheckBoxPanel extends JPanel {

	private final List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	private final TreeADDBranch branch;
	private final TreeADDPotential treeADD;

	public RemoveVariablesCheckBoxPanel(TreeADDBranch branch, TreeADDPotential treeADD) {
		this.branch = branch;
		this.treeADD = treeADD;
		initialize();
		repaint();
	}

	public void initialize() {
		//setLayout(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		List<Variable> posibleVariables = branch.getPotential().getVariables();

		/*for (Variable variable : posibleVariables) {*/
		for (int i = 1; i < posibleVariables.size(); i++) {
			JCheckBox checkBox = new JCheckBox(posibleVariables.get(i).getName());
			//checkBox.setAlignmentX((float) 0.5);
			//checkBox.setAlignmentY((float) 0.5);
			checkBoxes.add(checkBox);
			add(checkBox, CENTER_ALIGNMENT);
		}
	}

	public TreeADDBranch getBranch() {
		return this.branch;
	}

	public TreeADDPotential getTreeADDPotential() {
		return this.treeADD;
	}

	public List<JCheckBox> getCheckBoxes() {
		return this.checkBoxes;
	}
}
