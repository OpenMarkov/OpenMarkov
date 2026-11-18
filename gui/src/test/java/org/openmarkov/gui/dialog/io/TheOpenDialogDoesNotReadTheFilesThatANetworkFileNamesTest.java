/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXParseException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The «Open» dialog reads the version of the chosen file before the network is opened.
 *
 * @author Manuel Arias
 */
class TheOpenDialogDoesNotReadTheFilesThatANetworkFileNamesTest {

    @Test void itsVersionIsNotRead(@TempDir Path directory) throws Exception {
        Path localFile = Files.writeString(directory.resolve("local.dtd"),
                "<!ATTLIST ProbModelXML formatVersion CDATA \"1.0.0\">");
        Path network = Files.writeString(directory.resolve("doctype.pgmx"),
                "<!DOCTYPE ProbModelXML SYSTEM \"" + localFile.toUri() + "\">\n<ProbModelXML/>");
        NetworkOMFileChooser chooser = new NetworkOMFileChooser();
        chooser.setSelectedFile(network.toFile());

        assertThrows(SAXParseException.class, chooser::getPgmxFileFormat);
    }
}
