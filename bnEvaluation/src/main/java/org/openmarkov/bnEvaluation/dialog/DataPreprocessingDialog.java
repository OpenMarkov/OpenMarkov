/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.dialog;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.bnEvaluation.DataPreprocessor;
import org.openmarkov.bnEvaluation.component.DBOpenerPanel;
import org.openmarkov.bnEvaluation.component.PreprocessingOptionsPanel;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoReaderForExtension;
import org.openmarkov.gui.dialog.common.BottomPanelButtonDialog;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.gui.dialog.io.DBWriterOMFileChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Dialog that drives dataset preprocessing: it opens a case database, collects the options in a
 * {@link PreprocessingOptionsPanel} and saves the preprocessed dataset. The preprocessing itself is
 * performed by the pure {@link DataPreprocessor} service, so this class is a thin view.
 *
 * @author evillar
 * @author Manuel Arias
 */
public final class DataPreprocessingDialog extends BottomPanelButtonDialog {

    private final DBOpenerPanel dbOpenerPanel;
    private final PreprocessingOptionsPanel optionsPanel;
    private final JButton resetButton;
    private final JButton savePreprocessSetButton;
    private final DBWriterOMFileChooser saveWritter;

    public DataPreprocessingDialog(Frame owner) {
        super(owner);
        this.setTitle("Data Preprocessing");
        this.setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        saveWritter = new DBWriterOMFileChooser(false);
        saveWritter.setDialogTitle("Save preprocessed dataset in...");

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(750, 500));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        this.dbOpenerPanel = new DBOpenerPanel(this);
        mainPanel.add(this.dbOpenerPanel);

        this.optionsPanel = new PreprocessingOptionsPanel();

        resetButton = new JButton("Reset values");
        resetButton.setEnabled(false);
        resetButton.addActionListener(e -> optionsPanel.reset());

        savePreprocessSetButton = new JButton("Save dataset");
        savePreprocessSetButton.setEnabled(false);
        savePreprocessSetButton.addActionListener(e -> {
            try {
                savePreprocessSetButtonActionPerformed();
            } catch (IOException | NoReaderForExtension ex) {
                // A write failure or an unsupported extension are expectable user errors; report them
                // and keep the dialog open instead of aborting the application.
                JOptionPane.showMessageDialog(this,
                        "The dataset could not be saved: " + ex.getMessage(),
                        "Save error", JOptionPane.ERROR_MESSAGE);
            }
        });

        this.dbOpenerPanel.onOpen((databaseFile, database) -> {
            optionsPanel.updateForDatabase(database);
            resetButton.setEnabled(true);
            savePreprocessSetButton.setEnabled(true);
        });

        mainPanel.add(optionsPanel);
        this.add(mainPanel);

        JButton cancelButton = DialogBase.generateGenericCancelButton();
        cancelButton.setText("Close");
        addButtonToButtonsPanel(savePreprocessSetButton);
        addButtonToButtonsPanel(resetButton);
        setCancelButton(cancelButton);

        this.pack();
        setSize(new Dimension(880, 500));
    }

    /**
     * Saves the preprocessed dataset next to the source database file. Listener of the "Save dataset"
     * button.
     */
    private void savePreprocessSetButtonActionPerformed() throws IOException, NoReaderForExtension {
        if (!validateBeforeSave()) return;
        CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
        String extension = FilenameUtils.getExtension(this.dbOpenerPanel.getDatabaseFile().getName());
        String name = FilenameUtils.getBaseName(this.dbOpenerPanel.getDatabaseFile().getName());

        this.saveWritter.setSelectedFile(new File(this.dbOpenerPanel.getDatabaseFile()
                                                                    .getParentFile(), name + " - Preprocessed." + extension));
        if (this.saveWritter.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        var preprocessedDBFile = this.saveWritter.getSelectedFile();
        caseDbManager.getWriter(FilenameUtils.getExtension(preprocessedDBFile.getName()))
                     .save(preprocessedDBFile, DataPreprocessor.process(optionsPanel.buildRequest()));

        JOptionPane.showMessageDialog(this,
                                      "The preprocessed dataset has been saved.",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean validateBeforeSave() {
        DataPreprocessor.ValidationError error = DataPreprocessor.validate(optionsPanel.buildRequest());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error.message(), error.title(), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
