/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class WeibullHazardPotentialTest {
    
    private WeibullHazardPotential potential = null;
    private Variable rrVar = null;
    private Variable sexVar = null;
    private Variable ageVar = null;
    private Variable prosthesisTypeVar = null;
    
    @BeforeEach public void setUp() {
        
        // Revision Risk
        rrVar = new Variable("Revision Risk", "no", "yes");
        rrVar.setTimeSlice(5);
        sexVar = new Variable("Sex", "Female", "Male");
        ageVar = new Variable("Age", true, 0.0, Double.POSITIVE_INFINITY, false, 1.0);
        prosthesisTypeVar = new Variable("Prosthesis Type", "Standard", "NP1");
        ageVar.setTimeSlice(5);
        
        List<Variable> variables = Arrays.asList(rrVar, ageVar, sexVar, prosthesisTypeVar);
        double[] coefficients = new double[]{0.3740968, -5.490935, -0.0367022, 0.768536, -1.344474};
        double[] covarianceMatrix = new double[]{0.0022515, -0.005691, 0.0432191, 0.000000028, -0.000783, 0.00002715,
                0.0000051, -0.007247, 0.000033, 0.01189, 0.000259, -0.000642, -0.000111, 0.000184, 0.14636};
        potential = new WeibullHazardPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, coefficients,
                                               covarianceMatrix);
        potential.log = true;
    }
    
    @Test
    public void testTableProject() throws NumberFormatException, NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(ageVar, 65.0));
        TablePotential projectedPotential = potential.tableProject(evidence, null);
        double[] expectedValues = new double[]{0.99891, 0.00109, 0.99765, 0.00235, 0.99972, 2.84367E-4, 0.99939,
                6.13169E-4};
        Assertions.assertArrayEquals(expectedValues, projectedPotential.getValues(), 0.00001);
    }
    
    @Test public void testCholeskyDecomposition() {
        double[] cholesky = potential.getCholeskyDecomposition();
        double[] expectedCholesky = new double[]{0.0474, -0.1199, 0.1698, 5.901E-07, -0.00461, 0.00242, 0.0001074,
                -0.0426, -0.0673, 0.07451, 0.005458, 0.00007454, -0.0455, -0.03864, 0.3778};
        
        Assertions.assertArrayEquals(expectedCholesky, cholesky, 0.0001);
    }
    
}
