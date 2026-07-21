package org.openmarkov.inference.DES;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class deals with values from a data file. The format of the file is and Excel file .xlsx.
 * Each column contains the values for a node and each row is used in one simulation.
 * The first row contains the name of the nodes whose values are given in the file.
 * If there is less rows than the number of simulations, the algorithm iterates circularly
 * and extract the correspondent values in the current simulation
 * Currently, openMarkov is using Apache POI 3.17, in this version getCellType() is deprecated, but version 4 uses it again and  deprecated getCellTypeEnum()
 *
 * @author cmyago
 * @version 1.1 - 24/04/2021
 */
public class DataFromFileOld {
    /**
     * Hashmaps used instead of arrays because in the future data may not be only numeric
     * This list has one hashmap for each row of the file
     */
    List<HashMap> dataList = new ArrayList<HashMap>();

    /**
     * Element of dataList used in the current simulation
     */
    int dataListIndex = -1;


    /**
     * Excel file with the simulation data
     */
    XSSFWorkbook dataWorkbook = null;

    /**
     * DataSheet with the simulation Data
     */
    XSSFSheet dataSheet = null;


    /**
     * Creates a DataFromFile object, reads the values from filename and stares them into dataList
     *
     * @param dataFile File where simulation data is stored
     */
    DataFromFileOld(File dataFile) {
        boolean empty = false;
        try {
            FileInputStream fileInputStreaminputStream = new FileInputStream(dataFile);
            dataWorkbook = new XSSFWorkbook(dataFile);
            dataSheet = dataWorkbook.getSheetAt(0);

            Iterator<Row> rowIterator = dataSheet.iterator();
            Row firstRow = rowIterator.next();
            // Traversing over each row of XLSX file
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                HashMap<String, Double> rowHashMap = new HashMap<>();
                Iterator<Cell> cellIterator = row.cellIterator();
                Iterator<Cell> keyCellIterator = firstRow.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell dataCell = cellIterator.next();
                    Cell keyCell = keyCellIterator.next();
//                    Currently, openMarkov is using Apache POI 3.17, in this version getCellType() is deprecated, but version 4 uses it again and  deprecated getCellTypeEnum
                    CellType cellType = dataCell.getCellType();
                    switch (cellType) {
                        case STRING:
//                            System.out.print(dataCell.getStringCellValue() + "\t");
                            break;
                        case NUMERIC:
                            rowHashMap.put(keyCell.getStringCellValue(), dataCell.getNumericCellValue());

//                            System.out.print(dataCell.getNumericCellValue() + "\t");
                            break;
                        case BOOLEAN:
//                            System.out.print(dataCell.getBooleanCellValue() + "\t");
                            break;
                        default:
//                            System.out.print("Default \t");
                    }

                }
                if (!rowHashMap.isEmpty()) {
                    dataList.add(rowHashMap);
                }
            }
            dataWorkbook.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "desResultsExcelWriter exception");
            e.printStackTrace();
        }
    }

    /**
     * Returns a HashMap with the input values used in the current simulation
     *
     * @return the input values used in the current simulation
     */
    public HashMap<String, Double> getInputValues() {

        if ((dataList.size() - 1) == dataListIndex) {
            dataListIndex = 0;
        } else {
            dataListIndex++;
        }
        HashMap<String, Double> thisSimulationData = dataList.get(dataListIndex);
//        System.out.print(Arrays.toString(thisSimulationData.values().toArray()));
        return (thisSimulationData);
    }


}
