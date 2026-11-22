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
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Temporal options without discount rates are read with the default discounts, and a number of simulations or of
 * series that is not an integer, 1 or more, is rejected with a message that names it (errors 405 and 442).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class MissingDiscountsAndWrongSimulationNumbersTest {

    private static final String NETWORK = "DES-illness.pgmx";

    @TempDir Path directory;

    /** A copy of the test network with the first match of {@code regex} replaced by {@code replacement}. */
    private URL copyWith(String regex, String replacement) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(NETWORK).toURI()));
        assertTrue(Pattern.compile(regex).matcher(text).find(), NETWORK + " does not contain " + regex);
        Path copy = Files.writeString(directory.resolve(NETWORK), text.replaceFirst(regex, replacement));
        return copy.toUri().toURL();
    }

    private static ProbNet open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url).probNet();
    }

    private static List<Double> discountsOf(ProbNet network) {
        return network.getDecisionCriteria().stream().map(criterion -> criterion.getDiscount()).toList();
    }

    @Test void theUnchangedFileIsReadWithItsDiscountsAndNumbers() throws Exception {
        ProbNet network = open(copyWith("<ProbNet ", "<ProbNet "));

        MonteCarloOptions simulation = network.getInferenceOptions().getMonteCarloOptions();
        assertEquals(List.of(0.03, 0.0), discountsOf(network));
        assertEquals(3, simulation.getNumSimulations());
        assertEquals(2, simulation.getNumSeries());
    }

    @Test void withoutDiscountRatesEachCriterionKeepsItsDefaultDiscount() throws Exception {
        ProbNet network = open(copyWith("(?s)<DiscountRates>.*</DiscountRates>", ""));

        assertEquals(List.of(0.0, 0.0), discountsOf(network));
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"NumberOfSimulations, abc", "NumberOfSeries, 2.5", "NumberOfSimulations, -5", "NumberOfSeries, -2",
                "NumberOfSimulations, 0", "NumberOfSeries, 0"})
    void aNumberThatIsNotAnIntegerFromOne(String element, String written) throws Exception {
        URL url = copyWith("(<" + element + ">)[^<]*", "$1" + written);

        OpenMarkovException exception = assertThrows(PGMXParserException.DataNotValid.class, () -> open(url));
        assertEquals("The file cannot be read: " + element + " has the value " + written + ", which is not valid.",
                     exception.getExceptionMessage());
    }
}
