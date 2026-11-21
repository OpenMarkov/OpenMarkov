/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * When the workbook of an evaluation is saved and the system cannot open it, the user is told where it is, instead
 * of seeing an internal error (error 438).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheResultsThatCannotBeOpenedAreSaidToBeSavedTest {

    private final File file = new File("EvaluationNet.xlsx").getAbsoluteFile();

    @Test void nothingIsSaidIfTheFileIsOpened() {
        assertNull(BNEvaluationDialog.noticeIfItCannotBeOpened(file, opened -> { }));
    }

    @Test void aFailureToOpenItSaysWhereItIs() {
        String notice = BNEvaluationDialog.noticeIfItCannotBeOpened(file, opened -> {
            throw new IOException("no application is registered for .xlsx");
        });

        assertEquals("The results were saved in " + file + ", but the file could not be opened: "
                     + "no application is registered for .xlsx", notice);
    }

    @Test void aSystemThatCannotOpenFilesSaysWhereItIs() {
        String notice = BNEvaluationDialog.noticeIfItCannotBeOpened(file, opened -> {
            throw new UnsupportedOperationException("the system cannot open files");
        });

        assertEquals("The results were saved in " + file + ", but the file could not be opened: "
                     + "the system cannot open files", notice);
    }
}
