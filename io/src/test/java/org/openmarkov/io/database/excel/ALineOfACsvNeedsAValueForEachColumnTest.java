/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A line of a file of comma-separated values with fewer values than columns used to be read as a case that the file
 * does not have, its missing values taken as the first state seen in their column; a blank line, such as the last one
 * of many files, as a case with an empty state; and a line with one value too many stopped the reading without saying
 * which (error 124 of the pending list). Now blank lines are skipped and a line of the wrong width is refused, naming
 * it.
 *
 * @author Manuel Arias
 */
class ALineOfACsvNeedsAValueForEachColumnTest {

    private static File file(String content) throws IOException {
        Path file = Files.createTempDirectory("csv").resolve("cases.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        file.toFile().deleteOnExit();
        return file.toFile();
    }

    private static List<String> states(Variable variable) {
        return Arrays.stream(variable.getStates()).map(State::getName).toList();
    }

    @Test void blankLinesAreNotCases() throws Exception {
        CaseDatabase database = new CSVDataBaseIO().load(file("A,B\nyes,no\n\nno,yes\n\n"));

        assertEquals(2, database.getCases().length);
        assertEquals(List.of("no", "yes"), states(database.getVariables().getFirst()));
    }

    @Test void aLineWithFewerValuesIsRefusedNamingIt() throws IOException {
        File file = file("A,B,C\nyes,no,yes\nno,no\nyes,yes,no\n");

        ParsingSourceException.WrongNumberOfValues refusal = assertThrows(
                ParsingSourceException.WrongNumberOfValues.class, () -> new CSVDataBaseIO().load(file));
        assertEquals(3, refusal.line);
        assertEquals(2, refusal.values);
        assertEquals(3, refusal.columns);
        assertEquals("Line 3 of cases.csv has 2 values, but the first line names 3 columns. Each line must have one "
                     + "value for each column, separated by commas.", refusal.toString());
    }

    @Test void aLineWithMoreValuesIsRefusedNamingIt() throws IOException {
        File file = file("A,B,C\nyes,no,yes\nno,no,yes,no\n");

        ParsingSourceException.WrongNumberOfValues refusal = assertThrows(
                ParsingSourceException.WrongNumberOfValues.class, () -> new CSVDataBaseIO().load(file));
        assertEquals(3, refusal.line);
        assertEquals(4, refusal.values);
    }
}
