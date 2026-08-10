/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;
import java.util.concurrent.CancellationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetEvaluatorTest {

    private static ProbNet netWith(Variable... vars) {
        ProbNet net = new ProbNet();
        for (Variable v : vars) {
            net.addNode(v, NodeType.CHANCE);
        }
        return net;
    }

    /**
     * B-NetEvaluator #2: a case-database variable absent from the network must produce a
     * clear error, not an opaque NullPointerException.
     */
    @Test
    void databaseVariableMissingFromNetworkThrowsClearError() {
        Variable a = new Variable("A", 2);
        Variable b = new Variable("B", 2);
        ProbNet net = netWith(new Variable("A", 2));   // network has A, not B
        CaseDatabase db = new CaseDatabase(List.of(a, b), new int[][] { { 0, 0 } });

        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));

        NetEvaluator evaluator = new NetEvaluator(net, db, measures);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, evaluator::runEvaluator);
        assertTrue(ex.getMessage().contains("B"), "message should name the missing variable: " + ex.getMessage());
    }

    /**
     * Two database columns of the same name resolve to the same network variable. When they
     * disagree, one of the two findings cannot be added, and the evaluation must say so instead of
     * scoring the case with the value that happened to come first.
     */
    @Test
    void twoDatabaseColumnsOfTheSameNameThatDisagreeStopTheEvaluation() {
        Variable first = new Variable("A", 2);
        Variable repeated = new Variable("A", 2);
        ProbNet net = netWith(new Variable("A", 2));
        CaseDatabase db = new CaseDatabase(List.of(first, repeated), new int[][] { { 0, 1 } });

        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));

        NetEvaluator evaluator = new NetEvaluator(net, db, measures);
        assertThrows(IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther.class, evaluator::runEvaluator,
                "The case was evaluated with one of the two values, and the other was dropped in silence");
    }

    /**
     * B-NetEvaluator #3: an empty case database must not overrun the argmax over an empty
     * posterior array (ArrayIndexOutOfBoundsException).
     */
    @Test
    void emptyDatabaseDoesNotThrowOnConfusionMatrix() {
        Variable cls = new Variable("Class", 2);
        ProbNet net = netWith(new Variable("Class", 2));
        CaseDatabase emptyDb = new CaseDatabase(List.of(cls), new int[0][1]);

        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureMatrix(new MeasureMatrix(MeasureType.CONFUSIONMATRIX, new String[] { "0", "1" }, "Class"));

        NetEvaluator evaluator = new NetEvaluator(net, emptyDb, measures);
        assertDoesNotThrow(evaluator::runEvaluator);
    }

    // -------------------------------------------------------------------------
    // Log-likelihood with zero-probability cases (P3)
    // -------------------------------------------------------------------------

    private static final double LOG_PROB_FLOOR = Math.log(Double.MIN_NORMAL);

    /** A single-variable Bayesian network with P(A=0)=p0 and P(A=1)=p1. */
    private static ProbNet singleVariableNet(double p0, double p1) {
        Variable a = new Variable("A", 2);
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Node node = net.addNode(a, NodeType.CHANCE);
        node.setPotentials(List.of(
                new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { p0, p1 })));
        return net;
    }

    /** A database over variable A with {@code numZeros} cases in state 0 and {@code numOnes} in state 1. */
    private static CaseDatabase singleVariableDatabase(int numZeros, int numOnes) {
        Variable a = new Variable("A", 2);
        int[][] cases = new int[numZeros + numOnes][1];
        for (int i = 0; i < numZeros; i++) {
            cases[i][0] = 0;
        }
        for (int i = numZeros; i < numZeros + numOnes; i++) {
            cases[i][0] = 1;
        }
        return new CaseDatabase(List.of(a), cases);
    }

    private static double logLikelihood(ProbNet net, CaseDatabase db) throws Exception {
        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));
        new NetEvaluator(net, db, measures).runEvaluator();
        return measures.getMeasures().get(0).getValue();
    }

    /** With no impossible cases, the log-likelihood is exactly the direct sum of log P(case). */
    @Test
    void logLikelihoodEqualsDirectSumWhenEveryCaseIsPossible() throws Exception {
        ProbNet net = singleVariableNet(0.75, 0.25);
        CaseDatabase db = singleVariableDatabase(3, 2);   // 3x P=0.75, 2x P=0.25
        double expected = 3 * Math.log(0.75) + 2 * Math.log(0.25);
        assertThat(logLikelihood(net, db)).isCloseTo(expected, within(1e-9));
    }

    /** An impossible case (P=0) is floored, not dropped, so the result stays finite. */
    @Test
    void impossibleCasesAreFlooredAndKeepTheResultFinite() throws Exception {
        ProbNet net = singleVariableNet(1.0, 0.0);        // state 1 is impossible
        CaseDatabase db = singleVariableDatabase(2, 3);   // 2x P=1 (log 1 = 0), 3x P=0 (floored)
        double ll = logLikelihood(net, db);
        assertThat(ll).isFinite();
        assertThat(ll).isCloseTo(3 * LOG_PROB_FLOOR, within(1e-6));
    }

    /**
     * Comparability / no perverse reward: a network that gives an observed case a small positive
     * probability must score strictly higher than one that deems it impossible (P=0). Excluding
     * the zero cases instead of flooring them would break this and could invert the ranking.
     */
    @Test
    void networkAssigningPositiveProbabilityBeatsOneThatDeemsTheCaseImpossible() throws Exception {
        CaseDatabase db = singleVariableDatabase(4, 1);      // one observed case is in state 1
        ProbNet cautious = singleVariableNet(0.5, 0.5);      // P(state 1) = 0.5 > 0
        ProbNet overconfident = singleVariableNet(1.0, 0.0); // P(state 1) = 0

        double llCautious = logLikelihood(cautious, db);
        double llOverconfident = logLikelihood(overconfident, db);

        assertThat(llCautious).isFinite();
        assertThat(llOverconfident).isFinite();
        assertThat(llCautious).isGreaterThan(llOverconfident);
    }

    /** The penalty accumulates: each extra impossible case lowers the score by exactly the floor. */
    @Test
    void penaltyAccumulatesLinearlyWithTheNumberOfImpossibleCases() throws Exception {
        ProbNet net = singleVariableNet(1.0, 0.0);           // state 1 is impossible
        double llOneImpossible = logLikelihood(net, singleVariableDatabase(5, 1));
        double llThreeImpossible = logLikelihood(net, singleVariableDatabase(5, 3));
        // two extra impossible cases -> two extra floor penalties
        assertThat(llOneImpossible - llThreeImpossible).isCloseTo(-2 * LOG_PROB_FLOOR, within(1e-6));
    }

    // -------------------------------------------------------------------------
    // State correspondence by name (P8)
    // -------------------------------------------------------------------------

    /** A single-node Bayesian network over {@code variable} with the given prior. */
    private static ProbNet priorNet(Variable variable, double... prior) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Node node = net.addNode(variable, NodeType.CHANCE);
        node.setPotentials(List.of(
                new TablePotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY, prior)));
        return net;
    }

    /**
     * P8: when the case database and the network list the same states in a different order, the
     * observed state must be identified by NAME, not by position. Here the database records index
     * 0 = "no" while the network lists "yes" first; a position-based lookup would score it as "yes".
     */
    @Test
    void evidenceUsesStateNamesNotPositionsWhenOrderingsDiffer() throws Exception {
        ProbNet net = priorNet(new Variable("A", "yes", "no"), 0.9, 0.1);   // P(yes)=0.9, P(no)=0.1
        Variable dbA = new Variable("A", "no", "yes");                      // opposite order
        CaseDatabase db = new CaseDatabase(List.of(dbA), new int[][] { { 0 } });   // index 0 => "no"

        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));
        new NetEvaluator(net, db, measures).runEvaluator();

        // Observed state is "no" (P=0.1); the old position-based lookup would use "yes" (P=0.9).
        assertThat(measures.getMeasures().get(0).getValue()).isCloseTo(Math.log(0.1), within(1e-9));
    }

    /**
     * P8: the confusion matrix must align predicted and true states by name too. With no evidence
     * variables the posterior equals the prior, so the predicted class is the most probable state;
     * both axes must be expressed in the case-database ordering used for the labels.
     */
    @Test
    void confusionMatrixAlignsStatesByNameWhenOrderingsDiffer() throws Exception {
        ProbNet net = priorNet(new Variable("C", "yes", "no"), 0.9, 0.1);  // most probable: "yes"
        Variable dbC = new Variable("C", "no", "yes");                     // db order: index 1 => "yes"
        CaseDatabase db = new CaseDatabase(List.of(dbC), new int[][] { { 1 } });   // true state: "yes"

        MeasuresSet measures = new MeasuresSet("test");
        // Axis labels follow the database ordering: index 0 = "no", index 1 = "yes".
        measures.addMeasureMatrix(new MeasureMatrix(MeasureType.CONFUSIONMATRIX, new String[] { "no", "yes" }, "C"));
        new NetEvaluator(net, db, measures).runEvaluator();

        int[][] matrix = measures.getMeasureMatrix().getMatrix();
        // Predicted "yes" (db index 1) and true "yes" (db index 1): the single case lands on [1][1].
        assertThat(matrix[1][1]).isEqualTo(1);
        assertThat(matrix[0][0]).isZero();
        assertThat(matrix[0][1]).isZero();
        assertThat(matrix[1][0]).isZero();
    }

    /**
     * F4: when run off the event-dispatch thread and cancelled (its thread interrupted), the
     * evaluator stops promptly by throwing {@link CancellationException} instead of finishing.
     */
    @Test
    void anInterruptedThreadCancelsTheEvaluation() {
        ProbNet net = singleVariableNet(0.5, 0.5);
        CaseDatabase db = singleVariableDatabase(1, 1);
        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));
        NetEvaluator evaluator = new NetEvaluator(net, db, measures);

        Thread.currentThread().interrupt();
        try {
            assertThrows(CancellationException.class, evaluator::runEvaluator);
        } finally {
            Thread.interrupted();   // clear the flag so it does not leak to other tests
        }
    }

    /**
     * P8: if the database and the network disagree on the SET of states of a shared variable, the
     * evaluator must fail up front with a clear message rather than silently mistranslating.
     */
    @Test
    void mismatchedStateSetsThrowClearError() {
        ProbNet net = priorNet(new Variable("A", "yes", "no"), 0.5, 0.5);
        Variable dbA = new Variable("A", "yes", "maybe");   // "maybe" is not a state of the network variable
        CaseDatabase db = new CaseDatabase(List.of(dbA), new int[][] { { 0 } });

        MeasuresSet measures = new MeasuresSet("test");
        measures.addMeasureValue(new MeasureValue(MeasureType.LOGLIKELIHOOD));

        NetEvaluator evaluator = new NetEvaluator(net, db, measures);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, evaluator::runEvaluator);
        assertTrue(ex.getMessage().contains("maybe"),
                "message should name the offending state: " + ex.getMessage());
    }
}
