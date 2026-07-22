/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.util.LearningEditMotivation;
import org.openmarkov.learning.core.util.LearningEditProposal;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for {@link LearningEvaluator}, focused on P2: the algorithm configured by the user must
 * actually be used to learn every fold (previously the options were silently ignored).
 *
 * <p>The tests use a no-op {@link RecordingAlgorithm} double so they exercise the wiring without
 * depending on any real structure-learning result, and no graphical interface is involved.
 */
class LearningEvaluatorTest {

    /**
     * A learning-algorithm double that proposes no structural edits (so {@code run()} only does
     * parametric learning on the skeleton) and records the class-variable name it was configured
     * with. It has the {@code (ProbNet, CaseDatabase)} constructor required by the framework, so
     * it can also be instantiated reflectively for the default-parameters path.
     */
    public static class RecordingAlgorithm extends LearningAlgorithm {
        String classVariableSet;

        public RecordingAlgorithm(ProbNet probNet, CaseDatabase caseDatabase) {
            super(probNet, caseDatabase, 0.5);
        }

        @Override public void setClassVariableName(String classVariableName) {
            this.classVariableSet = classVariableName;
            super.setClassVariableName(classVariableName);
        }

        @Override public LearningEditProposal getBestEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {
            return null;
        }

        @Override public LearningEditProposal getNextEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {
            return null;
        }

        @Override public LearningEditMotivation getMotivation(PNEdit edit) {
            return null;
        }
    }

    /** A database of {@code numCases} cases over a 2-state class variable and a 2-state feature. */
    private static CaseDatabase database(int numCases) {
        Variable classVar = new Variable("Class", 2);
        Variable feature = new Variable("X", 2);
        int[][] cases = new int[numCases][2];
        for (int i = 0; i < numCases; i++) {
            cases[i][0] = i % 2;          // both class states appear
            cases[i][1] = (i / 2) % 2;    // both feature states appear
        }
        return new CaseDatabase(List.of(classVar, feature), cases);
    }

    private static MeasuresSet logLikelihoodTemplate() {
        MeasuresSet template = new MeasuresSet("cross-validation");
        template.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));
        return template;
    }

    /**
     * Core P2 guarantee: the algorithm factory (in production, the user's options dialog) is
     * invoked exactly once per fold, with that fold's own training database and a fresh skeleton,
     * and the configured class variable reaches every algorithm instance. Checked across several
     * (numCases, K) shapes, including uneven folds.
     */
    @Test
    void factoryIsInvokedOncePerFoldWithThatFoldsTrainingDatabase() throws Exception {
        int[][] shapes = { {12, 4}, {10, 5}, {9, 3}, {8, 2}, {13, 4} };
        for (int[] shape : shapes) {
            int numCases = shape[0];
            int k = shape[1];
            String tag = "numCases=" + numCases + ", K=" + k;

            CaseDatabase db = database(numCases);
            SplitSet[] sets = new SplitSetManager(db, 123L).crossValidation(k);

            List<CaseDatabase> seenTrain = new ArrayList<>();
            List<ProbNet> seenSkeleton = new ArrayList<>();
            List<RecordingAlgorithm> produced = new ArrayList<>();
            LearningEvaluator.LearningAlgorithmFactory factory = (skeleton, train) -> {
                seenSkeleton.add(skeleton);
                seenTrain.add(train);
                RecordingAlgorithm algorithm = new RecordingAlgorithm(skeleton, train);
                produced.add(algorithm);
                return algorithm;
            };

            LearningEvaluator evaluator =
                    new LearningEvaluator(RecordingAlgorithm.class, factory, sets, logLikelihoodTemplate());
            evaluator.setVariable("Class");
            MeasuresSet result = evaluator.runEvaluator();

            // exactly one algorithm was built per fold
            assertThat(produced).as(tag).hasSize(k);
            for (int i = 0; i < k; i++) {
                // the factory received precisely this fold's training database, not the whole set
                assertThat(seenTrain.get(i)).as(tag + " - train db of fold " + i)
                        .isSameAs(sets[i].getTrainDatabase());
                // and a skeleton that already carries the database variables
                assertThat(seenSkeleton.get(i).getVariable("Class")).as(tag + " - skeleton Class " + i).isNotNull();
                assertThat(seenSkeleton.get(i).getVariable("X")).as(tag + " - skeleton X " + i).isNotNull();
            }
            // the configured class variable reached every algorithm instance
            assertThat(produced).as(tag).allSatisfy(a -> assertThat(a.classVariableSet).isEqualTo("Class"));
            // the run accumulated one iteration per fold and returned a result
            assertThat(result).as(tag).isNotNull();
            assertThat(result.getNumIterations()).as(tag + " - iterations").isEqualTo(k);
        }
    }

    /**
     * When no options dialog is available ({@code null} factory), the evaluator must fall back to
     * the algorithm's default instantiation and still complete without error.
     */
    @Test
    void nullFactoryFallsBackToDefaultInstantiationAndCompletes() {
        CaseDatabase db = database(8);
        SplitSet[] sets = new SplitSetManager(db, 5L).crossValidation(2);

        LearningEvaluator evaluator =
                new LearningEvaluator(RecordingAlgorithm.class, null, sets, logLikelihoodTemplate());

        MeasuresSet result = assertDoesNotThrow(evaluator::runEvaluator);
        assertThat(result).isNotNull();
        assertThat(result.getNumIterations()).isEqualTo(2);
    }
}
