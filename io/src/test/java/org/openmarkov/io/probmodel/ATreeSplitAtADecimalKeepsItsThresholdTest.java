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
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A tree split by «Split interval» at a number with decimals is saved and opened again with the same thresholds.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ATreeSplitAtADecimalKeepsItsThresholdTest {

    @ParameterizedTest
    @CsvSource({"0.2, 30, 37.8, 42.1", "1.0, 30, 37.8, 42.1", "0.2, 0, 0.7, 1", "1.0, 0, 0.7, 1",
                "0.2, -1, 1e-50, 1", "1.0, -1, 1e-50, 1"})
    void theThresholdsAndTheBranchOfTheSplitPointSurvive(String format, double lower, double split, double upper,
                                                        @TempDir Path folder) throws Exception {
        ProbNetWriter writer = format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
        Path file = folder.resolve("network.pgmx");

        writer.write(file.toString(), network(lower, split, upper), List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();

        List<TreeADDBranch> branches = ((TreeADDPotential) reread.getNode("Fever").getPotential()).getBranches();
        assertEquals(lower, branches.get(0).getLowerBound().getLimit());
        assertEquals(split, branches.get(0).getUpperBound().getLimit());
        assertEquals(split, branches.get(1).getLowerBound().getLimit());
        assertEquals(upper, branches.get(1).getUpperBound().getLimit());
        assertTrue(branches.get(0).isInsideInterval(split), "the split point belongs to the first branch");
        assertTrue(branches.get(1).isInsideInterval(upper), "the end of the domain belongs to the last branch");
    }

    /** Temperature, numeric in [lower, upper], and Fever, whose tree splits Temperature at {@code split}. */
    private static ProbNet network(double lower, double split, double upper) {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable temperature = new Variable("Temperature", true, lower, upper, true, 0.1);
        Variable fever = new Variable("Fever", "no", "yes");
        network.addNode(temperature, NodeType.CHANCE).setPotential(
                new DeltaPotential(List.of(temperature), PotentialRole.CONDITIONAL_PROBABILITY, lower));
        network.addNode(fever, NodeType.CHANCE);
        network.addLink(temperature, fever, true);
        List<Variable> variables = List.of(fever, temperature);
        TreeADDBranch low = new TreeADDBranch(new Threshold(lower, false), new Threshold(split, true), temperature,
                new TablePotential(List.of(fever), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.9, 0.1}),
                variables);
        TreeADDBranch high = new TreeADDBranch(new Threshold(split, true), new Threshold(upper, true), temperature,
                new TablePotential(List.of(fever), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.2, 0.8}),
                variables);
        network.getNode(fever).setPotential(
                new TreeADDPotential(variables, temperature, PotentialRole.CONDITIONAL_PROBABILITY, List.of(low, high)));
        return network;
    }
}
