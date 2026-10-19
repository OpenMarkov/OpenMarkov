/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.io.database.excel.CSVDataBaseIO;
import org.openmarkov.io.database.weka.ArffDataBaseIO;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A case database saved in the format of Weka is read back with the same variables, states and cases (errors 122
 * and 123).
 *
 * @author Manuel Arias
 */
class AWekaDatabaseSavedByOpenMarkovIsReadBackTest {

    private static Path folder;

    private static File save(CaseDatabase database) throws Exception {
        folder = Files.createTempDirectory("carpeta ñ+á");
        File file = folder.resolve("casos.arff").toFile();
        file.deleteOnExit();
        new ArffDataBaseIO().save(file, database);
        return file;
    }

    private static CaseDatabase saveAndRead(CaseDatabase database) throws Exception {
        return new ArffDataBaseIO().load(save(database));
    }

    private static List<String> states(Variable variable) {
        return Arrays.stream(variable.getStates()).map(State::getName).toList();
    }

    private static void assertSame(CaseDatabase expected, CaseDatabase actual) {
        assertEquals(expected.getVariables().stream().map(Variable::getName).toList(),
                     actual.getVariables().stream().map(Variable::getName).toList());
        for (int column = 0; column < expected.getVariables().size(); column++) {
            assertEquals(states(expected.getVariables().get(column)), states(actual.getVariables().get(column)));
        }
        assertArrayEquals(expected.getCases(), actual.getCases());
    }

    @Test
    void statesNamedWithNumbersAndAnUnknownValueAreReadBack() throws Exception {
        CaseDatabase saved = new CaseDatabase(List.of(new Variable("N", "0", "1", "2"), new Variable("S", "a", "b", "?")),
                                              new int[][]{{0, 0}, {2, 2}, {1, 1}});

        assertSame(saved, saveAndRead(saved));
    }

    @Test
    void namesWithTheCharactersOfTheFormatAreReadBack() throws Exception {
        CaseDatabase saved = new CaseDatabase(
                List.of(new Variable("Smoker, daily?", "yes, a lot", "it's 'no'", "{none}"),
                        new Variable("50% \"rate\"", "C:\\data\\", "%low", "high\\'")),
                new int[][]{{0, 1}, {1, 2}, {2, 0}});

        assertSame(saved, saveAndRead(saved));
    }

    /** They are written as Weka writes them, so a line break does not cut the case in two (error 248). */
    @Test
    void namesWithTabsLineBreaksAndTheOtherCharactersThatWekaEscapesAreReadBack() throws Exception {
        CaseDatabase saved = new CaseDatabase(
                List.of(new Variable("Rate\tin %", "50% \"rate\"", "a\tb", "two\nlines", "carriage\rreturn", "r\u001Es")),
                new int[][]{{0}, {1}, {2}, {3}, {4}});

        assertSame(saved, saveAndRead(saved));
        String text = Files.readString(save(saved).toPath(), StandardCharsets.UTF_8);
        assertTrue(text.contains("{'50\\% \\\"rate\\\"','a\\tb','two\\nlines','carriage\\rreturn','r\\u001Es'}"), text);
    }

    /** The empty state that a blank cell of a CSV file gives is written '' and read back (error 246). */
    @Test
    void theEmptyStateOfABlankCellIsReadBack() throws Exception {
        Path csv = Files.createTempDirectory("csv").resolve("blank.csv");
        Files.writeString(csv, "A,B,C\nx,,w\ny,z,v\n", StandardCharsets.UTF_8);
        csv.toFile().deleteOnExit();
        CaseDatabase read = new CSVDataBaseIO().load(csv.toFile());

        assertTrue(states(read.getVariables().get(1)).contains(""), states(read.getVariables().get(1)).toString());
        assertSame(read, saveAndRead(read));
    }

    @Test
    void aColumnWhoseCasesAreAllUnknownIsReadBack() throws Exception {
        CaseDatabase saved = new CaseDatabase(List.of(new Variable("X", "a", "b"), new Variable("Y", "?")),
                                              new int[][]{{0, 0}, {1, 0}});

        assertSame(saved, saveAndRead(saved));
    }

    /** Neither Weka nor OpenMarkov read a column that is numeric and lists its values at the same time. */
    @Test
    void theFileHasNeitherNumericColumnsNorTheFolder() throws Exception {
        String text = Files.readString(save(new CaseDatabase(List.of(new Variable("N", "0", "1")),
                                                             new int[][]{{1}})).toPath(), StandardCharsets.UTF_8);

        assertFalse(text.toLowerCase().contains("numeric"), text);
        assertFalse(text.contains(folder.toString()), text);
        assertFalse(text.contains("carpeta"), text);
    }
}
