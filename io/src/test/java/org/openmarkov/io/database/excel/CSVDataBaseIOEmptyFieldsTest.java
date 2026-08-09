/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.excel;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for the empty-field handling of {@link CSVDataBaseIO}. A leading or trailing
 * empty field used to be dropped by the {@code Scanner} tokeniser, shifting the whole row by a
 * column. This checks that empty fields keep their column position.
 *
 * @author Manuel Arias
 */
public class CSVDataBaseIOEmptyFieldsTest {

    private CaseDatabase loadCsv(String content) throws IOException, EmptyDatabaseException, ParsingSourceException {
        File file = File.createTempFile("openmarkov-csv-test", ".csv");
        file.deleteOnExit();
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return new CSVDataBaseIO().load(file);
    }

    private String stateAt(CaseDatabase db, int row, int column) {
        Variable variable = db.getVariables().get(column);
        return variable.getStateName(db.getCases()[row][column]);
    }

    @Test public void leadingEmptyFieldDoesNotShiftTheRow() throws Exception {
        // Header A,B,C ; row 0 has an empty A. B and C must stay in their own columns.
        CaseDatabase db = loadCsv("A,B,C\n,male,30\nx,female,40\n");

        List<Variable> variables = db.getVariables();
        assertEquals(3, variables.size(), "three columns expected");
        assertEquals("A", variables.get(0).getName());
        assertEquals("B", variables.get(1).getName());
        assertEquals("C", variables.get(2).getName());

        assertEquals("", stateAt(db, 0, 0), "column A of the first row is the empty field");
        assertEquals("male", stateAt(db, 0, 1), "column B must be 'male', not shifted into A");
        assertEquals("30", stateAt(db, 0, 2), "column C must be '30'");
    }

    @Test public void trailingEmptyFieldDoesNotShiftTheRow() throws Exception {
        // row 0 has an empty C.
        CaseDatabase db = loadCsv("A,B,C\nx,male,\ny,female,40\n");

        assertEquals("x", stateAt(db, 0, 0));
        assertEquals("male", stateAt(db, 0, 1));
        assertEquals("", stateAt(db, 0, 2), "column C of the first row is the empty field");
    }
}
