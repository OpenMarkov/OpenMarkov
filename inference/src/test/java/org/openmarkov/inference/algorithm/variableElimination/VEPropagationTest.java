/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VEPropagation: posterior probability propagation
 * using variable elimination.
 *
 * @author Manuel Arias
 */
public class VEPropagationTest {

	private static final double TOLERANCE = 1E-4;

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaPriorsWithoutEvidence() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		assertNotNull(posteriors);
		assertEquals(8, posteriors.size(), "Should return posteriors for all 8 variables");

		// Known priors of the Asia network
		assertProbability(posteriors, probNet, "VisitToAsia", 0, 0.99);
		assertProbability(posteriors, probNet, "Smoker", 0, 0.50);
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaWithDyspneaEvidence() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		// Evidence: Dyspnea = yes (state 1)
		EvidenceCase evidence = new EvidenceCase();
		Variable dyspnea = probNet.getVariable("Dyspnea");
		evidence.addFinding(new Finding(dyspnea, 1));
		propagation.setPostResolutionEvidence(evidence);

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		// With Dyspnea=yes, the probability of Bronchitis=yes should increase
		TablePotential bronchitisPot = posteriors.get(probNet.getVariable("Bronchitis"));
		assertNotNull(bronchitisPot);
		double pBronchitisYes = bronchitisPot.getValues()[1];
		assertTrue(pBronchitisYes > 0.3,
				"P(Bronchitis=yes|Dyspnea=yes) should be > 0.3 (prior is 0.45)");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testChainPropagation() throws Exception {
		ProbNet probNet = TestNetworks.buildChain3();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		// P(A=a0) = 0.6
		assertProbability(posteriors, probNet, "A", 0, 0.6);

		// P(B=b0) = P(B=b0|A=a0)*P(A=a0) + P(B=b0|A=a1)*P(A=a1)
		//         = 0.8*0.6 + 0.3*0.4 = 0.48 + 0.12 = 0.60
		assertProbability(posteriors, probNet, "B", 0, 0.60);
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testChainWithEvidence() throws Exception {
		ProbNet probNet = TestNetworks.buildChain3();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		// Evidence: C = c1 (state 1)
		EvidenceCase evidence = new EvidenceCase();
		Variable varC = probNet.getVariable("C");
		evidence.addFinding(new Finding(varC, 1));
		propagation.setPostResolutionEvidence(evidence);

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		// P(C=c1|C=c1) = 1.0
		assertProbability(posteriors, probNet, "C", 1, 1.0);
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testProbabilitiesSumToOne() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		for (var entry : posteriors.entrySet()) {
			double[] values = entry.getValue().getValues();
			double sum = 0;
			for (double v : values) {
				sum += v;
			}
			assertEquals(1.0, sum, TOLERANCE,
					"Probabilities of " + entry.getKey().getName() + " should sum to 1");
		}
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaAllPriorsExact() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		assertProbability(posteriors, probNet, "VisitToAsia", 0, 0.99);
		assertProbability(posteriors, probNet, "Smoker", 0, 0.50);
		assertProbability(posteriors, probNet, "Tuberculosis", 0, 0.9896);
		assertProbability(posteriors, probNet, "LungCancer", 0, 0.945);
		assertProbability(posteriors, probNet, "Bronchitis", 0, 0.55);
		assertProbability(posteriors, probNet, "TuberculosisOrCancer", 0, 0.935172);
		assertProbability(posteriors, probNet, "X-ray", 0, 0.8897096);
		assertProbability(posteriors, probNet, "Dyspnea", 0, 0.5640294);
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaWithXRayEvidence() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		EvidenceCase evidence = new EvidenceCase();
		Variable xray = probNet.getVariable("X-ray");
		evidence.addFinding(new Finding(xray, 1));
		propagation.setPostResolutionEvidence(evidence);

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		// Known posteriors with X-ray=yes evidence
		assertProbability(posteriors, probNet, "VisitToAsia", 0, 0.9868);
		assertProbability(posteriors, probNet, "Smoker", 0, 0.3122);
		assertProbability(posteriors, probNet, "Tuberculosis", 0, 0.9076);
		assertProbability(posteriors, probNet, "LungCancer", 0, 0.5113);
		assertProbability(posteriors, probNet, "Bronchitis", 0, 0.4937);
		assertProbability(posteriors, probNet, "TuberculosisOrCancer", 0, 0.4240);
		assertProbability(posteriors, probNet, "Dyspnea", 0, 0.3592);
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaWithMultipleEvidence() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		// Evidence: Smoker=yes AND Dyspnea=yes
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(new Finding(probNet.getVariable("Smoker"), 1));
		evidence.addFinding(new Finding(probNet.getVariable("Dyspnea"), 1));
		propagation.setPostResolutionEvidence(evidence);

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		// Evidence variables should be deterministic
		assertProbability(posteriors, probNet, "Smoker", 1, 1.0);
		assertProbability(posteriors, probNet, "Dyspnea", 1, 1.0);

		// All remaining posteriors should still sum to 1
		for (var entry : posteriors.entrySet()) {
			double[] values = entry.getValue().getValues();
			double sum = 0;
			for (double v : values) sum += v;
			assertEquals(1.0, sum, TOLERANCE,
					"Probabilities of " + entry.getKey().getName() + " should sum to 1");
		}
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testDiamondPropagation() throws Exception {
		ProbNet probNet = TestNetworks.buildDiamond();
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		assertEquals(4, posteriors.size(), "Should return posteriors for all 4 variables");
		assertProbability(posteriors, probNet, "A", 0, 0.5);

		// P(B=b0) = P(B=b0|A=a0)*P(A=a0) + P(B=b0|A=a1)*P(A=a1)
		//         = 0.8*0.5 + 0.3*0.5 = 0.55
		assertProbability(posteriors, probNet, "B", 0, 0.55);

		// P(C=c0) = 0.6*0.5 + 0.1*0.5 = 0.35
		assertProbability(posteriors, probNet, "C", 0, 0.35);
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testSubsetOfVariablesOfInterest() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		VEPropagation propagation = new VEPropagation(probNet);

		// Only 2 variables of interest
		Variable smoker = probNet.getVariable("Smoker");
		Variable bronchitis = probNet.getVariable("Bronchitis");
		propagation.setVariablesOfInterest(List.of(smoker, bronchitis));

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		assertTrue(posteriors.containsKey(smoker));
		assertTrue(posteriors.containsKey(bronchitis));
	}

	private void assertProbability(HashMap<Variable, TablePotential> posteriors,
			ProbNet probNet, String varName, int stateIndex, double expected) {
		Variable variable = probNet.getVariable(varName);
		TablePotential potential = posteriors.get(variable);
		assertNotNull(potential, "Posterior should exist for " + varName);
		assertEquals(expected, potential.getValues()[stateIndex], TOLERANCE,
				"P(" + varName + "=" + stateIndex + ")");
	}
}
