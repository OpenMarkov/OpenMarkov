/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.operation.CEBaseOperations;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A finding on a variable that depends on a decision makes some of its options impossible. The
 * cost-effectiveness analysis compares the other options only.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class TheCostEffectivenessAnalysisLeavesOutImpossibleOptionsTest {

    private static final double DELTA = 1E-6;

    private static CEP globalAnalysis(String... variablesAndStates) throws Exception {
        ProbNet net = new PGMXReader().read(IntegrationTest.class.getResource("/networks/id/ID-CEA-test-2therapies-new-test.pgmx"))
                                      .probNet();
        EvidenceCase evidence = new EvidenceCase();
        for (int i = 0; i < variablesAndStates.length; i += 2) {
            Variable variable = net.getVariable(variablesAndStates[i]);
            evidence.addFinding(new Finding(variable, variable.getStateIndex(variablesAndStates[i + 1])));
        }
        VECEAnalysis analysis = new VECEAnalysis(net);
        analysis.setPreResolutionEvidence(evidence);
        return analysis.getCEP();
    }

    /** The result of the test is known, and only one option of the decision about the test allows it. */
    @ParameterizedTest
    @CsvSource({"not done, no", "negative, yes", "positive, yes"})
    void theAnalysisIsThatOfTheOnlyPossibleOption(String result, String option) throws Exception {
        CEP analysis = globalAnalysis("Test", result);
        CEP withTheOptionFixed = globalAnalysis("Test", result, "Dec: Test", option);

        assertFalse(analysis.isZero());
        assertArrayEquals(withTheOptionFixed.getThresholds(), analysis.getThresholds(), DELTA);
        assertArrayEquals(withTheOptionFixed.getCosts(), analysis.getCosts(), DELTA);
        assertArrayEquals(withTheOptionFixed.getEffectivities(), analysis.getEffectivities(), DELTA);
    }

    @Test
    void theBestPartitionIsThatOfTheOptionThatIsPossible() throws Exception {
        Variable decision = new Variable("D", "no", "yes");
        CEP possible = new CEP(new StrategyTree[]{null}, new double[]{10}, new double[]{2}, null, 0,
                               Double.POSITIVE_INFINITY);

        CEP ofTheFirst = CEBaseOperations.optimalCEP(decision, List.of(possible, CEP.getZeroPartition()));
        CEP ofTheSecond = CEBaseOperations.optimalCEP(decision, List.of(CEP.getZeroPartition(), possible));

        assertArrayEquals(new double[]{10}, ofTheFirst.getCosts(), DELTA);
        assertArrayEquals(new double[]{2}, ofTheSecond.getEffectivities(), DELTA);
        assertTrue(ofTheFirst.getIntervention(0).toString().contains("no"), ofTheFirst.getIntervention(0).toString());
        assertTrue(ofTheSecond.getIntervention(0).toString().contains("yes"), ofTheSecond.getIntervention(0).toString());
        assertTrue(CEBaseOperations.optimalCEP(decision, List.of(CEP.getZeroPartition(), CEP.getZeroPartition())).isZero());
    }
}
