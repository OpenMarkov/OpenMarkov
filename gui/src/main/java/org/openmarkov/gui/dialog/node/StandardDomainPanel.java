
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * Panel presenting radio buttons for selecting one of the predefined standard
 * domains (e.g. absent/present, no/yes, negative/positive).
 */
@SuppressWarnings("serial") public class StandardDomainPanel extends JPanel implements ItemListener {
	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ArrayList<JRadioButton> radioButtons = new ArrayList<JRadioButton>();

	public StandardDomainPanel() {
		initialize();
		repaint();
	}

	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ButtonGroup buttonGroup = new ButtonGroup();
		String[] defaultStates = { stringDatabase.getString("defaultStates.absentPresent.Text"),
				stringDatabase.getString("defaultStates.noYes.Text"),
				stringDatabase.getString("defaultStates.negativePositive.Text"),
				stringDatabase.getString("defaultStates.absentMildModerateSevere.Text"),
				stringDatabase.getString("defaultStates.lowMediumhigh.Text") };
		// String [] defaultStates = GUIDefaultStates.getListStrings();
		// String [] defaultStates2 =
		// GUIDefaultStates.getStringsLanguageDependent(GUIDefaultStates.getListStrings());
		for (String defaultState : defaultStates) {
			JRadioButton radioButton = new JRadioButton(defaultState);
			// radioButton.addItemListener(this);
			radioButtons.add(radioButton);
			buttonGroup.add(radioButton);
			add(radioButton, BorderLayout.CENTER);
		}
	}

	@Override public void itemStateChanged(ItemEvent e) {
		//String states = ((JRadioButton) e.getItem ()).getName ();
	}

	public ArrayList<JRadioButton> getRadioButtons() {
		return radioButtons;
	}
}
