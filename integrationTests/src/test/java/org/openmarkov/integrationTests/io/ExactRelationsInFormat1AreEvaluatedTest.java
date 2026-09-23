/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Format 1.0 writes an exact relation as an "Exact" distribution. It must be evaluated as the
 * exact relation it is, scales and discounts included: an influence diagram gives the same
 * expected utility in both formats.
 *
 * @author Manuel Arias
 */
public class ExactRelationsInFormat1AreEvaluatedTest {

    private static double[] utility(ProbNet net) throws Exception {
        return new VEEvaluation(net).getUtility().getValues();
    }

    private static ProbNet read(URL url) throws Exception {
        return new PGMXReader().read(url).probNet();
    }

    /** The influence diagrams in format 0.2 that can be evaluated. */
    private static List<URL> evaluableInfluenceDiagrams() {
        List<URL> urls = new ArrayList<>();
        Networks.getNetworks().filter(u -> u.getPath().matches(".*/networks/id/[^/]+\\.pgmx")).forEach(url -> {
            try {
                utility(read(url));
                urls.add(url);
            } catch (Exception | Error notEvaluable) {
                // another error, not this one
            }
        });
        assertFalse(urls.isEmpty());
        return urls;
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void theCopiesInFormat1GiveTheSameUtility() throws Exception {
        for (URL url02 : evaluableInfluenceDiagrams()) {
            String path10 = url02.getPath().replace("/networks/id/", "/networks/id/1-0/").replace(".pgmx", "-1-0.pgmx");
            if (Files.exists(Path.of(path10))) {
                assertArrayEquals(utility(read(url02)), utility(read(Path.of(path10).toUri().toURL())), 0.0, path10);
            }
        }
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void savingInFormat1KeepsTheUtility() throws Exception {
        for (URL url : evaluableInfluenceDiagrams()) {
            ProbNet net = read(url);
            double[] before = utility(net);
            Path written = Files.createTempFile("format-1-0", ".pgmx");
            new PGMXWriter_1_0().write(written.toString(), read(url), List.of());
            assertArrayEquals(before, utility(read(written.toUri().toURL())), 1E-9, url.getPath());
        }
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void decideTestInFormat1() throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/id/1-0/ID-decide-test-1-0.pgmx")).findFirst().orElseThrow();
        assertEquals(9.3289, utility(read(url))[0], 1E-9);
    }
}
