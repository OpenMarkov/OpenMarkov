/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

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
}
