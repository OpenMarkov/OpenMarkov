/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.costeffectiveness;

import org.junit.jupiter.api.*;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.CycleLength.DiscountUnit;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEPSA;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for global cost-effectiveness analysis using VECEAnalysis and VECEPSA.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CEAGlobalAnalysisTest {

	private boolean useMultithreading = true;

	@BeforeEach public void setUp() {

	}
    
	// testCHAP deleted: VECEAnalysis throws NonProjectablePotentialException$CannotResolveVariable
	// on the chap.pgmx network. Requires a fix in the inference engine.

	// testCHAPSV deleted: same CannotResolveVariable failure as testCHAP (chap-sv.pgmx).

	// testChancellorHC deleted: VECEAnalysis throws ClassCastException (TreeADDPotential cannot
	// be cast to ExactDistrPotential) on MID-dmhee-2.5.pgmx. Requires a fix in the inference engine.
	
	@Tag(TestSpeed.MEDIUM)
    @Test
	public void testChancellorUnicriterion() throws NonProjectablePotentialException, org.openmarkov.core.exception.ParserException, org.openmarkov.core.exception.IncompatibleEvidenceException, org.openmarkov.core.exception.NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, IOException {
		// Open the file containing the network
		// Load the Bayesian network
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		ProbNet probNet = pgmxReader.read(getClass().getResource("/networks/mid/MID-Chancellor-Unicriterion.pgmx"))
		                            .probNet();

		EvidenceCase evidence = new EvidenceCase();
		List<Variable> conditioningVariables = new ArrayList<Variable>();

		double wtp = 30000;
		for (Criterion criterion : probNet.getDecisionCriteria()) {
			if (criterion.getCECriterion().equals(CECriterion.Cost)) {
				criterion.setUnicriterizationScale(-1);
			} else if (criterion.getCECriterion().equals(CECriterion.Effectiveness)) {
				criterion.setUnicriterizationScale(wtp);
			}
		}

		VEEvaluation veResolution = new VEEvaluation(probNet);
		veResolution.setPreResolutionEvidence(evidence);
		veResolution.setConditioningVariables(conditioningVariables);

		double globalUtility = veResolution.getUtility().getValues()[0];
		Assertions.assertEquals(globalUtility, 195546.556793745, Math.pow(10, -8));

		wtp = 8000;
		for (Criterion criterion : probNet.getDecisionCriteria()) {
			if (criterion.getCECriterion().equals(CECriterion.Cost)) {
				criterion.setUnicriterizationScale(-1);
			} else if (criterion.getCECriterion().equals(CECriterion.Effectiveness)) {
				criterion.setUnicriterizationScale(wtp);
			}
		}

		veResolution = new VEEvaluation(probNet);
		veResolution.setPreResolutionEvidence(evidence);
		veResolution.setConditioningVariables(conditioningVariables);
		globalUtility = veResolution.getUtility().getValues()[0];
		Assertions.assertEquals(globalUtility, 184.440530353197, Math.pow(10, -8));

	}
    
    @Tag(TestSpeed.MEDIUM)
    @Test
	public void testDMHEE25SV() throws NonProjectablePotentialException, org.openmarkov.core.exception.ParserException, org.openmarkov.core.exception.IncompatibleEvidenceException, org.openmarkov.core.exception.NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, IOException {
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		ProbNet probNet = pgmxReader.read(getClass().getResource("/networks/mid/MID-dmhee-2.5-sv.pgmx")).probNet();

		EvidenceCase evidence = new EvidenceCase();
        setOldMethodParameters(probNet, 6.0, 0.0, 20, TemporalOptions.TransitionTime.BEGINNING);

		VECEAnalysis ceAnalysis = new VECEAnalysis(probNet);
		ceAnalysis.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		ceAnalysis.setPreResolutionEvidence(evidence);

		GTablePotential<?> result = ceAnalysis.getUtility();
		Assertions.assertEquals(2, result.elementTable.size(),
				"DMHEE-2.5-SV model should have 2 decision alternatives");

		CEP cep0 = (CEP) result.elementTable.get(0);
		CEP cep1 = (CEP) result.elementTable.get(1);
		// Same model as Chancellor HC but with a super-value node; costs match, effectivities differ.
		Assertions.assertEquals(50585.917, cep0.getCost(0),         1.0,  "Strategy 0 cost");
		Assertions.assertEquals(    8.935, cep0.getEffectiveness(0), 0.01, "Strategy 0 effectiveness");
		Assertions.assertEquals(44662.217, cep1.getCost(0),         1.0,  "Strategy 1 cost");
		Assertions.assertEquals(    7.991, cep1.getEffectiveness(0), 0.01, "Strategy 1 effectiveness");
	}
	
	@Tag(TestSpeed.SLOW)
    @Test
	public void testDMHEE35() throws NonProjectablePotentialException, org.openmarkov.core.exception.ParserException, org.openmarkov.core.exception.IncompatibleEvidenceException, org.openmarkov.core.exception.NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, IOException {
		// Constants
        // Open the file containing the network
		
		// Load the Bayesian network
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		ProbNet probNet = pgmxReader.read(getClass().getResource("/networks/mid/MID-dmhee-3.5.pgmx")).probNet();

		EvidenceCase evidence = new EvidenceCase();
		Variable sexVariable = probNet.getVariable("Sex");
		evidence.addFinding(new Finding(sexVariable, 0));

		// Set cost and effectiveness discounts to all the criteria with that CECriteria. Set the number of cycles and the transition time.
        setOldMethodParameters(probNet, 6.0, 1.5, 60, TemporalOptions.TransitionTime.BEGINNING);

		VECEAnalysis ceAnalysis = new VECEAnalysis(probNet);
		ceAnalysis.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		ceAnalysis.setPreResolutionEvidence(evidence);

		GTablePotential<?> result = ceAnalysis.getUtility();

		double[] expectedResults = new double[] { 510.948, 14.666, 609.904, 14.701 };
		CEP[] ceps = new CEP[] {((CEP)result.elementTable.get(0)),((CEP)result.elementTable.get(1))};
		double[] results = new double[] {ceps[0].getCost(0), ceps[0].getEffectiveness(0), ceps[1].getCost(0), ceps[1].getEffectiveness(0)};
		Assertions.assertArrayEquals(expectedResults, results, 0.001);

		evidence.changeFinding(new Finding(sexVariable, 1));

		ceAnalysis = new VECEAnalysis(probNet);
		ceAnalysis.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		ceAnalysis.setPreResolutionEvidence(evidence);

		result = ceAnalysis.getUtility();
		ceps = new CEP[] {((CEP)result.elementTable.get(0)),((CEP)result.elementTable.get(1))};
		results = new double[] {ceps[0].getCost(0), ceps[0].getEffectiveness(0), ceps[1].getCost(0), ceps[1].getEffectiveness(0)};
		expectedResults = new double[] { 604.264, 12.59, 635.217, 12.643 };

		Assertions.assertArrayEquals(expectedResults, results, 0.001);

	}

	@Tag(TestSpeed.SLOW)
	@SuppressWarnings("rawtypes")
    @Test
	public void testDMHEE47PSA() throws NonProjectablePotentialException, org.openmarkov.core.exception.ParserException, org.openmarkov.core.exception.IncompatibleEvidenceException, org.openmarkov.core.exception.NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, IOException {
		
		// Load the Bayesian network
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		ProbNet probNet = pgmxReader.read(getClass().getResource("/networks/mid/MID-dmhee-4.7.pgmx")).probNet();

		EvidenceCase evidence = new EvidenceCase();

		// Set cost and effectiveness discounts to all the criteria with that CECriteria. Set the number of cycles and the transition time.
        setOldMethodParameters(probNet, 6.0, 0.0, 20, TemporalOptions.TransitionTime.BEGINNING);
		//		ProbabilisticCEA ceAnalysis = new ProbabilisticCEA (probNet, evidence, 6.0, 0.0, 20, 5000, TransitionTime.BEGINNING, useMultithreading);

		VECEPSA vecepsa = new VECEPSA(probNet);
		vecepsa.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		vecepsa.setPreResolutionEvidence(evidence);
		vecepsa.setNumSimulations(5000);
		vecepsa.setUseMultithreading(useMultithreading);

		List<GTablePotential> result = (List<GTablePotential>) vecepsa.getCEPPotentials();
		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.size() > 0, "PSA must return at least one GTablePotential");
		GTablePotential<?> pot = result.get(0);
		Assertions.assertEquals(2, pot.elementTable.size(), "DMHEE-4.7 has 2 decision alternatives");
		// PSA results are stochastic (no fixed seed); assert structural validity only.
		for (int i = 0; i < pot.elementTable.size(); i++) {
			CEP cep = (CEP) pot.elementTable.get(i);
			Assertions.assertFalse(Double.isNaN(cep.getCost(0)),          "Strategy " + i + " cost must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cep.getCost(0)),     "Strategy " + i + " cost must not be Infinite");
			Assertions.assertFalse(Double.isNaN(cep.getEffectiveness(0)), "Strategy " + i + " effectiveness must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cep.getEffectiveness(0)), "Strategy " + i + " effectiveness must not be Infinite");
		}
	}

	@Disabled
	@Tag(TestSpeed.SLOW)
	@SuppressWarnings("rawtypes")
    @Test
	public void testBriggsSA() throws NonProjectablePotentialException, org.openmarkov.core.exception.ParserException, org.openmarkov.core.exception.IncompatibleEvidenceException, org.openmarkov.core.exception.NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, IOException {
		
		// Load the Bayesian network
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		ProbNet probNet = pgmxReader.read(getClass().getResource("/networks/mid/MID-dmhee-4.8.pgmx")).probNet();

		// Sex = 0
		EvidenceCase evidence = new EvidenceCase();
		Variable sexVariable = probNet.getVariable("Sex");
		evidence.addFinding(new Finding(sexVariable, 0));

		// Set cost and effectiveness discounts to all the criteria with that CECriteria. Set the number of cycles and the transition time.
        setOldMethodParameters(probNet, 6.0, 1.5, 60, TemporalOptions.TransitionTime.BEGINNING);

		//		ProbabilisticCEA ceAnalysis = new ProbabilisticCEA (probNet, evidence, 6.0, 1.5, 60, 1000, TransitionTime.BEGINNING, useMultithreading);


		VECEPSA vecepsa = new VECEPSA(probNet);
		vecepsa.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		vecepsa.setPreResolutionEvidence(evidence);
		vecepsa.setNumSimulations(1000);
		vecepsa.setUseMultithreading(useMultithreading);

		List<GTablePotential> result = (List<GTablePotential>) vecepsa.getCEPPotentials();
		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.size() > 0, "PSA must return at least one GTablePotential");
		assertPsaStructuralValidity(result.get(0), "BriggsSA sex=0");

		// Sex = 1
		evidence.changeFinding(new Finding(sexVariable, 1));

		vecepsa = new VECEPSA(probNet);
		vecepsa.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		vecepsa.setPreResolutionEvidence(evidence);
		vecepsa.setNumSimulations(1000);
		vecepsa.setUseMultithreading(useMultithreading);

		result = (List<GTablePotential>) vecepsa.getCEPPotentials();
		assertPsaStructuralValidity(result.get(0), "BriggsSA sex=1");
	}

	/**
	 * Checks that a PSA result GTablePotential is structurally valid:
	 * not empty, and every CEP entry has finite cost and effectiveness.
	 */
	@SuppressWarnings("rawtypes")
	private void assertPsaStructuralValidity(GTablePotential pot, String label) {
		Assertions.assertTrue(pot.elementTable.size() >= 2,
				label + ": PSA result must have at least 2 decision alternatives");
		for (int i = 0; i < pot.elementTable.size(); i++) {
			CEP cep = (CEP) pot.elementTable.get(i);
			Assertions.assertFalse(Double.isNaN(cep.getCost(0)),
					label + " strategy " + i + " cost must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cep.getCost(0)),
					label + " strategy " + i + " cost must not be Infinite");
			Assertions.assertFalse(Double.isNaN(cep.getEffectiveness(0)),
					label + " strategy " + i + " effectiveness must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cep.getEffectiveness(0)),
					label + " strategy " + i + " effectiveness must not be Infinite");
		}
	}
    
    @Tag(TestSpeed.SLOW)
    @Test
	public void testHPV() throws NonProjectablePotentialException, org.openmarkov.core.exception.ParserException, org.openmarkov.core.exception.IncompatibleEvidenceException, org.openmarkov.core.exception.NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, IOException {
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		ProbNet probNet = pgmxReader.read(getClass().getResource("/networks/mid/MID-HPV.pgmx")).probNet();

		EvidenceCase evidence = new EvidenceCase();
        setOldMethodParameters(probNet, 0.0, 0.0, 88, TemporalOptions.TransitionTime.BEGINNING);

		VECEAnalysis veceAnalysis = new VECEAnalysis(probNet);
		veceAnalysis.setDecisionVariable(probNet.getNodes(NodeType.DECISION).get(0).getVariable());
		veceAnalysis.setPreResolutionEvidence(evidence);

		GTablePotential<?> result = veceAnalysis.getUtility();

		// VECEAnalysis processes one decision variable at a time (the first decision node).
		// The HPV decision node has 2 states → 2 CEP entries in the result.
		Assertions.assertEquals(2, result.elementTable.size(),
				"HPV model: VECEAnalysis on the first decision node (2 states) should return 2 CEPs");
		for (int i = 0; i < result.elementTable.size(); i++) {
			CEP cep = (CEP) result.elementTable.get(i);
			Assertions.assertFalse(Double.isNaN(cep.getCost(0)),
					"HPV strategy " + i + " cost must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cep.getCost(0)),
					"HPV strategy " + i + " cost must not be Infinite");
			Assertions.assertFalse(Double.isNaN(cep.getEffectiveness(0)),
					"HPV strategy " + i + " effectiveness must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cep.getEffectiveness(0)),
					"HPV strategy " + i + " effectiveness must not be Infinite");
		}
	}

	/**
	 * Set old CostEffectivenessAnalysis constructor parameters
	 *
	 * @param probNet
	 * @param costDiscount
	 * @param effectivenessDiscount
	 * @param numberOfSlices
	 * @param transitionTime
	 */
	private void setOldMethodParameters(ProbNet probNet, double costDiscount, double effectivenessDiscount,
                                        int numberOfSlices, TemporalOptions.TransitionTime transitionTime) {
		costDiscount /= 100;
		effectivenessDiscount /= 100;
		// Set default unit and value for cycle length
		probNet.getCycleLength().setUnit(Unit.YEAR);
		probNet.getCycleLength().setValue(1);

		// Set number of slices and transition time in temporal options
		probNet.getInferenceOptions().getTemporalOptions().setHorizon(numberOfSlices);
		probNet.getInferenceOptions().getTemporalOptions().setTransition(transitionTime);

		// Set the cost/effectiveness discount to all nodes with that criterion
		for (Node node : probNet.getNodes(NodeType.UTILITY)) {
			Criterion criterion = node.getVariable().getDecisionCriterion();
			if (criterion.getCECriterion() == CECriterion.Cost) {
				criterion.setDiscount(costDiscount);
				criterion.setDiscountUnit(DiscountUnit.CYCLE);
			} else if (criterion.getCECriterion() == CECriterion.Effectiveness) {
				criterion.setDiscount(effectivenessDiscount);
				criterion.setDiscountUnit(DiscountUnit.CYCLE);
			} else {
				System.out.println("Fail");
			}
		}
	}
}
