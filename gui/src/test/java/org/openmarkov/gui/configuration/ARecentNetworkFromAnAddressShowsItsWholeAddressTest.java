/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.configuration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.NetworkSource;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A recent network brought from an address shows the whole address, with its server, and two addresses are the
 * same network only if they are written the same (error 371).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ARecentNetworkFromAnAddressShowsItsWholeAddressTest {

    private static NetworkSource from(String address) throws Exception {
        return new NetworkSource.SourceURL(URI.create(address).toURL());
    }

    @Test void theTextIsTheWholeAddress() throws Exception {
        assertEquals("https://www.cisiad.uned.es/redes/asia.pgmx",
                     LastOpenFiles.textOf(from("https://www.cisiad.uned.es/redes/asia.pgmx")));
    }

    @Test void theTextOfAFileIsItsWholePath() {
        File file = new File("redes", "asia.pgmx");
        assertEquals(file.getAbsolutePath(), LastOpenFiles.textOf(new NetworkSource.SourceFile(file)));
    }

    /** Names that do not exist: comparing them must not ask the network for anything. */
    @Test void theSamePathInTwoServersIsTwoNetworks() throws Exception {
        NetworkSource one = from("https://one.invalid/redes/asia.pgmx");
        NetworkSource other = from("https://other.invalid/redes/asia.pgmx");

        assertFalse(LastOpenFiles.same(one, other));
        assertTrue(LastOpenFiles.same(one, from("https://one.invalid/redes/asia.pgmx")));
        assertFalse(LastOpenFiles.same(one, new NetworkSource.SourceFile(new File("/redes/asia.pgmx"))));
    }
}
