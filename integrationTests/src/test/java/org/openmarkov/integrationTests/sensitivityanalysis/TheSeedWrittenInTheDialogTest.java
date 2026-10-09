/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.sensitivityanalysis.dialog.SensitivityAnalysisDialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * What the sensitivity analysis dialog takes as the seed from the text of its field.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheSeedWrittenInTheDialogTest {

    @Test void aNumberIsTheSeed() {
        assertEquals(2026L, SensitivityAnalysisDialog.seedOf("2026"));
        assertEquals(7L, SensitivityAnalysisDialog.seedOf(" 7 "));
    }

    @Test void anEmptyFieldIsNoSeed() {
        assertNull(SensitivityAnalysisDialog.seedOf(""));
    }

    @Test void aNumberThatDoesNotFitIsNoSeed() {
        assertNull(SensitivityAnalysisDialog.seedOf("1234567890123456789"));
    }
}
