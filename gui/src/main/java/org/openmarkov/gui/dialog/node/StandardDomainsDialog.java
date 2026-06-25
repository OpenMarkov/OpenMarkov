/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for selecting a standard domain (predefined set of states) to apply
 * to a discrete variable.
 */
@SuppressWarnings("serial") public class StandardDomainsDialog extends OkCancelDialog {

	private JPanel standardDomainsPanel;

	public StandardDomainsDialog(Window owner) {
		super(owner);
		initialize();
		setLocationRelativeTo(owner);
		setResizable(true);
		pack();

	}

	private void initialize() {

		configureComponentsPanel();
		pack();
	}

	private void configureComponentsPanel() {

		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		getComponentsPanel().add(getJPanelStandardDomains(), BorderLayout.CENTER);

	}

	public JPanel getJPanelStandardDomains() {

		if (standardDomainsPanel == null) {
			standardDomainsPanel = new StandardDomainPanel();
			//statesCheckBoxPanel.setLayout( new FlowLayout());
			standardDomainsPanel.setName("jPanelStandardDomains");
		}
		return standardDomainsPanel;

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
