/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.NetworkSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The list of recent networks keeps to the number chosen in the settings: it is shortened as soon as the number is
 * lowered, and also when a network of the list is opened again (error 446).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheRecentNetworksKeepToTheirNumberTest {

    private static boolean ignoreStorage;

    private List<NetworkSource> recentBefore;

    private int numberBefore;

    /** The test must not read or write the preferences of whoever runs it. */
    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void nineRecentNetworks() {
        recentBefore = new ArrayList<>(UserPreferences.LAST_OPEN_NETWORKS_SOURCES.get());
        numberBefore = UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.get();
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(10);
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES.set(
                new ArrayList<>(IntStream.rangeClosed(1, 9).mapToObj(n -> network("net" + n)).toList()));
    }

    @AfterEach void restoreTheRecentNetworks() {
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES.set(new ArrayList<>(recentBefore));
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(numberBefore);
    }

    private static NetworkSource network(String name) {
        return new NetworkSource.SourceFile(new File("/networks", name + ".pgmx"));
    }

    @Test void loweringTheNumberKeepsTheMostRecent() {
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(3);
        LastOpenFiles.keepOnlyTheMostRecent();

        assertEquals(List.of(network("net1"), network("net2"), network("net3")),
                     UserPreferences.LAST_OPEN_NETWORKS_SOURCES.get());
    }

    @Test void zeroLeavesNoRecentNetwork() {
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(0);
        LastOpenFiles.keepOnlyTheMostRecent();

        assertEquals(List.of(), UserPreferences.LAST_OPEN_NETWORKS_SOURCES.get());
    }

    @Test void openingAgainANetworkOfTheListKeepsToTheNumber() {
        UserPreferences.LAST_OPEN_NETWORKS_SOURCES_MAX.set(3);
        LastOpenFiles.setLastSource(network("net5"));

        assertEquals(List.of(network("net5"), network("net1"), network("net2")),
                     UserPreferences.LAST_OPEN_NETWORKS_SOURCES.get());
    }
}
