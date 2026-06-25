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
import org.openmarkov.inference.algorithm.likelihoodWeighting.LikelihoodWeighting;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LogicSampling;
import org.openmarkov.inference.algorithm.likelihoodWeighting.StochasticPropagation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-validation between stochastic inference (LikelihoodWeighting, LogicSampling)
 * and exact variable elimination. Verifies that approximate posteriors converge
 * to exact values within a reasonable tolerance.
 *
 * @author Manuel Arias
 */
public class StochasticVsExactTest {

	private static final double STOCHASTIC_TOLERANCE = 3E-2;
	private static final int SAMPLE_SIZE = 50000;
	private static final long SEED = 10071856L;

	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testLikelihoodWeightingVsVEOnAsiaNoEvidence() throws Exception {
		ProbNet veNet = TestNetworks.buildAsia();
		HashMap<Variable, TablePotential> exact = runVE(veNet, null);

		ProbNet lwNet = TestNetworks.buildAsia();
		HashMap<Variable, TablePotential> approx = runLikelihoodWeighting(lwNet, null);

		assertPosteriorsClose(exact, approx, veNet, "LW no-evidence");
	}

	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testLogicSamplingVsVEOnAsiaNoEvidence() throws Exception {
		ProbNet veNet = TestNetworks.buildAsia();
		HashMap<Variable, TablePotential> exact = runVE(veNet, null);

		ProbNet lsNet = TestNetworks.buildAsia();
		HashMap<Variable, TablePotential> approx = runLogicSampling(lsNet, null);

		assertPosteriorsClose(exact, approx, veNet, "LS no-evidence");
	}

	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testLikelihoodWeightingVsVEOnAsiaWithEvidence() throws Exception {
		ProbNet veNet = TestNetworks.buildAsia();
		EvidenceCase veEvidence = new EvidenceCase();
		veEvidence.addFinding(new Finding(veNet.getVariable("X-ray"), 1));
		HashMap<Variable, TablePotential> exact = runVE(veNet, veEvidence);

		ProbNet lwNet = TestNetworks.buildAsia();
		EvidenceCase lwEvidence = new EvidenceCase();
		lwEvidence.addFinding(new Finding(lwNet.getVariable("X-ray"), 1));
		HashMap<Variable, TablePotential> approx = runLikelihoodWeighting(lwNet, lwEvidence);

		// Stochastic algorithms exclude evidence variables from posteriors
		assertPosteriorsClose(exact, approx, TestNetworks.buildAsia(), "LW X-ray evidence",
				"X-ray");
	}

	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testLikelihoodWeightingVsVEOnChain() throws Exception {
		ProbNet veNet = TestNetworks.buildChain3();
		HashMap<Variable, TablePotential> exact = runVE(veNet, null);

		ProbNet lwNet = TestNetworks.buildChain3();
		HashMap<Variable, TablePotential> approx = runLikelihoodWeighting(lwNet, null);

		assertPosteriorsClose(exact, approx, TestNetworks.buildChain3(), "LW chain no-evidence");
	}

	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testLikelihoodWeightingVsVEOnDiamond() throws Exception {
		ProbNet veNet = TestNetworks.buildDiamond();
		HashMap<Variable, TablePotential> exact = runVE(veNet, null);

		ProbNet lwNet = TestNetworks.buildDiamond();
		HashMap<Variable, TablePotential> approx = runLikelihoodWeighting(lwNet, null);

		assertPosteriorsClose(exact, approx, TestNetworks.buildDiamond(), "LW diamond no-evidence");
	}

	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testStochasticProbabilitiesSumToOne() throws Exception {
		ProbNet lwNet = TestNetworks.buildAsia();
		HashMap<Variable, TablePotential> approx = runLikelihoodWeighting(lwNet, null);

		for (var entry : approx.entrySet()) {
			double[] values = entry.getValue().getValues();
			double sum = 0;
			for (double v : values) {
				sum += v;
			}
			assertEquals(1.0, sum, 1E-6,
					"Stochastic posteriors of " + entry.getKey().getName() + " should sum to 1");
		}
	}

	private HashMap<Variable, TablePotential> runVE(ProbNet probNet, EvidenceCase evidence) throws Exception {
		VEPropagation propagation = new VEPropagation(probNet);
		propagation.setVariablesOfInterest(probNet.getVariables());
		if (evidence != null) {
			propagation.setPostResolutionEvidence(evidence);
		}
		return propagation.getPosteriorValues();
	}

	private HashMap<Variable, TablePotential> runLikelihoodWeighting(ProbNet probNet,
			EvidenceCase evidence) throws Exception {
		LikelihoodWeighting lw = new LikelihoodWeighting(probNet);
		configureStochastic(lw, evidence);
		return lw.getPosteriorValues();
	}

	private HashMap<Variable, TablePotential> runLogicSampling(ProbNet probNet,
			EvidenceCase evidence) throws Exception {
		LogicSampling ls = new LogicSampling(probNet);
		configureStochastic(ls, evidence);
		return ls.getPosteriorValues();
	}

	private void configureStochastic(StochasticPropagation algorithm,
			EvidenceCase evidence) throws Exception {
		algorithm.setSampleSize(SAMPLE_SIZE);
		algorithm.setSeed(SEED);
		if (evidence != null) {
			algorithm.setPreResolutionEvidence(evidence);
		} else {
			algorithm.setPostResolutionEvidence(new EvidenceCase());
		}
	}

	private void assertPosteriorsClose(HashMap<Variable, TablePotential> exact,
			HashMap<Variable, TablePotential> approx, ProbNet referenceNet, String context,
			String... excludedVarNames) {
		Set<String> excluded = Set.of(excludedVarNames);
		for (Variable variable : referenceNet.getVariables()) {
			String name = variable.getName();
			if (excluded.contains(name)) {
				continue;
			}
			TablePotential exactPot = findByName(exact, name);
			TablePotential approxPot = findByName(approx, name);

			assertNotNull(exactPot, context + ": VE should have posterior for " + name);
			assertNotNull(approxPot, context + ": Stochastic should have posterior for " + name);

			double[] exactValues = exactPot.getValues();
			double[] approxValues = approxPot.getValues();
			assertEquals(exactValues.length, approxValues.length,
					context + ": posterior sizes should match for " + name);

			for (int i = 0; i < exactValues.length; i++) {
				assertEquals(exactValues[i], approxValues[i], STOCHASTIC_TOLERANCE,
						context + ": P(" + name + "=" + i + ") exact=" + exactValues[i]
								+ " approx=" + approxValues[i]);
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
