/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.DecisionTreeExpansion;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.amua.AmuaExporter;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Amua computes, by its expected value, a tree with a decision below another, so a tree with one criterion is
 * exported with all its decisions. It takes the options of the first node as the strategies to compare, so a tree
 * that begins with a chance node is refused saying so. With cost and effectiveness, one decision is the limit.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ATreeWithSeveralDecisionsIsExportedToAmuaTest {

    private static ProbNet example(String name) throws Exception {
        return new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/decision-tree-examples/" + name)).probNet();
    }

    private static ProbNet ofTheCollection(String endOfPath) throws Exception {
        URL url = Networks.getNetworks().filter(network -> network.getPath().endsWith(endOfPath)).findFirst().orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    /** The whole tree of the network, in the analysis that the network asks for. */
    private static AmuaExporter exporterOfTheTreeOf(ProbNet net) throws Exception {
        boolean isUnicriterion = net.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType()
                == MulticriteriaOptions.Type.UNICRITERION;
        DecisionTreeNode tree = new DecisionTreeExpansion(isUnicriterion ? EvaluationType.UNICRITERION : EvaluationType.CE)
                .expandTree(net.copy(), new EvidenceCase(), 5);
        return new AmuaExporter(tree);
    }

    private static void assertExported(ProbNet net, Path folder) throws Exception {
        AmuaExporter exporter = exporterOfTheTreeOf(net);
        assertTrue(exporter.isValidDTForAmua(), exporter.getValidationErrorMessage());
        File file = folder.resolve("tree.amua").toFile();
        exporter.writeAmuaDT(file);
        assertTrue(file.length() > 0);
    }

    private static String refusalOf(ProbNet net) throws Exception {
        AmuaExporter exporter = exporterOfTheTreeOf(net);
        assertFalse(exporter.isValidDTForAmua(), "The tree should not be exported to Amua");
        return exporter.getValidationErrorMessage();
    }

    @Test void aTreeWithATestAndATherapyIsExported(@TempDir Path folder) throws Exception {
        assertExported(ofTheCollection("/id/ID-decide-test.pgmx"), folder);
    }

    @Test void aTreeWithThreeDecisionsIsExported(@TempDir Path folder) throws Exception {
        assertExported(ofTheCollection("/id/ID-used-car-buyer.pgmx"), folder);
    }

    @Test void aTreeThatBeginsWithAChanceNodeIsRefusedSayingSo() throws Exception {
        assertEquals("The decision \"Therapy (T)\" must be the first node of the tree, but the tree begins with "
                     + "\"Result of test (Y)\". Amua compares the options of the first node as strategies.",
                     refusalOf(example("03.3-ID-indirect-evidence.pgmx")));
    }

    @Test void aCostEffectivenessTreeThatBeginsWithAChanceNodeIsRefusedSayingSo() throws Exception {
        assertEquals("The decision \"Therapy\" must be the first node of the tree, but the tree begins with "
                     + "\"Disease\". Amua compares the options of the first node as strategies.",
                     refusalOf(example("04.3-ID-CEA-disease-known.pgmx")));
    }

    @Test void aCostEffectivenessTreeWithADecisionBelowAnotherIsRefused() throws Exception {
        assertEquals("In a cost-effectiveness analysis, Amua supports only one decision node.",
                     refusalOf(example("04.5-ID-CEA-one-test.pgmx")));
    }
}
