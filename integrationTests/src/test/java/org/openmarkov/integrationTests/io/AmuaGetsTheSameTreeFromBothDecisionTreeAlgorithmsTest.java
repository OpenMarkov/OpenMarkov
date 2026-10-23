/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Where the depth runs out, the new decision tree algorithm ends a branch with the node of the next variable, a
 * decision here, and the old one with the utility node. Amua takes both as terminal nodes, so it gets the same file.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AmuaGetsTheSameTreeFromBothDecisionTreeAlgorithmsTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource({ "03.4-ID-decide-test.pgmx", "04.5-ID-CEA-one-test.pgmx" })
    void aTreeCutShortIsExportedAsWithTheOldAlgorithm(String network, @TempDir Path folder) throws Exception {
        ProbNet net = new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/decision-tree-examples/" + network)).probNet();
        boolean isUnicriterion = net.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType()
                == MulticriteriaOptions.Type.UNICRITERION;

        DecisionTreeNode oldTree = new DecompositionGenerateDecisionTree(net, 2).getDecisionTree();
        DecisionTreeNode newTree = new DecisionTreeExpansion(isUnicriterion ? EvaluationType.UNICRITERION
                                                                            : EvaluationType.CE)
                .expandTree(net.copy(), new EvidenceCase(), 2);

        // The same file name in two folders, because Amua names the model after the file
        assertEquals(amua(oldTree, folder.resolve("old")), amua(newTree, folder.resolve("new")));
    }

    private static String amua(DecisionTreeNode tree, Path folder) throws Exception {
        AmuaExporter exporter = new AmuaExporter(tree);
        assertTrue(exporter.isValidDTForAmua(), exporter.getValidationErrorMessage());
        File file = Files.createDirectories(folder).resolve("tree.amua").toFile();
        exporter.writeAmuaDT(file);
        return Files.readString(file.toPath());
    }
}
