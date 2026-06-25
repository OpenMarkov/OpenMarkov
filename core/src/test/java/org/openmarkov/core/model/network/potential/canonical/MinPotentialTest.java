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
public class MinPotentialTest {
    
    private final double admissibleError = 0.000000001;
    // Attributes
    private MinPotential minPotential;
    
    // Initialization
    @BeforeEach public void setUp() {
        
        // Define the variables
        Variable variableA = new Variable("A", "A0", "A1", "A2");
        Variable variableB = new Variable("B", "B0", "B1");
        Variable variableC = new Variable("C", "C0", "C1", "C2");
        
        // Conditional probability table for C: causal MIN
        ArrayList<Variable> variablesABC = new ArrayList<>();
        variablesABC.add(variableC);
        variablesABC.add(variableA);
        variablesABC.add(variableB);
        
        minPotential = new MinPotential(variablesABC);
        minPotential.setNoisyParameters(variableA, new double[]{0.0, 0.0, 1.0, 0.0, 0.2, 0.8, 0.7, 0.3, 0.0});
        minPotential.setNoisyParameters(variableB, new double[]{0.0, 0.0, 1.0, 0.6, 0.3, 0.1});
        minPotential.setLeakyParameters(new double[]{0.01, 0.1, 0.89});
    }
    
    @Test public void testGetLeakPotential() {
        assertEquals(0.01, minPotential.getLeakyParameters()[0], admissibleError);
        assertEquals(0.1, minPotential.getLeakyParameters()[1], admissibleError);
        assertEquals(0.89, minPotential.getLeakyParameters()[2], admissibleError);
    }
    
    @Test public void testGetCPT() {
        double[] cPTValues = null;
        cPTValues = minPotential.getCPT().getValues();
        assertEquals(0.01, cPTValues[0], admissibleError);
        assertEquals(0.1, cPTValues[1], admissibleError);
        assertEquals(0.278, cPTValues[4], admissibleError);
        assertEquals(0.297, cPTValues[7], admissibleError);
        assertEquals(0.8812, cPTValues[15], admissibleError);
        assertEquals(0.0, cPTValues[17], admissibleError);
    }
    
}
