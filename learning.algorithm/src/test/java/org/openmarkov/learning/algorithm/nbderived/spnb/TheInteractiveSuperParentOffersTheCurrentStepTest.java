/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.nbderived.spnb;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.pc.PCAlgorithmIntegrationTest;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.metric.cmi.accuracy.Accuracy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the interactive learning with the superparent classifier, filling the table offers alternatives for the current
 * step and does not move the search on: blocking the first proposal still offers the others, ticking «Only positive
 * edits» again gives the table of the start, and applying the first row until the table is empty learns the network
 * of the automatic learning.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheInteractiveSuperParentOffersTheCurrentStepTest {

    private static final String DATA = "network/learnTestDataBase.csv";
    private static final String CLASS = "A";

    private LearningManager manager;

    /** The algorithm as the learning window builds it, with «Unique superparent» unticked. */
    private SuperParentNBAlgorithm open() throws Exception {
        List<Variable> variables = new ArrayList<>();
        var database = PCAlgorithmIntegrationTest.loadCsvDatabase(DATA, variables);
        manager = new LearningManager(database, SuperParentNBAlgorithm.class, null, null);
        SuperParentNBAlgorithm algorithm = new SuperParentNBAlgorithm(manager.getLearnedNet(), database,
                                                                      new Accuracy(), 0.5, false);
        algorithm.setClassVariableName(CLASS);
        manager.init(algorithm);
        return algorithm;
    }

    /** What the dialog does after each change: the best edit, and then the next ones, up to 50. */
    private static List<LearningEditProposal> table(SuperParentNBAlgorithm algorithm, boolean onlyPositiveEdits) {
        List<LearningEditProposal> table = new ArrayList<>();
        LearningEditProposal proposal = algorithm.getBestEdit(true, onlyPositiveEdits);
        while (table.size() < 50 && proposal != null) {
            table.add(proposal);
            proposal = algorithm.getNextEdit(true, onlyPositiveEdits);
        }
        return table;
    }

    private static List<String> edits(List<LearningEditProposal> table) {
        return table.stream().map(proposal -> proposal.getEdit().toString()).toList();
    }

    private static List<String> linksBetweenFeatures(ProbNet net) {
        List<String> links = new ArrayList<>();
        net.getLinks().stream().filter(link -> !link.getFrom().getName().equals(CLASS))
           .forEach(link -> links.add(link.getFrom().getName() + "->" + link.getTo().getName()));
        links.sort(null);
        return links;
    }

    @Test void blockingTheFirstProposalStillOffersTheOthers() throws Exception {
        SuperParentNBAlgorithm algorithm = open();
        List<LearningEditProposal> table = table(algorithm, true);
        assertFalse(table.isEmpty());

        algorithm.blockEdit(table.getFirst());
        List<String> afterBlocking = edits(table(algorithm, true));

        assertFalse(afterBlocking.isEmpty(), "the table is empty after blocking " + table.getFirst());
        assertFalse(afterBlocking.contains(table.getFirst().getEdit().toString()));
    }

    @Test void tickingTheBoxAgainGivesTheTableOfTheStart() throws Exception {
        SuperParentNBAlgorithm algorithm = open();
        List<String> atTheStart = edits(table(algorithm, true));

        table(algorithm, false);

        assertEquals(atTheStart, edits(table(algorithm, true)));
    }

    @Test void applyingTheFirstRowLearnsTheNetworkOfTheAutomaticLearning() throws Exception {
        open();
        manager.learn();
        List<String> automatic = linksBetweenFeatures(manager.getLearnedNet());

        SuperParentNBAlgorithm algorithm = open();
        List<LearningEditProposal> table = table(algorithm, true);
        while (!table.isEmpty()) {
            table.getFirst().getEdit().executeEdit();
            table = table(algorithm, true);
        }

        assertTrue(automatic.size() > 1, automatic.toString());
        assertEquals(automatic, linksBetweenFeatures(manager.getLearnedNet()));
    }
}
