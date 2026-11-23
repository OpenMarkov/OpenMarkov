/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.database.excel.ErrorCellsOfASheet;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Before reading an Excel file with cells that have an error, the user is told where the only one is, or where the
 * first one is and how many there are.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheUserIsToldWhereTheErrorCellsAreTest {

    private static final File FILE = new File("survey.xlsx");

    @Test void oneCell() {
        assertEquals("The cell C7 of «survey.xlsx» has an error, such as #N/A.\n"
                     + "It can be read as an unknown value («?»).",
                     CaseDatabaseLoading.textAbout(new ErrorCellsOfASheet(1, "C7"), FILE));
    }

    @Test void severalCells() {
        assertEquals("«survey.xlsx» has 12 cells with an error, such as #N/A. The first one is C7.\n"
                     + "They can be read as unknown values («?»).",
                     CaseDatabaseLoading.textAbout(new ErrorCellsOfASheet(12, "C7"), FILE));
    }
}
