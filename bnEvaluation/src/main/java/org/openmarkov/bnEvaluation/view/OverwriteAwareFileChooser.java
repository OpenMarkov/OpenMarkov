/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import org.openmarkov.gui.dialog.common.CommonOptions;
import org.openmarkov.gui.dialog.common.OptionDialog;
import org.openmarkov.gui.dialog.io.OMFileChooser;

import javax.swing.SwingUtilities;
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
            OptionDialog<CommonOptions.YesNoCancel> dialog = new OptionDialog<>(SwingUtilities.windowForComponent(this), "Existing file", "That file already exists, overwrite?", CommonOptions.YesNoCancel.class);
            var option = dialog.request(CommonOptions.YesNoCancel.CANCEL);
            switch (option) {
                case YES -> super.approveSelection();
                case NO -> {
                } /* keep dialog open */
                case CANCEL -> cancelSelection();
            }
            return;
        }
        super.approveSelection();
    }
}
