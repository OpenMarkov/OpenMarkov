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
import java.util.concurrent.CancellationException;
import java.util.function.Predicate;

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
        verifyStateCorrespondence();
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
    // State correspondence between the case database and the network
    // -------------------------------------------------------------------------

    /**
     * Verifies that every case-database variable that also exists in the network shares the same
     * set of state names. Evidence and the confusion matrix translate between the two
     * <em>by state name</em> (see {@link #buildEvidenceCase} and {@link #posteriorsForClassVariable}),
     * because the two variables may order their states differently; if their state <em>sets</em>
     * differed, that translation would be impossible and the results would be corrupted silently.
     * A case-database variable absent from the network is not flagged here — that is reported where
     * the variable is actually used.
     *
     * @throws IllegalArgumentException naming the first variable whose state sets do not match
     */
    private void verifyStateCorrespondence() {
        for (Variable caseVar : caseDatabase.getVariables()) {
            Variable netVar = probNet.getVariable(caseVar.getName());
            if (netVar == null) {
                continue;
            }
            for (State caseState : caseVar.getStates()) {
                if (!netVar.containsState(caseState.getName())) {
                    throw new IllegalArgumentException(
                            "Variable '" + caseVar.getName() + "': state '" + caseState.getName()
                                    + "' exists in the case database but not in the network.");
                }
            }
            if (caseVar.getNumStates() != netVar.getNumStates()) {
                throw new IllegalArgumentException(
                        "Variable '" + caseVar.getName() + "' has " + caseVar.getNumStates()
                                + " states in the case database but " + netVar.getNumStates()
                                + " in the network.");
            }
        }
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
        int numStates = (numCases == 0) ? 0 : prob[0].length;
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

    /**
     * Log-probability floor for an "impossible" case — one the network assigns probability 0.
     * Such a case would contribute {@code log(0) = -infinity}, collapsing the whole log-likelihood
     * to -infinity and making it useless for ranking networks. Instead every impossible case
     * contributes this fixed, finite penalty: the log of the smallest positive probability a
     * {@code double} can represent, about -708.4.
     */
    private static final double MIN_LOG_PROBABILITY = Math.log(Double.MIN_NORMAL);

    /**
     * Log-likelihood of the dataset under the network: the sum of {@code log P(case)} over the N
     * cases, where a case with zero probability is floored to {@link #MIN_LOG_PROBABILITY} rather
     * than excluded.
     *
     * <p>Flooring (not excluding) is what keeps this measure usable when an automated procedure
     * compares networks: every network is scored over the same N cases, so the values are
     * comparable, and a network that deems an observed case impossible is <em>penalised</em> by a
     * large fixed amount per case instead of being rewarded by dropping that case from its sum.
     * The penalty accumulates linearly with the number of impossible cases, and cannot overflow:
     * with |term| &le; {@code -MIN_LOG_PROBABILITY} (~708) and at most ~2.1e9 cases, |sum| stays
     * below ~1.5e12, far from the {@code double} limit (~1.8e308).
     */
    private double calculateLogLikelihood()
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException,
            NotEvaluableNetworkException.NotApplicableNetwork {
        double[] prob = caseProbabilities();
        double logLikelihood = 0.0;   // sum of the finite log-probability terms
        long impossibleCases = 0;     // 'long' so the count itself can never overflow
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            if (prob[i] > 0.0) {
                logLikelihood += Math.log(prob[i]);
            } else {
                impossibleCases++;
            }
        }
        return logLikelihood + impossibleCases * MIN_LOG_PROBABILITY;
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
        Variable netClassVar = probNet.getVariable(classVarName);
        if (netClassVar == null) {
            throw new IllegalArgumentException(
                    "Class variable '" + classVarName + "' is not present in the network");
        }
        int numCases = caseDatabase.getNumCases();
        int numStates = classVar.getNumStates();
        // The posterior comes out in the NETWORK variable's state ordering; map each network state
        // index to the case-database index of the same name so the result lines up with realStates
        // and the confusion-matrix axes (both in case-database ordering).
        int[] netToDbState = new int[netClassVar.getNumStates()];
        for (int netIdx = 0; netIdx < netToDbState.length; netIdx++) {
            netToDbState[netIdx] = classVar.getStateIndex(netClassVar.getStateName(netIdx));
        }
        double[][] posteriors = new double[numCases][numStates];
        Predicate<Variable> includeAsEvidence = v -> !v.getName().equals(classVarName);
        for (int i = 0; i < numCases; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException();   // cancelled while running off the event-dispatch thread
            }
            EvidenceCase evidence = buildEvidenceCase(i, includeAsEvidence);
            double[] caseProb = posteriorOfClassVariable(evidence, netClassVar);
            for (int netIdx = 0; netIdx < caseProb.length; netIdx++) {
                posteriors[i][netToDbState[netIdx]] = caseProb[netIdx];
            }
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
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException();   // cancelled while running off the event-dispatch thread
            }
            EvidenceCase evidence = buildEvidenceCase(i, v -> true);
            probs[i] = doEvaluation(evidence).getValues()[0];
        }
        return probs;
    }

    /**
     * Builds the evidence case for the given row of the dataset, including only
     * the variables that satisfy {@code include}.
     */
    private EvidenceCase buildEvidenceCase(int caseIndex, Predicate<Variable> include)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        int[][] cases = caseDatabase.getCases();
        List<Variable> caseVariables = caseDatabase.getVariables();
        List<Finding> findings = new ArrayList<>(caseVariables.size());
        for (int j = 0; j < caseVariables.size(); j++) {
            Variable caseVar = caseVariables.get(j);
            if (!include.test(caseVar)) {
                continue;
            }
            Variable variable = probNet.getVariable(caseVar.getName());
            if (variable == null) {
                throw new IllegalArgumentException(
                        "Case-database variable '" + caseVar.getName() + "' is not present in the network");
            }
            // Translate by NAME: the stored index refers to the case-database variable's own state
            // ordering, which may differ from the network variable's ordering.
            String stateName = caseVar.getStateName(cases[caseIndex][j]);
            State state = variable.getState(stateName);
            if (state == null) {
                throw new IllegalArgumentException(
                        "Variable '" + caseVar.getName() + "': state '" + stateName
                                + "' is not present in the network.");
            }
            findings.add(new Finding(variable, state));
        }
        return new EvidenceCase(findings);
    }

    /**
     * Posterior over the class variable given {@code evidence}, via VE. Takes the <em>network</em>
     * variable (not the case-database one) so the posterior map lookup — keyed by identity — hits,
     * and returns the values in the network variable's own state ordering.
     */
    private double[] posteriorOfClassVariable(EvidenceCase evidence, Variable netClassVar)
            throws IncompatibleEvidenceException, ConstraintViolatedException,
            NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException,
            CannotNormalizePotentialException {
        Propagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(netClassVar));
        propagation.setPreResolutionEvidence(evidence);
        TablePotential posterior = propagation.getPosteriorValues().get(netClassVar);
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
