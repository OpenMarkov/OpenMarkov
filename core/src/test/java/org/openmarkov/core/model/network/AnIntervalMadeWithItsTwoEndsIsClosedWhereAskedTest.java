/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The interval made with its two ends, and whether each one is closed, is closed where it is asked to be. It used to
 * keep the lower end the other way round: (true, 0, 20, true) gave (0, 20] (error 314).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnIntervalMadeWithItsTwoEndsIsClosedWhereAskedTest {

    @Test void closedAtBothEndsTakesBothEnds() {
        PartitionedInterval interval = new PartitionedInterval(true, 0, 20, true);

        assertTrue(interval.isLeftClosed());
        assertTrue(interval.isRightClosed());
        assertTrue(interval.contains(0));
        assertTrue(interval.contains(20));
    }

    @Test void openAtBothEndsLeavesBothEndsOut() {
        PartitionedInterval interval = new PartitionedInterval(false, 0, 20, false);

        assertFalse(interval.isLeftClosed());
        assertFalse(interval.isRightClosed());
        assertFalse(interval.contains(0));
        assertTrue(interval.contains(10));
        assertFalse(interval.contains(20));
    }

    @Test void aNumericVariableWithAClosedLowerEndTakesItsMinimum() {
        Variable duration = new Variable("Duration", true, 0, 20, true, 1);

        assertTrue(duration.getPartitionedInterval().contains(0));
    }

    @Test void aNumericVariableWithAnOpenLowerEndLeavesItsMinimumOut() {
        Variable duration = new Variable("Duration", false, 0, 20, true, 1);

        assertFalse(duration.getPartitionedInterval().contains(0));
    }
}
