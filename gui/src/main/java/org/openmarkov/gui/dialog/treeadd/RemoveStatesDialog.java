/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for selecting states to dissociate (remove) from a TreeADD branch.
 */
@SuppressWarnings("serial") public class RemoveStatesDialog extends OkCancelDialog {

	private RemoveStatesCheckBoxPanel dissociateStatesCheckBoxPanel;
	private final TreeADDBranch treeADDBranch;
	private final TreeADDPotential parentTreeADD;

	public RemoveStatesDialog(Window owner, TreeADDBranch treeADDBranch, TreeADDPotential parentTreeADD) {
		super(owner);
		this.treeADDBranch = treeADDBranch;
		this.parentTreeADD = parentTreeADD;
		initialize();
		setLocationRelativeTo(owner);
		//setMinimumSize(new Dimension( 100, 100 ));
		setResizable(true);
		pack();

	}

	private void initialize() {

		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		/*dialogStringResource =
				StringResourceLoader.getUniqueInstance().getBundleDialogs();
		messageStringResource =
				StringResourceLoader.getUniqueInstance().getBundleMessages();
		setTitle(dialogStringResource
				.getValuesInAString("NodePotentialDialog.Title));*/
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		getComponentsPanel().add(getJPanelRemoveStates(), BorderLayout.CENTER);

	}

	protected JPanel getJPanelRemoveStates() {

		if (dissociateStatesCheckBoxPanel == null) {
			dissociateStatesCheckBoxPanel = new RemoveStatesCheckBoxPanel(treeADDBranch, parentTreeADD);
			//dissociateStatesCheckBoxPanel.setLayout( new FlowLayout() );
			dissociateStatesCheckBoxPanel.setName("jPanelDissociateBranchStates");

		}
		return dissociateStatesCheckBoxPanel;

	}
    
    public ChosenOption requestValues() {

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
		return true;
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {

	}
}
