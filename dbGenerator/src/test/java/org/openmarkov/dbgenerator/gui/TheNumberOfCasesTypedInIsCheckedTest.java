/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator.gui;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The number of cases typed in the generator is read when it is a whole number greater than 0, with or without
 * blanks around it, and refused with a message otherwise.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheNumberOfCasesTypedInIsCheckedTest {

    @ParameterizedTest @ValueSource(strings = {"100", " 100 "})
    void aWholeNumberAbove0IsRead(String typed) {
        assertEquals(100, DBGeneratorGUI.numberOfCases(typed));
    }

    @ParameterizedTest @ValueSource(strings = {"abc", "-5", "0", "", "1,5"})
    void anythingElseIsRefused(String typed) {
        var refused = assertThrows(InvalidArgumentException.class, () -> DBGeneratorGUI.numberOfCases(typed));

        assertEquals("Argument number of cases (" + typed + ") is invalid because it must be a whole number greater "
                     + "than 0.", refused.getExceptionMessage());
    }
}
