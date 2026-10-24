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

/**
 * In the interactive PC, applying the last removal that improves the network with «Only positive edits» ticked does
 * not move the algorithm to the orientation of colliders: unticking the box afterwards still offers the removals that
 * are left, the same links as when it is unticked one removal earlier.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class LookingAtTheTableDoesNotMoveThePhaseTest {

    private static PCAlgorithm pcOver(String data) throws Exception {
        List<Variable> variables = new ArrayList<>();
        var database = PCAlgorithmIntegrationTest.loadCsvDatabase(data, variables);
        ProbNet probNet = PCAlgorithmIntegrationTest.buildCompleteUndirectedGraph(variables);
        PCAlgorithm pc = new PCAlgorithm(probNet, database, 0.5, new CrossEntropyIndependenceTester(), 0.05, null);
        pc.init(null);
        return pc;
    }

    /**
     * The edits of the table, in alphabetical order, filled as the dialog does after each change: the best edit, and
     * then the next ones, up to 50.
     */
    private static List<String> table(PCAlgorithm pc, boolean onlyPositiveEdits) {
        List<String> table = new ArrayList<>();
        LearningEditProposal proposal = pc.getBestEdit(true, onlyPositiveEdits);
        while (table.size() < 50 && proposal != null) {
            table.add(proposal.getEdit().toString());
            proposal = pc.getNextEdit(true, onlyPositiveEdits);
        }
        table.sort(null);
        return table;
    }

    /**
     * Applies the removals that improve the network, asking each time for the best edit with both boxes ticked, as
     * the dialog does after each «Apply edit»; with {@code askAfterTheLast} false the box is unticked before the
     * dialog asks again after the last one. Returns how many it applied.
     */
    private static int applyTheRemovals(PCAlgorithm pc, int howMany, boolean askAfterTheLast) throws Exception {
        int applied = 0;
        LearningEditProposal proposal = pc.getBestEdit(true, true);
        while (proposal != null && proposal.getEdit() instanceof RemoveLinkEdit && applied < howMany) {
            proposal.getEdit().executeEdit();
            applied++;
            if (applied < howMany || askAfterTheLast) {
                proposal = pc.getBestEdit(true, true);
            }
        }
        return applied;
    }

    @ParameterizedTest
    @CsvSource({ "network/HeadToHead2.csv, 3", "network/BN-asia10k.csv, 6" })
    void unticking(String data, int removalsLeft) throws Exception {
        PCAlgorithm afterTheLast = pcOver(data);
        int removals = applyTheRemovals(afterTheLast, Integer.MAX_VALUE, true);
        PCAlgorithm beforeTheLast = pcOver(data);
        applyTheRemovals(beforeTheLast, removals, false);

        List<String> table = table(afterTheLast, false);

        assertEquals(PCAlgorithm.Phase.INITIAL_PHASE.ordinal(), afterTheLast.getPhase());
        assertEquals(table(beforeTheLast, false), table);
        assertEquals(removalsLeft, table.stream().filter(row -> row.startsWith("Remove")).count(), table.toString());
    }
}
