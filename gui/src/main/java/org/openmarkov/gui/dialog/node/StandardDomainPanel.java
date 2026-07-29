
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.gui.util.GUIDefaultStates;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Panel presenting radio buttons for selecting one of the standard domains on offer (e.g.
 * absent/present, no/yes, negative/positive), which are the ones the user wrote in the preferences
 * followed by the ones the program brings.
 */
@SuppressWarnings("serial") public class StandardDomainPanel extends JPanel {
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ArrayList<JRadioButton> radioButtons = new ArrayList<JRadioButton>();
    
    private final List<List<String>> states;

	public StandardDomainPanel() {
        this.states = GUIDefaultStates.getAllDomains();
		initialize();
		repaint();
	}

	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ButtonGroup buttonGroup = new ButtonGroup();

        for (List<String> defaultState : states) {
            JRadioButton radioButton = new JRadioButton(GUIDefaultStates.getString(defaultState));
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
