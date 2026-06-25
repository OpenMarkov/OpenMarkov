/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.gui;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmType;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for dialogs that configure learning algorithm parameters.
 * Provides a standard layout with an alpha (Laplace correction) field, an Accept button,
 * and a hook for subclasses to add algorithm-specific fields.
 *
 * @author joliva
 * @author ibermejo
 * @author Manuel Arias
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({JFrame.class, boolean.class}))
public abstract class AlgorithmParametersDialog extends JDialog {

	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	/**
	 * Alpha (Laplace-like correction) parameter, shared by all algorithm dialogs.
	 * Default value is 0.5.
	 */
	protected String alphaParameter = "0.5";

	/**
	 * Text field for the alpha parameter, created by {@link #initStandardLayout}.
	 */
	protected JTextField alphaText;

	/**
	 * The main panel containing the algorithm fields, created by {@link #initStandardLayout}.
	 */
	protected JPanel mainPanel;

	/**
	 * @param parent the parent frame
	 * @param modal  whether the dialog is modal
	 */
	public AlgorithmParametersDialog(Frame parent, boolean modal) {
		super(parent, modal);
	}

	/**
	 * A label-field pair to be added to the dialog above the alpha row.
	 */
	public record FieldRow(JLabel label, JComponent field) {}

	/**
	 * Builds the standard dialog layout: a titled panel containing the given custom fields,
	 * an alpha parameter row, and an Accept button. Subclasses should call this from their
	 * constructor after creating any custom fields they need.
	 *
	 * @param titleKey       localization key for the dialog title and panel border
	 * @param customFieldRows label-field pairs to place above the alpha row (may be empty)
	 */
	protected void initStandardLayout(String titleKey, FieldRow... customFieldRows) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(stringDatabase.getString(titleKey));
		setLocationRelativeTo(getParent());

		mainPanel = new JPanel(new GridBagLayout());
		JPanel panel = mainPanel;
		panel.setBorder(BorderFactory.createTitledBorder(stringDatabase.getString(titleKey)));

		alphaText = new JTextField(5);
		alphaText.setText(alphaParameter);
		JLabel alphaLabel = new JLabel(stringDatabase.getString("Learning.Alpha") + ":");
		alphaLabel.setToolTipText(stringDatabase.getString("Learning.Alpha.Tooltip"));

		JButton acceptButton = new JButton(stringDatabase.getString("Learning.Ok"));
		acceptButton.addActionListener(evt -> {
			try {
				applyAlpha();
				acceptCustomFields();
				setVisible(false);
			} catch (InvalidArgumentException e) {
				throw new UnrecoverableException(e);
			}
		});

		GridBagConstraints lc = new GridBagConstraints();
		lc.anchor = GridBagConstraints.WEST;
		lc.insets = new Insets(6, 8, 6, 6);

		GridBagConstraints fc = new GridBagConstraints();
		fc.gridx = 1;
		fc.anchor = GridBagConstraints.WEST;
		fc.fill = GridBagConstraints.HORIZONTAL;
		fc.weightx = 1.0;
		fc.insets = new Insets(6, 0, 6, 8);

		int row = 0;
		for (FieldRow fr : customFieldRows) {
			lc.gridy = row;
			fc.gridy = row;
			panel.add(fr.label(), lc);
			panel.add(fr.field(), fc);
			row++;
		}

		lc.gridy = row;
		fc.gridy = row;
		panel.add(alphaLabel, lc);
		panel.add(alphaText, fc);
		row++;

		GridBagConstraints bc = new GridBagConstraints();
		bc.gridy = row;
		bc.gridwidth = 2;
		bc.anchor = GridBagConstraints.CENTER;
		bc.insets = new Insets(12, 8, 8, 8);
		panel.add(acceptButton, bc);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pack();
	}

	/**
	 * Hook for subclasses to perform additional validation when Accept is clicked.
	 * Called after {@link #applyAlpha()} succeeds.
	 */
	protected void acceptCustomFields() {
		// Default: no extra validation
	}

	/**
	 * Validates the content of {@code alphaText}, and if valid, stores the
	 * value in {@code alphaParameter}. Throws {@link InvalidArgumentException}
     * if the value cannot be parsed as a number in [0, 1].
	 */
	protected void applyAlpha() {
		@Nullable Double alpha;
		try {
			alpha = Double.parseDouble(alphaText.getText());
		} catch (NumberFormatException e) {
			alpha = null;
		}
		if (alpha == null || alpha < 0 || alpha > 1) {
			throw new InvalidArgumentException(alpha, "alpha", "must be between 0 and 1");
		}
		alphaParameter = alphaText.getText();
	}

	public String getAlphaParameter() {
		return alphaParameter;
	}

	/**
	 * Returns the metric names declared in the algorithm's {@link LearningAlgorithmType#metrics()}.
	 */
	protected String[] algorithmMetrics() {
		AlgorithmConfiguration config = getClass().getAnnotation(AlgorithmConfiguration.class);
		return config.algorithm().getAnnotation(LearningAlgorithmType.class).metrics();
	}

	public abstract String getDescription();

	public abstract LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database);

}
