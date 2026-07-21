/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.excel;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.EmptyDatabaseException;
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
    public @NotNull CaseDatabase load(File file) throws IOException, EmptyDatabaseException {
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
            
            // the first row contains the names of the variables
            Row row = sheet.getRow(0);
            List<String> variableNames = new ArrayList<>();
            for (int i = 0; i < numVariables; i++) {
                variableNames.add(row.getCell(i).getRichStringCellValue().getString());
                variableStates[i] = new ArrayList<State>();
            }
            
            // Gets a int[][] variable that represents the excel file.
            // The first coordinate is the variable index and the second
            // is the row index of the excel datasheet. (IT'S THE OTHER WAY)
            // A cell contains the StateName index corresponding to
            // the String in the excel file cell
            int[][] cases = new int[rows - 1][numVariables];
            for (int i = 1; i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    row.getPhysicalNumberOfCells();
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
                            double doubleValue = cell.getNumericCellValue();
                            stateName = doubleValue == Math.round(doubleValue)
                                    ? Integer.toString((int) doubleValue)
                                    : Double.toString(doubleValue);
                        }
                        cases[i - 1][j] = findVariableStateIndexOrCreate(variableStates[j], stateName);
                    }
                }
            }
            
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
            
            return new CaseDatabase(probNet.getVariables(), cases);
        }
    }
    
    private int findVariableStateIndexOrCreate(List<State> variableStates, String stateName) {
        for (int index = 0; index < variableStates.size(); index++) {
            if (variableStates.get(index).getName().equals(stateName)) {
                return index;
            }
        }
        variableStates.add(new State(stateName));
        return variableStates.size() - 1;
    }
    
    @Override public void save(File file, CaseDatabase database) throws IOException {
        int numCell = 0;
        
        // create a workbook
        SXSSFWorkbook wb = new SXSSFWorkbook();
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
            row = sheet.createRow((short) i + 1);
            for (int j = 0; j < cases[i].length; j++) {
                // Create a cell and put a value in it.
                Cell cell = row.createCell(j);
                String data = database.getVariables().get(j).getStates()[cases[i][j]].getName();
                if (data.equals("?"))
                    continue;
                double numericData;
                try {
                    numericData = Double.parseDouble(data);
                } catch (NumberFormatException e) {
                    cell.setCellValue(new XSSFRichTextString(data));
                    continue;
                }
                cell.setCellValue(numericData);
            }
        }
        
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }
    
}
