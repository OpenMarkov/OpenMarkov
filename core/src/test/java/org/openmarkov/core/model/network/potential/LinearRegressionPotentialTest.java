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
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LinearRegressionPotentialTest {
    
    private LinearCombinationPotential potential = null;

    @BeforeEach public void setUp() {
        
        // Revision Risk
        Variable ageAtStateEntryVar = new Variable("Age at state entry", "3.4", "4.4");
        Variable timeInStateVar = new Variable("Time in state", "0", "1");
        Variable ageVar = new Variable("Age", "4.4");
        
        List<Variable> variables = Arrays.asList(ageAtStateEntryVar, ageVar, timeInStateVar);
        double[] coefficients = new double[]{0, 1, -1};
        VariableExpression[] covariates = new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "Constant"),
                ageVar.asVariableExpression(),
                timeInStateVar.asVariableExpression()
        };
        potential = new LinearCombinationPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, covariates, coefficients);
    }
    
    @Test public void testTableProject() throws NonProjectablePotentialException {
        EvidenceCase evidence = new EvidenceCase();
        TablePotential projectedPotential = potential.tableProject(evidence, null);
        double[] expectedValues = new double[]{0, 1, 1, 0};
        Assertions.assertArrayEquals(expectedValues, projectedPotential.getValues(), 0.00001);
    }
    
}
