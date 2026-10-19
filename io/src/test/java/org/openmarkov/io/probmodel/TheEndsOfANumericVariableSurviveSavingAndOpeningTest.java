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
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A numeric variable opens with the ends that its file says, and keeps them when it is saved and opened again. The
 * reader passed to the constructor of the variable whether the lower limit belongs to the left, the opposite of
 * whether it is closed, and the constructor kept it the other way round: the two mistakes cancelled out. The
 * constructor is now right (error 314), so the reader passes what it means.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheEndsOfANumericVariableSurviveSavingAndOpeningTest {

    /** In format 0.2, a lower limit that belongs to the right is closed, and one that belongs to the left is open. */
    private static final String FILE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ProbModelXML formatVersion="0.2.0">
              <ProbNet type="BayesianNetwork">
                <Variables>
                  <Variable name="Closed" type="numeric" role="chance">
                    <Precision>0.1</Precision>
                    <Thresholds>
                      <Threshold value="0.0" belongsTo="right" />
                      <Threshold value="10.0" belongsTo="left" />
                    </Thresholds>
                  </Variable>
                  <Variable name="Open" type="numeric" role="chance">
                    <Precision>0.1</Precision>
                    <Thresholds>
                      <Threshold value="0.0" belongsTo="left" />
                      <Threshold value="10.0" belongsTo="right" />
                    </Thresholds>
                  </Variable>
                </Variables>
              </ProbNet>
            </ProbModelXML>
            """;

    private static ProbNet read(Path file) throws Exception {
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    private static PartitionedInterval domain(ProbNet network, String name) throws Exception {
        return network.getVariable(name).getPartitionedInterval();
    }

    @Test void aFileOpensWithTheEndsItSays(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("network.pgmx");
        Files.writeString(file, FILE);

        ProbNet network = read(file);

        assertTrue(domain(network, "Closed").contains(0));
        assertTrue(domain(network, "Closed").contains(10));
        assertFalse(domain(network, "Open").contains(0));
        assertFalse(domain(network, "Open").contains(10));
    }

    @Test void theEndsSurviveSavingInFormat02(@TempDir Path folder) throws Exception {
        savedAndOpenedKeepTheirEnds(new PGMXWriter_0_2(), folder);
    }

    @Test void theEndsSurviveSavingInFormat10(@TempDir Path folder) throws Exception {
        savedAndOpenedKeepTheirEnds(new PGMXWriter_1_0(), folder);
    }

    private static void savedAndOpenedKeepTheirEnds(ProbNetWriter writer, Path folder) throws Exception {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        for (Variable variable : List.of(new Variable("Closed", true, 0, 10, true, 0.1),
                                         new Variable("Open", false, 0, 10, false, 0.1),
                                         new Variable("Unbounded"))) {
            network.addNode(variable, NodeType.CHANCE).setPotential(
                    new DeltaPotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY, 5));
        }
        Path file = folder.resolve("network.pgmx");

        writer.write(file.toString(), network, List.of());
        ProbNet reread = read(file);

        for (String name : List.of("Closed", "Open", "Unbounded")) {
            assertEquals(domain(network, name).isLeftClosed(), domain(reread, name).isLeftClosed(), name);
            assertEquals(domain(network, name).isRightClosed(), domain(reread, name).isRightClosed(), name);
        }
        assertTrue(domain(reread, "Closed").contains(0));
        assertFalse(domain(reread, "Open").contains(0));
    }
}
