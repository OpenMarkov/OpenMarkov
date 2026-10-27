/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the network of events of the tests, Onset is given by a life table of rates: 0.1 from time 0 and 1.5 from time
 * 5. A file with a value out of range in a life table used to be opened without a warning, and the simulation drew
 * times that were not numbers (error 359 of the pending list). Now the file is not opened, and the message says which
 * node, which time and which value; 1.5 is out of range only as a probability.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class ALifeTableOutOfRangeIsNotReadTest {

    @TempDir Path folder;

    private String saved;

    @BeforeEach public void setUp() throws Exception {
        ProbNet network = new PGMXReader().read(getClass().getClassLoader().getResource("DES-illness.pgmx")).probNet();
        Potential old = network.getNode("Onset").getPotentials().getFirst();
        FunctionPotential fromTimeZero = new FunctionPotential(old.getVariables(), PotentialRole.CONDITIONAL_PROBABILITY);
        network.getNode("Onset").setPotential(new PiecewiseExponentialPotential(
                old.getVariables(), PotentialRole.CONDITIONAL_PROBABILITY, new TreeMap<>(Map.of(0.0, 0.1, 5.0, 1.5)),
                fromTimeZero, true));
        Path file = folder.resolve("saved.pgmx");
        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        saved = Files.readString(file);
    }

    private Potential onsetIn(String text) throws Exception {
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, text);
        return new PGMXReader().read(file.toUri().toURL()).probNet().getNode("Onset").getPotentials().getFirst();
    }

    private String with(String from, String to) {
        String text = saved.replace(from, to);
        assertNotEquals(saved, text, "the file has " + from);
        return text;
    }

    @Test public void aLifeTableOfRatesIsRead() throws Exception {
        PiecewiseExponentialPotential lifeTable = (PiecewiseExponentialPotential) onsetIn(saved);
        assertTrue(lifeTable.isUseRates());
        assertEquals(Map.of(0.0, 0.1, 5.0, 1.5), lifeTable.getPiecewiseTable());
    }

    @Test public void aLifeTableOfProbabilitiesIsRead() throws Exception {
        PiecewiseExponentialPotential lifeTable = (PiecewiseExponentialPotential) onsetIn(
                with("<Rates>true</Rates>", "<Rates>false</Rates>").replace("0.1 1.5", "0.1 0.5"));
        assertFalse(lifeTable.isUseRates());
        assertEquals(Map.of(0.0, 0.1, 5.0, 0.5), lifeTable.getPiecewiseTable());
    }

    @Test public void aProbabilityAboveOneIsNotRead() {
        var rejected = assertThrows(PGMXParserException.LifeTableProbabilityOutOfRange.class,
                                    () -> onsetIn(with("<Rates>true</Rates>", "<Rates>false</Rates>")));
        assertEquals("The relation of Onset cannot be read: its probability at time 5.0, 1.5, must be a number "
                             + "between 0 and 1.", rejected.getExceptionMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "-0.2", "NaN", "Infinity", "abc" })
    public void aRateOutOfRangeIsNotRead(String rate) {
        var rejected = assertThrows(PGMXParserException.LifeTableRateOutOfRange.class,
                                    () -> onsetIn(with("0.1 1.5", "0.1 " + rate)));
        assertEquals("The relation of Onset cannot be read: its rate at time 5.0, " + rate
                             + ", must be a finite number not below 0.", rejected.getExceptionMessage());
    }
}
