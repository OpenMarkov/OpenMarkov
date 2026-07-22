/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import org.openmarkov.bnEvaluation.measures.MeasureMatrix;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * View adapter that renders a {@link MeasureMatrix} confusion matrix as a table with a header row
 * and column ({@code TRUE / PREDICTED->}), one row and column per class state, and a trailing
 * {@code Total} row and column with the row, column and grand totals. Counts are formatted with the
 * default-locale {@link NumberFormat}.
 *
 * @author Manuel Arias
 */
public final class ConfusionMatrixTableModel extends ReadOnlyStringTableModel {

    public ConfusionMatrixTableModel(MeasureMatrix measureMatrix) {
        super(build(measureMatrix));
    }

    private static Grid build(MeasureMatrix measureMatrix) {
        int numStates = measureMatrix.getNumStates();
        int[][] matrix = measureMatrix.getMatrix();
        String[] statesNames = measureMatrix.getStatesNames();
        String varName = measureMatrix.getVarName();
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

        String[] columnNames = new String[numStates + 2];
        String[][] cells = new String[numStates + 1][numStates + 2];
        columnNames[0] = "TRUE / PREDICTED->";
        columnNames[numStates + 1] = "Total";
        cells[numStates][0] = "Total";
        int sum = 0;
        for (int i = 0; i < numStates; i++) {
            columnNames[i + 1] = varName + "(" + statesNames[i] + ")";
            cells[i][0] = varName + "(" + statesNames[i] + ")";
            int rowTotal = 0;
            int colTotal = 0;
            for (int j = 1; j < numStates + 1; j++) {
                cells[i][j] = format.format(matrix[i][j - 1]);
                rowTotal = rowTotal + matrix[i][j - 1];
                colTotal = colTotal + matrix[j - 1][i];
            }
            cells[i][numStates + 1] = format.format(rowTotal);
            cells[numStates][i + 1] = format.format(colTotal);
            sum = sum + rowTotal;
        }
        cells[numStates][numStates + 1] = format.format(sum);
        return new Grid(columnNames, cells);
    }
}
