/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.sensitivityanalysis.model.ParameterType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisConfiguration;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Panel for uncertain parameters selection
 *
 * @author jperez-martin
 */
public class ParametersPanel extends JPanel {

	/**
	 * Selector for multiple uncertain parameters
	 */
	private List<JCheckBox> uncertainParametersXCheckBoxes;

	/**
	 * Selector for one parameter in X axis
	 */
	private List<JRadioButton> uncertainParametersXRadioButtons;

	/**
	 * Selector for one parameter in Y axis
	 */
	private List<JRadioButton> uncertainParametersYRadioButtons;

	/**
	 * Ucertain parameters
	 */
	private HashMap<String, UncertainParameter> uncertainParameters;

	/**
	 * Group of flags for GUI
	 */
	private SensitivityAnalysisConfiguration config;

	/**
	 * Controller of sensitivity analysis
	 */
	private SensitivityAnalysisController controller;

	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	/**
	 * Panel with the probNet uncertain parameters
	 *
	 * @param controller Controller of the sensitivity analysis
	 */
	public ParametersPanel(SensitivityAnalysisController controller) {
		super();
		this.uncertainParameters = controller.getUncertainParameters();
		this.config = controller.getConfiguration();
		this.controller = controller;
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		JPanel firstAxisParametersPanel = new JPanel();
		firstAxisParametersPanel.setLayout(new BoxLayout(firstAxisParametersPanel, BoxLayout.PAGE_AXIS));
		firstAxisParametersPanel
				.setBorder(new TitledBorder(stringDatabase.getString("SensitivityAnalysis.General.Parameters")));

		// If the uncertainty analysis type allows multiparameter selection
        if (config.getParameterType() == ParameterType.MULTI_PARAMETER) {
			uncertainParametersXCheckBoxes = new ArrayList<>();
			JCheckBox allParametersSelected = new JCheckBox(
					stringDatabase.getString("SensitivityAnalysis.General.All"));
			allParametersSelected.setSelected(true);
			uncertainParametersXCheckBoxes.add(allParametersSelected);
			firstAxisParametersPanel.add(allParametersSelected);
			firstAxisParametersPanel.add(new JSeparator());
			allParametersSelected.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					checkBoxSelected((JCheckBox) e.getSource());
				}
			});

			for (String uncertainParameterName : controller.getOrderedUncertainParametersKeys()) {
				JCheckBox uncertainParameter = new JCheckBox(uncertainParameterName);
				double baseValue = uncertainParameters.get(uncertainParameterName).getBaseLineValue();
				uncertainParameter.setToolTipText(String.valueOf(baseValue));
				uncertainParameter.addActionListener(new ActionListener() {
					@Override public void actionPerformed(ActionEvent e) {
						checkBoxSelected((JCheckBox) e.getSource());
					}
				});
				uncertainParametersXCheckBoxes.add(uncertainParameter);
				uncertainParameter.setSelected(true);
				firstAxisParametersPanel.add(uncertainParameter);
			}
			this.add(firstAxisParametersPanel);
        } else if (config.getParameterType() == ParameterType.ONE_PARAMETER) {
			// If the uncertainty analysis type not allows multiparameter selection
			uncertainParametersXRadioButtons = new ArrayList<>();
			ButtonGroup radioButtonGroupXAxis = new ButtonGroup();

			// Set X axis radio buttons
			for (String uncertainParameterName : controller.getOrderedUncertainParametersKeys()) {
				JRadioButton uncertainParameter = new JRadioButton(uncertainParameterName);
				double baseValue = uncertainParameters.get(uncertainParameterName).getBaseLineValue();
				uncertainParameter.setToolTipText(String.valueOf(baseValue));
				uncertainParameter.addActionListener(new ActionListener() {
					@Override public void actionPerformed(ActionEvent e) {
						checkRadioButtonSelected(config.isBiaxial(), (JRadioButton) e.getSource(),
								uncertainParametersYRadioButtons);
					}
				});
				radioButtonGroupXAxis.add(uncertainParameter);
				uncertainParametersXRadioButtons.add(uncertainParameter);
				firstAxisParametersPanel.add(uncertainParameter);
			}

			if (uncertainParametersXRadioButtons != null && !uncertainParametersXRadioButtons.isEmpty()) {
				uncertainParametersXRadioButtons.get(0).setSelected(true);
			}

			this.add(firstAxisParametersPanel);

			// Set Y axis radio buttons
			if (config.isBiaxial()) {
				ButtonGroup radioButtonGroupYAxis = new ButtonGroup();
				uncertainParametersYRadioButtons = new ArrayList<>();
				JPanel secondAxisParameters = new JPanel();
				secondAxisParameters.setLayout(new BoxLayout(secondAxisParameters, BoxLayout.PAGE_AXIS));
				secondAxisParameters.setBorder(
						new TitledBorder(stringDatabase.getString("SensitivityAnalysis.General.Parameters")));

				// Set Y axis radio buttons
				for (String uncertainParameterName : controller.getOrderedUncertainParametersKeys()) {
					JRadioButton uncertainParameter = new JRadioButton(uncertainParameterName);
					double baseValue = uncertainParameters.get(uncertainParameterName).getBaseLineValue();
					uncertainParameter.setToolTipText(String.valueOf(baseValue));
					uncertainParameter.addActionListener(new ActionListener() {
						@Override public void actionPerformed(ActionEvent e) {
							checkRadioButtonSelected(config.isBiaxial(), (JRadioButton) e.getSource(),
									uncertainParametersXRadioButtons);
						}
					});
					radioButtonGroupYAxis.add(uncertainParameter);
					uncertainParametersYRadioButtons.add(uncertainParameter);
					secondAxisParameters.add(uncertainParameter);
				}
				uncertainParametersXRadioButtons.get(0).setSelected(true);
				uncertainParametersXRadioButtons.get(1).setEnabled(false);

				uncertainParametersYRadioButtons.get(1).setSelected(true);
				uncertainParametersYRadioButtons.get(0).setEnabled(false);

				this.add(secondAxisParameters);
			}
		}
		updateParametersSelected();
	}

	/**
	 * Checks if a radiobutton is selected in a specified axis and disallows it if the radiobutton is selected
	 *
	 * @param isBiaxial     true if the analysis is biaxial
	 * @param radioButton   ratido button to check
	 * @param axisToCompare list in wich we check if the radio button is selected
	 */
	private void checkRadioButtonSelected(boolean isBiaxial, JRadioButton radioButton,
			List<JRadioButton> axisToCompare) {
		if (isBiaxial) {
			for (JRadioButton jRadioButton : axisToCompare) {
                jRadioButton.setEnabled(!radioButton.getText().equals(jRadioButton.getText()));
			}
		}
		updateParametersSelected();
	}

	/**
	 * Mark the parameter/s selected with the checkbox and controls ALL checkbox functionality
	 *
	 * @param jcheckBox Check box selected
	 */
	private void checkBoxSelected(JCheckBox jcheckBox) {
		if (jcheckBox.getText().equals(stringDatabase.getString("SensitivityAnalysis.General.All"))) {
			// If the "ALL" checkbox is selected, select all parameters and add them to the selected parameters list
			if (jcheckBox.isSelected()) {
				for (JCheckBox uncertainParameterCheckBox : uncertainParametersXCheckBoxes) {
					uncertainParameterCheckBox.setSelected(true);
				}
				// Else clear all parameters and remove them from the parameters list
			} else {
				for (JCheckBox uncertainParameterCheckBox : uncertainParametersXCheckBoxes) {
					uncertainParameterCheckBox.setSelected(false);
				}
			}
		} else {
            checkAllCheckBox();
        }
		updateParametersSelected();
	}

	/**
	 * Check if all parameters has been selected
	 */
	private void checkAllCheckBox() {
		boolean allChecked = true;
		for (JCheckBox uncertainParameterCheckBox : uncertainParametersXCheckBoxes) {
			if (!uncertainParameterCheckBox.getText()
					.equals(stringDatabase.getString("SensitivityAnalysis.General.All"))) {
				if (!uncertainParameterCheckBox.isSelected()) {
					allChecked = false;
				}
			}
		}

		if (allChecked) {
			for (JCheckBox uncertainParameterCheckBox : uncertainParametersXCheckBoxes) {
				if (uncertainParameterCheckBox.getText()
						.equals(stringDatabase.getString("SensitivityAnalysis.General.All"))) {
					uncertainParameterCheckBox.setSelected(true);
				}
			}
		} else {
			uncertainParametersXCheckBoxes.get(0).setSelected(false);
		}
	}

	/**
	 * Updates the selected parameters in both axis
	 */
	private void updateParametersSelected() {
		controller.getSensitivityAnalysisModel()
				.setSelectedUncertainParametersXAxis(getSelectedUncertainParametersXaxis());
		controller.getSensitivityAnalysisModel()
				.setSelectedUncertainParametersYAxis(getSelectedUncertainParametersYaxis());
	}

	/**
	 * Gets selected uncertain parameters of X axis
	 *
	 * @return
	 */
	public List<UncertainParameter> getSelectedUncertainParametersXaxis() {
		List<UncertainParameter> selectedUncertainParametersXaxis = new ArrayList<>();
        if (config.getParameterType() == ParameterType.MULTI_PARAMETER) {
			for (JCheckBox uncertainParameter : uncertainParametersXCheckBoxes) {
				if (uncertainParameter.isSelected() && !uncertainParameter.getText()
						.equals(stringDatabase.getString("SensitivityAnalysis.General.All"))) {
					selectedUncertainParametersXaxis.add(uncertainParameters.get(uncertainParameter.getText()));
				}
			}
        } else if (config.getParameterType() == ParameterType.ONE_PARAMETER) {
			for (JRadioButton uncertainParameter : uncertainParametersXRadioButtons) {
				if (uncertainParameter.isSelected()) {
					selectedUncertainParametersXaxis.add(uncertainParameters.get(uncertainParameter.getText()));
					break;
				}
			}
		}

		return selectedUncertainParametersXaxis;
	}

	/**
	 * Gets selected uncertain parameters of Y axis
	 *
	 * @return
	 */
	public List<UncertainParameter> getSelectedUncertainParametersYaxis() {
		List<UncertainParameter> selectedUncertainParametersYaxis = null;
        if (config.isBiaxial() && config.getParameterType() == ParameterType.ONE_PARAMETER) {
			selectedUncertainParametersYaxis = new ArrayList<>();
			for (JRadioButton uncertainParameter : uncertainParametersYRadioButtons) {
				if (uncertainParameter.isSelected()) {
					selectedUncertainParametersYaxis.add(uncertainParameters.get(uncertainParameter.getText()));
					break;
				}
			}
		}
		return selectedUncertainParametersYaxis;
	}
}
