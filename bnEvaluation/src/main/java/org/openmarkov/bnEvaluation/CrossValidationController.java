/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;

/**
 * Controller that runs a cross-validation (or repeated-sampling) evaluation from parameters already
 * read off the dialog, keeping the orchestration out of the view (model-view-controller separation).
 * It splits the database, learns and evaluates a network per iteration through
 * {@link LearningEvaluator}, and returns the accumulated {@link MeasuresSet}. It has no dependency on
 * Swing, so it can be unit-tested — and later run off the event-dispatch thread (phase F4).
 *
 * @author Manuel Arias
 */
public final class CrossValidationController {

    /** How the database is partitioned for each evaluation iteration. */
    public enum SplitMode {
        /** K disjoint folds; each is the test set once, the rest the training set. */
        CROSS_VALIDATION,
        /** {@code numberOfSamples} random test samples of {@code sampleSize} cases each. */
        MULTIPLE_SAMPLES
    }

    /**
     * Everything the controller needs, already read from the view.
     *
     * @param database          the case database to split and evaluate against
     * @param algorithmType     the learning-algorithm class
     * @param algorithmFactory  builds the configured algorithm per fold (skeleton, training data);
     *                          {@code null} falls back to the algorithm's default parameters
     * @param splitMode         cross-validation or repeated sampling
     * @param folds             number of folds (cross-validation)
     * @param numberOfSamples   number of samples (repeated sampling)
     * @param sampleSize        size of each sample (repeated sampling)
     * @param reproducible      whether to use a fixed random seed
     * @param seed              the seed, used only when {@code reproducible} is true
     * @param measuresSet       the measures to compute (also carries the report title)
     * @param classVariableName class variable for a discriminative algorithm, or {@code null}
     */
    public record Request(CaseDatabase database,
                          Class<? extends LearningAlgorithm> algorithmType,
                          LearningEvaluator.LearningAlgorithmFactory algorithmFactory,
                          SplitMode splitMode,
                          int folds,
                          int numberOfSamples,
                          int sampleSize,
                          boolean reproducible,
                          long seed,
                          MeasuresSet measuresSet,
                          String classVariableName) {
    }

    private CrossValidationController() {
    }

    /**
     * Runs the evaluation described by {@code request} and returns the accumulated measures.
     *
     * @param request the evaluation parameters
     * @return the measures accumulated over all iterations
     */
    public static MeasuresSet run(Request request) throws IncompatibleEvidenceException,
            ConstraintViolatedException, NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        SplitSetManager splitSetManager = request.reproducible()
                ? new SplitSetManager(request.database(), request.seed())
                : new SplitSetManager(request.database());
        SplitSet[] sets = switch (request.splitMode()) {
            case CROSS_VALIDATION -> splitSetManager.crossValidation(request.folds());
            case MULTIPLE_SAMPLES -> splitSetManager.multipleSamples(request.numberOfSamples(), request.sampleSize());
        };
        LearningEvaluator evaluator = new LearningEvaluator(
                request.algorithmType(), request.algorithmFactory(), sets, request.measuresSet());
        if (request.classVariableName() != null) {
            evaluator.setVariable(request.classVariableName());
        }
        return evaluator.runEvaluator();
    }
}
