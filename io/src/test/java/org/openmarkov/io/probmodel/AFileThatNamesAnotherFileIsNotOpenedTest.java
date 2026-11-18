/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.xml.sax.SAXParseException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network file whose DOCTYPE names another file is refused by every reader of the open path, without reading it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class AFileThatNamesAnotherFileIsNotOpenedTest {

    private static final String LOCAL_TEXT = "TEXT OF A LOCAL FILE";

    @TempDir static Path directory;
    private static URL network;

    @BeforeAll static void writeTheFiles() throws Exception {
        Path localFile = Files.writeString(directory.resolve("local.txt"), LOCAL_TEXT);
        String original = new String(AFileThatNamesAnotherFileIsNotOpenedTest.class.getClassLoader()
                                                                            .getResourceAsStream("columns-not-adding-up-to-one.pgmx")
                                                                            .readAllBytes());
        String withEntity = original
                .replace("<ProbModelXML ", "<!DOCTYPE ProbModelXML [<!ENTITY local SYSTEM \"" + localFile.toUri() + "\">]>\n<ProbModelXML ")
                .replace("<Comment><![CDATA[<<Double click to add/modify comment>>]]></Comment>", "<Comment>&local;</Comment>");
        network = Files.writeString(directory.resolve("doctype.pgmx"), withEntity).toUri().toURL();
    }

    @Test public void openingItIsRefused() {
        assertThrows(ProbNetParserException.BadlyStructuredFile.class,
                     () -> FormatManager.getInstance().getProbNetReader(network).read(network));
    }

    @Test public void readingItIsRefused() {
        assertThrows(ProbNetParserException.XMLInvalid.class, () -> new PGMXReader().read(network));
    }

    @Test public void checkingItsVersionIsRefused() {
        assertThrows(SAXParseException.class, () -> FormatManager.getInstance().checkVersion(network));
    }

    @Test public void checkingItsSchemaDoesNotReadTheOtherFile() throws Exception {
        List<String> problems = FormatManager.getInstance().schemaProblems(network);

        assertEquals(1, problems.size());
        assertTrue(problems.getFirst().contains("local.txt"));
        assertFalse(problems.getFirst().contains(LOCAL_TEXT));
    }
}
