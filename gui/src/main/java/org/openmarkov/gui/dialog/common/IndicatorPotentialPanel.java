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
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.ParseException;

/**
 * Panel for indicator potential in events.
 * @author cmyago
 * @version 1.0 FIXME resize panel elements
 */

@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = IndicatorPotential.class) public class IndicatorPotentialPanel
		extends PotentialPanel {

	private JSpinner tteSpinner;
	private JSpinner pOccurrenceSpinner;

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

		// The spinners only accept valid values: a time not below 0 and a probability between 0 and 1
		tteSpinner = numberSpinner(new SpinnerNumberModel(Double.valueOf(0), Double.valueOf(0), null, Double.valueOf(1)));

		JLabel tteLabel = new JLabel("Time-to-event:");
		tteLabel.setLabelFor(tteSpinner);
		namelessPanel.add(tteLabel);
		namelessPanel.add(tteSpinner);
		pOccurrenceSpinner = numberSpinner(new SpinnerNumberModel(0, 0, 1, 0.01));
		JLabel probabilityTextLabel = new JLabel("Probability of ocurrence:");
		probabilityTextLabel.setLabelFor(pOccurrenceSpinner);
		namelessPanel.add(probabilityTextLabel);
		namelessPanel.add(pOccurrenceSpinner);

		add(namelessPanel);

	}

	@Override public void setData(Node node) {
		this.node = node;
		tteSpinner.setValue(((IndicatorPotential)(node.getPotentials().get(0))).getTte());
		pOccurrenceSpinner.setValue(((IndicatorPotential)(node.getPotentials().get(0))).getpOccurrence());
	}

	/**
	 * A spinner for {@code model} that shows all the decimals of its value (the default editor shows 0.0001 as 0) and
	 * rejects a text that is a number only in part, such as «1,5», instead of reading it as 1.
	 */
	private static JSpinner numberSpinner(SpinnerNumberModel model) {
		JSpinner spinner = new JSpinner(model);
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.################");
		editor.getFormat().setStrict(true);
		spinner.setEditor(editor);
		return spinner;
	}

	/** The value written in {@code spinner} if it is valid; otherwise, its last valid value. */
	private static double valueOf(JSpinner spinner) {
		try {
			spinner.commitEdit();
		} catch (ParseException e) {
			// Not a number or out of range: the last valid value is kept
		}
		return ((Number) spinner.getValue()).doubleValue();
	}

	@Override public boolean saveChanges() throws DoEditException, BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong {
		boolean result = super.saveChanges();
        IndicatorPotential oldPotential = (IndicatorPotential) node.getPotentials().get(0);
		IndicatorPotential newPotential =  new IndicatorPotential(oldPotential);

		newPotential.setTte(valueOf(tteSpinner));
		newPotential.setpOccurrence(valueOf(pOccurrenceSpinner));
		new PotentialChangeEdit(node, oldPotential, newPotential).executeEdit();
		return result;
	}

	@Override public void close() {
		// Do nothing
	}

}
