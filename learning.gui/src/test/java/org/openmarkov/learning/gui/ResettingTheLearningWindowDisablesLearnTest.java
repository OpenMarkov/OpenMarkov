/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.gui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * After «Reset values» the learning window has no dataset, so «Learn» is disabled (error 217).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The learning window needs a screen")
class ResettingTheLearningWindowDisablesLearnTest {

    private static boolean ignoreStorage;

    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void learnIsDisabledAfterReset(@TempDir Path folder) throws Exception {
        File dataset = Files.writeString(folder.resolve("dataset.csv"), "A,B\nyes,no\nno,no\nyes,yes\n").toFile();
        AtomicBoolean enabledAfterLoading = new AtomicBoolean();
        AtomicBoolean enabledAfterReset = new AtomicBoolean(true);
        SwingUtilities.invokeAndWait(() -> {
            JFrame owner = new JFrame();
            try {
                LearningDialog dialog = new LearningDialog(owner);
                load(dialog, dataset);
                JButton learn = (JButton) field(dialog, "learnButton");
                enabledAfterLoading.set(learn.isEnabled());
                button(dialog.getContentPane(), "Reset values").doClick();
                enabledAfterReset.set(learn.isEnabled());
                dialog.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                owner.dispose();
            }
        });
        assertTrue(enabledAfterLoading.get());
        assertFalse(enabledAfterReset.get());
    }

    /** Loads the dataset as the window does once the user has chosen its file. */
    private static void load(LearningDialog dialog, File dataset) throws Exception {
        Field path = LearningDialog.class.getDeclaredField("databasePath");
        path.setAccessible(true);
        path.set(dialog, dataset.getAbsolutePath());
        Method loadCaseFile = LearningDialog.class.getDeclaredMethod("loadCaseFile", File.class);
        loadCaseFile.setAccessible(true);
        loadCaseFile.invoke(dialog, dataset);
    }

    private static Object field(LearningDialog dialog, String name) throws Exception {
        Field field = LearningDialog.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(dialog);
    }

    private static JButton button(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container inner) {
                JButton found = button(inner, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
