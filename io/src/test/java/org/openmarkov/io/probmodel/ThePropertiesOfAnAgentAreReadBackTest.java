/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Properties;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network with an agent that has properties, saved by OpenMarkov in either version of the format, opens again
 * with those properties (error 440).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ThePropertiesOfAnAgentAreReadBackTest {

    @TempDir Path directory;

    static Stream<ProbNetWriter> writers() {
        return Stream.of(new PGMXWriter_0_2(), new PGMXWriter_1_0());
    }

    private static ProbNet open(URL url) throws Exception {
        ProbNetInfo read = FormatManager.getInstance().getProbNetReader(url).read(url);
        return read.probNet();
    }

    @ParameterizedTest
    @MethodSource("writers")
    void aPropertyOfAnAgentSurvivesSavingAndOpening(ProbNetWriter writer) throws Exception {
        ProbNet network = open(getClass().getClassLoader().getResource("test-decpomdp-manual.pgmx"));
        Properties properties = new Properties();
        properties.put("colour", "red");
        network.getAgents().getFirst().put(properties);
        Path saved = directory.resolve("saved.pgmx");

        writer.write(saved.toString(), network, null);

        assertEquals("red", open(saved.toUri().toURL()).getAgents().getFirst().getAdditionalProperties().get("colour"));
    }
}
