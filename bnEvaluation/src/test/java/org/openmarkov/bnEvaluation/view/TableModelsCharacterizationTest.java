/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation.view;

import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Characterises the {@code view} table adapters (F2): they must reproduce, cell for cell, the
 * tables the {@code measures} model classes used to build, so that moving the Swing code out of the
 * model causes no regression. These tests locked the layout when the adapters replaced the original
 * {@code xxxToTable()} methods and guard it against future drift.
 *
 * @author Manuel Arias
 */
class TableModelsCharacterizationTest {

    // ---- fixtures ---------------------------------------------------------

    /** A 2x2 confusion matrix on class variable C(yes,no) with indicators and per-case posteriors. */
    private static MeasureMatrix confusionMatrix() {
        MeasureMatrix mm = new MeasureMatrix(MeasureType.CONFUSIONMATRIX, new String[] { "yes", "no" }, "C");
        mm.setMatrix(new int[][] { { 3, 1 }, { 2, 4 } }, 10);
        mm.setIndicators();
        Variable c = new Variable("C", "yes", "no");
        CaseDatabase db = new CaseDatabase(List.of(c), new int[][] { { 0 }, { 1 } });
        mm.setIndividualProb(db, new double[][] { { 0.7, 0.3 }, { 0.4, 0.6 } }, new int[] { 0, 1 });
        return mm;
    }

    private static MeasuresSet scores() {
        MeasuresSet ms = new MeasuresSet("t");
        MeasureValue ll = new MeasureValue(MeasureType.LOGLIKELIHOOD);
        ll.setValue(-10.0, 5);
        ms.addMeasureValue(ll);
        MeasureValue bayes = new MeasureValue(MeasureType.BAYES);
        bayes.setValue(-20.0, 5);
        ms.addMeasureValue(bayes);
        return ms;
    }

    // ---- permanent layout characterisation --------------------------------

    @Test
    void confusionMatrixLayout() {
        TableModel m = new ConfusionMatrixTableModel(confusionMatrix());
        assertEquals(List.of("TRUE / PREDICTED->", "C(yes)", "C(no)", "Total"), columnNames(m));
        assertEquals(List.of("C(yes)", "3", "1", "4"), row(m, 0));
        assertEquals(List.of("C(no)", "2", "4", "6"), row(m, 1));
        assertEquals(List.of("Total", "5", "5", "10"), row(m, 2));
    }

    @Test
    void indicatorsLayout() {
        MeasureMatrix mm = confusionMatrix();
        TableModel m = new IndicatorsTableModel(mm.getIndicators(), mm.getVarName(), mm.getStatesNames());
        assertEquals(List.of("State", "TP rate", "FP rate", "Precision", "Recall", "F Measure"), columnNames(m));
        assertEquals(4, m.getRowCount());
        assertEquals("C (yes)", m.getValueAt(0, 0));
        assertEquals(f(0.75), m.getValueAt(0, 1));                 // TP rate = 3/4
        assertEquals(m.getValueAt(0, 1), m.getValueAt(0, 4));      // Recall repeats TP rate
        assertEquals("Accuracy", m.getValueAt(3, 0));
        assertEquals(f(0.7), m.getValueAt(3, 1));                  // accuracy = (3+4)/10
        assertNull(m.getValueAt(3, 2));                            // accuracy row leaves the tail empty
    }

    @Test
    void individualProbabilitiesLayout() {
        MeasureMatrix mm = confusionMatrix();
        TableModel m = new IndividualProbabilityTableModel(mm.getIndividualProb(), mm.getStatesNames(), mm.getVarName());
        assertEquals(List.of("C", "P(C=yes)", "P(C=no)", "most probable state"), columnNames(m));
        assertEquals(List.of("yes", f(0.7), f(0.3), "yes"), row(m, 0));
        assertEquals(List.of("no", f(0.4), f(0.6), "no"), row(m, 1));
    }

    @Test
    void scoresLayout() {
        TableModel m = new ScoresTableModel(scores().buildScoresRows());
        assertEquals(List.of("", ""), columnNames(m));
        assertEquals(5, m.getRowCount());
        assertEquals("", m.getValueAt(0, 1));       // first section: empty value column
        assertEquals(f(-10.0), m.getValueAt(1, 1)); // log-likelihood score
        assertEquals(f(2.0), m.getValueAt(2, 1));   // loss = -(-10)/5
        assertEquals("", m.getValueAt(3, 1));        // second section
        assertEquals(f(-20.0), m.getValueAt(4, 1)); // Bayesian score
    }

    /** Formats a value exactly as the adapters do (default-locale three-decimal string). */
    private static String f(double value) {
        return String.format("%.3f", value);
    }

    // ---- helpers ----------------------------------------------------------

    private static List<String> columnNames(TableModel m) {
        List<String> names = new ArrayList<>();
        for (int c = 0; c < m.getColumnCount(); c++) {
            names.add(m.getColumnName(c));
        }
        return names;
    }

    private static List<String> row(TableModel m, int r) {
        List<String> cells = new ArrayList<>();
        for (int c = 0; c < m.getColumnCount(); c++) {
            Object v = m.getValueAt(r, c);
            cells.add(v == null ? null : v.toString());
        }
        return cells;
    }
}
