/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.openmarkov.bnEvaluation.measures.MeasureType.LOGLIKELIHOOD;

/**
 * This class stores the set of measures. It can be relative to
 * - a network evaluation (numIterations=1)
 * - an algorithm evaluation (numIterations>1).
 *
 * @author evillar
 * @version 1.0
 */
public class MeasuresSet {
    
    private MeasureMatrix matrix;
    private ArrayList<MeasureValue> measures;
    
    private int numIterations;
    //private int numCases;
    private boolean allVariablesAreUsed;
    private String measureTitle;
    
    
    /**
     * constructor to create an empty object (numIterations=1)
     * Its used in BNEvaluationDialog and CrossValidationDialog
     */
    public MeasuresSet(String measureTitle) {
        matrix = null;
        measures = new ArrayList<>();
        numIterations = 1;
        allVariablesAreUsed = true;
        this.measureTitle = measureTitle;
    }
    
    /**
     * copy constructor to create a measuresSet with the measures empty
     * and numIterations=0 !!!
     *
     * @param measuresSet the template to copy structure from
     */
    public MeasuresSet(MeasuresSet measuresSet) {
        MeasureMatrix matrixToCopy = measuresSet.getMeasureMatrix();
        if (matrixToCopy != null) {
            matrix = new MeasureMatrix(MeasureType.CONFUSIONMATRIX,
                                       matrixToCopy.getStatesNames(),
                                       matrixToCopy.getVarName());
        } else {
            matrix = null;
        }
        measures = new ArrayList<>();
        for (MeasureValue measure : measuresSet.getMeasures()) {
            measures.add(new MeasureValue(measure.getMeasureType()));
        }
        numIterations = 0;
        allVariablesAreUsed = true;
        measureTitle = measuresSet.getMeasureTitle();
    }
    
    // getters
    public MeasureMatrix getMeasureMatrix() {
        return matrix;
    }
    
    public ArrayList<MeasureValue> getMeasures() {
        return measures;
    }
    
    public String getMeasureTitle() {
        return measureTitle;
    }
    
    public int getNumMeasuresValue() {
        return measures.size();
    }
    
    public void setNotAllVariablesAreUsed() {
        allVariablesAreUsed = false;
    }
    
    /**
     * This method adds a new measure to the Array measures
     *
     * @param measure the scalar measure to add
     */
    public void addMeasureValue(MeasureValue measure) {
        measures.add(measure);
    }
    
    /**
     * This method adds a measure to MeasureMAtrix variable
     *
     * @param measure the confusion matrix measure to set
     */
    public void addMeasureMatrix(MeasureMatrix measure) {
        matrix = measure;
    }
    
    /**
     * This method adds a new iteration, add value in each measures
     * and sum numCases
     *
     * @param measureSetToAdd the measure set from one iteration to accumulate
     */
    public void accumulateMeasureSet(MeasuresSet measureSetToAdd) {
        // the sets to accumulate are similar, if matrix is not null, then getMeasureMatrix is also not null
        if (matrix != null) {
            matrix.accumulate(measureSetToAdd.getMeasureMatrix());
        }
        ArrayList<MeasureValue> measuresValue = measureSetToAdd.getMeasures();
        for (int i = 0; i < measures.size(); i++) {
            measures.get(i).accumulate(measuresValue.get(i));
        }
        numIterations = numIterations + 1;
    }
    
    /** Computes averages by dividing accumulated values by the number of iterations. */
    public void setAveraged() {
        // the indicators are calculated with the total of cases
        if (matrix != null) {
            matrix.setIndicators();
        }
        // the value-measures are divided by the number of iterations
        for (MeasureValue measure : measures) {
            measure.averageValue(numIterations);
        }
    }
    
    /**
     * This methods manages the note to use when not all the variables are used
     * to calculate the measures
     *
     * @return a human-readable information string about the measures
     */
    public String getMeasureInformation() {
        String information = measureTitle + "\n";
        if (matrix != null) {
            information = information +
                    "Confusion matrix are calculated with " + matrix.getNumCases() + " cases.\n";
        }
        if (measures.size() > 0) {
            int numCasesScores = measures.get(0).getNumCases();
            information = information + "Scores are calculated with " + numCasesScores +
                    " cases.\n ";
        }
        if (!allVariablesAreUsed) {
            information = information +
                    "The probabilities were calculated without evidence in all the variables.\n";
        }
        return information;
    }
    
    /**
     * Builds the typed list of rows that make up the structured Scores view.
     * Each entry is either a {@link ScoresRow.Section} title or a
     * {@link ScoresRow.Data} measure value, in display order.
     */
    public List<ScoresRow> buildScoresRows() {
        List<ScoresRow> rows = new ArrayList<>();
        if (measures.isEmpty()) {
            return rows;
        }
        boolean logIncluded = measures.get(0).getMeasureType() == LOGLIKELIHOOD;
        rows.add(new ScoresRow.Section(
                logIncluded ? "Goodness of fit Log-likelihood measures" : "Score measures"));
        for (MeasureValue measure : measures) {
            rows.add(new ScoresRow.Data(measure.getMeasureType() + " score", measure.getValue()));
            if (measure.getMeasureType() == LOGLIKELIHOOD) {
                double loss = -measure.getValue() / measure.getNumCases();
                rows.add(new ScoresRow.Data(measure.getMeasureType() + " Loss", loss));
                if (measures.size() > 1) {
                    rows.add(new ScoresRow.Section("Score measures"));
                }
            }
        }
        return rows;
    }

    /**
     * Builds a {@link JTable} backed by the structured rows from
     * {@link #buildScoresRows()}. Section rows have an empty value column,
     * which the dialog renderer interprets as a heading.
     */
    public JTable scoresToTable() {
        List<ScoresRow> rows = buildScoresRows();
        String[][] data = new String[rows.size()][2];
        for (int i = 0; i < rows.size(); i++) {
            ScoresRow row = rows.get(i);
            if (row instanceof ScoresRow.Section section) {
                data[i][0] = section.title();
                data[i][1] = "";
            } else if (row instanceof ScoresRow.Data dataRow) {
                data[i][0] = dataRow.label();
                data[i][1] = String.format("%.3f", dataRow.value());
            }
        }
        return new JTable(data, new String[]{"", ""});
    }
    
    // Read-only accessors for the export module.

    /** Number of evaluation iterations accumulated in this set (≥ 1). */
    public int getNumIterations() { return numIterations; }

    /** Whether every variable in the database had evidence during evaluation. */
    public boolean isAllVariablesAreUsed() { return allVariablesAreUsed; }

}
