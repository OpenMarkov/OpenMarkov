/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.TablesAFileLeavesOut;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A file may leave out the numbers of a table, or the table of a parent of a canonical model. The reader filled them
 * in without a warning, and a table of utilities without numbers did not open (error 399 of the pending list). Now the
 * network opens and the window is told which tables were left out and how each was filled in.
 *
 * @author Manuel Arias
 */
class TablesThatAFileLeavesOutAreFoundOnOpeningTest {

    @TempDir Path folder;

    private static URL network(String fileName) {
        return Networks.getNetworks().filter(network -> network.getPath().endsWith("/" + fileName)).findFirst().orElseThrow();
    }

    /** A copy of the network without the first text that matches {@code leftOut}, which must be there. */
    private URL without(String fileName, String leftOut) throws Exception {
        String text = Files.readString(Path.of(network(fileName).toURI()));
        String changed = text.replaceFirst(leftOut, "");
        assertNotEquals(text, changed);
        Path file = folder.resolve(fileName);
        Files.writeString(file, changed);
        return file.toUri().toURL();
    }

    @Test void aTableOfProbabilitiesWithoutNumbersOpensAsUniform() throws Exception {
        URL file = without("BN-asia.pgmx", "<Values>0.95 0.05 0.02 0.98</Values>");

        assertEquals(List.of("X-ray: its table has no numbers. It is opened as uniform."), TablesAFileLeavesOut.in(file));
        ProbNet read = new PGMXReader().read(file).probNet();
        assertArrayEquals(new double[] {0.5, 0.5, 0.5, 0.5},
                          ((TablePotential) read.getNode("X-ray").getPotential()).getValues(), 1e-12);
    }

    @Test void aTableOfUtilitiesWithoutNumbersOpensWithZeros() throws Exception {
        URL file = without("ID-decide-test.pgmx", "<Values>10.0 4.0 9.0 8.0</Values>");

        assertEquals(List.of("Health state: its table has no numbers. It is opened with zeros."),
                     TablesAFileLeavesOut.in(file));
        ProbNet read = new PGMXReader().read(file).probNet();
        ExactDistrPotential utility = (ExactDistrPotential) read.getNode("Health state").getPotential();
        assertArrayEquals(new double[] {0, 0, 0, 0}, utility.getTablePotential().getValues(), 1e-12);
    }

    @Test void aTableOfUtilitiesWithoutNumbersInFormat1OpensWithZeros() throws Exception {
        URL file = without("ID-decide-test-1-0.pgmx", "<Values>10.0 4.0 9.0 8.0</Values>");

        assertEquals(List.of("Health state: its table has no numbers. It is opened with zeros."),
                     TablesAFileLeavesOut.in(file));
        ProbNet read = new PGMXReader().read(file).probNet();
        ExactDistrPotential utility = (ExactDistrPotential) read.getNode("Health state").getPotential();
        assertArrayEquals(new double[] {0, 0, 0, 0}, utility.getTablePotential().getValues(), 1e-12);
    }

    @Test void aCanonicalModelWithoutTheTableOfAParentOpensWithTheTableByDefault() throws Exception {
        URL file = without("BN-noisy-or-four-parents.pgmx",
                           "<Potential type=\"Table\">\\s*<Variables>\\s*<Variable name=\"Fever\" />\\s*"
                                   + "<Variable name=\"Paludism\" />\\s*</Variables>\\s*<Values>[^<]*</Values>\\s*</Potential>");

        assertEquals(List.of("Fever: its canonical model has no table for the parent Paludism. It is opened with the"
                                     + " table by default, in which Paludism alone determines Fever."),
                     TablesAFileLeavesOut.in(file));
        ProbNet read = new PGMXReader().read(file).probNet();
        ICIPotential model = (ICIPotential) read.getNode("Fever").getPotential();
        assertArrayEquals(new double[] {1, 0, 0, 1}, model.getNoisyParameters(read.getVariable("Paludism")), 1e-12);
    }

    @Test void aFileWithAllItsTablesGivesNoWarning() throws Exception {
        for (String complete : List.of("BN-asia.pgmx", "ID-decide-test.pgmx", "BN-noisy-or-four-parents.pgmx")) {
            assertEquals(List.of(), TablesAFileLeavesOut.in(network(complete)), complete);
        }
    }
}
