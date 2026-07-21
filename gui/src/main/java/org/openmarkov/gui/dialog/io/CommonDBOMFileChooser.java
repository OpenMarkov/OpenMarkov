/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.gui.configuration.LocalPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.File;

public abstract class CommonDBOMFileChooser extends OMFileChooser {
    
    protected static CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
    
    public CommonDBOMFileChooser(boolean acceptAllFiles) {
        super();
        setAcceptAllFileFilterUsed(acceptAllFiles);
        setCurrentDirectory(LocalPreferences.LATEST_OPEN_DATASET_DIRECTORY.get());
        rescanCurrentDirectory();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(this::focusDirectoryList);
            }
        });
    }

    private void focusDirectoryList() {
        JList<?> directoryList = findFirstJList(this);
        if (directoryList != null) {
            directoryList.requestFocusInWindow();
        }
    }

    private static JList<?> findFirstJList(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JList<?> list) {
                return list;
            } else if (comp instanceof Container subContainer) {
                JList<?> found = findFirstJList(subContainer);
                if (found != null) return found;
            }
        }
        return null;
    }
    
    @Override
    public int showOpenDialog(Component parent) {
        setCurrentDirectory(LocalPreferences.LATEST_OPEN_DATASET_DIRECTORY.get());
        autoPickFilter();
        int result = super.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            LocalPreferences.LATEST_OPEN_DATASET_DIRECTORY.set(getSelectedFile().getAbsoluteFile());
            LocalPreferences.LATEST_SAVED_DATASET_EXTENSION.set(FilenameUtils.getExtension(getSelectedFile().getAbsoluteFile()
                                                                                                            .getName()));
        }
        return result;
    }
    
    @Override
    public int showSaveDialog(Component parent) {
        setCurrentDirectory(LocalPreferences.LATEST_OPEN_DATASET_DIRECTORY.get());
        autoPickFilter();
        int result = super.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            LocalPreferences.LATEST_SAVED_DATASET_EXTENSION.set(FilenameUtils.getExtension(getSelectedFile().getAbsoluteFile()
                                                                                                            .getName()));
            LocalPreferences.LATEST_SAVED_DATASET_DIRECTORY.set(getSelectedFile().getParentFile());
        }
        return result;
    }
    
    private void autoPickFilter() {
        File selectedFile = getRawSelectedFile();
        if (selectedFile != null) {
            var extension = FilenameUtils.getExtension(selectedFile.getName());
            for (var filter : getChoosableFileFilters()) {
                if (filter instanceof FileFilterByExtension<?> fileFilterByExtension) {
                    for (var filterExtension : fileFilterByExtension.getExtensions()) {
                        if (filterExtension.equals(extension)) {
                            setFileFilter(filter);
                            return;
                        }
                    }
                }
            }
        }
        for (var filter : getChoosableFileFilters()) {
            if (filter instanceof FileFilterByExtension<?> fileFilterByExtension) {
                for (var filterExtension : fileFilterByExtension.getExtensions()) {
                    if (filterExtension.equals(LocalPreferences.LATEST_SAVED_DATASET_EXTENSION.get())) {
                        setFileFilter(filter);
                        return;
                    }
                }
            }
        }
    }
    
}
