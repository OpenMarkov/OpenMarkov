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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 * A case database with a column in which every case has a different value, such as an identifier, is read in a
 * time that grows with its number of cases, not with its square (error 421).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ADatabaseWithAColumnOfIdentifiersIsReadQuicklyTest {

    private static final int CASES = 100_000;

    @TempDir Path folder;

    /** A database with an identifier, different in each case, and a class with two values. */
    private static CaseDatabase databaseWithIdentifiers() {
        State[] identifiers = new State[CASES];
        int[][] cases = new int[CASES][2];
        for (int i = 0; i < CASES; i++) {
            // Not in order, so that the readers that sort their states have something to sort
            int identifier = (int) ((i * 7919L) % CASES);
            identifiers[identifier] = new State("id" + (1_000_000 + identifier));
            cases[i][0] = identifier;
            cases[i][1] = i % 2;
        }
        ProbNet network = new ProbNet();
        network.addNode(new Variable("Id", identifiers), org.openmarkov.core.model.network.NodeType.CHANCE);
        network.addNode(new Variable("Class", "no", "yes"), org.openmarkov.core.model.network.NodeType.CHANCE);
        return new CaseDatabase(network.getVariables(), cases);
    }

    private static String valueOf(CaseDatabase database, int aCase, int column) {
        return database.getVariables().get(column).getStates()[database.getCases()[aCase][column]].getName();
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "arff", "xlsx"})
    void itIsReadInSecondsWithTheSameCases(String extension) throws Exception {
        CaseDatabase written = databaseWithIdentifiers();
        Path file = folder.resolve("identifiers." + extension);
        CaseDatabaseManager manager = new CaseDatabaseManager();
        manager.getWriter(extension).save(file.toFile(), written);

        CaseDatabase read = assertTimeout(Duration.ofSeconds(12), () -> manager.getReader(extension).load(file.toFile()));

        assertEquals(CASES, read.getCases().length);
        assertEquals(CASES, read.getVariables().getFirst().getNumStates());
        for (int aCase : List.of(0, 1, 2, CASES / 2, CASES - 1)) {
            assertEquals(valueOf(written, aCase, 0), valueOf(read, aCase, 0));
            assertEquals(valueOf(written, aCase, 1), valueOf(read, aCase, 1));
        }
    }
}
