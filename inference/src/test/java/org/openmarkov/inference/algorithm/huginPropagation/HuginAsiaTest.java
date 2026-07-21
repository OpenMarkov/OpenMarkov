/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.huginPropagation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HuginPropagation using the Asia network built programmatically.
 * Verifies that Hugin produces the same results as VEPropagation.
 *
 * @author Manuel Arias
 */
public class HuginAsiaTest {

	private static final double TOLERANCE = 1E-4;

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaPriorsNoEvidence() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		ClusterPropagation propagation = new HuginPropagation(probNet);
		propagation.compilePriorPotentials();
		propagation.setPreResolutionEvidence(new EvidenceCase());

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		assertNotNull(posteriors);
		assertEquals(8, posteriors.size(), "Should return posteriors for all 8 variables");

		// Known priors
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
		ClusterPropagation propagation = new HuginPropagation(probNet);
		propagation.compilePriorPotentials();

		// Evidence: X-ray = yes (state 1)
		EvidenceCase evidence = new EvidenceCase();
		Variable xray = probNet.getVariable("X-ray");
		evidence.addFinding(new Finding(xray, 1));
		propagation.setPreResolutionEvidence(evidence);

		HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

		// With X-ray=yes, posterior probabilities should shift
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
	public void testProbabilitiesSumToOne() throws Exception {
		ProbNet probNet = TestNetworks.buildAsia();
		ClusterPropagation propagation = new HuginPropagation(probNet);
		propagation.compilePriorPotentials();
		propagation.setPreResolutionEvidence(new EvidenceCase());

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
	public void testAllStorageLevelsProduceSameResults() throws Exception {
		double[] expectedVisitToAsia = null;

		for (ClusterPropagation.StorageLevel level : ClusterPropagation.StorageLevel.values()) {
			ProbNet probNet = TestNetworks.buildAsia();
			ClusterPropagation propagation = new HuginPropagation(probNet);
			propagation.setStorageLevel(level);
			propagation.compilePriorPotentials();
			propagation.setPreResolutionEvidence(new EvidenceCase());

			HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();
			double[] values = posteriors.get(probNet.getVariable("VisitToAsia")).getValues();

			if (expectedVisitToAsia == null) {
				expectedVisitToAsia = values;
			} else {
				assertEquals(expectedVisitToAsia[0], values[0], TOLERANCE,
						"Storage level " + level + " should produce same result");
			}
		}
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
