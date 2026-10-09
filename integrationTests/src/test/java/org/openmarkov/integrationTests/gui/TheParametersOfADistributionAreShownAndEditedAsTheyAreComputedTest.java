/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import networks.Networks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.action.AugmentedPotentialValueEdit;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The effectiveness of ID-CEA-minimal in format 1.0 is 0.8 without the therapy and 1.3 with it, and the file gives
 * only those numbers. The panel of the relation shows the expressions of the parameters and edits them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheParametersOfADistributionAreShownAndEditedAsTheyAreComputedTest {

    /** The row and the column of the panel where the effectiveness without the therapy is. */
    private static final int ROW = 1;
    private static final int COLUMN = 1;

    private ProbNet probNet;

    @BeforeEach void read() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/ID-CEA-minimal-1-0.pgmx"))
                          .findFirst()
                          .orElseThrow();
        probNet = new PGMXReader().read(url).probNet();
    }

    private static AugmentedProbTable effectiveness(ProbNet network) {
        return ((UnivariateDistrPotential) network.getNode("Effectiveness").getPotential()).getDistributionTable();
    }

    private static List<String> expressions(ProbNet network) {
        return Arrays.stream(effectiveness(network).getFunctionValues())
                     .map(VariableExpression::asStringExpression)
                     .toList();
    }

    private void write2() throws Exception {
        new AugmentedPotentialValueEdit(probNet.getNode("Effectiveness"),
                                        new VariableExpression(Collections.emptyList(), "2"), ROW, COLUMN,
                                        Collections.emptyList()).executeEdit();
    }

    @Test void theExpressionsReadAreTheNumbersOfTheFile() {
        assertEquals(List.of("0.8", "1.3"), expressions(probNet));
    }

    @Test void aNumberWrittenInACellIsTheNumberThatIsComputed() throws Exception {
        double utilityBefore = new VEEvaluation(probNet).getUtility().getValues()[0];

        write2();

        assertEquals(List.of("2", "1.3"), expressions(probNet));
        assertArrayEquals(new double[]{ 2.0, 1.3 }, effectiveness(probNet).getValues());
        assertEquals(53000.0, utilityBefore);
        assertEquals(60000.0, new VEEvaluation(probNet).getUtility().getValues()[0]);
    }

    @Test void aScaledRelationShowsTheScaledNumbers() {
        UnivariateDistrPotential scaled =
                (UnivariateDistrPotential) probNet.getNode("Effectiveness").getPotential().deepCopy(probNet);
        scaled.scalePotential(10);

        assertArrayEquals(new double[]{ 8.0, 13.0 }, scaled.getDistributionTable().getValues());
        assertEquals(List.of("8", "13"), Arrays.stream(scaled.getDistributionTable().getFunctionValues())
                                               .map(VariableExpression::asStringExpression)
                                               .toList());
    }

    @Test void theFileSavedSaysTheSameInItsTwoCopies() throws Exception {
        write2();
        Path written = Files.createTempDirectory("parameters").resolve("written.pgmx");
        new PGMXWriter_1_0().write(written.toString(), probNet, List.of());

        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        assertEquals(List.of("2", "1.3"), expressions(reread));
        assertArrayEquals(new double[]{ 2.0, 1.3 }, effectiveness(reread).getValues());
    }
}
