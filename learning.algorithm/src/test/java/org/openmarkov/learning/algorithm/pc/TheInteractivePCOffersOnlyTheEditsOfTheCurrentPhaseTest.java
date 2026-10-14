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
 * What the dialog of interactive learning does with PC: it fills its table asking for the best edit and then for the
 * next ones, up to 50. The table holds only the edits of the current phase, and filling it does not move the
 * algorithm: at the start, the removals of links that build the skeleton.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheInteractivePCOffersOnlyTheEditsOfTheCurrentPhaseTest {

    private static PCAlgorithm pcOver(String data) throws Exception {
        List<Variable> variables = new ArrayList<>();
        var database = PCAlgorithmIntegrationTest.loadCsvDatabase(data, variables);
        ProbNet probNet = PCAlgorithmIntegrationTest.buildCompleteUndirectedGraph(variables);
        PCAlgorithm pc = new PCAlgorithm(probNet, database, 0.5, new CrossEntropyIndependenceTester(), 0.05, null);
        pc.init(null);
        return pc;
    }

    private static List<LearningEditProposal> table(PCAlgorithm pc, boolean onlyPositiveEdits) {
        List<LearningEditProposal> table = new ArrayList<>();
        LearningEditProposal proposal = pc.getBestEdit(true, onlyPositiveEdits);
        while (table.size() < 50 && proposal != null) {
            table.add(proposal);
            proposal = pc.getNextEdit(true, onlyPositiveEdits);
        }
        return table;
    }

    private static void assertOnlyRemovals(List<LearningEditProposal> table) {
        assertTrue(!table.isEmpty() && table.stream().allMatch(proposal -> proposal.getEdit() instanceof RemoveLinkEdit),
                   table.toString());
    }

    @ParameterizedTest
    @CsvSource({ "network/HeadToHead2.csv", "network/BN-asia10k.csv" })
    void whenTheTableIsFilledAgain(String data) throws Exception {
        PCAlgorithm pc = pcOver(data);

        assertOnlyRemovals(table(pc, true));
        assertOnlyRemovals(table(pc, false));
        assertOnlyRemovals(table(pc, true));
        assertEquals(PCAlgorithm.Phase.INITIAL_PHASE.ordinal(), pc.getPhase());
    }

    /** «Complete phase» after the table is filled: the links of the skeleton are removed, and nothing is oriented. */
    @ParameterizedTest
    @CsvSource({ "network/HeadToHead2.csv, 5", "network/BN-asia10k.csv, 7" })
    void whenThePhaseIsCompleted(String data, int linksOfTheSkeleton) throws Exception {
        PCAlgorithm pc = pcOver(data);
        table(pc, true);

        pc.runTillNextPhase();

        assertEquals(linksOfTheSkeleton, pc.net().getLinks().size());
        assertTrue(pc.net().getLinks().stream().noneMatch(link -> link.isDirected()));
        assertEquals(PCAlgorithm.Phase.HEAD_TO_HEAD_ORIENTATION.ordinal(), pc.getPhase());
    }
}
