/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation;
import org.openmarkov.inference.algorithm.huginPropagation.HuginPropagation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-validation tests: verifies that VEPropagation and HuginPropagation
 * produce the same posterior probabilities on the same networks.
 *
 * @author Manuel Arias
 */
public class CrossValidationTest {

	private static final double TOLERANCE = 1E-4;

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaNoEvidenceVEvsHugin() throws Exception {
		HashMap<Variable, TablePotential> vePosteriors = runVE(TestNetworks.buildAsia(), null);
		HashMap<Variable, TablePotential> huginPosteriors = runHugin(TestNetworks.buildAsia(), null);

		assertSamePosteriors(vePosteriors, huginPosteriors, TestNetworks.buildAsia());
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaWithEvidenceVEvsHugin() throws Exception {
		// Evidence: Dyspnea = yes (state 1)
		ProbNet veNet = TestNetworks.buildAsia();
		EvidenceCase veEvidence = new EvidenceCase();
		veEvidence.addFinding(new Finding(veNet.getVariable("Dyspnea"), 1));

		ProbNet huginNet = TestNetworks.buildAsia();
		EvidenceCase huginEvidence = new EvidenceCase();
		huginEvidence.addFinding(new Finding(huginNet.getVariable("Dyspnea"), 1));

		HashMap<Variable, TablePotential> vePosteriors = runVE(veNet, veEvidence);
		HashMap<Variable, TablePotential> huginPosteriors = runHugin(huginNet, huginEvidence);

		assertSamePosteriors(vePosteriors, huginPosteriors, TestNetworks.buildAsia());
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testChainNoEvidenceVEvsHugin() throws Exception {
		HashMap<Variable, TablePotential> vePosteriors = runVE(TestNetworks.buildChain3(), null);
		HashMap<Variable, TablePotential> huginPosteriors = runHugin(TestNetworks.buildChain3(), null);

		assertSamePosteriors(vePosteriors, huginPosteriors, TestNetworks.buildChain3());
	}

	private HashMap<Variable, TablePotential> runVE(ProbNet probNet, EvidenceCase evidence) throws Exception {
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());
		if (evidence != null) {
			propagation.setPostResolutionEvidence(evidence);
		}
		return propagation.getPosteriorValues();
	}

	private HashMap<Variable, TablePotential> runHugin(ProbNet probNet, EvidenceCase evidence) throws Exception {
		ClusterPropagation propagation = new HuginPropagation(probNet);
		propagation.compilePriorPotentials();
		propagation.setPreResolutionEvidence(evidence != null ? evidence : new EvidenceCase());
		return propagation.getPosteriorValues();
	}

	private void assertSamePosteriors(HashMap<Variable, TablePotential> vePosteriors,
			HashMap<Variable, TablePotential> huginPosteriors, ProbNet referenceNet) {
		for (Variable variable : referenceNet.getVariables()) {
			String name = variable.getName();

			// Find matching variables by name (different ProbNet instances)
			TablePotential vePot = findByName(vePosteriors, name);
			TablePotential huginPot = findByName(huginPosteriors, name);

			assertNotNull(vePot, "VE should have posterior for " + name);
			assertNotNull(huginPot, "Hugin should have posterior for " + name);

			double[] veValues = vePot.getValues();
			double[] huginValues = huginPot.getValues();
			assertEquals(veValues.length, huginValues.length,
					"Posterior sizes should match for " + name);

			for (int i = 0; i < veValues.length; i++) {
				assertEquals(veValues[i], huginValues[i], TOLERANCE,
						"VE and Hugin should agree on P(" + name + "=" + i + ")");
			}
		}
	}

	private TablePotential findByName(HashMap<Variable, TablePotential> posteriors, String name) {
		for (var entry : posteriors.entrySet()) {
			if (entry.getKey().getName().equals(name)) {
				return entry.getValue();
			}
		}
		return null;
	}
}
