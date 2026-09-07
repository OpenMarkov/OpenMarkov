/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comparing two numbers and asking whether one is zero are different questions: the first admits a
 * round error that is a fraction of what is compared, the second cannot, because no fraction of
 * zero tells one number from another.
 *
 * @author Manuel Arias
 */
public class ComparingNumbersAsksOneQuestionAtATimeTest {

    @Tag(TestSpeed.FAST)
    @Test public void theComparisonGivesTheSameAnswerInBothOrders() {
        double one = 1.0;
        double justBelow = 1.0 - DiscretePotentialOperations.maxRoundErrorAllowed;

        assertEquals(DiscretePotentialOperations.almostEqual(one, justBelow),
                DiscretePotentialOperations.almostEqual(justBelow, one),
                "the order of the two numbers must not decide the answer");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aNumberFarBelowTheRoundErrorCountsAsZero() {
        assertTrue(DiscretePotentialOperations.isZero(1e-30), "1e-30 is zero for any purpose here");
        assertTrue(DiscretePotentialOperations.isZero(0.0), "zero is zero");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aNumberSmallButRealDoesNotCountAsZero() {
        assertFalse(DiscretePotentialOperations.isZero(1e-12),
                "a utility of 1e-12 is a real number, as maximizing showed");
        assertFalse(DiscretePotentialOperations.isZero(-1e-12), "and so is its negative");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theComparisonStaysRelative() {
        assertTrue(DiscretePotentialOperations.almostEqual(1e6, 1e6 + 0.001),
                "a millionth of a fraction of a million is still a round error");
        assertFalse(DiscretePotentialOperations.almostEqual(1.0, 1.001),
                "a thousandth of one is not");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theTwoQuestionsHaveTheirOwnThresholds() {
        assertTrue(DiscretePotentialOperations.maxDeviationFromZero
                        < DiscretePotentialOperations.maxRoundErrorAllowed,
                "the absolute threshold must not swallow what the fraction admits");
    }
}
