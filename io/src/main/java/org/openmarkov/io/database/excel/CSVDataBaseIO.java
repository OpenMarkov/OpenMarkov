/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.excel;

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
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class contains some routines to load a database from a '.csv' file
 * (Comma Separated Values). The first line of the file has the names of
 * the variables.
 *
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@CaseDatabaseFormat(extension = "csv", name = "CSV") public class CSVDataBaseIO
        implements CaseDatabaseReader, CaseDatabaseWriter {
    
    /**
     * If the value of this variable is 0 all the Strings are kept as reading,
     * if the value is 1, all the Strings are changed to lower case and if the
     * value is 2, all the Strings are changed to upper case.<p>
     * This variable is useful to avoid transcription problems when the data
     * source has been entered by persons.
     */
    private static final int translateToLowerUpperCase = 0;
    
    /**
     * Returns a bayesian network with a list of variables:
     * {@code ProbNet} with a
     * {@code openmarkov.networks.constraints.compound.BNConstraint}.
     */
    /**
     * Builds a Bayesian network containing only variables (no links) from the given names and states.
     *
     * @param fileName             the file name to use as the network name
     * @param variablesNames       the names of the variables
     * @param variablesStatesNames the state names for each variable
     *
     * @return a {@link ProbNet} containing the variables
     */
    public static ProbNet getBayesNetVariables(String fileName, List<String> variablesNames,
                                               List<List<String>> variablesStatesNames) {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        LinkedHashMap<String, String> ioNet = new LinkedHashMap<>();
        ioNet.put("Name", fileName);
        for (int i = 0; i < variablesNames.size(); ++i) {
            String variableName = variablesNames.get(i);
            List<String> variableStateNames = variablesStatesNames.get(i);
            LinkedHashMap<String, String> infoNode = new LinkedHashMap<String, String>();
            State[] states = new State[variableStateNames.size()];
            
            for (int j = 0; j < variableStateNames.size(); ++j) {
                states[j] = new State(variableStateNames.get(j));
            }
            infoNode.put("Title", variableName);
            infoNode.put("CoordinateX", "0");
            infoNode.put("CoordinateY", "0");
            infoNode.put("UseDefaultStates", "false");
            Variable variable = new Variable(variableName, states);
            Node node = probNet.addNode(variable, NodeType.CHANCE);
            node.setAdditionalProperties(infoNode);
            infoNode.put("Name", variableName);
        }
        probNet.setAdditionalProperties(ioNet);
        
        return probNet;
    }
    
    /**
     * Opens a database from a '.csv' file and creates a ProbNet
     * building the variables and states dinamically while reading, and
     * returning the cases on the database.
     *
     * @return {@code CaseDatabase} with the cases in the database.
     */
    @Override public @NotNull CaseDatabase load(File file) throws EmptyDatabaseException, FileNotFoundException {
        // TODO Solve: This method assumes that 1) ALL the states of every variable are always included in the dataset, 2) All the variables are discrete.
        // First row contains all the attributes names and the attributes number
        Scanner scanner = new Scanner(file);
        // first use a Scanner to get each line
        if (!scanner.hasNextLine()) {
            scanner.close();
            throw new EmptyDatabaseException(file.getName());
        }
        List<String> variablesNames = getVariableNames(scanner.nextLine());
        int numColumns = variablesNames.size();
        List<List<String>> variablesStatesNames = new ArrayList<>(numColumns);
        for (int i = 0; i < numColumns; i++) {
            variablesStatesNames.add(new ArrayList<String>());
        }
        List<int[]> data = new ArrayList<int[]>(); // Read data
        List<Map<String, Integer>> variablesStates = new ArrayList<Map<String, Integer>>(); // Variables states
        for (int i = 0; i < numColumns; i++) {
            variablesStates.add(new HashMap<String, Integer>());
        }
        int numRows = 0;
        while (scanner.hasNextLine()) {
            data.add(getDataLine(scanner.nextLine(), variablesStates, variablesStatesNames));
            numRows++;
        }
        scanner.close(); // ensure the underlying stream is always closed
        // Sort states alphabetically and remap case indices
        List<int[]> remappings = new ArrayList<>(numColumns);
        for (int i = 0; i < numColumns; i++) {
            List<String> stateNames = variablesStatesNames.get(i);
            List<String> sortedNames = new ArrayList<>(stateNames);
            Collections.sort(sortedNames);
            int[] remap = new int[stateNames.size()];
            for (int oldIdx = 0; oldIdx < stateNames.size(); oldIdx++) {
                remap[oldIdx] = sortedNames.indexOf(stateNames.get(oldIdx));
            }
            remappings.add(remap);
            stateNames.clear();
            stateNames.addAll(sortedNames);
        }
        for (int[] row : data) {
            for (int j = 0; j < numColumns; j++) {
                row[j] = remappings.get(j)[row[j]];
            }
        }
        /*
         * Creation of the probNet. The probNet only contains variables but no
         * links.
         */
        ProbNet probNet = getBayesNetVariables(file.getName(), variablesNames, variablesStatesNames);
        int[][] cases = new int[numRows][numColumns];
        for (int i = 0; i < numRows; i++) {
            int[] row = data.get(i);
            for (int j = 0; j < numColumns; j++) {
                cases[i][j] = row[j];
            }
        }
        return new CaseDatabase(probNet.getVariables(), cases);
    }
    
    @Override public void save(File file, CaseDatabase database) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            // Resource to write text file
//			FileOutputStream fileOut = new FileOutputStream(fileName);
//			writer = new OutputStreamWriter(fileOut);
            // Write the first line with the variables names
            List<Variable> variables = database.getVariables();
            int numVariablesMinus1 = variables.size() - 1;
            for (int i = 0; i < numVariablesMinus1; i++) {
                writer.write(variables.get(i).getName() + ",");
            }
            writer.write(variables.get(numVariablesMinus1).getName() + "\r\n");
            for (int row = 0; row < database.getCases().length; row++) {
                for (int column = 0; column < numVariablesMinus1; column++) {
                    Variable variable = variables.get(column);
                    writer.write(variable.getStateName(database.getCases()[row][column]) + ",");
                }
                Variable variable = variables.get(numVariablesMinus1);
                writer.write(variable.getStateName(database.getCases()[row][numVariablesMinus1]) + "\r\n");
            }
        }
    }
    
    /**
     * This method writes a CSV database file
     *
     * @param file    {@code File} path to the resulting .csv file
     * @param probNet {@code ProbNet} contains the list of variables
     * @param cases   {@code ArrayList} of {@code int[]} examples to
     *                save in the database
     */
    public void save(File file, ProbNet probNet, ArrayList<int[]> cases) throws IOException {
        int numCases = cases.size();
        int[][] newCases = new int[numCases][];
        for (int i = 0; i < newCases.length; i++) {
            newCases[i] = cases.get(i);
        }
        save(file, new CaseDatabase(probNet.getVariables(), newCases));
    }
    
    /**
     * Reads the first line of a 'csv' file where is stored the variable list.
     *
     * @param firstLine {@code String}
     *
     * @return Variables list. {@code ArrayList} of {@code String}
     */
    private static ArrayList<String> getVariableNames(String firstLine) {
        ArrayList<String> variableNames = new ArrayList<String>();
        // Use a second Scanner to parse the content of each line
        Scanner scanner = new Scanner(firstLine);
        scanner.useDelimiter("[,;]");
        while (scanner.hasNext()) {
            variableNames.add(scanner.next());
        }
        scanner.close();
        return variableNames;
    }
    
    /**
     * Reads a data line of a 'csv' file.
     *
     * @param line                 Data to be parsed. {@code String}
     * @param variablesStates      Current states of variables that can be updated
     * @param variablesStatesNames per-variable ordered list of state names, updated as new states are found
     *                             if appears new ones. {@code ArrayList} of {@code HashSet} of
     *                             {@code String}.
     *
     * @return A data line, each cell contains the state number readed.
     * {@code Integer[]}.
     */
    private static int[] getDataLine(String line, List<Map<String, Integer>> variablesStates,
                                     List<List<String>> variablesStatesNames) {
        int[] statesLines = new int[variablesStates.size()];
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter("[,;]");
        int numVariable = 0;
        while (scanner.hasNext()) {
            String stateVariable = scanner.next();
            switch (translateToLowerUpperCase) {
                case 1:
                    stateVariable = stateVariable.toLowerCase();
                    break;
                case 2:
                    stateVariable = stateVariable.toUpperCase();
                    break;
                default:
                    break;
            }
            Map<String, Integer> variableStates = variablesStates.get(numVariable);
            Integer stateNumber = variableStates.get(stateVariable);
            if (stateNumber == null) {
                stateNumber = variableStates.size();
                variableStates.put(stateVariable, stateNumber);
                variablesStatesNames.get(numVariable).add(stateVariable);
            }
            statesLines[numVariable] = stateNumber;
            numVariable++;
        }
        scanner.close();
        return statesLines;
    }
    
}
