/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.core.ICIPotentialEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;

import java.util.List;

/**
 * Edit that modifies a single cell value in an ICI (noisy-OR/noisy-MAX) potential table,
 * automatically redistributing probabilities across the column to ensure they sum to 1.
 * Supports both noisy parameters and leaky parameters.
 */
@SuppressWarnings("serial") public class ICITablePotentialValueEdit extends PNEdit {
    
    /**
     * The column of the table where is the potential
     */
    private final int col;
    /**
     * The row of the table where is the potential
     */
    private final int row;
    /**
     * The new value of the potential
     */
    private final Double newValue;
    /**
     * The node
     */
    private final Node node;
    /** The ICI potential being edited. */
    private final ICIPotential iciPotential;
    /** The variables of the ICI potential. */
    private final List<Variable> variables;
    /** The noisy parameters before the edit. */
    private double[] lastNoisyParameters;
    /** The noisy parameters after the edit. */
    private double[] newNoisyParameters;
    /** The parent variable whose noisy parameters are being edited. */
    private Variable noisyVariable;
    /** The leaky parameters before the edit. */
    private double[] lastLeakyParameters;
    /** The leaky parameters after the edit. */
    private double[] newLeakyParameters;
    /** Whether the edit targets leaky parameters (true) or noisy parameters (false). */
    private final boolean leakyFlag;
    
    //
    private final int position;
    
    private int columnGroup;
    /** Index of the selected value within the column group. */
    private final int indexSelected;
    /** Number of conditioned (child) states. */
    private final int conditionedStates;
    /**
     * A list that store the edition order
     */
    private List<Integer> priorityList;
    // Constructor
    
    /**
     * Creates a new {@code NodePotentialEdit} specifying the node to be
     * edited, the new value of the potential, the row and column where is the
     * value to be modified and a priority list for potentials updating.
     *
     * @param node     the node to be edited
     * @param newValue the new value
     * @param col      the column in the edited table
     * @param row      the row in the edited table
     */
    public ICITablePotentialValueEdit(Node node, Double newValue, int row, int col, List<Integer> priorityList) {
        super(node.getProbNet());
        this.node = node;
        this.row = row;
        this.col = col;
        this.newValue = newValue;
        this.priorityList = priorityList;
        this.indexSelected = node.getVariable().getNumStates() - (
                row - 2 + 1
        );
        
        this.iciPotential = getThisICIPotential(node.getPotentials());
        this.variables = iciPotential.getVariables();
        
        this.conditionedStates = variables.get(0).getNumStates();
        int[] numColumnsParents = new int[variables.size()];
        for (int i = 1; i < variables.size(); ++i) {
            numColumnsParents[i - 1] = variables.get(i).getNumStates();
        }
        numColumnsParents[variables.size() - 1] = 1;
        
        int[] acummulativeColumns = new int[variables.size()];
        acummulativeColumns[0] = numColumnsParents[0];
        for (int i = 1; i < numColumnsParents.length; ++i) {
            acummulativeColumns[i] = numColumnsParents[i] + acummulativeColumns[i - 1];
        }
        
        columnGroup = 0;
        //leak
        //columnGroup is 0 when leaky parameters
        if (col == acummulativeColumns[acummulativeColumns.length - 1]) {//last column for the table leak potential
            leakyFlag = true;
            this.lastLeakyParameters = iciPotential.getLeakyParameters();
            this.position = (columnGroup) * conditionedStates + (conditionedStates + 1) - row;
            newLeakyParameters = lastLeakyParameters.clone();
            
            //initializes priorityList for the leaky potential
			/*for (int i = 0 ;i <conditionedStates; i++){
				this.priorityList.add(i);
			}*/
            
            //noisy
        } else {
            leakyFlag = false;
            for (int i = 0; i < acummulativeColumns.length - 1; ++i) {
                if (i == 0) {
                    if (col <= acummulativeColumns[i]) {
                        this.noisyVariable = variables.get(i + 1);//first variable
                        columnGroup = (col - 1);
                        break;
                    }
                    
                } else if (acummulativeColumns[i - 1] < col && col <= acummulativeColumns[i]) {
                    this.noisyVariable = variables.get(i + 1);
                    columnGroup = (col - 1) - acummulativeColumns[i - 1];//offset within the noisy parameters array
                    break;
                }
            }
			/*int offset = 0;
			//initialize priorList for noisy potential
			for (int i = 0; i<noisyVariable.getNumStates(); i++){//columngroup
				if (columnGroup == i){
					for (int j = 0 ;j<conditionedStates; j++){
						this.priorityList.add(j+offset);
					}
					break;
				}
				offset += conditionedStates;
			}*/
            
            this.lastNoisyParameters = iciPotential.getNoisyParameters(noisyVariable);
            // number of previous columns of the variable*number of conditioned states + number of rows -1 - row
            this.position = (columnGroup) * conditionedStates + (conditionedStates + 1) - row;
            
            newNoisyParameters = lastNoisyParameters.clone();
            
        }
        
    }
    
    /**
     * Converts an internal parameter index into the corresponding JTable row position.
     *
     * @param index       the parameter index
     * @param columnGroup the column group offset
     * @param numOfStates the number of conditioned states
     * @return the row position in the JTable
     */
    public static int toPositionOnJtable(int index, int columnGroup, int numOfStates) {
        
        return (columnGroup * numOfStates) + numOfStates + 1 - index;
        
    }
    
    @Override protected void doEdit() throws DoEditException {
        if (priorityList.isEmpty()) {
            //User is editing a new column of potentials //node
            priorityList = getPriorityListInitialization();
            
        } else {
            //the user is editing the same column of potentials that last
            //time
            priorityList.remove((Integer) position);
            priorityList.add(position);
            
        }
        
        double[] params = leakyFlag ? newLeakyParameters : newNoisyParameters;
        PotentialsTablePanelOperations.redistributeProbabilities(
                params, position, newValue, priorityList, pos -> true);

        ICIPotentialEdit iciPotentialEdit = leakyFlag
                ? new ICIPotentialEdit(probNet, iciPotential, newLeakyParameters)
                : new ICIPotentialEdit(probNet, iciPotential, noisyVariable, newNoisyParameters);
        iciPotentialEdit.executeEdit();
    }
    
    @Override public void undo() {
        super.undo();
    }
    
    /**
     * Initializes the priority list for probability redistribution.
     * The edited cell is placed last so it has the lowest priority during redistribution.
     *
     * @return the initialized priority list
     */
    public List<Integer> getPriorityListInitialization() {
        
        if (!leakyFlag) {
            //noisy parameters
            int offset = 0;
            //initialize priorList for noisy potential
            for (int i = 0; i < noisyVariable.getNumStates(); i++) {//columngroup
                if (columnGroup == i) {
                    for (int j = 0; j < conditionedStates; j++) {
                        
                        if (j != indexSelected)
                            this.priorityList.add(j + offset);
                    }
                    priorityList.add(indexSelected + offset);
                    break;
                }
                offset += conditionedStates;
            }
        } else {//leaky parameters
            //initializes priorityList for the leaky potential
            for (int i = 0; i < conditionedStates; i++) {
                if (i != indexSelected)
                    priorityList.add(i);
            }
            priorityList.add(indexSelected);
        }
        return priorityList;
    }
    
    /**
     * Retrieves the ICI potential from a node's potential list.
     *
     * @param listPotentials the list of potentials belonging to the node
     * @return the first potential cast as an {@code ICIPotential}
     */
    private static ICIPotential getThisICIPotential(List<Potential> listPotentials) {
        return ((ICIPotential) listPotentials.get(0));
    }
    
    public boolean getLeakyFlag() {
        return leakyFlag;
    }
    
    /**
     * Gets the new value
     *
     * @return newValue
     */
    public double getNewValue() {
        return newValue;
    }
    
    /**
     * Gets the table-potential of the node
     *
     * @return iciPotential
     */
    public ICIPotential getPotential() {
        return iciPotential;
    }
    
    /**
     * Gets the table-potential of the node
     *
     * @return variable1 {@code Variable}
     */
    public Variable getNoisyVariable() {
        return noisyVariable;
    }
    
    /**
     * Gets the position edited
     *
     * @return position {@code Integer}
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * Gets the row position associated to value edited if priorityList no exists
     *
     * @return the position in the table
     */
    public int getRowPosition() {
        return row;
    }
    
    public int getRowPosition(int position) {
        
        return toPositionOnJtable(position, columnGroup, node.getVariable().
                                                             getNumStates());
        
    }
    
    /**
     * Gets the column position associated to value edited if priorityList no exists
     *
     * @return the position in the table
     */
    public int getColumnPosition() {
        return col;
    }
    
    /**
     * Gets the priority list
     *
     * @return the priority list
     */
    public List<Integer> getPriorityList() {
        return priorityList;
    }
    
    public double[] getNewNoisyValues() {
        return newNoisyParameters;
    }
    
    public double[] getNewLeakyValues() {
        return newLeakyParameters;
    }
    
    public double[] getLastNoisyValues() {
        return lastNoisyParameters;
    }
    
    public double[] getLastLeakyValues() {
        return lastLeakyParameters;
    }
    
    /*
     * First position is the noisy potential
     * Second position is the index within it
     * @param row the row
     * @param column the column
     * @return the result
     */
	/*private double roundingDouble(double number) {

		double positions = Math.pow( 10, (double) decimalPositions );
		return Math.round( number * positions ) / positions;
	}*/
 
}
