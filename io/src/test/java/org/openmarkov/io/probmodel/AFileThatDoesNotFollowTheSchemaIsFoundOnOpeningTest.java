/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file with a misspelt element still opens, and the places where it does not follow the schema
 * are found to warn about them.
 *
 * @author Manuel Arias
 */
public class AFileThatDoesNotFollowTheSchemaIsFoundOnOpeningTest {

    private static final String NETWORK = "test-ici-reading.pgmx";
    private static final String TEMPORAL_NETWORK = "DES-illness.pgmx";

    @Tag(TestSpeed.MEDIUM)
    @Test public void aMisspeltElementIsFoundWithItsLine(@TempDir Path folder) throws Exception {
        URL correct = getClass().getClassLoader().getResource(NETWORK);
        String text = Files.readString(Path.of(correct.toURI()));
        Path misspelt = folder.resolve(NETWORK);
        Files.writeString(misspelt, text.replaceFirst("<Values>", "<Valores>").replaceFirst("</Values>", "</Valores>"));
        long line = text.substring(0, text.indexOf("<Values>")).lines().count();

        List<String> found = FormatManager.getInstance().schemaProblems(misspelt.toUri().toURL());

        assertFalse(found.isEmpty());
        assertTrue(found.getFirst().startsWith("Line " + line + ": "), found.toString());
        assertTrue(found.getFirst().contains("Invalid content was found starting with element 'Valores'"),
                found.toString());
    }

    @Tag(TestSpeed.MEDIUM)
    @ParameterizedTest @ValueSource(strings = {"<Slices>7</Slices>", "<Transition>BEGINNING</Transition>"})
    public void temporalOptionsWithoutTheirSlicesOrTransitionAreFound(String element, @TempDir Path folder) throws Exception {
        URL correct = getClass().getClassLoader().getResource(TEMPORAL_NETWORK);
        Path without = folder.resolve(TEMPORAL_NETWORK);
        Files.writeString(without, Files.readString(Path.of(correct.toURI())).replaceFirst(element, ""));
        String name = element.substring(1, element.indexOf('>'));

        List<String> found = FormatManager.getInstance().schemaProblems(without.toUri().toURL());

        assertFalse(FormatManager.getInstance().schemaProblems(correct).toString().contains("TemporalOptions"));
        assertTrue(found.stream().anyMatch(problem -> problem.contains("'TemporalOptions' is not complete")
                                                      && problem.contains(name)), found.toString());
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aCorrectFileGivesNoWarning() throws Exception {
        assertEquals(List.of(), FormatManager.getInstance().schemaProblems(getClass().getClassLoader().getResource(NETWORK)));
    }
}
