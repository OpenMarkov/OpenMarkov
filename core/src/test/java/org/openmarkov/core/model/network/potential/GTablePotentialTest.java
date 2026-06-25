/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GTablePotential}.
 *
 * <p>Covers constructor behaviour (sizes, invariants) and the known side-effect
 * that {@code values[]} is allocated but always stays all-zero — an artefact of
 * extending {@link TablePotential} while not using the double array.
 * This is relevant for the planned GTablePotential re-parenting (Rediseño 1,
 * phase 5).
 *
 * @author Manuel Arias
 */
class GTablePotentialTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Variable var(String name, int numStates) {
        State[] states = new State[numStates];
        for (int i = 0; i < numStates; i++) states[i] = new State("s" + i);
        return new Variable(name, states);
    }

    // ------------------------------------------------------------------
    // Constructor: single variable
    // ------------------------------------------------------------------

    @Test
    void singleVariableConstructorCreatesCorrectSizeElementTable() {
        Variable v = var("X", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(v));
        // elementTable initial capacity == tableSize == 3
        // It is empty (no elements added yet)
        assertEquals(0, g.elementTable.size());
        // But we can add exactly tableSize elements
        g.elementTable.add("a");
        g.elementTable.add("b");
        g.elementTable.add("c");
        assertEquals(3, g.elementTable.size());
    }

    @Test
    void singleVariableConstructorHasCorrectTableSize() {
        Variable v = var("X", 4);
        GTablePotential<String> g = new GTablePotential<>(List.of(v));
        assertEquals(4, g.getTableSize(), "tableSize should equal number of states");
    }

    // ------------------------------------------------------------------
    // Constructor: two variables
    // ------------------------------------------------------------------

    @Test
    void twoVariableConstructorHasCorrectTableSize() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<Integer> g = new GTablePotential<>(List.of(x, y));
        // tableSize = 2 * 3 = 6
        assertEquals(6, g.getTableSize());
    }

    @Test
    void twoVariableConstructorElementTableHasCorrectCapacity() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<Integer> g = new GTablePotential<>(List.of(x, y));
        // Fill elementTable to capacity (tableSize)
        for (int i = 0; i < 6; i++) g.elementTable.add(i);
        assertEquals(6, g.elementTable.size());
    }

    // ------------------------------------------------------------------
    // Constructor: three variables
    // ------------------------------------------------------------------

    @Test
    void threeVariableConstructorHasCorrectTableSize() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        Variable z = var("Z", 4);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y, z));
        assertEquals(24, g.getTableSize(), "2*3*4 = 24");
    }

    // ------------------------------------------------------------------
    // Constructor: null / empty variables
    // ------------------------------------------------------------------

    @Test
    void nullVariablesConstructorCreatesConstantPotential() {
        GTablePotential<String> g = new GTablePotential<>((List<Variable>) null);
        assertNotNull(g.elementTable);
        assertEquals(0, g.elementTable.size()); // capacity = 1 but no elements added
    }

    @Test
    void emptyVariablesConstructorCreatesConstantPotential() {
        GTablePotential<String> g = new GTablePotential<>(List.of());
        assertNotNull(g.elementTable);
        assertEquals(0, g.elementTable.size());
    }

    // ------------------------------------------------------------------
    // Constructor with role and pre-built elementTable
    // ------------------------------------------------------------------

    @Test
    void roleAndElementTableConstructorSetsElementTable() {
        Variable v = var("X", 2);
        List<String> table = List.of("alpha", "beta");
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED, table);
        assertSame(table, g.elementTable);
    }

    @Test
    void roleAndElementTableConstructorHasCorrectTableSize() {
        Variable v = var("X", 2);
        List<Integer> table = List.of(10, 20);
        GTablePotential<Integer> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED, table);
        assertEquals(2, g.getTableSize());
    }

    // ------------------------------------------------------------------
    // Invariant: elementTable is independent of numeric state
    // ------------------------------------------------------------------

    @Test
    void writingToElementTableDoesNotAffectTableSize() {
        Variable v = var("X", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED);
        g.elementTable.add("a");
        g.elementTable.add("b");
        g.elementTable.add("c");

        assertEquals(3, g.getTableSize(),
                "tableSize must remain equal to the number of variable states");
        assertEquals(3, g.elementTable.size(),
                "elementTable size must equal the number of added elements");
    }

    // ------------------------------------------------------------------
    // getVariables / dimensions / offsets (inherited from AbstractIndexedPotential)
    // ------------------------------------------------------------------

    @Test
    void getVariablesReturnsCorrectVariables() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y));
        assertEquals(List.of(x, y), g.getVariables());
    }

    @Test
    void getDimensionsAreCorrect() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y));
        int[] dims = g.getDimensions();
        assertEquals(2, dims[0]);
        assertEquals(3, dims[1]);
    }

    @Test
    void getOffsetsAreCorrect() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y));
        int[] off = g.getOffsets();
        assertEquals(1, off[0], "offset[0] = 1");
        assertEquals(2, off[1], "offset[1] = numStates(X) * offset[0] = 2");
    }

    // ------------------------------------------------------------------
    // Role — correctly stored after reparenting to AbstractIndexedPotential
    // ------------------------------------------------------------------

    @Test
    void roleParameterIsStoredByTwoArgConstructor() {
        Variable v = var("X", 2);
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED);
        assertEquals(PotentialRole.UNSPECIFIED, g.getPotentialRole(),
                "GTablePotential(vars, role) must store the role via super(variables, role).");
    }

    @Test
    void roleParameterIsStoredByThreeArgConstructor() {
        Variable v = var("X", 2);
        List<String> table = List.of("a", "b");
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED, table);
        assertEquals(PotentialRole.UNSPECIFIED, g.getPotentialRole(),
                "Three-arg constructor must also store the role via super(variables, role).");
    }

    @Test
    void noRoleConstructorHasNullRole() {
        Variable v = var("X", 2);
        GTablePotential<String> g = new GTablePotential<>(List.of(v));
        assertNull(g.getPotentialRole(), "Single-arg constructor passes null role — getPotentialRole() must be null.");
    }
}
