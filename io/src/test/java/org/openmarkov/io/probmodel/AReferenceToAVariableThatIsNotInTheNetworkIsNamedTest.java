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
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A relation, a link or a finding that names a variable the network does not have, or that does not say its name,
 * is rejected with a message that says which name is wrong (error 396).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AReferenceToAVariableThatIsNotInTheNetworkIsNamedTest {

    private static final String MAMMOGRAPHY = "MID-mammography.pgmx";
    private static final String TREE = "(<Potential type=\"Tree/ADD\" role=\"conditionalProbability\">\\s*<Variables>"
                                       + "(?:\\s*<Variable[^>]*/>){3}\\s*)<Variable name=\"Breast density\" />";
    private static final String SUBTREE = "(<Potential type=\"Tree/ADD\">\\s*<Variables>"
                                          + "(?:\\s*<Variable[^>]*/>){3}\\s*)<Variable name=\"Breast density\" />";

    @TempDir Path directory;

    /** A copy of a test network with the first match of {@code regex} replaced by {@code replacement}. */
    private URL copyOf(String network, String regex, String replacement) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(network).toURI()));
        assertTrue(Pattern.compile(regex).matcher(text).find(), network + " does not contain " + regex);
        Path copy = Files.writeString(directory.resolve(network), text.replaceFirst(regex, replacement));
        return copy.toUri().toURL();
    }

    private static Object open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    @ParameterizedTest
    @ValueSource(strings = {MAMMOGRAPHY, "BN-conditional-gaussian.pgmx", "test-ici-reading.pgmx"})
    void theUnchangedFileIsRead(String network) throws Exception {
        assertNotNull(open(copyOf(network, "<ProbNet ", "<ProbNet ")));
    }

    static Stream<Arguments> wrongReferences() {
        return Stream.of(
                Arguments.of("a finding", MAMMOGRAPHY,
                             "<EvidenceCase variable=\"Breast density\"", "<EvidenceCase variable=\"Breast densty\"",
                             "a finding names the variable Breast densty"),
                Arguments.of("a link to a cycle that does not exist", MAMMOGRAPHY,
                             "(<Link directed=\"true\">\\s*<Variable name=\"State\" timeSlice=\")0", "$19",
                             "a link names the variable State [9]"),
                Arguments.of("a tree", MAMMOGRAPHY, TREE, "$1<Variable name=\"Breast densty\" />",
                             "the relation of State [1] names the variable Breast densty"),
                Arguments.of("a subtree", MAMMOGRAPHY, SUBTREE, "$1<Variable name=\"Breast densty\" />",
                             "the relation of State [1] names the variable Breast densty"),
                Arguments.of("the variable a tree is split on", MAMMOGRAPHY,
                             "<TopVariable name=\"Death \\(OC\\)\"", "<TopVariable name=\"Death (0C)\"",
                             "the relation of State [1] names the variable Death (0C) [1]"),
                Arguments.of("the utility variable of a table", MAMMOGRAPHY,
                             "<UtilityVariable name=\"Cost of mamm\"", "<UtilityVariable name=\"Cost of mam\"",
                             "a relation names the variable Cost of mam [0]"),
                Arguments.of("the utility variable of a leaf", MAMMOGRAPHY,
                             "(<Potential type=\"Table\">\\s*)<UtilityVariable name=\"Last year cost\"",
                             "$1<UtilityVariable name=\"Last year cst\"",
                             "a relation names the variable Last year cst [1]"),
                Arguments.of("a relation in format 1.0", "BN-conditional-gaussian.pgmx",
                             "(<Variable name=\"Height\" />\\s*)<Variable name=\"Age\" />", "$1<Variable name=\"Agee\" />",
                             "the relation of Height names the variable Agee"),
                Arguments.of("a canonical model", "test-ici-reading.pgmx",
                             "(<Subpotentials>\\s*<Potential type=\"Table\">\\s*<Variables>\\s*<Variable name=\"H\" />\\s*)"
                             + "<Variable name=\"A\" />", "$1<Variable name=\"AA\" />",
                             "the relation of H names the variable AA"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrongReferences")
    void aNameThatIsNotAVariableOfTheNetwork(String where, String network, String regex, String replacement,
                                             String expected) throws Exception {
        URL url = copyOf(network, regex, replacement);

        OpenMarkovException exception = assertThrows(PGMXParserException.UnknownVariable.class, () -> open(url));
        assertEquals("The file cannot be read: " + expected + ", which is not in the network.",
                     exception.getExceptionMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {TREE, SUBTREE})
    void aReferenceWithoutName(String regex) throws Exception {
        URL url = copyOf(MAMMOGRAPHY, regex, "$1<Variable />");

        OpenMarkovException exception = assertThrows(PGMXParserException.DataMissing.class, () -> open(url));
        assertEquals("The file cannot be read: Variable has no name.", exception.getExceptionMessage());
    }
}
