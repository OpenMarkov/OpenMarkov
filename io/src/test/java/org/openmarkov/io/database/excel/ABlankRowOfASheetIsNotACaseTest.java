/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A row of a sheet without any value, missing or only formatted, is not a case, as a blank line of a CSV is not.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ABlankRowOfASheetIsNotACaseTest {

    private static Workbook workbook(String extension) {
        return extension.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
    }

    private static void row(Sheet sheet, int index, String a, String b) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(a);
        row.createCell(1).setCellValue(b);
    }

    private static List<String> cases(Workbook workbook, String extension, Path folder) throws Exception {
        Path file = folder.resolve("cases." + extension);
        try (FileOutputStream out = new FileOutputStream(file.toFile())) {
            workbook.write(out);
        }
        CaseDatabase database = new CaseDatabaseManager().getReader(extension).load(file.toFile());
        List<Variable> variables = database.getVariables();
        return Arrays.stream(database.getCases())
                     .map(aCase -> variables.get(0).getStates()[aCase[0]].getName() + " "
                             + variables.get(1).getStates()[aCase[1]].getName())
                     .collect(Collectors.toList());
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aMissingRowInTheMiddleIsSkippedAndTheLastRowIsRead(String extension, @TempDir Path folder) throws Exception {
        Workbook workbook = workbook(extension);
        Sheet sheet = workbook.createSheet();
        row(sheet, 0, "A", "B");
        row(sheet, 1, "a1", "b1");
        row(sheet, 3, "a3", "b3");
        row(sheet, 4, "a4", "b4");
        row(sheet, 5, "a5", "b5");

        assertEquals(List.of("a1 b1", "a3 b3", "a4 b4", "a5 b5"), cases(workbook, extension, folder));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void rowsWithBordersAndNoDataAreNotCases(String extension, @TempDir Path folder) throws Exception {
        Workbook workbook = workbook(extension);
        Sheet sheet = workbook.createSheet();
        row(sheet, 0, "A", "B");
        row(sheet, 1, "x", "y");
        row(sheet, 2, "z", "w");
        CellStyle border = workbook.createCellStyle();
        border.setBorderBottom(BorderStyle.THIN);
        for (int i = 3; i < 6; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellStyle(border);
            row.createCell(1).setCellStyle(border);
        }

        assertEquals(List.of("x y", "z w"), cases(workbook, extension, folder));
    }
}
