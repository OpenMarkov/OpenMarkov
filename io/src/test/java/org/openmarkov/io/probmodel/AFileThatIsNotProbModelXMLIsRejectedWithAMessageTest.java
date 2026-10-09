/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The open dialog offers every .xml file, and the files of other formats, such as XMLBIF, have that extension.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class AFileThatIsNotProbModelXMLIsRejectedWithAMessageTest {

    private static void open(URL url) throws Exception {
        FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    @Test public void anXMLBIFFileIsNotAProbModelXMLNetwork() {
        URL url = getClass().getClassLoader().getResource("netAB.xml");

        PGMXParserException.NotAProbModelXMLFile exception =
                assertThrows(PGMXParserException.NotAProbModelXMLFile.class, () -> open(url));
        assertTrue(exception.getExceptionMessage()
                            .endsWith("netAB.xml is not a ProbModelXML network: its root element is BIF."));
    }

    @Test public void aFileWithoutVersionHasAVersionThatIsNotSupported() throws Exception {
        Path file = Files.createTempFile("noVersion", ".xml");
        Files.writeString(file, "<ProbModelXML/>");

        assertThrows(ProbNetParserException.WrongVersion.class, () -> open(file.toUri().toURL()));
    }
}
