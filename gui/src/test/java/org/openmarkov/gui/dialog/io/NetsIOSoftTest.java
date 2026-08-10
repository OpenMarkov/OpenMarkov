/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.gui.exception.CorruptNetworkFile;

import java.net.URI;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NetsIOSoftTest {

    @Test
    public void testURLConnection() throws java.io.IOException, ProbNetParserException, NoReaderForFileException, CorruptNetworkFile {
        NetsIO.openNetworkURL(URI.create("https://bitbucket.org/cisiad/org.probmodelxml.networks/raw/master/bn/BN-asia.pgmx")
                                 .toURL());
    }
}
