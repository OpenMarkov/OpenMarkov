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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.RelevancesThatAreNotNumbers;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * The properties that the user gives to a node come back from a file with the values they were written with, and a
 * relevance that is not a number does not stop the network from being opened.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ThePropertiesOfANodeAreReadAsTheyWereWrittenTest {

    @TempDir Path folder;

    private static ProbNetWriter writer(String format) {
        return format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
    }

    /** The file of a network with one node, «A», that has those properties. */
    private Path saved(String format, Map<String, String> properties) throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable a = new Variable("A", "no", "yes");
        Node node = net.addNode(a, NodeType.CHANCE);
        node.setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY));
        node.setAdditionalProperties(new LinkedHashMap<>(properties));
        Path file = folder.resolve("network.pgmx");
        writer(format).write(file.toString(), net, List.of());
        return file;
    }

    private static Node opened(URL url) throws Exception {
        return new PGMXReader().read(url).probNet().getNode("A");
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void theValuesKeepTheirCapitals(String format) throws Exception {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("Flag", "TRUE");
        properties.put("Other flag", "False");
        properties.put("Note", "Yes");
        properties.put("Count", "007");

        Node node = opened(saved(format, properties).toUri().toURL());

        assertEquals(properties, node.getAdditionalProperties());
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aRelevanceThatIsNotANumberIsLeftOutAndToldOf(String format) throws Exception {
        Path file = saved(format, Map.of("Mark", "7"));
        String text = Files.readString(file);
        String changed = text.replace("name=\"Mark\" value=\"7\"", "name=\"Relevance\" value=\"high\"");
        assertNotEquals(text, changed);
        Files.writeString(file, changed);
        URL url = file.toUri().toURL();

        Node node = opened(url);

        assertEquals(Node.DEFAULT_RELEVANCE, node.getRelevance());
        assertEquals(Map.of(), node.getAdditionalProperties());
        assertEquals(List.of("A: high"), RelevancesThatAreNotNumbers.in(url));
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aRelevanceThatIsANumberGivesNoWarning(String format) throws Exception {
        Path file = saved(format, Map.of("Mark", "7"));
        Files.writeString(file, Files.readString(file).replace("name=\"Mark\" value=\"7\"",
                                                               "name=\"Relevance\" value=\"7\""));
        URL url = file.toUri().toURL();

        assertEquals(7.0, opened(url).getRelevance());
        assertEquals(List.of(), RelevancesThatAreNotNumbers.in(url));
    }
}
