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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
    
    /**
     * Projecting on evidence for some of the parents must return a table whose
     * FIRST variable is the conditioned one: {@code getConditionedVariable()} is
     * defined as the first variable, and consumers use it to decide which node a
     * projected table belongs to. The multiplication of the factorization used to
     * order the result however the product came out; measured on the DAN
     * evaluation that uncovered it, the projected table came back headed by a
     * parent, was attributed to that parent's node, and overwrote the parent's
     * own distribution (the testDANDating family).
     */
    @Test public void tableProjectKeepsTheConditionedVariableFirst() throws Exception {
        maxPotential.setLeakyParameters(new double[]{0.989, 0.01, 0.001});
        Variable variableB = maxPotential.getVariables().get(2);
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(variableB, variableB.getState("B1")));

        TablePotential projected = maxPotential.tableProject(evidence, null);

        assertEquals("C", projected.getVariables().get(0).getName(),
                "the conditioned variable must stay first after projecting");
        // And the numbers are the ones the expanded CPT gives when projected the
        // plain-table way, which keeps the variable order on its own.
        TablePotential reference = maxPotential.getCPT().tableProject(evidence, null);
        assertEquals(reference.getVariables(), projected.getVariables());
        assertArrayEquals(reference.getValues(), projected.getValues(), admissibleError);
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
