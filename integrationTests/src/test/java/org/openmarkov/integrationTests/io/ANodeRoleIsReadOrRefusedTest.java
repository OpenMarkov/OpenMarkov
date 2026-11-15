/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The reader of .pgmx files refuses a variable whose role is not a node type, or that has no role, naming the node,
 * in formats 0.2 and 1.0.
 *
 * @author Manuel Arias
 */
class ANodeRoleIsReadOrRefusedTest {

    private static URL network(String fileName) {
        return Networks.getNetworks().filter(network -> network.getPath().endsWith("/" + fileName)).findFirst().orElseThrow();
    }

    /** {@code fileName} with the role of the variable {@code variable} replaced by {@code role}, or removed if it is null. */
    private static URL withRole(String fileName, String variable, String role) throws Exception {
        String text = Files.readString(Path.of(network(fileName).toURI()));
        Pattern definition = Pattern.compile("(<Variable name=\"" + Pattern.quote(variable) + "\"[^>]*?) role=\"[^\"]*\"");
        String changed = definition.matcher(text).replaceFirst(
                "$1" + (role == null ? "" : Matcher.quoteReplacement(" role=\"" + role + "\"")));
        assertNotEquals(text, changed, variable + " has no role in " + fileName);
        Path file = Files.createTempDirectory("nodeRole").resolve(fileName);
        Files.writeString(file, changed);
        return file.toUri().toURL();
    }

    private static PGMXParserException.UnknownNodeRole refusedWithRole(String fileName, String variable, String role)
            throws Exception {
        URL file = withRole(fileName, variable, role);
        return assertThrows(PGMXParserException.UnknownNodeRole.class, () -> new PGMXReader().read(file));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invented", "Chance", "CHANCE", "" })
    void aRoleThatIsNotANodeTypeIsRefusedNamingTheNodeAndTheRole(String role) throws Exception {
        var refused = refusedWithRole("BN-asia.pgmx", "X-ray", role);
        assertEquals(role, refused.role);
        assertEquals("X-ray", refused.nodeName);
    }

    @Test void aVariableWithoutARoleIsRefusedNamingTheNode() throws Exception {
        URL file = withRole("BN-asia.pgmx", "X-ray", null);
        var refused = assertThrows(PGMXParserException.NodeWithoutRole.class, () -> new PGMXReader().read(file));
        assertEquals("X-ray", refused.nodeName);
    }

    @Test void aRoleThatIsNotANodeTypeIsRefusedInFormat10() throws Exception {
        var refused = refusedWithRole("ID-decide-test-1-0.pgmx", "Therapy", "invented");
        assertEquals("Therapy", refused.nodeName);
    }

    @Test void theNodeOfATimeSliceIsNamedWithIt() throws Exception {
        assertEquals("Age [0]", refusedWithRole("MID-CHAP-Ryan-Griffin.pgmx", "Age", "invented").nodeName);
    }

    @ParameterizedTest
    @ValueSource(strings = { "BN-asia.pgmx", "ID-decide-test-1-0.pgmx", "MID-CHAP-Ryan-Griffin.pgmx" })
    void theUnchangedNetworkIsRead(String fileName) throws Exception {
        new PGMXReader().read(network(fileName));
    }
}
