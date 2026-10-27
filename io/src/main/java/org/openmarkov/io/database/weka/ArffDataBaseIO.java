/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.weka;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.ByteOrderMark;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.core.io.database.plugin.CaseDatabaseFormat;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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

    /** The unknown value. */
    private static final String MISSING = "?";
    
    /**
     * Opens a database file (in '.arff' format), as {@link ArffReader} describes.
     *
     * @return the variables and the cases of the database.
     *
     * @throws IOException if the file cannot be read.
     * @throws ParsingSourceException.CouldNotParseSourceException if the file is not a case database in this format,
     *         with the line where it fails.
     * @throws ParsingSourceException.StartsWithAByteOrderMark if the file starts with a byte order mark, which the
     *         format does not allow: Weka refuses it too.
     */
    @Override
    public @NotNull CaseDatabase load(File file)
            throws IOException, ParsingSourceException.CouldNotParseSourceException,
            ParsingSourceException.RepeatedVariableNames, ParsingSourceException.StartsWithAByteOrderMark {
        ByteOrderMark.refuse(file);
        try {
            return CaseDatabaseReader.withDistinctVariableNames(file, ArffReader.read(file.toPath()));
        } catch (ParseException e) {
            throw new ParsingSourceException.CouldNotParseSourceException(e);
        }
    }
    
    /**
     * Saves a case database to a file in ARFF format, which {@link ArffReader} and Weka read back. Every column lists
     * its states between braces; a state {@code ?} is not declared, and its cases are written as {@code ?}, the
     * unknown value. The relation is named after the file, not its folder.
     *
     * @param file     the output file
     * @param database the case database to save
     *
     * @throws IOException if writing to the file fails
     */
    @Override public void save(File file, CaseDatabase database) throws IOException {
        try (Writer output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            String fileName = file.getName();
            int dot = fileName.lastIndexOf('.');
            output.write("@RELATION " + quoted(dot > 0 ? fileName.substring(0, dot) : fileName) + "\n\n");
            List<Variable> variables = database.getVariables();
            for (Variable variable : variables) {
                String states = Arrays.stream(variable.getStates()).map(State::getName)
                                      .filter(name -> !name.equals(MISSING)).map(ArffDataBaseIO::quoted)
                                      .collect(Collectors.joining(","));
                output.write("@ATTRIBUTE " + quoted(variable.getName()) + " {" + states + "}\n");
            }
            output.write("\n@DATA\n");
            for (int[] example : database.getCases()) {
                StringJoiner line = new StringJoiner(",", "", "\n");
                for (int column = 0; column < example.length; column++) {
                    String state = variables.get(column).getStates()[example[column]].getName();
                    line.add(state.equals(MISSING) ? MISSING : quoted(state));
                }
                output.write(line.toString());
            }
        }
    }

    /**
     * Quotes a name or a value that has a character with a meaning in the format, escaping with a backslash the
     * characters that Weka escapes, as its Utils.backQuoteChars does: a line break is not written as such, so it
     * does not cut the case in two.
     */
    private static String quoted(String text) {
        if (!text.isEmpty() && !text.equals(MISSING) && text.chars().noneMatch(
                character -> Character.isWhitespace(character) || ",'\"{}%\\".indexOf(character) >= 0)) {
            return text;
        }
        StringBuilder escaped = new StringBuilder("'");
        for (char character : text.toCharArray()) {
            int escape = ArffReader.UNESCAPED.indexOf(character);
            if (escape >= 0) {
                escaped.append('\\').append(ArffReader.ESCAPED.charAt(escape));
            } else if (character == ArffReader.RECORD_SEPARATOR) {
                escaped.append("\\u001E");
            } else {
                escaped.append(character);
            }
        }
        return escaped.append('\'').toString();
    }
}
