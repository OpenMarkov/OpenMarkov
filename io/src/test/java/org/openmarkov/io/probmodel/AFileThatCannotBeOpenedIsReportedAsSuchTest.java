/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * A network file that is not there, or cannot be read, is reported as a file that cannot be opened, with the reason,
 * and not as a badly structured file (error 392).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFileThatCannotBeOpenedIsReportedAsSuchTest {

    @TempDir Path directory;

    private static void open(URL url) throws Exception {
        FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    private static void assertCannotBeOpened(Path file) throws Exception {
        URL url = file.toUri().toURL();

        ProbNetParserException.CannotOpenFile exception =
                assertThrows(ProbNetParserException.CannotOpenFile.class, () -> open(url));
        String message = exception.getExceptionMessage();
        assertAll(() -> assertTrue(message.startsWith("Cannot open file " + url.getFile() + "."), message),
                  () -> assertTrue(message.contains(file.toString()), message),
                  () -> assertFalse(message.contains(">>>"), message));
    }

    @Test void aFileThatIsNotThere() throws Exception {
        assertCannotBeOpened(directory.resolve("deleted.pgmx"));
    }

    @Test void aFileInAFolderThatIsNotThere() throws Exception {
        assertCannotBeOpened(directory.resolve("deleted").resolve("network.pgmx"));
    }

    @Test void aFileThatCannotBeRead() throws Exception {
        Path file = Files.writeString(directory.resolve("unreadable.pgmx"), "<ProbModelXML formatVersion=\"0.2.0\"/>");
        file.toFile().setReadable(false, false);
        assumeFalse(Files.isReadable(file), "The file system or the user cannot take away the permission to read");

        assertCannotBeOpened(file);
    }

    @Test void aBadlyStructuredFileStillSaysWhere() throws Exception {
        Path file = Files.writeString(directory.resolve("badly-structured.pgmx"),
                "<ProbModelXML formatVersion=\"0.2.0\">\n<Variables>\n</ProbModelXML>");
        URL url = file.toUri().toURL();

        ProbNetParserException.BadlyStructuredFile exception =
                assertThrows(ProbNetParserException.BadlyStructuredFile.class, () -> open(url));
        String message = exception.getExceptionMessage();
        assertAll(() -> assertTrue(message.contains("An error occurred at line 3, column "), message),
                  () -> assertFalse(message.contains(">>>"), message),
                  () -> assertFalse(message.endsWith(".."), message));
    }
}
