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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The relation of an event node names a distribution and a parametrization. A file that names a
 * pair the program does not know, or no parametrization, is rejected saying which node and which pair.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class AnUnknownDistributionOfAnEventIsRejectedWithAMessageTest {

    private static final String PARAMETRIZATION = " parametrization=\"Nu\"";

    private String network() throws Exception {
        return Files.readString(Path.of(getClass().getClassLoader().getResource("DES-illness.pgmx").toURI()));
    }

    private static String messageOnReading(Path folder, String text) throws Exception {
        Path file = folder.resolve("network.pgmx");
        Files.writeString(file, text);
        return assertThrows(PGMXParserException.UnknownDistribution.class,
                () -> new PGMXReader().read(file.toUri().toURL())).getExceptionMessage();
    }

    @Test public void theNetworkAsItIsCanBeRead() {
        assertDoesNotThrow(() -> new PGMXReader().read(getClass().getClassLoader().getResource("DES-illness.pgmx")));
    }

    @Test public void aParametrizationThatDoesNotExist(@TempDir Path folder) throws Exception {
        String text = network().replaceFirst(PARAMETRIZATION, " parametrization=\"Alpha / Beta\"");

        assertEquals("The relation of Onset cannot be read: the program does not know the distribution Exact "
                + "with the parametrization Alpha / Beta.", messageOnReading(folder, text));
    }

    @Test public void noParametrization(@TempDir Path folder) throws Exception {
        String text = network().replaceFirst(PARAMETRIZATION, "");

        assertEquals("The relation of Onset cannot be read: the program does not know the distribution Exact "
                + "without a parametrization.", messageOnReading(folder, text));
    }
}
