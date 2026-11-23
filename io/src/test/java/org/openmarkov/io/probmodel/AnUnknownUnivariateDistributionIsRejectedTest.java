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
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
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
 * A univariate distribution whose name or parametrization the program does not know, or that does not say its name,
 * is rejected with a message that names the node, and is not read as a fixed value (error 404).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnUnknownUnivariateDistributionIsRejectedTest {

    private static final String NETWORK = "ID-CEA-minimal-1-0.pgmx";
    private static final String RELATION_OF_COST =
            "(<Potential type=\"UnivariateDistr\") distribution=\"Exact\"(>\\s*<Variables>\\s*"
            + "<Variable name=\"Cost\" />\\s*<Variable name=\"Therapy\" />\\s*</Variables>\\s*)<Values>0.0 14000.0</Values>";

    @TempDir Path directory;

    /** A copy of the test network whose relation of Cost has those attributes and those numbers. */
    private URL costWith(String attributes, String values) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(NETWORK).toURI()));
        assertTrue(Pattern.compile(RELATION_OF_COST).matcher(text).find(), NETWORK + " has no relation of Cost");
        String replacement = "$1 " + attributes + "$2<Values>" + values + "</Values>";
        Path copy = Files.writeString(directory.resolve(NETWORK), text.replaceFirst(RELATION_OF_COST, replacement));
        return copy.toUri().toURL();
    }

    private static ProbNetInfo open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "distribution=\"Normal\"                                 | 0.0 1.0 14000.0 2.0 | NormalFunction",
            "distribution=\"Normal\" parametrization=\"mu, sigma\"   | 0.0 1.0 14000.0 2.0 | NormalFunction"})
    void aKnownDistributionIsRead(String attributes, String values, String function) throws Exception {
        ProbNetInfo read = open(costWith(attributes, values));

        UnivariateDistrPotential relation =
                (UnivariateDistrPotential) read.probNet().getNode("Cost").getPotentials().getFirst();
        assertEquals(function, relation.getProbDensFunctionClass().getSimpleName());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "distribution=\"Gaussian\"                                 | Gaussian without a parametrization",
            "distribution=\"Gaussian\" parametrization=\"mu, sigma\"   | Gaussian with the parametrization mu, sigma",
            "distribution=\"Normal\" parametrization=\"mean, sd\"      | Normal with the parametrization mean, sd",
            "distribution=\"Normal\" parametrization=\"\"              | 'Normal with the parametrization '"})
    void anUnknownDistribution(String attributes, String expected) throws Exception {
        URL url = costWith(attributes, "0.0 1.0 14000.0 2.0");

        OpenMarkovException exception = assertThrows(PGMXParserException.UnknownDistribution.class, () -> open(url));
        assertEquals("The relation of Cost cannot be read: the program does not know the distribution "
                     + expected + ".", exception.getExceptionMessage());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {"role=\"utility\"", "role=\"utility\" parametrization=\"mu, sigma\""})
    void aDistributionWithoutName(String attributes) throws Exception {
        URL url = costWith(attributes, "0.0 14000.0");

        OpenMarkovException exception = assertThrows(PGMXParserException.DataMissing.class, () -> open(url));
        assertEquals("The file cannot be read: the relation of Cost has no distribution.",
                     exception.getExceptionMessage());
    }
}
