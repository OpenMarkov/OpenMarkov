/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.component;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The limits of the intervals of a numeric variable, as the table of its domain shows them. Most utility nodes of
 * the networks of the repository have precision 0.0 or 2.0.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheLimitsOfTheIntervalsAreShownWithTheDecimalsOfThePrecisionTest {

    private static List<String> shown(String precision) {
        return Arrays.asList(DiscretizeTablePanel.convertToStringLimitValues(new double[]{ 0, 10, 3.14, 2.5, 1234 },
                                                                             precision));
    }

    @Test void aPrecisionWithoutDecimalsShowsNoDecimalPoint() {
        List<String> wholeNumbers = List.of("0", "10", "3", "2", "1234");

        assertEquals(wholeNumbers, shown("0.0"));
        assertEquals(wholeNumbers, shown("1.0"));
        assertEquals(wholeNumbers, shown("2.0"));
        assertEquals(wholeNumbers, shown("10.0"));
        assertEquals(wholeNumbers, shown("100.0"));
    }

    @Test void aPrecisionWithDecimalsShowsThatManyDecimals() {
        assertEquals(List.of("0.0", "10.0", "3.1", "2.5", "1234.0"), shown("0.5"));
        assertEquals(List.of("0.00", "10.00", "3.14", "2.50", "1234.00"), shown("0.01"));
        assertEquals("3.1400000000", shown("1.0E-10").get(2));
    }
}
