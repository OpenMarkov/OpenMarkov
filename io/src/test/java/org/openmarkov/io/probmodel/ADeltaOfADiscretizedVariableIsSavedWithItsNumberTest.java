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
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The delta of a discretized variable keeps a number, not a state: it is saved as that number, as the delta of a
 * numeric variable is, and opened with the same number and interval.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ADeltaOfADiscretizedVariableIsSavedWithItsNumberTest {

    /** «low», from 0 to 50, and «high», from 50 to 100. */
    private static Variable discretized() {
        PartitionedInterval interval = new PartitionedInterval(new double[]{0, 50, 100},
                                                               new boolean[]{true, false, false});
        return new Variable("D", new State[]{new State("low"), new State("high")}, interval, 0.1);
    }

    private static DeltaPotential savedAndOpened(ProbNet network, String format, Path folder) throws Exception {
        ProbNetWriter writer = format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
        Path file = folder.resolve("network.pgmx");
        writer.write(file.toString(), network, List.of());
        return (DeltaPotential) new PGMXReader().read(file.toUri().toURL()).probNet().getNode("D").getPotential();
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aDeltaOfAChanceNode(String format, @TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable d = discretized();
        network.addNode(d, NodeType.CHANCE)
               .setPotential(new DeltaPotential(List.of(d), PotentialRole.CONDITIONAL_PROBABILITY, 70));

        DeltaPotential delta = savedAndOpened(network, format, folder);

        assertEquals(70, delta.getNumericValue());
        assertEquals(1, delta.getStateIndex());
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aDeltaPolicyOfADecision(String format, @TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable d = discretized();
        network.addNode(d, NodeType.DECISION).setPotential(new DeltaPotential(List.of(d), PotentialRole.POLICY, 70));

        DeltaPotential delta = savedAndOpened(network, format, folder);

        assertEquals(70, delta.getNumericValue());
        assertEquals(1, delta.getStateIndex());
    }
}
