package org.openmarkov.inference.DES;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * This class deals with values from a data file. The format of the file is csv
 * The header contains the name of the nodes to be supplied with data
 * Each column contains the values for a node and each row is used in one simulation.
 * The first row contains the name of the nodes whose values are given in the file.
 * If there is less rows than the number of simulations, the algorithm iterates circularly
 * and extract the correspondent values in the current simulation
 *
 *  FIXME --> Currently (v2) the whole file is kept in memory for speed; it will be upgraded with the option of reading the file row by row in order to save memory
 *
 *  @author cmyago
 *  @version 1.1 - 24/04/2021 - Kept in DataFromFileOld
 *  @version 2 - 26/08/2923 - Redone for using csv files and new code structure and algorithm; the whole file is kept in memory for speedd
 */

public class DataFromFile {
    private final String CSV_SEPARATOR = ",";
    /**
     * Data file
     */
     private  Path dataFilePath;

    /**
     * Headers
     */
    private  String[] nodeNames;
     /**
     * There can be states or numbers, therefore everything is taken as a String and cast to double
     */
    private  String[][] data;

    /**
     * Element of dataList used in the current simulation
     */
    private int dataIndex = -1;


    /**
     * Creates a new DataFromFile instance.
     * Reads the values from filename and stares them into dataList
     *
     * @param dataFilePath <code>File</code> where simulation data is stored
     */
    DataFromFile(Path dataFilePath) throws IOException {
        if (dataFilePath==null) return;
        try{
            //for Java version between 7 and 11 it should be using Paths.get
            this.dataFilePath = dataFilePath;
            List<String> lines = Files.readAllLines(this.dataFilePath);

            //Header with the name of the nodes
            nodeNames = lines.get(0).split(CSV_SEPARATOR);

            //Data
            data = new String[lines.size()-1][nodeNames.length];

            for (int i = 0; i < data.length ; i++) {
                data[i] = lines.get(i+1).split(CSV_SEPARATOR);
            }

            //FIXME catch for debugging; to be removed
        } catch (IOException | InvalidPathException |
                 PatternSyntaxException e) { //FIXME try-catch for debugging; to be removed

            JOptionPane.showMessageDialog(null, "desResultsExcelWriter exception");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Reset the treatment of the data. Next index will correspond to the first data row
     */
    public void resetData(){
        dataIndex =-1;
    }

    /**
     * Returns the index of the next data row to be processed
     * @return the index of the next data row to be processed (following the last used row; if the final is reached, <code>1</code> is returned*
     */
    public int nextDataIndex(){
        if (data == null) return -1;
        if ((data.length - 1) == dataIndex) {
            dataIndex = 0;
        } else {
            dataIndex++;
        }
        return dataIndex;
    }

    /**
     * Takes the input data corresponding to
     * @param dataIndex index which the row of data to be used in the simulation
     * @return a {@link HashMap} <nodeName, value> with the row corresponding to <code>dataIndex</code>
     */
    public HashMap<String, String> getInputValues(int dataIndex) {
        if (data ==null) return null;
        HashMap<String, String> simulationData =new HashMap<>();
        for (int i = 0; i < nodeNames.length ; i++) {
            simulationData.put(nodeNames[i],data[dataIndex][i]);
        }
        return (simulationData);
    }


}
