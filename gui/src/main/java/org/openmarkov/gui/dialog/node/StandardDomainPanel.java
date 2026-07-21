
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.UserPreferences;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Panel presenting radio buttons for selecting one of the predefined standard
 * domains (e.g. absent/present, no/yes, negative/positive).
 */
@SuppressWarnings("serial") public class StandardDomainPanel extends JPanel {
	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ArrayList<JRadioButton> radioButtons = new ArrayList<JRadioButton>();
    
    private List<List<String>> states;
	
	public StandardDomainPanel() {
        this.states = new ArrayList<>(UserPreferences.CUSTOM_DOMAINS.get());
        Stream.of(
                      stringDatabase.getString("defaultStates.absentPresent.Text"),
                      stringDatabase.getString("defaultStates.noYes.Text"),
                      stringDatabase.getString("defaultStates.negativePositive.Text"),
                      stringDatabase.getString("defaultStates.absentMildModerateSevere.Text"),
                      stringDatabase.getString("defaultStates.lowMediumhigh.Text")
              )
              .forEach(rawDomain -> states.add(Arrays.stream(rawDomain.split("-")).map(String::trim).toList()));
		initialize();
		repaint();
	}

	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ButtonGroup buttonGroup = new ButtonGroup();

		// String [] defaultStates = GUIDefaultStates.getListStrings();
		// String [] defaultStates2 =
		// GUIDefaultStates.getStringsLanguageDependent(GUIDefaultStates.getListStrings());
        for (List<String> defaultState : states) {
            JRadioButton radioButton = new JRadioButton(defaultState.stream().collect(Collectors.joining(" - ")));
			// radioButton.addItemListener(this);
			radioButtons.add(radioButton);
			buttonGroup.add(radioButton);
            radioButton.addItemListener(e -> chosenStates = defaultState);
			add(radioButton, BorderLayout.CENTER);
		}
	}


	public ArrayList<JRadioButton> getRadioButtons() {
		return radioButtons;
	}
    
    public List<String> getChosenStates() {
        return Collections.unmodifiableList(this.chosenStates);
    }
    
    private List<String> chosenStates;
}
