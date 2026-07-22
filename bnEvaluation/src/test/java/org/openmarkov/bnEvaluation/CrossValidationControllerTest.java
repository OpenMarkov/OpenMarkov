/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link CrossValidationController}, the evaluation orchestration extracted from
 * {@code CrossValidationDialog} (F3). It reuses the no-op {@link LearningEvaluatorTest.RecordingAlgorithm}
 * double, so the wiring is exercised without any real structure learning or user interface: split
 * mode routing, seed reproducibility through the controller, and the class-variable pass-through.
 *
 * @author Manuel Arias
 */
class CrossValidationControllerTest {

    /** A database of {@code numCases} cases over a 2-state class variable and a 2-state feature. */
    private static CaseDatabase database(int numCases) {
        Variable classVar = new Variable("Class", 2);
        Variable feature = new Variable("X", 2);
        int[][] cases = new int[numCases][2];
        for (int i = 0; i < numCases; i++) {
            cases[i][0] = i % 2;
            cases[i][1] = (i / 2) % 2;
        }
        return new CaseDatabase(List.of(classVar, feature), cases);
    }

    private static MeasuresSet logLikelihoodTemplate() {
        MeasuresSet template = new MeasuresSet("cross-validation");
        template.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));
        return template;
    }

    @Test
    void crossValidationRunsOneIterationPerFoldWithTheClassVariable() throws Exception {
        List<LearningEvaluatorTest.RecordingAlgorithm> produced = new ArrayList<>();
        LearningEvaluator.LearningAlgorithmFactory factory = recordInto(produced);

        CrossValidationController.Request request = new CrossValidationController.Request(
                database(12), LearningEvaluatorTest.RecordingAlgorithm.class, factory,
                CrossValidationController.SplitMode.CROSS_VALIDATION,
                4, 0, 0, true, 123L, logLikelihoodTemplate(), "Class");
        MeasuresSet result = CrossValidationController.run(request);

        assertThat(produced).hasSize(4);
        assertThat(result.getNumIterations()).isEqualTo(4);
        assertThat(produced).allSatisfy(a -> assertThat(a.classVariableSet).isEqualTo("Class"));
    }

    @Test
    void multipleSamplesRunsOneIterationPerSample() throws Exception {
        List<LearningEvaluatorTest.RecordingAlgorithm> produced = new ArrayList<>();
        LearningEvaluator.LearningAlgorithmFactory factory = recordInto(produced);

        CrossValidationController.Request request = new CrossValidationController.Request(
                database(10), LearningEvaluatorTest.RecordingAlgorithm.class, factory,
                CrossValidationController.SplitMode.MULTIPLE_SAMPLES,
                0, 3, 4, true, 7L, logLikelihoodTemplate(), "Class");
        MeasuresSet result = CrossValidationController.run(request);

        assertThat(produced).hasSize(3);
        assertThat(result.getNumIterations()).isEqualTo(3);
    }

    @Test
    void reproducibleSeedGivesIdenticalSplitsThroughTheController() throws Exception {
        List<int[][]> firstRun = trainCasesPerFold(3, 42L);
        List<int[][]> secondRun = trainCasesPerFold(3, 42L);

        assertEquals(firstRun.size(), secondRun.size());
        for (int i = 0; i < firstRun.size(); i++) {
            assertTrue(Arrays.deepEquals(firstRun.get(i), secondRun.get(i)),
                    "fold " + i + " training set should be identical for the same seed");
        }
    }

    @Test
    void generativeRequestDoesNotSetAClassVariable() throws Exception {
        List<LearningEvaluatorTest.RecordingAlgorithm> produced = new ArrayList<>();
        LearningEvaluator.LearningAlgorithmFactory factory = recordInto(produced);

        CrossValidationController.Request request = new CrossValidationController.Request(
                database(8), LearningEvaluatorTest.RecordingAlgorithm.class, factory,
                CrossValidationController.SplitMode.CROSS_VALIDATION,
                2, 0, 0, true, 5L, logLikelihoodTemplate(), null);   // no class variable
        CrossValidationController.run(request);

        assertThat(produced).isNotEmpty();
        assertThat(produced).allSatisfy(a -> assertThat(a.classVariableSet).isNull());
    }

    // ---- helpers ----------------------------------------------------------

    private static LearningEvaluator.LearningAlgorithmFactory recordInto(
            List<LearningEvaluatorTest.RecordingAlgorithm> produced) {
        return (skeleton, train) -> {
            LearningEvaluatorTest.RecordingAlgorithm algorithm =
                    new LearningEvaluatorTest.RecordingAlgorithm(skeleton, train);
            produced.add(algorithm);
            return algorithm;
        };
    }

    /** Runs a cross-validation and returns the training-case grid seen for each fold. */
    private static List<int[][]> trainCasesPerFold(int folds, long seed) throws Exception {
        List<int[][]> trainCases = new ArrayList<>();
        LearningEvaluator.LearningAlgorithmFactory factory = (skeleton, train) -> {
            trainCases.add(train.getCases());
            return new LearningEvaluatorTest.RecordingAlgorithm(skeleton, train);
        };
        CrossValidationController.run(new CrossValidationController.Request(
                database(9), LearningEvaluatorTest.RecordingAlgorithm.class, factory,
                CrossValidationController.SplitMode.CROSS_VALIDATION,
                folds, 0, 0, true, seed, logLikelihoodTemplate(), "Class"));
        return trainCases;
    }
}
