/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Dialog for selecting a labeled branch to use as a reference in a TreeADD potential.
 */
@SuppressWarnings("serial") public class SetReferenceDialog extends OkCancelDialog {

	private final TreeADDBranch branch;
	private final JComboBox<String> labels;
	private final Map<String, TreeADDBranch> labeledBranches;

	public SetReferenceDialog(Window owner, TreeADDBranch branch, TreeADDPotential rootTreeADDPotential) {
		super(owner);
		this.branch = branch;
		labels = new JComboBox<>();
		labels.setSize(new Dimension(150, 25));
		setMinimumSize(new Dimension(200, 100));
		setLocationRelativeTo(owner);
		getComponentsPanel().add(labels, BorderLayout.NORTH);

		labeledBranches = rootTreeADDPotential.getLabeledBranches();
		for (String label : labeledBranches.keySet()) {
			Potential labeledPotential = labeledBranches.get(label).getPotential();
			if (branch.getParentVariables().containsAll(labeledPotential.getVariables())) {
				labels.addItem(label);
			}
		}
	}

	@Override protected boolean doOkClickBeforeHide() {
		String selectedLabel = labels.getSelectedItem().toString();
		branch.setReferencedBranch(labeledBranches.get(selectedLabel));
		return true;
	}

}
