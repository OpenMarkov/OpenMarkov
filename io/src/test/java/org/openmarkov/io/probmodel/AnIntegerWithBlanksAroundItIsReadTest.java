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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the network of events of the tests, the number of slices and the coordinate x of «Therapy» are integers. Written
 * with blanks around them, they are read.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnIntegerWithBlanksAroundItIsReadTest {

    @TempDir Path folder;

    @Test void isRead() throws Exception {
        String saved = Files.readString(Path.of(getClass().getClassLoader().getResource("DES-illness.pgmx").toURI()));
        String text = saved.replace("<Slices>7</Slices>", "<Slices> 9 </Slices>")
                           .replace("<Coordinates x=\"60\" y=\"60\"/>", "<Coordinates x=\" 61\" y=\"60\"/>");
        assertTrue(text.contains("<Slices> 9 </Slices>") && text.contains("x=\" 61\""), "the file has both integers");
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, text);

        ProbNet probNet = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(9, probNet.getInferenceOptions().getTemporalOptions().getHorizon());
        assertEquals(61, probNet.getNode("Therapy").getCoordinateX());
    }
}
