/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ConditionalGaussianPotential;
import org.openmarkov.core.model.network.potential.DiscretizedCauchyPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A Conditional Gaussian or a Discretized Cauchy is saved, in formats 0.2 and 1.0, with its two parameters, so that the
 * file opens again with the same numbers; a file without one of them is refused naming the node and the parameter.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AGaussianOrCauchyRelationIsSavedWithItsParametersTest {

    private static ProbNet read(URL url) throws Exception {
        return new PGMXReader().read(url).probNet();
    }

    /** {@code probNet} written in format {@code version} and read again. */
    private static ProbNet savedAndOpened(ProbNet probNet, String version) throws Exception {
        Path file = Files.createTempDirectory("parameters").resolve("network.pgmx");
        if (version.equals("0.2")) {
            new PGMXWriter_0_2().write(file.toString(), probNet, List.of());
        } else {
            new PGMXWriter_1_0().write(file.toString(), probNet, List.of());
        }
        return read(file.toUri().toURL());
    }

    private static double[] values(Potential parameter) {
        return ((TablePotential) parameter).getValues();
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.2", "1.0" })
    void aConditionalGaussianKeepsItsMeanAndVariance(String version) throws Exception {
        ProbNet original = read(getClass().getResource("/BN-conditional-gaussian.pgmx"));
        var before = (ConditionalGaussianPotential) original.getNode("Height").getPotential();

        var after = (ConditionalGaussianPotential) savedAndOpened(original, version).getNode("Height").getPotential();

        assertArrayEquals(values(before.getMean()), values(after.getMean()));
        assertArrayEquals(values(before.getVariance()), values(after.getVariance()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.2", "1.0" })
    void aDiscretizedCauchyKeepsItsMedianAndScale(String version) throws Exception {
        ProbNet original = read(getClass().getResource("/BN-discretized-cauchy.pgmx"));
        var before = (DiscretizedCauchyPotential) original.getNode("Rainfall").getPotential();

        var after = (DiscretizedCauchyPotential) savedAndOpened(original, version).getNode("Rainfall").getPotential();

        assertArrayEquals(values(before.getMedian()), values(after.getMedian()));
        assertArrayEquals(values(before.getScale()), values(after.getScale()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.2", "1.0" })
    void aNewConditionalGaussianKeepsItsParameters(String version) throws Exception {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setName("network");
        Variable p = new Variable("P", "absent", "present");
        Variable x = new Variable("X", "low", "high");
        probNet.addNode(p, NodeType.CHANCE);
        Node nodeX = probNet.addNode(x, NodeType.CHANCE);
        probNet.addLink(p, x, true);
        var gaussian = new ConditionalGaussianPotential(List.of(x, p), PotentialRole.CONDITIONAL_PROBABILITY);
        ((TablePotential) gaussian.getMean()).setValues(new double[] { 1, 2, 3, 4 });
        nodeX.setPotential(gaussian);

        var after = (ConditionalGaussianPotential) savedAndOpened(probNet, version).getNode("X").getPotential();

        assertArrayEquals(new double[] { 1, 2, 3, 4 }, values(after.getMean()));
        assertArrayEquals(values(gaussian.getVariance()), values(after.getVariance()));
    }

    @Test void aFileWithoutTheVarianceIsRefusedNamingTheNode() throws Exception {
        Path original = Path.of(getClass().getResource("/BN-conditional-gaussian.pgmx").toURI());
        String withoutVariance = Files.readString(original).replaceFirst("(?s)<Variance>\\s*<Potential.*?</Variance>", "");
        Path file = Files.createTempDirectory("parameters").resolve("BN-conditional-gaussian.pgmx");
        Files.writeString(file, withoutVariance);

        var refused = assertThrows(PGMXParserException.RelationParameterMissing.class, () -> read(file.toUri().toURL()));

        assertEquals("Height", refused.variableName);
        assertEquals("Variance", refused.parameterName);
    }
}
