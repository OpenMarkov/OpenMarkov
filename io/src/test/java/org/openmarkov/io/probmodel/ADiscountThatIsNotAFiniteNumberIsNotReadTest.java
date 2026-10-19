/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * In the network of events of the tests, the discount of the criterion «cost» is 0.03. A file whose discount is not a
 * finite number used to be opened, and then the window of the inference options, which shows the discount in
 * percent, could not be opened (error 298). Now the file is not opened, and the message names the criterion.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ADiscountThatIsNotAFiniteNumberIsNotReadTest {

    private static final String DISCOUNT = "<DiscountRate Criterion=\"cost\" unit=\"YEAR\" value=\"0.03\"/>";

    @TempDir Path folder;

    private String saved;

    @BeforeEach void readTheFile() throws Exception {
        saved = Files.readString(Path.of(getClass().getClassLoader().getResource("DES-illness.pgmx").toURI()));
    }

    private ProbNet read(String discount) throws Exception {
        String text = saved.replace(DISCOUNT, DISCOUNT.replace("0.03", discount));
        if (!discount.equals("0.03")) {
            assertNotEquals(saved, text, "the file has " + DISCOUNT);
        }
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, text);
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    @ParameterizedTest
    @ValueSource(strings = { "NaN", "Infinity", "-Infinity", "abc" })
    void isNotRead(String discount) {
        var refused = assertThrows(PGMXParserException.DiscountOutOfRange.class, () -> read(discount));

        assertEquals("Criterion cost cannot be read: its discount, " + discount + ", must be a finite number.",
                     refused.getExceptionMessage());
    }

    @Test void aFiniteDiscountIsRead() throws Exception {
        Criterion cost = read("0.03").getDecisionCriteria().stream()
                                     .filter(criterion -> criterion.getCriterionName().equals("cost")).findFirst().orElseThrow();

        assertEquals(0.03, cost.getDiscount());
    }
}
