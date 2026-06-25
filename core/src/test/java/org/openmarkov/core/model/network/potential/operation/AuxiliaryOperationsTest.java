/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test of:
 * <ul>
 * <li><code>testGetProperPotentials()
 * </code>
 * <li><code>testGetUnionVariables()</code>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuxiliaryOperationsTest {
    
    private final int NUM_POTENTIALS_ARRAY = 10;
    private final int NUM_CONSTANT_POTENTIALS = NUM_POTENTIALS_ARRAY;
    private final int numNormalPotentials = NUM_POTENTIALS_ARRAY;
    private final int NUM_VARS_PER_POTENTIAL = 4;
    private final int NUM_STATES_VARIABLES = 3;
    private List<TablePotential> emptyPotentialsList;
    private List<TablePotential> oneConstantPotentialsList;
    private List<TablePotential> oneProperPotentialsList;
    private List<TablePotential> severalMixedPotentialsList;
    private List<TablePotential> constantPotentials;
    
    private List<TablePotential> normalPotentials;
    
    @BeforeEach public void setUp() {
        emptyPotentialsList = new ArrayList<>();
        constantPotentials = SharedTestUtilities.generatePotentials(NUM_CONSTANT_POTENTIALS, 0, 0, 0);
        oneConstantPotentialsList = new ArrayList<>();
        oneConstantPotentialsList.add(constantPotentials.get(0));
        normalPotentials = SharedTestUtilities
                .generatePotentials(numNormalPotentials, NUM_VARS_PER_POTENTIAL, 0, NUM_STATES_VARIABLES);
        oneProperPotentialsList = new ArrayList<>();
        oneProperPotentialsList.add(normalPotentials.get(0));
        
        severalMixedPotentialsList = new ArrayList<>();
        
        // Initializes constant potentials tables with 1, 2, ... n
        for (int i = 0; i < NUM_CONSTANT_POTENTIALS; i++) {
            severalMixedPotentialsList.add(constantPotentials.get(i));
            severalMixedPotentialsList.add(normalPotentials.get(i));
        }
        
        // Initializes potentials tables with:
        // number of potential * potential table length + table position + 1
        TablePotential auxTablePotential;
        for (int i = 0; i < numNormalPotentials; i++) {
            auxTablePotential = normalPotentials.get(i);
            for (int j = 0; j < auxTablePotential.getValues().length; j++) {
                auxTablePotential.getValues()[j] = auxTablePotential.getValues().length * i + j + 1;
            }
        }
    }
    
    @Test public void testGetProperPotentials() {
        // Test empty list
        List<TablePotential> properPotentials = AuxiliaryOperations.getNonConstantPotentials(emptyPotentialsList);
        assertNotNull(properPotentials);
        assertEquals(0, properPotentials.size());
        
        // Test list with one constant potential
        properPotentials = AuxiliaryOperations.getNonConstantPotentials(oneConstantPotentialsList);
        assertEquals(0, properPotentials.size());
        
        // Test list with several proper and constant potentials
        properPotentials = AuxiliaryOperations.getNonConstantPotentials(severalMixedPotentialsList);
        assertEquals(NUM_POTENTIALS_ARRAY, properPotentials.size());
    }
    
    @Test public void testGetUnionVariables() {
        // Check union of constant potentials (no variables)
        List<Variable> union = AuxiliaryOperations.getUnionVariables(constantPotentials);
        assertNotNull(union);
        assertEquals(0, union.size());
        
        // Check union of several potentials
        // Generate 2 potentials with 3 variables each with 2 variables in common
        
        normalPotentials = SharedTestUtilities.generatePotentials(2, 3, 2, 2);
        
        union = AuxiliaryOperations.getUnionVariables(normalPotentials);
        // Check number of variables in union
        assertEquals(4, union.size());
        // Check that all the variables of the potentials are in the union
        for (Potential potential : normalPotentials) {
            List<Variable> variables = potential.getVariables();
            for (Variable variable : variables) {
                assertTrue(union.contains(variable));
            }
        }
    }
    
}
