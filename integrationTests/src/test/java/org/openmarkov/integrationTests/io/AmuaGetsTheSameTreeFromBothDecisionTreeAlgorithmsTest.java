/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

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
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecompositionGenerateDecisionTree;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.amua.AmuaExporter;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Where the depth runs out, the new decision tree algorithm ends a branch with the node of the next variable, a
 * decision here, and the old one with the utility node. Amua takes both as terminal nodes: in unicriterion it gets
 * the same file, and in cost-effectiveness, where those nodes depend on the willingness to pay, both are refused.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AmuaGetsTheSameTreeFromBothDecisionTreeAlgorithmsTest {

    private static ProbNet network(String name) throws Exception {
        return new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/decision-tree-examples/" + name)).probNet();
    }

    private static DecisionTreeNode oldTree(ProbNet net) throws Exception {
        return new DecompositionGenerateDecisionTree(net, 2).getDecisionTree();
    }

    private static DecisionTreeNode newTree(ProbNet net) throws Exception {
        boolean isUnicriterion = net.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType()
                == MulticriteriaOptions.Type.UNICRITERION;
        return new DecisionTreeExpansion(isUnicriterion ? EvaluationType.UNICRITERION : EvaluationType.CE)
                .expandTree(net.copy(), new EvidenceCase(), 2);
    }

    @Test void aUnicriterionTreeCutShortIsExportedAsWithTheOldAlgorithm(@TempDir Path folder) throws Exception {
        ProbNet net = network("03.4-ID-decide-test.pgmx");

        // The same file name in two folders, because Amua names the model after the file
        assertEquals(amua(oldTree(net), folder.resolve("old")), amua(newTree(net), folder.resolve("new")));
    }

    @Test void aCostEffectivenessTreeCutShortIsRefusedWithBothAlgorithms() throws Exception {
        ProbNet net = network("04.5-ID-CEA-one-test.pgmx");

        assertRefusedAt(oldTree(net), "Value");
        assertRefusedAt(newTree(net), "Therapy");
    }

    private static void assertRefusedAt(DecisionTreeNode tree, String node) {
        AmuaExporter exporter = new AmuaExporter(tree);
        assertFalse(exporter.isValidDTForAmua(), "The tree should not be exported to Amua");
        assertTrue(exporter.getValidationErrorMessage().startsWith("The node \"" + node + "\" ends the tree"),
                   exporter.getValidationErrorMessage());
    }

    private static String amua(DecisionTreeNode tree, Path folder) throws Exception {
        AmuaExporter exporter = new AmuaExporter(tree);
        assertTrue(exporter.isValidDTForAmua(), exporter.getValidationErrorMessage());
        File file = Files.createDirectories(folder).resolve("tree.amua").toFile();
        exporter.writeAmuaDT(file);
        return Files.readString(file.toPath());
    }
}
