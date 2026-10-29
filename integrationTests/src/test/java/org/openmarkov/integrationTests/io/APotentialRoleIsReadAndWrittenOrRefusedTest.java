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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The reader of format 0.2 refuses a potential role that does not exist, naming the node, and the writer of that format
 * refuses a potential without a role, also naming the node, instead of failing with a NullPointerException (error 114
 * of the pending list). The three networks of the repository that had the role «joinProbability» are saved again.
 *
 * @author Manuel Arias
 */
class APotentialRoleIsReadAndWrittenOrRefusedTest {

    private static URL network(String fileName) {
        return Networks.getNetworks().filter(network -> network.getPath().endsWith("/" + fileName)).findFirst().orElseThrow();
    }

    @Tag(TestSpeed.MEDIUM)
    @ParameterizedTest
    @ValueSource(strings = { "ID-CEA-test-2therapies-new-test.pgmx", "MID-CHAP-Ryan-Griffin.pgmx", "MID-Cochlear.pgmx" })
    void theNetworkIsSavedInFormat02AndOpensAgain(String fileName) throws Exception {
        ProbNet original = new PGMXReader().read(network(fileName)).probNet();
        for (Potential potential : original.getPotentials()) {
            assertNotNull(potential.getPotentialRole(), potential.getVariable(0).getName());
        }
        Path written = Files.createTempDirectory("role").resolve(fileName);
        new PGMXWriter_0_2().write(written.toString(), original, List.of());
        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        assertEquals(original.getNumNodes(), reread.getNumNodes());
    }

    @Test void theExpectedUtilityOfTheNewTestIsTheSameAfterSaving() throws Exception {
        ProbNet original = new PGMXReader().read(network("ID-CEA-test-2therapies-new-test.pgmx")).probNet();
        Path written = Files.createTempDirectory("role").resolve("ID-CEA-test-2therapies-new-test.pgmx");
        new PGMXWriter_0_2().write(written.toString(), original, List.of());
        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        assertEquals(271387.976, new VEEvaluation(original).getUtility().getValues()[0], 1E-3);
        assertEquals(271387.976, new VEEvaluation(reread).getUtility().getValues()[0], 1E-3);
    }

    /** Reading {@code fileName} with the role of the table of {@code variable} changed to one that does not exist. */
    private static PGMXParserException.UnknownPotentialRole refusedWithAnInventedRole(String fileName, String variable)
            throws Exception {
        String text = Files.readString(Path.of(network(fileName).toURI()));
        String table = "<Potential type=\"Table\" role=\"conditionalProbability\">\\s*<Variables>\\s*<Variable name=\"" + variable + "\"";
        String withAnInventedRole = text.replaceFirst(table, "<Potential type=\"Table\" role=\"inventedRole\">\n<Variables>\n<Variable name=\"" + variable + "\"");
        Path file = Files.createTempDirectory("role").resolve(fileName);
        Files.writeString(file, withAnInventedRole);
        return assertThrows(PGMXParserException.UnknownPotentialRole.class, () -> new PGMXReader().read(file.toUri().toURL()));
    }

    @Test void aRoleThatDoesNotExistIsRefusedNamingTheNode() throws Exception {
        var refused = refusedWithAnInventedRole("ID-CEA-test-2therapies-new-test.pgmx", "New test");
        assertEquals("inventedRole", refused.role);
        assertEquals("New test", refused.nodeName);
    }

    @Test void theNodeOfATimeSliceIsNamedWithIt() throws Exception {
        assertEquals("AIDS [0]", refusedWithAnInventedRole("MID-CHAP-Ryan-Griffin.pgmx", "AIDS").nodeName);
    }

    @Test void aPotentialWithoutARoleIsRefusedByFormat02NamingTheNode() throws Exception {
        ProbNet probNet = new PGMXReader().read(network("ID-CEA-test-2therapies-new-test.pgmx")).probNet();
        Node newTest = probNet.getNode("New test");
        newTest.getPotentials().getFirst().setPotentialRole(null);
        Path directory = Files.createTempDirectory("role");
        var refused = assertThrows(WriterException.PotentialWithoutRole.class,
                                   () -> new PGMXWriter_0_2().write(directory.resolve("format02.pgmx").toString(), probNet, List.of()));
        assertEquals("New test", refused.nodeName);
        new PGMXWriter_1_0().write(directory.resolve("format10.pgmx").toString(), probNet, List.of());
    }
}
