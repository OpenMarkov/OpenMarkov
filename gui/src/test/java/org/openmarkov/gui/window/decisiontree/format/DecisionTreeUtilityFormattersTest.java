/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CEP;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecisionTreeUtilityFormattersTest {

    private static final DecimalFormat DF =
            new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Test
    void dispatchesDoubleToDoubleFormatter() {
        String out = DecisionTreeUtilityFormatters.format(7.5, DF, false);
        assertEquals(" U=7.5000", out);
    }

    @Test
    void dispatchesCEPToCepFormatter() {
        CEP zero = CEP.getZeroPartition();
        String out = DecisionTreeUtilityFormatters.format(zero, DF, false);
        assertEquals(" ", out);
    }

    @Test
    void nullUtilityProducesEmptyString() {
        assertEquals("", DecisionTreeUtilityFormatters.format(null, DF, false));
    }

    @Test
    void unsupportedTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DecisionTreeUtilityFormatters.format("not-a-utility", DF, false));
    }
}
