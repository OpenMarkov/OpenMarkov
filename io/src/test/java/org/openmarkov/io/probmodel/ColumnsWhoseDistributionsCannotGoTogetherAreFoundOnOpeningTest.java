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
import org.openmarkov.core.model.network.potential.DistributionsOfAColumn;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file whose column for y0 has 0.2 with no distribution next to a Beta still opens, and the column is found to
 * warn about it. In the other file that cell goes with a range, which it can.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ColumnsWhoseDistributionsCannotGoTogetherAreFoundOnOpeningTest {

    private ProbNet read(String name) throws Exception {
        return new PGMXReader().read(getClass().getClassLoader().getResource(name)).probNet();
    }

    @Test void theWrongColumnIsFound() throws Exception {
        List<String> found = DistributionsOfAColumn.wrongIn(read("column-with-distributions-that-cannot-go-together.pgmx"));

        assertEquals(1, found.size(), found.toString());
        assertTrue(found.getFirst().startsWith("X (Y = y0): State \"x0\" has the fixed value 0.2 next to the Beta"),
                   found.toString());
    }

    @Test void aFileWithColumnsThatCanBeSampledGivesNoWarning() throws Exception {
        assertEquals(List.of(), DistributionsOfAColumn.wrongIn(read("column-with-uncertainty-in-some-cells.pgmx")));
    }
}
