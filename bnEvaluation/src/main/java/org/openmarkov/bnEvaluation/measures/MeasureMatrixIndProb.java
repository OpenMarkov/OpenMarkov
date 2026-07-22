package org.openmarkov.bnEvaluation.measures;

import org.openmarkov.core.model.database.CaseDatabase;

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

}
