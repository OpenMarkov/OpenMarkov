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
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * In the network of events of the tests, the discount of the criterion «cost» is 0.03. A file whose discount has no
 * «value» is not opened, and the message says which criterion has no discount.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ADiscountWithoutAValueIsRefusedWithItsCriterionTest {

    private static final String DISCOUNT = "<DiscountRate Criterion=\"cost\" unit=\"YEAR\" value=\"0.03\"/>";

    @TempDir Path folder;

    @Test void isNotRead() throws Exception {
        String saved = Files.readString(Path.of(getClass().getClassLoader().getResource("DES-illness.pgmx").toURI()));
        String text = saved.replace(DISCOUNT, DISCOUNT.replace(" value=\"0.03\"", ""));
        assertNotEquals(saved, text, "the file has " + DISCOUNT);
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, text);

        var refused = assertThrows(PGMXParserException.DiscountMissing.class,
                                   () -> new PGMXReader().read(file.toUri().toURL()));

        assertEquals("Criterion cost cannot be read: it has no discount.", refused.getExceptionMessage());
    }
}
