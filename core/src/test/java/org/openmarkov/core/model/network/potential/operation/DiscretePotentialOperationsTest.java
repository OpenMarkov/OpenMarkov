/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.Choice;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.util.UtilTestMethods;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DiscretePotentialOperations}.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DiscretePotentialOperationsTest {
    
    /*  Public scope for use in all tests. */
    public static final double maxError = 0.0001;
    private final int numConstantPotentials = 10;
    private SharedTestUtilities commonVariables;
    private List<TablePotential> constantPotentials;
    
    private List<TablePotential> normalPotentials;
    
    private List<TablePotential> allPotentials;

    /**
     * This method creates the variables used in the tests
     */
    @BeforeEach public void setUp() {
        constantPotentials = new ArrayList<>();
        normalPotentials = new ArrayList<>();
        allPotentials = new ArrayList<>();
        constantPotentials = SharedTestUtilities.generatePotentials(numConstantPotentials, 0, 0, 0);
        int numNormalPotentials = 10;
        int numVariableStates = 3;
        int numVarsNormalPotentials = 4;
        normalPotentials = SharedTestUtilities
                .generatePotentials(numNormalPotentials, numVarsNormalPotentials, 0, numVariableStates);
        
        // Initializes the constant potentials tables with 1, 2, ... n
        for (int i = 0; i < numConstantPotentials; i++) {
            constantPotentials.get(i).getValues()[0] = i + 1;
        }
        
        // Initializes the potentials tables with:
        TablePotential auxTablePotential;
        for (int i = 0; i < numNormalPotentials; i++) {
            auxTablePotential = normalPotentials.get(i);
            for (int j = 0; j < auxTablePotential.getValues().length; j++) {
                auxTablePotential.getValues()[j] = auxTablePotential.getValues().length * i + j + 1;
            }
        }
        commonVariables = new SharedTestUtilities();
    }
    
    @Test public void testSum() {
        List<TablePotential> potentials = new ArrayList<>();
        potentials.add(commonVariables.tpAB);
        potentials.add(commonVariables.tpCAB);
        // Call method under test
        TablePotential sum = DiscretePotentialOperations.sum(potentials);
        // test variables
        List<Variable> sumVariables = sum.getVariables();
        assertEquals(4, sumVariables.size());
        List<Variable> testVariables = Arrays
                .asList(commonVariables.a, commonVariables.b, commonVariables.c, commonVariables.d);
        assertTrue(sumVariables.containsAll(testVariables));
        // test table content
        double[] table = sum.getValues();
        assertEquals(36, table.length);
        int[] configuration = {0, 0, 0, 0};
        assertEquals(0.3, getConfiguration(testVariables, configuration, sum), maxError);
        configuration[0] = 1; // a=1, b=0, c=0, d=0
        assertEquals(0.3, getConfiguration(testVariables, configuration, sum), maxError);
        configuration[0] = 0;
        configuration[1] = 1; // a=0, b=1, c=0, d=0
        assertEquals(0.4, getConfiguration(testVariables, configuration, sum), maxError);
        configuration[1] = 0;
        configuration[2] = 1; // a=0, b=0, c=1, d=0
        assertEquals(0.9, getConfiguration(testVariables, configuration, sum), maxError);
        configuration[2] = 0;
        configuration[3] = 1; // a=0, b=0, c=0, d=1
        assertEquals(0.5, getConfiguration(testVariables, configuration, sum), maxError);
        configuration[0] = 1;
        configuration[1] = 1;
        configuration[2] = 1; // a=1, b=1, c=1, d=1
        assertEquals(0.6, getConfiguration(testVariables, configuration, sum), maxError);
    }
    
    @Test public void testMultiply() {
        
        // Call method under test
        TablePotential multiplication = DiscretePotentialOperations.multiply(commonVariables.potentials);
        List<Variable> variables = multiplication.getVariables();
        assertEquals(4, variables.size());
        assertTrue(variables.contains(commonVariables.c));
        assertTrue(variables.contains(commonVariables.b));
        assertTrue(variables.contains(commonVariables.a));
        assertTrue(variables.contains(commonVariables.d));
        assertEquals(36, multiplication.getValues().length);
        int[] coordinate = {0, 0, 0, 0}; // test configuration a=0,b=0,c=0,d=0
        double value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
        assertEquals(0.007, value, maxError);
        coordinate[0] = 1; // test configuration a=1,b=0,c=0,d=0
        value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
        assertEquals(0.007, value, maxError);
        coordinate[0] = 0;
        coordinate[1] = 1; // test configuration a=0,b=1,c=0,d=0
        value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
        assertEquals(0.014, value, maxError);
        coordinate[1] = 0;
        coordinate[2] = 1; // test configuration a=0,b=0,c=1,d=0
        value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
        assertEquals(0.028, value, maxError);
        coordinate[2] = 0;
        coordinate[3] = 1; // test configuration a=0,b=0,c=0,d=1
        value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
        assertEquals(0.014, value, maxError);
    }
    
    @SuppressWarnings("unchecked") @Test
    public void testNewMultiply() {
        // Test constant multiplication
        List<TablePotential> potentials = allPotentials;
        
        // Only 1 potential so in this case the method does not do anything
        potentials.add(constantPotentials.get(1));
        TablePotential aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        
        assertEquals(1, aPotential.getValues().length);
        assertEquals(2.0, aPotential.getValues()[0], maxError);
        
        // Two constant potentials
        potentials.add(constantPotentials.get(1));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        
        assertEquals(4.0, aPotential.getValues()[0], maxError);
        
        // Several constant potentials
        for (int i = 2; i < numConstantPotentials; i++) {
            potentials.add(constantPotentials.get(i));
        }
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        assertEquals(7257600.0, aPotential.getValues()[0], maxError);
        
        // Test constant and binary potential multiplication
        // Only 1 potential so in this case the method does not do anything
        potentials.clear();
        potentials.add(normalPotentials.getFirst());
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        
        // Check variables:
        // Same number of variables
        TablePotential normalPotential = normalPotentials.getFirst();
        assertEquals(aPotential.getVariables().size(), normalPotential.getVariables().size());
        // Same variables
        assertTrue(aPotential.getVariables().containsAll(normalPotential.getVariables()));
        
        // Check table
        // size:
        assertEquals(81, aPotential.getValues().length);
        // travels around all the table
        assertEquals(1.0, aPotential.getValues()[0], maxError);
        
        // Two normal potentials
        potentials.add(normalPotentials.get(1));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        // check table size
        assertEquals(81 * 81, aPotential.getValues().length);
        // check table content
        assertEquals(82.0, aPotential.getValues()[0], maxError);
        ArrayList<Variable> variablesPotentials = (ArrayList<Variable>) (getUnionVariablesOrdered(potentials));
        int[] coordinate1 = {1, 0, 0, 0, 0, 0, 0, 0};
        double configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate1, aPotential);
        assertEquals(164.0, configuration, maxError);
        coordinate1[2] = 1;
        coordinate1[4] = 1;
        coordinate1[6] = 1;
        configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate1, aPotential);
        assertEquals(1012.0, configuration, maxError);
        
        // Two potentials constant and normal: c * n
        potentials.clear();
        potentials.add(constantPotentials.get(1));
        potentials.add(normalPotentials.get(0));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        assertEquals(81, aPotential.getValues().length);
        assertEquals(2.0, aPotential.getValues()[0], maxError);
        int[] coordinate2 = {1, 0, 0, 0};
        variablesPotentials = (ArrayList<Variable>) ( getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate2, aPotential);
        assertEquals(4.0, configuration, maxError);
        
        // Two potentials constant and normal: n * c
        potentials.clear();
        potentials.add(normalPotentials.get(0));
        potentials.add(constantPotentials.get(1));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        assertEquals(81, aPotential.getValues().length);
        assertEquals(2.0, aPotential.getValues()[0], maxError);
        variablesPotentials = (ArrayList<Variable>) ( getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate2, aPotential);
        assertEquals(4.0, configuration, maxError);
        
        // Three potentials constant and normal: c1 * c2 * n
        potentials.clear();
        potentials.add(constantPotentials.get(1));
        potentials.add(constantPotentials.get(2));
        potentials.add(normalPotentials.get(0));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        assertEquals(81, aPotential.getValues().length);
        assertEquals(6.0, aPotential.getValues()[0], maxError);
        variablesPotentials = (ArrayList<Variable>) (getUnionVariablesOrdered(potentials));
        int[] coordinate3 = {1, 0, 0, 0, 0, 0, 0, 0};
        configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate3, aPotential);
        assertEquals(12.0, configuration, maxError);
        
        // Three potentials constant and normal: c1 * n * c2
        potentials.clear();
        potentials.add(constantPotentials.get(1));
        potentials.add(normalPotentials.get(0));
        potentials.add(constantPotentials.get(2));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        assertEquals(81, aPotential.getValues().length);
        assertEquals(6.0, aPotential.getValues()[0], maxError);
        variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate3, aPotential);
        assertEquals(12.0, configuration, maxError);
        
        // Three potentials constant and normal: n * c1 * c2
        potentials.clear();
        potentials.add(normalPotentials.get(0));
        potentials.add(constantPotentials.get(1));
        potentials.add(constantPotentials.get(2));
        aPotential = (TablePotential) PotentialOperations.multiply(potentials);
        assertEquals(81, aPotential.getValues().length);
        assertEquals(6.0, aPotential.getValues()[0], maxError);
        variablesPotentials = (ArrayList<Variable>) ( getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate3, aPotential);
        assertEquals(12.0, configuration, maxError);
    }
    
    @Test public void testMultiplyAndMarginalize() {
        // Call method under test
        TablePotential mulAndMarg = DiscretePotentialOperations
                .multiplyAndMarginalize(commonVariables.potentials, commonVariables.a);
        List<Variable> variables = mulAndMarg.getVariables();
        assertEquals(3, variables.size());
        assertTrue(variables.contains(commonVariables.b));
        assertTrue(variables.contains(commonVariables.c));
        assertTrue(variables.contains(commonVariables.d));
        assertEquals(12, mulAndMarg.getValues().length);
        int[] coordinate = {0, 0, 0}; // coordinate = {0,0,0}
        double value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
        assertEquals(0.0875, value, maxError);
        coordinate[0] = 1; // coordinate = {1,0,0}
        value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
        assertEquals(0.063, value, maxError);
        coordinate[0] = 0;
        coordinate[1] = 1; // coordinate = {0,1,0}
        value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
        assertEquals(0.2625, value, maxError);
        coordinate[1] = 0;
        coordinate[2] = 1; // coordinate = {0,0,1}
        value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
        assertEquals(0.273, value, maxError);
        coordinate[0] = 2;
        coordinate[1] = 1; // coordinate = {2,1,1}
        value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
        assertEquals(0.1435, value, maxError);
    }
    
    @Test
    public void testMultiplyAndMarginalizeProjected() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        // Create data
        // Variables
        Variable A = new Variable("A", 2);
        Variable B = new Variable("B", 2);
        Variable C = new Variable("C", 2);
        // Arrays of variables
        ArrayList<Variable> bPotentialVariables = new ArrayList<>();
        bPotentialVariables.add(B);
        bPotentialVariables.add(A);
        ArrayList<Variable> bcPotentialVariables = new ArrayList<>();
        bcPotentialVariables.add(C);
        bcPotentialVariables.add(A);
        bcPotentialVariables.add(B);
        // table of potentials
        double[] bTable = {0.7, 0.3, 0.9, 0.1};
        double[] bcTable = {0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02};
        // tablePotentials
        TablePotential bPotential = new TablePotential(bPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                       bTable);
        TablePotential bcPotential = new TablePotential(bcPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                        bcTable);
        // evidence case
        Finding aFinding = new Finding(A, 0);
        EvidenceCase evidenceCase = new EvidenceCase();
        evidenceCase.addFinding(aFinding);
        // project potentials
        bPotential = bPotential.tableProject(evidenceCase, null);
        bcPotential = bcPotential.tableProject(evidenceCase, null);
        // collection of potentials to multiply and marginalize
        ArrayList<TablePotential> potentials = new ArrayList<>();
        potentials.add(bPotential);
        potentials.add(bcPotential);
        // variables to keep and marginalize
        ArrayList<Variable> variablesToKeep = new ArrayList<>();
        variablesToKeep.add(C);
        ArrayList<Variable> variablesToMarginalize = new ArrayList<>();
        variablesToMarginalize.add(B);
        
        // Do test
        TablePotential result = DiscretePotentialOperations
                .multiplyAndMarginalize(potentials, variablesToKeep, variablesToMarginalize);
        // Test table
        assertEquals(0.192, result.getValues()[0], maxError);
        assertEquals(0.808, result.getValues()[1], maxError);
        assertEquals(2, result.getValues().length);
        
        // Test variables
        List<Variable> variables = result.getVariables();
        assertEquals(1, variables.size());
        assertTrue(variables.contains(C));
        
        int[] offsets = result.getOffsets();
        assertEquals(1, offsets.length);
        assertEquals(1, offsets[0]);
    }
    
    @Test
    /** Multiplies and maximizes two potentials: <code>tpAB(a,b)</code> and
     * <code>tpCAB(c,a,d)</code> that share a variable: <code>a</code>. */
    public void testMultiplyAndMaximize() {
        ArrayList<Potential> potentialsVariable = new ArrayList<>();
        potentialsVariable.add(commonVariables.tpAB);
        potentialsVariable.add(commonVariables.tpCAB);
        Variable variableToMaximize = commonVariables.a; // The common variable
        Object[] potentials = DiscretePotentialOperations.multiplyAndMaximize(potentialsVariable, variableToMaximize);
        // Test begins
        assertEquals(2, potentials.length); // Produces two potentials
        TablePotential maximized = (TablePotential) potentials[0];
        // Test variables of first potential
        List<Variable> maximizedVariables = maximized.getVariables();
        assertEquals(3, maximizedVariables.size());
        assertTrue(maximizedVariables.contains(commonVariables.b));
        assertTrue(maximizedVariables.contains(commonVariables.c));
        assertTrue(maximizedVariables.contains(commonVariables.d));
        assertFalse(maximizedVariables.contains(commonVariables.a));
        // Test maximization
        assertEquals(12, maximized.getValues().length);
        assertEquals(0.21, maximized.getValues()[0], maxError);
        HashMap<Variable, Integer> coordinate = new HashMap<>();
        coordinate.put(commonVariables.b, 0);
        coordinate.put(commonVariables.c, 0);
        coordinate.put(commonVariables.d, 0);
        assertEquals(0.21, maximized.getProbability(coordinate), 0.0001);
        coordinate.put(commonVariables.b, 1);
        assertEquals(0.09, maximized.getProbability(coordinate), 0.0001);
        
        //		assertNotNull(maximized.interventions);
        //		assertEquals(maximized.interventions.length, maximized.getValues().length);
    }
    
    @Test public void testGetProperPotentials() {
        // Call method under test
        List<TablePotential> properPotentials = AuxiliaryOperations
                .getNonConstantPotentials(commonVariables.potentials);
        assertEquals(2, properPotentials.size());
        assertTrue(properPotentials.contains(commonVariables.tpAB));
        assertTrue(properPotentials.contains(commonVariables.tpCAB));
    }
    
    @Test public void testGetConstantFactor() {
        // Call method under test
        double constantFactor = DiscretePotentialOperations.getConstantFactor(commonVariables.potentials);
        assertEquals(0.35, constantFactor, maxError);
    }
    
    @Test public void testGetAccumulatedOffsets() {
    }
    
    @Test public void testNormalize() throws CannotNormalizePotentialException {
        TablePotential multiplication = DiscretePotentialOperations.multiply(commonVariables.potentials);
        //			TablePotential normalized;
        //				normalized = (TablePotential)
        DiscretePotentialOperations.normalize(multiplication);
        //				int[] configuration = {0, 0, 0, 0};
        //				int[] dimensions = normalized.getDimensions();
        //				List<Variable> variablesNormalized = normalized.getVariables();
        
    }
    
    @Test public void testDivide() {
        // Call method under test
        TablePotential division = DiscretePotentialOperations
                .divide(commonVariables.tpConstant07, commonVariables.tpAB);
        assertEquals(2, division.getVariables().size());
        assertTrue(division.contains(commonVariables.a));
        assertTrue(division.contains(commonVariables.b));
        assertEquals(7, division.getValues()[0], maxError);
        
        division = DiscretePotentialOperations.divide(commonVariables.tpAB, commonVariables.tpCAB);
        assertEquals(4, division.getVariables().size());
        assertTrue(division.contains(commonVariables.a));
        assertTrue(division.contains(commonVariables.b));
        assertTrue(division.contains(commonVariables.d));
        assertTrue(division.contains(commonVariables.d));
        
        int[] coordinate = {0, 0, 0, 0};
        double value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
        assertEquals(0.5, value, maxError);
        coordinate[2] = 1; // {0,0,1,0}
        value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
        assertEquals(0.125, value, maxError);
        coordinate[2] = 0;
        coordinate[0] = 1; // {1,0,0,0}
        value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
        assertEquals(2, value, maxError);
        coordinate[0] = 0;
        coordinate[1] = 1; // {0,1,0,0}
        value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
        assertEquals(1, value, maxError);
        coordinate[1] = 0;
        coordinate[3] = 1; // {0,0,0,1}
        value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
        assertEquals(0.25, value, maxError);
    }
    
    @Test public void testMaximize() {
        // Call method under test
        Object[] potentials = DiscretePotentialOperations.maximize(commonVariables.tpAB, commonVariables.a);
        TablePotential maximizedPotential = (TablePotential) potentials[0];
        GTablePotential choicesPotential = (GTablePotential) potentials[1];
        // Test maximized potential
        // Check variables
        List<Variable> variablesPotential = maximizedPotential.getVariables();
        assertEquals(1, variablesPotential.size());
        assertTrue(variablesPotential.contains(commonVariables.b));
        // Check table
        double[] table = maximizedPotential.getValues();
        assertEquals(3, table.length); // size table reduced
        assertEquals(0.7, table[0], maxError);
        assertEquals(0.5, table[1], maxError);
        assertEquals(0.6, table[2], maxError);
        // Test choices potential
        // Check variables. It must contain same variables as
        // maximized potential
        variablesPotential = choicesPotential.getVariables();
        assertEquals(1, variablesPotential.size());
        assertTrue(variablesPotential.contains(commonVariables.b));
        // Check table
        Choice choice = (Choice) choicesPotential.elementTable.get(0); // table[0]
        assertEquals(2, choice.getValues()[0]);
        assertEquals(commonVariables.a, choice.getVariable());
        assertEquals(1, choice.getNumValues());
        
        choice = (Choice) choicesPotential.elementTable.get(1); // table[1]
        assertEquals(1, choice.getValues()[0]);
        assertEquals(commonVariables.a, choice.getVariable());
        assertEquals(1, choice.getNumValues());
        
        choice = (Choice) choicesPotential.elementTable.get(2); // table[2]
        assertEquals(0, choice.getValues()[0]);
        assertEquals(commonVariables.a, choice.getVariable());
        assertEquals(1, choice.getNumValues());
        
    }
    
    @Test public void testReorder1() {
        double[] table = {0.2, 0.8, 0.4, 0.6};
        final int numVariables = 2;
        
        // Setup: create a table potential
        TablePotential orderedAB = // Variables: A, B
                SharedTestUtilities.createTablePotential(numVariables, table);
        List<Variable> variablesAB = orderedAB.getVariables();
        
        // Create variables in other order: B, A
        List<Variable> variablesBA = new ArrayList<>();
        variablesBA.add(variablesAB.get(1));
        variablesBA.add(variablesAB.get(0));
        
        // Call method DiscretePotentialOperations.reorder
        TablePotential reorderedBA = (TablePotential) orderedAB.reorder(variablesBA);
        
        // Test
        // 1. Test variables
        List<Variable> variablesReordered = reorderedBA.getVariables();
        assertEquals(numVariables, variablesReordered.size());
        assertEquals(variablesReordered.get(0), variablesAB.get(1));
        assertEquals(variablesReordered.get(1), variablesAB.get(0));
        // 2. Test table
        assertEquals(0.2, reorderedBA.getValues()[0], maxError);
        assertEquals(0.4, reorderedBA.getValues()[1], maxError);
        assertEquals(0.8, reorderedBA.getValues()[2], maxError);
        assertEquals(0.6, reorderedBA.getValues()[3], maxError);
    }
    
    @Test public void testReorder2() {
        // Original TablePotential: commonVariables.tpCAB. Variables: C,A,D
        List<Variable> variablesBeforeReorder = commonVariables.tpCAB.getVariables();
        List<Variable> variablesAfterReorder = new ArrayList<>(variablesBeforeReorder);
        Collections.reverse(variablesAfterReorder); // reorder the variables
        assertEquals(variablesAfterReorder.size(), variablesBeforeReorder.size());
        assertEquals( // Test Collections.reverse
                variablesAfterReorder.get(0), variablesBeforeReorder.get(2));
        TablePotential tablePotentialAfterReorder = (TablePotential) commonVariables.tpCAB.reorder(variablesAfterReorder);
        // Test variables:
        // 1. Test numVariables
        List<Variable> variablesReorderedPotential = tablePotentialAfterReorder.getVariables();
        int numVariablesReordered = variablesReorderedPotential.size();
        assertEquals(variablesAfterReorder.size(), numVariablesReordered);
        // 2. Test variables
        for (int i = 0; i < numVariablesReordered; i++) {
            assertEquals(variablesAfterReorder.get(i), variablesReorderedPotential.get(i));
        }
        // Test table of TablePotential
        assertEquals(commonVariables.tpCAB.getValues().length, tablePotentialAfterReorder.getValues().length);
        double[] reorderedTable = {0.2, 0.4, 0.1, 0.9, 0.3, 0.8, 0.8, 0.6, 0.9, 0.1, 0.7, 0.2};
        for (int i = 0; i < reorderedTable.length; i++) {
            assertEquals(reorderedTable[i], tablePotentialAfterReorder.getValues()[i], maxError);
        }
    }
    
    @Test public void testReorder3() {
        // another reorder
        List<Variable> variablesBeforeReorder = commonVariables.tpCAB.getVariables();
        List<Variable> variablesAfterReorder = new ArrayList<>(variablesBeforeReorder);
        variablesAfterReorder.remove(0);
        Collections.reverse(variablesAfterReorder); // reorder the variables
        variablesAfterReorder.add(0, variablesBeforeReorder.get(0));
        // Ordination: (0, 1, 2) -> (0, 2, 1)
        TablePotential tablePotentialAfterReorder = (TablePotential) commonVariables.tpCAB.reorder(variablesAfterReorder);
        // Test variables:
        // 1. Test numVariables
        List<Variable> variablesReorderedPotential = tablePotentialAfterReorder.getVariables();
        int numVariablesReordered = variablesReorderedPotential.size();
        assertEquals(variablesAfterReorder.size(), numVariablesReordered);
        // 2. Test variables
        for (int i = 0; i < numVariablesReordered; i++) {
            assertEquals(variablesAfterReorder.get(i), variablesReorderedPotential.get(i));
        }
        // Test table of TablePotential
        assertEquals(commonVariables.tpCAB.getValues().length, tablePotentialAfterReorder.getValues().length);
        double[] reorderedTable = {0.2, 0.8, 0.4, 0.6, 0.1, 0.9, 0.9, 0.1, 0.3, 0.7, 0.8, 0.2};
        for (int i = 0; i < reorderedTable.length; i++) {
            assertEquals(reorderedTable[i], tablePotentialAfterReorder.getValues()[i], maxError);
        }
    }
    
    @Test
    /** Test without utilities */ public void testSumOutVariable1() {
        Marginalization marginalization = new SumOutVariable(commonVariables.a, commonVariables.potentials);
        TablePotential utility = marginalization.getUtility();
        assertTrue(utility == null || (utility.getValues().length == 1 && utility.getValues()[0] == 0.0)); // No utility
        TablePotential probability = marginalization.getProbability();
        assertNotNull(probability);
        TablePotential tablePotential = marginalization.getProbability();
        assertEquals(PotentialRole.JOINT_PROBABILITY, tablePotential.getPotentialRole());
        List<Variable> variables = tablePotential.getVariables();
        assertEquals(3, variables.size());
        assertTrue(variables.contains(commonVariables.b));
        assertTrue(variables.contains(commonVariables.c));
        assertTrue(variables.contains(commonVariables.d));
        assertEquals(12, tablePotential.getValues().length);
    }
    
    // que me lleva a desactivar este test.
    //
    
    @Test
    @Disabled("Manolo> Hay un problema con la forma en que el métoodo SumOutVariable particiona el conjunto de potenciales entre de probabilidad y de utilidad")
    public void testSumOutVariable2() throws NonProjectablePotentialException {
        ProbNet perfectKnowledge = IDFactory.createNoKnowledge();
        Variable disease = perfectKnowledge.getVariable("Disease");
        Variable therapy = perfectKnowledge.getVariable("Therapy");
        List<Potential> networkPotentials = perfectKnowledge.getPotentials(disease);
        List<TablePotential> networkTablePotentials = generateTablePotentials(networkPotentials);
        Marginalization marginalization = new SumOutVariable(disease, networkTablePotentials);
        // Asserts
        TablePotential utility = marginalization.getUtility();
        assertNotNull(utility);
        assertFalse(utility instanceof StrategicTablePotential);
        List<Variable> utilityVariables = utility.getVariables();
        assertEquals(1, utilityVariables.size());
        assertTrue(utilityVariables.contains(therapy));
    }
    
    @Test public void testSumOutVariable3() {
        // Create P(X), P/Y/X), U(X,D)
        // Variables
        Variable X = new Variable("X", "a", "b");
        Variable Y = new Variable("Y", "c", "d");
        Variable decisionVariable = new Variable("D", "opt 1", "opt 2");
        // Potential P(X)
        List<Variable> pXvariables = new ArrayList<>(1);
        pXvariables.add(X);
        TablePotential pX = new TablePotential(pXvariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                               new double[]{0.8, 0.2});
        // Potential P(Y|X)
        List<Variable> pYXvariables = new ArrayList<>(2);
        pYXvariables.add(Y);
        pYXvariables.add(X);
        TablePotential pYX = new TablePotential(pYXvariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                new double[]{0.9, 0.1, 0.4, 0.6});
        // Potential U(X,D)
        List<Variable> decXDVariables = new ArrayList<>(2);
        decXDVariables.add(X);
        decXDVariables.add(decisionVariable);
        TablePotential pU = new TablePotential(decXDVariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                               new double[]{20, 30, 10, 40});
        // List of potentials
        List<TablePotential> potentials = new ArrayList<>(3);
        potentials.add(pX);
        potentials.add(pYX);
        potentials.add(pU);
        
        // Invocation
        Marginalization marginalization = new SumOutVariable(X, potentials);
        
        // TODO Finish
    }
    
    @Test public void testSumOutVariable4() {
        Marginalization marginalization = new SumOutVariable(commonVariables.a, commonVariables.potentials);
        TablePotential utility = marginalization.getUtility();
        assertTrue(utility == null || (utility.getValues().length == 1 && utility.getValues()[0] == 0.0)); // No utility
        TablePotential probability = marginalization.getProbability();
        assertNotNull(probability);
        TablePotential tablePotential = marginalization.getProbability();
        assertEquals(PotentialRole.JOINT_PROBABILITY, tablePotential.getPotentialRole());
        List<Variable> variables = tablePotential.getVariables();
        assertEquals(3, variables.size());
        assertTrue(variables.contains(commonVariables.b));
        assertTrue(variables.contains(commonVariables.c));
        assertTrue(variables.contains(commonVariables.d));
        assertEquals(12, tablePotential.getValues().length);
    }
    
    @Test
    public void testMerge() throws PotentialOperationException.DifferentSizesInPotentialsAndStates {
        // ----------------------------------------------------------------
        // 1. Plain TablePotential merge
        // ----------------------------------------------------------------
        Variable dec = new Variable("D", "d0", "d1");          // 2-state decision
        Variable X   = new Variable("X", "x0", "x1");          // 2-state chance

        TablePotential p0 = new TablePotential(List.of(X), PotentialRole.UNSPECIFIED);
        p0.getValues()[0] = 0.3;  // X=x0
        p0.getValues()[1] = 0.7;  // X=x1

        TablePotential p1 = new TablePotential(List.of(X), PotentialRole.UNSPECIFIED);
        p1.getValues()[0] = 0.6;
        p1.getValues()[1] = 0.4;

        AbstractIndexedPotential mergedRaw = DiscretePotentialOperations.merge(dec, Arrays.asList(p0, p1));
        TablePotential merged = (TablePotential) mergedRaw;

        // Plain merge must NOT return GTablePotential
        assertFalse(mergedRaw instanceof GTablePotential,
                "Plain-potential merge should return TablePotential, not GTablePotential");

        // Decision variable is first, X second
        List<Variable> mergedVars = merged.getVariables();
        assertEquals(2, mergedVars.size());
        assertEquals(dec, mergedVars.get(0), "Decision variable must be first");
        assertEquals(X,   mergedVars.get(1));

        // For variables [D(2), X(2)], offsets = [1, 2]:
        //   values[0] = (D=0, X=0)  from p0[X=0] = 0.3
        //   values[1] = (D=1, X=0)  from p1[X=0] = 0.6
        //   values[2] = (D=0, X=1)  from p0[X=1] = 0.7
        //   values[3] = (D=1, X=1)  from p1[X=1] = 0.4
        assertEquals(4, merged.getValues().length);
        assertEquals(0.3, merged.getValues()[0], maxError, "(D=0, X=0)");
        assertEquals(0.6, merged.getValues()[1], maxError, "(D=1, X=0)");
        assertEquals(0.7, merged.getValues()[2], maxError, "(D=0, X=1)");
        assertEquals(0.4, merged.getValues()[3], maxError, "(D=1, X=1)");

        // ----------------------------------------------------------------
        // 2. GTablePotential<CEP> merge
        // ----------------------------------------------------------------
        CEP cep00 = new CEP.CEPBuilder().build(null, 100.0, 2.0);
        CEP cep01 = new CEP.CEPBuilder().build(null, 200.0, 3.0);
        CEP cep10 = new CEP.CEPBuilder().build(null, 300.0, 4.0);
        CEP cep11 = new CEP.CEPBuilder().build(null, 400.0, 5.0);

        GTablePotential<CEP> g0 = new GTablePotential<>(List.of(X), PotentialRole.UNSPECIFIED);
        g0.elementTable.add(cep00);  // X=x0
        g0.elementTable.add(cep01);  // X=x1

        GTablePotential<CEP> g1 = new GTablePotential<>(List.of(X), PotentialRole.UNSPECIFIED);
        g1.elementTable.add(cep10);
        g1.elementTable.add(cep11);

        AbstractIndexedPotential gMergedRaw = DiscretePotentialOperations.merge(dec, Arrays.asList(g0, g1));

        assertInstanceOf(GTablePotential.class, gMergedRaw,
                "GTablePotential merge must return a GTablePotential");
        @SuppressWarnings("unchecked")
        GTablePotential<CEP> gMerged = (GTablePotential<CEP>) gMergedRaw;

        // Variable order: [D, X]
        assertEquals(dec, gMerged.getVariables().get(0));
        assertEquals(X,   gMerged.getVariables().get(1));

        // elementTable has 4 entries
        assertEquals(4, gMerged.elementTable.size());

        // CEP placement mirrors the double-values layout:
        //   position 0: (D=0, X=0) → cep00
        //   position 1: (D=1, X=0) → cep10
        //   position 2: (D=0, X=1) → cep01
        //   position 3: (D=1, X=1) → cep11
        assertSame(cep00, gMerged.elementTable.get(0), "(D=0, X=0) must be cep00");
        assertSame(cep10, gMerged.elementTable.get(1), "(D=1, X=0) must be cep10");
        assertSame(cep01, gMerged.elementTable.get(2), "(D=0, X=1) must be cep01");
        assertSame(cep11, gMerged.elementTable.get(3), "(D=1, X=1) must be cep11");

        // ----------------------------------------------------------------
        // 3. Context variables: potentials have more than one variable
        // ----------------------------------------------------------------
        Variable Y = new Variable("Y", "y0", "y1");
        TablePotential pXY0 = new TablePotential(Arrays.asList(X, Y), PotentialRole.UNSPECIFIED);
        TablePotential pXY1 = new TablePotential(Arrays.asList(X, Y), PotentialRole.UNSPECIFIED);
        Arrays.fill(pXY0.getValues(), 1.0);
        Arrays.fill(pXY1.getValues(), 2.0);

        TablePotential mergedXY = (TablePotential) DiscretePotentialOperations.merge(dec, Arrays.asList(pXY0, pXY1));
        assertEquals(3, mergedXY.getVariables().size(), "merged variables: [D, X, Y]");
        assertEquals(dec, mergedXY.getVariables().getFirst());
        assertEquals(8, mergedXY.getValues().length, "2 × 2 × 2 = 8 positions");
        // Positions where D=0 come from pXY0 (1.0), positions where D=1 from pXY1 (2.0)
        // D is the first variable, so it alternates: [1.0, 2.0, 1.0, 2.0, ...]
        for (int i = 0; i < 8; i++) {
            double expected = (i % 2 == 0) ? 1.0 : 2.0;
            assertEquals(expected, mergedXY.getValues()[i], maxError,
                    "Position " + i + ": D=" + (i % 2));
        }

        // ----------------------------------------------------------------
        // 4. Size mismatch must throw
        // ----------------------------------------------------------------
        assertThrows(
                PotentialOperationException.DifferentSizesInPotentialsAndStates.class,
                () -> DiscretePotentialOperations.merge(dec, List.of(p0)), // 2-state dec, only 1 potential
                "Providing fewer potentials than decision states must throw");
    }
    
    /**
     * Extends {@link #testMultiplyAndMaximize()} to verify the second element of
     * the result — the {@code GTablePotential<Choice>} that records which decision
     * state was chosen as optimal at each position of the remaining variables.
     */
    @Test
    public void testMultiplyAndMaximize_choiceTableIsPopulated() {
        ArrayList<Potential> potentialsVariable = new ArrayList<>();
        potentialsVariable.add(commonVariables.tpAB);
        potentialsVariable.add(commonVariables.tpCAB);
        Variable a = commonVariables.a; // variable to maximize

        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(potentialsVariable, a);
        assertEquals(2, result.length);

        // result[1] must be a GTablePotential<Choice>
        assertInstanceOf(GTablePotential.class, result[1], "result[1] must be a GTablePotential");
        @SuppressWarnings("unchecked")
        GTablePotential<Choice> choices = (GTablePotential<Choice>) result[1];

        TablePotential maximized = (TablePotential) result[0];

        // Same variables as the maximized potential
        assertEquals(maximized.getVariables(), choices.getVariables(),
                "Choice potential must have the same variables as the maximized potential");
        assertEquals(maximized.getValues().length, choices.elementTable.size(),
                "One Choice per position in the result table");

        // Spot-check: position 0 always corresponds to (all context vars = 0).
        //
        // Position 0 → all context variables at state 0 (regardless of variable ordering)
        //   tpAB[A=0,B=0]*tpCAD[C=0,A=0,D=0] = 0.1*0.2 = 0.02
        //   tpAB[A=1,B=0]*tpCAD[C=0,A=1,D=0] = 0.2*0.1 = 0.02
        //   tpAB[A=2,B=0]*tpCAD[C=0,A=2,D=0] = 0.7*0.3 = 0.21  ← max at A=2
        //
        // Note: variablesToKeep is built from a HashSet, so the variable order
        // in the result potential is non-deterministic beyond position 0.
        // Position-specific assertions beyond position 0 would be fragile.
        Choice ch0 = choices.elementTable.getFirst();
        assertEquals(a, ch0.getVariable());
        assertEquals(1, ch0.getNumValues());
        assertEquals(2, ch0.getValues()[0], "(all context vars=0): optimal A should be 2");

        // Structural assertions across all choices
        for (int i = 0; i < choices.elementTable.size(); i++) {
            Choice ch = choices.elementTable.get(i);
            assertNotNull(ch, "Choice at position " + i + " must not be null");
            assertEquals(a, ch.getVariable(), "Choice must reference the maximized variable");
            assertTrue(ch.getNumValues() >= 1, "At least one optimal state per position");
            for (int k = 0; k < ch.getNumValues(); k++) {
                int stateIdx = ch.getValues()[k];
                assertTrue(stateIdx >= 0 && stateIdx < a.getNumStates(),
                        "State index " + stateIdx + " must be in [0, " + a.getNumStates() + ")");
            }
        }
    }

    /**
     * Tests that tied states are correctly recorded in the {@code Choice} table
     * when all decision states yield equal utility.
     */
    @Test
    public void testMultiplyAndMaximize_tiedStatesBothRecorded() {
        Variable dec = new Variable("Dec", "opt0", "opt1");
        TablePotential uniform = new TablePotential(List.of(dec), PotentialRole.UNSPECIFIED);
        uniform.getValues()[0] = 1.0;
        uniform.getValues()[1] = 1.0; // tie

        ArrayList<Potential> potsList = new ArrayList<>();
        potsList.add(uniform);
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(potsList, dec);

        @SuppressWarnings("unchecked")
        GTablePotential<Choice> choices = (GTablePotential<Choice>) result[1];
        assertEquals(1, choices.elementTable.size(), "One remaining position (no context variables)");

        Choice ch = choices.elementTable.getFirst();
        assertEquals(dec, ch.getVariable());
        assertEquals(2, ch.getNumValues(), "Both tied states must be recorded");

        int[] vals = ch.getValues();
        boolean has0 = false, has1 = false;
        for (int i = 0; i < ch.getNumValues(); i++) {
            if (vals[i] == 0) has0 = true;
            if (vals[i] == 1) has1 = true;
        }
        assertTrue(has0, "State 0 must be listed as a tied optimum");
        assertTrue(has1, "State 1 must be listed as a tied optimum");
    }

    /**
     * Tests a three-way tie: all three states have equal utility, so numValues must be 3.
     */
    @Test
    public void testMultiplyAndMaximize_threeWayTieAllRecorded() {
        Variable dec = new Variable("Dec", "opt0", "opt1", "opt2");
        TablePotential uniform = new TablePotential(List.of(dec), PotentialRole.UNSPECIFIED);
        Arrays.fill(uniform.getValues(), 5.0);

        ArrayList<Potential> potsList3 = new ArrayList<>();
        potsList3.add(uniform);
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(potsList3, dec);

        @SuppressWarnings("unchecked")
        GTablePotential<Choice> choices = (GTablePotential<Choice>) result[1];
        Choice ch = choices.elementTable.getFirst();

        assertEquals(3, ch.getNumValues(), "All three tied states must be recorded");
    }

    private List<TablePotential> generateTablePotentials(List<Potential> potentials) throws NonProjectablePotentialException {
        List<TablePotential> tablePotentials = new ArrayList<>(potentials.size());
        for (Potential potential : potentials) {
            if (potential instanceof ExactDistrPotential) {
                tablePotentials.add(((ExactDistrPotential) potential).getTablePotential());
            } else {
                tablePotentials.add(potential.getCPT());
            }
        }
        return tablePotentials;
    }
    
    @Disabled("Ignored because a NullPointerException")
    @Test
    public void testMaxOutVariable() throws NonProjectablePotentialException {
        // Method invocation
        ProbNet perfectKnowledge = IDFactory.createPerfectKnowledge();
        Variable disease = perfectKnowledge.getVariable("Disease");
        Variable therapy = perfectKnowledge.getVariable("Therapy");
        List<Potential> potentials = perfectKnowledge.getPotentials(therapy);
        List<TablePotential> tablePotentials = projectToTable(potentials, perfectKnowledge);
        TablePotential utility = (new MaxOutVariable(therapy, null, tablePotentials)).getUtility();
        
        // Asserts
        // Test utility potential
        assertNotNull(utility);
        
        // Utility variables
        List<Variable> utilityVariables = utility.getVariables();
        assertEquals(1, utilityVariables.size());
        
        // Test utility values
        assertEquals(disease, utilityVariables.getFirst());
        assertEquals(2, utility.getValues().length);
        assertEquals(10, utility.getValues()[0], maxError);
        assertEquals(7.75, utility.getValues()[1], maxError);
        
        // Test utility interventions
        assertInstanceOf(StrategicTablePotential.class, utility);
        StrategicTablePotential strategicUtility = (StrategicTablePotential) utility;
        assertNotNull(strategicUtility.strategyTrees);
        assertEquals(2, strategicUtility.strategyTrees.length);
        StrategyTree strategyTreeNo = strategicUtility.strategyTrees[0];
        assertEquals(therapy, strategyTreeNo.getRootVariable());
        List<TreeADDBranch> branches = strategyTreeNo.getBranches();
        assertEquals(1, branches.size());
        TreeADDBranch branch = branches.getFirst();
        List<State> states = branch.getBranchStates();
        assertEquals(1, states.size());
        State noState = states.getFirst();
        assertEquals(0, therapy.getStateIndex(noState));
    }
    
    private List<TablePotential> projectToTable(List<Potential> potentials, ProbNet probNet) throws NonProjectablePotentialException {
        List<TablePotential> tablePotentials = new ArrayList<>(potentials.size());
        for (Potential potential : potentials) {
            tablePotentials.add(potential.tableProject(new EvidenceCase(), probNet.getInferenceOptions()));
        }
        return tablePotentials;
    }
    
    //TODO This test should be reviewed after we decide what to do with the role policy
/*	@Test
	public void joinTestMaxOutAndSumOut() throws NodeNotFoundException {
		ProbNet testDecision = org.openmarkov.core.model.network.factory.IDFactory.buildIDDecideTest();

		// Remove disease
		Variable disease = testDecision.getVariable("Disease");
		List<TablePotential> diseasePotentials = getTablePotentials(testDecision.getPotentials(disease));
		List<TablePotential> afterRemovingDiseasePotentials = 
				DiscretePotentialOperations.sumOutVariable(disease, diseasePotentials);
		removePotentials(testDecision, diseasePotentials);
		testDecision.removeNode(testDecision.getNode(disease));
		addNoPolicyPotentials(testDecision, afterRemovingDiseasePotentials);
		
		// Remove therapy
		Variable therapy = testDecision.getVariable("Therapy");
		List<TablePotential> therapyPotentials = getTablePotentials(testDecision.getPotentials(therapy));
		List<TablePotential> afterRemovingTherapyPotentials = 
				DiscretePotentialOperations.maxOutVariable(therapy, therapyPotentials);
		removePotentials(testDecision, therapyPotentials);
		testDecision.removeNode(testDecision.getNode(therapy));
		addNoPolicyPotentials(testDecision, afterRemovingTherapyPotentials);
	
		// Remove result of test
		Variable resultOfTest = testDecision.getVariable("Result of test");
		List<TablePotential> resultOfTestPotentials = getTablePotentials(testDecision.getPotentials(resultOfTest));
		List<TablePotential> afterRemovingResultOfTestPotentials = 
				DiscretePotentialOperations.sumOutVariable(resultOfTest, resultOfTestPotentials);
		removePotentials(testDecision, resultOfTestPotentials);
		testDecision.removeNode(testDecision.getNode(resultOfTest));
		addNoPolicyPotentials(testDecision, afterRemovingResultOfTestPotentials);

		// Remove do test
		Variable doTest = testDecision.getVariable("Do test?");
		List<TablePotential> doTestPotentials = getTablePotentials(testDecision.getPotentials(doTest));
		List<TablePotential> afterRemovingDoTestPotentials = 
				DiscretePotentialOperations.maxOutVariable(doTest, doTestPotentials);
		removePotentials(testDecision, afterRemovingDoTestPotentials);
		testDecision.removeNode(testDecision.getNode(doTest));
		addNoPolicyPotentials(testDecision, afterRemovingDoTestPotentials);
	}*/
    
    private void addNoPolicyPotentials(ProbNet probNet, List<TablePotential> potentials) {
        for (Potential potential : potentials) {
            if (potential.getPotentialRole() != PotentialRole.POLICY) {
                probNet.addPotential(potential);
            }
        }
    }
    
    private void removePotentials(ProbNet probNet, List<TablePotential> potentials) {
        for (Potential potential : potentials) {
            probNet.removePotential(potential);
        }
    }
    
    /**
     * Translates the coordinate received in (variables, coordinateVariables)
     * to the potential variables ordination and returns the configuration
     * value.
     *
     * @param variables           <code>ArrayList</code> of <code>Variable</code>
     * @param coordinateVariables <code>int[]</code>
     * @param potential           <code>TablePotential</code>
     *
     * @return configuration value. <code>double</code>
     */
    private double getConfiguration(List<Variable> variables, int[] coordinateVariables, TablePotential potential) {
        List<Variable> variablesPotential = potential.getVariables();
        int[] coordinate = new int[variablesPotential.size()];
        int i = 0;
        for (Variable variablePotential : variablesPotential) {
            coordinate[i++] = coordinateVariables[variables.indexOf(variablePotential)];
        }
        return potential.getValues()[potential.getPosition(coordinate)];
    }
    
    /**
     * @param potentials <code>ArrayList</code> of <code>Potential</code>.
     *
     * @return An <code>ArrayList</code> of <code>Variable</code> with all the
     * variables of the potentials in order: first the variables of the first
     * potential, next the remaining variables of the second potential, etc.
     */
    private List<Variable> getUnionVariablesOrdered(List<? extends Potential> potentials) {
        List<Variable> variables = new ArrayList<>();
        for (Potential potential : potentials) {
            for (Variable variable : potential.getVariables()) {
                if (!variables.contains(variable)) {
                    variables.add(variable);
                }
            }
        }
        return variables;
    }
    
}
