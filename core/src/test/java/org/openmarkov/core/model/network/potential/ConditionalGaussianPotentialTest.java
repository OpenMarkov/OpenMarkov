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
    
    /**
     * The first three configurations have variance 1, so their numbers are the same whether the
     * variance is read as a variance or as a standard deviation. The fourth one tells them apart:
     * its mean is 0.1 and its variance 1.2, so sigma = 1.095445 and the thresholds 0.5 and 1.5 sit
     * at z = 0.365148 and z = 1.277919, with Phi(0.365148) = 0.642500 and Phi(1.277919) = 0.899121.
     */
    @Test public void testTableProject() throws NumberFormatException, NonProjectablePotentialException {

        TablePotential projectedPotential = gaussianPotential.tableProject(new EvidenceCase(), null);

        Assertions.assertEquals(288, projectedPotential.tableSize);
        // mean 0, variance 1
        Assertions.assertEquals(0.6914, projectedPotential.getValues()[0], 10E-4);
        Assertions.assertEquals(0.2417, projectedPotential.getValues()[1], 10E-4);
        Assertions.assertEquals(0.0668, projectedPotential.getValues()[2], 10E-4);
        // mean 1, variance 1
        Assertions.assertEquals(0.3085, projectedPotential.getValues()[3], 10E-4);
        Assertions.assertEquals(0.3829, projectedPotential.getValues()[4], 10E-4);
        Assertions.assertEquals(0.3085, projectedPotential.getValues()[5], 10E-4);
        // mean 2, variance 1
        Assertions.assertEquals(0.0668, projectedPotential.getValues()[6], 10E-4);
        Assertions.assertEquals(0.2417, projectedPotential.getValues()[7], 10E-4);
        Assertions.assertEquals(0.6914, projectedPotential.getValues()[8], 10E-4);
        // mean 0.1, variance 1.2
        Assertions.assertEquals(0.6425, projectedPotential.getValues()[9], 10E-4);
        Assertions.assertEquals(0.2569, projectedPotential.getValues()[10], 10E-4);
        Assertions.assertEquals(0.1006, projectedPotential.getValues()[11], 10E-4);
    }
    
    /**
     * The variance potential holds a variance, so the projection must discretize a normal
     * whose standard deviation is its square root.
     * <p>
     * Two parent configurations, both with mean 0, over a conditioned variable of three
     * states, whose default thresholds are 0.5 and 1.5:
     * <ul>
     * <li>variance 4, that is sigma = 2: Phi(0.25) = 0.598706 and Phi(0.75) = 0.773373;</li>
     * <li>variance 1, that is sigma = 1: Phi(0.50) = 0.691462 and Phi(1.50) = 0.933193.</li>
     * </ul>
     * The second configuration is the control: with a variance of 1 the variance and the
     * standard deviation coincide, so its numbers do not tell the two apart. The first one
     * does: taking the variance for a standard deviation would give Phi(0.125) = 0.549738
     * and Phi(0.375) = 0.646170 instead.
     */
    @Test public void tableProjectReadsTheVarianceAsAVarianceAndNotAsAStandardDeviation()
            throws NonProjectablePotentialException {
        Variable height = new Variable("Height", "low", "medium", "high");
        Variable sex = new Variable("Sex", "female", "male");
        ConditionalGaussianPotential potential = new ConditionalGaussianPotential(asList(height, sex),
                                                                                   PotentialRole.CONDITIONAL_PROBABILITY);
        List<Variable> parent = new ArrayList<>(List.of(sex));
        potential.setMean(new TablePotential(parent, PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0, 0}));
        potential.setVariance(new TablePotential(parent, PotentialRole.CONDITIONAL_PROBABILITY, new double[]{4, 1}));

        double[] values = potential.tableProject(new EvidenceCase(), null).getValues();

        // sex = female, variance 4, sigma 2
        Assertions.assertEquals(0.598706, values[0], 1E-6);
        Assertions.assertEquals(0.174666, values[1], 1E-6);
        Assertions.assertEquals(0.226627, values[2], 1E-6);
        // sex = male, variance 1, sigma 1
        Assertions.assertEquals(0.691462, values[3], 1E-6);
        Assertions.assertEquals(0.241730, values[4], 1E-6);
        Assertions.assertEquals(0.066807, values[5], 1E-6);
    }

    /**
     * A potential built without an explicit variance must be usable in every configuration.
     * The first one used to be left at variance 0, which is a distribution with all its mass
     * on a single point, so its conditioned variable took its first state with probability 1.
     */
    @Test public void theDefaultVarianceIsOneInEveryConfiguration() throws NonProjectablePotentialException {
        Variable height = new Variable("Height", "low", "medium", "high");
        Variable sex = new Variable("Sex", "female", "male");
        ConditionalGaussianPotential potential = new ConditionalGaussianPotential(asList(height, sex),
                                                                                   PotentialRole.CONDITIONAL_PROBABILITY);

        for (double variance : ((TablePotential) potential.getVariance()).getValues()) {
            Assertions.assertEquals(1.0, variance);
        }
    }

    @Test public void testTableProjectWithEvidence()
            throws org.openmarkov.core.exception.IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(predictedAudiometry, 2)); // on
        evidence.addFinding(new Finding(micAge, 2)); // >90 and <= 365
        
        TablePotential projectedPotential = gaussianPotential.tableProject(evidence, null);

        Assertions.assertEquals(24, projectedPotential.tableSize);
        // mean 1.6, variance 1.4, so sigma = 1.183216: Phi(-0.929644) = 0.176271, Phi(-0.084515) = 0.466323
        Assertions.assertEquals(0.1763, projectedPotential.getValues()[0], 10E-4);
        Assertions.assertEquals(0.2901, projectedPotential.getValues()[1], 10E-4);
        Assertions.assertEquals(0.5337, projectedPotential.getValues()[2], 10E-4);
        // mean 1.7, variance 1.6
        Assertions.assertEquals(0.1714, projectedPotential.getValues()[3], 10E-4);
        Assertions.assertEquals(0.2658, projectedPotential.getValues()[4], 10E-4);
        Assertions.assertEquals(0.5628, projectedPotential.getValues()[5], 10E-4);
        // mean 1.65, variance 1.5
        Assertions.assertEquals(0.1739, projectedPotential.getValues()[6], 10E-4);
        Assertions.assertEquals(0.2774, projectedPotential.getValues()[7], 10E-4);
        Assertions.assertEquals(0.5487, projectedPotential.getValues()[8], 10E-4);

    }
    
}
