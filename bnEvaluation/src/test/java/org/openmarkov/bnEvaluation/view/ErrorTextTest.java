/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.view;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The error windows of the evaluation tools show the text of the exception that refuses a case database, which an
 * OpenMarkov exception does not keep in {@code getMessage()} (error 418).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ErrorTextTest {

    @Test void aDatabaseTheReaderRefusesSaysWhy(@TempDir Path folder) throws Exception {
        Path file = Files.writeString(folder.resolve("width.csv"), "A,B\nx\n");

        ParsingSourceException refused = assertThrows(ParsingSourceException.class,
                () -> new CaseDatabaseManager().getReader("csv").load(file.toFile()));

        assertEquals("Line 2 of width.csv has 1 values, but the first line names 2 columns. "
                     + "Each line must have one value for each column, separated by commas.", ErrorText.of(refused));
    }

    @Test void anEmptyDatabaseSaysSo() {
        assertEquals("Dataset from empty.csv is empty.", ErrorText.of(new EmptyDatabaseException("empty.csv")));
    }

    @Test void anExtensionWithoutWriterSaysSo() {
        NoWriterForExtensionException refused =
                assertThrows(NoWriterForExtensionException.class, () -> new CaseDatabaseManager().getWriter("abc"));

        assertEquals("There is no writer for file format abc", ErrorText.of(refused));
    }

    @Test void anyOtherExceptionGivesItsMessage() {
        assertEquals("The disk is full", ErrorText.of(new IOException("The disk is full")));
    }

    @Test void anExceptionWithoutMessageGivesItsName() {
        assertEquals("IOException", ErrorText.of(new IOException()));
    }
}
