/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.excel;


import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
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
     * Opens a database from a '.xlsx' file and creates a ProbNet
     * building the variables and states dinamically while reading, and
     * returning the cases on the database.
     *
     * @return {@code int[][]} matrix with the cases in the database.
     *
     * @throws IOException if the file does not exist or the file format is not
     *                     correct.
     */
    @Override @SuppressWarnings({"unchecked", "static-access"})
    public @NotNull CaseDatabase load(File file)
            throws IOException, EmptyDatabaseException, ParsingSourceException.RepeatedVariableNames {
        //create a FileInputStream object to read the data
        FileInputStream fs = new FileInputStream(file);
        try (
                // create a workbook out of the input stream
                XSSFWorkbook wb = new XSSFWorkbook(fs);
        ) {
            // get a reference to the worksheet
            XSSFSheet sheet = wb.getSheetAt(0);
            
            // number of rows and columns
            int rows = sheet.getPhysicalNumberOfRows();
            if (rows == 0) {
                throw new EmptyDatabaseException(file.getName());
            }
            int numVariables = sheet.getRow(0).getPhysicalNumberOfCells();
            List<State>[] variableStates = (ArrayList<State>[]) new ArrayList[numVariables];
            List<Map<String, Integer>> stateIndexes = new ArrayList<>();
            
            // the first row contains the names of the variables
            Row row = sheet.getRow(0);
            List<String> variableNames = new ArrayList<>();
            for (int i = 0; i < numVariables; i++) {
                variableNames.add(row.getCell(i).getRichStringCellValue().getString());
                variableStates[i] = new ArrayList<State>();
                stateIndexes.add(new HashMap<>());
            }
            
            // Gets a int[][] variable that represents the excel file.
            // The first coordinate is the variable index and the second
            // is the row index of the excel datasheet. (IT'S THE OTHER WAY)
            // A cell contains the StateName index corresponding to
            // the String in the excel file cell
            List<int[]> rowCases = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                if (row != null && hasAValue(row, numVariables)) {
                    int[] aCase = new int[numVariables];
                    for (int j = 0; j < numVariables; j++) {
                        
                        Cell cell = row.getCell(j);
                        /*
                         * we can have a string or an integer. If we have an absent
                         * value, cell is null
                         */
                        /* EXTERNAL LIBRARY NOTE:
                         * Apache POI is expected to recover getCellType() on version 4.0.
                         * When it happens, switch the the three if lines for the commented ones.
                         *  TODO Manolo> When migrating to Java 17, I have made changes that should be tested.
                         */
                        String stateName;
                        if ((cell == null) || (cell.getCellType() == CellType.BLANK)) {
                            stateName = "?";
                        } else if (cell.getCellType() == CellType.STRING) {
                            if (cell.getRichStringCellValue().length() == 0) {
                                stateName = "?";
                            } else {
                                stateName = cell.getRichStringCellValue().getString();
                            }
                            
                        } else if (cell.getCellType() == CellType.BOOLEAN) {
                            
                            stateName = Boolean.toString(cell.getBooleanCellValue());
                        } else {
                            stateName = stateNameOf(cell.getNumericCellValue());
                        }
                        aCase[j] = findVariableStateIndexOrCreate(variableStates[j], stateIndexes.get(j), stateName);
                    }
                    rowCases.add(aCase);
                }
            }
            int[][] cases = rowCases.toArray(new int[0][]);
            
            /* Creation of the probNet. The probNet only contains variables but
             * no links. */
            ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
            LinkedHashMap<String, String> ioNet = new LinkedHashMap<String, String>();
            ioNet.put("Name", file.getName());
            int i = 0;
            for (String variableName : variableNames) {
                LinkedHashMap<String, String> infoNode = new LinkedHashMap<String, String>();
                infoNode.put("Title", variableName);
                infoNode.put("NodeType", NodeType.CHANCE.name());
                infoNode.put("TypeOfVariable", VariableType.FINITE_STATES.name());
                infoNode.put("CoordinateX", "0");
                infoNode.put("CoordinateY", "0");
                infoNode.put("UseDefaultStates", "false");
                State[] aux = new State[1];
                Variable variable = new Variable(variableName, variableStates[i].toArray(aux));
                Node node = probNet.addNode(variable, NodeType.CHANCE);
                node.setAdditionalProperties(infoNode);
                infoNode.put("Name", variableName);
                i++;
            }
            probNet.setAdditionalProperties(ioNet);
            
            return CaseDatabaseReader.accepted(file, new CaseDatabase(probNet.getVariables(), cases));
        }
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
    
    /** @return the name of the state that a numeric cell with {@code value} is read as */
    static String stateNameOf(double value) {
        return value == Math.round(value) ? Integer.toString((int) value) : Double.toString(value);
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
