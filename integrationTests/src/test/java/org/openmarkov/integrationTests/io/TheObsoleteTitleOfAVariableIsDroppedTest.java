/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reader drops the obsolete «Title» property of each variable, so it is not among the properties of the nodes and
 * saving the network does not write it back.
 *
 * @author Manuel Arias
 */
class TheObsoleteTitleOfAVariableIsDroppedTest {

    private static final String TITLE = "name=\"Title\"";

    private static URL asia() {
        return Networks.getNetworks().filter(network -> network.getPath().endsWith("/BN-asia.pgmx")).findFirst().orElseThrow();
    }

    @Test void noNodeHasTheTitleAndSavingDoesNotWriteIt() throws Exception {
        assertTrue(Files.readString(Path.of(asia().toURI())).contains(TITLE), "the file has the property");
        ProbNet asia = new PGMXReader().read(asia()).probNet();

        for (Node node : asia.getNodes()) {
            assertFalse(node.getAdditionalProperties().containsKey("Title"), node.getName());
        }
        Path saved = Files.createTempDirectory("title").resolve("BN-asia.pgmx");
        new PGMXWriter_0_2().write(saved.toString(), asia, List.of());
        assertFalse(Files.readString(saved).contains(TITLE));
    }
}
