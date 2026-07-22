/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.measures;

/**
 * This class calculates and stores the indicators obtained from
 * the confusion matrix.
 *
 * @author evillar
 * @version 1.0
 */

public class MeasureMatrixIndicators {

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
            // Guard the undefined ratios (a class with no real instances, no predictions of it,
            // or all cases in one class). Reporting 0 for those keeps the weighted means finite:
            // an absent class has weight sumRows[i] = 0, but NaN * 0 would still poison the sum.
            this.tp[i] = (sumRows[i] == 0) ? 0.0 : ((double) matrix[i][i] / sumRows[i]);
            this.fp[i] = (numCases - sumRows[i] == 0) ? 0.0
                    : (((double) sumCols[i] - matrix[i][i]) / ((double) numCases - sumRows[i]));
            this.precision[i] = (sumCols[i] == 0) ? 0.0 : ((double) matrix[i][i] / sumCols[i]);
            this.fMeasure[i] = (this.precision[i] + this.tp[i] == 0) ? 0.0
                    : ((2.0 * this.precision[i] * this.tp[i]) / (this.precision[i] + this.tp[i]));
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
