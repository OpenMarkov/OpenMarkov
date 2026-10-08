/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEExpectedUtilityDecision;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The expected utility of a decision is a table on the decision and its informational predecessors. When the
 * utility does not depend on some of them, the table repeats its value for each of their states.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class TheExpectedUtilityOfADecisionHasAValueForEveryConfigurationTest {

    private static final double DELTA = 1E-6;

    private static TablePotential expectedUtility(String path, String decision) throws Exception {
        ProbNet net = new PGMXReader().read(IntegrationTest.class.getResource("/networks" + path)).probNet();
        return new VEExpectedUtilityDecision(net, net.getVariable(decision)).getExpectedUtility();
    }

    @Test
    void theEvidenceEnteredBeforeTheResolutionIsUsed() throws Exception {
        ProbNet net = new PGMXReader().read(IntegrationTest.class.getResource("/networks/id/ID-decide-test.pgmx")).probNet();
        EvidenceCase diseasePresent = new EvidenceCase();
        diseasePresent.addFinding(new Finding(net.getVariable("Disease"), 1));
        VEExpectedUtilityDecision task = new VEExpectedUtilityDecision(net, net.getVariable("Do test?"));
        task.setPreResolutionEvidence(diseasePresent);

        assertArrayEquals(new double[]{7.25, 7.05}, task.getExpectedUtility().getValues(), DELTA);
    }

    private static List<String> names(TablePotential potential) {
        return potential.getVariables().stream().map(Variable::getName).toList();
    }

    @Test
    void aNetworkWithoutUtilityGivesZeroForEachOption() throws Exception {
        TablePotential utility = expectedUtility("/id/ID-only-decision-no-utility.pgmx", "D");

        assertEquals(List.of("D"), names(utility));
        assertArrayEquals(new double[]{0, 0}, utility.getValues(), DELTA);
    }

    @Test
    void aDecisionThatDoesNotChangeTheUtilityGivesTheSameValueForEachOption() throws Exception {
        TablePotential utility = expectedUtility("/id/ID-three-dec-two-util.pgmx", "D");

        assertEquals(List.of("D"), names(utility));
        assertArrayEquals(new double[]{5, 5}, utility.getValues(), DELTA);
    }

    @Test
    void theValueIsRepeatedForEachStateOfAPredecessorThatDoesNotChangeTheUtility() throws Exception {
        TablePotential ofD1 = expectedUtility("/id/ID-three-dec-two-util.pgmx", "D1");
        TablePotential ofD2 = expectedUtility("/id/ID-three-dec-two-util.pgmx", "D2");

        assertEquals(List.of("D1", "D"), names(ofD1));
        assertArrayEquals(new double[]{4, 5, 4, 5}, ofD1.getValues(), DELTA);
        assertEquals(List.of("D2", "D", "D1"), names(ofD2));
        assertArrayEquals(new double[]{4, 1, 4, 1, 5, 2, 5, 2}, ofD2.getValues(), DELTA);
    }

    @Test
    void aDecisionAnalysisNetworkGivesAValueForEveryConfiguration() throws Exception {
        TablePotential ofAsk = expectedUtility("/dan/DAN-dating.pgmx", "Ask?");
        TablePotential ofNightClub = expectedUtility("/dan/DAN-dating.pgmx", "NClub?");

        assertArrayEquals(new double[]{7.52, 7.52}, ofAsk.getValues(), DELTA);
        assertEquals(List.of("NClub?", "Ask?", "Accept"), names(ofNightClub));
        assertArrayEquals(new double[]{0.52, 16.82304, 0.52, 16.82304, 0.52, 16.82304, 0.52, 16.82304},
                          ofNightClub.getValues(), DELTA);
    }
}
