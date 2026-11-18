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
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A utility, a scale, a role in cost-effectiveness or a discount that names a criterion the network does not
 * declare is refused, instead of being ignored.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACriterionThatTheNetworkDoesNotHaveIsRefusedTest {

    private static final String NETWORK = "DES-illness.pgmx";

    private static void open(URL url) throws Exception {
        FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    private Path changed(Path folder, String original, String changed, int occurrence) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(NETWORK).toURI()));
        int at = -1;
        for (int i = 0; i < occurrence; i++) {
            at = text.indexOf(original, at + 1);
        }
        Path file = folder.resolve(NETWORK);
        Files.writeString(file, text.substring(0, at) + changed + text.substring(at + original.length()));
        return file;
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "<Criterion name=\"effectiveness\"/>|<Criterion name=\"efectiveness\"/>|1|Variable QALY|efectiveness",
            "<Scale Criterion=\"effectiveness\"|<Scale Criterion=\"efectiveness\"|1|Scale|efectiveness",
            "<Scale Criterion=\"effectiveness\"|<Scale Criterion=\"efectiveness\"|2|Scale|efectiveness",
            "<CE_Criterion Criterion=\"effectiveness\"|<CE_Criterion Criterion=\"efectiveness\"|1|CE_Criterion|efectiveness",
            "<DiscountRate Criterion=\"cost\"|<DiscountRate Criterion=\"costs\"|1|DiscountRate|costs"})
    void aMisspeltCriterionIsRefusedWithItsNameAndWhereItIs(String original, String changed, int occurrence,
                                                           String where, String name, @TempDir Path folder)
            throws Exception {
        URL url = changed(folder, original, changed, occurrence).toUri().toURL();

        PGMXParserException.UnknownCriterion exception =
                assertThrows(PGMXParserException.UnknownCriterion.class, () -> open(url));
        assertEquals(where, exception.elementName);
        assertEquals(name, exception.criterionName);
    }

    @Test void theMessageSaysTheCriterionAndWhereItIsUsed(@TempDir Path folder) throws Exception {
        URL url = changed(folder, "<Criterion name=\"effectiveness\"/>", "<Criterion name=\"efectiveness\"/>", 1)
                .toUri().toURL();

        PGMXParserException.UnknownCriterion exception =
                assertThrows(PGMXParserException.UnknownCriterion.class, () -> open(url));
        assertEquals("The file cannot be read: Variable QALY names the criterion efectiveness, which is not in the "
                     + "list of decision criteria of the network.", exception.getExceptionMessage());
    }

    @Test void aCriterionWrittenInCapitalsIsStillFound(@TempDir Path folder) throws Exception {
        open(changed(folder, "<Criterion name=\"effectiveness\"/>", "<Criterion name=\"Effectiveness\"/>", 1)
                     .toUri().toURL());
    }
}
