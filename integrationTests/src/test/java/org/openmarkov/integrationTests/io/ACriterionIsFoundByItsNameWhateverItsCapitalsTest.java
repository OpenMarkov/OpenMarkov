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
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file whose utility and inference options name the criterion "effectiveness" as " Effectiveness " is read as the
 * network with "effectiveness": criterion names are compared without capitals or outer spaces (error 58).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACriterionIsFoundByItsNameWhateverItsCapitalsTest {

    private static final String NETWORK = "/mid/MID-Chancellor.pgmx";

    private static URL chancellor() {
        return Networks.getNetworks().filter(url -> url.getPath().endsWith(NETWORK)).findFirst().orElseThrow();
    }

    @Test void theUtilityAndTheOptionsGetTheCriterionOfTheNetwork(@TempDir Path folder) throws Exception {
        String original;
        try (var in = chancellor().openStream()) {
            original = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String declaration = "<Criterion name=\"effectiveness\" unit=\"QALY\" />";
        assertTrue(original.contains(declaration));
        String changed = original.replace("<Criterion name=\"effectiveness\" />", "<Criterion name=\" Effectiveness \" />")
                                 .replace("Criterion=\"effectiveness\"", "Criterion=\" Effectiveness \"");
        Path file = folder.resolve("MID-Chancellor.pgmx");
        Files.writeString(file, changed, StandardCharsets.UTF_8);

        ProbNet net = new PGMXReader().read(file.toUri().toURL()).probNet();
        Criterion effectiveness = net.getDecisionCriteria().stream()
                                     .filter(criterion -> criterion.getCriterionName().equals("effectiveness"))
                                     .findFirst().orElseThrow();
        assertSame(effectiveness, net.getVariable("Life years", 0).getDecisionCriterion());

        CEP expected = new VECEAnalysis(new PGMXReader().read(chancellor()).probNet()).getCEP();
        CEP read = new VECEAnalysis(net).getCEP();
        assertEquals(expected.getNumIntervals(), read.getNumIntervals());
        for (int i = 0; i < expected.getNumIntervals(); i++) {
            assertEquals(expected.getCost(i), read.getCost(i), 1E-9 * Math.abs(expected.getCost(i)));
            assertEquals(expected.getEffectiveness(i), read.getEffectiveness(i),
                         1E-9 * Math.abs(expected.getEffectiveness(i)));
        }
    }
}
