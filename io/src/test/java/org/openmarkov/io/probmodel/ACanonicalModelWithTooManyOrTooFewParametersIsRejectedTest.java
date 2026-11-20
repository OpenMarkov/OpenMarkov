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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A table of a canonical model with more or fewer parameters than the model accepts is rejected with a message that
 * names the node and both counts (error 429).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACanonicalModelWithTooManyOrTooFewParametersIsRejectedTest {

    private static final String NETWORK = "test-ici-reading.pgmx";
    private static final String LEAK = "(<Variable name=\"H\" />\\s*</Variables>\\s*)<Values>0.99 0.01</Values>";
    private static final String TABLE_OF_TUNING = "<Values>0.9 0.1 0.0 0.2 0.8 0.0 0.3 0.0 0.7</Values>";

    @TempDir Path directory;

    /** A copy of the test network with the first match of {@code regex} replaced by {@code replacement}. */
    private URL copyWith(String regex, String replacement) throws Exception {
        String content = Files.readString(Path.of(getClass().getClassLoader().getResource(NETWORK).toURI()));
        assertTrue(Pattern.compile(regex).matcher(content).find(), NETWORK + " does not contain " + regex);
        Path copy = Files.writeString(directory.resolve(NETWORK), content.replaceFirst(regex, replacement));
        return copy.toUri().toURL();
    }

    private static Object open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    @Test void theUnchangedFileIsRead() throws Exception {
        assertNotNull(open(copyWith("<ProbNet ", "<ProbNet ")));
    }

    @Test void theFourNumbersOfATuningModelAreRead() throws Exception {
        assertNotNull(open(copyWith(TABLE_OF_TUNING, "<Values>0.7 0.0 0.0 0.9</Values>")));
    }

    @Test void aLeakWithTooFewParameters() throws Exception {
        URL url = copyWith(LEAK, "$1<Values>0.99</Values>");

        OpenMarkovException exception = assertThrows(PGMXParserException.WrongNumberOfValues.class, () -> open(url));
        assertEquals("The file cannot be read: the relation of H needs 2 numbers in Values, and it has 1.",
                     exception.getExceptionMessage());
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource(delimiter = '|', value = {
            "<Values>1.0 0.0 0.0 1.0</Values>                     | <Values>1.0 0.0 0.0</Values>         | H needs 4 numbers in Values, and it has 3",
            "<Values>1.0 0.0 0.0 1.0</Values>                     | <Values>1.0 0.0 0.0 1.0 0.5</Values> | H needs 4 numbers in Values, and it has 5",
            "<Values>0.9 0.1 0.0 0.2 0.8 0.0 0.3 0.0 0.7</Values> | <Values>0.9 0.1 0.0 0.2 0.8 0.0 0.3 0.0</Values> | I needs 4 or 9 numbers in Values, and it has 8"})
    void aTableOfAParentWithTooManyOrTooFewParameters(String table, String replacement, String expected) throws Exception {
        URL url = copyWith(table, replacement);

        OpenMarkovException exception = assertThrows(PGMXParserException.WrongNumberOfValues.class, () -> open(url));
        assertEquals("The file cannot be read: the relation of " + expected + ".", exception.getExceptionMessage());
    }
}
