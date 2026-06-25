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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ConditionalGaussianPotentialTest {
    
    private ConditionalGaussianPotential gaussianPotential;
    private Variable predictedAudiometry;
    private Variable micAge;
    
    @BeforeEach public void setUp() {
        Variable meanVariable = new Variable("Mean");
        Variable varianceVariable = new Variable("Variance");
        predictedAudiometry = new Variable("Predicted audiometry", "off/off", "off", "on");
        Variable processorTypeChanged = new Variable("Processor type changed", "no", "yes");
        micAge = new Variable("Mic age", "<=30", ">30 and <=90", ">90 and <= 365", ">365");
        Variable electrodeChanged = new Variable("Electrode changed", "0", "1", "2", "3+");
        Variable audiometry = new Variable("Audiometry", "off/off", "off", "on");
        List<Variable> parentVariables = asList(predictedAudiometry, processorTypeChanged, micAge, electrodeChanged);
        List<Variable> meanPotentialVariables = asList(meanVariable, predictedAudiometry, processorTypeChanged, micAge,
                                                       electrodeChanged);
        List<Variable> potentialVariables = new ArrayList<>(parentVariables);
        potentialVariables.addFirst(audiometry);
        LinearCombinationPotential meanPotential = new LinearCombinationPotential(meanPotentialVariables,
                                                                                  PotentialRole.CONDITIONAL_PROBABILITY);
        meanPotential.setCoefficients(new double[]{0, 1, 0.1, -0.2, 0.05});
        List<Variable> variancePotentialVariables = asList(varianceVariable, predictedAudiometry, processorTypeChanged,
                                                           micAge, electrodeChanged);
        LinearCombinationPotential variancePotential = new LinearCombinationPotential(variancePotentialVariables,
                                                                                      PotentialRole.CONDITIONAL_PROBABILITY);
        variancePotential.setCoefficients(new double[]{1, 0, 0.2, 0.2, 0.1});
        gaussianPotential = new ConditionalGaussianPotential(potentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        gaussianPotential.setMean(meanPotential);
        gaussianPotential.setVariance(variancePotential);
    }
    
    @Test public void testTableProject() throws NumberFormatException, NonProjectablePotentialException {
        
        TablePotential projectedPotential = gaussianPotential.tableProject(new EvidenceCase(), null);
        
        Assertions.assertEquals(288, projectedPotential.tableSize);
        Assertions.assertEquals(0.6914, projectedPotential.getValues()[0], 10E-4);
        Assertions.assertEquals(0.2417, projectedPotential.getValues()[1], 10E-4);
        Assertions.assertEquals(0.0668, projectedPotential.getValues()[2], 10E-4);
        Assertions.assertEquals(0.3085, projectedPotential.getValues()[3], 10E-4);
        Assertions.assertEquals(0.3829, projectedPotential.getValues()[4], 10E-4);
        Assertions.assertEquals(0.3085, projectedPotential.getValues()[5], 10E-4);
        Assertions.assertEquals(0.0668, projectedPotential.getValues()[6], 10E-4);
        Assertions.assertEquals(0.2417, projectedPotential.getValues()[7], 10E-4);
        Assertions.assertEquals(0.6914, projectedPotential.getValues()[8], 10E-4);
        Assertions.assertEquals(0.6305, projectedPotential.getValues()[9], 10E-4);
        Assertions.assertEquals(0.2477, projectedPotential.getValues()[10], 10E-4);
        Assertions.assertEquals(0.1216, projectedPotential.getValues()[11], 10E-4);
    }
    
    @Test public void testTableProjectWithEvidence()
            throws org.openmarkov.core.exception.IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(predictedAudiometry, 2)); // on
        evidence.addFinding(new Finding(micAge, 2)); // >90 and <= 365
        
        TablePotential projectedPotential = gaussianPotential.tableProject(evidence, null);
        
        Assertions.assertEquals(24, projectedPotential.tableSize);
        Assertions.assertEquals(0.2160, projectedPotential.getValues()[0], 10E-4);
        Assertions.assertEquals(0.2555, projectedPotential.getValues()[1], 10E-4);
        Assertions.assertEquals(0.5284, projectedPotential.getValues()[2], 10E-4);
        Assertions.assertEquals(0.2266, projectedPotential.getValues()[3], 10E-4);
        Assertions.assertEquals(0.2236, projectedPotential.getValues()[4], 10E-4);
        Assertions.assertEquals(0.5497, projectedPotential.getValues()[5], 10E-4);
        Assertions.assertEquals(0.2216, projectedPotential.getValues()[6], 10E-4);
        Assertions.assertEquals(0.2385, projectedPotential.getValues()[7], 10E-4);
        Assertions.assertEquals(0.5398, projectedPotential.getValues()[8], 10E-4);
        
    }
    
}
