/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.menutoolbar.toolbar.InferenceToolBar;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listener associated to OptionsInferenceDialog.
 *
 * @author asaez
 * @version 1.0
 */
public class PropagationOptionsDialogListener implements ActionListener {
	/**
	 * The Dialog to which this listener is associated
	 */
    final PropagationOptionsDialog automaticPropagationOptionsDialog;
	/**
	 * The editor panel that called the associated dialog.
	 */
    final NetworkEditorPanel networkEditorPanel;
	/**
	 * The inference tool bar associated to the panel.
	 */
    final InferenceToolBar inferenceToolBar;

	/**
	 * constructor
	 */
	public PropagationOptionsDialogListener(PropagationOptionsDialog optionsInferenceDialog, NetworkEditorPanel networkEditorPanel,
			InferenceToolBar inferenceToolBar) {
		this.automaticPropagationOptionsDialog = optionsInferenceDialog;
		this.networkEditorPanel = networkEditorPanel;
		this.inferenceToolBar = inferenceToolBar;
	}

	/**
	 * Invoked when an action occurs.
	 *
	 * @param actionEvent event information.
	 */
	@Override public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		String inferenceType = automaticPropagationOptionsDialog.getButtonGroup().getSelection().getActionCommand();
		StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        if (command.equals(stringDatabase.getString("OptionsInferenceDialog.jButtonOK"))) {
            if (inferenceType.equals(stringDatabase.getString("OptionsInferenceDialog.optionAuto"))) {
				networkEditorPanel.setAutomaticPropagation(true);
				networkEditorPanel.setPropagationActive(true);
                if (networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
					for (int caseIndex = 0; caseIndex < networkEditorPanel.getEvidenceManager().getNumberOfCases(); caseIndex++) {
                        if (!networkEditorPanel.getEvidenceManager().getEvidenceCasesCompilationState(caseIndex)) {
                            try {
                                networkEditorPanel.getEvidenceManager().doPropagation(networkEditorPanel.getEvidenceManager().getEvidenceCase(caseIndex), caseIndex);
                            } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
                                     NotEnoughMemoryException | IncompatibleEvidenceException |
                                     ConstraintViolatedException | CannotNormalizePotentialException e) {
                                throw new UnrecoverableException(e);
                            }
                            networkEditorPanel.updateAllVisualStates("", caseIndex);
						}
					}
					networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
					inferenceToolBar.setCurrentEvidenceCaseName(networkEditorPanel.getEvidenceManager().getCurrentCase());
					networkEditorPanel.getEvidenceManager().updateNodesFindingState(networkEditorPanel.getEvidenceManager().getCurrentEvidenceCase());
				}
            } else if (inferenceType.equals(stringDatabase.getString("OptionsInferenceDialog.optionManual"))) {
				networkEditorPanel.setAutomaticPropagation(false);
                if (networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
					inferenceToolBar.setCurrentEvidenceCaseName(networkEditorPanel.getEvidenceManager().getCurrentCase());
				}
			}
        } else if (command.equals(stringDatabase.getString("OptionsInferenceDialog.jButtonCancel"))) {
			// do nothing
		}
		automaticPropagationOptionsDialog.setVisible(false);
	}
}
