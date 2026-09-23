/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.GLMPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MID-mammography writes pow(Age [1],8) without braces. It is read with the variable in braces,
 * so it can be computed, and saved that way.
 *
 * @author Manuel Arias
 */
public class CovariatesWithoutBracesAreRepairedTest {

    private static final Path NETWORK = Path.of(System.getProperty("user.dir")).getParent()
            .resolve("0_miscellaneous/networks/mid/MID-mammography.pgmx");

    private static VariableExpression firstCovariate(ProbNet net) {
        return Arrays.stream(((GLMPotential) net.getNode("Death (OC) [1]").getPotential()).getCovariates())
                     .filter(covariate -> covariate.asStringExpression().startsWith("pow("))
                     .findFirst().orElseThrow();
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void theNameGetsItsBracesAndSurvivesSaving() throws Exception {
        ProbNet net = new PGMXReader().read(NETWORK.toUri().toURL()).probNet();
        assertEquals("pow({Age [1]},8)", firstCovariate(net).asStringExpression());
        assertEquals(List.of(net.getVariable("Age [1]")), firstCovariate(net).references());

        Path written = Files.createTempFile("mammography", ".pgmx");
        new PGMXWriter_0_2().write(written.toString(), net, List.of());
        assertTrue(Files.readString(written).contains("pow({Age [1]},8)"));
    }
}
