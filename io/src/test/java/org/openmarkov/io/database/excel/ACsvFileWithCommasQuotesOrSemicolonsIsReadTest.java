/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A file of separated values is read back as it was saved when a name has a comma or a quote; the quotes that other
 * programs put around the values are not part of them; and a file separated by semicolons may write its decimals
 * with a comma.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACsvFileWithCommasQuotesOrSemicolonsIsReadTest {

    @TempDir Path folder;

    private CaseDatabase read(String content) throws Exception {
        Path file = folder.resolve("cases.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return new CSVDataBaseIO().load(file.toFile());
    }

    private static List<String> names(CaseDatabase database) {
        return database.getVariables().stream().map(Variable::getName).toList();
    }

    private static List<String> states(CaseDatabase database, int variable) {
        return Arrays.stream(database.getVariables().get(variable).getStates()).map(State::getName).toList();
    }

    /** The values of each case, by their names. */
    private static List<List<String>> cases(CaseDatabase database) {
        List<List<String>> cases = new ArrayList<>();
        for (int[] aCase : database.getCases()) {
            List<String> values = new ArrayList<>();
            for (int column = 0; column < aCase.length; column++) {
                values.add(database.getVariables().get(column).getStateName(aCase[column]));
            }
            cases.add(values);
        }
        return cases;
    }

    @Test void whatIsSavedWithCommasQuotesAndSemicolonsIsReadBack() throws Exception {
        CaseDatabase saved = new CaseDatabase(
                List.of(new Variable("Edema in face, neck", "[18.0 , 37.5]", "(37.5 , 57.0]"),
                        new Variable("50% \"rate\"", "a;b", "\"quoted\""), new Variable("Sex", "F", "M")),
                new int[][] { { 0, 0, 1 }, { 1, 1, 0 }, { 0, 1, 1 } });
        Path file = folder.resolve("saved.csv");

        new CSVDataBaseIO().save(file.toFile(), saved);
        CaseDatabase read = new CSVDataBaseIO().load(file.toFile());

        assertEquals(names(saved), names(read));
        assertEquals(cases(saved), cases(read));
    }

    @Test void aFileWithoutCommasInItsValuesIsSavedWithoutQuotes() throws Exception {
        CaseDatabase saved = new CaseDatabase(List.of(new Variable("A", "x", "y"), new Variable("B", "u v", "w")),
                                              new int[][] { { 0, 1 }, { 1, 0 } });
        Path file = folder.resolve("saved.csv");

        new CSVDataBaseIO().save(file.toFile(), saved);

        assertEquals("A,B\r\nx,w\r\ny,u v\r\n", Files.readString(file));
    }

    @Test void theQuotesThatRPutsAreNotPartOfTheNames() throws Exception {
        CaseDatabase read = read("\"Id\",\"Sexo\",\"Fuma\"\n\"1\",\"M\",\"si, mucho\"\n\"2\",\"F\",\"no\"\n");

        assertEquals(List.of("Id", "Sexo", "Fuma"), names(read));
        assertEquals(List.of(List.of("1", "M", "si, mucho"), List.of("2", "F", "no")), cases(read));
    }

    @Test void aFileSeparatedBySemicolonsMayWriteItsDecimalsWithAComma() throws Exception {
        CaseDatabase read = read("Sexo;Peso;Nota\nM;70,5;a,b\nF;61;c\nM;-3,25;d\n");

        assertEquals(List.of("Sexo", "Peso", "Nota"), names(read));
        assertEquals(List.of("-3.25", "61", "70.5"), states(read, 1));
        assertEquals(List.of("a,b", "c", "d"), states(read, 2));
    }

    @Test void aFileSeparatedByCommasKeepsItsSemicolons() throws Exception {
        CaseDatabase read = read("A,B\nx;y,1\nz,2\n");

        assertEquals(List.of(List.of("x;y", "1"), List.of("z", "2")), cases(read));
    }

    @Test void aFirstLineWithBothSeparatorsIsRefused() {
        ParsingSourceException.TwoSeparators refusal =
                assertThrows(ParsingSourceException.TwoSeparators.class, () -> read("A,B;C\nx,y;z\n"));

        assertEquals("The first line of cases.csv separates the names of its columns with commas and with "
                     + "semicolons, so the values cannot be told apart. Use only one of them, or write between "
                     + "double quotes the names that have the other.", refusal.toString());
    }
}
