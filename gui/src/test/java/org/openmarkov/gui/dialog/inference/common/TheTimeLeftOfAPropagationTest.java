/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The time left that the progress dialog of a propagation tells.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTimeLeftOfAPropagationTest {

    @Test void aQuarterDoneInTenSecondsLeavesThirty() {
        assertEquals(30, PropagationProgressDialog.secondsLeft(10_000, 0.25));
    }

    @Test void withAlmostNothingDoneItIsNotTold() {
        assertEquals(-1, PropagationProgressDialog.secondsLeft(10_000, 0.01));
    }

    @Test void whenAllIsDoneItIsNotTold() {
        assertEquals(-1, PropagationProgressDialog.secondsLeft(10_000, 1));
    }
}
