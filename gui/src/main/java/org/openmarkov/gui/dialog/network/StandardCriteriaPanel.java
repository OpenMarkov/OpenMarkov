/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Panel presenting radio buttons for selecting one of the predefined
 * standard decision criteria configurations.
 */
public class StandardCriteriaPanel extends JPanel {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ArrayList<JRadioButton> radioButtons = new ArrayList<JRadioButton>();

	public StandardCriteriaPanel() {
		initialize();
		repaint();
	}

	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ButtonGroup buttonGroup = new ButtonGroup();
		String[] defaultStates = { stringDatabase.getString("defaultCriteria.costBenefitEuros.Text"),
				stringDatabase.getString("defaultCriteria.costBenefitPounds.Text"),
				stringDatabase.getString("defaultCriteria.costBenefitDollars.Text"),
				stringDatabase.getString("defaultCriteria.costEffectivenessEurosQALY.Text"),
				stringDatabase.getString("defaultCriteria.costEffectivenessPoundsQALY.Text"),
				stringDatabase.getString("defaultCriteria.costEffectivenessDollarsQALY.Text") };
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

	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	public ArrayList<JRadioButton> getRadioButtons() {
		return radioButtons;
	}

}
