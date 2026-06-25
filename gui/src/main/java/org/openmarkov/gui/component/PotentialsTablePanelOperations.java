/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

//import java.util.ArrayList;

import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.List;
import java.util.function.IntPredicate;


/**
 * Auxiliary methods for PotentialsTablePanel class
 *
 * @author jlgozalo
 * @author Manuel Arias
 * @version 2.0 cmyago - 27/05/2016
 */
public class PotentialsTablePanelOperations {

	/**
	 * calculate the first editable Row of the table, based upon the number of parents for the node.
	 * The first editable row equals the number of parents of the node
	 *
	 * @param node - node with contains the potentials
	 */
    public int calculateFirstEditableRow(Node node) {
		return node.getPotentials().stream().findFirst().map(p -> p.getNumVariables()-1).orElse(0);
	}

	/**
	 * This method calculates the last editable row of the table.
	 * The last editable row is (number_of_parents of the node + number_of_states of the variable node)
	 * <p>
	 * @param node node who "owns" the table
	 */
    public int calculateLastEditableRow(Node node) throws ThereIsNoPotentialsInNodeException {
        Potential potential = node.getFirstPotential();
        if (PotentialsTablePanelOperations.getIsExactDistrPotential(potential)) {
			return potential.getNumVariables() - 1;
		}
        return node.getFirstPotential().getNumVariables() - 1 + node.getVariable().getStates().length - 1;
    }
	
	/**
	 * True if the class of the potential is ExactDistrPotential
	 *
	 * @param potential - The potential to check
	 * @return true if the class of the potential is ExactDistrPotential; false otherwise
	 */
    public static boolean getIsExactDistrPotential(Potential potential) {
		return (potential instanceof ExactDistrPotential);
		// potential.getClass().getName().equals("org.openmarkov.core.model.network.potential.ExactDistrPotential");
	}
    
    public int getPotentialIndex(int row, int column, Node node) throws ThereIsNoPotentialsInNodeException {
		// First of all we get the start index of the column
		int potentialIndex = getPotentialStartIndexOfColumn(column, node);
		// We get the last editable row in the JTable
		int lastRow = calculateLastEditableRow(node);
		// Then we move a number of positions equals to the row (without the headers)
		potentialIndex += (lastRow - row);
		return potentialIndex;
	}


	/**
	 * Created for univariateDistributions
	 */
    
    public static int getPotentialIndex(int row, int column, TablePotential tableDistribution) {
		// First of all we get the start index of the column
		int potentialIndex = getPotentialStartIndexOfColumn(column, tableDistribution);

		// We get the last editable row in the JTable
		int lastRow = calculateLastEditableRow(tableDistribution);

		// Then we move a number of positions equals to the row (without the headers)
		potentialIndex += (lastRow - row);
		return potentialIndex;
	}
    
    public static int getPotentialStartIndexOfColumn(int column, TablePotential tablePotential) {

		// Index in tablePotential of the beginning of the column
		int position = 0;
		// Making the column 1 as the first (column 0)
		int temp = column - 1;

		// In this code we get the coordinates (states index) of the variable and
		// we calculate the position in the list of potentials. The position
		// The position is the product of each state index and the respective offset
		// s[0]*offset[0] + s[1]*offset[1] + ..... + s[n]*offset[n]

		// Dimensions--&gt; list with the states of each variable of the potential
		//
		// Now there is no difference between CHANCE and UTILITY
		int[] dimensions = tablePotential.getDimensions();
        if (dimensions == null) {
			return 0;
        }
        int numberOfDimensions = dimensions.length - 1;
        
        int lowerBound = 0;
		//if (getIsExactDistrPotential(potential)) lowerBound = -1;
		for (int i = numberOfDimensions; i > lowerBound; i--) {
			int dimension = dimensions[i];
			position += (temp % dimension) * tablePotential.getOffsets()[i];
			temp = temp / dimension;
		}
		return position;
	}
    
    public static int calculateFirstEditableRow(TablePotential potential) {
        return potential.getNumVariables() - 1;
	}
    
    public static int calculateLastEditableRow(TablePotential potential) {
        return potential.getNumVariables() - 1 + potential.getVariable(0).getStates().length - 1;
	}


	/**
	 * Given the number of column of a JTable,
	 * this method calculates the index in the table of a first potential of a node
	 * corresponding to the first cell in the column.
	 * If there is no potential or the potential has no states, the method returns 0 (keeping the previous behaviour)
	 *
	 * @param column - the index of a column
	 * @param node   - the node with the potential
	 * @return index of the potential.
	 */
    public static int getPotentialStartIndexOfColumn(int column, Node node) throws ThereIsNoPotentialsInNodeException {
		/*
		 * This code is here and in getPotentialIndex because this method is used not only in  getPotentialIndex
		 * but in org.openmarkov.gui.action.TablePotentialValueEdit
		 */
        Potential potential = node.getFirstPotential();
        TablePotential tablePotential;

		if (potential instanceof ExactDistrPotential exactDistr)
			tablePotential = exactDistr.getTablePotential();
		else
			tablePotential = (TablePotential) potential;

		// Index in tablePotential of the beginning of the column
		int position = 0;
		// Making the column 1 as the first (column 0)
		int temp = column - 1;

		// Supposing Dimensions >=1 UNCLEAR

		// In this code we get the coordinates (states index) of the variable and
		// we calculate the position in the list of potentials. The position
		// The position is the product of each state index and the respective offset
		// s[0]*offset[0] + s[1]*offset[1] + ..... + s[n]*offset[n]

		// Dimensions--&gt; list with the states of each variable of the potential
		//
		// Now there is no difference between CHANCE and UTILITY
		int[] dimensions = tablePotential.getDimensions();
        if (dimensions == null) {
			return 0;
        }
        int numberOfDimensions = dimensions.length - 1;
        
        int lowerBound = 0;
		if (getIsExactDistrPotential(potential))
			lowerBound = -1;
		for (int i = numberOfDimensions; i > lowerBound; i--) {
			int dimension = dimensions[i];
			position += (temp % dimension) * tablePotential.getOffsets()[i];
			temp = temp / dimension;
		}
		return position;
	}

	/**
	 * Redistributes probability values so they sum to 1.0, following the priority list order.
	 * Sets the value at {@code editedPosition} first, then adjusts others by priority:
	 * if the sum exceeds 1.0, values are decremented in priority order;
	 * otherwise the remainder is added to the first editable position.
	 *
	 * @param values          the probability array to modify in place
	 * @param editedPosition  the index that was just edited
	 * @param newValue        the new value for the edited position
	 * @param priorityList    positions ordered by editing priority (least recently edited first)
	 * @param isEditable      predicate that returns true for positions eligible for redistribution
	 */
	public static void redistributeProbabilities(
			double[] values, int editedPosition, double newValue,
			List<Integer> priorityList, IntPredicate isEditable) {
		int maxDecimals = 10;
		double epsilon = Math.pow(10, -(maxDecimals + 2));
		values[editedPosition] = Util.roundAndReduce(newValue, epsilon, maxDecimals);

		double sum = 0.0;
		for (int pos : priorityList) {
			if (isEditable.test(pos)) {
				sum = Util.roundAndReduce(sum + values[pos], epsilon, maxDecimals);
			}
		}

		double rest = Math.abs(Util.roundAndReduce(1 - sum, epsilon, maxDecimals));
		if (sum > 1.0) {
			for (int pos : priorityList) {
				if (rest == 0) break;
				if (isEditable.test(pos)) {
					rest = Util.roundAndReduce(rest - values[pos], epsilon, maxDecimals);
					if (rest < 0) {
						values[pos] = Math.abs(Util.roundAndReduce(rest, epsilon, maxDecimals));
						break;
					}
					values[pos] = 0.0;
				}
			}
		} else {
			for (int pos : priorityList) {
				if (isEditable.test(pos)) {
					values[pos] = Util.roundAndReduce(values[pos] + rest, epsilon, maxDecimals);
					break;
				}
			}
		}
	}

}

