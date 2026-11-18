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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The writer trims the blanks at the ends of the state of a «Delta», and the reader finds the state without them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ADeltaOnAStateWithBlanksAtItsEndsIsReadBackTest {

    private static ProbNet network(String state) {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable x = new Variable("X", "no", state);
        network.addNode(x, NodeType.CHANCE)
               .setPotential(new DeltaPotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, x.getStates()[1]));
        return network;
    }

    private static ProbNet savedAndOpened(ProbNet network, String format, Path file) throws Exception {
        ProbNetWriter writer = format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
        writer.write(file.toString(), network, List.of());
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"0.2|'yes '", "1.0|'yes '", "0.2|' yes'", "1.0|' yes'", "0.2|'yes\t'", "1.0|'yes\t'"})
    void theDeltaIsOpenedOnItsStateAndSavedAgain(String format, String state, @TempDir Path folder) throws Exception {
        ProbNet reread = savedAndOpened(network(state), format, folder.resolve("first.pgmx"));

        DeltaPotential delta = (DeltaPotential) reread.getNode("X").getPotential();
        assertEquals(state, delta.getState().getName());
        assertEquals(1, delta.getStateIndex());
        savedAndOpened(reread, format, folder.resolve("second.pgmx"));
    }

    @Test void aDeltaOnAStateThatDoesNotExistIsRefused(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("network.pgmx");
        new PGMXWriter_0_2().write(file.toString(), network("yes"), List.of());
        Files.writeString(file, Files.readString(file).replace("<State>yes</State>", "<State>maybe</State>"));

        PGMXParserException.FoundUnknownState exception = assertThrows(PGMXParserException.FoundUnknownState.class,
                () -> new PGMXReader().read(file.toUri().toURL()));
        assertEquals("maybe", exception.stateName);
    }
}
