
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.base.CloseEditStackOptions;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog that allows the user to reorder the parent variables of a node,
 * changing the variable ordering in its potential.
 */
@SuppressWarnings("serial") public class ReorderVariablesDialog extends OkCancelDialog {
	private JPanel variablesCombinationPanel;
	private final Node node;
	private ReorderVariablesPanel reorderVariablesPanel;

	public ReorderVariablesDialog(Window owner, Node node) {
		super(owner);
		this.node = node;
		node.getProbNet().getPNESupport().setWithUndo(true);
        node.getProbNet().getPNESupport().openNewSubEditHistory();
		initialize();
		setLocationRelativeTo(owner);
		setName("ReorderVariablesDialog");
		// setMinimumSize(new Dimension( 100, 100 ));
		setResizable(true);
		pack();
	}

	private void initialize() {
        setTitle(stringDatabase.getString("NodePotentialReorderVariables.Title"));
		configureComponentsPanel();
		pack();
	}

	private void configureComponentsPanel() {
		// getComponentsPanel().setLayout(new BorderLayout(5, 5));
		// getComponentsPanel().add( getVariablesCombinationPanel(),
		// BorderLayout.CENTER );
		getComponentsPanel().add(getReorderVariablesPanel());
	}

	protected JPanel getVariablesCombinationPanel() {
		if (variablesCombinationPanel == null) {
			variablesCombinationPanel = new VariablesCombinationPanel(node);
			// dissociateStatesCheckBoxPanel.setLayout( new FlowLayout() );
			variablesCombinationPanel.setName("variablesCombinationPanel");
		}
		return variablesCombinationPanel;
	}

	public ReorderVariablesPanel getReorderVariablesPanel() {
		if (reorderVariablesPanel == null) {
			reorderVariablesPanel = new ReorderVariablesPanel(node);
			reorderVariablesPanel.setName("networkAgentsPanel");
			reorderVariablesPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		return reorderVariablesPanel;
	}
    
    public ChosenOption requestValues() {
		getReorderVariablesPanel();
		setVisible(true);
        return getSelectedOption();
	}

	/**
	 * This method carries out the actions when the user press the Ok button
	 * before hide the dialog.
	 *
	 * @return true if the dialog box can be closed.
	 */
	@Override protected boolean doOkClickBeforeHide() {
        node.getProbNet().getPNESupport().closeSubEditHistory();
		return true;
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {
        node.getProbNet().getPNESupport().closeSubEditHistory(CloseEditStackOptions.FORGET);
		// TODO PNESupport must support more depth levels parenthesis
		// As current performance edits from ReorderVariablesPanel only be
		// undone when cancel
		// NodesPropertiesDialog
		for (int i = getReorderVariablesPanel().getEdits().size() - 1; i >= 0; i--) {
			getReorderVariablesPanel().getEdits().get(i).undo();
		}
	}
}
