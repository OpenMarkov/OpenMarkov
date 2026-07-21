/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.elvira;

import antlr.RecognitionException;
import antlr.TokenStreamException;
//import org.antlr.v4.runtime.RecognitionException;
//import antlr.TokenStreamException;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class contains some routines to load a database from a '.dbc' file
 * (the format used by Elvira).
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
//DBC is currently deactivated. It can be re-enabled by uncommenting the following line:
//@CaseDatabaseFormat(extension = "dbc", name = "ElviraDB")
public class ElviraDataBaseIO implements CaseDatabaseReader, CaseDatabaseWriter {
    
    private HashMap<String, Object> ioNet;
    
    /**
     * @param out       {@code PrintWriter}
     * @param variables the variables
     */
    private static void writeVariables(PrintWriter out, List<Variable> variables) {
        // write coment
        out.println("// Variables");
        out.println();
        
        // write variables
        for (Variable variable : variables) {
            if (variable.getName().contains(" ")) {
                out.print("node \"" + variable.getName() + "\"(");
            } else {
                out.print("node " + variable.getName() + "(");
            }
            
            VariableType variableKind = variable.getVariableType();
            switch (variableKind) {
                case FINITE_STATES: {
                    out.print("finite-states");
                    break;
                }
                case NUMERIC: {
                    out.print("continuous");
                    break;
                }
                case DISCRETIZED: {
                    out.print("hybrid");
                    break;
                }
            }
            
            out.println(") {");
            
            // write kind of node
            out.println("kind-of-node = chance;");
            
            // write kind of variable
            variableKind = variable.getVariableType();
            out.print("type-of-variable = ");
            switch (variableKind) {
                case FINITE_STATES: {
                    out.print("finite-states");
                    break;
                }
                case NUMERIC: {
                    out.print("continuous");
                    break;
                }
                case DISCRETIZED: {
                    out.print("hybrid");
                    break;
                }
            }
            out.println(';');
            
            // end of node
            out.println('}');
            out.println();
        }
        
    }
    
    /**
     * @param out   {@code PrintWriter}
     * @param cases {@code int[][]} database cases
     */
    private static void writeRelation(PrintWriter out, int[][] cases) {
        
        out.println();
        
        out.println("relation  {");
        out.println("memory = true;");
        out.println("cases = (");
        
        for (int i = 0; i < cases.length; i++) {
            if (cases[i] != null) {
                out.print("[");
                for (int j = 0; j < cases[i].length - 1; j++)
                    out.print(cases[i][j] + ",");
                out.println(cases[i][cases[i].length - 1] + "]");
            }
        }
        out.println(");");
        out.println('}');
        out.println('}');
        out.println();
    }
    
    /**
     * @param string with an integer or something else.
     *
     * @return {@code true} if {@code string} contains an integer.
     */
    private static boolean isInteger(String string) {
        try {
            int integer = Integer.parseInt(string);
            int numDigits = 0;
            do {
                integer = integer / 10;
                numDigits++;
            } while (integer > 0);
            if (numDigits != string.length()) {
                return false;
            }
        } catch (NumberFormatException n) {
            return false;
        }
        return true;
    }
    
    /**
     * Opens a database file (in '.dbc' format) and creates the associated
     * network
     *
     * @return {@code int[][]} matrix with the cases in the database.
     *
     * @throws IOException if the file does not exist or the file format is not
     *                     correct.
     */
    @Override
    public @NotNull CaseDatabase load(File file) throws IOException, ParsingSourceException.CouldNotParseSourceException {
        ElviraDBParser parser;
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
        try (FileInputStream fileStream = new FileInputStream(file)) {
            parser = new ElviraDBParser(new ElviraDBLexer(fileStream));
            ioNet = parser.database();
            ProbNet probNet = (ProbNet) ioNet.get("ProbNet");
            for (Entry<String, Object> property : ioNet.entrySet())
                properties.put(property.getKey(), property.getValue().
                                                          toString());
            probNet.setAdditionalProperties(properties);
            return new CaseDatabase(probNet.getVariables(), parser.getCases());
        } catch (TokenStreamException | RecognitionException e) {
            throw new ParsingSourceException.CouldNotParseSourceException(e);
        }
    }
    
    @Override public void save(File file, CaseDatabase database) throws IOException {
        save(file, database.getCases(), database.getVariables());
    }
    
    /**
     * @param variables variables
     * @param cases     {@code int[][]}
     *
     * @throws IOException IOException
     */
    public static void save(File file, int[][] cases, List<Variable> variables)
            throws IOException {
        FileWriter writer = new FileWriter(file);
        PrintWriter out = new PrintWriter(writer);
        save(out, variables, cases);
        out.close();
    }
    
    /**
     * This method writes the {@code BayesNet} in a file
     *
     * @param out       {@code PrintWriter}
     * @param variables the variables
     * @param cases     the cases
     */
    private static void save(PrintWriter out, List<Variable> variables, int[][] cases) {
        writePreamble(out, new HashMap<String, String>(), cases.length);
        writeVariables(out, variables);
        writeRelation(out, cases);
    }
    
    /**
     * @param out     {@code PrintWriter}
     * @param infoNet {@code HashMap}
     */
    private static void writePreamble(PrintWriter out, HashMap<String, String> infoNet, int numCases) {
        // preamble comment
        out.println("//	   Network");
        out.println("//	   Elvira format");
        out.println();
        
        // bnet or influence diagram
        //            @SuppressWarnings("unused")
        //            Object object = infoNet.get("BayesNet");
        //            if (object != null) {
        out.print("data-base ");
        //            } else {
        //                    throw new UnknownNetworkTypeException ();
        //            }
        out.print('"');
        String name = infoNet.get("Name");
        if (name != null) {
            out.println(name + '"' + " {");
        } else {
            out.println("NoNameNet" + '"' + " {");
        }
        out.println();
        
        out.println("number-of-cases = " + numCases + ";");
        
        // additionalProperties bnet comment
        out.println("//		 Network Properties");
        out.println();
        
        // kindofgraph = "...";
        String objKindOfGraph = infoNet.get("KindOfGraph");
        if (objKindOfGraph != null) {
            out.println("kindofgraph = " + '"' + objKindOfGraph + '"' + ';');
        }
        
        // title = "...";
        String title = infoNet.get("TitleNet");
        if (title != null) {
            out.print("title = ");
            out.print('"');
            out.print(title);
            out.print('"');
            out.println(";");
        }
        
        // comment = "...";
        String comment = infoNet.get("CommentNet");
        if (comment != null) {
            out.print("comment = ");
            out.print('"');
            out.print(comment);
            out.print('"');
            out.println(";");
        }
        
        // author = "...";
        String author = infoNet.get("AuthorNet");
        if (author != null) {
            out.print("author = ");
            out.print('"');
            out.print(author);
            out.print('"');
            out.println(";");
        }
        
        // whochanged = "...";
        String whochanged = infoNet.get("WhoChanged");
        if (whochanged != null) {
            out.print("whochanged = ");
            out.print('"');
            out.print(whochanged);
            out.print('"');
            out.println(";");
        }
        
        // whenchanged = "...";
        String whenchanged = infoNet.get("WhenChanged");
        if (whenchanged != null) {
            out.print("whenchanged = ");
            out.print('"');
            out.print(whenchanged);
            out.print('"');
            out.println(";");
        }
        
        // visualprecision = "...";
        String objVisualPrecision = infoNet.get("VisualPrecision");
        if (objVisualPrecision != null) {
            out.println("visualprecision = " + '"' + objVisualPrecision + '"' + ';');
        }
        
        // version = ...;
        String objVersion = infoNet.get("Version");
        if (objVersion != null) {
            out.println("version = " + objVersion + ';');
        }
        
        // node default states
        Object objDefaultStates = infoNet.get("DefaultNodeStates");
        if (objDefaultStates != null) {
            String[] defaultStates = (String[]) objDefaultStates;
            out.print("default node states = (");
            for (int i = 0; i < defaultStates.length - 1; i++) {
                out.print('"' + defaultStates[i] + '"' + " , ");
            }
            out.println('"' + defaultStates[defaultStates.length - 1] + '"' + ");");
        }
        out.println();
    }
    
}
