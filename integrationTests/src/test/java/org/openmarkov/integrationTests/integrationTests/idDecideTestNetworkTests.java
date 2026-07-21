/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.OptimalPolicies;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEExpectedUtilityDecision;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEOptimalIntervention;

public abstract class idDecideTestNetworkTests extends IDNetworkTests {


	@Disabled
    @Test
    public void veResolutionTestWithoutEvidence() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		VEEvaluation veEvaluation;
			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
			TablePotential utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 9.3289, deltaEquals);
	}
    
    @Test
    public void veResolutionTestWithEvidences() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		EvidenceCase evidenceCase = new EvidenceCase();
		Variable disease = null;
		Variable doTest = null;
		VEEvaluation veEvaluation;
		Finding finding;
		Finding secondFinding;

		// First evidence - Finding -> Disease = absent
			disease = probNet.getVariable("Disease");
			finding = new Finding(disease, 0);
			evidenceCase.addFinding(finding);
			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(evidenceCase);
			TablePotential utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 10, deltaEquals);
			
		// Second evidence - Finding -> Disease = present
			evidenceCase = new EvidenceCase();
			disease = probNet.getVariable("Disease");
			// Set disease as present
			finding = new Finding(disease, 1);
			evidenceCase.addFinding(finding);
			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(evidenceCase);
			utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 7.25, deltaEquals);

		// Third evidence - Multiple findings -> Disease = present; Do test? = yes
			evidenceCase = new EvidenceCase();
			disease = probNet.getVariable("Disease");
			doTest = probNet.getVariable("Do test?");

			// Set Disease = present
			finding = new Finding(disease, 1);
			evidenceCase.addFinding(finding);

			// Set Do Test? = yes
			secondFinding = new Finding(doTest, 1);
			evidenceCase.addFinding(secondFinding);

			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(evidenceCase);
			utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 7.05, deltaEquals);
	}
    
    @Test
    public void veOptimalPolicyTest() throws NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ConstraintViolatedException {
		OptimalPolicies veOptimalPolicy;
			Variable decisionVariable = probNet.getVariable("Therapy");
			veOptimalPolicy = new VEEvaluation(probNet);
			TablePotential optimalPolicy = (TablePotential) veOptimalPolicy.getOptimalPolicy(decisionVariable);
			double[] expectedValues = { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1 };
			Assertions.assertArrayEquals(optimalPolicy.getValues(), expectedValues, deltaEquals);
	}
    
    @Test
    public void veExpectedUtilityTest() throws NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ConstraintViolatedException {
		VEExpectedUtilityDecision veExpectedUtilityDecision;
			Variable decisionVariable = probNet.getVariable("Therapy");
			veExpectedUtilityDecision = new VEExpectedUtilityDecision(probNet, decisionVariable);
			TablePotential expectedUtility = veExpectedUtilityDecision.getExpectedUtility();
			double[] expectedValues = { 9.16, 8.11, -0.2, -0.95, 0.0, -0.75, 9.7107227, 8.03512, 0.0, -0.75, 4.810443,
					7.2184073 };
			Assertions.assertArrayEquals(expectedUtility.getValues(), expectedValues, deltaEquals);
	}

	@Disabled
    @Test
    public void veOptimalIntervention() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		VEOptimalIntervention veOptimalIntervention;
			veOptimalIntervention = new VEOptimalIntervention(probNet, preResolutionEvidence);
			StrategyTree optimalStrategyTree = veOptimalIntervention.getOptimalIntervention();

			Variable doTestVariable = probNet.getVariable("Do test?");
			Assertions.assertTrue(optimalStrategyTree.getRootVariable().equals(doTestVariable));
			Assertions.assertTrue(veOptimalIntervention.getOptimalIntervention().getBranches().size() == 1);

			TreeADDBranch branchDoTestYes = veOptimalIntervention.getOptimalIntervention().getBranches().get(0);
			Assertions.assertTrue(branchDoTestYes.getStates().get(0).getName().equals("yes"));

			StrategyTree subStrategyTree = (StrategyTree) branchDoTestYes.getPotential();
			Variable resultOfTestVariable = probNet.getVariable("Result of test");
			Assertions.assertTrue(subStrategyTree.getRootVariable().equals(resultOfTestVariable));
			Assertions.assertTrue(subStrategyTree.getBranches().size() == 2);

			Assertions.assertTrue(subStrategyTree.getBranches().get(0).getStates().get(0).getName().equals("negative"));
			StrategyTree potBranch0 = (StrategyTree) subStrategyTree.getBranches().get(0).getPotential();
			Assertions.assertTrue(potBranch0.getRootVariable().getName().equals("Therapy"));
			Assertions.assertTrue(potBranch0.getBranches().get(0).getStates().get(0).getName().equals("no"));

			Assertions.assertTrue(subStrategyTree.getBranches().get(1).getStates().get(0).getName().equals("positive"));
			StrategyTree potBranch1 = (StrategyTree) subStrategyTree.getBranches().get(1).getPotential();
			Assertions.assertTrue(potBranch1.getRootVariable().getName().equals("Therapy"));
			Assertions.assertTrue(potBranch1.getBranches().get(0).getStates().get(0).getName().equals("yes"));
	}

	

}
