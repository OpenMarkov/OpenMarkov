/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The message shown when no sample of a stochastic propagation is compatible with the evidence.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheZeroSampleWeightMessageIsSpelledRightTest {

    @Test
    void theMessageSaysWeight() {
        var exception = new IncompatibleEvidenceException.SamplesWeightIsZero(new double[0][0]);
        assertEquals("Samples weight is 0", exception.getExceptionMessage());
    }
}
