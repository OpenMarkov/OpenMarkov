/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A CSV or Weka file saved in Windows-1252, as Excel and other programs of Windows save text, is read whole and with
 * its characters, as the same file in UTF-8 is.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACaseFileSavedInWindowsIsReadWithItsCharactersTest {

    private static CaseDatabase read(String text, String charset, String extension, Path folder) throws Exception {
        Path file = folder.resolve("cases." + extension);
        Files.write(file, text.getBytes(Charset.forName(charset)));
        return new CaseDatabaseManager().getReader(extension).load(file.toFile());
    }

    private static List<String> states(CaseDatabase database, String variable) {
        Variable found = database.getVariables().stream().filter(v -> v.getName().equals(variable)).findFirst().get();
        return Arrays.stream(found.getStates()).map(State::getName).toList();
    }

    @ParameterizedTest @ValueSource(strings = {"windows-1252", "UTF-8"})
    void aSmallCsvWithAnEnyeIsReadWhole(String charset, @TempDir Path folder) throws Exception {
        StringBuilder text = new StringBuilder("Ciudad,Fuma\n");
        for (int i = 0; i < 9; i++) {
            text.append(i % 2 == 0 ? "Soria,si\n" : "Madrid,no\n");
        }
        text.append("Logroño,no\n");

        CaseDatabase database = read(text.toString(), charset, "csv", folder);

        assertEquals(10, database.getCases().length);
        assertTrue(states(database, "Ciudad").contains("Logroño"));
    }

    @ParameterizedTest @ValueSource(strings = {"windows-1252", "UTF-8"})
    void aLongCsvWithAnAccentInRow300IsReadWhole(String charset, @TempDir Path folder) throws Exception {
        StringBuilder text = new StringBuilder("Paciente,Sexo,Fuma\n");
        for (int i = 1; i <= 10_000; i++) {
            String sex = i == 300 ? "varón" : (i % 3 == 0 ? "mujer" : "hombre");
            text.append(i).append(',').append(sex).append(',').append(i % 2 == 0 ? "si" : "no").append('\n');
        }

        CaseDatabase database = read(text.toString(), charset, "csv", folder);

        assertEquals(10_000, database.getCases().length);
        assertEquals(List.of("no", "si"), states(database, "Fuma"));
        assertTrue(states(database, "Sexo").contains("varón"));
    }

    @ParameterizedTest @ValueSource(strings = {"windows-1252", "UTF-8"})
    void aWekaFileKeepsItsApostrophesEurosAndDashes(String charset, @TempDir Path folder) throws Exception {
        String text = """
                @relation r
                @attribute Nota {'Señor’s','precio 10€','2–3','camión'}
                @data
                'Señor’s'
                'precio 10€'
                '2–3'
                'camión'
                """;

        CaseDatabase database = read(text, charset, "arff", folder);

        assertEquals(List.of("Señor’s", "precio 10€", "2–3", "camión"), states(database, "Nota"));
    }
}
