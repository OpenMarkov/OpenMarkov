/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Sheets as people make them are read: a column computed by a formula, a title that is a number, a cell with an
 * error. And one that cannot be read is refused saying which cell or what is wrong.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnOrdinarySheetOfExcelIsReadTest {

    @TempDir Path folder;

    private static ExcelDataBaseIO reader(String extension) {
        return extension.equals("xlsx") ? new ExcelDataBaseIO() : new OldExcelDataBaseIO();
    }

    /** A file with one sheet, filled by {@code content}, whose formulas are saved with what they give, as Excel does. */
    private File sheet(String extension, Consumer<Sheet> content) throws Exception {
        File file = folder.resolve("cases." + extension).toFile();
        try (Workbook workbook = extension.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {
            content.accept(workbook.createSheet("cases"));
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            workbook.write(out);
        }
        return file;
    }

    /** The titles Level and Value in the cells of {@code firstColumn} and the next one of the first row. */
    private static void titles(Sheet sheet, int firstColumn) {
        Row row = sheet.createRow(0);
        row.createCell(firstColumn).setCellValue("Level");
        row.createCell(firstColumn + 1).setCellValue("Value");
    }

    private static List<String> names(CaseDatabase database) {
        return database.getVariables().stream().map(Variable::getName).toList();
    }

    private static List<List<String>> cases(CaseDatabase database) {
        List<List<String>> cases = new ArrayList<>();
        for (int[] aCase : database.getCases()) {
            List<String> values = new ArrayList<>();
            for (int column = 0; column < aCase.length; column++) {
                values.add(database.getVariables().get(column).getStateName(aCase[column]));
            }
            cases.add(values);
        }
        return cases;
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aFormulaCountsAsTheValueItGives(String extension) throws Exception {
        File file = sheet(extension, sheet -> {
            Row titles = sheet.createRow(0);
            titles.createCell(0).setCellValue("Value");
            titles.createCell(1).setCellValue("Level");
            titles.createCell(2).setCellValue("Positive");
            titles.createCell(3).setCellValue("Double");
            for (int i = 1; i <= 2; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(i == 1 ? 0 : 3);
                row.createCell(1).setCellFormula("IF(A" + (i + 1) + ">1,\"high\",\"low\")");
                row.createCell(2).setCellFormula("A" + (i + 1) + ">0");
                row.createCell(3).setCellFormula("A" + (i + 1) + "*2");
            }
        });

        CaseDatabase read = reader(extension).load(file);

        assertEquals(List.of(List.of("0", "low", "false", "0"), List.of("3", "high", "true", "6")), cases(read));
        assertEquals(new ErrorCellsOfASheet(0, null), ErrorCellsOfASheet.in(file));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aTitleMayBeANumber(String extension) throws Exception {
        File file = sheet(extension, sheet -> {
            sheet.createRow(0).createCell(0).setCellValue(2024);
            sheet.getRow(0).createCell(1).setCellValue("Sex");
            sheet.createRow(1).createCell(0).setCellValue("a");
            sheet.getRow(1).createCell(1).setCellValue("M");
        });

        assertEquals(List.of("2024", "Sex"), names(reader(extension).load(file)));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void theCellsWithAnErrorAreUnknownValuesAndAreCounted(String extension) throws Exception {
        File file = sheet(extension, sheet -> {
            titles(sheet, 0);
            for (int i = 1; i <= 3; i++) {
                sheet.createRow(i).createCell(0).setCellValue("a" + i);
            }
            sheet.getRow(1).createCell(1).setCellValue(1);
            sheet.getRow(2).createCell(1).setCellErrorValue(FormulaError.NA.getCode());
            sheet.getRow(3).createCell(1).setCellFormula("1/0");
        });

        CaseDatabase read = reader(extension).load(file);

        assertEquals(List.of(List.of("a1", "1"), List.of("a2", "?"), List.of("a3", "?")), cases(read));
        assertEquals(new ErrorCellsOfASheet(2, "B3"), ErrorCellsOfASheet.in(file));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aColumnWithValuesAndWithoutTitleIsRefusedNamingItsCell(String extension) throws Exception {
        File tableFromB1 = sheet(extension, sheet -> {
            titles(sheet, 1);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("x");
            row.createCell(1).setCellValue("high");
            row.createCell(2).setCellValue(1);
        });
        ParsingSourceException.ColumnWithoutName refusal = assertThrows(
                ParsingSourceException.ColumnWithoutName.class, () -> reader(extension).load(tableFromB1));
        assertEquals("The cell A1 of cases." + extension + " is empty, and its column has values. The first row of "
                     + "the sheet must name every column, from the cell A1 on.", refusal.toString());

        File valuesBeyondTheTitles = sheet(extension, sheet -> {
            titles(sheet, 0);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("high");
            row.createCell(1).setCellValue(1);
            row.createCell(2).setCellValue("lost");
        });
        assertEquals("C1", assertThrows(ParsingSourceException.ColumnWithoutName.class,
                                        () -> reader(extension).load(valuesBeyondTheTitles)).cell);

        File firstRowEmpty = sheet(extension, sheet -> sheet.createRow(1).createCell(0).setCellValue("x"));
        assertEquals("A1", assertThrows(ParsingSourceException.ColumnWithoutName.class,
                                        () -> reader(extension).load(firstRowEmpty)).cell);
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aBlankCellWithFormatAfterTheTitlesIsNotAColumn(String extension) throws Exception {
        File file = sheet(extension, sheet -> {
            titles(sheet, 0);
            sheet.getRow(0).createCell(2).setBlank();
            sheet.createRow(1).createCell(0).setCellValue("high");
            sheet.getRow(1).createCell(1).setCellValue(1);
        });

        assertEquals(List.of("Level", "Value"), names(reader(extension).load(file)));
    }

    @ParameterizedTest @ValueSource(strings = {"xlsx", "xls"})
    void aFileThatIsNotASheetOfItsExtensionIsRefused(String extension) throws Exception {
        File text = folder.resolve("text." + extension).toFile();
        Files.writeString(text.toPath(), "A,B\nx,y\n");
        ParsingSourceException.NotASheet refusal =
                assertThrows(ParsingSourceException.NotASheet.class, () -> reader(extension).load(text));
        assertEquals("text." + extension + " is not an Excel file of the type that its extension says. Check the "
                     + "file, or give it the extension of its type.", refusal.toString());

        String other = extension.equals("xlsx") ? "xls" : "xlsx";
        File ofTheOtherType = sheet(other, sheet -> titles(sheet, 0));
        File renamed = folder.resolve("renamed." + extension).toFile();
        Files.copy(ofTheOtherType.toPath(), renamed.toPath());
        assertThrows(ParsingSourceException.NotASheet.class, () -> reader(extension).load(renamed));

        File empty = folder.resolve("empty." + extension).toFile();
        Files.write(empty.toPath(), new byte[0]);
        Class<? extends Exception> refusalOfAnEmptyFile =
                extension.equals("xlsx") ? EmptyDatabaseException.class : ParsingSourceException.NotASheet.class;
        assertThrows(refusalOfAnEmptyFile, () -> reader(extension).load(empty));
    }
}
