package org.openmarkov.bnEvaluation.measures;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import javax.swing.*;
import java.util.List;

/**
 * Stores individual posterior probabilities for each case in a dataset,
 * along with the most probable state per case. Supports export to JTable and Excel.
 */
public class MeasureMatrixIndProb {
    
    // information used when showing individual probabilities
    private final CaseDatabase caseDatabase;
    private final double[][] prob;
    private final String[] stateMaxProb;
    
    public MeasureMatrixIndProb(CaseDatabase cases, double[][] prob, String[] stateMaxProb) {
        this.caseDatabase = cases;
        this.prob = prob;
        this.stateMaxProb = stateMaxProb;
    }
    
    // Read-only accessors for the export module.

    public CaseDatabase getCaseDatabase() { return caseDatabase; }

    public double[][] getProbabilities() { return prob; }

    public String[] getMostProbableStates() { return stateMaxProb; }

    /**
     * This method returns a JTable with the probabilities
     *
     * @return JTable
     */
    public JTable probToTable(String[] statesNames,
                              String varName) {
        List<Variable> variables = caseDatabase.getVariables();
        int[][] cases = caseDatabase.getCases();
        // initialize variables
        int numVariables = variables.size();
        int ncol = numVariables + statesNames.length + 1;
        String[][] cases_table = new String[caseDatabase.getNumCases()][ncol];
        String[] headers = new String[ncol];
        // headers
        for (int j = 0; j < numVariables; j++) {
            headers[j] = variables.get(j).getName();
        }
        for (int j = 0; j < statesNames.length; j++) {
            headers[numVariables + j] = "P(" + varName + "=" + statesNames[j] + ")";
        }
        headers[numVariables + statesNames.length] = "most probable state";
        // information
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            for (int j = 0; j < variables.size(); j++) {
                cases_table[i][j] = variables.get(j).getStateName(cases[i][j]);
            }
            for (int j = 0; j < statesNames.length; j++) {
                cases_table[i][numVariables + j] = String.format("%.3f", prob[i][j]);
            }
            cases_table[i][numVariables + statesNames.length] = stateMaxProb[i];
        }
        return new JTable(cases_table, headers);
    }
    
}
