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
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.util.Collections;
import java.util.List;

/**
 * A table potential whose cells contain symbolic function expressions ({@link VariableExpression})
 * rather than plain numeric values. Each cell can hold a formula referencing numeric parent
 * variables, enabling conditional probability tables that depend on continuous parameters.
 * Extends {@link UncertainTablePotential} and is used as the internal storage for
 * {@link AugmentedProbTablePotential} and {@link UnivariateDistrPotential}.
 *
 * @author Manuel Arias
 */
public class AugmentedProbTable extends UncertainTablePotential {
    
    /**
     * The default function
     */
    public static final VariableExpression DEFAULT_FUNCTION = new VariableExpression(Collections.emptyList(), "1");
    public static final VariableExpression COMPLEMENT_FUNCTION = new VariableExpression(Collections.emptyList(), "Complement");
    
    private VariableExpression[] functionValues;
    
    /*Note should be discrete variables*/
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    public AugmentedProbTable(List<Variable> stateVariables, PotentialRole role) {
        super(stateVariables, role);
        int numVariables = (stateVariables != null) ? stateVariables.size() : 0;
        if (numVariables != 0) {
            // Number of states of each variable
            dimensions = calculateDimensions(stateVariables);
            // TODO Remove the comment or use it.
/*			int cellsInRow = 1;
			for (int i = 1; i < dimensions.length; i++) {
				cellsInRow *= dimensions[i];
			}*/
            offsets = calculateOffsets(dimensions);
            tableSize = computeTableSize(stateVariables);
            try {
                functionValues = new VariableExpression[tableSize];
                // Get  public int getPosition(int[] coordinates)
                int increment = variables.getFirst().getNumStates();
                for (int i = 0; i < tableSize; i++) {
                    functionValues[i] = COMPLEMENT_FUNCTION;
                }
                for (int i = 0; i < tableSize; i += increment) {
                    functionValues[i] = DEFAULT_FUNCTION;
                }
            } catch (NegativeArraySizeException e) {
                throw new IllegalArgumentException("Negative table size: " + tableSize, e);
            }
        } else {// In this case the potential is a constant
            tableSize = 1;
            setFunctionValues(new VariableExpression[tableSize]);
            offsets = new int[0];
        }
    }
    
    public AugmentedProbTable(List<Variable> stateVariables, PotentialRole role, VariableExpression[] functionValues) {
        this(stateVariables, role);
        this.functionValues = functionValues;
    }
    
    /**
     * Internal constructor used to create a projected potential.
     *
     * @param stateVariables  . {@code ArrayList} of {@code Variable}
     * @param role            . {@code PotentialRole}
     * @param table           . {@code double[]}
     * @param initialPosition First position in {@code table} (used in projected
     *                        potentials).
     * @param offsets         of variables. {@code int[]}
     * @param dimensions      . Number of states of each variable. {@code int[]}
     */
    private AugmentedProbTable(List<Variable> stateVariables, PotentialRole role, VariableExpression[] table, int initialPosition,
                           int[] offsets, int[] dimensions) {
        super(stateVariables, role);
        this.setFunctionValues(table);
        this.initialPosition = initialPosition;
        this.offsets = offsets;
        this.dimensions = dimensions;
        tableSize = computeTableSize(stateVariables);
    }
    
    public AugmentedProbTable(AugmentedProbTable potential) {
        super(potential);  // UncertainTablePotential(UncertainTablePotential)
        this.initialPosition = potential.getInitialPosition();
        this.offsets = potential.getOffsets();
        this.dimensions = potential.getDimensions();
        tableSize = potential.tableSize;
        //UNCLEAR Clones??
        setFunctionValues(potential.getFunctionValues().clone());
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     * Firstly I suppose we have only discrete variables and at least one parent is NUMERIC
     *
     * @param node      . {@code Node}
     * @param variables . {@code List} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     *
     * @return True if an instance of a certain Potential type makes sense given the variables and the potential role.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        VariableType variableType = node.getVariable().getVariableType();
        if (!(variableType == VariableType.FINITE_STATES || variableType == VariableType.DISCRETIZED)) {
            return false;
        }
        return variables.stream()
                        .skip(1)
                        .anyMatch(variable -> variable.getVariableType() == VariableType.NUMERIC);
    }
    
    /*******
     * Assigns a value at the table for the combination of a set of variables
     * and the corresponding state indices.
     *
     * @param variables the variables
     *            . {@code ArrayList} of {@code Variable}
     * @param statesIndexes the states indexes
     *            . {@code int[]}
     * @param function value to be assigned
     */
    public void setValue(List<Variable> variables, int[] statesIndexes, VariableExpression function) {
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
     * Given a set of variables and a set of corresponding states indices, gets
     * the corresponding value in the table.
     *
     * @param stateVariables . {@code ArrayList} of {@code Variable}
     * @param statesIndices  . {@code int[]}
     *
     * @return {@code double}
     * Condition: All the variables in this potentials are included into the
     * received variables.
     */
    public VariableExpression getFunctionValue(List<Variable> stateVariables, int[] statesIndices) {
        int position = 0;
        for (int i = 0; i < stateVariables.size(); i++) {
            Variable variable = stateVariables.get(i);
            int indexVariable = this.variables.indexOf(variable);
            if (indexVariable != -1) {
                position += offsets[indexVariable] * statesIndices[i];
            }
        }
        return getFunctionValues()[position];
    }
    
    /**
     * @return : Table containing the values of the
     * potential.
     */
    public VariableExpression[] getFunctionValues() {
        return functionValues;
    }
    
    public void setFunctionValues(VariableExpression[] functionValues) {
        this.functionValues = functionValues;
    }
    
    // Methods
    
    @Override public boolean equals(Object arg0) {
        boolean isEqual = super.equals(arg0) && arg0 instanceof TablePotential;
        if (isEqual) {
            double[] otherValues = ((TablePotential) arg0).getValues();
            double[] myValues = getValues();
            if (myValues.length == otherValues.length) {
                for (int i = 0; i < myValues.length; i++) {
                    isEqual &= myValues[i] == otherValues[i];
                }
            } else {
                isEqual = false;
            }
        }
        return isEqual;
    }
    
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override public Potential copy() {
        return new AugmentedProbTable(this);
    }
    
    @Override public boolean isUncertain() {
        return false;
    }
    
    @Override public void scalePotential(double scale) {
    }
    
    
    /**
     * Returns a copy of this potential with its variables (and the corresponding function
     * expression cells) rearranged into the given order.
     */
    @Override
    public AugmentedProbTable reorder(List<Variable> newOrderOfVariables) {
        AugmentedProbTable newPotential = new AugmentedProbTable(newOrderOfVariables, getPotentialRole());
        int[] accOffsets = getAccumulatedOffsets(newOrderOfVariables);
        int[] potentialPositions = new int[getNumVariables()];
        int[] potentialDimensions = getDimensions();
        VariableExpression[] valuesOrigPotential = getFunctionValues();
        VariableExpression[] valuesNewPotential = newPotential.getFunctionValues();
        
        int copyTablePosition = 0;
        int numVariables = newOrderOfVariables.size();
        int incrementedVariable, i;
        for (i = 0; i < valuesOrigPotential.length - 1; i++) {
            valuesNewPotential[copyTablePosition] = valuesOrigPotential[i];
            
            for (incrementedVariable = 0; incrementedVariable < numVariables; incrementedVariable++) {
                potentialPositions[incrementedVariable]++;
                if (potentialPositions[incrementedVariable] == potentialDimensions[incrementedVariable]) {
                    potentialPositions[incrementedVariable] = 0;
                } else {
                    break;
                }
            }
            copyTablePosition += accOffsets[incrementedVariable];
        }
        valuesNewPotential[copyTablePosition] = valuesOrigPotential[i];
        newPotential.properties = properties;
        return newPotential;
    }

    @Override
    public AugmentedProbTable reorder(Variable variable, State[] newOrder) {
        AugmentedProbTable copyPotential = new AugmentedProbTable(this);
        int variableIndex = variables.indexOf(variable);
        if (variableIndex < 0) {
            return copyPotential;
        }
        VariableExpression[] orig = functionValues;
        VariableExpression[] copy = copyPotential.getFunctionValues();
        int offset = offsets[variableIndex];
        State[] oldOrder = variable.getStates();
        int[] displacements = new int[newOrder.length];
        for (int i = 0; i < newOrder.length; ++i) {
            for (int j = 0; j < oldOrder.length; j++) {
                if (oldOrder[i] == newOrder[j]) {
                    displacements[i] = j - i;
                    break;
                }
            }
        }
        for (int i = 0; i < orig.length; i++) {
            int indexOfState = (i / offset) % variable.getNumStates();
            int newIndex = i + (displacements[indexOfState] * offset);
            copy[newIndex] = orig[i];
        }
        return copyPotential;
    }


}
