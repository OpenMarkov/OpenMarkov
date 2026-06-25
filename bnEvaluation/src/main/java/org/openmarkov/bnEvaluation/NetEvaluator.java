/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.learning.metric.Metric;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Computes goodness-of-fit measures of a {@link ProbNet} on a {@link CaseDatabase}.
 * <p>
 * The available measures are described by {@link MeasureType}:
 * <ul>
 *   <li>{@link MeasureType#CONFUSIONMATRIX} — confusion matrix and derived
 *       indicators on a designated <em>class variable</em>.</li>
 *   <li>{@link MeasureType#LOGLIKELIHOOD} — sum of log-probabilities of every
 *       case under the network.</li>
 *   <li>The remaining types delegate to {@link MeasureType#newMetric()}.</li>
 * </ul>
 *
 * @author evillar
 */
public class NetEvaluator {

    private final ProbNet probNet;
    private final CaseDatabase caseDatabase;
    private final MeasuresSet measuresSet;

    public NetEvaluator(ProbNet probNet, CaseDatabase caseDatabase, MeasuresSet measuresSet) {
        this.probNet = probNet;
        this.caseDatabase = caseDatabase;
        this.measuresSet = measuresSet;
    }

    /**
     * Runs the evaluator and populates {@link #measuresSet} with the computed
     * measures, returning it for chaining.
     */
    public MeasuresSet runEvaluator() throws IncompatibleEvidenceException, ConstraintViolatedException,
            NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork,
            CannotNormalizePotentialException {
        MeasureMatrix measureMatrix = measuresSet.getMeasureMatrix();
        if (measureMatrix != null) {
            populateConfusionMatrix(measureMatrix);
        }
        for (MeasureValue measure : measuresSet.getMeasures()) {
            switch (measure.getMeasureType()) {
                case CONFUSIONMATRIX -> {
                    /* already populated above */
                }
                case LOGLIKELIHOOD -> measure.setValue(calculateLogLikelihood(), caseDatabase.getNumCases());
                default            -> measure.setValue(calculateScore(measure.getMeasureType()),
                                                       caseDatabase.getNumCases());
            }
        }
        return measuresSet;
    }

    // -------------------------------------------------------------------------
    // Confusion matrix
    // -------------------------------------------------------------------------

    private void populateConfusionMatrix(MeasureMatrix measureMatrix)
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        String varName = measureMatrix.getVarName();
        double[][] probStates = posteriorsForClassVariable(varName);
        int[] realStates = realStates(varName);
        int[] estimatedStates = argmaxByRow(probStates);
        int[][] matrix = confusionMatrix(realStates, estimatedStates, measureMatrix.getNumStates());
        measureMatrix.setMatrix(matrix, caseDatabase.getNumCases());
        measureMatrix.setIndicators();
        if (measureMatrix.getShowIndividualProb()) {
            measureMatrix.setIndividualProb(caseDatabase, probStates, estimatedStates);
        }
    }

    /** True-state index per case for the class variable. */
    private int[] realStates(String varName) {
        Variable variable = caseDatabase.getVariable(varName);
        if (variable == null) {
            throw new IllegalArgumentException(
                    "Class variable '" + varName + "' is not present in the case database");
        }
        return caseDatabase.getCases(variable);
    }

    /** Most-probable-state index per row of {@code prob}. */
    private static int[] argmaxByRow(double[][] prob) {
        int numCases = prob.length;
        int numStates = prob[0].length;
        int[] estimated = new int[numCases];
        for (int i = 0; i < numCases; i++) {
            int bestJ = 0;
            double bestP = prob[i][0];
            for (int j = 1; j < numStates; j++) {
                if (prob[i][j] > bestP) {
                    bestJ = j;
                    bestP = prob[i][j];
                }
            }
            estimated[i] = bestJ;
        }
        return estimated;
    }

    /** Tally of (real, estimated) pairs into a {@code numStates × numStates} matrix. */
    private static int[][] confusionMatrix(int[] realStates, int[] estimatedStates, int numStates) {
        int[][] matrix = new int[numStates][numStates];
        for (int i = 0; i < realStates.length; i++) {
            matrix[realStates[i]][estimatedStates[i]]++;
        }
        return matrix;
    }

    // -------------------------------------------------------------------------
    // Score-based measures
    // -------------------------------------------------------------------------

    /** Sum of log P(case) over the dataset. */
    private double calculateLogLikelihood()
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork {
        double[] prob = caseProbabilities();
        return IntStream.range(0, caseDatabase.getNumCases())
                .mapToDouble(i -> Math.log(prob[i]))
                .sum();
    }

    /** Score from the {@link Metric} associated with the given {@link MeasureType}. */
    private double calculateScore(MeasureType type) {
        Metric metric = type.newMetric();
        metric.init(probNet, caseDatabase);
        return metric.getScore();
    }

    // -------------------------------------------------------------------------
    // Inference helpers
    // -------------------------------------------------------------------------

    /**
     * For every case, builds the {@link EvidenceCase} that fixes every variable
     * <em>except</em> the class variable, propagates it, and collects the
     * posterior over the class variable.
     */
    private double[][] posteriorsForClassVariable(String classVarName)
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        Variable classVar = caseDatabase.getVariable(classVarName);
        if (classVar == null) {
            throw new IllegalArgumentException(
                    "Class variable '" + classVarName + "' is not present in the case database");
        }
        int numCases = caseDatabase.getNumCases();
        int numStates = classVar.getNumStates();
        double[][] posteriors = new double[numCases][numStates];
        Predicate<Variable> includeAsEvidence = v -> !v.getName().equals(classVarName);
        for (int i = 0; i < numCases; i++) {
            EvidenceCase evidence = buildEvidenceCase(i, includeAsEvidence);
            double[] caseProb = posteriorOfClassVariable(evidence, classVar);
            System.arraycopy(caseProb, 0, posteriors[i], 0, numStates);
        }
        return posteriors;
    }

    /**
     * Probability of every case under the network when <em>all</em> case
     * variables are observed.
     */
    private double[] caseProbabilities()
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork {
        int numCases = caseDatabase.getNumCases();
        double[] probs = new double[numCases];
        for (int i = 0; i < numCases; i++) {
            EvidenceCase evidence = buildEvidenceCase(i, v -> true);
            probs[i] = doEvaluation(evidence).getValues()[0];
        }
        return probs;
    }

    /**
     * Builds the evidence case for the given row of the dataset, including only
     * the variables that satisfy {@code include}.
     */
    private EvidenceCase buildEvidenceCase(int caseIndex, Predicate<Variable> include) {
        int[][] cases = caseDatabase.getCases();
        List<Variable> caseVariables = caseDatabase.getVariables();
        List<Finding> findings = new ArrayList<>(caseVariables.size());
        for (int j = 0; j < caseVariables.size(); j++) {
            Variable caseVar = caseVariables.get(j);
            if (!include.test(caseVar)) {
                continue;
            }
            Variable variable = probNet.getVariable(caseVar.getName());
            State state = variable.getState(variable.getStateName(cases[caseIndex][j]));
            findings.add(new Finding(variable, state));
        }
        return new EvidenceCase(findings);
    }

    /** Posterior over {@code classVar} given {@code evidence}, via VE. */
    private double[] posteriorOfClassVariable(EvidenceCase evidence, Variable classVar)
            throws IncompatibleEvidenceException, ConstraintViolatedException,
            NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException,
            CannotNormalizePotentialException {
        Propagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(classVar));
        propagation.setPreResolutionEvidence(evidence);
        TablePotential posterior = propagation.getPosteriorValues().get(classVar);
        return posterior.getValues();
    }

    /** Probability of the evidence (joint over all observed variables) via VE. */
    private TablePotential doEvaluation(EvidenceCase evidence)
            throws IncompatibleEvidenceException, ConstraintViolatedException,
            NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException {
        VEEvaluation veEvaluation = new VEEvaluation(probNet);
        veEvaluation.setPreResolutionEvidence(evidence);
        return veEvaluation.getProbability();
    }
}
