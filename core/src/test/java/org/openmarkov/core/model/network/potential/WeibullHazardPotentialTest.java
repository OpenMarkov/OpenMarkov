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
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
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

    /**
     * Sampling relies on a parent variable named exactly {@code Lambda}. Without it, the answer
     * must say what is missing, not break with a message that names nothing.
     */
    @Test
    public void samplingWithoutALambdaParentSaysWhatIsMissing() throws Exception {
        potential.setTimeVariable(ageVar);
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(ageVar, 65.0));

        InvalidArgumentException error = Assertions.assertThrows(InvalidArgumentException.class,
                () -> potential.sampleConditionedVariable(new double[]{0.5}, evidence));
        Assertions.assertTrue(error.getMessage().contains("Lambda"),
                () -> "the error must name the missing variable; it says: " + error.getMessage());
    }

    /**
     * The rest of the class locates the shape parameter gamma by asking the covariates for its
     * position; sampling read position 0 unconditionally, so the two disagreed as soon as the
     * covariates came in another order.
     */
    @Test
    public void theGammaUsedForSamplingIsTheOneTheCovariatesDeclare() throws Exception {
        Variable lambdaVar = new Variable("Lambda", true, 0.0, 1000.0, false, 0.001);
        Variable timeVar = new Variable("Time", true, 0.0, 1000.0, false, 0.001);
        VariableExpression[] covariates = {VariableExpression.Common.CONSTANT, VariableExpression.Common.GAMMA};
        // A stray large value sits where the old code read gamma from; the declared gamma is ln 2.
        double[] coefficients = {99.0, Math.log(2)};
        WeibullHazardPotential weibull = new WeibullHazardPotential(
                Arrays.asList(new Variable("X", 2), lambdaVar, timeVar), PotentialRole.CONDITIONAL_PROBABILITY,
                covariates, coefficients, null);
        weibull.setTimeVariable(timeVar);
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(lambdaVar, 0.1));
        evidence.addFinding(new Finding(timeVar, 2.0));

        // gamma = 2 → transition probability = 1 − exp(0.1·(1² − 2²)) ≈ 0.26 < 0.5 → no event.
        Assertions.assertEquals(0.0, weibull.sampleConditionedVariable(new double[]{0.5}, evidence));
    }
}
