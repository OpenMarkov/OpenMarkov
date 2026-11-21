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
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Saving a case database as .xlsx leaves none of the temporary files the library keeps the rows in, whether the
 * file is written or not (error 414).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class SavingInExcelLeavesNoTemporaryFilesTest {

    /** The temporary files of the sheets that are being written. */
    private static Set<Path> temporaryFiles() throws IOException {
        Path folder = Path.of(System.getProperty("java.io.tmpdir"), "poifiles");
        if (!Files.isDirectory(folder)) {
            return Set.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(file -> file.getFileName().toString().startsWith("poi-sxssf-sheet"))
                        .collect(Collectors.toSet());
        }
    }

    private static CaseDatabase database() {
        ProbNet network = new ProbNet();
        network.addNode(new Variable("A", "x", "y"), NodeType.CHANCE);
        return new CaseDatabase(network.getVariables(), new int[][]{{0}, {1}, {0}});
    }

    @Test void neitherWhenTheFileIsWrittenNorWhenItCannotBe(@TempDir Path folder) throws Exception {
        Set<Path> before = temporaryFiles();
        ExcelDataBaseIO writer = new ExcelDataBaseIO();

        for (int i = 0; i < 3; i++) {
            writer.save(folder.resolve("cases" + i + ".xlsx").toFile(), database());
        }
        // A folder is not a file that can be written
        assertThrows(IOException.class, () -> writer.save(folder.toFile(), database()));

        Set<Path> left = temporaryFiles();
        left.removeAll(before);
        assertEquals(Set.of(), left);
    }
}
