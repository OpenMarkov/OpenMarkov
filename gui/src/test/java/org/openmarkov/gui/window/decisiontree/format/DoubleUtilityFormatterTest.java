/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DoubleUtilityFormatterTest {

    private static final DecimalFormat DF =
            new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Test
    void plainValueWithoutSlashPrefix() {
        String out = DoubleUtilityFormatter.INSTANCE.format(7.5, DF, false);
        assertEquals(" U=7.5000", out);
    }

    @Test
    void plainValueWithSlashPrefix() {
        String out = DoubleUtilityFormatter.INSTANCE.format(7.5, DF, true);
        assertEquals(" /  U=7.5000", out);
    }

    @Test
    void nanIsRenderedAsZero() {
        String out = DoubleUtilityFormatter.INSTANCE.format(Double.NaN, DF, false);
        assertEquals(" U=0.0000", out);
    }

    @Test
    void nullIsRenderedAsZero() {
        String out = DoubleUtilityFormatter.INSTANCE.format(null, DF, false);
        assertEquals(" U=0.0000", out);
    }

    @Test
    void instanceIsSingleton() {
        assertSame(DoubleUtilityFormatter.INSTANCE, DoubleUtilityFormatter.INSTANCE);
    }
}
