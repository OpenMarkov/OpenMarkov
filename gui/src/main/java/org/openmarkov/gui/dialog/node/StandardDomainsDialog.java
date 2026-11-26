/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

/**
 * Dialog for selecting a standard domain (predefined set of states) to apply
 * to a discrete variable.
 */
@SuppressWarnings("serial") public class StandardDomainsDialog extends OkCancelDialog {
    
    private StandardDomainPanel standardDomainsPanel;
	private final Node node;
	
	public StandardDomainsDialog(Window owner, Node node) {
		super(owner);
		this.node = node;
		initialize();
		setLocationRelativeTo(owner);
		setResizable(true);
		pack();
	}

	private void initialize() {
		// The title is that of the button that opens it, as nothing said what the window was for (error 180)
		setTitle(stringDatabase.getString("StandardDomain.Text"));
		configureComponentsPanel();
		// «OK» waits for a choice: accepting without one ended in an unexpected error (error 173)
		getOKButton().setEnabled(false);
		for (JRadioButton radioButton : standardDomainsPanel.getRadioButtons()) {
			radioButton.addItemListener(event -> getOKButton().setEnabled(
					standardDomainsPanel.getRadioButtons().stream().anyMatch(JRadioButton::isSelected)));
		}
		pack();
	}

	private void configureComponentsPanel() {
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		getComponentsPanel().add(getJPanelStandardDomains(), BorderLayout.CENTER);
	}
    
    public StandardDomainPanel getJPanelStandardDomains() {
		if (standardDomainsPanel == null) {
			standardDomainsPanel = new StandardDomainPanel(node);
			//statesCheckBoxPanel.setLayout( new FlowLayout());
			standardDomainsPanel.setName("jPanelStandardDomains");
		}
		return standardDomainsPanel;
	}
    
    public List<String> getChosenStates() {
        return standardDomainsPanel.getChosenStates();
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
		return standardDomainsPanel.hasChosenStates();
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {
	}
	
}
