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
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * The network of events of the tests, with one of its options changed so that it lacks a datum or has a value that
 * does not exist, is not opened, and the message says which element lacks what or has which value.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AMissingOrUnknownInferenceOptionIsRefusedByNameTest {

    @TempDir Path folder;

    static Stream<Arguments> changes() {
        return Stream.of(
                arguments("<Scale Criterion=\"cost\" Value=\"1.0\"/>", "<Scale Criterion=\"cost\"/>",
                          "Scale has no Value."),
                arguments("<Scale Criterion=\"cost\" Value=\"1.0\"/>", "<Scale Value=\"1.0\"/>",
                          "Scale has no Criterion."),
                arguments("<Scale Criterion=\"cost\" Value=\"1.0\"/>", "<Scale Criterion=\"cost\" Value=\"abc\"/>",
                          "Scale has the Value abc, which is not valid."),
                arguments("<SelectedAnalysisType>UNICRITERION</SelectedAnalysisType>", "",
                          "MulticriteriaOptions has no SelectedAnalysisType."),
                arguments("<SelectedAnalysisType>UNICRITERION</SelectedAnalysisType>",
                          "<SelectedAnalysisType>unicriterion</SelectedAnalysisType>",
                          "SelectedAnalysisType has the value unicriterion, which is not valid."),
                arguments("<DiscountRate Criterion=\"cost\" unit=\"YEAR\" value=\"0.03\"/>",
                          "<DiscountRate unit=\"YEAR\" value=\"0.03\"/>", "DiscountRate has no Criterion."),
                arguments("<DiscountRate Criterion=\"cost\" unit=\"YEAR\" value=\"0.03\"/>",
                          "<DiscountRate Criterion=\"cost\" value=\"0.03\"/>", "DiscountRate has no unit."),
                arguments("<DiscountRate Criterion=\"cost\" unit=\"YEAR\" value=\"0.03\"/>",
                          "<DiscountRate Criterion=\"cost\" unit=\"year\" value=\"0.03\"/>",
                          "DiscountRate has the unit year, which is not valid."),
                arguments("<Slices>7</Slices>", "", "TemporalOptions has no Slices."),
                arguments("<Slices>7</Slices>", "<Slices>seven</Slices>",
                          "Slices has the value seven, which is not valid."),
                arguments("<Transition>BEGINNING</Transition>", "<Transition>START</Transition>",
                          "Transition has the value START, which is not valid."),
                arguments("<CE_Criterion Criterion=\"cost\" Value=\"Cost\"/>", "<CE_Criterion Criterion=\"cost\"/>",
                          "CE_Criterion has no Value."),
                arguments("<TimeUnit Value=\"1.0\" unit=\"YEAR\"/>", "<TimeUnit Value=\"1.0\" unit=\"year\"/>",
                          "TimeUnit has the unit year, which is not valid."),
                arguments("<TimeUnit Value=\"1.0\" unit=\"YEAR\"/>", "<TimeUnit Value=\"one\" unit=\"YEAR\"/>",
                          "TimeUnit has the Value one, which is not valid."));
    }

    @ParameterizedTest @MethodSource("changes")
    void isRefused(String written, String changed, String message) throws Exception {
        String saved = saved();
        int at = saved.indexOf(written);
        assertTrue(at >= 0, "the file has " + written);
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, saved.substring(0, at) + changed + saved.substring(at + written.length()));

        var refused = assertThrows(PGMXParserException.class, () -> new PGMXReader().read(file.toUri().toURL()));

        assertEquals("The file cannot be read: " + message, refused.getExceptionMessage());
    }

    @Test void theFileAsItIsIsRead() throws Exception {
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, saved());

        assertNotNull(new PGMXReader().read(file.toUri().toURL()).probNet());
    }

    private String saved() throws Exception {
        return Files.readString(Path.of(getClass().getClassLoader().getResource("DES-illness.pgmx").toURI()));
    }
}
