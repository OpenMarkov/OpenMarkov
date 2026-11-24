/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.settings;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.NetworkSource;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Changing in the settings the number of recent networks shows at once that many in «Open recent network...»
 * (error 446).
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheMenuOfRecentNetworksFollowsTheirNumberTest {

    private static boolean ignoreStorage;

    private List<NetworkSource> recentBefore;

    private int numberBefore;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void nineRecentNetworksInTheMenu() throws Exception {
        recentBefore = new ArrayList<>(UserPreferences.LAST_OPEN_NETWORKS_SOURCES.get());
        numberBefore = UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.get();
        SwingUtilities.invokeAndWait(() -> {
            UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(10);
            UserPreferences.LAST_OPEN_NETWORKS_SOURCES.set(new ArrayList<>(
                    IntStream.rangeClosed(1, 9).mapToObj(n -> network("net" + n)).toList()));
            MainGUI.INSTANCE.mainPanel.getMainMenu().rechargeFileMenu();
        });
    }

    @AfterEach void restoreTheRecentNetworks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UserPreferences.LAST_OPEN_NETWORKS_SOURCES.set(new ArrayList<>(recentBefore));
            UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(numberBefore);
            MainGUI.INSTANCE.mainPanel.getMainMenu().rechargeFileMenu();
        });
    }

    private static NetworkSource network(String name) {
        return new NetworkSource.SourceFile(new File("/networks", name + ".pgmx"));
    }

    @Tag(TestSpeed.MEDIUM)
    @Test void loweringTheNumberShortensTheMenu() throws Exception {
        List<String> shown = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            SettingsDialog.changeTheNumberOfRecentNetworks(3);
            shown.addAll(recentNetworksInTheMenu());
        });

        assertEquals(List.of(network("net1"), network("net2"), network("net3")).stream()
                             .map(LastOpenFiles::textOf).toList(), shown);
    }

    /** The texts of the items of «Open recent network...», in the File menu. */
    private static List<String> recentNetworksInTheMenu() {
        JMenu file = MainGUI.INSTANCE.mainPanel.getMainMenu().getMenu(0);
        String title = StringDatabase.getUniqueInstance().getString("File.OpenRecent");
        for (Component component : file.getMenuComponents()) {
            if (component instanceof JMenu recent && recent.getText().equals(title)) {
                return Arrays.stream(recent.getMenuComponents())
                             .filter(JMenuItem.class::isInstance)
                             .map(item -> ((JMenuItem) item).getText())
                             .toList();
            }
        }
        throw new AssertionError("The File menu has no «" + title + "»");
    }
}
