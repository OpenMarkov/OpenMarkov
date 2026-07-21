/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;

import java.util.List;

/**
 * {@code NodePotentialEdit} is a simple edit that allows to modify the
 * node's {@code Potential} values. It is implemented for TablePotential
 * Only
 *
 * @author mpalacios
 * @version 1.1 28/05/2016 - cmyago - Eliminated the different treatment of the utility nodes and introduces the behaviour of ExactDistrPotential
 * - adding the attribute getExactDistrPotential
 */
public class TablePotentialValueEdit extends PotentialChangeEdit {
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
    private Double newValue;
    /**
     * A list that store the edition order
     */
    private final List<Integer> priorityList;
    /**
     * The index of the value selected in the graphic table
     */
    private final int indexSelected;
    /**
     * The potential
     */
    private final TablePotential tablePotential;
    /**
     * Old table potential
     */
    private TablePotential oldTablePotential;
    
    /**
     * True is the tablePotential belongs to a ExactDistrPotential
     */
    private final boolean isExactDistrPotential;
    
    /**
     * For doEdit
     */
    private ExactDistrPotential oldExactDistrPotential;
    private ExactDistrPotential exactDistrPotential;
    /**
     * the increment to get the real position of the value modified
     */
    private final int increment;
    
    /**
     * Pseudo-util class with common operations used  in potential tables
     */
    private final PotentialsTablePanelOperations tablePotentialsPanelOperations;

    private final Object[][] notEditablePostitions;
    private final Node node;
    
    // Constructor
    
    /**
     * Creates a new {@code NodePotentialEdit} specifying the node to be
     * edited, the new value of the potential, the row and column where is the
     * value to be modified and a priority list for potentials updating.
     *
     * @param node                 the node to be edited
     * @param newValue             the new value
     * @param col                  the column in the edited table
     * @param row                  the row in the edited table
     * @param priorityList         the priority lists for potentials update.
     * @param notEditablePositions two dimensional array with the information about editable
     *                             positions.
     *                             cmyago added the new initialisation of getExactDistrPotential
     */
    public TablePotentialValueEdit(Node node, Double newValue, int row, int col, List<Integer> priorityList,
                                   Object[][] notEditablePositions) throws ThereIsNoPotentialsInNodeException {
        super(node, null, null);
        this.node = node;
        Potential potential = node.getFirstPotential();
        this.isExactDistrPotential = potential instanceof ExactDistrPotential;
        if (potential instanceof ExactDistrPotential exactDistr) {
            this.oldExactDistrPotential = exactDistr;
            this.oldTablePotential = exactDistr.getTablePotential();
        } else if (potential instanceof TablePotential table) {
            this.oldTablePotential = table;
        }
        this.row = row;
        this.col = col;
        this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();
        this.newValue = newValue;
        this.priorityList = priorityList;
        this.notEditablePostitions = notEditablePositions;
        this.indexSelected = tablePotentialsPanelOperations.calculateLastEditableRow(node) - row;
        this.increment = PotentialsTablePanelOperations.getPotentialStartIndexOfColumn(col, node);

        /**
         * the table potential
         */
        double[] newTable;
        if (isExactDistrPotential) {
            //copy returns null so
            this.exactDistrPotential = new ExactDistrPotential(potential.getVariables(),
                                                               potential.getPotentialRole());
            
            TablePotential newPotential = (TablePotential) (oldExactDistrPotential.getTablePotential().copy());
            this.exactDistrPotential.setTablePotential(newPotential);
            this.tablePotential = newPotential;
            newTable = this.exactDistrPotential.getTablePotential().getValues();
        } else {
            // Reorder the values table of TablePotential
            this.tablePotential = (TablePotential) oldTablePotential.copy();
            // values table reordered
            newTable = this.tablePotential.getValues();
        }
        
        // Get the potential index
        /**
         * Index of the value selected
         */
        int potentialSelected = tablePotentialsPanelOperations.getPotentialIndex(row, col, node);
        
        if (!getExactDistrPotential()) {
            if (priorityList.isEmpty()) {
                // User is editing a new column of potentials //node
                priorityList = getPriorityListInitialization();
            } else {
                // the user is editing a the same column of potentials that last
                // time
                priorityList.remove((Integer) potentialSelected);
                priorityList.add(potentialSelected);
            }
            PotentialsTablePanelOperations.redistributeProbabilities(
                    newTable, potentialSelected, newValue, priorityList, this::isEditablePosition);
            this.oldPotential = oldTablePotential;
            this.newPotential = tablePotential;
        } else {
            newTable[potentialSelected] = newValue;
            tablePotential.getValues()[potentialSelected] = newValue;
            this.oldPotential = oldExactDistrPotential;
            this.newPotential = exactDistrPotential;
        }
    }
    
    /***
     * Checks if the position in the table of tablePotential corresponds to an editable cell if there is a priority list
     * UNCLEAR --&gt; Have I to change the behaviour; depends on doEdit()
     * @param position the position
     * @return true if the cell is editable
     * revised --&gt; not changed
     */
    private boolean isEditablePosition(int position) {
        boolean editable = false;
        int row = getRowPosition(position);
        if (this.notEditablePostitions.length > row && this.notEditablePostitions[0].length > col) {
            if (this.notEditablePostitions[row][col] == null) {
                editable = true;
            }
        } else {
            editable = true;
        }
        return editable;
    }
    
    /**
     * Gets the table-potential of the node
     *
     * @return variable1 {@code Variable}
     */
    public TablePotential getPotential() {
        return tablePotential;
    }
    
    /**
     * Gets the priority list initialisation.
     * Creates a list with the positions in the table of tablePotential of the column containing the edited value.
     * The list first contains the positions corresponding to the not edited cells
     * and the last position corresponds to the edited cell
     *
     * @return the priority list initialised with the the value edited in the
     * last place of the list
     * <p>
     * revised --&gt; not changed
     */
    private List<Integer> getPriorityListInitialization() {
        for (int i = 0; i < node.getVariable().getNumStates(); i++) {
            if (i != indexSelected)
                priorityList.add(i + increment);
        }
        priorityList.add(indexSelected + increment);
        return priorityList;
    }
    
    /**
     * Gets the priority list
     *
     * @return the priority list
     */
    public List<Integer> getPriorityList() {
        return priorityList;
    }
    
    /*
     * private double roundingDouble(double number) { double positions =
     * Math.pow( 10, (double) decimalPositions ); return Math.round( number *
     * positions ) / positions; }
     */
    
    /**
     * Gets the row position associated to value edited if priorityList exists
     *
     * @param position position of the value in the array of values
     * @return the position in the table
     */
    public int getRowPosition(int position) {
        try {
            int lastRow = tablePotentialsPanelOperations.calculateLastEditableRow(node);
            return lastRow - position % tablePotential.getDimensions()[0];
        } catch (ThereIsNoPotentialsInNodeException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Gets the row position associated to value edited if priorityList no
     * exists
     *
     * @return the position in the table
     */
    public int getRowPosition() {
        return row;
    }
    
    /**
     * Gets the column where the value is edited
     *
     * @return the column edited
     */
    public int getColumnPosition() {
        return col;
    }
    

    
    /**
     * @return true if tablePotential comes from a ExactDistrPotential
     */
    public boolean getExactDistrPotential() {
        return isExactDistrPotential;
    }
    
}
