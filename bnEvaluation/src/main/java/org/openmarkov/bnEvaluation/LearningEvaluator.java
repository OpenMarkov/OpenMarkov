/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.exception.EmptyModelNetException;
import org.openmarkov.learning.core.exception.UnobservedVariablesException;

import java.util.concurrent.CancellationException;

/**
 * Evaluates a learning algorithm by cross-validation: for every train/test split it learns a
 * network from the training set, evaluates it against the test set with {@link NetEvaluator},
 * and averages the measures across the iterations.
 *
 * @author evillar
 */
public class LearningEvaluator {

    /**
     * Receives the evaluation progress after each iteration, so a caller (the cross-validation
     * dialog, phase F4) can drive a progress bar while the work runs off the event-dispatch thread.
     */
    @FunctionalInterface
    public interface ProgressListener {
        /** A listener that does nothing, for callers that do not track progress. */
        ProgressListener NONE = (completedIterations, totalIterations) -> { };

        /**
         * @param completedIterations iterations finished so far (0 before the first)
         * @param totalIterations     total number of iterations
         */
        void onProgress(int completedIterations, int totalIterations);
    }

    /**
     * Builds the (configured) learning algorithm for one fold, given the initial network skeleton
     * and that fold's training database.
     *
     * <p>In production this is {@code optionsGUI::getInstance}, so the parameters the user set in
     * the algorithm's options dialog are honoured. Passing it as a function (rather than a
     * reference to the GUI dialog) keeps this class free of any dependency on the user interface
     * and makes it unit-testable.
     */
    @FunctionalInterface
    public interface LearningAlgorithmFactory {
        LearningAlgorithm create(ProbNet skeletonNet, CaseDatabase trainDatabase);
    }

    private final Class<? extends LearningAlgorithm> algorithmClass;
    private final LearningAlgorithmFactory algorithmFactory;
    private final SplitSet[] sets;
    private final MeasuresSet[] measuresSet;
    private final MeasuresSet measureSetToCalculate;
    private final int numIterations;
    private String classVariableName;

    /**
     * Constructor for the class. The instance is created in CrossValidationDialog.
     *
     * @param algorithmClass        the learning algorithm class (used to build the network
     *                              skeleton and, when {@code algorithmFactory} is {@code null}, to
     *                              instantiate the algorithm with its default parameters)
     * @param algorithmFactory      builds the configured algorithm for each fold, or {@code null}
     *                              to use the algorithm's default parameters
     * @param sets                  the train/test split sets, one per iteration
     * @param measureSetToCalculate template defining which measures to compute
     */
    public LearningEvaluator(Class<? extends LearningAlgorithm> algorithmClass,
                             LearningAlgorithmFactory algorithmFactory,
                             SplitSet[] sets, MeasuresSet measureSetToCalculate) {
        this.algorithmClass = algorithmClass;
        this.algorithmFactory = algorithmFactory;
        this.sets = sets;
        this.measureSetToCalculate = measureSetToCalculate;
        this.numIterations = sets.length;
        this.measuresSet = new MeasuresSet[numIterations];
        this.classVariableName = null;
    }

    public void setVariable(String classVariableName) {
        this.classVariableName = classVariableName;
    }

    /**
     * Runs the full evaluation loop: for each split, learns a network from the training set,
     * evaluates it against the test set, and accumulates the measures.
     *
     * @return the averaged measures across all iterations
     */
    public MeasuresSet runEvaluator() throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        return runEvaluator(ProgressListener.NONE);
    }

    /**
     * Runs the full evaluation loop reporting progress after each iteration. When run off the
     * event-dispatch thread and cancelled (its thread interrupted), it stops at the next iteration
     * boundary by throwing {@link CancellationException}.
     *
     * @param progressListener notified with (completed, total) before the first iteration and after
     *                         each one
     * @return the averaged measures across all iterations
     */
    public MeasuresSet runEvaluator(ProgressListener progressListener) throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        // measuresSetMean has numIterations=0
        MeasuresSet measuresSetMean = new MeasuresSet(measureSetToCalculate);
        progressListener.onProgress(0, numIterations);
        // loop in k
        for (int i = 0; i < numIterations; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException();
            }
            CaseDatabase trainDatabase = sets[i].getTrainDatabase();
            CaseDatabase testDatabase = sets[i].getTestDatabase();
            ProbNet trainNet = learnTrainNet(trainDatabase);
            // test with testDatabase
            // a new measuresSet is created with the same structure than measureSetToCalculate
            measuresSet[i] = new MeasuresSet(measureSetToCalculate);
            NetEvaluator netEvaluator = new NetEvaluator(trainNet, testDatabase, measuresSet[i]);
            netEvaluator.runEvaluator();
            measuresSetMean.accumulateMeasureSet(measuresSet[i]);
            progressListener.onProgress(i + 1, numIterations);
        }
        measuresSetMean.setAveraged();
        return measuresSetMean;
    }

    private ProbNet learnTrainNet(CaseDatabase trainDatabase) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, CannotNormalizePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        LearningManager learningManager;
        try {
            learningManager = new LearningManager(trainDatabase, algorithmClass, null, null);
        } catch (EmptyModelNetException | UnobservedVariablesException e) {
            throw new UnreachableException(e);
        }
        // Build the algorithm honouring the user's options; fall back to defaults when none.
        LearningAlgorithm learningAlgorithm = (algorithmFactory != null)
                ? algorithmFactory.create(learningManager.getLearnedNet(), trainDatabase)
                : learningManager.instantiate(algorithmClass);
        if (classVariableName != null) {
            learningAlgorithm.setClassVariableName(classVariableName);
        }
        learningManager.init(learningAlgorithm);
        learningManager.learn();
        return learningManager.getLearnedNet();
    }

}
