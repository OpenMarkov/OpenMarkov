/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.ParserException;

/**
 * Reads a Elvira file and generates tokens.
 *
 * @author Manuel Arias
 */
public class ElviraScanner {
    // Attributes
    private static final int TT_LEFTP = 40;
    private static final int TT_RIGHTP = 41;
    private static final int TT_COMMA = 44;
    private static final int TT_SEMICOLON = 59;
    private static final int TT_ASSIGNMENT = 61;
    private static final int TT_LEFTSB = 91;
    private static final int TT_RIGHTSB = 93;
    private static final int TT_LEFTCB = 123;
    private static final int TT_RIGHTCB = 125;
    /**
     * Singleton pattern.
     */
    private static ElviraScanner elviraScanner = null;
    private StreamTokenizer streamTokenizer;
    private boolean readNextToken = true;
    private String fileName;
    
    // Constructor
    private ElviraScanner() {
        fileName = null;
    }
    
    // Methods
    /**
     * Returns a new scanner instance (slight modification of the singleton pattern).
     *
     * @return a fresh {@code ElviraScanner} instance
     */
    public static ElviraScanner getUniqueInstance() {
        // Without worrying about whether there is a scanner, it creates a new
        // one (slight modification of singleton pattern).
        elviraScanner = new ElviraScanner();
        return elviraScanner;
    }
    
    /**
     * Initializes the scanner with the given file name and input stream,
     * configuring the stream tokenizer for Elvira syntax.
     *
     * @param fileName        the name of the file being scanned
     * @param fileInputStream the input stream to read from
     */
    public void initializeScanner(String fileName, InputStream fileInputStream) {
        this.fileName = fileName;
        streamTokenizer = new StreamTokenizer(
                new InputStreamReader(fileInputStream, Charset.forName("windows-1252")));
        streamTokenizer.resetSyntax();
        streamTokenizer.wordChars('a', 'z');
        streamTokenizer.wordChars('A', 'Z');
        streamTokenizer.wordChars('0', '9');
        streamTokenizer.wordChars('#', '&');
        streamTokenizer.wordChars('-', '.');
        streamTokenizer.wordChars('?', '@');
        streamTokenizer.wordChars(128 + 32, 255);
        streamTokenizer.whitespaceChars(0, ' ');
        streamTokenizer.commentChar('/');
        streamTokenizer.quoteChar('"');
        streamTokenizer.wordChars('_', '_');
        streamTokenizer.wordChars(':', ':');
        streamTokenizer.wordChars('\'', '\'');
        streamTokenizer.quoteChar('"'); // For strings
        streamTokenizer.slashSlashComments(true); // Consider // as comments
    }
    
    /**
     * Reads and returns the next token from the input stream.
     *
     * @return the next {@code ElviraToken}
     * @throws ParserException if a parsing error occurs
     * @throws IOException     if an I/O error occurs
     */
    public ElviraToken getNextToken() throws ParserException, IOException {
        ReservedWord reservedWord = readNextToken(streamTokenizer);
        if (reservedWord == null) {
            if (streamTokenizer.ttype == TT_RIGHTCB) {
                return new ElviraToken(TokenType.RESERVED, ReservedWord.RIGHTCB);
            }
            String identifier = readIdentifier(streamTokenizer, false);
            if (identifier != null) {
                streamTokenizer.nextToken();
                readNextToken = false;
                return new ElviraToken(TokenType.IDENTIFIER, identifier);
            }
            readNextToken = true;
            return getNextToken();
        }
        
        // Is a reserved word
        switch (reservedWord) {
            case IDIAGRAM, BNET, IDIAGRAMSV, NODE -> {
                String name = readIdentifier(streamTokenizer, true);
                return new ElviraToken(TokenType.RESERVED, reservedWord, name);
            }
            case KIND_OF_GRAPH, WHOCHANGED, WHENCHANGED, UNIT, COMMENT, PURPOSE, KIND_OF_RELATION, NAME_OF_RELATION -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                String content = readIdentifier(streamTokenizer, true);
                return new ElviraToken(TokenType.RESERVED, reservedWord, content);
            }
            case VISUALPRECISION -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                String visualPrecision = readIdentifier(streamTokenizer, true);
                double precision = Double.parseDouble(visualPrecision);
                return new ElviraToken(TokenType.RESERVED, reservedWord, precision);
            }
            case PRECISION, POSX, POSY, NUM_STATES -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                int intValue = readInt(streamTokenizer, true);
                return new ElviraToken(TokenType.RESERVED, reservedWord, intValue);
            }
            case VERSION, RELEVANCE -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                double doubleValue = readDouble(streamTokenizer, true);
                return new ElviraToken(TokenType.RESERVED, reservedWord, doubleValue);
            }
            case DETERMINISTIC, ACTIVE -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                String booleanString = readIdentifier(streamTokenizer, true);
                boolean booleanValue = Boolean.parseBoolean(booleanString);
                return new ElviraToken(TokenType.RESERVED, reservedWord, booleanValue);
            }
            case MAX -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                double max = readInt(streamTokenizer, true);
                return new ElviraToken(TokenType.RESERVED, reservedWord, max);
            }
            case MIN -> {
                readToken(streamTokenizer);
                if (streamTokenizer.ttype == TT_ASSIGNMENT) {
                    double min = readInt(streamTokenizer, true);
                    return new ElviraToken(TokenType.RESERVED, reservedWord, min);
                }
                if (streamTokenizer.ttype == TT_LEFTP) {
                    return getCanonicalToken(reservedWord);
                }
                throw new ParserException.MissingToken("MIN");
            }
            case DEFAULT -> {
                checkToken(streamTokenizer, ReservedWord.NODE);
                checkToken(streamTokenizer, ReservedWord.STATES);
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                readTokenType(streamTokenizer, TT_LEFTP);
                List<String> defaultStates = new ArrayList<String>();
                do {
                    defaultStates.add(readIdentifier(streamTokenizer, false));
                    if (streamTokenizer.ttype == TT_COMMA) {
                        streamTokenizer.nextToken();
                    }
                } while (streamTokenizer.ttype != TT_RIGHTP);
                readNextToken = true;
                readTokenType(streamTokenizer, TT_SEMICOLON);
                String[] states = new String[defaultStates.size()];
                int i = 0;
                for (String stateName : defaultStates) {
                    states[i++] = stateName;
                }
                return new ElviraToken(TokenType.RESERVED, reservedWord, states);
            }
            case FINITE_STATES, CONTINUOUS -> {
                streamTokenizer.nextToken(); // Skips ')' or ';'
                if (streamTokenizer.ttype == TT_RIGHTP) {
                    streamTokenizer.nextToken(); // Skips '{'
                }
                return new ElviraToken(TokenType.RESERVED, reservedWord);
            }
            case KIND_OF_NODE, TYPE_OF_VARIABLE, VALUES -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                return new ElviraToken(TokenType.RESERVED, reservedWord);
            }
            case CHANCE, DECISION, UTILITY, UTILITYCOMBINATION -> {
                readTokenType(streamTokenizer, TT_SEMICOLON);
                return new ElviraToken(TokenType.RESERVED, reservedWord);
            }
            case STATES -> {
                readTokenType(streamTokenizer, TT_ASSIGNMENT);
                readTokenType(streamTokenizer, TT_LEFTP);
                List<String> states = new ArrayList<String>();
                do {
                    String stateName = readIdentifier(streamTokenizer, false);
                    states.add(stateName);
                    if (streamTokenizer.ttype == TT_COMMA) {
                        streamTokenizer.nextToken();
                    }
                } while (streamTokenizer.ttype != TT_RIGHTP);
                String[] stateList = new String[states.size()];
                for (int i = 0; i < stateList.length; i++) {
                    stateList[i] = states.get(i);
                }
                readNextToken = true;
                readTokenType(streamTokenizer, TT_SEMICOLON);
                return new ElviraToken(TokenType.RESERVED, reservedWord, stateList);
            }
            case LINK -> {
                String node1Name = readIdentifier(streamTokenizer, false);
                String node2Name = readIdentifier(streamTokenizer, false);
                readTokenType(streamTokenizer, TT_SEMICOLON);
                return new ElviraToken(TokenType.RESERVED, reservedWord, node1Name, node2Name);
            }
            case RELATION -> {
                ArrayList<String> variablesNames = new ArrayList<String>();
                streamTokenizer.nextToken();
                readNextToken = false;
                while (streamTokenizer.ttype != TT_LEFTCB) {
                    variablesNames.add(readIdentifier(streamTokenizer, false));
                }
                readNextToken = true; // Skips LEFTB: {
                String[] variables = new String[variablesNames.size()];
                for (int i = 0; i < variables.length; i++) {
                    variables[i] = variablesNames.get(i);
                }
                return new ElviraToken(TokenType.RESERVED, reservedWord, variables);
            }
            case TABLE, GENERALIZED_TABLE -> {
                readTokenType(streamTokenizer, TT_LEFTP);
                ArrayList<Double> arrayTable = new ArrayList<Double>();
                streamTokenizer.nextToken();
                int i = 0;
                while (streamTokenizer.ttype != TT_RIGHTP) {
                    String tokenString = streamTokenizer.sval;
                    if ((tokenString != null) && (tokenString.toUpperCase().startsWith("E")) && (i > 0)) {
                        String newDoubleValue = arrayTable.get(i - 1).toString() + tokenString;
                        arrayTable.set(i - 1, Double.parseDouble(newDoubleValue));
                    } else if (streamTokenizer.ttype == TT_LEFTSB) {
                        while (streamTokenizer.ttype != TT_RIGHTSB) {
                            streamTokenizer.nextToken();
                        }
                        streamTokenizer.nextToken();
                    } else {
                        i++;
                        arrayTable.add(Double.parseDouble(streamTokenizer.sval));
                    }
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == TT_COMMA) { // Comma separated
                        streamTokenizer.nextToken();
                    }
                }
                readTokenType(streamTokenizer, TT_SEMICOLON);
                double[] table = new double[arrayTable.size()];
                i = 0;
                for (Double doubleValue : arrayTable) {
                    table[i++] = doubleValue;
                }
                return new ElviraToken(TokenType.RESERVED, reservedWord, table);
            }
            case OR, CAUSAL_MAX, GENERALIZED_MAX, AND -> {
                readTokenType(streamTokenizer, TT_LEFTP);
                return getCanonicalToken(reservedWord);
            }
            default -> {
            }
        }
        return new ElviraToken(TokenType.RESERVED, reservedWord);
    }
    
    /**
     * @param reservedWord {@code ReservedWord}
     *
     * @return ElviraToken
     */
    private ElviraToken getCanonicalToken(ReservedWord reservedWord) throws IOException, ParserException.MismatchedToken {
        ArrayList<String> relationsNames = new ArrayList<String>();
        String relationName;
        while (streamTokenizer.ttype != TT_RIGHTP) {
            relationName = readIdentifier(streamTokenizer, true);
            relationsNames.add(relationName);
        }
        readTokenType(streamTokenizer, TT_SEMICOLON);
        int numRelations = relationsNames.size();
        String[] relationsNames2 = new String[numRelations];
        for (int i = 0; i < numRelations; i++) {
            relationsNames2[i] = relationsNames.get(i);
        }
        return new ElviraToken(TokenType.RESERVED, reservedWord, relationsNames2);
    }
    
    /**
     * Reads next token and tries to identify it as a reserved word. Considere
     * the case that the token can have underline characters.
     *
     * @param streamTokenizer {@code StreamTokenizer}
     *
     * @return The reserved word or {@code null}. {@code ReservedWord}
     *
     * @throws IOException launch by {@code StreamTokenizer} if an I/O
     *                     error occurs.
     */
    private ReservedWord readNextToken(StreamTokenizer streamTokenizer) throws IOException {
        if (readNextToken) {
            streamTokenizer.nextToken();
        }
        readNextToken = true;
        if (streamTokenizer.sval != null) {
            ReservedWord reservedWord = ReservedWordTokens.getReservedWord(streamTokenizer.sval);
            if (reservedWord == null) {
                String identifier = streamTokenizer.sval;
                streamTokenizer.nextToken();
                readNextToken = false;
                return ReservedWordTokens.getReservedWord(identifier);
            }
            return reservedWord;
        }
        return null;
    }
    
    /**
     * Reads a token and ensures that it has a given type
     *
     * @param streamTokenizer {@code StreamTokenizer}
     * @param ttype           {@code StreamTokenizer} descriptor. {@code int}
     *
     * @throws IOException     launch by {@code StreamTokenizer} if an I/O
     *                         error occurs.
     * @throws ParserException parser exception
     */
    private void readTokenType(StreamTokenizer streamTokenizer, int ttype) throws IOException, ParserException.MismatchedToken {
        readToken(streamTokenizer);
        if (streamTokenizer.ttype != ttype) {
            throw new ParserException.MismatchedToken(ttype, streamTokenizer.ttype);
        }
    }
    
    /**
     * Reads a token.
     *
     * @param streamTokenizer {@code StreamTokenizer}
     *
     * @throws IOException if an I/O error occurs
     */
    private void readToken(StreamTokenizer streamTokenizer) throws IOException {
        if (readNextToken) {
            streamTokenizer.nextToken();
        }
        readNextToken = true;
    }
    
    /**
     * Reads a token and checks that it is equals to an expected token.
     *
     * @param streamTokenizer      {@code StreamTokenizer}
     * @param reservedWordExpected {@code ReservedWord}
     *
     * @throws ParserException if parser occurs
     */
    private static void checkToken(StreamTokenizer streamTokenizer, ReservedWord reservedWordExpected)
            throws IOException, ParserException.MismatchedToken {
        streamTokenizer.nextToken();
        ReservedWord reservedWordReaded = ReservedWordTokens.getReservedWord(streamTokenizer.sval);
        if (reservedWordReaded != reservedWordExpected) {
            throw new ParserException.MismatchedToken(reservedWordExpected.toString(), streamTokenizer.sval);
        }
    }
    
    /**
     * Reads an identifier string with quotations or not.
     *
     * @param streamTokenizer {@code StreamTokenizer}
     * @param skipNextToken   Put this parameter to {@code true} when the
     *                        identifiers are separates with one symbol (i.e. commas).
     *                        {@code boolean}
     *
     * @return {@code String}
     *
     * @throws IOException IOException
     */
    private String readIdentifier(StreamTokenizer streamTokenizer, boolean skipNextToken) throws IOException {
        String identifier = null;
        if (readNextToken) {
            streamTokenizer.nextToken();
        }
        if (streamTokenizer != null) {
            identifier = streamTokenizer.sval;
            streamTokenizer.nextToken();
            readNextToken = skipNextToken;
        }
        return identifier;
    }
    
    /**
     * Reads a double with quotations or not.
     *
     * @param streamTokenizer {@code StreamTokenizer}
     * @param skipNextToken   {@code boolean}
     *
     * @return Readed double. {@code double}
     *
     * @throws IOException if an I/O error occurs
     */
    private static double readDouble(StreamTokenizer streamTokenizer, boolean skipNextToken) throws IOException {
        streamTokenizer.nextToken();
        double value = Double.parseDouble(streamTokenizer.sval);
        if (skipNextToken) {
            streamTokenizer.nextToken();
        }
        return value;
    }
    
    /**
     * Reads an integer with quotations or not.
     *
     * @param streamTokenizer {@code StreamTokenizer}
     * @param skipNextToken   {@code boolean}
     *
     * @return Readed int. {@code int}
     *
     * @throws IOException if an I/O error occurs
     */
    private static int readInt(StreamTokenizer streamTokenizer, boolean skipNextToken) throws IOException {
        streamTokenizer.nextToken();
        int value = Double.valueOf(streamTokenizer.sval).intValue();
        if (skipNextToken) {
            streamTokenizer.nextToken();
        }
        return value;
    }
    
    /**
     * Returns the current line number being read by the scanner.
     *
     * @return the current line number
     */
    public int lineno() {
        return streamTokenizer.lineno();
    }
    
    public String toString() {
        if (fileName == null) {
            return "No file";
        }
        return "File: " + fileName + ".\n" + "ReadNextToken: " + readNextToken + ". " + "Token: "
                + streamTokenizer.toString();
    }
}
