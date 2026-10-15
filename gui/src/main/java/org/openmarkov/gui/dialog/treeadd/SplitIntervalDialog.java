/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.JOptionPane;
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
	 * before hide the dialog: it is closed only if the threshold is a number.
	 *
	 * @return true if the dialog box can be closed.
	 */
	@Override protected boolean doOkClickBeforeHide() {
		if (isANumber(getJPanelSplitInterval().getLimit().getText())) {
			return true;
		}
		JOptionPane.showMessageDialog(this, "The threshold value must be a number, such as 1.5.", "Invalid number",
		                              JOptionPane.ERROR_MESSAGE);
		return false;
	}

	/** Whether the editor of the tree can read {@code text} as a number. */
	static boolean isANumber(String text) {
		try {
			return !Double.isNaN(Double.parseDouble(text));
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {

	}

}
