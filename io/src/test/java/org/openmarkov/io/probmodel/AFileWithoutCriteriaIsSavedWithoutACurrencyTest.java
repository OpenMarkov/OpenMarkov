/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A network whose file keeps no decision criteria is opened with the default criterion alone, without a currency,
 * and saved that way: the file does not depend on the machine that saves it (error 299 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class AFileWithoutCriteriaIsSavedWithoutACurrencyTest {

    private static final String NETWORK = "test-ici-reading.pgmx";

    private ProbNet open() throws Exception {
        return new PGMXReader().read(getClass().getClassLoader().getResource(NETWORK)).probNet();
    }

    private static List<String> criteria(ProbNet probNet) {
        return probNet.getDecisionCriteria().stream().map(Criterion::getCriterionName).toList();
    }

    static Stream<ProbNetWriter> writers() {
        return Stream.of(new PGMXWriter_0_2(), new PGMXWriter_1_0());
    }

    @Test public void itIsOpenedWithTheDefaultCriterionAndNoCurrency() throws Exception {
        ProbNet network = open();

        assertEquals(List.of("---"), criteria(network));
        assertFalse(network.getAdditionalProperties().containsKey("Currency"));
    }

    @ParameterizedTest
    @MethodSource("writers")
    public void itIsSavedWithTheDefaultCriterionAndNoCurrency(ProbNetWriter writer, @TempDir Path folder)
            throws Exception {
        Path file = folder.resolve("network.pgmx");
        writer.write(file.toString(), open(), List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(List.of("---"), criteria(reread));
        assertFalse(reread.getAdditionalProperties().containsKey("Currency"));
    }
}
