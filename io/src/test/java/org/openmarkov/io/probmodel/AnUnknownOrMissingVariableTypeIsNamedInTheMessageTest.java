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
import org.junit.jupiter.params.provider.ValueSource;
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
 * A variable whose type does not exist, or that has no type, is rejected with a message that names its node
 * (error 395).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnUnknownOrMissingVariableTypeIsNamedInTheMessageTest {

    @TempDir Path directory;

    /** A copy of a test network with {@code text} replaced by {@code replacement}, the first time it appears. */
    private URL copyOf(String network, String text, String replacement) throws Exception {
        String content = Files.readString(Path.of(getClass().getClassLoader().getResource(network).toURI()));
        assertTrue(content.contains(text), network + " does not contain " + text);
        Path copy = Files.writeString(directory.resolve(network),
                                      content.replaceFirst(Pattern.quote(text), replacement));
        return copy.toUri().toURL();
    }

    private static Object open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    private static String messageOpening(URL url) {
        OpenMarkovException exception = assertThrows(PGMXParserException.class, () -> open(url));
        return exception.getExceptionMessage();
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void theUnchangedFileIsRead(String network) throws Exception {
        URL url = copyOf(network, "<ProbNet ", "<ProbNet ");
        assertNotNull(open(url));
    }

    @ParameterizedTest(name = "{0}: {3}")
    @CsvSource(delimiter = '|', value = {
            "MID-mammography.pgmx         | name=\"State\" timeSlice=\"0\" type=\"finiteStates\" | FiniteStates | State [0]",
            "MID-mammography.pgmx         | name=\"Cost of mamm\" timeSlice=\"0\" type=\"numeric\" | Numeric | Cost of mamm [0]",
            "BN-conditional-gaussian.pgmx | name=\"Age\" type=\"finiteStates\"                     | FiniteStates | Age"})
    void aTypeThatDoesNotExist(String network, String variable, String type, String node) throws Exception {
        URL url = copyOf(network, variable, variable.replaceFirst("type=\"\\w+\"", "type=\"" + type + "\""));

        assertEquals("The node " + node + " has the variable type " + type + ", which does not exist: "
                     + "it must be finiteStates, numeric, discretized or event.", messageOpening(url));
    }

    @ParameterizedTest(name = "{0}: {2}")
    @CsvSource(delimiter = '|', value = {
            "MID-mammography.pgmx         | name=\"State\" timeSlice=\"0\" type=\"finiteStates\" | State [0]",
            "MID-mammography.pgmx         | name=\"Cost of mamm\" timeSlice=\"0\" type=\"numeric\" | Cost of mamm [0]",
            "BN-conditional-gaussian.pgmx | name=\"Age\" type=\"finiteStates\"                     | Age"})
    void noType(String network, String variable, String node) throws Exception {
        URL url = copyOf(network, variable, variable.replaceFirst(" type=\"\\w+\"", ""));

        assertEquals("The node " + node + " has no variable type.", messageOpening(url));
    }
}
