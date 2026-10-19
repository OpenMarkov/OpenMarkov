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
import org.openmarkov.io.database.weka.ArffDataBaseIO;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Weka writes a backslash before a quote, a backslash, {@code %}, a tab, a line break and U+001E in a quoted name or
 * value; they are read as the characters they stand for, not with the backslash (error 248). The file is as
 * weka.core.Utils.quote of Weka 3.8.6 writes it.
 *
 * @author Manuel Arias
 */
class WhatWekaEscapesIsReadAsItIsTest {

    private static final String FILE = String.join("\n",
            "@relation test",
            "@attribute 'Rate\\tin %' {'50\\% \\\"rate\\\"','a\\tb','two\\nlines','r\\u001Es',c}",
            "@data",
            "'50\\% \\\"rate\\\"'",
            "'a\\tb'",
            "'two\\nlines'",
            "'r\\u001Es'",
            "c",
            "");

    @Test
    void theNamesAndTheCasesHaveTheCharactersTheyStandFor() throws Exception {
        Path file = Files.createTempDirectory("weka").resolve("cases.arff");
        Files.writeString(file, FILE, StandardCharsets.UTF_8);
        file.toFile().deleteOnExit();

        CaseDatabase database = new ArffDataBaseIO().load(file.toFile());

        Variable variable = database.getVariables().getFirst();
        assertEquals("Rate\tin %", variable.getName());
        assertEquals(List.of("50% \"rate\"", "a\tb", "two\nlines", "r\u001Es", "c"),
                     Arrays.stream(variable.getStates()).map(State::getName).toList());
        assertArrayEquals(new int[][]{{0}, {1}, {2}, {3}, {4}}, database.getCases());
    }
}
