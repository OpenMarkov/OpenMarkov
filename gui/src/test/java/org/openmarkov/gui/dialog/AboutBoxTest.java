/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import java.time.LocalDate;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The About window takes the version and the date from the build, not from text written by hand, and names the
 * authors.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AboutBoxTest {

    @Test
    void theVersionAndTheDateComeFromTheBuild() {
        Properties build = AboutBox.readBuildProperties();
        String version = build.getProperty("version");
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"), version);
        assertDoesNotThrow(() -> LocalDate.parse(build.getProperty("date")));
    }

    @Test
    void itNamesTheAuthorsInTheirOrder() {
        String names = AboutBox.authorsLabel(400).getText().replace("&nbsp;", " ");
        assertTrue(names.contains("<b>Authors.</b> Manuel Arias, Francisco Javier Díez, Manuel Luque, Íñigo Bermejo, "
                + "Jorge Pérez, Jorge Rico, José Andrés Mota, Carmen María Yago, Jesús Oliva, Antonio Sáez, "
                + "Caroline König</div>"), names);
    }
}
