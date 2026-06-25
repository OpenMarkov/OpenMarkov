/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.factory.NetsFactory;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.openmarkov.core.model.network.factory.IDFactory.*;

public abstract class InferencePropagationTaskIDTest extends InferencePropagationTaskDecTest {
    
    protected ProbNet iD_DecisionTestProblemWithSV;
    
    /**
     * Test for diagnosis problem
     *
     * @throws NotEvaluableNetworkException
     * @throws IncompatibleEvidenceException
     */
    @Disabled("Old tests with an AssertionError")
    @Test public void testPosteriorProbsAndUtilsIDDecisionTestProblem()
            throws
            NonProjectablePotentialException, NotEvaluableNetworkException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        ProbNet network = buildIDDecideTest();
        
        List<Variable> variablesOfInterest = Arrays
                .asList(network.getVariable(decTestName), network.getVariable(therapyName),
                        network.getVariable(diseaseName), network.getVariable(testResultName),
                        network.getVariable(healthStateName), network.getVariable(therapyCostName),
                        network.getVariable(testCostName));
        
        EvidenceCase preResolutionEvidence = new EvidenceCase();
        
        //Without post-resolution evidence
        EvidenceCase postResolutionEvidence = new EvidenceCase();
        
        Propagation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network, variablesOfInterest, preResolutionEvidence,
                                                                            postResolutionEvidence);
        
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, network, 1.0, 0.1532, 0.8468, 0.1532, 0.14,
                                                            9.6312, -0.0383, -0.2);
        
        // Post-resolution evidence: therapy = no
        postResolutionEvidence = new EvidenceCase();
        postResolutionEvidence.addFinding(network, therapyName, "no");
        
        algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network, variablesOfInterest, preResolutionEvidence,
                                                                postResolutionEvidence);
        
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, network, 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
                                                            9.895843174303259, 0.0, -0.2);
        
        // Post-resolution evidence: result of test = negative
        postResolutionEvidence = new EvidenceCase();
        postResolutionEvidence.addFinding(network, NetsFactory.testResultName, "negative");
        
        
        algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network, variablesOfInterest, preResolutionEvidence,
                                                                postResolutionEvidence);
        
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, network, 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
                                                            9.895843174303259, 0.0, -0.2);
        
        // Post-resolution evidence: do test? = yes
        postResolutionEvidence = new EvidenceCase();
        postResolutionEvidence.addFinding(network, IDFactory.decTestName, "yes");
        
        
        algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network, variablesOfInterest, preResolutionEvidence,
                                                                postResolutionEvidence);
        
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, network, 1.0, 0.1532, 0.8468, 0.1532, 0.14,
                                                            9.6312, -0.0383, -0.2);
        
        // Post-resolution evidence: do test? = yes & result of test = positive
        postResolutionEvidence = new EvidenceCase();
        postResolutionEvidence.addFinding(network, IDFactory.decTestName, "yes");
        postResolutionEvidence.addFinding(network, NetsFactory.testResultName, "positive");
        
        
        algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network, variablesOfInterest, preResolutionEvidence,
                                                                postResolutionEvidence);
        
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, network, 1.0, 1.0, 0.0, 1.0, 0.8316, 8.1684,
                                                            -0.25, -0.2);
        
        postResolutionEvidence.addFinding(network, NetsFactory.diseaseName, "present");
        
        
    }
    
    protected void checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(Propagation algorithm, ProbNet diagram, double t,
                                                                       double y1, double y2, double d, double x, double uHealthState, double uCostOfTherapy, double uCostOfTest)
            throws NonProjectablePotentialException, NotEvaluableNetworkException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        
        Variable variableT = diagram.getVariable(decTestName);
        Variable variableD = diagram.getVariable(therapyName);
        Variable variableX = diagram.getVariable(diseaseName);
        Variable variableY = diagram.getVariable(testResultName);
        Variable variableU1 = diagram.getVariable(healthStateName);
        Variable variableU2 = diagram.getVariable(therapyCostName);
        Variable variableU3 = diagram.getVariable(testCostName);
        
        HashMap<Variable, TablePotential> aPosterioriProbabilities = algorithm.getPosteriorValues();
        
        checkProbabilityPotential(aPosterioriProbabilities, variableX, x);
        checkProbabilityPotential(aPosterioriProbabilities, variableY, y1, y2);
        checkProbabilityPotential(aPosterioriProbabilities, variableD, d);
        checkProbabilityPotential(aPosterioriProbabilities, variableT, t);
        checkUtilityPotential(aPosterioriProbabilities, variableU1, uHealthState);
        checkUtilityPotential(aPosterioriProbabilities, variableU2, uCostOfTherapy);
        checkUtilityPotential(aPosterioriProbabilities, variableU3, uCostOfTest);
        
        
    }
    
    //@Test
    //TODO Review the minor error in test
    //TODO: has this test sense here
    @Test public void testPreAndPostResolutionEvidenceIDDecisionTestProblem()
            throws IncompatibleEvidenceException, NonProjectablePotentialException {
        ProbNet diagram = iD_DecisionTestProblemWithSV;
        
        Propagation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram, null, null, null);
        
        //TODO Test combination of pre and post resolution findings.
        
        //Variable variableT = diagram.getVariable("T");
        //Variable variableD = diagram.getVariable("D");
        
        EvidenceCase preResolutionEvidence = new EvidenceCase();
        preResolutionEvidence.addFinding(diagram, diseaseName, "present");
        algorithm.setPreResolutionEvidence(preResolutionEvidence);
        
        // Test optimal policy
			
		/*	HashMap<Variable, TablePotential> optimalStrategy = variableElimination
					.getOptimizedPolicies();
			TablePotential policyT = optimalStrategy.get(variableT);
			TablePotential policyD = optimalStrategy.get(variableD);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(policyT, variableT, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(policyD, variableD, 2));

			// Test the a priori case
			HashMap<Variable, TablePotential> aPrioriProbabilities = variableElimination
					.getProbsAndUtilities();*/
        // Read the variables
		/*	variableX = diagram.getVariable("X");
			assertNotNull(variableX);
			variableY = diagram.getVariable("Y");
			assertNotNull(variableY);
			variableU1 = diagram.getVariable("U1");
			assertNotNull(variableU1);
			variableU2 = diagram.getVariable("U2");
			assertNotNull(variableU2);

			checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
			checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
					0.9084);
			checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
			checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
			checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
			checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);*/
        
        // Test the expected utilities of the policy
        // StrategyUtilities strategyUtilities =
        // variableElimination.getUtilityTables();
        // TablePotential policyUtilities =
        // strategyUtilities.getUtilities(D);
        
        // Test the optimal choice of the policy
        // double[] truePolicy = {1.0, 0.0, 0.0, 1.0};
        
        // assertTrue(areEquals(policy.getValues(),truePolicy));
        
    }
    
    /**
     * @param network
     * @return A Task for 'network'. If the network is not evaluable
     * with the algorithm then the test calling this method is skipped.
     */
    protected Propagation buildInferenceTaskAndSkipTestIfNotEvaluable(ProbNet network,
                                                                      List<Variable> variablesOfInterest, EvidenceCase preResolutionEvidence, EvidenceCase postResolutionEvidence) {
        
        //If the network is not evaluable then the test is skipped
        boolean isEvaluable = true;
        Propagation task = buildInferenceTask(network, variablesOfInterest, preResolutionEvidence, postResolutionEvidence);
        assumeTrue(isEvaluable);
        return task;
    }
    
}