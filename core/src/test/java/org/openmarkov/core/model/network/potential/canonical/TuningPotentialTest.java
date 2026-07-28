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
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TuningPotentialTest {
    
    private final double admissibleError = 0.000000001;
    
    private TuningPotential tuningModelPotential;
    
    // Initialization
    @BeforeEach public void setUp() {
        // Define the variables
        Variable dT = new Variable("dT", "down", "st.quo", "up");
        Variable dM = new Variable("dM", "down", "st.quo", "up");
        Variable dG = new Variable("dG", "down", "st.quo", "up");
        Variable nerveSoft = new Variable("Nerve_Soft", "Softer", "St.quo", "Louder");
        
        List<Variable> variables = new ArrayList<>();
        variables.add(nerveSoft);
        variables.add(dT);
        variables.add(dM);
        variables.add(dG);
        
        tuningModelPotential = new TuningPotential(variables);
        
        tuningModelPotential.setNoisyParameters(dT, new double[]{1.0, 0.0, 0.0, 1.0});
        tuningModelPotential.setNoisyParameters(dM, new double[]{0.1, 0.2, 0.2, 0.1});
        tuningModelPotential.setNoisyParameters(dG, new double[]{1.0, 0.0, 0.0, 1.0});
    }
    
    @Test public void testGetCPT() throws NonProjectablePotentialException {
        double[] cPTValues = tuningModelPotential.getCPT().getValues();
        assertEquals(1.0, cPTValues[0], admissibleError);
        assertEquals(0.0, cPTValues[1], admissibleError);
        assertEquals(0.8, cPTValues[3], admissibleError);
        assertEquals(0.2, cPTValues[4], admissibleError);
        assertEquals(1.0, cPTValues[18], admissibleError);
        assertEquals(1.0, cPTValues[80], admissibleError);
    }

    // -----------------------------------------------------------------------
    // The x=+ column with ASYMMETRIC parameters (the fixed copy-paste slip)
    // -----------------------------------------------------------------------

    /**
     * The middle cell of the x=+ column used to repeat the expression of the x=- column, so
     * with asymmetric parameters (c++ != c--, c+- != c-+) the x=+ column did not add up
     * to 1: the table was not a probability distribution. The parameters of the fixture
     * above are symmetric, which is exactly why the old tests could not see it.
     */
    @Test public void withAsymmetricParametersEveryColumnAddsUpToOne() {
        Variable child = new Variable("Z", "down", "st.quo", "up");
        Variable parent = new Variable("X", "down", "st.quo", "up");
        TuningPotential potential = new TuningPotential(new ArrayList<>(List.of(child, parent)));

        // c++ = 0.6, c+- = 0.1, c-+ = 0.2, c-- = 0.3 : deliberately asymmetric
        potential.setNoisyParameters(parent, new double[]{0.6, 0.1, 0.2, 0.3});

        double[] table = potential.getNoisyParameters(parent);
        for (int column = 0; column < 3; column++) {
            double sum = table[3 * column] + table[3 * column + 1] + table[3 * column + 2];
            assertEquals(1.0, sum, admissibleError, "Column " + column + " does not add up to 1");
        }
        assertEquals(1 - 0.6 - 0.1, table[7], admissibleError,
                "The middle cell of the x=+ column must be the complement of its own column");
    }
}
