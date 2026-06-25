/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.menutoolbar.toolbar.InferenceToolBar;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Dialog box to set the inference options
 *
 * @author asaez
 * @version 1.0
 */
public class PropagationOptionsDialog extends JDialog {
	/**
	 *
	 */
	private static final long serialVersionUID = 1912979194800110113L;
	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	/**
	 * Button group that holds the radio buttons that will be shown. There is a
	 * radio button for each state of the node.
	 */
    final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * This method initialises this instance.
	 *
	 * @param owner       window that owns this dialog.
	 * @param networkEditorPanel the editor panel that called this dialog.
	 */
	public PropagationOptionsDialog(Window owner, NetworkEditorPanel networkEditorPanel, InferenceToolBar inferenceToolBar) {
		JPanel principalPanel = new JPanel();
		JPanel textPanel = new JPanel();
		JPanel radioButtonsPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton(stringDatabase.getString("OptionsInferenceDialog.jButtonOK"));
        JButton cancelButton = new JButton(stringDatabase.getString("OptionsInferenceDialog.jButtonCancel"));
        setTitle(stringDatabase.getString("OptionsInferenceDialog.Title"));
		this.getContentPane().setLayout(new BorderLayout());
		setLocationRelativeTo(owner);
		this.getContentPane().add(principalPanel, BorderLayout.CENTER);
		principalPanel.setLayout(new BorderLayout());
		textPanel.setLayout(new GridLayout(2, 1));
		principalPanel.add(textPanel, BorderLayout.NORTH);
		radioButtonsPanel.setLayout(new GridLayout(2, 1));
		JRadioButton jRadioButton1 = new JRadioButton(
                stringDatabase.getString("OptionsInferenceDialog.optionAuto"));
        jRadioButton1.setActionCommand(stringDatabase.getString("OptionsInferenceDialog.optionAuto"));
		JRadioButton jRadioButton2 = new JRadioButton(
                stringDatabase.getString("OptionsInferenceDialog.optionManual"));
        jRadioButton2.setActionCommand(stringDatabase.getString("OptionsInferenceDialog.optionManual"));
		radioButtonsPanel.add(jRadioButton1);
		radioButtonsPanel.add(jRadioButton2);
		if (networkEditorPanel.isAutomaticPropagation()) {
			jRadioButton1.setSelected(true);
		} else {
			jRadioButton2.setSelected(true);
		}

		buttonGroup.add(jRadioButton1);
		buttonGroup.add(jRadioButton2);
		principalPanel.add(radioButtonsPanel, BorderLayout.CENTER);
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		principalPanel.add(buttonsPanel, BorderLayout.SOUTH);
		PropagationOptionsDialogListener optionsInferenceDialogListener = new PropagationOptionsDialogListener(this,
																											   networkEditorPanel, inferenceToolBar);
		okButton.addActionListener(optionsInferenceDialogListener);
		cancelButton.addActionListener(optionsInferenceDialogListener);
        this.setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE));
		pack();
		setMinimumSize(new Dimension(300, getHeight()));
		setModal(true);
	}

	/**
	 * This method returns the button group contained by this dialog.
	 *
	 * @return the button group contained by this dialog.
	 */
	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}
}
