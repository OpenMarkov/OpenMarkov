/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.weka;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads a case database in ARFF, the format of Weka: a header with {@code @relation}, an {@code @attribute} line per
 * column and {@code @data}, followed by one case per line, its values separated by commas. Only nominal columns,
 * those that list their values between braces, can be read. The keywords may be written in any case, a name or a
 * value may be quoted with {@code '} or {@code "}, and a {@code %} that is not quoted starts a comment, which ends
 * with the line, if it starts a word: within an unquoted word, such as {@code 50%}, it is part of it, as the
 * writers of OpenMarkov before October 2026 wrote it and its former reader read it.
 * <p>
 * A {@code ?} stands for an unknown value: the column gets a state {@code ?}, as in the other readers of case
 * databases. Anything else that does not fit, such as a value that its column does not declare or a line with more
 * or fewer values than columns, makes the whole file be refused, with the line where it happens.
 *
 * @author Manuel Arias
 */
final class ArffReader {

    private static final String MISSING = "?";

    /** The characters that Weka writes after a backslash, and those that they stand for, in the same order. */
    static final String ESCAPED = "\\'\"%tnr";
    static final String UNESCAPED = "\\'\"%\t\n\r";
    /** U+001E, the other character that Weka escapes: it writes a backslash, a u and 001E. */
    static final char RECORD_SEPARATOR = '\u001E';

    private final List<String> names = new ArrayList<>();
    private final List<List<String>> states = new ArrayList<>();
    /** How many of the states of each column its header declares; the unknown value of the cases goes after them */
    private final List<Integer> declared = new ArrayList<>();
    private final List<int[]> cases = new ArrayList<>();

    private ArffReader() {
    }

    /**
     * @param file a file in UTF-8, or else in ISO-8859-1, as older files were written
     *
     * @throws ParseException with the number of the line that cannot be read
     */
    static CaseDatabase read(Path file) throws IOException, ParseException {
        ArffReader reader = new ArffReader();
        reader.readLines(text(file).lines().toList());
        List<Variable> variables = new ArrayList<>();
        for (int column = 0; column < reader.names.size(); column++) {
            variables.add(new Variable(reader.names.get(column), reader.states.get(column).toArray(new String[0])));
        }
        return new CaseDatabase(variables, reader.cases.toArray(new int[0][]));
    }

    private static String text(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                                         .onUnmappableCharacter(CodingErrorAction.REPORT)
                                         .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException notUtf8) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }

    private void readLines(List<String> lines) throws ParseException {
        boolean inData = false;
        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String line = lines.get(index).strip();
            if (line.isEmpty() || line.startsWith("%")) {
                continue;
            }
            if (inData) {
                readCase(line, lineNumber);
                continue;
            }
            String keyword = line.split("[\\s%]", 2)[0].toLowerCase(Locale.ROOT);
            switch (keyword) {
                case "@relation" -> { }
                case "@attribute" -> readAttribute(line.substring(keyword.length()).strip(), lineNumber);
                case "@data" -> {
                    if (names.isEmpty()) {
                        throw new ParseException("Line " + lineNumber + ": there is no @attribute before @data", lineNumber);
                    }
                    inData = true;
                }
                default -> throw new ParseException("Line " + lineNumber + ": expected @relation, @attribute or @data, "
                                                    + "found «" + line + "»", lineNumber);
            }
        }
        if (!inData) {
            throw new ParseException("There is no @data line", lines.size());
        }
    }

    /**
     * @param declaration what follows {@code @attribute}: the name and, between braces, the values
     */
    private void readAttribute(String declaration, int lineNumber) throws ParseException {
        int nameEnd = declaration.startsWith("%") ? 0 : endOfName(declaration, lineNumber);
        String name = unquote(declaration.substring(0, nameEnd));
        String type = withoutComment(declaration.substring(nameEnd), lineNumber);
        if (name.isEmpty()) {
            throw new ParseException("Line " + lineNumber + ": the attribute has no name", lineNumber);
        }
        if (!type.startsWith("{") || !type.endsWith("}")) {
            throw new ParseException("Line " + lineNumber + ": the attribute «" + name + "» is of type «" + type
                                     + "»; only attributes that list their values between braces can be read",
                                     lineNumber);
        }
        List<String> values = new ArrayList<>();
        String list = type.substring(1, type.length() - 1);
        // No values: every case of the column is unknown
        for (String field : list.isBlank() ? List.<String>of() : fields(list, lineNumber)) {
            String value = unquote(field);
            // Two commas in a row leave a value out; '' is the empty value, which Weka and the writer write
            if (field.isEmpty() || values.contains(value)) {
                throw new ParseException("Line " + lineNumber + ": the attribute «" + name + "» has an empty or a "
                                         + "repeated value", lineNumber);
            }
            values.add(value);
        }
        names.add(name);
        states.add(values);
        declared.add(values.size());
    }

    /** Where the name of an attribute ends: at its closing quote, or else at the first blank or brace. */
    private static int endOfName(String declaration, int lineNumber) throws ParseException {
        if (declaration.startsWith("'") || declaration.startsWith("\"")) {
            return closingQuote(declaration, 0, lineNumber) + 1;
        }
        int end = 0;
        while (end < declaration.length() && !Character.isWhitespace(declaration.charAt(end))
               && declaration.charAt(end) != '{') {
            end++;
        }
        return end;
    }

    private void readCase(String line, int lineNumber) throws ParseException {
        if (line.startsWith("{")) {
            throw new ParseException("Line " + lineNumber + ": cases in sparse format cannot be read", lineNumber);
        }
        List<String> fields = fields(withoutComment(line, lineNumber), lineNumber);
        if (fields.size() != names.size()) {
            throw new ParseException("Line " + lineNumber + ": " + fields.size() + " values for " + names.size()
                                     + " attributes", lineNumber);
        }
        int[] example = new int[names.size()];
        for (int column = 0; column < example.length; column++) {
            String field = fields.get(column);
            List<String> values = states.get(column);
            if (field.equals(MISSING)) {
                if (!values.contains(MISSING)) {
                    values.add(MISSING);
                }
                example[column] = values.indexOf(MISSING);
                continue;
            }
            String value = unquote(field);
            // A quoted ? is a value of that name, which the header must declare
            List<String> declaredValues = values.subList(0, declared.get(column));
            example[column] = declaredValues.indexOf(value);
            if (example[column] == -1) {
                throw new ParseException("Line " + lineNumber + ": the value «" + value + "» is not one of those of the "
                                         + "attribute «" + names.get(column) + "»: " + declaredValues, lineNumber);
            }
        }
        cases.add(example);
    }

    /**
     * Splits a list of values at the commas that are not quoted, and strips the blanks around each value. A quoted
     * value keeps its quotes, so that a quoted {@code ?} is not taken for an unknown value.
     */
    private static List<String> fields(String list, int lineNumber) throws ParseException {
        List<String> fields = new ArrayList<>();
        int start = 0;
        for (int position = 0; position <= list.length(); position++) {
            if (position == list.length() || list.charAt(position) == ',') {
                fields.add(list.substring(start, position).strip());
                start = position + 1;
            } else if (list.charAt(position) == '\'' || list.charAt(position) == '"') {
                position = closingQuote(list, position, lineNumber);
            }
        }
        return fields;
    }

    /** {@code text}, stripped, up to the {@code %} that starts a comment, if any (see the class comment). */
    private static String withoutComment(String text, int lineNumber) throws ParseException {
        for (int position = 0; position < text.length(); position++) {
            char character = text.charAt(position);
            // A quote before it can only be one that closes, since the loop skips what is quoted
            if (character == '%' && (position == 0 || " \t,{}'\"".indexOf(text.charAt(position - 1)) >= 0)) {
                return text.substring(0, position).strip();
            }
            if (character == '\'' || character == '"') {
                position = closingQuote(text, position, lineNumber);
            }
        }
        return text.strip();
    }

    /** The position of the quote that closes the one at {@code open}; a backslash escapes a quote or itself. */
    private static int closingQuote(String text, int open, int lineNumber) throws ParseException {
        char quote = text.charAt(open);
        for (int position = open + 1; position < text.length(); position++) {
            char character = text.charAt(position);
            if (character == '\\' && position + 1 < text.length()
                && (text.charAt(position + 1) == quote || text.charAt(position + 1) == '\\')) {
                position++;
            } else if (character == quote) {
                return position;
            }
        }
        throw new ParseException("Line " + lineNumber + ": a quote is not closed", lineNumber);
    }

    /** The text of a quoted field, with the escapes that Weka writes undone, as its Utils.unbackQuoteChars does. */
    private static String unquote(String field) {
        if (field.length() < 2 || (field.charAt(0) != '\'' && field.charAt(0) != '"')
            || field.charAt(field.length() - 1) != field.charAt(0)) {
            return field;
        }
        StringBuilder text = new StringBuilder();
        for (int position = 1; position < field.length() - 1; position++) {
            char character = field.charAt(position);
            if (character == '\\' && position + 2 < field.length()) {
                int escape = ESCAPED.indexOf(field.charAt(position + 1));
                if (escape >= 0) {
                    position++;
                    character = UNESCAPED.charAt(escape);
                } else if (field.startsWith("u001E", position + 1) && position + 6 < field.length()) {
                    position += 5;
                    character = RECORD_SEPARATOR;
                }
            }
            text.append(character);
        }
        return text.toString();
    }
}
