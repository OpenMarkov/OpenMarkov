/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.IndicatorPotential;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Panel for indicator potential in events.
 * @author cmyago
 * @version 1.0 FIXME resize panel elements
 */

@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = IndicatorPotential.class) public class IndicatorPotentialPanel
		extends PotentialPanel {

	private JTextField tteTextField;
	private JTextField pOcurrenceTextField;

	private JComboBox<String> stateComboBox;
	private JSpinner valueSpinner;
	private Node node;

	public IndicatorPotentialPanel(Node node) {
		super();
		this.node = node;
		initComponents();
		setData(node);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		JPanel namelessPanel = new JPanel();
		namelessPanel.setLayout(new GridLayout(2,2,10, 20));
		namelessPanel.setBorder(new EmptyBorder(15, 400, 15, 400));
//		namelessPanel.setBorder(new EtchedBorder());

		tteTextField = new JTextField(15);

		JLabel tteLabel = new JLabel("Time-to-event:");
		tteLabel.setLabelFor(tteTextField);
		namelessPanel.add(tteLabel);
		namelessPanel.add(tteTextField);
		pOcurrenceTextField = new JTextField(15);
		JLabel probabilityTextLabel = new JLabel("Probability of ocurrence:");
		probabilityTextLabel.setLabelFor(pOcurrenceTextField);
		namelessPanel.add(probabilityTextLabel);
		namelessPanel.add(pOcurrenceTextField);

		add(namelessPanel);

	}

	@Override public void setData(Node node) {
		this.node = node;
		tteTextField.setText(String.valueOf(((IndicatorPotential)(node.getPotentials().get(0))).getTte()));
		pOcurrenceTextField.setText(String.valueOf(((IndicatorPotential)(node.getPotentials().get(0))).getpOccurrence()));
	}

	@Override public boolean saveChanges() throws DoEditException, BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong {
		boolean result = super.saveChanges();
        IndicatorPotential oldPotential = (IndicatorPotential) node.getPotentials().get(0);
		IndicatorPotential newPotential =  new IndicatorPotential(oldPotential);

			newPotential.setTte(Double.parseDouble(tteTextField.getText()));
			newPotential.setpOccurrence(Double.parseDouble(pOcurrenceTextField.getText()));
		new PotentialChangeEdit(node, oldPotential, newPotential).executeEdit();
		return result;
	}

	@Override public void close() {
		// Do nothing
	}

}
