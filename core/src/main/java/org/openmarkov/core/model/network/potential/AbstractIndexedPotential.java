/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Abstract base class that holds the dimension/offset bookkeeping shared by
 * {@link TablePotential} and {@link GTablePotential}.
 * <p>
 * It does <em>not</em> own a {@code values} array; subclasses decide how to
 * store their data.  The fields {@code dimensions}, {@code offsets},
 * {@code tableSize} and {@code initialPosition} are computed once in the
 * constructor and are available to all descendants.
 *
 * @author Manuel Arias Calleja
 * @since OpenMarkov 0.3
 */
public abstract class AbstractIndexedPotential extends Potential {

    /**
     * Dimensions (number of states) of the variables.
     */
    protected int[] dimensions;

    /**
     * Offsets of the variables in the table that represents this potential.
     */
    protected int[] offsets;

    /**
     * Indicates the first configuration.  In a new potential it is 0.
     * In a projected potential it may be different from 0.
     */
    protected int initialPosition = 0;

    /**
     * Number of configurations in this potential.  Note that this number can
     * be less than the actual array length when the potential is a projection.
     */
    protected int tableSize;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Main constructor.  Computes {@code dimensions}, {@code offsets} and
     * {@code tableSize} from {@code variables}.
     *
     * @param variables List of variables; may be {@code null} or empty for a
     *                  constant potential.
     * @param role      Potential role.
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    protected AbstractIndexedPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        int numVariables = (variables != null) ? variables.size() : 0;
        if (numVariables != 0) {
            dimensions = calculateDimensions(variables);
            offsets    = calculateOffsets(dimensions);
            tableSize  = computeTableSize(variables);
        } else {
            tableSize = 1;
            offsets   = new int[0];
        }
    }

    /**
     * Copy constructor.
     * <p>
     * {@code super(source)} invokes {@code Potential(Potential)} which calls
     * {@code this(variables, role)} within the {@code Potential} class — that
     * path does <em>not</em> pass through
     * {@code AbstractIndexedPotential(variables, role)}, so
     * {@code dimensions}, {@code offsets} and {@code tableSize} would remain
     * uninitialised after the super-chain.  We therefore recompute them
     * explicitly here.
     *
     * @param source Source potential to copy from.
     */
    protected AbstractIndexedPotential(AbstractIndexedPotential source) {
        super(source); // → Potential(Potential) → Potential(variables, role) — sets variables/role/comment/criterion
        // Recompute indexed fields (not set by the Potential copy chain).
        List<Variable> vars = source.getVariables();
        int n = (vars != null) ? vars.size() : 0;
        if (n != 0) {
            dimensions = calculateDimensions(vars);
            offsets    = calculateOffsets(dimensions);
            tableSize  = computeTableSize(vars);
        } else {
            tableSize = 1;
            offsets   = new int[0];
        }
        this.initialPosition = source.initialPosition;
    }

    // -------------------------------------------------------------------------
    // Static utility methods (accessible as TablePotential.xxx() via inheritance)
    // -------------------------------------------------------------------------

    /**
     * Returns the dimension array for a list of finite-states variables.
     * This method is {@code static} because it can be used without creating
     * a potential (e.g. to estimate memory requirements).
     *
     * @param fsVariables List of finite-states variables.
     * @return Array where {@code result[i]} is the number of states of
     * {@code fsVariables.get(i)}.
     */
    public static int[] calculateDimensions(List<Variable> fsVariables) {
        int numVariables = fsVariables == null ? 0 : fsVariables.size();
        int[] dimensions = new int[numVariables];
        for (int i = 0; i < numVariables; i++) {
            dimensions[i] = fsVariables.get(i).getNumStates();
        }
        return dimensions;
    }

    /**
     * Returns the offset array from a dimension array.
     * This method is {@code static} because it can be used without creating
     * a potential.
     *
     * @param dimensions Array of dimensions.
     * @return Array where {@code result[i]} is the stride for variable {@code i}.
     */
    public static int[] calculateOffsets(int[] dimensions) {
        int[] offsets = new int[dimensions.length];
        if (dimensions.length == 0) {
            return offsets;
        }
        offsets[0] = 1;
        for (int i = 1; i < dimensions.length; i++) {
            offsets[i] = dimensions[i - 1] * offsets[i - 1];
        }
        return offsets;
    }

    /**
     * Calculates the product of the number of states of all variables.
     * In projected potentials {@code tableSize} can be less than the backing
     * array length.
     *
     * @param variables List of variables.
     * @return Product of the number of states of all variables.
     */
    public static int computeTableSize(List<Variable> variables) {
        int tableSize = 1;
        for (Variable variable : variables) {
            tableSize *= variable.getNumStates();
        }
        return tableSize;
    }

    /**
     * Returns the accumulated offsets of {@code variables} relative to
     * {@code otherVariables}.
     * <p>
     * The accumulated offset for position {@code i} represents the increment
     * (positive or negative) in the position of the {@code otherVariables}
     * table when variable {@code i} of {@code variables} is incremented by one.
     *
     * @param variables      Variables whose order drives the traversal.
     * @param otherVariables Variables of the other potential.
     * @return Array of accumulated offsets.
     */
    public static int[] getAccumulatedOffsets(List<Variable> variables, List<Variable> otherVariables) {
        int otherSize = otherVariables.size();
        int thisSize  = variables.size();
        int[] accOffsetXY = new int[thisSize];
        if (otherSize == 0) {
            return accOffsetXY; // Initialized to 0
        }
        int[] ordering = new int[thisSize];
        for (int i = 0; i < ordering.length; i++) {
            ordering[i] = otherVariables.indexOf(variables.get(i));
        }
        // offsets of otherVariables
        int[] offsetX = new int[otherSize];
        offsetX[0] = 1;
        for (int i = 1; i < offsetX.length; i++) {
            offsetX[i] = offsetX[i - 1] * otherVariables.get(i - 1).getNumStates();
        }
        int[] offsetXY = new int[thisSize];
        int ordering_0 = ordering[0];
        offsetXY[0]    = (ordering_0 == -1) ? 0 : offsetX[ordering_0];
        accOffsetXY[0] = offsetXY[0];
        for (int j = 1; j < accOffsetXY.length; j++) {
            int ordering_j = ordering[j];
            offsetXY[j]    = (ordering_j == -1) ? 0 : offsetX[ordering_j];
            int numStatesYj_1 = variables.get(j - 1).getNumStates();
            accOffsetXY[j] = accOffsetXY[j - 1] + offsetXY[j] - (numStatesYj_1 * offsetXY[j - 1]);
        }
        return accOffsetXY;
    }

    /**
     * Uses accumulated offsets to calculate the next position in a potential.
     * The content of {@code actualPosition} will be modified.
     *
     * @param actualPosition   Current position.
     * @param actualCoordinate Array of current coordinates (modified in place).
     * @param dimensions       Array of dimensions.
     * @param accOffsets       Array of accumulated offsets.
     * @return Next position, or {@code -1} if the end of the potential is
     * reached.
     */
    public static int getNextPosition(int actualPosition, int[] actualCoordinate,
                                      int[] dimensions, int[] accOffsets) {
        for (int j = 0; j < actualCoordinate.length; j++) {
            actualCoordinate[j]++;
            if (actualCoordinate[j] < dimensions[j]) {
                return actualPosition + accOffsets[j];
            }
            actualCoordinate[j] = 0;
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // Instance methods
    // -------------------------------------------------------------------------

    /**
     * Returns the linear position in the table for a given coordinate array.
     *
     * @param coordinates Array of state indices, one per variable.
     * @return Linear position.
     */
    public int getPosition(int[] coordinates) {
        int position     = 0;
        int numVariables = (variables != null) ? variables.size() : 0;
        for (int i = 0; i < numVariables; i++) {
            position += offsets[i] * coordinates[i];
        }
        return position;
    }

    /**
     * Returns the linear position for a configuration of variables that are
     * not necessarily in the same order as the variables in the potential.
     *
     * @param configuration Evidence case.
     * @return Linear position.
     */
    public int getPosition(EvidenceCase configuration) {
        int[] coordinates;
        int   sizeCoordinates;
        int   sizeEvi     = configuration.getFindings().size();
        List<Variable> varsTable = this.getVariables();
        int   startLoop   = 0;

        if (varsTable.size() != sizeEvi) {
            sizeCoordinates = sizeEvi + 1;
            startLoop       = 1;
            coordinates     = new int[sizeCoordinates];
            coordinates[0]  = 0;
        } else {
            sizeCoordinates = sizeEvi;
            coordinates     = new int[sizeCoordinates];
        }

        for (int i = startLoop; i < sizeCoordinates; i++) {
            coordinates[i] = configuration.getFinding(varsTable.get(i)).getStateIndex();
        }
        return getPosition(coordinates);
    }

    /**
     * Returns the coordinate array for a given linear position.
     *
     * @param position Linear position in the table.
     * @return Array of state indices, one per variable.
     */
    public int[] getConfiguration(int position) {
        int[] coordinate = new int[offsets.length];
        for (int i = offsets.length - 1; i >= 0; i--) {
            coordinate[i]  = position / offsets[i];
            position       -= coordinate[i] * offsets[i];
        }
        return coordinate;
    }

    /**
     * Returns the accumulated offsets of this potential relative to
     * {@code otherVariables}.
     *
     * @param otherVariables Variables of another potential.
     * @return Array of accumulated offsets.
     */
    public int[] getAccumulatedOffsets(List<Variable> otherVariables) {
        return getAccumulatedOffsets(this.variables, otherVariables);
    }

    /**
     * Returns the accumulated offsets of a projected potential.
     *
     * @param otherVariables    Variables in the projected potential.
     * @param originalVariables Complete set of variables in the projected potential.
     * @return Accumulated offsets; size equals {@code otherVariables.size()}.
     */
    public int[] getProjectedAccumulatedOffsets(List<Variable> otherVariables,
                                                List<Variable> originalVariables) {
        if (otherVariables == originalVariables) {
            return getAccumulatedOffsets(otherVariables);
        }
        int[] originalAccOffsets = getAccumulatedOffsets(originalVariables);
        int[] accOffsets         = new int[otherVariables.size()];
        int j = 0;
        for (int i = 0; i < originalVariables.size(); i++) {
            Variable variable = originalVariables.get(i);
            if (otherVariables.contains(variable)) {
                accOffsets[j++] = originalAccOffsets[i];
            }
        }
        return accOffsets;
    }

    /**
     * @return Dimensions of the variables (number of states per variable).
     */
    public int[] getDimensions() {
        return dimensions;
    }

    /**
     * @return Offsets of the variables in the table.
     */
    public int[] getOffsets() {
        return offsets;
    }

    /**
     * @return First position in the backing array (0 for new potentials,
     * possibly non-zero for projections).
     */
    public int getInitialPosition() {
        return initialPosition;
    }

    /**
     * @return Number of configurations in this potential.
     */
    public int getTableSize() {
        return tableSize;
    }
}
