/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for editing the numerical parameters (e.g. shape, scale) of a probability
 * density function used in model uncertainty specifications.
 */
@SuppressWarnings("serial") public class DistributionParameterDialog extends OkCancelDialog {

    /** English help shown when hovering the {@code nu} label of the Complement distribution. */
    private static final String NU_HELP_TOOLTIP =
            "<html><b>nu</b> &mdash; weight of this Complement entry.<br>"
            + "The probability mass left unused by the other entries in the column is shared among<br>"
            + "the Complement entries in proportion to their nu values, so the column still adds up to 1.<br>"
            + "With a single Complement entry, nu can be left empty: it takes all the remaining mass.</html>";

    private double[] parameters;
    private final List<TextField> parameterTextFields;
	public DistributionParameterDialog(Window owner, String distributionType, double[] parameters) {
		super(owner);
		this.parameters = parameters;
		ProbDensFunctionManager distrManager = ProbDensFunctionManager.getUniqueInstance();
		String[] parameterNames = distrManager.getParameters(distributionType);
		this.parameterTextFields = new ArrayList<>(parameterNames.length);

		setTitle(distributionType);

		BorderLayout layout = new BorderLayout(5, 5);
		getComponentsPanel().setLayout(layout);

		JPanel parametersPanel = new JPanel(new GridLayout(parameterNames.length, 1, 5, 3));
		parametersPanel.setBorder(new TitledBorder("Parameters"));

		for (int i = 0; i < parameterNames.length; ++i) {
			JLabel parameterLabel = new JLabel(parameterNames[i]);
			if ("nu".equals(parameterNames[i])) {
				// AWT TextField has no tooltip; show the nu help on its (Swing) label instead.
				parameterLabel.setToolTipText(NU_HELP_TOOLTIP);
			}
			TextField parameterTextField = new TextField(5);
			if (parameters != null) {
				parameterTextField.setText(String.valueOf(parameters[i]));
			}
			parameterTextField.addFocusListener(new TextFieldFocusListener());
			parametersPanel.add(parameterLabel);
			parametersPanel.add(parameterTextField);
			parameterTextFields.add(parameterTextField);
		}
		getComponentsPanel().add(parametersPanel, BorderLayout.NORTH);
		pack();

		Point parentLocation = owner.getLocation();
		Dimension parentSize = owner.getSize();
		int x = (int) (parentLocation.getX() + parentSize.getWidth() / 2 - getSize().getWidth() / 2);
		int y = (int) (parentLocation.getY() + parentSize.getHeight() / 2 - getSize().getHeight() / 2);
		setLocation(new Point(x, y));

		if (parameterTextFields.isEmpty()) {
			this.setVisible(false);
		}
	}

	public DistributionParameterDialog(Window owner, String distributionType) {
		this(owner, distributionType, null);
	}

	@Override protected boolean doOkClickBeforeHide() {
		if (parameters == null) {
			parameters = new double[parameterTextFields.size()];
		}
		for (int i = 0; i < parameterTextFields.size(); ++i) {
			String text = parameterTextFields.get(i).getText().trim();
			if (text.isEmpty()) {
				// Leave the parameter unspecified (0). The caller (UncertainValuesDialog) validates the
				// family as a whole and, for a single Complement entry, supplies the nu it needs.
				parameters[i] = 0;
				continue;
			}
			try {
				parameters[i] = Double.parseDouble(text);
			} catch (NumberFormatException ex) {
				// Show a message and keep the dialog open instead of letting the exception reach the
				// event-dispatch thread as an uncaught error.
				JOptionPane.showMessageDialog(this,
						"Please enter a valid number for every parameter.",
						"Invalid parameter",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	@Override protected void doCancelClickBeforeHide() {
		if (parameters == null) {
			parameters = new double[parameterTextFields.size()];
		}
	}

	public double[] getParameters() {
		return parameters;
	}
    
    private static class TextFieldFocusListener implements FocusListener {
		@Override public void focusGained(FocusEvent e) {
			((TextComponent) e.getSource()).selectAll();

		}

		@Override public void focusLost(FocusEvent e) {
			// ignore

		}

	}

}
