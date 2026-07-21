package org.openmarkov.io.database.excel;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.model.database.CaseDatabase;
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

/**
 * Reader and writer for the legacy Excel {@code .xls} (HSSF) format.
 * Extends {@link ExcelDataBaseIO} to reuse common database I/O behaviour.
 */
@CaseDatabaseFormat(extension = "xls", name = "OldExcel") public class OldExcelDataBaseIO
        extends ExcelDataBaseIO {
    
    @Override
    public @NotNull CaseDatabase load(File file) throws IOException, EmptyDatabaseException {
        /* Each field stores an ArrayList with the StateNames of the
         * corresponding variable */
        
        //create a POIFSFileSystem object to read the data
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        
        try (
                // create a workbook out of the input stream
                HSSFWorkbook wb = new HSSFWorkbook(fs);
        ) {
            // get a reference to the worksheet
            HSSFSheet sheet = wb.getSheetAt(0);
            
            // number of rows and columns
            int rows = sheet.getPhysicalNumberOfRows();
            if (rows == 0) {
                throw new EmptyDatabaseException(file.getName());
            }
            int numVariables = sheet.getRow(0).getPhysicalNumberOfCells();
            int[][] cases = new int[rows - 1][numVariables];
            List<State>[] variableStates = (ArrayList<State>[]) new ArrayList[numVariables];
            
            // the first row contains the names of the variables
            HSSFRow row = sheet.getRow(0);
            List<String> variableNames = new ArrayList<String>();
            for (int i = 0; i < numVariables; i++) {
                variableNames.add(row.getCell(i).getRichStringCellValue().getString());
                variableStates[i] = new ArrayList<State>();
            }
            
            // Gets a int[][] variable that represents the excel file.
            // The first coordinate is the variable index and the second
            // is the row index of the excel datasheet. (IT'S THE OTHER WAY)
            // A cell contains the StateName index corresponding to
            // the String in the excel file cell
            for (int i = 1; i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < numVariables; j++) {
                        HSSFCell cell = row.getCell(j);
                        /*
                         * we can have a string or an integer. If we have an absent
                         * value, cell is null
                         */
                        /* EXTERNAL LIBRARY NOTE:
                         * Apache POI is expected to recover getCellType() on version 4.0.
                         * When it happens, switch the the three if lines for the commented ones.
                         */
                        // if ((cell == null) || (cell.getCellType() == cell.CELL_TYPE_BLANK))
                        String stateName;
                        if ((cell == null) || (cell.getCellType() == CellType.BLANK))
                            stateName = "?";
                            
                            // else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                        else if (cell.getCellType() == CellType.STRING) {
                            if (cell.getRichStringCellValue().length() == 0)
                                stateName = "?";
                            else
                                stateName = cell.getRichStringCellValue().getString();
                            
                            // } else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
                        } else if (cell.getCellType() == CellType.BOOLEAN) {
                            stateName = Boolean.toString(cell.getBooleanCellValue());
                        } else {
                            double doubleValue = cell.getNumericCellValue();
                            stateName = doubleValue == Math.round(doubleValue) ?
                                    Integer.toString((int) doubleValue) :
                                    Double.toString(doubleValue);
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
    
    @Override
    public void save(File file, CaseDatabase database) throws IOException {
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
                    cell.setCellValue(new HSSFRichTextString(data));
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
