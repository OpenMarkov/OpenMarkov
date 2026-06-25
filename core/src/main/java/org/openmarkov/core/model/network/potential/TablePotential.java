/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.TablePotentialSampler;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * A {@code TablePotential} is a type of relation with a list of
 * probabilistic nodes. All variables will be discrete in this class.
 * <p>
 * Attributes {@code dimensions} and {@code offsets} only make sense
 * when the number of variables is greater than 0. Please be careful to check it
 * when necessary.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@PotentialType(names = {"ProbTable", "Table"})
public class TablePotential extends AbstractIndexedPotential
        implements Comparable<TablePotential>, Projectable, Reorderable, Scalable, CEUtilityPotential, DESSimulablePotential {
    // Attributes
    /**
     * Table storing the numerical values of the potential.
     * Use {@link #getValues()} for read access and {@link #setValues(double[])} for bulk assignment.
     */
    private double[] values;

    // Constructors
    
    /**
     * @param variables . {@code List} of {@code Variable} used to build the
     *                  {@code TablePotential}.
     * @param role      . {@code PotentialRole}
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    public TablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role); // AbstractIndexedPotential: computes dimensions, offsets, tableSize
        try {
            values = new double[tableSize];
        } catch (NegativeArraySizeException e) {
            throw new OutOfMemoryError();
        }
        setUniform(); // Initializes the table as an uniform potential
    }
    
    /**
     * @param variables . {@code ArrayList} of {@code Variable}
     * @param role      . {@code PotentialRole}
     * @param table     . {@code double[]}
     *                  Condition: All variables must be discrete.
     */
    public TablePotential(List<Variable> variables, PotentialRole role, double[] table) {
        this(variables, role);
        this.values = table;
    }
    
    /**
     * @param role      . {@code PotentialRole}
     * @param variables . {@code ArrayList} of {@code Variable}
     *                  Condition: All variables must be discrete.
     */
    public TablePotential(PotentialRole role, Variable... variables) {
        
        this(toList(variables), role);
    }
    
    public TablePotential(TablePotential potential) {
        super(potential); // AbstractIndexedPotential: copies initialPosition, recomputes dimensions/offsets/tableSize
        values = potential.values.clone();
        // uncertainValues lives in UncertainTablePotential; handled by its own copy constructor
    }
    
    // Methods

    /**
     * Returns the internal values array directly (live reference, not a copy).
     * Callers must not assign a new array to the returned reference; use
     * {@link #setValues(double[])} for bulk replacement.
     */
    public double[] getValues() {
        return values;
    }

    /**
     * Replaces the values array.
     *
     * @param newValues new values array
     */
    public void setValues(double[] newValues) {
        this.values = newValues;
    }

    // Static methods calculateDimensions, calculateOffsets, computeTableSize,
    // getAccumulatedOffsets and getNextPosition have moved to AbstractIndexedPotential.
    // They are still accessible as TablePotential.calculateDimensions(...) etc.
    // via Java static-method inheritance.

    // TODO This method is not used and the parameters node and role are not used. Remove the method or the parameters.

    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      . {@code Node}
     * @param variables . {@code List} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     *
     * @return True   if an instance of a certain Potential type makes sense given the variables and the potential role.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getNodeType() == NodeType.EVENT) {
            return false;
        }
        return variables.stream()
                        .map(Variable::getVariableType)
                        .noneMatch(type -> type != VariableType.FINITE_STATES && type != VariableType.DISCRETIZED);
    }

    // TODO This method is not used. undertainValues is not used. Remove the method or the parameter.
    
    /**
     * @param uncertainValues List of uncertain values
     *
     * @return true if the uncertain values are correct
     */
    public static boolean checkUncertainTable(List<UncertainValue> uncertainValues) {
        return true;
    }
    
    /**
     * Remove a variable of the potential
     *
     * @param variable Variable to be removed
     *
     * @return Potential without the removed variable
     */
    @Override public Potential removeVariable(Variable variable) {
        Potential newPotential = this;
        if (variables.contains(variable)) {
            Finding finding = new Finding(variable, 0);
            EvidenceCase evidenceCase = new EvidenceCase();
            try {
                evidenceCase.addFinding(finding);
                newPotential = tableProject(evidenceCase, null);
            } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther |
                     NonProjectablePotentialException e) {
                throw new UnreachableException(e);
            }
        }
        return newPotential;
    }
    
    /**
     * @param evidenceCase {@code EvidenceCase}
     *
     * @return A {@code List} of {@code TablePotential}s containing
     * only one element, which is a {@code ProjectedPotential}
     * <p>
     * Note: uncertain values are handled by {@link UncertainTablePotential#tableProject}.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException {
        List<Variable> unobservedVariables = new ArrayList<>(variables);
        if (evidenceCase != null) {
            unobservedVariables.removeAll(evidenceCase.getVariables());
        }
        int numUnobservedVariables = unobservedVariables.size();
        int numVariables = (variables != null) ? variables.size() : 0;
        if (numVariables == numUnobservedVariables) { // No projection.
            return this;
        }
        TablePotential projectedPotential = new TablePotential(unobservedVariables, role);
        int length = projectedPotential.values.length;
        int firstPosition = 0;
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            if ((evidenceCase != null) && evidenceCase.contains(variable)) {
                int state = evidenceCase.getState(variable);
                firstPosition += state * offsets[i];
            }
        }
        if (numUnobservedVariables == 0) {
            projectedPotential.values[0] = values[firstPosition];
        } else {
            int[] accumulatedOffsets = projectedPotential.getAccumulatedOffsets(variables);
            int numVariablesProjected = projectedPotential.getNumVariables();
            int[] projectedCoordinate = new int[numVariablesProjected];
            int[] projectedDimensions = new int[numVariablesProjected];
            for (int i = 0; i < numVariablesProjected; i++) {
                projectedDimensions[i] = unobservedVariables.get(i).getNumStates();
            }
            for (int projectedPosition = 0; projectedPosition < length - 1; projectedPosition++) {
                projectedPotential.values[projectedPosition] = values[firstPosition];
                int increasedVariable = 0;
                for (int j = 0; j < projectedCoordinate.length; j++) {
                    projectedCoordinate[j]++;
                    if (projectedCoordinate[j] < projectedDimensions[j]) {
                        increasedVariable = j;
                        break;
                    }
                    projectedCoordinate[j] = 0;
                }
                firstPosition += accumulatedOffsets[increasedVariable];
            }
            projectedPotential.values[length - 1] = values[firstPosition];
        }
        return projectedPotential;
    }
    
    @Override public TablePotential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
        return tableProject(evidenceCase, null);
    }
    
    // Instance methods getAccumulatedOffsets, getProjectedAccumulatedOffsets,
    // getPosition, getConfiguration, getDimensions, getOffsets, getInitialPosition,
    // getTableSize have moved to AbstractIndexedPotential.

        /**
     * Given a set of variables and a set of corresponding states indices, gets
     * the corresponding value in the table.
     *
     * @param variables     . {@code ArrayList} of {@code Variable}
     * @param statesIndices . {@code int[]}
     *
     * @return {@code double}
     * Condition: All the variables in this potentials are included into the
     * received variables.
     */
    public double getValue(List<Variable> variables, int[] statesIndices) {
        int position = 0;
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            int indexVariable = this.variables.indexOf(variable);
            if (indexVariable != -1) {
                position += offsets[indexVariable] * statesIndices[i];
            }
        }
        return values[position];
    }
    
    /**
     * Given a set an EvidenceCase, gets the corresponding value in the table.
     *
     * @param configuration . {@code EvidenceCase}
     *
     * @return {@code double}
     * Condition: All the variables in this potentials are included into the
     * variables field of the evidence case (configuration).
     */
    public double getValue(EvidenceCase configuration) {
        List<Variable> variables = configuration.getVariables();
        int size = variables.size();
        int[] states = new int[size];
        List<Finding> findings = configuration.getFindings();
        for (int i = 0; i < size; i++) {
            states[i] = findings.get(i).getStateIndex();
        }
        return getValue(variables, states);
    }
    
    /*******
     * Assigns a value at the table for the combination of a set of variables
     * and the corresponding state indices.
     *
     * @param variables the variables
     *            . {@code ArrayList} of {@code Variable}
     * @param statesIndexes the states indexes
     *            . {@code int[]}
     * @param value Value
     */
    public void setValue(List<Variable> variables, int[] statesIndexes, double value) {
        int position = 0;
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            int indexVariable = this.variables.indexOf(variable);
            if (indexVariable != -1) {
                position += offsets[indexVariable] * statesIndexes[i];
            }
        }
        values[position] = value;
    }
    
    /**
     * Returns the uncertain values for sensitivity analysis.
     * Always {@code null} for plain {@code TablePotential};
     * overridden in {@link UncertainTablePotential}.
     */
    public UncertainValue[] getUncertainValues() {
        return null;
    }

    /**
     * Sets uncertain values.
     * {@code null} is accepted as a no-op (removing uncertainty from a non-uncertain potential is harmless).
     * Passing a non-null array on a plain {@code TablePotential} is an error;
     * use {@link UncertainTablePotential} instead.
     *
     * @param uv uncertain values array, or {@code null}
     * @throws UnsupportedOperationException if {@code uv} is non-null
     */
    public void setUncertainValues(UncertainValue[] uv) {
        if (uv != null) {
            throw new UnsupportedOperationException(
                    "Cannot set uncertain values on a plain TablePotential. Use UncertainTablePotential instead.");
        }
        // null is a no-op: removing uncertainty from a non-uncertain potential is harmless
    }

    /**
     * Consistently sets uncertain values and the corresponding numeric values for one column.
     * Dispatches via virtual methods so that {@link UncertainTablePotential} handles the storage.
     */
    public void setUncertainValuesConsistently(List<UncertainValue> uncertainValues, List<Double> newValuesColumn, int basePosition) {
        // Allocate the uncertain-values table only if it does not exist yet; allocating on every
        // call would discard the uncertain values already set on other columns. The array must run
        // parallel to the values array (same length and indexing), not be a single column: both
        // placeUncertainColumn and placeValuesColumn write at i + basePosition, and basePosition is
        // a position in the values array (> 0 for any column other than the first). getValues().length
        // is used rather than tableSize because in projected potentials tableSize can be smaller than
        // the backing array, which would otherwise re-introduce the out-of-bounds write.
        if (getUncertainValues() == null) {
            setUncertainValues(new UncertainValue[getValues().length]);  // virtual dispatch
        }
        placeUncertainColumn(uncertainValues, getVariable(0), basePosition);  // virtual dispatch
        placeValuesColumn(newValuesColumn, basePosition);
    }

    /**
     * Places uncertain values in the uncertain-values table at the given base position.
     * Calls {@link #getUncertainValues()} via virtual dispatch, so subclasses that override
     * that method will use their own storage.
     */
    public void placeUncertainColumn(List<UncertainValue> column, Variable variable, int basePosition) {
        UncertainValue[] table = getUncertainValues();   // virtual: null for plain TablePotential
        // A column occupies contiguous cells starting at basePosition (stride 1: the conditioned
        // variable is variable 0 with offset 1, and a utility value occupies a single cell). The
        // column height is the size of the column itself — number of states of the conditioned
        // variable for a chance potential, or 1 for a utility value — not getVariable(0).getNumStates(),
        // which would mis-size the loop for utility potentials. When clearing (column == null) the
        // height is taken from the variable argument.
        int columnHeight = (column != null) ? column.size() : variable.getNumStates();
        checkColumnFits(columnHeight, basePosition, table.length);
        for (int i = 0; i < columnHeight; i++) {
            table[i + basePosition] = (column != null) ? column.get(i) : null;
        }
    }

    /**
     * Places the given numeric values into a single column of the values table at the given base
     * position, leaving the rest of the table untouched. Symmetric with {@link #placeUncertainColumn}.
     * The column height is the size of {@code column} (stride 1: contiguous cells from basePosition).
     */
    public void placeValuesColumn(List<Double> column, int basePosition) {
        double[] table = getValues();
        checkColumnFits(column.size(), basePosition, table.length);
        for (int i = 0; i < column.size(); i++) {
            table[i + basePosition] = column.get(i);
        }
    }

    /**
     * Validates that a column of {@code columnHeight} consecutive cells starting at {@code basePosition}
     * fits within a table of {@code tableLength} cells. Guards against a column whose size is
     * inconsistent with the potential structure, which would otherwise corrupt a neighbouring column
     * or throw an opaque {@link ArrayIndexOutOfBoundsException}.
     */
    private static void checkColumnFits(int columnHeight, int basePosition, int tableLength) {
        if (basePosition < 0 || columnHeight < 0 || basePosition + columnHeight > tableLength) {
            throw new IllegalArgumentException(
                    "Column of " + columnHeight + " cell(s) at base position " + basePosition
                    + " does not fit in a table of length " + tableLength
                    + "; the column size is inconsistent with the potential structure.");
        }
    }
    
    /**
     * Compares two {@code TablePotential}s using {@code tableSize} as
     * a criterion.
     *
     * @param other {@code Object}.
     *
     * @return {@code int}:
     * <p>
     * &#60;0 if {@code this} table size is minor than the received
     * potential
     * =
     * 0 if tables size is equal
     * &#62;
     * 0 if {@code this} table size is greater than the table size
     * of received potential.
     */
    @Override public int compareTo(TablePotential other) {
        return this.tableSize - other.tableSize;
    }
    
    /**
     * @param configuration Evidence case
     *
     * @return true if and only if the potential contains uncertainty values for
     * a certain configuration
     */
    public boolean hasUncertainty(EvidenceCase configuration) {
        UncertainValue[] uv = getUncertainValues();   // virtual: null for plain TablePotential
        if (uv == null) {
            return false;
        }
        int positionConfiguration = getPosition(configuration);
        return uv[positionConfiguration] != null;
    }
    
    // TODO revisar para que no use tableProject(...)
    @Override public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
        ArrayList<Finding> inducedFindings = new ArrayList<>();
        if (role != PotentialRole.CONDITIONAL_PROBABILITY && role != PotentialRole.POLICY) {
            return inducedFindings;
        }
        // Iterates over the list of parents. If some parent is not in the
        // evidence case, it is not possible to induce a new Finding
        for (int i = 1; i < variables.size(); i++) {
            if (!evidenceCase.contains(variables.get(i))) {
                // returnS the empty list
                return inducedFindings;
            }
        }
        // Checks if the projected potentials are deterministic
        TablePotential projectedPotential = null;
        try {
            projectedPotential = tableProject(evidenceCase, null);
        } catch (NonProjectablePotentialException e) {
            throw new UnreachableException(e);
        }
        if (projectedPotential.getNumVariables() == 1) {
            double[] table = projectedPotential.values;
            int zeros = 0;
            int position = 0;
            for (int i = 0; i < table.length; i++) {
                if (table[i] == 0.0) {
                    zeros++;
                } else {
                    position = i;
                }
            }
            if (zeros == (table.length - 1)) {// new finding
                inducedFindings.add(new Finding(projectedPotential.getVariable(0), position));
            }
            
        }
        return inducedFindings;
    }
    
    /**
     * Initialize the table as a uniform potential.
     */
    public void setUniform() {
        int numVariables;
        boolean setValue = false;
        double value = 0.0;
        if (variables != null) {
            numVariables = variables.size();
            if (numVariables > 0 && noNumericVariables() && (
                    role == PotentialRole.CONDITIONAL_PROBABILITY || role == PotentialRole.POLICY ||
                            role == PotentialRole.JOINT_PROBABILITY || role == PotentialRole.LINK_RESTRICTION)) {
                setValue = true;
                value = 0.0;
                switch (role) {
                /*
                20/10/2014 - Solving issue 211
                https://bitbucket.org/cisiad/org.openmarkov.issues/issue/211/policy-tables-imposed-should-be-a
                Moved the case POLICY statement to share the code with CONDITIONAL_PROBABILITY.
                Before, it was acting as JOINT_PROBABILITY
                 */
                    case CONDITIONAL_PROBABILITY:
                    case POLICY:
                        value = 1.0 / variables.getFirst().getNumStates();
                        break;
                    case JOINT_PROBABILITY:
                        value = 1.0;
                        for (Variable variable : variables) {
                            value *= variable.getNumStates();
                        }
                        value = 1 / value;
                        break;
                    case LINK_RESTRICTION:
                        value = 1.0;
                        break;
                    default:
                        // Do nothing
                        break;
                } // When role = UTILITY -> value = 0.0 (default)
                Arrays.fill(values, value);
            } else if (numVariables == 0) {
                setValue = true;
                if (role == PotentialRole.JOINT_PROBABILITY) {
                    value = 1.0;
                } else {
                    value = 0.0;
                }
            }
            if (setValue) Arrays.fill(values, value);
        }
    }
    
    /**
     * Overrides {@code toString} method. Mainly for test purposes
     */
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));
        // writes variables names
        String out = super.toString();
        // Print configurations
        int valuesPosition = 0;
        boolean openBrace = false;
        if (out.length() < STRING_MAX_LENGTH) {
            if (!variables.isEmpty()) {
                out += " = {";
                openBrace = true;
            } else {
                out += " ";
            }
        }
        while ((out.length() < STRING_MAX_LENGTH) && (valuesPosition < values.length)) {
            out += formatter.format(values[valuesPosition++]);
            if ((valuesPosition < values.length) && (out.length() < (STRING_MAX_LENGTH - 2))) {
                out += ", ";
            }
        }
        if (values.length != 1) {
            if (valuesPosition != values.length || variables.isEmpty()) {
                out += "...";
            }
        }
        if (openBrace) {
            out += "}";
        }
        return out.toString();
    }
    
    @Override public String treeADDString() {
        if (role == PotentialRole.CONDITIONAL_PROBABILITY && variables != null && variables.size() == 1) {
            Variable firstVariable = variables.getFirst();
            for (int i = 0; i < firstVariable.getNumStates(); i++) {
                if (values[i] == 1) {
                    return firstVariable.getName() + " = " + firstVariable.getStateName(i);
                }
            }
        }
        return toString();
    }
    
    /**
     * Generates a sampled potential.
     * For plain {@code TablePotential} (no uncertain values) returns {@code this}.
     * {@link UncertainTablePotential} overrides this via {@link #getUncertainValues()}.
     */
    public Potential sample(boolean isInsideOfExactDistrPotential) {
        if (getUncertainValues() != null) {
            return new TablePotentialSampler().sample(this, isInsideOfExactDistrPotential);
        }
        return this;
    }
    
    @Override public Potential sample() {
        return sample(false);
    }
    
    @Override public boolean equals(Object arg0) {
        boolean isEqual = super.equals(arg0) && arg0 instanceof TablePotential;
        if (isEqual) {
            double[] otherValues = ((TablePotential) arg0).getValues();
            if (values.length == otherValues.length) {
                for (int i = 0; i < values.length; i++) {
                    isEqual &= values[i] == otherValues[i];
                }
            } else {
                isEqual = false;
            }
        }
        return isEqual;
    }
    
    @Override public Potential copy() {
        return new TablePotential(this);
    }
    
    @Override public int sampleConditionedVariable(Random randomGenerator, Map<Variable, Integer> sampledParents) {
        int index = 0;
        int sampleIndex = 0;
        // find index of first position for the given configuration
        for (int i = 1; i < variables.size(); ++i) {
            index += sampledParents.get(variables.get(i)) * offsets[i];
        }
        double random = randomGenerator.nextDouble();
        double accumulatedProbability = values[index + sampleIndex];
        while (random > accumulatedProbability
                // Make sure we don't go out of bounds even if the sum of probabilities
                // is smaller than one.
                && sampleIndex < variables.getFirst().getNumStates() - 1) {
            ++sampleIndex;
            accumulatedProbability += values[index + sampleIndex];
        }
        return sampleIndex;
    }
    
    @Override public double getProbability(HashMap<Variable, Integer> sampledStateIndexes) {
        int index = 0;
        // find index of first position for the given configuration
        for (int i = 0; i < variables.size(); ++i) {
            index += sampledStateIndexes.get(variables.get(i)) * offsets[i];
        }
        return values[index];
    }
    
    public double getUtility(HashMap<Variable, Integer> sampledStateIndexes, HashMap<Variable, Double> utilities) {
        return getProbability(sampledStateIndexes);
    }
    
    @Override public TablePotential addVariable(Variable newVariable) {
        // creates the new potential
        List<Variable> newVariables = new ArrayList<>(variables);
        newVariables.add(newVariable);
        TablePotential newPotential = new TablePotential(newVariables, role);
        // assigns the values of the new potential
        int newVariableNumStates = newVariable.getNumStates();
        for (int i = 0; i < newVariableNumStates; i++) {
            for (int j = 0; j < values.length; j++) {
                newPotential.values[j + i * values.length] = values[j];
                // newPotential.uncertainValues[j + i * values.length] =
                // uncertainValues[j];
            }
        }
        return newPotential;
    }
    
    @Override public boolean isUncertain() {
        return getUncertainValues() != null;   // false for plain TablePotential; UncertainTablePotential overrides getUncertainValues()
    }
    
    @Override public void scalePotential(double scale) {
        for (int j = 0; j < this.values.length; j++) {
            this.values[j] = this.values[j] * scale;
        }
    }

    /** Implements {@link Scalable#scale(double)}; delegates to {@link #scalePotential(double)}. */
    @Override public void scale(double factor) {
        scalePotential(factor);
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        TablePotential potential = (TablePotential) super.deepCopy(copyNet);

        if (this.dimensions != null) {
            potential.dimensions = this.dimensions.clone();
        }

        potential.initialPosition = this.initialPosition;
        potential.offsets = this.offsets.clone();
        potential.tableSize = this.tableSize;

        // uncertainValues lives in UncertainTablePotential; handled by its own deepCopy override

        potential.setValues(this.values.clone());

        return potential;
    }
    
    /**
     * @return The maximum time slice of the variables referenced by this potential
     */
    public int getTimeSlice() {
        int maxTimeSlice = Integer.MIN_VALUE;
        int variableTimeSlice;
        for (Variable variable : variables) {
            maxTimeSlice = ((variableTimeSlice = variable.getTimeSlice()) > maxTimeSlice) ?
                    variableTimeSlice :
                    maxTimeSlice;
        }
        return maxTimeSlice;
        
    }
    
    /**
     * @return The first value of attribute 'values' (that at 0-th position)
     */
    public double getFirstValue() {
        return values[0];
    }
    
    /**
     * @return true iff the table potential has strategy-tree interventions.
     * Always {@code false} for plain {@code TablePotential}; overridden in
     * {@link StrategicTablePotential}.
     */
    public boolean hasInterventions() {
        return false;
    }

    /**
     * @param decision Decision variable
     * @return true iff this potential has an intervention for {@code decision}.
     * Always {@code false} for plain {@code TablePotential}; overridden in
     * {@link StrategicTablePotential}.
     */
    public boolean hasInterventionForDecision(Variable decision) {
        return false;
    }
    
    // 14/08/2022 - sampleConditionedVariable refactored for dealing with nuisance variance
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) {
        
        int index = 0;
        int sampleIndex = 0;
        // find index of first position for the given configuration
        for (int i = 1; i < variables.size(); ++i) {
            index += parents.getState(variables.get(i)) * offsets[i];
        }
        double accumulatedProbability = values[index + sampleIndex];
        //24/10/2023; adapted to indeterminate number of random numbers to sample potentials
        while (randomNumbers[0] > accumulatedProbability
                // Make sure we don't go out of bounds even if the sum of probabilities
                // is smaller than one.
                && sampleIndex < variables.get(0).getNumStates() - 1) {
            ++sampleIndex;
            accumulatedProbability += values[index + sampleIndex];
        }
        return sampleIndex;
    }
    
    @Override
    public TablePotential reorder(List<Variable> newOrderOfVariables) {
        TablePotential newPotential = new TablePotential(newOrderOfVariables, getPotentialRole());
        int[] accOffsets = getAccumulatedOffsets(newOrderOfVariables);
        int[] potentialPositions = new int[getNumVariables()];
        int[] potentialDimensions = getDimensions();
        double[] valuesOrigPotential = values;
        double[] valuesNewPotential = newPotential.values;

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
        if (isAdditive()) {
            newPotential.setCriterion(getCriterion());
        }
        newPotential.properties = properties;
        return newPotential;
    }
    
    @Override
    public TablePotential reorder(Variable variable, State[] newOrder) {
        TablePotential copyPotential = (TablePotential) copy();
        double[] tablePotential = values;
        double[] tableCopyPotential = copyPotential.values;
        int[] displacements = new int[newOrder.length];
        List<Variable> variables = copyPotential.getVariables();
        int variableIndex = variables.indexOf(variable);
        int offset = copyPotential.getOffsets()[variableIndex];
        State[] oldOrder = variable.getStates();
        for (int i = 0; i < newOrder.length; ++i) {
            displacements[i] = -1;
            int j = 0;
            boolean found = false;
            while (!found) {
                if (oldOrder[i] == newOrder[j]) {
                    displacements[i] = j - i;
                    found = true;
                }
                ++j;
            }
        }
        for (int i = 0; i < tablePotential.length; i++) {
            int indexOfState = (i / offset) % variable.getNumStates();
            int newIndex = i + (displacements[indexOfState % variable.getNumStates()] * offset);
            tableCopyPotential[newIndex] = tablePotential[i];
        }
        if (isAdditive()) {
            copyPotential.setCriterion(getCriterion());
        }
        copyPotential.properties = properties;
        return copyPotential;
    }
    
}