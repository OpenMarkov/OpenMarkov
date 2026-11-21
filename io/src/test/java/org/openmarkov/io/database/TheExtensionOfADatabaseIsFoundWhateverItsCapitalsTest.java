/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoReaderForExtension;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.testTags.TestSpeed;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The reader and the writer of a case database are found whatever the capitals of the extension of the file, and
 * asking for one that does not exist says which of the two is missing (error 417).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheExtensionOfADatabaseIsFoundWhateverItsCapitalsTest {

    private final CaseDatabaseManager manager = new CaseDatabaseManager();

    @ParameterizedTest
    @CsvSource({"CSV, csv", "Csv, csv", "XLSX, xlsx", "Xls, xls", "ARFF, arff"})
    void theReaderAndTheWriterAreThoseOfTheExtensionInSmallLetters(String written, String extension)
            throws Exception {
        assertEquals(manager.getReader(extension).getClass(), manager.getReader(written).getClass());
        assertEquals(manager.getWriter(extension).getClass(), manager.getWriter(written).getClass());
    }

    @Test void aFileWithItsExtensionInCapitalsIsRead(@TempDir Path folder) throws Exception {
        Path file = Files.writeString(folder.resolve("DATOS.CSV"), "A,B\nx,u\ny,v\n");
        String name = file.getFileName().toString();

        CaseDatabase database = manager.getReader(name.substring(name.lastIndexOf('.') + 1)).load(file.toFile());

        assertEquals(2, database.getCases().length);
    }

    @Test void anExtensionThatNoReaderHasIsRefusedSayingThereIsNoReader() {
        NoReaderForExtension exception = assertThrows(NoReaderForExtension.class, () -> manager.getReader("abc"));

        assertEquals("There is no reader for file format abc", exception.getExceptionMessage());
    }

    @Test void anExtensionThatNoWriterHasIsRefusedSayingThereIsNoWriter() {
        NoWriterForExtensionException exception =
                assertThrows(NoWriterForExtensionException.class, () -> manager.getWriter("abc"));

        assertEquals("There is no writer for file format abc", exception.getExceptionMessage());
    }
}
