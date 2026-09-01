/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.common.CommonOptions;
import org.openmarkov.gui.dialog.common.OptionDialog;
import org.openmarkov.io.elvira.ElviraParser;
import org.openmarkov.io.elvira.ElviraWriter;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements a file chooser dialog file to select OpenMarkov files.
 *
 * @author ibermejo
 */
@SuppressWarnings("serial")
public class NetworkOMFileChooser extends OMFileChooser {
    /**
     * Creates a new file chooser that starts in the current directory,
     * filtering the files with the file filters.
     *
     * @param isOpening Indicates if the file chooser is for opening a file
     *                  (isOpening=true) or for saving (isOpening=false)
     */
    public NetworkOMFileChooser(boolean acceptAllfile, boolean isOpening) {
        super();
        setName("NetworkOMFileChooser");
        setAcceptAllFileFilterUsed(acceptAllfile);
        rescanCurrentDirectory();
        var parsers = isOpening ? FormatManager.readersClasses() : FormatManager.writersClasses();
        parsers
                .sorted(Comparator.comparing(readerClass -> FormatManager.info(readerClass).description()))
                .forEach(readerClass -> {
                    String description = FormatManager.info(readerClass).description();
                    addChoosableFileFilter(new FileFilterByExtension<>(readerClass, List.of(FormatManager.info(readerClass)
                                                                                                         .extensions()), description));
                });
        
        File currentDirectory = null;
        if (isOpening) {
            currentDirectory = UserPreferences.LATEST_OPEN_DIRECTORY.get();
            for (var filter : getChoosableFileFilters()) {
                if (filter instanceof FileFilterByExtension<?> fileFilterByExtension) {
                    if (fileFilterByExtension.getFormatInfo() instanceof Class<?> formatClass && formatClass == UserPreferences.LATEST_SAVED_NETWORK_READER_CLASS.get()) {
                        this.setFileFilter(fileFilterByExtension);
                        break;
                    }
                    ;
                }
            }
        } else {
            for (var filter : getChoosableFileFilters()) {
                if (filter instanceof FileFilterByExtension<?> fileFilterByExtension) {
                    if (fileFilterByExtension.getFormatInfo() instanceof Class<?> formatClass && formatClass == UserPreferences.LATEST_SAVED_NETWORK_WRITER_CLASS.get()) {
                        this.setFileFilter(fileFilterByExtension);
                        break;
                    }
                }
            }
        }
        
        setCurrentDirectory(currentDirectory);
    }
    
    public NetworkOMFileChooser() {
        this(false, true);
    }
    
    @Override
    public int showOpenDialog(Component parent) {
        int result = super.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            UserPreferences.LATEST_OPEN_DIRECTORY.set(getSelectedFile());
            try {
                UserPreferences.LATEST_NETWORK_FORMAT.set(getPgmxFileFormat());
            } catch (SAXException | IOException e) {
                throw new UnrecoverableException(e);
            }
        }
        return result;
    }
    
    @Override
    public int showSaveDialog(Component parent) {
        int result = super.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            UserPreferences.LATEST_OPEN_DIRECTORY.set(getSelectedFile());
            if (getFileFilter() instanceof FileFilterByExtension fileFilterByExtension) {
                var writerClass = (Class<? extends ProbNetWriter>) fileFilterByExtension.getFormatInfo();
                Class<? extends ProbNetReader> readerClass = null;
                if (writerClass == ElviraWriter.class) {
                    readerClass = ElviraParser.class;
                } else if (writerClass == PGMXWriter_0_2.class || writerClass == PGMXWriter_1_0.class) {
                    readerClass = PGMXReader.class;
                }
                if (readerClass != null) {
                    UserPreferences.LATEST_SAVED_NETWORK_READER_CLASS.set(readerClass);
                    UserPreferences.LATEST_SAVED_NETWORK_WRITER_CLASS.set(writerClass);
                }
            }
        }
        return result;
    }
    
    @Override
    public void approveSelection() {
        if (getDialogType() == SAVE_DIALOG) {
            File selectedFile = getSelectedFile();
            if ((selectedFile != null) && selectedFile.exists()) {
                OptionDialog<CommonOptions.YesNo> dialog = new OptionDialog<>(SwingUtilities.windowForComponent(this),
                                                                              "Ovewrite file",
                                                                              "The file " + selectedFile.getName() + " already exists. Do you want to replace the existing file?", CommonOptions.YesNo.class);
                var option = dialog.request(CommonOptions.YesNo.NO);
                if (option != CommonOptions.YesNo.YES) {
                    return;
                }
            }
        }
        super.approveSelection();
    }
    
    /**
     * Extracts the version of a pgmx file and concatenate it to the String
     * "OpenMarkov" for having the description of the file
     *
     * @return the format OpenMarkov.version of a pgmx file
     *
     * @throws IOException  if an I/O error occurs
     * @throws SAXException if an XML parsing error occurs
     */
    
    public String getPgmxFileFormat() throws SAXException, IOException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getSelectedFile());
            String version = doc.getDocumentElement().getAttribute("formatVersion");
            // Removing the last digit of the version
            version = version.substring(0, version.lastIndexOf('.'));
            return "OpenMarkov." + version;
        } catch (ParserConfigurationException e) {
            throw new UnreachableException(e);
        }
    }
}
