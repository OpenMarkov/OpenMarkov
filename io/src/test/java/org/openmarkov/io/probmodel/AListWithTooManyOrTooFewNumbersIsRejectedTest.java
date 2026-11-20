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
 * A table, the coefficients or the covariance matrix of a regression, or the thresholds of a discretized variable,
 * with more or fewer numbers than it needs, is rejected with a message that names the node and both counts
 * (error 397).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AListWithTooManyOrTooFewNumbersIsRejectedTest {

    private static final String MAMMOGRAPHY = "MID-mammography.pgmx";
    private static final String GAUSSIAN = "BN-conditional-gaussian.pgmx";
    private static final String DES = "DES-illness.pgmx";
    private static final String CEA = "ID-CEA-minimal-1-0.pgmx";
    private static final String UTILITY_TABLE = "(<UtilityVariable name=\"Cost of mamm\"[^>]*>\\s*<Variables>\\s*"
                                                + "<Variable[^>]*>\\s*</Variables>\\s*<Values>)0.0 108.0";
    private static final String TABLE_OF_A_LEAF = "(<Variable name=\"Breast density\" />\\s*</Variables>\\s*"
                                                  + "<Values>)0.0 0.054 ";
    private static final String COEFFICIENTS = "<Coefficients>0.0012229 3.09E-17</Coefficients>";
    private static final String STATES_OF_AGE = "(<Variable name=\"Age\") type=\"finiteStates\"([^>]*>(?:(?!</Variable>).)*?</States>)";

    @TempDir Path directory;

    /** A copy of a test network with the first match of {@code regex} replaced by {@code replacement}. */
    private URL copyOf(String network, String regex, String replacement) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(network).toURI()));
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        assertTrue(pattern.matcher(text).find(), network + " does not contain " + regex);
        Path copy = Files.writeString(directory.resolve(network), pattern.matcher(text).replaceFirst(replacement));
        return copy.toUri().toURL();
    }

    private static Object open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    /** Age, with its two states, as a discretized variable split by {@code thresholds}. */
    private static String discretizedAge(String... thresholds) {
        StringBuilder replacement = new StringBuilder("$1 type=\"discretized\"$2<Thresholds>");
        for (String threshold : thresholds) {
            replacement.append("<Threshold value=\"").append(threshold).append("\" belongsTo=\"right\" />");
        }
        return replacement.append("</Thresholds>").toString();
    }

    @ParameterizedTest
    @ValueSource(strings = {MAMMOGRAPHY, GAUSSIAN, DES, CEA})
    void theUnchangedFileIsRead(String network) throws Exception {
        assertNotNull(open(copyOf(network, "<ProbNet ", "<ProbNet ")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"<CovarianceMatrix>", "<CholeskyDecomposition>"})
    void aRegressionWithAMatrixOfTheRightSizeIsRead(String matrix) throws Exception {
        String end = matrix.replace("<", "</");
        assertNotNull(open(copyOf(MAMMOGRAPHY, COEFFICIENTS, COEFFICIENTS + matrix + "0.1 0.0 0.1" + end)));
    }

    @Test
    void aDiscretizedVariableWithOneThresholdMoreThanItsStatesIsRead() throws Exception {
        assertNotNull(open(copyOf(GAUSSIAN, STATES_OF_AGE, discretizedAge("-Infinity", "40", "Infinity"))));
    }

    static Stream<Arguments> wrongLists() {
        return Stream.of(
                Arguments.of("a utility table with a number less", MAMMOGRAPHY, UTILITY_TABLE, "$1108.0",
                             "the relation of Cost of mamm [0] needs 2 numbers in Values, and it has 1"),
                Arguments.of("a utility table with a number more", MAMMOGRAPHY, UTILITY_TABLE, "$10.0 108.0 7.0",
                             "the relation of Cost of mamm [0] needs 2 numbers in Values, and it has 3"),
                Arguments.of("a probability table in format 1.0", GAUSSIAN,
                             "<Values>0.5 0.5</Values>", "<Values>0.5 0.5 0.1</Values>",
                             "the relation of Age needs 2 numbers in Values, and it has 3"),
                Arguments.of("a probability table in a leaf of a tree", MAMMOGRAPHY, TABLE_OF_A_LEAF, "$10.054 ",
                             "the relation of State [1] needs 48 numbers in Values, and it has 47"),
                Arguments.of("a distribution in format 1.0", CEA,
                             "<Values>0.0 14000.0</Values>", "<Values>0.0</Values>",
                             "the relation of Cost needs 2 numbers in Values, and it has 1"),
                Arguments.of("a transition table", DES, "<Values>0.0 1.0 </Values>", "<Values>0.0 </Values>",
                             "the relation of Ill needs 2 numbers in Values, and it has 1"),
                Arguments.of("a distribution table", DES,
                             "<Values>2.0 5.0 3.0 6.0 </Values>", "<Values>2.0 5.0 3.0 </Values>",
                             "the relation of Onset needs 4 numbers in Values, and it has 3"),
                Arguments.of("the coefficients of a regression", MAMMOGRAPHY,
                             COEFFICIENTS, "<Coefficients>0.0012229</Coefficients>",
                             "the relation of Death (OC) [1] needs 2 numbers in Coefficients, and it has 1"),
                Arguments.of("the covariance matrix of a regression", MAMMOGRAPHY,
                             COEFFICIENTS, COEFFICIENTS + "<CovarianceMatrix>0.1 0.1</CovarianceMatrix>",
                             "the relation of Death (OC) [1] needs 3 numbers in CovarianceMatrix, and it has 2"),
                Arguments.of("the Cholesky decomposition of a regression", MAMMOGRAPHY,
                             COEFFICIENTS, COEFFICIENTS + "<CholeskyDecomposition>0.1 0.0 0.1 0.0</CholeskyDecomposition>",
                             "the relation of Death (OC) [1] needs 3 numbers in CholeskyDecomposition, and it has 4"),
                Arguments.of("the thresholds of a discretized variable", GAUSSIAN,
                             STATES_OF_AGE, discretizedAge("-Infinity", "Infinity"),
                             "the variable Age needs 3 numbers in Thresholds, and it has 2"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrongLists")
    void aListWithTooManyOrTooFewNumbers(String what, String network, String regex, String replacement,
                                         String expected) throws Exception {
        URL url = copyOf(network, regex, replacement);

        OpenMarkovException exception = assertThrows(PGMXParserException.WrongNumberOfValues.class, () -> open(url));
        assertEquals("The file cannot be read: " + expected + ".", exception.getExceptionMessage());
    }
}
