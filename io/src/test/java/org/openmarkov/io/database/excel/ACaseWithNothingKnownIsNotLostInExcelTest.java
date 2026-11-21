/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database.excel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A case in which nothing is known, saved in Excel, is still a case when the file is opened: it is not written as
 * a blank row, which the reader skips (error 434).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACaseWithNothingKnownIsNotLostInExcelTest {

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "xls"})
    void theCasesAreReadBackWithTheirUnknownValues(String extension, @TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet();
        network.addNode(new Variable("A", "c", "?", ""), NodeType.CHANCE);
        network.addNode(new Variable("B", "d", "?"), NodeType.CHANCE);
        int[][] cases = {{0, 0}, {1, 1}, {0, 1}, {2, 1}, {0, 0}};
        Path file = folder.resolve("cases." + extension);
        CaseDatabaseManager manager = new CaseDatabaseManager();

        manager.getWriter(extension).save(file.toFile(), new CaseDatabase(network.getVariables(), cases));
        CaseDatabase read = manager.getReader(extension).load(file.toFile());

        List<Variable> variables = read.getVariables();
        List<String> rows = Arrays.stream(read.getCases())
                                  .map(aCase -> variables.get(0).getStates()[aCase[0]].getName() + " "
                                                + variables.get(1).getStates()[aCase[1]].getName())
                                  .toList();
        assertEquals(List.of("c d", "? ?", "c ?", "? ?", "c d"), rows);
    }
}
