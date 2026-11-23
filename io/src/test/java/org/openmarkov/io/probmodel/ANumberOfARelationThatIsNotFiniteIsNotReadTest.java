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
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A file with a NaN or an infinite among the numbers of a relation, a table, a fixed value or a parameter of an
 * uncertain value, is not opened, and the message names the node (option chosen by Manuel Arias). It used to be opened
 * without a warning, and the results were zeros or minus infinity (error 398 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANumberOfARelationThatIsNotFiniteIsNotReadTest {

    @TempDir Path folder;

    /** A Bayesian network in the format of that version with the node Smoker and the relation given. */
    private ProbNet read(String version, String relation) throws Exception {
        Path file = folder.resolve("network.pgmx");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ProbModelXML formatVersion="%s">
                  <ProbNet type="BayesianNetwork">
                    <Variables>
                      <Variable name="Smoker" type="finiteStates" role="chance">
                        <States>
                          <State name="no" />
                          <State name="yes" />
                        </States>
                      </Variable>
                    </Variables>
                    <Potentials>
                      %s
                    </Potentials>
                  </ProbNet>
                </ProbModelXML>
                """.formatted(version, relation));
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    private static String table(String version, String content) {
        String opening = version.startsWith("0") ? "<Potential type=\"Table\" role=\"conditionalProbability\">"
                                                 : "<Potential type=\"ProbTable\">";
        return opening + "<Variables><Variable name=\"Smoker\" /></Variables>" + content + "</Potential>";
    }

    @ParameterizedTest
    @CsvSource({ "0.2.0, NaN", "0.2.0, Infinity", "0.2.0, -Infinity", "1.0.0, NaN", "1.0.0, Infinity", "1.0.0, -Infinity" })
    void inATable(String version, String number) {
        String relation = table(version, "<Values>" + number + " 0.5</Values>");

        var refused = assertThrows(PGMXParserException.NumberNotFinite.class, () -> read(version, relation));

        assertEquals("The relation of Smoker cannot be read: " + number + " is not a finite number.",
                     refused.getExceptionMessage());
    }

    @ParameterizedTest
    @CsvSource({ "0.2.0, Exact, NaN", "0.2.0, Beta, NaN 96.0", "1.0.0, Exact, NaN", "1.0.0, Beta, 4.0 Infinity" })
    void inAnUncertainValue(String version, String distribution, String parameters) {
        String relation = table(version, "<Values>0.04 0.96</Values><UncertainValues><Value distribution=\""
                + distribution + "\">" + parameters + "</Value><Value distribution=\"Complement\">0.96</Value></UncertainValues>");

        var refused = assertThrows(PGMXParserException.NumberNotFinite.class, () -> read(version, relation));

        assertEquals("Smoker", refused.nodeName);
    }

    @ParameterizedTest
    @CsvSource({ "0.2.0", "1.0.0" })
    void finiteNumbersAreRead(String version) throws Exception {
        ProbNet read = read(version, table(version, "<Values>0.25 0.75</Values>"));

        assertArrayEquals(new double[] {0.25, 0.75},
                          ((TablePotential) read.getNode("Smoker").getPotential()).getValues(), 1e-12);
    }

    @Test void theLimitOfAnIntervalMayBeInfinite() throws Exception {
        Path file = folder.resolve("interval.pgmx");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ProbModelXML formatVersion="0.2.0">
                  <ProbNet type="BayesianNetwork">
                    <Variables>
                      <Variable name="Age" type="discretized" role="chance">
                        <States>
                          <State name="young" />
                          <State name="old" />
                        </States>
                        <Thresholds>
                          <Threshold value="0.0" belongsTo="left" />
                          <Threshold value="50.0" belongsTo="left" />
                          <Threshold value="Infinity" belongsTo="right" />
                        </Thresholds>
                      </Variable>
                    </Variables>
                  </ProbNet>
                </ProbModelXML>
                """);

        ProbNet read = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(Double.POSITIVE_INFINITY, read.getVariable("Age").getPartitionedInterval().getMax());
    }
}
