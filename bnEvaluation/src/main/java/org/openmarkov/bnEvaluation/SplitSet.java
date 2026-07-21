package org.openmarkov.bnEvaluation;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.gui.util.JTableGeneration;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;


/**
 * The SplitSet class stores two sets of data: testDatabase and trainDatabase
 * The division of a CaseDatabase into two sets is performed by the class SplitSetManager
 */
public class SplitSet {
    private CaseDatabase testDatabase;
    private CaseDatabase trainDatabase;
    private String title;
    
    public SplitSet(CaseDatabase testDataBase, CaseDatabase trainDataBase) {
        this.testDatabase = testDataBase;
        this.trainDatabase = trainDataBase;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public CaseDatabase getTestDatabase() {
        return testDatabase;
    }
    
    public CaseDatabase getTrainDatabase() {
        return trainDatabase;
    }
    
    /**
     * This method returns the relative frequencies of cases in each variable in
     * test-set, train-set and the complete set of data in a JTable
     *
     * @return JTable
     */
    public JTable toTable() {
        int numTestCases = testDatabase.getNumCases();
        int numTrainCases = trainDatabase.getNumCases();
        int numTotalCases = numTrainCases + numTestCases;
        var rows = testDatabase.getVariables().stream().flatMap(variable -> {
            int[] casesTest = testDatabase.getCases(variable);
            int[] casesTrain = trainDatabase.getCases(variable);
            int[] freqTest = new int[variable.getNumStates()];
            for (int i = 0; i < numTestCases; i++) {
                freqTest[casesTest[i]] += 1;
            }
            int[] freqTrain = new int[variable.getNumStates()];
            for (int i = 0; i < numTrainCases; i++) {
                freqTrain[casesTrain[i]] += 1;
            }
            AtomicBoolean isFirstState = new AtomicBoolean(true);
            return Arrays.stream(variable.getStates())
                         .sorted(Comparator.comparing(State::getName))
                         .map(state -> {
                             int stateIndex = variable.getStateIndex(state);
                             return Stream.of(
                                     isFirstState.getAndSet(false) ? variable.getName() : "",
                                     state.getName(),
                                     String.format("%3.2f", 100. * (freqTest[stateIndex] + freqTrain[stateIndex]) / numTotalCases),
                                     String.format("%3.2f", 100. * freqTest[stateIndex] / numTestCases),
                                     String.format("%3.2f", 100. * freqTrain[stateIndex] / numTrainCases)
                             );
                         });
        });
        return JTableGeneration.dataToJTable(Stream.of("Variable", "State", "Dataset (%)", "Testing (%)", "Training (%)"), rows);
    }
    
}
