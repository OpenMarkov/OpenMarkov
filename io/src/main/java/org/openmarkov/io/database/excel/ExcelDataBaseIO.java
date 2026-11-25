/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.excel;


import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.core.io.database.plugin.CaseDatabaseFormat;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.io.File;
import java.math.BigDecimal;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

/**
 * This class contains some routines to load a database from a '.xls' file
 * (the format used by Excel). The first line of the file has the names of
 * the variables.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@CaseDatabaseFormat(extension = "xlsx", name = "Excel") public class ExcelDataBaseIO
        implements CaseDatabaseReader, CaseDatabaseWriter {
    
    /**
     * Reads the first sheet of the file: its first row names the variables, from the cell A1 on, and each of the
     * other rows is a case. A formula counts as the value it gives; a blank cell and one with an error, such as
     * #N/A, are unknown values. {@link ErrorCellsOfASheet} tells of the cells with an error.
     */
    @Override public @NotNull CaseDatabase load(File file)
            throws IOException, EmptyDatabaseException, ParsingSourceException.RepeatedVariableNames,
                   ParsingSourceException.NotASheet, ParsingSourceException.ColumnWithoutName,
                   ParsingSourceException.CellPastTheLastColumn {
        try (Workbook workbook = open(file)) {
            return read(file, workbook.getSheetAt(0));
        }
    }
    
    /**
     * @return the workbook of the file
     *
     * @throws ParsingSourceException.NotASheet if the file is not a workbook of the format of this reader
     */
    Workbook open(File file) throws IOException, ParsingSourceException.NotASheet, EmptyDatabaseException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return openWorkbook(stream);
        } catch (EmptyFileException empty) {
            throw new EmptyDatabaseException(file.getName());
        } catch (NotOfficeXmlFileException | OfficeXmlFileException | NotOLE2FileException | POIXMLException other) {
            throw new ParsingSourceException.NotASheet(file.getName());
        }
    }
    
    /** @return the workbook that {@code stream} holds, in the format of this reader */
    protected Workbook openWorkbook(FileInputStream stream) throws IOException {
        return new XSSFWorkbook(stream);
    }
    
    private static CaseDatabase read(File file, Sheet sheet)
            throws EmptyDatabaseException, ParsingSourceException.RepeatedVariableNames,
                   ParsingSourceException.ColumnWithoutName, ParsingSourceException.CellPastTheLastColumn {
        refuseCellsPastTheLastColumn(file, sheet);
        int numVariables = numberOfColumnsWithAValue(sheet);
        if (numVariables == 0) {
            throw new EmptyDatabaseException(file.getName());
        }
        // The first row names the variables
        Row titles = sheet.getRow(0);
        List<String> variableNames = new ArrayList<>();
        List<List<State>> variableStates = new ArrayList<>();
        List<Map<String, Integer>> stateIndexes = new ArrayList<>();
        for (int column = 0; column < numVariables; column++) {
            String name = valueOf(titles == null ? null : titles.getCell(column));
            if (name == null) {
                throw new ParsingSourceException.ColumnWithoutName(file.getName(),
                                                                   new CellReference(0, column).formatAsString());
            }
            variableNames.add(name);
            variableStates.add(new ArrayList<>());
            stateIndexes.add(new HashMap<>());
        }
        List<int[]> rowCases = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && hasAValue(row, numVariables)) {
                int[] aCase = new int[numVariables];
                for (int j = 0; j < numVariables; j++) {
                    String value = valueOf(row.getCell(j));
                    aCase[j] = findVariableStateIndexOrCreate(variableStates.get(j), stateIndexes.get(j),
                                                              value == null ? "?" : value);
                }
                rowCases.add(aCase);
            }
        }
        int[][] cases = rowCases.toArray(new int[0][]);
        
        // The network only has the variables, without links
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        LinkedHashMap<String, String> ioNet = new LinkedHashMap<>();
        ioNet.put("Name", file.getName());
        for (int i = 0; i < numVariables; i++) {
            String variableName = variableNames.get(i);
            LinkedHashMap<String, String> infoNode = new LinkedHashMap<>();
            infoNode.put("Title", variableName);
            infoNode.put("NodeType", NodeType.CHANCE.name());
            infoNode.put("TypeOfVariable", VariableType.FINITE_STATES.name());
            infoNode.put("CoordinateX", "0");
            infoNode.put("CoordinateY", "0");
            infoNode.put("UseDefaultStates", "false");
            Variable variable = new Variable(variableName, variableStates.get(i).toArray(new State[1]));
            Node node = probNet.addNode(variable, NodeType.CHANCE);
            node.setAdditionalProperties(infoNode);
            infoNode.put("Name", variableName);
        }
        probNet.setAdditionalProperties(ioNet);
        
        return CaseDatabaseReader.accepted(file, new CaseDatabase(probNet.getVariables(), cases));
    }
    
    /**
     * Refuses a cell past the last column that a sheet of its format can have, XFD in .xlsx and IV in .xls. No
     * spreadsheet program writes one, but a damaged file can carry it, and POI keeps only 16 bits of its column: the
     * cell was read as one column too many, left out, or, when its column wrapped around onto another, read in the
     * place of that column's cell, all without a word (error 450 of the pending list). So the letters of the cell are
     * looked at as the file writes them, not at the column that POI makes of them.
     */
    private static void refuseCellsPastTheLastColumn(File file, Sheet sheet)
            throws ParsingSourceException.CellPastTheLastColumn {
        String lastColumn = sheet.getWorkbook().getSpreadsheetVersion().getLastColumnName();
        for (Row row : sheet) {
            for (Cell cell : row) {
                String reference = cell instanceof XSSFCell asWritten ? asWritten.getReference()
                                                                      : cell.getAddress().formatAsString();
                if (isPast(columnLettersOf(reference), lastColumn)) {
                    throw new ParsingSourceException.CellPastTheLastColumn(file.getName(), reference, lastColumn);
                }
            }
        }
    }

    /** @return the letters that start {@code reference}, in upper case: «XFE» for XFE2 */
    private static String columnLettersOf(String reference) {
        int end = 0;
        while (end < reference.length() && Character.isLetter(reference.charAt(end))) {
            end++;
        }
        return reference.substring(0, end).toUpperCase(Locale.ROOT);
    }

    /**
     * @return whether the column {@code letters} comes after {@code lastColumn}, both in upper case; no letters at
     * all, as POI writes a column it has made negative, counts as past it
     */
    static boolean isPast(String letters, String lastColumn) {
        return letters.isEmpty() || letters.length() > lastColumn.length()
               || letters.length() == lastColumn.length() && letters.compareTo(lastColumn) > 0;
    }

    /** @return the number of columns of {@code sheet} up to the last one that has a value in some row */
    private static int numberOfColumnsWithAValue(Sheet sheet) {
        int columns = 0;
        for (Row row : sheet) {
            for (int column = row.getLastCellNum() - 1; column >= columns; column--) {
                if (valueOf(row.getCell(column)) != null || hasAnError(row.getCell(column))) {
                    columns = column + 1;
                    break;
                }
            }
        }
        return columns;
    }
    
    /** @return whether {@code cell} has an error, such as #N/A, written or given by its formula */
    static boolean hasAnError(Cell cell) {
        return cell != null && typeOfTheValueOf(cell) == CellType.ERROR;
    }
    
    private static CellType typeOfTheValueOf(Cell cell) {
        return cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
    }
    
    /** @return what {@code cell} says, a formula counting as the value it gives; null if it is blank or has an error */
    private static String valueOf(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (typeOfTheValueOf(cell)) {
            case STRING -> cell.getRichStringCellValue().length() == 0 ? null : cell.getRichStringCellValue().getString();
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC -> shown(cell);
            default -> null;
        };
    }
    
    /** @return the index of the state {@code stateName}, which is added to the states and to their index if new */
    static int findVariableStateIndexOrCreate(List<State> variableStates, Map<String, Integer> indexOf,
                                              String stateName) {
        Integer index = indexOf.putIfAbsent(stateName, variableStates.size());
        if (index != null) {
            return index;
        }
        variableStates.add(new State(stateName));
        return variableStates.size() - 1;
    }
    
    /**
     * Refuses a database that a sheet of {@code version} cannot hold, before anything is written.
     *
     * @throws IOException saying the limits of the format, and the formats that have none
     */
    static void checkItFits(CaseDatabase database, SpreadsheetVersion version, String extension) throws IOException {
        int cases = database.getCases().length;
        int variables = database.getVariables().size();
        int maxCases = version.getMaxRows() - 1;
        if (cases > maxCases || variables > version.getMaxColumns()) {
            throw new IOException(String.format(Locale.ROOT, "The dataset has %d cases and %d variables, but a %s file "
                                                + "holds at most %d cases and %d variables. Save it as CSV or as Weka "
                                                + "(.arff).", cases, variables, extension, maxCases,
                                                version.getMaxColumns()));
        }
    }
    
    /** Writes numbers and dates as Excel shows them, with a point for the decimals. */
    private static final DataFormatter AS_EXCEL_SHOWS = new DataFormatter(Locale.ROOT);
    
    /** @return the number or the date of {@code cell} as the sheet shows it, with the format of the cell; null if it shows nothing */
    private static String shown(Cell cell) {
        CellStyle style = cell.getCellStyle();
        if (style.getDataFormat() == 0) {
            return stateNameOf(cell.getNumericCellValue());
        }
        String shown = AS_EXCEL_SHOWS.formatRawCellContents(cell.getNumericCellValue(), style.getDataFormat(),
                                                            style.getDataFormatString()).strip();
        return shown.isEmpty() ? null : shown;
    }
    
    /**
     * @return the name of the state that a numeric cell with {@code value} and without a format of its own is read
     * as: a whole number with all its digits, where Excel would shorten the long ones to powers of ten, and any other
     * as Excel shows it
     */
    static String stateNameOf(double value) {
        if (Double.isFinite(value) && value == Math.rint(value)) {
            return new BigDecimal(value).toBigInteger().toString();
        }
        return AS_EXCEL_SHOWS.formatRawCellContents(value, 0, "General");
    }
    
    /** @return the number to write for the state {@code stateName}, or {@code null} if it would not be read back as it is */
    static Double numberToWrite(String stateName) {
        double value;
        try {
            value = Double.parseDouble(stateName);
        } catch (NumberFormatException e) {
            return null;
        }
        return Double.isFinite(value) && stateNameOf(value).equals(stateName) ? value : null;
    }
    
    /** @return whether some of the first {@code numberOfColumns} cells of {@code row} has a value */
    static boolean hasAValue(Row row, int numberOfColumns) {
        for (int j = 0; j < numberOfColumns; j++) {
            Cell cell = row.getCell(j);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !(cell.getCellType() == CellType.STRING && cell.getRichStringCellValue().length() == 0)) {
                return true;
            }
        }
        return false;
    }
    
    @Override public void save(File file, CaseDatabase database) throws IOException {
        checkItFits(database, SpreadsheetVersion.EXCEL2007, ".xlsx");
        int numCell = 0;
        
        // Closing the workbook deletes the temporary file it keeps its rows in
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            // create a sheet
            SXSSFSheet sheet = wb.createSheet("new sheet");
            // Create a row to put the names of the attributes.
            Row row = sheet.createRow(0);
        
            /* Attributes */
            for (Variable variable : database.getVariables()) {
                String variableName = variable.getName();
                // Create a cell and put a value in it.
                Cell cell = row.createCell(numCell);
                cell.setCellValue(new XSSFRichTextString(variableName));
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
                        cell.setCellValue(new XSSFRichTextString("?"));
                        continue;
                    }
                    Double number = numberToWrite(data);
                    if (number == null) {
                        cell.setCellValue(new XSSFRichTextString(data));
                    } else {
                        cell.setCellValue(number);
                    }
                }
            }
        
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
        }
    }
    
}
