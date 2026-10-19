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
import org.openmarkov.core.model.network.potential.IndicatorPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * In the network of events of the tests, Onset is given by an indicator: time to the event 2 and probability of
 * occurrence 0.25. A file with a number out of range in the indicator used to be opened without a warning; the
 * simulation used it as it was, and accepting the dialog of the node ended in an unexpected error (error 266 of the
 * pending list). Now the file is not opened, and the message says which node and which number. A file without one
 * of the two numbers used to fail with a null pointer, and the message only said that the network was corrupted
 * (error 276); now it says which node and which number is missing.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class AnIndicatorOutOfRangeIsNotReadTest {

    @TempDir Path folder;

    private String saved;

    @BeforeEach public void setUp() throws Exception {
        ProbNet network = new PGMXReader().read(getClass().getClassLoader().getResource("DES-illness.pgmx")).probNet();
        Potential old = network.getNode("Onset").getPotentials().getFirst();
        network.getNode("Onset").setPotential(new IndicatorPotential(old.getVariables(), PotentialRole.CONDITIONAL_PROBABILITY, 2, 0.25));
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

    @Test public void aSoundIndicatorIsRead() throws Exception {
        IndicatorPotential indicator = (IndicatorPotential) onsetIn(saved);
        assertEquals(2, indicator.getTte());
        assertEquals(0.25, indicator.getpOccurrence());
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1", "NaN", "Infinity", "abc" })
    public void aTimeToEventOutOfRangeIsNotRead(String tte) {
        var rejected = assertThrows(PGMXParserException.TimeToEventOutOfRange.class,
                                    () -> onsetIn(with("<TTE>2.0</TTE>", "<TTE>" + tte + "</TTE>")));
        assertEquals("The relation of Onset cannot be read: its time to event, " + tte
                             + ", must be a finite number not below 0.", rejected.getExceptionMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "2", "-0.5", "NaN" })
    public void aProbabilityOfOccurrenceOutOfRangeIsNotRead(String pOccurrence) {
        var rejected = assertThrows(PGMXParserException.ProbabilityOfOccurrenceOutOfRange.class,
                                    () -> onsetIn(with("<ProbabilityOccurrence>0.25</ProbabilityOccurrence>",
                                                       "<ProbabilityOccurrence>" + pOccurrence + "</ProbabilityOccurrence>")));
        assertEquals("The relation of Onset cannot be read: its probability of occurrence, " + pOccurrence
                             + ", must be a number between 0 and 1.", rejected.getExceptionMessage());
    }

    @Test public void anIndicatorWithoutTimeToEventIsNotRead() {
        var rejected = assertThrows(PGMXParserException.TimeToEventMissing.class,
                                    () -> onsetIn(with("<TTE>2.0</TTE>", "")));
        assertEquals("The relation of Onset cannot be read: it has no time to event.", rejected.getExceptionMessage());
    }

    @Test public void anIndicatorWithoutProbabilityOfOccurrenceIsNotRead() {
        var rejected = assertThrows(PGMXParserException.ProbabilityOfOccurrenceMissing.class,
                                    () -> onsetIn(with("<ProbabilityOccurrence>0.25</ProbabilityOccurrence>", "")));
        assertEquals("The relation of Onset cannot be read: it has no probability of occurrence.",
                     rejected.getExceptionMessage());
    }
}
