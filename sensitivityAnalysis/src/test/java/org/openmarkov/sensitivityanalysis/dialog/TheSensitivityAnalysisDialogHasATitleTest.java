/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.SwingUtilities;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The dialog in which a sensitivity analysis is asked for is titled «Sensitivity analysis».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheSensitivityAnalysisDialogHasATitleTest {

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-one-chance-sa.pgmx");
        try (InputStream network = TheSensitivityAnalysisDialogHasATitleTest.class.getResourceAsStream(
                "/ID-one-chance-sa.pgmx")) {
            Files.copy(network, file);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainGUI.INSTANCE.mainPanel.openNetwork(file.toString());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel();
        });
    }

    @AfterAll static void closeTheNetwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> tab.close());
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void theTitle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SensitivityAnalysisController controller = new SensitivityAnalysisController(MainGUI.INSTANCE);
            controller.getConfiguration().setIsUnicriterion(true);
            SensitivityAnalysisDialog dialog = new SensitivityAnalysisDialog(MainGUI.INSTANCE, controller);
            try {
                assertEquals("Sensitivity analysis", dialog.getTitle());
            } finally {
                dialog.dispose();
            }
        });
    }
}
