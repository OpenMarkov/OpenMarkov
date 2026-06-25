/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.util.List;

/**
 * Adapted class from Augmented Table, to be used with Table With Events
 * It allows to use a table with functions instead of numeric values
 */
public class TableWithFunctions extends TablePotential {
	/**
	 * The default function
	 */
	private static final String DEFAULT_FUNCTION = "1";

	private String[] functionValues;

	/*Note should be discrete variables*/
	public TableWithFunctions(List<Variable> stateVariables, PotentialRole role) {

		super(stateVariables, role);
		int numVariables = (stateVariables != null) ? stateVariables.size() : 0;
		if (numVariables != 0) {
			// Number of states of each variable
			dimensions = calculateDimensions(stateVariables);
			int cellsInRow = 1;
			for (int i = 1; i < dimensions.length; i++) {
				cellsInRow *= dimensions[i];
			}
			offsets = calculateOffsets(dimensions);
			tableSize = computeTableSize(stateVariables);
			try {
				functionValues = new String[tableSize];
				// Get  public int getPosition(int[] coordinates)
				int increment = variables.get(0).getNumStates();
				for (int i = 0; i < tableSize; i++) {
					functionValues[i] = DEFAULT_FUNCTION;
				}
				for (int i = 0; i < tableSize; i += increment) {
					functionValues[i] = DEFAULT_FUNCTION;
				}

			} catch (NegativeArraySizeException e) {
				throw new OutOfMemoryError();
			}
		} else {// In this case the potential is a constant
			tableSize = 1;
			setFunctionValues(new String[tableSize]);
			offsets = new int[0];
		}
	}

	public TableWithFunctions(List<Variable> stateVariables, PotentialRole role, String[] functionValues) {
		this(stateVariables, role);
		this.functionValues = functionValues;
	}

	/**
	 * Internal constructor used to create a projected potential.
	 *
	 * @param stateVariables  . <code>ArrayList</code> of <code>Variable</code>
	 * @param role            . <code>PotentialRole</code>
	 * @param table           . <code>double[]</code>
	 * @param initialPosition First position in <code>table</code> (used in projected
	 *                        potentials).
	 * @param offsets         of variables. <code>int[]</code>
	 * @param dimensions      . Number of states of each variable. <code>int[]</code>
	 */
	private TableWithFunctions(List<Variable> stateVariables, PotentialRole role, String[] table, int initialPosition,
                               int[] offsets, int[] dimensions) {
		super(stateVariables, role);
		// this.originalVariables = this.variables;
		this.setFunctionValues(table);
		this.initialPosition = initialPosition;
		this.offsets = offsets;
		this.dimensions = dimensions;
		tableSize = computeTableSize(stateVariables);
	}

	public TableWithFunctions(TableWithFunctions potential) {
		super(potential);
		this.initialPosition = potential.getInitialPosition();
		this.offsets = potential.getOffsets();
		this.dimensions = potential.getDimensions();
		tableSize = potential.tableSize;
		//UNCLEAR Clones??
		setFunctionValues(potential.getFunctionValues().clone());

		//UNCLEAR???
		//strategyTrees = potential.strategyTrees;
	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role.
	 * Firstly I suppose we have only discrete variables and at least one parent is NUMERIC
	 *
	 * @param node      . <code>Node</code>
	 * @param variables . <code>List</code> of <code>Variable</code>.
	 * @param role      . <code>PotentialRole</code>.
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		boolean suitable = false;
		VariableType variableType = node.getVariable().getVariableType();
		if (variableType == VariableType.FINITE_STATES || variableType == VariableType.DISCRETIZED) {
			for (Variable variable : variables.subList(1, variables.size())) {
				if (variable.getVariableType() == VariableType.NUMERIC) {
					suitable = true;
					break;
				}

			}
		}
		return suitable;
	}

	/*******
	 * Assigns a value at the table for the combination of a set of variables
	 * and the corresponding state indices.
	 *
	 * @param variables
	 *            . <code>ArrayList</code> of <code>Variable</code>
	 * @param statesIndexes
	 *            . <code>int[]</code>
	 * @param function value to be assigned
	 */
	public void setValue(List<Variable> variables, int[] statesIndexes, String function) {
		int position = 0;
		for (int i = 0; i < variables.size(); i++) {
			Variable variable = variables.get(i);
			int indexVariable = this.variables.indexOf(variable);
			if (indexVariable != -1) {
				position += offsets[indexVariable] * statesIndexes[i];
			}
		}
		getFunctionValues()[position] = function;
	}

	/**
	 * Returns a String with the function stored in the position given by configuration
	 * @param configuration Configuration which determines the position in the table. This configuration is a valid configuration of TableWithEvents
	 * @return a String with the function stored in the position given by configuration
	 */
	public String getFunctionValue(EvidenceCase configuration){
		EvidenceCase stateConfiguration = new EvidenceCase(configuration);
		int position =0;
		for (Variable stateVariable:stateConfiguration.getVariables()){
			int indexVariable = this.variables.indexOf(stateVariable);
			if (indexVariable != -1) {
				position += offsets[indexVariable] * configuration.getFinding(stateVariable).getStateIndex();
			}
		}
		return functionValues[position];
	}

	/**
	 * Returns the value of the functions with the function stored in the position given by configuration and with the
	 * values of numericConfiguration
	 * @param configuration Configuration that determine the position in the Table by its VariableType.FINITE_STATES variables
	 * , and the values of the variables of the Function by its VariableType.NUMERIC variables.
	 *  This configuration is a valid configuration of TableWithEvents
	 * @param numericConfiguration variables of the selected function
	 * @return the value of the functions with the function stored in the position given by configuration and with the
	 * values of numericConfiguration
	 */
	public double getEvaluatedFunctionValue(EvidenceCase configuration, EvidenceCase numericConfiguration){
		String functionValue= getFunctionValue(configuration);
		FunctionPotential functionPotential =new FunctionPotential(numericConfiguration.getVariables(), getPotentialRole(), new VariableExpression(numericConfiguration.getVariables(), functionValue));
        return functionPotential.sampleConditionedVariable(new double[]{0}, numericConfiguration);
    }


//	/**
//	 * Given a set of variables and a set of corresponding states indices, gets
//	 * the corresponding value in the table.
//	 *
//	 * @param stateVariables . <code>ArrayList</code> of <code>Variable</code>
//	 * @param statesIndices  . <code>int[]</code>
//	 * @return <code>double</code>
//	 * @argCondition All the variables in this potentials are included into the
//	 * received variables.
//	 */
//	public String getFunctionValue(List<Variable> stateVariables, int[] statesIndices) {
//		int position = 0;
//		for (int i = 0; i < stateVariables.size(); i++) {
//			Variable variable = stateVariables.get(i);
//			int indexVariable = this.variables.indexOf(variable);
//			if (indexVariable != -1) {
//				position += offsets[indexVariable] * statesIndices[i];
//			}
//		}
//		return getFunctionValues()[position];
//	}

	/**
	 * @return : Table containing the values of the
	 * potential.
	 * @consultation
	 */
	public String[] getFunctionValues() {
		return functionValues;
	}

	public void setFunctionValues(String[] functionValues) {
		this.functionValues = functionValues;
	}

	// Methods

	@Override public boolean equals(Object arg0) {
		boolean isEqual = super.equals(arg0) && arg0 instanceof TablePotential;
		if (isEqual) {
			double[] otherValues = ((TablePotential) arg0).getValues();
			if (getValues().length == otherValues.length) {
				for (int i = 0; i < getValues().length; i++) {
					isEqual &= getValues()[i] == otherValues[i];
				}
			} else {
				isEqual = false;
			}
		}
		return isEqual;
	}

	@Override public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
	                                                      List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}

	@Override public Potential copy() {
		return new TableWithFunctions(this);
	}

	@Override public boolean isUncertain() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public void scalePotential(double scale) {
		//TODO
	}

}
