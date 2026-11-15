/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Closing without choosing a file the window in which «Open» asks for it does nothing: no error and no new tab.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ClosingTheOpenDialogWithoutAFileDoesNothingTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** @return the file chooser shown in some window, or null if none is showing */
    private static JFileChooser showingFileChooser() {
        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
                JFileChooser chooser = fileChooserIn(window);
                if (chooser != null) {
                    return chooser;
                }
            }
        }
        return null;
    }

    private static JFileChooser fileChooserIn(Component component) {
        if (component instanceof JFileChooser chooser) {
            return chooser;
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                JFileChooser chooser = fileChooserIn(child);
                if (chooser != null) {
                    return chooser;
                }
            }
        }
        return null;
    }

    @Test void closingTheFileChooserWithoutAFileGivesNoErrorAndNoTab() throws Exception {
        AtomicReference<MainPanelListenerAssistant> assistant = new AtomicReference<>();
        AtomicInteger tabsBefore = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            assistant.set(MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant());
            tabsBefore.set(MainGUI.INSTANCE.mainPanel.getTabPanels().getjTabbedPane().getTabCount());
        });
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                assistant.get().actionPerformed(new ActionEvent(MainGUI.INSTANCE, ActionEvent.ACTION_PERFORMED,
                                                                ActionCommands.OPEN_NETWORK.toString()));
            } catch (Throwable throwable) {
                thrown.set(throwable);
            } finally {
                done.countDown();
            }
        });
        JFileChooser chooser = null;
        for (int attempt = 0; attempt < 100 && chooser == null; attempt++) {
            Thread.sleep(100);
            chooser = showingFileChooser();
        }
        assertNotNull(chooser, "«Open» shows a file chooser");
        JFileChooser shown = chooser;
        SwingUtilities.invokeLater(shown::cancelSelection);

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(thrown.get());
        AtomicInteger tabsAfter = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> tabsAfter.set(MainGUI.INSTANCE.mainPanel.getTabPanels().getjTabbedPane().getTabCount()));
        assertEquals(tabsBefore.get(), tabsAfter.get());
    }
}
