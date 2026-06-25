/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import org.openmarkov.gui.dialog.io.OMFileChooser;

import javax.swing.JOptionPane;
import java.io.File;

/**
 * {@link OMFileChooser} that prompts the user to confirm overwrite when an
 * existing file is selected in a save dialog. Yes proceeds, No keeps the
 * dialog open, Cancel dismisses it.
 *
 * <p>Extracted from {@code ResultsDialog} so other dialogs in the module can
 * reuse the same UX.</p>
 */
public final class OverwriteAwareFileChooser extends OMFileChooser {

    @Override
    public void approveSelection() {
        File file = getSelectedFile();
        if (file != null && file.exists() && getDialogType() == SAVE_DIALOG) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "That file already exists, overwrite?",
                    "Existing file",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            switch (answer) {
                case JOptionPane.YES_OPTION -> super.approveSelection();
                case JOptionPane.NO_OPTION, JOptionPane.CLOSED_OPTION -> { /* keep dialog open */ }
                case JOptionPane.CANCEL_OPTION -> cancelSelection();
            }
            return;
        }
        super.approveSelection();
    }
}
