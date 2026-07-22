/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import org.openmarkov.bnEvaluation.measures.MeasureMatrixIndProb;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * View adapter that renders the per-case posterior probabilities held by a
 * {@link MeasureMatrixIndProb} as a table: one column per database variable (showing the observed
 * state), one {@code P(var=state)} column per class state and a final {@code most probable state}
 * column, with one row per case.
 *
 * @author Manuel Arias
 */
public final class IndividualProbabilityTableModel extends ReadOnlyStringTableModel {

    public IndividualProbabilityTableModel(MeasureMatrixIndProb individualProb, String[] statesNames, String varName) {
        super(build(individualProb, statesNames, varName));
    }

    private static Grid build(MeasureMatrixIndProb individualProb, String[] statesNames, String varName) {
        CaseDatabase caseDatabase = individualProb.getCaseDatabase();
        double[][] prob = individualProb.getProbabilities();
        String[] stateMaxProb = individualProb.getMostProbableStates();
        List<Variable> variables = caseDatabase.getVariables();
        int[][] cases = caseDatabase.getCases();
        int numVariables = variables.size();
        int numColumns = numVariables + statesNames.length + 1;

        String[] columnNames = new String[numColumns];
        for (int j = 0; j < numVariables; j++) {
            columnNames[j] = variables.get(j).getName();
        }
        for (int j = 0; j < statesNames.length; j++) {
            columnNames[numVariables + j] = "P(" + varName + "=" + statesNames[j] + ")";
        }
        columnNames[numVariables + statesNames.length] = "most probable state";

        String[][] cells = new String[caseDatabase.getNumCases()][numColumns];
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            for (int j = 0; j < variables.size(); j++) {
                cells[i][j] = variables.get(j).getStateName(cases[i][j]);
            }
            for (int j = 0; j < statesNames.length; j++) {
                cells[i][numVariables + j] = String.format("%.3f", prob[i][j]);
            }
            cells[i][numVariables + statesNames.length] = stateMaxProb[i];
        }
        return new Grid(columnNames, cells);
    }
}
