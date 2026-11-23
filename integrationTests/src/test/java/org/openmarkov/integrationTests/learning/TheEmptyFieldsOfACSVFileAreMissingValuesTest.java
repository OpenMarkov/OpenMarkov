/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.learning;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.database.excel.CSVDataBaseIO;
import org.openmarkov.learning.core.preprocess.MissingValues;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The options for the missing values of the learning act on the empty fields of a CSV file as on its question marks.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheEmptyFieldsOfACSVFileAreMissingValuesTest {

    @TempDir Path folder;

    /** The cases of the file after applying {@code option} to every variable. */
    private CaseDatabase processed(String content, MissingValues.Option option) throws Exception {
        Path file = folder.resolve("cases.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        CaseDatabase read = new CSVDataBaseIO().load(file.toFile());
        Map<String, MissingValues.Option> options = new LinkedHashMap<>();
        for (Variable variable : read.getVariables()) {
            options.put(variable.getName(), option);
        }
        return MissingValues.process(read, options);
    }

    private static List<String> states(CaseDatabase database, int variable) {
        return Arrays.stream(database.getVariables().get(variable).getStates()).map(State::getName).sorted().toList();
    }

    @Test void theCasesWithEmptyFieldsAreErased() throws Exception {
        CaseDatabase withEmptyFields = processed("Sex,Smoker\nM,yes\nF,\n,no\nM,no\nF,yes\n,\n",
                                                 MissingValues.Option.ELIMINATE);
        CaseDatabase withQuestionMarks = processed("Sex,Smoker\nM,yes\nF,?\n?,no\nM,no\nF,yes\n?,?\n",
                                                   MissingValues.Option.ELIMINATE);

        assertEquals(3, withEmptyFields.getCases().length);
        assertEquals(List.of("F", "M"), states(withEmptyFields, 0));
        assertEquals(List.of("no", "yes"), states(withEmptyFields, 1));
        assertEquals(withQuestionMarks.getCases().length, withEmptyFields.getCases().length);
    }

    @Test void theEmptyFieldsAreFilledInWithTheMode() throws Exception {
        CaseDatabase filled = processed("Sex,Smoker\nM,yes\nF,\nM,no\nM,no\nF,yes\nF,no\n",
                                        MissingValues.Option.IMPUTE_MODE);

        assertEquals(6, filled.getCases().length);
        assertEquals(List.of("no", "yes"), states(filled, 1));
        assertEquals("no", filled.getVariables().get(1).getStateName(filled.getCases()[1][1]));
    }
}
