/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.gui.exception.CorruptNetworkFile;

import java.net.URL;
import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NetsIOSoftTest {

    final ArrayList<String> urlsToTest = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        // BN
        urlsToTest.add("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/bn/BN-asia.pgmx");
        // DAN
        urlsToTest.add("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/dan/DAN-decide-test.pgmx");
        // ID
        urlsToTest.add("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/id/ID-decide-test.pgmx");
        // MID
        urlsToTest.add("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/mid/MID-Chancellor.pgmx");
        // POMDP
        urlsToTest.add("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/pomdp/POMDP-coffee-robot.pgmx");
        // Dec-POMDP
        urlsToTest.add("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/pomdp/Dec-POMDP-wireless-network.pgmx");
    }

    @Disabled("Ignored because a deprecated network")
    @Test
    public void testURLConnection() throws java.io.IOException, ProbNetParserException, NoReaderForFileException, CorruptNetworkFile {
        for (String urlString : urlsToTest) {
            URL url = new URL(urlString);
            NetsIO.openNetworkURL(url);
        }
    }
}
