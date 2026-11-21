/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.DatabaseWithoutCasesException;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A case database that only has the names of its variables is refused when it is opened, whatever its format, and
 * the same database with a case is read (error 412).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ADatabaseWithoutCasesIsRefusedTest {

    @TempDir Path folder;

    /** A file of that format with the columns A and B and, if asked, one case. */
    private Path database(String format, boolean withACase) throws Exception {
        Path file = folder.resolve("template." + format.replace("-blank-lines", ""));
        switch (format) {
            case "csv" -> Files.writeString(file, "A,B\n" + (withACase ? "x,u\n" : ""));
            case "csv-blank-lines" -> Files.writeString(file, "A,B\n\n" + (withACase ? "x,u\n" : "") + "\n");
            case "arff" -> Files.writeString(file, "@relation r\n@attribute A {x,y}\n@attribute B {u,v}\n@data\n"
                                                   + (withACase ? "x,u\n" : ""));
            default -> {
                try (Workbook workbook = format.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
                     FileOutputStream out = new FileOutputStream(file.toFile())) {
                    Sheet sheet = workbook.createSheet();
                    row(sheet, 0, "A", "B");
                    if (withACase) {
                        row(sheet, 1, "x", "u");
                    }
                    workbook.write(out);
                }
            }
        }
        return file;
    }

    private static void row(Sheet sheet, int index, String a, String b) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(a);
        row.createCell(1).setCellValue(b);
    }

    private static CaseDatabase open(Path file) throws Exception {
        String name = file.getFileName().toString();
        return new CaseDatabaseManager().getReader(name.substring(name.lastIndexOf('.') + 1)).load(file.toFile());
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "csv-blank-lines", "xlsx", "xls", "arff"})
    void withoutCasesItIsRefused(String format) throws Exception {
        Path file = database(format, false);

        DatabaseWithoutCasesException exception = assertThrows(DatabaseWithoutCasesException.class, () -> open(file));
        assertEquals("Dataset from " + file.getFileName() + " has no cases: it only has the names of its variables.",
                     exception.getExceptionMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "csv-blank-lines", "xlsx", "xls", "arff"})
    void withACaseItIsRead(String format) throws Exception {
        CaseDatabase database = open(database(format, true));

        assertEquals(1, database.getCases().length);
        assertEquals(2, database.getVariables().size());
    }
}
