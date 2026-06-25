/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.measures;


import org.openmarkov.core.model.database.CaseDatabase;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * This class represents a confusion matrix measure.
 * Extends the abstract class Measure
 *
 * @author evillar
 * @version 1.0
 */
public class MeasureMatrix extends Measure {
    
    private int[][] matrix;
    private int numStates;
    private String[] statesNames;
    private String varName;
    private MeasureMatrixIndicators indicators;
    private MeasureMatrixIndProb individualProb;
    private boolean showIndividualProb;
    
    /**
     * This constructor is used when the instance
     * is created in BNEvaluationDialog or in LearningDialog
     *
     * @param type        the measure type (should be {@link MeasureType#CONFUSIONMATRIX})
     * @param statesNames the names of the classification variable states
     * @param varName     the name of the classification variable
     */
    public MeasureMatrix(MeasureType type, String[] statesNames,
                         String varName) {
        super(type);
        this.statesNames = statesNames;
        numStates = statesNames.length;
        matrix = new int[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                matrix[i][j] = 0;
            }
        }
        this.varName = varName;
        indicators = null;
        individualProb = null;
        showIndividualProb = false;
    }
    
    //setters and getters
    public int[][] getMatrix() {
        return matrix;
    }
    
    public int getNumStates() {
        return numStates;
    }
    
    public String[] getStatesNames() {
        return statesNames;
    }
    
    public String getVarName() {
        return varName;
    }
    
    public boolean getShowIndividualProb() {
        return showIndividualProb;
    }
    
    public void setShowIndividualProb() {
        showIndividualProb = true;
    }
    
    /**
     * Sets the individual posterior probabilities for each case and the estimated states.
     *
     * @param caseDatabase    the case database used for evaluation
     * @param prob            posterior probability matrix (cases x states)
     * @param estimatedStates index of the most probable state for each case
     */
    public void setIndividualProb(CaseDatabase caseDatabase,
                                  double[][] prob,
                                  int[] estimatedStates) {
        
        String[] stateMaxProb = new String[caseDatabase.getNumCases()];
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            stateMaxProb[i] = statesNames[estimatedStates[i]];
        }
        individualProb = new MeasureMatrixIndProb(caseDatabase, prob, stateMaxProb);
        
    }
    
    /**
     * this method is called from evaluator
     *
     * @param matrix   the confusion matrix values
     * @param numCases the number of cases used to compute the matrix
     */
    public void setMatrix(int[][] matrix, int numCases) {
        this.matrix = matrix;
        super.setNumCases(numCases);
    }
    
    /** Computes and stores the confusion matrix indicators (TP, FP, precision, F-measure, accuracy). */
    public void setIndicators() {
        indicators = new MeasureMatrixIndicators(matrix, super.getNumCases());
    }
    
    
    /**
     * This method is called when evaluating an algorithm, to add two confusion matrices.
     *
     * @param measure the confusion matrix measure to add
     */
    @Override public void accumulate(Measure measure) {
        int[][] matrixToAdd = ((MeasureMatrix) measure).getMatrix();
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                matrix[i][j] = matrix[i][j] + matrixToAdd[i][j];
            }
        }
        super.setNumCases(super.getNumCases() + measure.getNumCases());
    }
    
    /**
     * This method return a JTable with the confusion matrix
     *
     * @return JTable
     */
    public JTable matrixToTable() {
        
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        String[][] matrixTable = new String[numStates + 1][numStates + 2];
        String[] statesTable = new String[numStates + 2];
        statesTable[0] = "TRUE / PREDICTED->";
        statesTable[numStates + 1] = "Total";
        matrixTable[numStates][0] = "Total";
        int sum = 0;
        for (int i = 0; i < numStates; i++) {
            statesTable[i + 1] = varName + "(" + statesNames[i] + ")";
            matrixTable[i][0] = varName + "(" + statesNames[i] + ")";
            int rowTotal = 0;
            int colTotal = 0;
            for (int j = 1; j < (numStates + 1); j++) {
                matrixTable[i][j] = format.format(matrix[i][j - 1]);
                rowTotal = rowTotal + matrix[i][j - 1];
                colTotal = colTotal + matrix[j - 1][i];
            }
            matrixTable[i][numStates + 1] = format.format(rowTotal);
            matrixTable[numStates][i + 1] = format.format(colTotal);
            sum = sum + rowTotal;
        }
        matrixTable[numStates][numStates + 1] = format.format(sum);
        return new JTable(matrixTable, statesTable);
    }
    
    /**
     * This method return a JTable with the indicators
     *
     * @return JTable
     */
    public JTable indicatorsToTable() {
        return indicators.toTable(varName, statesNames);
    }
    
    /**
     * This method return a JTable with the individual probabilities
     *
     * @return JTable
     */
    
    public JTable probToTable() {
        return individualProb.probToTable(statesNames, varName);
    }
    
    /** Indicators (TP/FP/precision/F-measure/accuracy) computed from the matrix. */
    public MeasureMatrixIndicators getIndicators() { return indicators; }

    /** Per-case posteriors and most-probable state, when enabled. */
    public MeasureMatrixIndProb getIndividualProb() { return individualProb; }

}
