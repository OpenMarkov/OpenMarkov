/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.SplitSetManager.GenerationOrder;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The sentence that tells how a dataset was split writes its percentages with two decimals (error 374).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheSentenceOfASplitDatasetTest {

    @Test void threeOfSevenCasesAtRandom() {
        assertEquals("<li>3 (42.86%) randomly selected cases for testing.</li>"
                     + "<li>4 (57.14%) remaining cases for training.</li>",
                     SplitDatasetDialog.casesDescription(GenerationOrder.Random, 3, 4, 7));
    }

    @Test void theFirstAndTheLastCases() {
        assertEquals("<li>The first 1 (25.00%) cases for testing.</li><li>3 (75.00%) cases for training.</li>",
                     SplitDatasetDialog.casesDescription(GenerationOrder.TestsFirst, 1, 3, 4));
        assertEquals("<li>The last 1 (25.00%) cases for testing.</li><li>3 (75.00%) cases for training.</li>",
                     SplitDatasetDialog.casesDescription(GenerationOrder.TestsLast, 1, 3, 4));
    }
}
