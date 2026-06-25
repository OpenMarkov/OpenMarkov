/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.gui.component.AugmentedValuesTable;
import org.openmarkov.gui.component.AugmentedValuesTableModel;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;
import org.openmarkov.gui.component.ValuesTable;
import org.openmarkov.gui.component.ValuesTableModel;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * This class implements a Table potential table with the following features:
 * <ul>
 * <li>Its elements, except the first column, are modifiable.
 * <li>New
 * elements can be added, creating a new key row with empty data.
 * s* <li>The
 * key data (first column) consist of a key string following of the index of the
 * row and it is used for internal purposes only.
 * s* <li>The key data is
 * hidden.
 * s* <li>The information of a row (except the first column) can not
 * be taken up or down.
 * s* <li>The rows can not be removed.
 * s* <li>The first
 * editable row is the one that has the values of the potentials.
 * s* <li>The
 * rows between 0 and the first editable row are ocuppied by the values of the
 * states of the parents of the variable.
 * s* <li>The header of columns is
 * hidden.</li>
 * </ul>
 *
 * @author carmenyago Apr/2017
 */

@PotentialPanelPlugin(potentialClasses = AugmentedProbTablePotential.class)
public class AugmentedProbTablePotentialPanel extends TablePotentialPanel {
    /**
     * Variables of the AugmentedProbTablePotential.
     * The attribute {@code variables} contains the variables of the AugmentedProbTable
     */
    protected List<Variable> potentialVariables;
    
    public AugmentedProbTablePotentialPanel() {
        super();
    }
    
    @Override public void setPotential(Potential potential) {
        super.setPotential(potential);
    }
    
    @Override public void setTablePotential(TablePotential tablePotential) {
        super.setTablePotential(tablePotential);
    }
    
    @Override public AugmentedProbTablePotential getPotential() {
        return (AugmentedProbTablePotential) super.getPotential();
    }
    
    @Override public AugmentedProbTable getTablePotential() {
        return (AugmentedProbTable) super.getTablePotential();
    }
    
    /**
     * This method creates, initialises, and displays an AugmentedValuesTable object for the first potential of the node
     *
     * @param node : node whose first potential is a AugmentedProbTablePotential
     */
    public AugmentedProbTablePotentialPanel(Node node) throws ThereIsNoPotentialsInNodeException {
        super();
        this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();
        this.node = node;
        // This panel displays the first potential of the node
        setPotential(node.getFirstPotential());
        //The table associated to tablePotential
        setTablePotential(getPotential().getAugmentedProbTable());
        
        //The list of variables of the AugmentedProbTablePotential
        potentialVariables = getPotential().getVariables();
        
        //The list of variables of the AugmentedProbTable of TablePotential
        variables = getTablePotential().getVariables();
        
        // Creating the table; class AugmentedValuesTable
        valuesTable = new AugmentedValuesTable(node, getTableModel(), modifiable);
        valuesTable.setName("PotentialsTablePanel.augmentedValuesTable");
        valuesTable.setVisible(true);
        modifiable = true;
        // Previous--&gt;Ok
        setTableSpecificListeners();
        
        setData();
        
        setLayout(new BorderLayout());
        
        // If the ScrollPane is not created, initialise it and set the Viewport.
        // Then add the element to the Layout.
        add(getValuesTable(), BorderLayout.CENTER);
        
        repaint();
    }
    
    /**
     * This method returns the tableModel of valuesTable. If valuesTable has not a tableModel, this method creates one.
     *
     * @return the tableModel of valuesTable.
     *
     * @see ValuesTable
     * revised--&gt;minor changes
     */
    @Override protected ValuesTableModel getTableModel() {
        if ((valuesTable == null) || (valuesTable.getTableModel() == null)) {
            return new AugmentedValuesTableModel(data, columns, firstEditableRow);
        }
        return (AugmentedValuesTableModel) valuesTable.getModel();
        
    }
    
    /**
     * Sets a new table model with new data and new columns based on three
     * items:
     * <ul>
     * <li>list of Potentials of the variable
     * <li>states of the variable
     * <li>parents of the variable
     * </ul>
     * This method obtains if the node has link restrictions and store it in hasLinkrestriction,
     * stores the probNet in ValuesTable
     * fills the tableData (tableData consists of headers + data),
     * sets the columns name in a Excel mode (A,B,C,...AA,AB...),
     * sets the tableModel in valuesTable( tableData + column names),
     * sets uncertaintyInColumns with the columns with uncertainty,
     * sets the cell renders according to the type of node, and
     * in the tableMoel, sets the not editable cells due to links restrictions and uncertainty in columns.
     * Finally, this method adjust the size of the cells in valuesTable
     * UNCLEAR--&gt; It is necessary to Override??
     *
     * @author carmenyago
     */
    @Override public void setData() throws ThereIsNoPotentialsInNodeException {
        
        // true
        hasLinkRestriction = LinkRestrictionPotentialOperations.hasLinkRestriction(node);
        // Sets the probNet in the table
        
        valuesTable.setData(node);
        
        uncertaintyInColumns = null;
        
        // tableData contains the table to be displayed in ValuesTable
        // No override. Overriding some internal methos
        Object[][] tableData = convertListPotentialsToTableFormat();
        
        // Sets the column names in Excel style: A, B, C,....AA,AB...
        // These column names aren't displayed
        String[] newColumns = ValuesTable.getColumnsIdsSpreadSheetStyle(tableData[0].length);
        
        //Sets the table model in valuesTable
        //No override
        setDataInValuesTable(tableData, newColumns);
        
        // set the Cell Renders according to NodeType (a different renderer for some DECISON nodes) and the uncertainty
        setCellRenderers(uncertaintyInColumns);
        
        //this.getTableModel().setNotEditablePositions(getNotEditablePositions());
        
        // Establish the column width
        valuesTable.fitColumnsWidthToContent();
    }
    
    /**
     * Sets a new table model with new data and new columns in valuesTable
     * Only to put and AugmentedValuesTableModel--&gt;Change in values table
     *
     * @param newData    new data for the table
     * @param newColumns new columns for the table
     */
    @Override public void setDataInValuesTable(Object[][] newData, String[] newColumns) {
        
        // Table data
        data = newData.clone();
        // Table columns
        columns = newColumns.clone();
        
        // resets the tableModel
        valuesTable.resetModel();
        
        // Sets the valuesTable tableModel with columns, data
        valuesTable.setModel(new AugmentedValuesTableModel(data, columns, firstEditableRow));
        
        // Initialises a false an array which tells which data are modified
        valuesTable.initializeDataModified(false);
        
        valuesTable.setLastEditableRow(lastEditableRow);
        valuesTable.setFirstEditableRow(firstEditableRow);
        
        //show/hide rows based on the showingAllParameters attribute using a RowFilter mechanism.
        valuesTable.setShowingAllParameters(true);
        
        valuesTable.setNodeType(node.getNodeType());
    }
    
    /**
     * Creates and empty array of empty objects with the [number_of_rows][number_of_columns] of the valuesTable
     * Considers the potential is not null
     * Minor override only necessary for calculateFirstEditableRow,also removed tableDeltaPotential lines
     *
     * @author carmenyago
     * <p>
     * Continuous variables have only one state
     * tableSize is always greater than 0
     */
    @Override protected Object[][] createEmptyTable() {
        
        int numColumns = 1; // Variables column
        AugmentedProbTable tablePotential = getTablePotential();
        // First editable row coincides with the number of parents
        //CHANGE (minor node by tablePotential
        firstEditableRow = PotentialsTablePanelOperations.calculateFirstEditableRow(tablePotential);
        
        // The baseIndexForCoordinates is the first editable row--&gt;What for--&gt;UNCLEAR
        // The property baseIndexForCoordinates is not Visible. baseIndexForCoordinates= row
        setBaseIndexForCoordinates(firstEditableRow);
        
        // Number of data elements of tablePotential
        int tableSize = tablePotential
                .getTableSize();//--&gt;UNCLEAR What happens when there is no parent (f.e. when Tree/ADD )
        
        // Number of states of the variable of the node; if isTableDeltaPotential numDimensions=1
        int numDimensions = tablePotential.getDimensions()[0];
        // Parent variables + states of node variable
        int numRows = firstEditableRow + numDimensions;
        lastEditableRow = numRows - 1;
        
        /*if (!isTableDeltaPotential) numRows++;*/ //--&gt; UNCLEAR Last row with the name of the variable and the state with '1' is REMOVED
        numColumns = numColumns + tableSize / numDimensions;
        
        // create the array of arrays
        return new Object[numRows][numColumns];
    }
    
    /**
     * Sets the data table from potential in oldValues
     *
     * @param oldValues the old values
     *
     * @return an array filled with the date table from tablePotential or tableDeltaPotential filled with the data values
     * from tablePotential or tableDeltaPotential in the correct positions to be displayed by ValuesTable
     */
    @Override protected Object[][] setPotentialDataInCentreArea(Object[][] oldValues) {
        Object[][] values = oldValues;
        
        int numColumns = values[0].length;
        
        AugmentedProbTable tablePotential = getTablePotential();
        VariableExpression[] initialValues = tablePotential.getFunctionValues();
        for (int j = 1; j <= numColumns - 1; j++) {
            
            // put the values on the table
            for (int i = getLastEditableRow(); i >= getFirstEditableRow(); i--) {
                int potentialIndex = PotentialsTablePanelOperations.getPotentialIndex(i, j, tablePotential);
                VariableExpression value = initialValues[potentialIndex];
                values[i][j] = value;
            }
        }
        return values;
    }
    
    /**
     * This method calculates the number of data cells and stores it in the attribute positions.
     * The number of data cell is the product of the number of states of all variables
     * <p>
     * Override because we need the variables in tablePotential no in potential
     */
    @Override protected long setNumberOfPostions() {
        AugmentedProbTable tablePotential = getTablePotential();
        long numPositions = 1;
        for (Variable variable : tablePotential.getVariables()) {
            numPositions = numPositions * variable.getNumStates();
        }
        setPosition(numPositions);
        return numPositions;
    }
    
    /**
     * This method initialises valuesTable and defines that first two columns cannot be selected
     *
     * @return a new values table.
     * revised--&gt;not changed
     */
    @Override public ValuesTable getValuesTable() {
        if (valuesTable == null) {
            valuesTable = new ValuesTable(node, getTableModel(), modifiable);
            valuesTable.setName("PotentialsTablePanel.valuesTable");
        }
        return valuesTable;
    }
    
    //	/**
    //	 * Handles an action performed
    //	 * revised--&gt;not changed
    //	 */
    //	public void actionPerformed(ActionEvent e) {
    //		String actionCommand = e.getActionCommand();
    //		if (actionCommand.equals(ActionCommands.UNCERTAINTY_ASSIGN)
    //				|| actionCommand.equals(ActionCommands.UNCERTAINTY_EDIT)) {
    //			try {
    //				showUncertaintyDialog();
    //			} catch (WrongCriterionException e1) {
    //				e1.printStackTrace();
    //				JOptionPane.showMessageDialog(this,
    //						stringDatabase.getValuesInAString(e1.getMessage()),
    //						stringDatabase.getValuesInAString(e1.getMessage()),
    //						JOptionPane.ERROR_MESSAGE);
    //			}
    //		} else if (actionCommand.equals(ActionCommands.UNCERTAINTY_REMOVE)) {
    //			try {
    //				removeUncertainty();
    //			} catch (WrongCriterionException e1) {
    //				e1.printStackTrace();
    //				JOptionPane.showMessageDialog(this,
    //						stringDatabase.getValuesInAString(e1.getMessage()),
    //						stringDatabase.getValuesInAString(e1.getMessage()),
    //						JOptionPane.ERROR_MESSAGE);
    //			}
    //		}
    //	}
    
    /**
     * Method to define the specific listeners in this table (not defined in the
     * common KeyTable hierarchy. This method creates the evidence object
     * when the user do right click on the table.
     */
    @Override protected void setTableSpecificListeners() {
        valuesTable.onTables(omjTable -> omjTable.addMouseListener(new MouseClickedListener()));
    }
    
    @Override
    public boolean saveChanges() throws BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong, DoEditException {
        return super.saveChanges();
    }
    
    @Override public void close() {
        super.close();
    }
    
    /**
     * This class overrides the double click listener calling the
     */
    private class MouseClickedListener extends MouseAdapter {
        
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                int row = valuesTable.rowAtPoint(e.getPoint(), e.getSource());
                int column = valuesTable.columnAtPoint(e.getPoint(), e.getSource());
                var expression = valuesTable.getValueAt(row, column, e.getSource()).toString();
                AugmentedProbTablePotential potential = getPotential();
                List<Variable> parameterVariables = potential.getParameterVariables();
                ArithmeticExpressionDialog expressionDialog =
                        new ArithmeticExpressionDialog(null, parameterVariables, expression);
                expressionDialog.setVisible(true);
                if (expressionDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
                    valuesTable.setValueAt(new VariableExpression(parameterVariables, expressionDialog.getExpression()), row, column, e.getSource());
                    //TODO: Continue here changing String expressions to VariableExpression expressions.
                    //valuesTable.setValueAt(new VariableExpression(parameterVariables, expressionDialog.getExpression()), row, column);
                }
            }
            if (e.getClickCount() == 2) {
                //TODO; now it does nothing
            }
        }
    }
    
}




