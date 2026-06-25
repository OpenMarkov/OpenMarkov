/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.measures;

import javax.swing.*;

/**
 * This class calculates and stores the indicators obtained from
 * the confusion matrix.
 *
 * @author evillar
 * @version 1.0
 */

public class MeasureMatrixIndicators {
    
    private static final String NUM_FORMAT = "%.3f";
    
    /**
     * confusion matrix indicators
     */
    private final int numStates;
    private final double[] tp;
    private final double[] fp;
    private final double[] precision;
    private final double[] fMeasure;
    private final double accuracy;
    
    /**
     * The builder gathers the necessary information
     * to calculate the indicators and calls the method
     * to calculate them.
     */
    public MeasureMatrixIndicators(int[][] matrix, int numCases) {
        this.numStates = matrix[0].length;
        
        //Calculate sums
        int[] sumCols = new int[this.numStates];
        int[] sumRows = new int[this.numStates];
        for (int i1 = 0; i1 < this.numStates; i1++) {
            sumRows[i1] = 0;
            sumCols[i1] = 0;
            for (int j = 0; j < this.numStates; j++) {
                sumRows[i1] = sumRows[i1] + matrix[i1][j];
                sumCols[i1] = sumCols[i1] + matrix[j][i1];
            }
        }
        
        //Calculate the indicators
        this.tp = new double[this.numStates + 1];
        this.fp = new double[this.numStates + 1];
        this.precision = new double[this.numStates + 1];
        this.fMeasure = new double[this.numStates + 1];
        // indicator for each state
        double tpAcum = 0.0;
        double fpAcum = 0.0;
        double precisionAcum = 0.0;
        double fMeasureAcum = 0.0;
        double accuracy = 0.0;
        // loop in each state
        for (int i = 0; i < this.numStates; i++) {
            this.tp[i] = ((double) matrix[i][i] / sumRows[i]);
            this.fp[i] = ((double) sumCols[i] - matrix[i][i]) / ((double) numCases - sumRows[i]);
            this.precision[i] = ((double) matrix[i][i] / sumCols[i]);
            this.fMeasure[i] = (2.0 * this.precision[i] * this.tp[i]) / (this.precision[i] + this.tp[i]);
            accuracy = accuracy + matrix[i][i];
            // sum of the indicators with weights=num cases of real states
            tpAcum = tpAcum + this.tp[i] * sumRows[i];
            fpAcum = fpAcum + this.fp[i] * sumRows[i];
            precisionAcum = precisionAcum + this.precision[i] * sumRows[i];
            fMeasureAcum = fMeasureAcum + this.fMeasure[i] * sumRows[i];
        }
        // average all states
        this.tp[this.numStates] = tpAcum / numCases;
        this.fp[this.numStates] = fpAcum / numCases;
        this.precision[this.numStates] = precisionAcum / numCases;
        this.fMeasure[this.numStates] = fMeasureAcum / numCases;
        this.accuracy = accuracy / numCases;
    }
    
    /**
     * This method returns a JTable with the indicators
     *
     * @return JTable
     */
    public JTable toTable(String varName, String[] statesNames) {
        // there are numStates+1 rows and 5 columns (indicators)
        String[][] indicatorsTable = new String[this.numStates + 2][6];
        for (int i = 0; i < this.numStates; i++) {
            indicatorsTable[i][0] = varName + " (" + statesNames[i] + ")";
            indicatorsTable[i][1] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[i]);
            indicatorsTable[i][2] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fp[i]);
            indicatorsTable[i][3] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.precision[i]);
            indicatorsTable[i][4] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[i]);
            indicatorsTable[i][5] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fMeasure[i]);
        }
        indicatorsTable[this.numStates][0] = varName + " mean";
        indicatorsTable[this.numStates][1] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[this.numStates]);
        indicatorsTable[this.numStates][2] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fp[this.numStates]);
        indicatorsTable[this.numStates][3] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.precision[this.numStates]);
        indicatorsTable[this.numStates][4] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[this.numStates]);
        indicatorsTable[this.numStates][5] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fMeasure[this.numStates]);
        // accuracity
        indicatorsTable[this.numStates + 1][0] = "Accuracy";
        indicatorsTable[this.numStates + 1][1] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.accuracy);
        JTable tabla = new JTable(indicatorsTable, new String[]{"State", "TP rate", "FP rate", "Precision", "Recall", "F Measure"});
        return tabla;
    }
    
    // Read-only accessors for the export module.

    /** Number of class-variable states (length of {@link #getTpRates()} − 1). */
    public int getNumStates() { return numStates; }

    /** True-positive rate per state, plus a final entry with the weighted mean. */
    public double[] getTpRates() { return tp; }

    /** False-positive rate per state, plus a final entry with the weighted mean. */
    public double[] getFpRates() { return fp; }

    /** Precision per state, plus a final entry with the weighted mean. */
    public double[] getPrecisions() { return precision; }

    /** F-measure per state, plus a final entry with the weighted mean. */
    public double[] getFMeasures() { return fMeasure; }

    /** Overall accuracy. */
    public double getAccuracy() { return accuracy; }

}
