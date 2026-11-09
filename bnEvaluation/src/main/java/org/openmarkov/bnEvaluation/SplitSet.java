package org.openmarkov.bnEvaluation;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.util.JTableGeneration;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.Arrays;
import java.util.Comparator;
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
        JTable rawTable = JTableGeneration.dataToJTable(Stream.of("Variable", "State", "Dataset (%)", "Testing (%)", "Training (%)"), rows);
        
        JTable table = new JTable(rawTable.getModel()) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                var isVariableLine = !this.getValueAt(row, 0).toString().isEmpty();
                if (row != 0 && isVariableLine && c instanceof JComponent jc) {
                    jc.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GUIColors.SplitSet.Dialog.VARIABLE_SEPARATOR.getColor()));
                }
                return c;
            }
        };
        
        DefaultTableCellRenderer rightAlignRender = new DefaultTableCellRenderer();
        rightAlignRender.setHorizontalAlignment(JLabel.RIGHT);
        
        table.getColumnModel().getColumn(2).setCellRenderer(rightAlignRender);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlignRender);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlignRender);
        
        return table;
    }
    
}
