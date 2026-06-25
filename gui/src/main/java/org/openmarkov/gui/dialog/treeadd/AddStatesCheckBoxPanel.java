/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel with checkboxes for selecting which states to add to a TreeADD branch.
 */
@SuppressWarnings("serial") public class AddStatesCheckBoxPanel extends JPanel {
	private final List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	private final TreeADDBranch branch;
	private final TreeADDPotential treeADD;

	public AddStatesCheckBoxPanel(TreeADDBranch branch, TreeADDPotential treeADD) {
		this.branch = branch;
		this.treeADD = treeADD;
		initialize();
		repaint();
	}

	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// setLayout(new BorderLayout());
		Variable topVariable = branch.getRootVariable();
		State[] states = topVariable.getStates();

		for (State state : states) {
			if (branch.getBranchStates().contains(state)) {
				continue;
			}
			JCheckBox checkBox = new JCheckBox(state.getName());
			// checkBox.setLayout(new BorderLayout());
			checkBoxes.add(checkBox);
			// checkBox.setAlignmentY((float) 0.0);
			// checkBox.setAlignmentX((float) 0.5);
			add(checkBox, BorderLayout.CENTER);

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
