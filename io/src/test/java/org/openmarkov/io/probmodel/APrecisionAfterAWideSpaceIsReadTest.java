/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A precision written after a wide space (U+2003), which the reader strips, is read the same for a variable of states
 * and for a numeric one.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APrecisionAfterAWideSpaceIsReadTest {

    @TempDir Path folder;

    @ParameterizedTest
    @CsvSource({ "finiteStates, <States><State name=\"no\" /><State name=\"yes\" /></States>", "numeric, ''" })
    void isRead(String type, String states) throws Exception {
        Path file = folder.resolve("network.pgmx");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ProbModelXML formatVersion="0.2.0">
                  <ProbNet type="BayesianNetwork">
                    <Variables>
                      <Variable name="X" type="%s" role="chance">
                        <Precision> 0.5</Precision>
                        %s
                      </Variable>
                    </Variables>
                  </ProbNet>
                </ProbModelXML>
                """.formatted(type, states));
        ProbNet probNet = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(0.5, probNet.getVariable("X").getPrecision());
    }
}
