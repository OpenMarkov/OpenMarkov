/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The number or the date of a cell is read as the sheet shows it: a number of many digits with all of them, a date
 * with the format of its cell, and a decimal with the digits of that format.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANumberOfASheetIsReadAsTheSheetShowsItTest {

    @TempDir Path folder;

    private interface Content {
        void fill(Workbook workbook, Sheet sheet);
    }

    private static ExcelDataBaseIO reader(String extension) {
        return extension.equals("xlsx") ? new ExcelDataBaseIO() : new OldExcelDataBaseIO();
    }

    /** What is read from a sheet with the title X in A1 and whatever {@code content} puts under it. */
    private List<String> read(String extension, Content content) throws Exception {
        File file = folder.resolve("cases." + extension).toFile();
        try (Workbook workbook = extension.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("cases");
            sheet.createRow(0).createCell(0).setCellValue("X");
            content.fill(workbook, sheet);
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            workbook.write(out);
        }
        CaseDatabase database = reader(extension).load(file);
        Variable x = database.getVariables().getFirst();
        List<String> values = new ArrayList<>();
        for (int[] aCase : database.getCases()) {
            values.add(x.getStateName(aCase[0]));
        }
        return values;
    }

    private static Cell cell(Sheet sheet, int row) {
        return sheet.createRow(row).createCell(0);
    }

    private static CellStyle format(Workbook workbook, String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        return style;
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void numbersOfManyDigitsKeepThemAll(String extension) throws Exception {
        assertEquals(List.of("600123456", "3000000000", "4000000000", "-5000000000", "12345678901234", "-7", "0"),
                     read(extension, (workbook, sheet) -> {
                         double[] numbers = { 600123456, 3000000000.0, 4000000000.0, -5000000000.0, 12345678901234.0,
                                              -7, 0 };
                         for (int i = 0; i < numbers.length; i++) {
                             cell(sheet, i + 1).setCellValue(numbers[i]);
                         }
                     }));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aDateIsReadWithTheFormatOfItsCell(String extension) throws Exception {
        assertEquals(List.of("15/01/2024", "2024-01-16"), read(extension, (workbook, sheet) -> {
            Cell first = cell(sheet, 1);
            first.setCellValue(LocalDate.of(2024, 1, 15));
            first.setCellStyle(format(workbook, "dd/mm/yyyy"));
            Cell second = cell(sheet, 2);
            second.setCellValue(LocalDate.of(2024, 1, 16));
            second.setCellStyle(format(workbook, "yyyy-mm-dd"));
        }));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aComputedDecimalIsTheSameStateAsTheOneWritten(String extension) throws Exception {
        assertEquals(List.of("0.3", "0.3", "0.25", "1.5"), read(extension, (workbook, sheet) -> {
            cell(sheet, 1).setCellFormula("0.1+0.2");
            cell(sheet, 2).setCellValue(0.3);
            cell(sheet, 3).setCellValue(0.25);
            cell(sheet, 4).setCellValue(1.5);
        }));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aDecimalHasTheDigitsOfTheFormatOfItsCell(String extension) throws Exception {
        assertEquals(List.of("0.33", "2.50", "33%"), read(extension, (workbook, sheet) -> {
            Cell third = cell(sheet, 1);
            third.setCellValue(1.0 / 3);
            third.setCellStyle(format(workbook, "0.00"));
            Cell half = cell(sheet, 2);
            half.setCellValue(2.5);
            half.setCellStyle(format(workbook, "0.00"));
            Cell percentage = cell(sheet, 3);
            percentage.setCellValue(1.0 / 3);
            percentage.setCellStyle(format(workbook, "0%"));
        }));
    }
}
