/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.pc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.OrientLinkEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.core.util.LearningEditProposal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the interactive PC, the table never offers the opposite of an orientation that a Meek rule forces, and still
 * offers both directions of a link that no rule orients.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheOppositeOfAMeekOrientationIsNotOfferedTest {

    @Test void alongTheWholeLearning() throws Exception {
        List<Variable> variables = new ArrayList<>();
        var database = PCAlgorithmIntegrationTest.loadCsvDatabase("network/BN-asia10k.csv", variables);
        ProbNet probNet = PCAlgorithmIntegrationTest.buildCompleteUndirectedGraph(variables);
        PCAlgorithm pc = new PCAlgorithm(probNet, database, 0.5, new CrossEntropyIndependenceTester(), 0.05, null);
        pc.init(null);

        boolean bothDirectionsOfAFreeLink = false;
        List<LearningEditProposal> table = table(pc);
        while (!table.isEmpty()) {
            Set<String> orientations = new HashSet<>();
            Set<String> forced = new HashSet<>();
            for (LearningEditProposal row : table) {
                if (row.getEdit() instanceof OrientLinkEdit orientation) {
                    String link = orientation.getVariableFrom().getName() + "->" + orientation.getVariableTo().getName();
                    orientations.add(link);
                    if (row.getMotivation().toString().startsWith("Meek")) {
                        forced.add(link);
                    }
                }
            }
            for (String link : forced) {
                assertFalse(orientations.contains(opposite(link)), "the table offers the opposite of " + link + ": " + table);
            }
            for (String link : orientations) {
                if (!forced.contains(link) && orientations.contains(opposite(link)) && !forced.contains(opposite(link))) {
                    bothDirectionsOfAFreeLink = true;
                }
            }
            table.getFirst().getEdit().executeEdit();
            table = table(pc);
        }
        assertTrue(bothDirectionsOfAFreeLink, "no table offered both directions of a link that no rule orients");
    }

    /** What the dialog does after each change: the best edit, and then the next ones, up to 50. */
    private static List<LearningEditProposal> table(PCAlgorithm pc) {
        List<LearningEditProposal> table = new ArrayList<>();
        LearningEditProposal proposal = pc.getBestEdit(true, true);
        while (table.size() < 50 && proposal != null) {
            table.add(proposal);
            proposal = pc.getNextEdit(true, true);
        }
        return table;
    }

    private static String opposite(String link) {
        String[] ends = link.split("->");
        return ends[1] + "->" + ends[0];
    }
}
