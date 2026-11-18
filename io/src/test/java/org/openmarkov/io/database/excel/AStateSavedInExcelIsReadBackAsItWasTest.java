/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Saving a case database in Excel and reading it back gives the same states: a state is written as a number only
 * when it reads back as the same text.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AStateSavedInExcelIsReadBackAsItWasTest {

    private static final List<String> STATES = List.of("01", "1.0", "3.10", "1e3", "2d", "5F", "-0", " 7 ", "0x1p3",
                                                       "08001", "texto", "12345678901", "NaN", "Infinity", "1",
                                                       "2.5", "-3");

    /** The states that the reader gives back as they are when they are written as numbers. */
    private static final List<String> NUMBERS = List.of("1", "2.5", "-3");

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void everyStateIsReadBackAndTheNumbersStayNumbers(String extension, @TempDir Path folder) throws Exception {
        Variable variable = new Variable("X", STATES.toArray(new String[0]));
        int[][] cases = new int[STATES.size()][1];
        for (int i = 0; i < cases.length; i++) {
            cases[i][0] = i;
        }
        File file = folder.resolve("cases." + extension).toFile();
        CaseDatabaseManager manager = new CaseDatabaseManager();

        manager.getWriter(extension).save(file, new CaseDatabase(List.of(variable), cases));
        CaseDatabase reread = manager.getReader(extension).load(file);

        assertEquals(STATES, Arrays.stream(reread.getVariables().getFirst().getStates()).map(State::getName).toList());
        try (Workbook workbook = WorkbookFactory.create(file)) {
            for (int i = 0; i < STATES.size(); i++) {
                CellType type = workbook.getSheetAt(0).getRow(i + 1).getCell(0).getCellType();
                assertEquals(NUMBERS.contains(STATES.get(i)) ? CellType.NUMERIC : CellType.STRING, type, STATES.get(i));
            }
        }
    }
}
