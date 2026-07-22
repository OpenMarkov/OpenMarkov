/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import org.openmarkov.bnEvaluation.measures.MeasureMatrixIndicators;

/**
 * View adapter that renders the {@link MeasureMatrixIndicators} of a confusion matrix as a table
 * with one row per class state, a weighted-mean row and a final {@code Accuracy} row. The
 * {@code Recall} column repeats the {@code TP rate} value (they are the same quantity), and the
 * accuracy row leaves the remaining columns empty; both are preserved from the original layout.
 *
 * @author Manuel Arias
 */
public final class IndicatorsTableModel extends ReadOnlyStringTableModel {

    private static final String NUM_FORMAT = "%.3f";

    public IndicatorsTableModel(MeasureMatrixIndicators indicators, String varName, String[] statesNames) {
        super(build(indicators, varName, statesNames));
    }

    private static Grid build(MeasureMatrixIndicators indicators, String varName, String[] statesNames) {
        int numStates = indicators.getNumStates();
        double[] tp = indicators.getTpRates();
        double[] fp = indicators.getFpRates();
        double[] precision = indicators.getPrecisions();
        double[] fMeasure = indicators.getFMeasures();

        String[] columnNames = { "State", "TP rate", "FP rate", "Precision", "Recall", "F Measure" };
        String[][] cells = new String[numStates + 2][6];
        for (int i = 0; i < numStates; i++) {
            cells[i][0] = varName + " (" + statesNames[i] + ")";
            cells[i][1] = String.format(NUM_FORMAT, tp[i]);
            cells[i][2] = String.format(NUM_FORMAT, fp[i]);
            cells[i][3] = String.format(NUM_FORMAT, precision[i]);
            cells[i][4] = String.format(NUM_FORMAT, tp[i]);
            cells[i][5] = String.format(NUM_FORMAT, fMeasure[i]);
        }
        cells[numStates][0] = varName + " mean";
        cells[numStates][1] = String.format(NUM_FORMAT, tp[numStates]);
        cells[numStates][2] = String.format(NUM_FORMAT, fp[numStates]);
        cells[numStates][3] = String.format(NUM_FORMAT, precision[numStates]);
        cells[numStates][4] = String.format(NUM_FORMAT, tp[numStates]);
        cells[numStates][5] = String.format(NUM_FORMAT, fMeasure[numStates]);
        cells[numStates + 1][0] = "Accuracy";
        cells[numStates + 1][1] = String.format(NUM_FORMAT, indicators.getAccuracy());
        return new Grid(columnNames, cells);
    }
}
