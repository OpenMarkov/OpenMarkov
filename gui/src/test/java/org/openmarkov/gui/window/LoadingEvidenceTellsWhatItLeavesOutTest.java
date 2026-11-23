/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Load evidence» on a network with the variables Smoker and Cancer, both with the states no and yes. What cases of
 * a database it loads, and what it tells of the columns and the values that give no finding.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class LoadingEvidenceTellsWhatItLeavesOutTest {

    private ProbNet net;

    @BeforeEach void theNetwork() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.addNode(new Variable("Smoker", "no", "yes"), NodeType.CHANCE);
        net.addNode(new Variable("Cancer", "no", "yes"), NodeType.CHANCE);
    }

    /** The findings of each case, as «Smoker=yes». */
    private static List<List<String>> findings(List<EvidenceCase> cases) {
        return cases.stream().map(evidenceCase -> evidenceCase.getFindings().stream().map(
                finding -> finding.getVariable().getName() + "=" + finding.getState()).sorted().toList()).toList();
    }

    @Test void aDatabaseThatMatchesTheNetworkIsLoadedWithoutWarnings() throws Exception {
        CaseDatabase database = new CaseDatabase(
                List.of(new Variable("Smoker", "no", "yes"), new Variable("Cancer", "?", "yes")),
                new int[][] { { 1, 1 }, { 0, 0 } });

        EvidenceOfADatabase evidence = EvidenceOfADatabase.of(net, database);

        assertEquals(List.of(List.of("Cancer=yes", "Smoker=yes"), List.of("Smoker=no")), findings(evidence.cases()));
        assertEquals(List.of(), evidence.leftOut());
    }

    @Test void aColumnThatTheNetworkDoesNotHaveIsLeftOutAndToldOf() throws Exception {
        CaseDatabase database = new CaseDatabase(
                List.of(new Variable("Id", "1", "2"), new Variable("Smoker", "no", "yes")),
                new int[][] { { 0, 1 }, { 1, 0 } });

        EvidenceOfADatabase evidence = EvidenceOfADatabase.of(net, database);

        assertEquals(List.of(List.of("Smoker=yes"), List.of("Smoker=no")), findings(evidence.cases()));
        assertEquals(List.of("Column Id: the network has no variable with that name."), evidence.leftOut());
    }

    @Test void theValuesThatAreNotStatesAreCountedAndARowWithoutFindingsIsNotACase() throws Exception {
        CaseDatabase database = new CaseDatabase(
                List.of(new Variable("Smoker", "Yes", "no", "yes"), new Variable("Cancer", "NA", "Yes", "no")),
                new int[][] { { 0, 1 }, { 2, 0 }, { 0, 2 }, { 1, 2 } });

        EvidenceOfADatabase evidence = EvidenceOfADatabase.of(net, database);

        assertEquals(List.of(List.of("Smoker=yes"), List.of("Cancer=no"), List.of("Cancer=no", "Smoker=no")),
                     findings(evidence.cases()));
        assertEquals(List.of("Smoker: «Yes» is not one of its states (2 times).",
                             "Cancer: «Yes» is not one of its states (once).",
                             "Cancer: «NA» is not one of its states (once).",
                             "1 row gave no finding and has not been added."), evidence.leftOut());
    }

    @Test void aDatabaseWithNothingOfTheNetworkGivesNoCases() throws Exception {
        CaseDatabase database = new CaseDatabase(List.of(new Variable("Smoker", "No", "Yes")),
                                                 new int[][] { { 0 }, { 1 }, { 1 } });

        EvidenceOfADatabase evidence = EvidenceOfADatabase.of(net, database);

        assertEquals(List.of(), evidence.cases());
        assertEquals(List.of("Smoker: «No» is not one of its states (once).",
                             "Smoker: «Yes» is not one of its states (2 times).",
                             "3 rows gave no finding and have not been added."), evidence.leftOut());
    }
}
