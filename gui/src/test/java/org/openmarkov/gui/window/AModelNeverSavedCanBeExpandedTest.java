/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * «Expand Network» names the expanded network after the model's file, without its extension, and puts it in the
 * folder of that file; a model never saved keeps its whole name, even with a dot, and gives a network without a file.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AModelNeverSavedCanBeExpandedTest {

    private static final String SUFFIX =
            StringDatabase.getUniqueInstance().getString("CostEffectiveness.ExpandNetwork.FileName");

    @Test void aModelNeverSavedGivesANetworkWithoutAFile() {
        String fileName = InferenceHandler.expandedFileName("New network", null);

        assertEquals("New network" + SUFFIX + ".pgmx", fileName);
        assertNull(InferenceHandler.expandedFile(null, fileName));
    }

    @Test void aModelNeverSavedKeepsADotInItsName() {
        assertEquals("Model v1.2" + SUFFIX + ".pgmx", InferenceHandler.expandedFileName("Model v1.2", null));
    }

    @Test void aSavedModelGivesANetworkInItsFolder() {
        File folder = new File("a", "b");
        String modelFile = new File(folder, "MID-x.pgmx").getPath();
        String fileName = InferenceHandler.expandedFileName("MID-x.pgmx", modelFile);

        assertEquals("MID-x" + SUFFIX + ".pgmx", fileName);
        assertEquals(new File(folder, fileName).getPath(), InferenceHandler.expandedFile(modelFile, fileName));
    }
}
