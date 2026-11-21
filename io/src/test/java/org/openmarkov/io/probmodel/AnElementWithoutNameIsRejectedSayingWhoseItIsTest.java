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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A state, a decision criterion, a property or an agent without name is rejected with a message that says whose it
 * is, because a network with one of them cannot be saved (error 403).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnElementWithoutNameIsRejectedSayingWhoseItIsTest {

    private static final String MAMMOGRAPHY = "MID-mammography.pgmx";
    private static final String GAUSSIAN = "BN-conditional-gaussian.pgmx";
    private static final String AGENTS = "test-decpomdp-manual.pgmx";

    @TempDir Path directory;

    /** A copy of a test network with the first match of {@code regex} replaced by {@code replacement}. */
    private URL copyOf(String network, String regex, String replacement) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(network).toURI()));
        assertTrue(Pattern.compile(regex).matcher(text).find(), network + " does not contain " + regex);
        Path copy = Files.writeString(directory.resolve(network), text.replaceFirst(regex, replacement));
        return copy.toUri().toURL();
    }

    private static ProbNetInfo open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    @ParameterizedTest
    @ValueSource(strings = {MAMMOGRAPHY, GAUSSIAN, AGENTS})
    void theUnchangedFileIsReadAndSaved(String network) throws Exception {
        ProbNetInfo read = open(copyOf(network, "<ProbNet ", "<ProbNet "));
        Path saved = directory.resolve("saved.pgmx");

        read.writer().write(saved.toString(), read.probNet(), read.evidence());

        assertTrue(Files.size(saved) > 0);
    }

    static Stream<Arguments> elementsWithoutName() {
        return Stream.of(
                Arguments.of("a state", MAMMOGRAPHY, "<State name=\"healthy\" />", "<State />",
                             "a state of State [0]"),
                Arguments.of("a state in format 1.0", GAUSSIAN, "<State name=\"young\" />", "<State />",
                             "a state of Age"),
                Arguments.of("a decision criterion", MAMMOGRAPHY,
                             "(<DecisionCriteria>\\s*)<Criterion name=\"cost\" />", "$1<Criterion />",
                             "a decision criterion"),
                Arguments.of("a property of the network", MAMMOGRAPHY, "<DecisionCriteria>",
                             "<AdditionalProperties><Property value=\"1.0\" /></AdditionalProperties><DecisionCriteria>",
                             "a property of the network"),
                Arguments.of("a property of a variable", MAMMOGRAPHY, "(<Variable name=\"Mamm frequency\"[^>]*>)",
                             "$1<AdditionalProperties><Property value=\"7.0\" /></AdditionalProperties>",
                             "a property of Mamm frequency"),
                Arguments.of("a default state", MAMMOGRAPHY, "</ProbNet>",
                             "</ProbNet><DefaultStates><State /><State name=\"b\" /></DefaultStates>",
                             "a default state of the network"),
                Arguments.of("an agent", AGENTS, "<Agent name=\"Agent 1\"/>", "<Agent/>", "an agent"),
                Arguments.of("a property of an agent", AGENTS, "<Agent name=\"Agent 1\"/>",
                             "<Agent name=\"Agent 1\"><Property value=\"1\"/></Agent>", "a property of an agent"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("elementsWithoutName")
    void anElementWithoutName(String what, String network, String regex, String replacement, String expected)
            throws Exception {
        URL url = copyOf(network, regex, replacement);

        OpenMarkovException exception = assertThrows(PGMXParserException.DataMissing.class, () -> open(url));
        assertEquals("The file cannot be read: " + expected + " has no name.", exception.getExceptionMessage());
    }
}
