
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.link;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import java.awt.*;
import java.text.MessageFormat;

/**
 * This class implements the revelation arc dialog box for the edition of the
 * revelation arc properties.
 */
@SuppressWarnings("serial") public class RevelationArcEditDialog extends OkCancelDialog {
	private final Link<Node> link;
	/***
	 * Jpanel showing the values table of the first node
	 */
	private RevelationArcPanel revelationArcPanel;

	public RevelationArcEditDialog(Window owner, Link<Node> link) {
		super(owner);
		this.link = link;
        link.getFrom().getProbNet().getPNESupport().openNewSubEditHistory();
		initialize();
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(750, 450));
		setResizable(true);
	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {
        Node node2 = link.getTo();
		String title = "";
		if (link != null) {
            MessageFormat messageForm = new MessageFormat(stringDatabase.getString("RevelationArcDialog.Title"));
			Object[] labelArgs = new Object[] { node2.getName() };
			title = messageForm.format(labelArgs);
		}
		setTitle(title);
		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		getComponentsPanel().add(getRevelationArcPanel(), BorderLayout.CENTER);
	}

	private RevelationArcPanel getRevelationArcPanel() {
		if (this.revelationArcPanel == null) {
			this.revelationArcPanel = new RevelationArcPanel(link);
		}
		return revelationArcPanel;
	}

	/**
	 * @return An integer indicating the button clicked by the user when closing
	 * this dialog
	 */
    public ChosenOption requestValues() {
		revelationArcPanel.setFieldsFromProperties(link);
		setVisible(true);
        return getSelectedOption();
	}

	/**
	 * This method carries out the actions when the user presses the OK button
	 * before hiding the dialog.
	 *
	 * @return true if all the fields are correct.
	 */
	@Override protected boolean doOkClickBeforeHide() {
		getRevelationArcPanel().saveChanges();
        link.getFrom().getProbNet().getPNESupport().closeSubEditHistory();
		return true;
	}

	@Override protected void doCancelClickBeforeHide() {
        link.getFrom().getProbNet().getPNESupport().cancelLastSubEditHistory();
	}
}
