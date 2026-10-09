/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The networks of the repository in format 0.2 that have a utility node that multiplies its parents. Most of
 * them have utility nodes that add their parents too.
 *
 * @author Manuel Arias
 */
class SumAndProductUtilityNodesSurviveSavingInFormat02Test {

    @Tag(TestSpeed.MEDIUM)
    @ParameterizedTest
    @ValueSource(strings = { "ID-arthronet.pgmx", "ID-mediastinet.pgmx", "DAN-arthronet.pgmx",
            "DAN-economic-mediastinet.pgmx", "DAN-mediastinet.pgmx", "DAN-qale-mediastinet.pgmx", "MID-HPV.pgmx" })
    void theSavedNetworkIsTheSameNetwork(String fileName) throws Exception {
        ProbNet original = read(fileName);
        Map<String, String> utilityNodes = utilityNodes(original);
        double[] utility = utility(original);

        Path written = Files.createTempDirectory("format02").resolve(fileName);
        new PGMXWriter_0_2().write(written.toString(), original, List.of());

        assertEquals(utilityNodes, utilityNodes(original), "saving must not change the network that is saved");
        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        assertEquals(utilityNodes, utilityNodes(reread));
        assertArrayEquals(utility, utility(reread), 1E-9);
    }

    private static ProbNet read(String fileName) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/" + fileName))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    /** The class of the potential of each utility node and the names of its variables, by name of the node. */
    private static Map<String, String> utilityNodes(ProbNet probNet) {
        Map<String, String> description = new TreeMap<>();
        for (Node node : probNet.getNodes(NodeType.UTILITY)) {
            Potential potential = node.getPotentials().get(0);
            description.put(node.getName(), potential.getClass().getSimpleName() + potential.getVariables()
                                                                                              .stream()
                                                                                              .map(Variable::getName)
                                                                                              .toList());
        }
        return description;
    }

    private static double[] utility(ProbNet probNet) throws Exception {
        if (probNet.getNetworkType() instanceof DecisionAnalysisNetworkType) {
            return new DANDecompositionIntoSymmetricDANsEvaluation(probNet).getUtility().getValues();
        }
        return new VEEvaluation(probNet).getUtility().getValues();
    }
}
