/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.weka;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.core.io.database.plugin.CaseDatabaseFormat;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class contains some routines to load a database from a '.arff' file
 * (the format used by Weka).
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@CaseDatabaseFormat(extension = "arff", name = "WekaDB")
public class ArffDataBaseIO implements CaseDatabaseReader, CaseDatabaseWriter {
    
    private HashMap<String, Object> ioNet;
    
    /**
     * Opens a database file (in '.arff' format) and creates the associated
     * network
     *
     * @return {@code int[][]} matrix with the cases in the database.
     *
     * @throws IOException if the file does not exist or the file format is not
     *                     correct.
     */
    @Override
    public @NotNull CaseDatabase load(File file) throws IOException, ParsingSourceException.CouldNotParseSourceException {
        try (FileInputStream fileStream = new FileInputStream(file)) {
            ArffParser parser = new ArffParser(new ArffLexer(fileStream));
            ioNet = parser.relation();
            ProbNet probNet = (ProbNet) ioNet.get("ProbNet");
            LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
            for (Entry<String, Object> property : ioNet.entrySet()) {
                properties.put(property.getKey(), property.getValue().toString());
            }
            probNet.setAdditionalProperties(properties);
            return new CaseDatabase(probNet.getVariables(), parser.getCases());
        } catch (TokenStreamException | RecognitionException e) {
            throw new ParsingSourceException.CouldNotParseSourceException(e);
        }
    }
    
    /**
     * Saves a case database to a file in ARFF format.
     *
     * @param file     the output file
     * @param database the case database to save
     *
     * @throws IOException if writing to the file fails
     */
    @Override public void save(File file, CaseDatabase database) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter output = new OutputStreamWriter(stream);
        /* Relation name */
        String relationName = file.getAbsolutePath();
        output.write("\n@RELATION \"" + relationName + "\"\n");
        /* Attributes and values */
        State[] states;
        for (Variable variable : database.getVariables()) {
            String nodeName = variable.getName();
            if (nodeName.contains(" "))
                output.write("\n@ATTRIBUTE \"" + nodeName + "\" ");
            else
                output.write("\n@ATTRIBUTE " + nodeName + " ");
            states = variable.getStates();
            /*
             * Before printing the states of the node, we have to know if the
             * node is numeric or not.
             */
            boolean numeric = true;
            for (int i = 0; i < states.length; i++) {
                try {
                    if (!states[i].getName().equals("?")) {
                        Integer.parseInt(states[i].getName());
                    }
                } catch (NumberFormatException e) {
                    numeric = false;
                }
            }
            if (numeric) {
                output.write("numeric {");
            } else {
                output.write("{");
            }
            for (int i = 0; i < states.length; i++) {
                if (states[i].getName().equals("?")) {
                    if (i != 0 && i != states.length - 1) {
                        output.write(",");
                    }
                } else {
                    if (states[i].getName().contains(" ")) {
                        output.write("\"" + states[i].getName() + "\"");
                    } else {
                        output.write(states[i].getName());
                    }
                    if ((i != states.length - 1) && (!states[i + 1].getName().equals("?"))) {
                        output.write(",");
                    }
                }
            }
            output.write("}\n");
        }
        /*
         * Data. If any of the attributes is "String", then we have to get the
         * correct index.
         */
        output.write("\n@DATA\n");
        List<Variable> variables = database.getVariables();
        int[][] cases = database.getCases();
        for (int i = 0; i < cases.length; i++) {
            for (int j = 0; j < cases[i].length; j++) {
                states = variables.get(j).getStates();
                if (states[cases[i][j]].getName().contains(" ")) {
                    output.write("\"" + states[cases[i][j]].getName() + "\"");
                } else {
                    output.write(states[cases[i][j]].getName());
                }
                if (j != cases[i].length - 1) {
                    output.write(",");
                }
            }
            output.write("\n");
        }
        output.close();
    }
}
