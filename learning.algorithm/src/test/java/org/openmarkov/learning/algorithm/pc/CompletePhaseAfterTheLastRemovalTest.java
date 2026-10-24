/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.pc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.core.util.LearningEditProposal;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the interactive PC, once every removal has been applied from the table, the table shows the colliders and a
 * single «Complete phase» applies them, as completing the removals and then the colliders from the start does.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class CompletePhaseAfterTheLastRemovalTest {

    private static PCAlgorithm pcOver(String data) throws Exception {
        List<Variable> variables = new ArrayList<>();
        var database = PCAlgorithmIntegrationTest.loadCsvDatabase(data, variables);
        ProbNet probNet = PCAlgorithmIntegrationTest.buildCompleteUndirectedGraph(variables);
        PCAlgorithm pc = new PCAlgorithm(probNet, database, 0.5, new CrossEntropyIndependenceTester(), 0.05, null);
        pc.init(null);
        return pc;
    }

    private static List<String> links(ProbNet net) {
        List<String> links = new ArrayList<>();
        net.getLinks().forEach(link -> links.add(link.getFrom().getName() + (link.isDirected() ? "->" : "--")
                                                         + link.getTo().getName()));
        links.sort(null);
        return links;
    }

    @ParameterizedTest
    @CsvSource({ "network/HeadToHead2.csv", "network/BN-asia10k.csv" })
    void aSinglePressAppliesTheColliders(String data) throws Exception {
        PCAlgorithm byPhases = pcOver(data);
        byPhases.runTillNextPhase();
        byPhases.runTillNextPhase();

        PCAlgorithm byHand = pcOver(data);
        LearningEditProposal proposal = byHand.getBestEdit(true, true);
        while (proposal != null && proposal.getEdit() instanceof RemoveLinkEdit) {
            proposal.getEdit().executeEdit();
            proposal = byHand.getBestEdit(true, true);
        }
        byHand.runTillNextPhase();

        assertTrue(byHand.net().getLinks().stream().anyMatch(link -> link.isDirected()), links(byHand.net()).toString());
        assertEquals(links(byPhases.net()), links(byHand.net()));
        assertEquals(byPhases.getPhase(), byHand.getPhase());
    }
}
