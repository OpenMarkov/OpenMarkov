package org.openmarkov.io.database.excel;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseFormat;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Reader and writer for the legacy Excel {@code .xls} (HSSF) format.
 * Extends {@link ExcelDataBaseIO} to reuse common database I/O behaviour.
 */
@CaseDatabaseFormat(extension = "xls", name = "OldExcel") public class OldExcelDataBaseIO
        extends ExcelDataBaseIO {
    
    @Override protected Workbook openWorkbook(FileInputStream stream) throws IOException {
        return new HSSFWorkbook(stream);
    }
    
    @Override
    public void save(File file, CaseDatabase database) throws IOException {
        ExcelDataBaseIO.checkItFits(database, SpreadsheetVersion.EXCEL97, ".xls");
        int numCell = 0;
        
        // create a workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        // create a sheet
        HSSFSheet sheet = wb.createSheet("new sheet");
        // Create a row to put the names of the attributes.
        Row row = sheet.createRow(0);
        
        /* Attributes */
        for (Variable variable : database.getVariables()) {
            String variableName = variable.getName();
            // Create a cell and put a value in it.
            Cell cell = row.createCell(numCell);
            cell.setCellValue(new HSSFRichTextString(variableName));
            numCell++;
        }
        
        /* Cases */
        int[][] cases = database.getCases();
        for (int i = 0; i < cases.length; i++) {
            row = sheet.createRow(i + 1);
            for (int j = 0; j < cases[i].length; j++) {
                // Create a cell and put a value in it.
                Cell cell = row.createCell(j);
                String data = database.getVariables().get(j).getStates()[cases[i][j]].getName();
                // As text, so that a case with nothing known is not a blank row, which the reader skips
                if (data.equals("?") || data.isEmpty()) {
                    cell.setCellValue(new HSSFRichTextString("?"));
                    continue;
                }
                Double number = ExcelDataBaseIO.numberToWrite(data);
                if (number == null) {
                    cell.setCellValue(new HSSFRichTextString(data));
                } else {
                    cell.setCellValue(number);
                }
            }
        }
        
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }
    
}
