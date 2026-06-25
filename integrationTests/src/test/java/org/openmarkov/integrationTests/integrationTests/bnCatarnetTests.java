/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.*;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class bnCatarnetTests {
	private final String networkName = "networks/bn/BN-catarnet.pgmx";

	// Delta parameter for Assertions.Equals methods
	private final double deltaEquals = Math.pow(10, -4);

	private ProbNet probNet;
	private EvidenceCase preResolutionEvidence;

	@BeforeEach public void setUp() throws java.net.URISyntaxException, org.openmarkov.core.exception.ParserException, FileNotFoundException {
		
		
		// Load the network: ID-decide-test
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		PGMXReader.NetworkAndEvidence probNetInfo = null;
		probNetInfo = pgmxReader.read(getClass().getClassLoader().getResource(networkName));
		this.probNet = probNetInfo.probNet();
		if (probNetInfo.evidence().size() != 0) {
			this.preResolutionEvidence = probNetInfo.evidence().get(0);
		}
	}
	
	@Tag(TestSpeed.MEDIUM)
    @Test
	public void vePropagationWithoutEvidence() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		VEPropagation vePropagation;
		EvidenceCase postResolutionEvidence = new EvidenceCase();
		List<Variable> variablesOfInterest = new ArrayList<>();
			variablesOfInterest.add(probNet.getVariable("ganancia_av"));
			variablesOfInterest.add(probNet.getVariable("deslu_global_post"));
			variablesOfInterest.add(probNet.getVariable("deslu_pre_no_catar"));
			vePropagation = new VEPropagation(probNet);
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(preResolutionEvidence);
			vePropagation.setPostResolutionEvidence(postResolutionEvidence);
			HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();

			for (Variable variable : variablesOfInterest) {
				double[] expectedValues = new double[0];
				switch (variable.getName()) {
				case "ganancia_av":
					expectedValues = new double[] { 0.0010, 0.0053, 0.0095, 0.3974, 0.4059, 0.1435, 0.0374 };
					break;
				case "deslu_global_post":
					expectedValues = new double[] { 0.7735, 0.0344, 0.0202, 0.1071, 0.0647 };
					break;
				case "deslu_pre_no_catar":
					expectedValues = new double[] { 0.9190, 0.0810 };
					break;
				}
				Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
			}
	}
	
	@Tag(TestSpeed.MEDIUM)
    @Test
	public void vePropagationWithPostResolutionEvidence2() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		VEPropagation vePropagation;
		EvidenceCase postResolutionEvidence = new EvidenceCase();
		List<Variable> variablesOfInterest = new ArrayList<>();
			variablesOfInterest.add(probNet.getVariable("ganancia_deslu"));
			variablesOfInterest.add(probNet.getVariable("deslu_global_post"));
			variablesOfInterest.add(probNet.getVariable("ruptura_caps_post"));

			// New Finding: ganancia_av = g3
			Finding finding1 = new Finding(probNet.getVariable("ganancia_av"), 6);
			postResolutionEvidence.addFinding(finding1);

			// New Finding: deslu_global_pre = ojo contral
			Finding finding2 = new Finding(probNet.getVariable("deslu_global_pre"), 3);
			postResolutionEvidence.addFinding(finding2);
			vePropagation = new VEPropagation(probNet);
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(preResolutionEvidence);
			vePropagation.setPostResolutionEvidence(postResolutionEvidence);
			HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();

			for (Variable variable : variablesOfInterest) {
				double[] expectedValues = new double[0];
				switch (variable.getName()) {
				case "ganancia_deslu":
					expectedValues = new double[] { 0.0009, 0.7776, 0.2215 };
					break;
				case "deslu_global_post":
					expectedValues = new double[] { 0.0231, 0.1460, 0.0546, 0.7741, 0.0022 };
					break;
				case "ruptura_caps_post":
					expectedValues = new double[] { 0.8508, 0.1492 };
					break;
				}
				Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
			}
	}
	@Disabled
    @Test
	public void vePropagationIncompatibleEvidence() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		VEPropagation vePropagation;
		EvidenceCase postResolutionEvidence = new EvidenceCase();
		List<Variable> variablesOfInterest = probNet.getVariables();
			// New Finding: ganancia_av = g3
			Finding finding1 = new Finding(probNet.getVariable("ganancia_av"), 6);
			postResolutionEvidence.addFinding(finding1);

			// New Finding: deslu_global_pre = ojo contral
			Finding finding2 = new Finding(probNet.getVariable("deslu_global_pre"), 3);
			postResolutionEvidence.addFinding(finding2);

			// New Finding: av_pre = (0.7, 1] --> Incompatible evidence!
			Finding finding3 = new Finding(probNet.getVariable("av_pre"), 3);
			postResolutionEvidence.addFinding(finding3);

		boolean incompatibleEvidenceExceptionOcurred = false;
			vePropagation = new VEPropagation(probNet);
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(preResolutionEvidence);
			vePropagation.setPostResolutionEvidence(postResolutionEvidence);
			vePropagation.getPosteriorValues();
	}

}
