/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.*;

/**
 * Main {@code JFrame} class for the project
 * This class calls the dialog with Uncertainty Options
 *
 * @author jperez-martin
 */
public class SensitivityAnalysisFrameGenerator {
    
    /**
     * Constructor. initialises the instance.
     *
     * @param owner window that owns the dialog.
     * @return a new dialog, or {@code null} if the user cancelled the inference options
     */
    public static @Nullable SensitivityAnalysisDialog create(JFrame owner) {
        SensitivityAnalysisController controller = new SensitivityAnalysisController(owner);
        InferenceOptionsDialog inferenceOptionsDialog = new InferenceOptionsDialog(controller.getProbNet(),
                                                                                   GUIUtils.getOwner(MainGUI.INSTANCE.mainPanel), null);
        if (inferenceOptionsDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
            controller.getConfiguration().setIsUnicriterion(
                    inferenceOptionsDialog.getMulticriteriaOptions()
                                          .getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION
                            || controller.getProbNet()
                                         .getDecisionCriteria() != null
                            && controller.getProbNet().getDecisionCriteria().size() == 1);
            return new SensitivityAnalysisDialog(owner, controller);
        }
        return null;
    }
}
