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
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comment, free text, is saved without the characters that a network file cannot keep; any other text with one,
 * such as the name of a node in a network that did not come from the editor, stops the save with a message.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ATextWithACharacterAFileCannotKeepIsNotSavedAsItIsTest {

    private static ProbNetWriter writer(String format) {
        return format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
    }

    private static ProbNet network(String nodeName) {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable x = new Variable(nodeName, "yes", "no");
        network.addNode(x, NodeType.CHANCE)
               .setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY));
        return network;
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aCommentIsSavedWithoutThem(String format, @TempDir Path folder) throws Exception {
        ProbNet network = network("X");
        Node node = network.getNode("X");
        node.setComment("Line\u000Bbreak");
        network.setComment("Bell\u0007");
        node.getPotential().setComment("Line\u000Bbreak");
        Path file = folder.resolve("network.pgmx");

        writer(format).write(file.toString(), network, List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals("Linebreak", reread.getNode("X").getComment());
        assertEquals("Bell", reread.getComment());
        assertEquals("Linebreak", reread.getNode("X").getPotential().getComment());
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aNameWithOneStopsTheSaveWithAMessage(String format, @TempDir Path folder) {
        Path file = folder.resolve("network.pgmx");

        WriterException.CharacterAFileCannotKeep refused = assertThrows(WriterException.CharacterAFileCannotKeep.class,
                () -> writer(format).write(file.toString(), network("Line\u000Bbreak"), List.of()));

        assertTrue(refused.reason.contains("0x000b"), refused.reason);
        assertFalse(Files.exists(file));
    }
}
