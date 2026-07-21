/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Panel that displays radio buttons for selecting which combination of parent
 * variables to condition on when viewing a potential.
 */
@SuppressWarnings("serial") public class VariablesCombinationPanel extends JPanel {

	private final ButtonGroup buttonGroup = new ButtonGroup();

	private final ArrayList<JRadioButton> radioButtons = new ArrayList<JRadioButton>();

	private final Node node;

	public VariablesCombinationPanel(Node node) {
		this.node = node;
		initialize();
		repaint();
	}

	@SuppressWarnings("unchecked") public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ButtonGroup buttonGroup = new ButtonGroup();
		List<Variable> variables = node.getPotentials().get(0).getVariables();
		List<Variable> possibleVariables = new ArrayList<Variable>();
		for (int i = 1; i < variables.size(); i++) {
			possibleVariables.add(variables.get(i));
		}

		List<String> possibilities = new ArrayList<String>();
		int size = possibleVariables.size();
		@SuppressWarnings("rawtypes") Vector vector = new Vector();
		for (int i = 0; i < size; i++) {
			vector.add(possibleVariables.get(i).getName());
		}
		String inicio = vector.toString();
		String fin = "";
		int i = size - 1;
		while (!inicio.equals(fin)) {
			if (i > 0) {
				Object aux = vector.get(i);
				vector.set(i, vector.get(i - 1));
				vector.set(i - 1, aux);
				i--;
			}
			if (i == 0) {
				i = size - 1;
			}
			fin = vector.toString();
			possibilities.add(fin);
		}

		for (int j = possibilities.size() - 1; j >= 0; j--) {
			JRadioButton radioButton = new JRadioButton(possibilities.get(j));
			radioButtons.add(radioButton);
			buttonGroup.add(radioButton);
			add(radioButton, BorderLayout.CENTER);
		}

		radioButtons.get(0).setSelected(true);
	}

	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	public ArrayList<JRadioButton> getRadioButtons() {
		return radioButtons;
	}
}
