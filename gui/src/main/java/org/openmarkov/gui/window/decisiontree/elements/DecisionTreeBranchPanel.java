/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.decisiontree.elements;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.gui.window.decisiontree.format.DecisionTreeUtilityFormatters;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * GUI representation of a logical decision tree branch.
 * It displays branch-specific information such as variable states and probabilities.
 */
@SuppressWarnings("serial")
public non-sealed class DecisionTreeBranchPanel extends DecisionTreeElementPanel {
	
	/** The underlying logical tree branch. */
	private final DecisionTreeBranch treeBranch;

	/**
	 * Constructs a panel for a specific decision tree branch.
	 * @param treeBranch The logical branch to be represented.
	 */
	public DecisionTreeBranchPanel(DecisionTreeBranch treeBranch) {
		super();
		this.treeBranch = treeBranch;
		super.initialize();
	}
	
	public DecisionTreeBranch getTreeBranch() {
		return treeBranch;
	}

	/**
	 * Builds the foreground to be shown in the branch
	 * @return A HTML string with the branch description
	 */
	public String getBranchDescriptiontHTML() {
		String txtLeft = "<html><table border=1>";
		DecisionTreeNode parent = treeBranch.getParent();
		if (parent != null && parent.getNodeType() == NodeType.DECISION) {
			if (parent.isBestDecision(treeBranch)) {
				txtLeft += "<td width=10px bgcolor=#3FD111 border=0></td>";
			} else {
				txtLeft += "<td width=10px border=0></td>";
			}
		}
		txtLeft += "<td align=center border=0>";
		if (treeBranch.getBranchVariable() != null) {
			txtLeft += treeBranch.getBranchVariable().getName() + "=" + treeBranch.getBranchState().getName();
		}
		if (parent != null && parent.getNodeType() == NodeType.CHANCE) {
			txtLeft += " /  P=" + df.format(treeBranch.getBranchProbability());
		}
		txtLeft += DecisionTreeUtilityFormatters.format(treeBranch.getUtility(), df, parent != null)
				+ "</td></table></html>";
		return txtLeft.toString();
	}
	
	
	@Override public JComponent makeSummary() {
		return new JLabel();
	}
	
	/** {@inheritDoc} */
	@Override public void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		((JLabel) summaryLabel.get()).setText(getBranchDescriptiontHTML());
	}
}
