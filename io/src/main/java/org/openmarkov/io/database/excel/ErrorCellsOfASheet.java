/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.OpenMarkovException;

import java.io.File;
import java.io.IOException;

/**
 * The cells with an error, such as #N/A or #DIV/0!, among the cases of an Excel file. The readers take them for
 * unknown values.
 *
 * @param count how many there are
 * @param first the first one, as Excel names it (C7), or null if there is none
 *
 * @author Manuel Arias
 */
public record ErrorCellsOfASheet(int count, @Nullable String first) {

    private static final ErrorCellsOfASheet NONE = new ErrorCellsOfASheet(0, null);

    /** @return the cells with an error of the first sheet of {@code file}; none if it is not an Excel file that can be read */
    public static ErrorCellsOfASheet in(File file) {
        ExcelDataBaseIO reader = switch (FilenameUtils.getExtension(file.getName()).toLowerCase()) {
            case "xlsx" -> new ExcelDataBaseIO();
            case "xls" -> new OldExcelDataBaseIO();
            default -> null;
        };
        if (reader == null) {
            return NONE;
        }
        try (Workbook workbook = reader.open(file)) {
            int count = 0;
            String first = null;
            for (Row row : workbook.getSheetAt(0)) {
                for (Cell cell : row) {
                    if (row.getRowNum() > 0 && ExcelDataBaseIO.hasAnError(cell)) {
                        count++;
                        if (first == null) {
                            first = cell.getAddress().formatAsString();
                        }
                    }
                }
            }
            return new ErrorCellsOfASheet(count, first);
        } catch (IOException | OpenMarkovException | RuntimeException notReadable) {
            // The reader will say why when it reads the file
            return NONE;
        }
    }
}
