/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The precision of a variable arrives as the text of a number: 0.01, but also 0.0, 2.0 or 10.0.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class RoundingWithAPrecisionKeepsTheWholePartTest {

    @ParameterizedTest
    @ValueSource(strings = { "0.0", "1.0", "2.0", "0.5", "10.0", "100.0", "0.01", "1.0E-4", "1.0E-10" })
    void aWholeNumberIsNotChanged(String precision) {
        assertEquals(1234.0, Util.roundWithPrecision(1234, precision));
    }

    @Test void theDecimalsAreThoseOfThePrecision() {
        assertEquals(3.14, Util.roundWithPrecision(3.14159, "0.01"));
        assertEquals(3.0, Util.roundWithPrecision(3.14159, "10.0"));
        assertEquals(3.1415926536, Util.roundWithPrecision(3.14159265358979, "1.0E-10"));
    }
}
