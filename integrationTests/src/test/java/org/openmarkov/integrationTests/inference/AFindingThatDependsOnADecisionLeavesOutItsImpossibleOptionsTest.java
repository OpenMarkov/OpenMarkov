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
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A finding on a variable that depends on a decision is possible with some options of the decision and not
 * with others. The evaluation and the cost-effectiveness analysis choose among the options that allow it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class AFindingThatDependsOnADecisionLeavesOutItsImpossibleOptionsTest {

    private static final double DELTA = 1E-6;
    private static final String TWO_TESTS = "/networks/id/ID-CEA-test-2therapies-new-test.pgmx";

    private static ProbNet read(String path) throws Exception {
        return new PGMXReader().read(IntegrationTest.class.getResource(path)).probNet();
    }

    private static EvidenceCase evidence(ProbNet net, String... variablesAndStates) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        for (int i = 0; i < variablesAndStates.length; i += 2) {
            Variable variable = net.getVariable(variablesAndStates[i]);
            evidence.addFinding(new Finding(variable, variable.getStateIndex(variablesAndStates[i + 1])));
        }
        return evidence;
    }

    private static VEEvaluation evaluation(ProbNet net, String... variablesAndStates) throws Exception {
        VEEvaluation evaluation = new VEEvaluation(net);
        evaluation.setPreResolutionEvidence(evidence(net, variablesAndStates));
        return evaluation;
    }

    private static CEP globalAnalysis(String path, String... variablesAndStates) throws Exception {
        ProbNet net = read(path);
        VECEAnalysis analysis = new VECEAnalysis(net);
        analysis.setPreResolutionEvidence(evidence(net, variablesAndStates));
        return analysis.getCEP();
    }

    private static void assertSameAnalysis(CEP expected, CEP actual) {
        assertFalse(actual.isZero());
        assertArrayEquals(expected.getThresholds(), actual.getThresholds(), DELTA);
        assertArrayEquals(expected.getCosts(), actual.getCosts(), DELTA);
        assertArrayEquals(expected.getEffectivities(), actual.getEffectivities(), DELTA);
    }

    /** The result of the new test is only possible if the test is done, and "no" is the first option. */
    @ParameterizedTest
    @CsvSource({"negative, 295308.343573, 0.8138", "positive, 166921.022556, 0.1862"})
    void theEvaluationIsThatOfTheOnlyPossibleOption(String result, double utility, double probability) throws Exception {
        VEEvaluation evaluation = evaluation(read(TWO_TESTS), "New test", result);
        VEEvaluation withTheOptionFixed = evaluation(read(TWO_TESTS), "New test", result, "Dec: New test", "yes");

        assertEquals(utility, evaluation.getUtility().getValues()[0], DELTA);
        assertEquals(probability, evaluation.getProbability().getValues()[0], DELTA);
        assertEquals(withTheOptionFixed.getUtility().getValues()[0], evaluation.getUtility().getValues()[0], DELTA);
        assertEquals(withTheOptionFixed.getProbability().getValues()[0], evaluation.getProbability().getValues()[0], DELTA);
    }

    @Test
    void theProbabilityOfTheEvidenceIsThatOfThePossibleOption() throws Exception {
        assertEquals(0.1754, evaluation(read(TWO_TESTS), "Test", "positive").getProbability().getValues()[0], DELTA);
    }

    @ParameterizedTest
    @CsvSource({"negative", "positive"})
    void theCostEffectivenessAnalysisIsThatOfTheOnlyPossibleOption(String result) throws Exception {
        assertSameAnalysis(globalAnalysis(TWO_TESTS, "New test", result, "Dec: New test", "yes"),
                           globalAnalysis(TWO_TESTS, "New test", result));
    }

    /** Here the partition of the impossible option is not the one of probability zero. */
    @ParameterizedTest
    @CsvSource({"not-performed, no", "negative, yes", "positive, yes"})
    void anImpossibleOptionWithCostAndEffectivenessIsLeftOutToo(String result, String option) throws Exception {
        String path = "/networks/id/ID-CEA-test-2therapies.pgmx";

        assertSameAnalysis(globalAnalysis(path, "Test", result, "Dec:Test", option), globalAnalysis(path, "Test", result));
    }

    /** With a test that gives a result even when it is not done, both options are possible, with different probability. */
    @Test
    void amongSeveralPossibleOptionsTheBestOneIsChosenWithItsProbability() throws Exception {
        String text;
        try (InputStream in = getClass().getResourceAsStream("/networks/id/ID-decide-test.pgmx")) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String table = "1.0 0.0 0.0 0.0 0.97 0.03 1.0 0.0 0.0 0.0 0.09 0.91";
        assertTrue(text.contains(table));
        Path file = Files.createTempFile("both-options-possible", ".pgmx");
        Files.writeString(file, text.replace(table, "0.5 0.4 0.1 0.0 0.97 0.03 0.5 0.1 0.4 0.0 0.09 0.91"));

        VEEvaluation evaluation = evaluation(new PGMXReader().read(file.toUri().toURL()).probNet(),
                                             "Result of test", "positive");
        VEEvaluation withoutTest = evaluation(new PGMXReader().read(file.toUri().toURL()).probNet(),
                                              "Result of test", "positive", "Do test?", "no");
        VEEvaluation withTest = evaluation(new PGMXReader().read(file.toUri().toURL()).probNet(),
                                           "Result of test", "positive", "Do test?", "yes");
        Files.delete(file);

        double utilityWithoutTest = withoutTest.getUtility().getValues()[0];
        double utilityWithTest = withTest.getUtility().getValues()[0];
        VEEvaluation best = utilityWithTest > utilityWithoutTest ? withTest : withoutTest;
        assertEquals(0.142, withoutTest.getProbability().getValues()[0], DELTA);
        assertEquals(0.1532, withTest.getProbability().getValues()[0], DELTA);
        assertEquals(Math.max(utilityWithTest, utilityWithoutTest), evaluation.getUtility().getValues()[0], DELTA);
        assertEquals(best.getProbability().getValues()[0], evaluation.getProbability().getValues()[0], DELTA);
    }
}
