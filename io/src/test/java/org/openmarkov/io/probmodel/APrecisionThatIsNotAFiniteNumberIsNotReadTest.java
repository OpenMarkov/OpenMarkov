/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A file whose variable has a precision that is not a finite number not below 0, the only ones its values can be
 * rounded to, is not opened, and the message names the variable (option A chosen by Manuel Arias). It used to be
 * opened without a warning, and «Node properties» on that node ended in an unexpected error (error 264).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APrecisionThatIsNotAFiniteNumberIsNotReadTest {

    @TempDir Path folder;

    private ProbNet read(String precision) throws Exception {
        Path file = folder.resolve("network.pgmx");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ProbModelXML formatVersion="0.2.0">
                  <ProbNet type="BayesianNetwork">
                    <Variables>
                      <Variable name="Smoker" type="finiteStates" role="chance">
                        <Precision>%s</Precision>
                        <States>
                          <State name="no" />
                          <State name="yes" />
                        </States>
                      </Variable>
                    </Variables>
                  </ProbNet>
                </ProbModelXML>
                """.formatted(precision));
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    @ParameterizedTest
    @ValueSource(strings = { "NaN", "Infinity", "-Infinity", "-1", "abc" })
    void isNotRead(String precision) {
        var refused = assertThrows(PGMXParserException.PrecisionOutOfRange.class, () -> read(precision));

        assertEquals("Variable Smoker cannot be read: its precision, " + precision
                     + ", must be a finite number not below 0.", refused.getExceptionMessage());
    }

    @Test void aPrecisionOfZeroIsRead() throws Exception {
        assertEquals(0.0, read("0.0").getVariable("Smoker").getPrecision());
    }
}
