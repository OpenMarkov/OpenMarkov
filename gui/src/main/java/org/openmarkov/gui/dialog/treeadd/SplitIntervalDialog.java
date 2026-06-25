/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.gui.dialog.common.OkCancelDialog;

import java.awt.*;

/**
 * Dialog for specifying the split point when dividing a continuous interval
 * in a TreeADD branch.
 */
@SuppressWarnings("serial") public class SplitIntervalDialog extends OkCancelDialog {

	private SplitIntervalPanel splitIntervalPanel;

	public SplitIntervalDialog(Window owner) {
		super(owner);
		initialize();
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(200, 200));
		//setResizable(true);
		pack();

	}

	private void initialize() {
		setTitle("Split Interval");
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
		getComponentsPanel().add(getJPanelSplitInterval(), BorderLayout.CENTER);

	}

	protected SplitIntervalPanel getJPanelSplitInterval() {

		if (splitIntervalPanel == null) {
			splitIntervalPanel = new SplitIntervalPanel();
			//splitIntervalPanel.setLayout( new FlowLayout() );
			splitIntervalPanel.setName("jPanelSplitInterval");

		}
		return splitIntervalPanel;

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
        return getJPanelSplitInterval().getLimit().getText() != null;
    }

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {

	}

}
