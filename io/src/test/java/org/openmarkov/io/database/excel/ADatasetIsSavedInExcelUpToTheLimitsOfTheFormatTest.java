/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A dataset is saved in Excel up to the number of rows and columns of the format, and beyond them it is refused with
 * a message that proposes the formats without limits.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ADatasetIsSavedInExcelUpToTheLimitsOfTheFormatTest {

    private static CaseDatabase database(int numberOfCases, int numberOfVariables) {
        List<Variable> variables = new ArrayList<>();
        for (int v = 0; v < numberOfVariables; v++) {
            variables.add(new Variable("V" + v, "a", "b"));
        }
        int[][] cases = new int[numberOfCases][numberOfVariables];
        for (int i = 0; i < numberOfCases; i++) {
            cases[i][0] = i % 2;
        }
        return new CaseDatabase(variables, cases);
    }

    @Test void moreThan32768CasesAreSavedInXlsx(@TempDir Path folder) throws Exception {
        File file = folder.resolve("cases.xlsx").toFile();
        CaseDatabase database = database(32_769, 3);
        CaseDatabaseManager manager = new CaseDatabaseManager();

        manager.getWriter("xlsx").save(file, database);
        CaseDatabase reread = manager.getReader("xlsx").load(file);

        assertEquals(32_769, reread.getCases().length);
        assertArrayEquals(database.getCases()[32_768], reread.getCases()[32_768]);
    }

    @Test void moreThan32768CasesAreSavedInXls(@TempDir Path folder) throws Exception {
        File file = folder.resolve("cases.xls").toFile();
        CaseDatabaseManager manager = new CaseDatabaseManager();

        manager.getWriter("xls").save(file, database(32_769, 3));

        assertEquals(32_769, manager.getReader("xls").load(file).getCases().length);
    }

    @Test void tooManyCasesOrVariablesForXlsAreRefusedWithoutAFile(@TempDir Path folder) throws Exception {
        File file = folder.resolve("cases.xls").toFile();
        CaseDatabaseManager manager = new CaseDatabaseManager();

        IOException rows = assertThrows(IOException.class, () -> manager.getWriter("xls").save(file, database(65_536, 1)));
        IOException columns = assertThrows(IOException.class, () -> manager.getWriter("xls").save(file, database(2, 257)));

        assertTrue(rows.getMessage().contains("at most 65535 cases and 256 variables"), rows.getMessage());
        assertTrue(columns.getMessage().contains("Save it as CSV or as Weka (.arff)."), columns.getMessage());
        assertFalse(file.exists());
    }
}
