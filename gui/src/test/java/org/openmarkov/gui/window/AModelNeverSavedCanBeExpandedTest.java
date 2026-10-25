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
 * «Expand Network» names the expanded network after the model, without the extension when the name has one, and puts
 * it in the folder of the model's file; a model created with «New network» and never saved gives a network without a
 * file.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AModelNeverSavedCanBeExpandedTest {

    private static final String SUFFIX =
            StringDatabase.getUniqueInstance().getString("CostEffectiveness.ExpandNetwork.FileName");

    @Test void aModelNeverSavedGivesANetworkWithoutAFile() {
        String fileName = InferenceHandler.expandedFileName("New network");

        assertEquals("New network" + SUFFIX + ".pgmx", fileName);
        assertNull(InferenceHandler.expandedFile(null, fileName));
    }

    @Test void aSavedModelGivesANetworkInItsFolder() {
        File folder = new File("a", "b");
        String fileName = InferenceHandler.expandedFileName("MID-x.pgmx");

        assertEquals("MID-x" + SUFFIX + ".pgmx", fileName);
        assertEquals(new File(folder, fileName).getPath(),
                     InferenceHandler.expandedFile(new File(folder, "MID-x.pgmx").getPath(), fileName));
    }
}
