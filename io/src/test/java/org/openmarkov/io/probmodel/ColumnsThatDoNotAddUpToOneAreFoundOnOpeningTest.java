/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ColumnsThatDoNotAddUpToOne;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A file whose columns do not add up to one still opens, and the columns are found to warn about
 * them: a table and the parameters of a canonical model, each summing 0.1.
 *
 * @author Manuel Arias
 */
public class ColumnsThatDoNotAddUpToOneAreFoundOnOpeningTest {

    private ProbNet read(String name) throws Exception {
        return new PGMXReader_0_2().read(getClass().getClassLoader().getResource(name)).probNet();
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void theTwoWrongColumnsAreFound() throws Exception {
        List<String> found = ColumnsThatDoNotAddUpToOne.in(read("columns-not-adding-up-to-one.pgmx"));

        assertEquals(2, found.size(), found.toString());
        assertEquals(1, found.stream().filter(line -> line.startsWith("E: 0.1")).count(), found.toString());
        assertEquals(1, found.stream().filter(line -> line.startsWith("H (A = ")).count(), found.toString());
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aCorrectNetworkGivesNoWarning() throws Exception {
        assertEquals(List.of(), ColumnsThatDoNotAddUpToOne.in(read("test-ici-reading.pgmx")));
    }
}
