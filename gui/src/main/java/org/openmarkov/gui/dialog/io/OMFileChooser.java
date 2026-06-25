/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.OperatingSystem;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;

/**
 * This class implements a file chooser dialog file to select OpenMarkov files.
 *
 * @author jmendoza
 * @author m.arias
 * @version 1.1 jlgozalo - set appropriate variables names and redo For loop to
 * use enhanced loop syntax
 */
public class OMFileChooser extends JFileChooser {
    
    /**
     * Static field representing the default file format
     */
    public static final String DEFAULT_FILE_FORMAT = "OpenMarkov.1.0";
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 9076351651764305920L;
    
    /**
     * Directory where the dialog box searchs the files.
     */
    private static final String directoryPath = System.getProperty("user.home");
    
    /**
     * Creates a new file chooser that starts in the current directory,
     * filtering the files with the file filters.
     */
    public OMFileChooser() {
        super();
        setName("OMFileChooser");
        setTextsInLocale();
        setCurrentDirectory(new File(directoryPath));
        rescanCurrentDirectory();
    }
    
    @Override public @Nullable File getSelectedFile() {
        File selectedFile = super.getSelectedFile();
        if (selectedFile == null) return null;
        
        return OMFileChooser.applyFilter(OMFileChooser.correctQuotes(selectedFile), getFileFilter());
    }
    
    public @Nullable File getRawSelectedFile() {
        return super.getSelectedFile();
    }
    
    private static @Nullable File applyFilter(File file, FileFilter fileFilter) {
        if (fileFilter instanceof FileFilterByExtension<?> fileFilterByExtension) {
            var lowercasedName = file.getName().toLowerCase();
            boolean properIsExtensionOnFile = fileFilterByExtension.getExtensions().stream()
                                                                   .anyMatch(extension -> lowercasedName.endsWith("." + extension));
            if (!properIsExtensionOnFile) {
                file = new File(file.getParentFile(), file.getName() + "." + fileFilterByExtension.getExtensions()
                                                                                                  .getFirst());
            }
        }
        return file;
    }
    
    private static File correctQuotes(File selectedFile) {
        if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
            var absPath = selectedFile.getAbsolutePath();
            if (absPath.indexOf('"') != absPath.lastIndexOf('"')) {
                return new File(absPath.substring(absPath.indexOf('"') + 1, absPath.lastIndexOf('"')));
            }
        }
        return selectedFile;
    }
    
    @Override public File[] getSelectedFiles() {
        File[] selectedFiles = super.getSelectedFiles();
        if (selectedFiles == null) return null;
        return Arrays.stream(selectedFiles).map(OMFileChooser::correctQuotes).toArray(File[]::new);
    }
    
    /**
     * to fix the bug in JFileChooser to display foreground in different languages the
     * foreground of the components must be set explicitly
     */
    private static void setTextsInLocale() {
        
        StringDatabase stringDb = StringDatabase.getUniqueInstance();
        
        UIManager.put("OMFileChooser.cancelButtonText", stringDb.getString("OMFileChooser.cancelButtonText"));
        UIManager.put("OMFileChooser.cancelButtonToolTipText", stringDb.getString("OMFileChooser.cancelButtonToolTipText"));
        UIManager.put("OMFileChooser.detailsViewActionLabelText",
                      stringDb.getString("OMFileChooser.detailsViewActionLabelText"));
        UIManager.put("OMFileChooser.detailsViewButtonToolTipText",
                      stringDb.getString("OMFileChooser.detailsViewButtonToolTipText"));
        UIManager.put("OMFileChooser.fileNameLabelText", stringDb.getString("OMFileChooser.fileNameLabelText"));
        UIManager.put("OMFileChooser.filesOfTypeLabelText", stringDb.getString("OMFileChooser.filesOfTypeLabelText"));
        UIManager.put("OMFileChooser.helpButtonText", stringDb.getString("OMFileChooser.helpButtonText"));
        UIManager.put("OMFileChooser.helpButtonToolTipText", stringDb.getString("OMFileChooser.helpButtonToolTipText"));
        UIManager.put("OMFileChooser.homeFolderToolTipText", stringDb.getString("OMFileChooser.homeFolderToolTipText"));
        UIManager.put("OMFileChooser.listViewActionLabelText", stringDb.getString("OMFileChooser.listViewActionLabelText"));
        UIManager.put("OMFileChooser.listViewButtonToolTipTextlist",
                      stringDb.getString("OMFileChooser.newFolderToolTipText"));
        UIManager.put("OMFileChooser.lookInLabelText", stringDb.getString("OMFileChooser.lookInLabelText"));
        UIManager.put("OMFileChooser.newFolderActionLabelText",
                      stringDb.getString("OMFileChooser.newFolderActionLabelText"));
        UIManager.put("OMFileChooser.newFolderToolTipText", stringDb.getString("OMFileChooser.newFolderToolTipText"));
        UIManager.put("OMFileChooser.openButtonTextOpen", stringDb.getString("OMFileChooser.openButtonTextOpen"));
        UIManager.put("OMFileChooser.openButtonToolTipText", stringDb.getString("OMFileChooser.openButtonToolTipText"));
        UIManager.put("OMFileChooser.refreshActionLabelText", stringDb.getString("OMFileChooser.refreshActionLabelText"));
        UIManager.put("OMFileChooser.saveButtonTextSave", stringDb.getString("OMFileChooser.saveButtonTextSave"));
        UIManager.put("OMFileChooser.saveButtonToolTipText", stringDb.getString("OMFileChooser.saveButtonToolTipText"));
        UIManager.put("OMFileChooser.upFolderToolTipText", stringDb.getString("OMFileChooser.upFolderToolTipText"));
        UIManager.put("OMFileChooser.updateButtonText", stringDb.getString("OMFileChooser.updateButtonText"));
        UIManager.put("OMFileChooser.updateButtonToolTipText", stringDb.getString("OMFileChooser.updateButtonToolTipText"));
        UIManager.put("OMFileChooser.viewMenuLabelText", stringDb.getString("OMFileChooser.viewMenuLabelText"));
        
    }
    
    /**
     * Sets the file given by description
     *
     * @param description - description of the filter: "Elvira" or "OpenMarkov_version"
     */
    public void setFileFilter(String description) {
        boolean isSet = false;
        for (FileFilter filter : getChoosableFileFilters()) {
            if (filter instanceof FileFilterByExtension && ((FileFilterByExtension) filter).getFileDescription()
                                                                                           .equals(description)) {
                setFileFilter(filter);
                isSet = true;
                break;
            }
        }
        // In case there is an outdated value in the register
        if (!isSet) {
            LocalPreferences.LATEST_NETWORK_FORMAT.set(OMFileChooser.DEFAULT_FILE_FORMAT);
            description = OMFileChooser.DEFAULT_FILE_FORMAT;
            for (FileFilter filter : getChoosableFileFilters()) {
                if (filter instanceof FileFilterByExtension && ((FileFilterByExtension) filter).getFileDescription()
                                                                                               .equals(description)) {
                    setFileFilter(filter);
                    break;
                }
            }
        }
        rescanCurrentDirectory();
    }
    
    
}
