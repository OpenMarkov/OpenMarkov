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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

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
}
