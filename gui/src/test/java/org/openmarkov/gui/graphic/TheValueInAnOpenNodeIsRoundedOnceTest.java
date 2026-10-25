/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.graphic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A value in the box of an open node is rounded to four decimals once, when written.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheValueInAnOpenNodeIsRoundedOnceTest {

    private static String written(double value) {
        VisualState state = new VisualState(null, 0, "state");
        state.setStateValue(0, value);
        return VisualState.formatValue(state.getStateValue(0));
    }

    @Test void theFourthDecimalIsRoundedFromTheValue() {
        assertEquals("0.1235", written(0.12345));
        assertEquals("0.0003", written(0.00025));
        assertEquals("0.3333", written(0.33335));
    }
}
