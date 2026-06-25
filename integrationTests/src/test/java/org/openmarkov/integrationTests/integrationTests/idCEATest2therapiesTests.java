/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.inference.tasks.OptimalPolicies;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEOptimalIntervention;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.FileNotFoundException;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class idCEATest2therapiesTests {

	private final String networkName = "networks/id/ID-CEA-test-2therapies.pgmx";

	// Delta parameter for Assertions.Equals methods
	private final double deltaEquals = Math.pow(10, -4);

	private ProbNet probNet;
	private EvidenceCase preResolutionEvidence;
	
	@BeforeEach public void setUp() throws java.net.URISyntaxException, ProbNetParserException, FileNotFoundException {
		
		// Load the network: ID-decide-test
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		PGMXReader.NetworkAndEvidence probNetInfo = null;
		probNetInfo = pgmxReader.read(getClass().getClassLoader().getResource(networkName));
		this.probNet = probNetInfo.probNet();
		if (probNetInfo.evidence().size() != 0) {
			this.preResolutionEvidence = probNetInfo.evidence().get(0);
		}
	}
	@Disabled
    @Test
    public void veResolutionTestWithoutEvidence() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		VEEvaluation veEvaluation;
			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
			TablePotential utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 269569.4, deltaEquals);

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
			Assertions.assertEquals(utility.getValues()[0], 10 * 30000, deltaEquals);

		// Second evidence - Finding -> Disease = present
			evidenceCase = new EvidenceCase();
			disease = probNet.getVariable("Disease");
			// Set disease as present
			finding = new Finding(disease, 1);
			evidenceCase.addFinding(finding);
			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(evidenceCase);
			utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 125000, deltaEquals);

		// Third evidence - Multiple findings -> Disease = present; Do test? = yes
			evidenceCase = new EvidenceCase();
			disease = probNet.getVariable("Disease");
			doTest = probNet.getVariable("Dec:Test");

			// Set Disease = present
			finding = new Finding(disease, 1);
			evidenceCase.addFinding(finding);

			// Set Do Test? = yes
			secondFinding = new Finding(doTest, 1);
			evidenceCase.addFinding(secondFinding);

			veEvaluation = new VEEvaluation(probNet);
			veEvaluation.setPreResolutionEvidence(evidenceCase);
			utility = veEvaluation.getUtility();
			Assertions.assertEquals(utility.getValues()[0], 124850, deltaEquals);
	}

	@Disabled
    @Test
    public void veOptimalPolicyTest() throws NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ConstraintViolatedException {
		OptimalPolicies veOptimalPolicy;
			Variable decisionVariable = probNet.getVariable("Therapy");
			veOptimalPolicy = new VEEvaluation(probNet);
			TablePotential optimalPolicy = (TablePotential) veOptimalPolicy.getOptimalPolicy(decisionVariable);
			double[] expectedValues = { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0 };
			Assertions.assertArrayEquals(optimalPolicy.getValues(), expectedValues, deltaEquals);
	}
    
    @Test
    public void veOptimalIntervention() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		VEOptimalIntervention veOptimalIntervention;
			veOptimalIntervention = new VEOptimalIntervention(probNet, preResolutionEvidence);
			StrategyTree optimalStrategyTree = veOptimalIntervention.getOptimalIntervention();

			Variable doTestVariable = probNet.getVariable("Dec:Test");
			Assertions.assertTrue(optimalStrategyTree.getRootVariable().equals(doTestVariable));
			Assertions.assertTrue(veOptimalIntervention.getOptimalIntervention().getBranches().size() == 1);

			TreeADDBranch branchDoTestYes = veOptimalIntervention.getOptimalIntervention().getBranches().get(0);
			Assertions.assertTrue(branchDoTestYes.getStates().get(0).getName().equals("yes"));

			StrategyTree subStrategyTree = (StrategyTree) branchDoTestYes.getPotential();
			Variable resultOfTestVariable = probNet.getVariable("Test");
			Assertions.assertTrue(subStrategyTree.getRootVariable().equals(resultOfTestVariable));
			Assertions.assertTrue(subStrategyTree.getBranches().size() == 2);

			Assertions.assertTrue(subStrategyTree.getBranches().get(0).getStates().get(0).getName().equals("negative"));
			StrategyTree potBranch0 = (StrategyTree) subStrategyTree.getBranches().get(0).getPotential();
			Assertions.assertTrue(potBranch0.getRootVariable().getName().equals("Therapy"));
			Assertions.assertTrue(potBranch0.getBranches().get(0).getStates().get(0).getName().equals("no"));

			Assertions.assertTrue(subStrategyTree.getBranches().get(1).getStates().get(0).getName().equals("positive"));
			StrategyTree potBranch1 = (StrategyTree) subStrategyTree.getBranches().get(1).getPotential();
			Assertions.assertTrue(potBranch1.getRootVariable().getName().equals("Therapy"));
			Assertions.assertTrue(potBranch1.getBranches().get(0).getStates().get(0).getName().equals("therapy 1"));
	}
    
    @Test
    public void veCEAGlobalTests() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		CEAnalysis veceaGlobal;
			probNet.getInferenceOptions().getMultiCriteriaOptions()
					.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
			veceaGlobal = new VECEAnalysis(probNet);
			veceaGlobal.setPreResolutionEvidence(preResolutionEvidence);

			CEP cep = (CEP) (veceaGlobal.getUtility()).elementTable.get(0);
			Assertions.assertTrue(cep.getNumIntervals() == 3);

			// First interval
			Assertions.assertEquals(cep.getCost(11171.0), 0, deltaEquals);
			Assertions.assertEquals(cep.getEffectiveness(0.0), 8.768, deltaEquals);

			// Second interval
			Assertions.assertEquals(cep.getCost(11171.4), 3874, deltaEquals);
			Assertions.assertEquals(cep.getEffectiveness(33383.4), 9.11478, deltaEquals);

			// Third interval
			Assertions.assertEquals(cep.getCost(500000.0), 13184, deltaEquals);
			Assertions.assertEquals(cep.getEffectiveness(33383.6), 9.39366, deltaEquals);
	}
    
    @Test
    public void veCEADecisionDecTestTests() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		CEAnalysis veceaDecision;
		probNet.getInferenceOptions().getMultiCriteriaOptions()
				.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
			Variable decisionVariable = null;
			EvidenceCase evidenceCaseWithScenario = new EvidenceCase();
				decisionVariable = probNet.getVariable("Therapy");

				// Scenario
				Variable decTestVariable = probNet.getVariable("Dec:Test");
				Finding decTestYes = new Finding(decTestVariable, 1);
				evidenceCaseWithScenario.addFinding(decTestYes);

				Variable testVariable = probNet.getVariable("Test");
				Finding testNegative = new Finding(testVariable, 1);
				evidenceCaseWithScenario.addFinding(testNegative);

			veceaDecision = new VECEAnalysis(probNet);
			veceaDecision.setPreResolutionEvidence(evidenceCaseWithScenario);
			veceaDecision.setDecisionVariable(decisionVariable);
			GTablePotential cepPotential = veceaDecision.getUtility();
			// There are three therapies (no, therapy 1, therapy 2)
			Assertions.assertTrue(cepPotential.elementTable.size() == 3);

			// CEP -> no therapy
			CEP noTherapyCEP = (CEP) cepPotential.elementTable.get(0);
			Assertions.assertTrue(noTherapyCEP.getNumIntervals() == 1);
			Assertions.assertEquals(noTherapyCEP.getCost(11171.0), 150.0, deltaEquals);
			Assertions.assertEquals(noTherapyCEP.getEffectiveness(0.0), 9.8486, deltaEquals);

			// CEP -> therapy 1
			CEP therapyOneCEP = (CEP) cepPotential.elementTable.get(1);
			Assertions.assertTrue(therapyOneCEP.getNumIntervals() == 1);
			Assertions.assertEquals(therapyOneCEP.getCost(500000.0), 20150, deltaEquals);
			Assertions.assertEquals(therapyOneCEP.getEffectiveness(30000.0), 9.7985, deltaEquals);

			// CEP -> therapy 1
			CEP therapyTwoCEP = (CEP) cepPotential.elementTable.get(2);
			Assertions.assertTrue(therapyTwoCEP.getNumIntervals() == 1);
			Assertions.assertEquals(therapyTwoCEP.getCost(10.0), 70150, deltaEquals);
			Assertions.assertEquals(therapyTwoCEP.getEffectiveness(5.0), 9.2518, deltaEquals);
	}

}
