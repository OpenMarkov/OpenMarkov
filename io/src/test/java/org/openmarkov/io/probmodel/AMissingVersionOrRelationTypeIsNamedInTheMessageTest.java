/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file without format version, or a relation without type or with a type that does not exist, is rejected with a
 * message that says what is missing and in which file or node (error 393).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AMissingVersionOrRelationTypeIsNamedInTheMessageTest {

    @TempDir Path directory;

    /** A copy of a test network with the first match of {@code regex} replaced by {@code replacement}. */
    private URL copyOf(String network, String regex, String replacement) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(network).toURI()));
        assertTrue(Pattern.compile(regex).matcher(text).find(), network + " does not contain " + regex);
        Path copy = Files.writeString(directory.resolve(network), text.replaceFirst(regex, replacement));
        return copy.toUri().toURL();
    }

    private static String messageOpening(URL url) {
        OpenMarkovException exception = assertThrows(PGMXParserException.class,
                () -> FormatManager.getInstance().getProbNetReader(url).read(url));
        return exception.getExceptionMessage();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileWithoutFormatVersion(String network) throws Exception {
        URL url = copyOf(network, " formatVersion=\"[0-9.]+\"", "");

        assertEquals("File " + url.getFile() + " does not say which version of ProbModelXML it follows: "
                     + "its root element has no formatVersion attribute.", messageOpening(url));
    }

    @ParameterizedTest(name = "{0}: {3}")
    @CsvSource(delimiter = '|', value = {
            "MID-mammography.pgmx         | <Potential type=\"Delta\"                   | <Potential | State [0]",
            "BN-conditional-gaussian.pgmx | <Potential type=\"Table\"                   | <Potential | Age",
            "BN-conditional-gaussian.pgmx | (<Mean>\\s*)<Potential type=\"Table\"       | $1<Potential | Height"})
    void aRelationWithoutType(String network, String regex, String replacement, String node) throws Exception {
        assertEquals("The relation of " + node + " has no type.",
                     messageOpening(copyOf(network, regex, replacement)));
    }

    @ParameterizedTest(name = "{0}: {3}")
    @CsvSource(delimiter = '|', value = {
            "MID-mammography.pgmx         | <Potential type=\"Delta\"             | <Potential type=\"Tabla\"   | State [0]",
            "BN-conditional-gaussian.pgmx | <Potential type=\"Table\"             | <Potential type=\"Tabla\"   | Age",
            "BN-conditional-gaussian.pgmx | (<Mean>\\s*)<Potential type=\"Table\" | $1<Potential type=\"Tabla\" | Height"})
    void aRelationWithATypeThatDoesNotExist(String network, String regex, String replacement, String node)
            throws Exception {
        assertEquals("The relation of " + node + " has the type Tabla, which is not supported.",
                     messageOpening(copyOf(network, regex, replacement)));
    }
}
