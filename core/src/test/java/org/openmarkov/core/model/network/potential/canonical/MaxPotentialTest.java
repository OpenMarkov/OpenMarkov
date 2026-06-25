/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MaxPotentialTest {
    
    private final double admissibleError = 0.000000001;
    // Attributes
    private MaxPotential maxPotential;
    
    // Initialization
    @BeforeEach public void setUp() {
        
        // Define the variables
        Variable variableA = new Variable("A", "A0", "A1", "A2");
        Variable variableB = new Variable("B", "B0", "B1");
        Variable variableC = new Variable("C", "C0", "C1", "C2");
        
        // Conditional probability table for C: causal MAX
        ArrayList<Variable> variablesABC = new ArrayList<>();
        variablesABC.add(variableC);
        variablesABC.add(variableA);
        variablesABC.add(variableB);
        
        maxPotential = new MaxPotential(variablesABC);
        maxPotential.setNoisyParameters(variableA, new double[]{1.0, 0.0, 0.0, 0.0, 0.3, 0.7, 0.0, 0.1, 0.9});
        maxPotential.setNoisyParameters(variableB, new double[]{1.0, 0.0, 0.0, 0.0, 0.2, 0.8});
    }
    
    @Test public void testGetLeakPotential() {
        maxPotential.setLeakyParameters(new double[]{0.989, 0.01, 0.001});
        assertEquals(0.989, maxPotential.getLeakyParameters()[0], admissibleError);
        assertEquals(0.01, maxPotential.getLeakyParameters()[1], admissibleError);
        assertEquals(0.001, maxPotential.getLeakyParameters()[2], admissibleError);
    }
    
    @Test public void testGetCPT() {
        maxPotential.setLeakyParameters(new double[]{0.989, 0.01, 0.001});
        double[] cPTValues = null;
        cPTValues = maxPotential.getCPT().getValues();
        assertEquals(0.989, cPTValues[0], admissibleError);
        assertEquals(0.01, cPTValues[1], admissibleError);
        assertEquals(0.2997, cPTValues[4], admissibleError);
        assertEquals(0.0999, cPTValues[7], admissibleError);
        assertEquals(0.0, cPTValues[15], admissibleError);
        assertEquals(0.98002, cPTValues[17], admissibleError);
    }
    
    @Test public void testGetCPTDefaultLeaky() {
        double[] cPTValues = null;
        cPTValues = maxPotential.getCPT().getValues();
        assertEquals(1.0, cPTValues[0], admissibleError);
        assertEquals(0.0, cPTValues[1], admissibleError);
        assertEquals(0.3, cPTValues[4], admissibleError);
        assertEquals(0.1, cPTValues[7], admissibleError);
        assertEquals(0.0, cPTValues[15], admissibleError);
        assertEquals(0.98, cPTValues[17], admissibleError);
    }
    
}
