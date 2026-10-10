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
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The table of an event and the table of a transition do not carry uncertainty. A file that brings
 * it there is rejected saying which node.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class UncertaintyInAnEventTableIsRejectedWithAMessageTest {

    /** Reads the test network after adding uncertainty to its first potential of that type. */
    private String messageOnReading(Path folder, String potentialType) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource("DES-illness.pgmx").toURI()));
        Matcher values = Pattern.compile("<Potential type=\"" + potentialType + "\">.*?<Values>(.*?)</Values>", Pattern.DOTALL)
                                .matcher(text);
        assertTrue(values.find());
        int cells = values.group(1).trim().split("\\s+").length;
        String uncertainty = "<UncertainValues>" + "<Value distribution=\"Range\">0.1 0.3</Value>".repeat(cells)
                + "</UncertainValues>";
        Path file = folder.resolve("network.pgmx");
        Files.writeString(file, text.substring(0, values.end()) + uncertainty + text.substring(values.end()));

        return assertThrows(PGMXParserException.UncertaintyInAnEventTable.class,
                () -> new PGMXReader().read(file.toUri().toURL())).getExceptionMessage();
    }

    @Test public void inTheTableOfAnEvent(@TempDir Path folder) throws Exception {
        assertEquals("The relation of Onset cannot be read: the table of an event or of a transition cannot carry "
                + "uncertainty.", messageOnReading(folder, "DistributionTable"));
    }

    @Test public void inATransitionTable(@TempDir Path folder) throws Exception {
        assertEquals("The relation of Ill cannot be read: the table of an event or of a transition cannot carry "
                + "uncertainty.", messageOnReading(folder, "TransitionTable"));
    }
}
