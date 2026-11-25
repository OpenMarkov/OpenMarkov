/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A cell past XFD, the last column that a sheet can have, is refused naming it. No spreadsheet program writes one,
 * but a damaged file can carry it, and POI keeps only 16 bits of its column: the cell used to be read as one column
 * too many, left out without a word, or, when its column wrapped around onto another, read in the place of that
 * column's cell (error 450 of the pending list). The workbooks are written by POI with the cell in column C, and the
 * reference of the cell is then changed inside the file, where POI would not let it be written.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACellPastTheLastColumnIsRefusedTest {

    @TempDir Path folder;

    /**
     * XFE is one past XFD; ZZZZZ is in the first row, among the names; ZZZZZZZ does not fit in an int; and CRXR, the
     * column 65 538, wraps around onto column B, so its value took the place of the case's «b».
     */
    @ParameterizedTest @ValueSource(strings = {"XFE2", "ZZZZZ1", "ZZZZZZZ2", "CRXR2"})
    void aCellPastXFDIsRefusedNamingIt(String reference) throws Exception {
        File file = sheetWithACellIn(reference);
        ParsingSourceException.CellPastTheLastColumn refusal = assertThrows(
                ParsingSourceException.CellPastTheLastColumn.class, () -> new ExcelDataBaseIO().load(file));
        assertEquals(reference, refusal.cell);
        assertEquals("The cell " + reference + " of " + file.getName() + " is past XFD, the last column that a sheet "
                     + "can have. No spreadsheet program writes there, so the file is damaged and cannot be read.",
                     refusal.toString());
    }

    /** XFD is still a column of the sheet: its value has no name in the first row, as with any other column. */
    @Test
    void aCellInXFDIsStillAColumnWithoutName() throws Exception {
        File file = sheetWithACellIn("XFD2");
        assertEquals("C1", assertThrows(ParsingSourceException.ColumnWithoutName.class,
                                        () -> new ExcelDataBaseIO().load(file)).cell);
    }

    /**
     * A sheet with the variables X and Y, the cases «a b» and «c d», and one more cell with «v», moved to
     * {@code reference}, whose number says its row.
     */
    private File sheetWithACellIn(String reference) throws Exception {
        int row = Integer.parseInt(reference.replaceAll("[A-Z]", "")) - 1;
        File written = folder.resolve("written.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(written)) {
            Sheet sheet = workbook.createSheet("cases");
            String[][] values = {{"X", "Y"}, {"a", "b"}, {"c", "d"}};
            for (int i = 0; i < values.length; i++) {
                Row cells = sheet.createRow(i);
                for (int j = 0; j < values[i].length; j++) {
                    cells.createCell(j).setCellValue(values[i][j]);
                }
            }
            sheet.getRow(row).createCell(2).setCellValue("v");
            workbook.write(out);
        }
        File moved = folder.resolve("cell-in-" + reference + ".xlsx").toFile();
        String from = "r=\"C" + (row + 1) + "\"";
        try (ZipFile zip = new ZipFile(written);
             ZipOutputStream out = new ZipOutputStream(new FileOutputStream(moved))) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                byte[] bytes = zip.getInputStream(entry).readAllBytes();
                if (entry.getName().startsWith("xl/worksheets/")) {
                    String xml = new String(bytes, StandardCharsets.UTF_8);
                    String changed = xml.replace(from, "r=\"" + reference + "\"");
                    assertNotEquals(xml, changed, "The cell C" + (row + 1) + " is not where it was expected");
                    bytes = changed.getBytes(StandardCharsets.UTF_8);
                }
                out.putNextEntry(new ZipEntry(entry.getName()));
                out.write(bytes);
                out.closeEntry();
            }
        }
        return moved;
    }
}
