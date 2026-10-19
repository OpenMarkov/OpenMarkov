/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.desnets;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The file of the trace of a simulation is named after the network without its extension. A new network, not saved
 * yet, is called «Untitled», without a dot, and cutting at the first dot failed before the simulation started; a name
 * with several dots was cut at the first one (error 104).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTraceOfANetworkNotSavedYetHasAFileTest {

    /** The trace is written where the program writes it; what this test writes there is removed afterwards. */
    @Test void theTraceOfAnUntitledNetworkIsWritten() throws Exception {
        Path results = Path.of(MonteCarloOptions.DESNET_RESULTS_DIRECTORY);
        boolean resultsExisted = Files.exists(results);
        Path logs = results.resolve(MonteCarloOptions.TEXTUAL_LOG_DIRECTORY);
        ProbNet net = new ProbNet(DESNetworkType.getUniqueInstance());
        net.setName("Untitled");
        net.getInferenceOptions().getMonteCarloOptions().setTextualLog(true);
        try {
            DESLogTextWriter writer = new DESLogTextWriter(net);
            writer.endLog();

            File[] written = logs.toFile().listFiles((folder, name) -> name.startsWith("Untitled-") && name.endsWith(".log"));
            assertTrue(written != null && written.length > 0);
        } finally {
            if (resultsExisted) {
                File[] written = logs.toFile().listFiles((folder, name) -> name.startsWith("Untitled-"));
                for (File file : written == null ? new File[0] : written) {
                    file.delete();
                }
            } else if (Files.exists(results)) {
                try (Stream<Path> paths = Files.walk(results)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
                }
            }
        }
    }

    @Test void aNameWithoutExtensionIsKeptWhole() {
        assertEquals("Untitled", DESLogTextWriter.withoutExtension("Untitled"));
    }

    @Test void onlyTheExtensionIsTakenAway() {
        assertEquals("a", DESLogTextWriter.withoutExtension("a.pgmx"));
        assertEquals("a.b", DESLogTextWriter.withoutExtension("a.b.pgmx"));
    }
}
