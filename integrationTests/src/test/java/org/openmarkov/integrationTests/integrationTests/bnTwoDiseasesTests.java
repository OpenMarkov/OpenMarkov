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
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class bnTwoDiseasesTests {

	private final String networkName = "networks/bn/BN-two-diseases.pgmx";

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
    
    @Test
    public void vePropagationWithoutEvidence() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		VEPropagation vePropagation;
		EvidenceCase postResolutionEvidence = new EvidenceCase();
		List<Variable> variablesOfInterest = probNet.getVariables();
			vePropagation = new VEPropagation(probNet);
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(preResolutionEvidence);
			vePropagation.setPostResolutionEvidence(postResolutionEvidence);

			HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();

			for (Variable variable : variablesOfInterest) {
				double[] expectedValues = new double[0];
				switch (variable.getName()) {
				case "Virus A":
					expectedValues = new double[] { 0.98, 0.02 };
					break;
				case "Virus B":
					expectedValues = new double[] { 0.99, 0.01 };
					break;
				case "Vaccination":
					expectedValues = new double[] { 0.2, 0.8 };
					break;
				case "Disease 1":
					expectedValues = new double[] { 0.9732, 0.0268 };
					break;
				case "Disease 2":
					expectedValues = new double[] { 0.9820, 0.0180 };
					break;
				case "Symptom":
					expectedValues = new double[] { 0.9483, 0.0517 };
					break;
				case "Anomaly":
					expectedValues = new double[] { 0.9725, 0.0275 };
					break;
				case "X-ray":
					expectedValues = new double[] { 0.9586, 0.0414 };
					break;
				case "Ecography":
					expectedValues = new double[] { 0.9278, 0.0722 };
					break;
				default:
				case "Sign":
					expectedValues = new double[] { 0.9715, 0.0285 };
					break;
				}
				Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
			}
	}

	@Disabled
    @Test
    public void vePropagationWithPostResolutionEvidence1() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		VEPropagation vePropagation;
		EvidenceCase postResolutionEvidence = new EvidenceCase();
		List<Variable> variablesOfInterest = probNet.getVariables();
		// New Finding: Disease 1 = present
			Finding finding = new Finding(probNet.getVariable("Disease 1"), 1);
			postResolutionEvidence.addFinding(finding);
			vePropagation = new VEPropagation(probNet);
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(preResolutionEvidence);
			vePropagation.setPostResolutionEvidence(postResolutionEvidence);
			HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();

			for (Variable variable : variablesOfInterest) {
				double[] expectedValues = new double[0];
				switch (variable.getName()) {
				case "Virus A":
					expectedValues = new double[] { 0.3286, 0.6714 };
					break;
				case "Virus B":
					expectedValues = new double[] { 0.6640, 0.3360 };
					break;
				case "Vaccination":
					expectedValues = new double[] { 0.2, 0.8 };
					break;
				case "Disease 1":
					expectedValues = new double[] { 0, 1 };
					break;
				case "Disease 2":
					expectedValues = new double[] { 0.9820, 0.0180 };
					break;
				case "Symptom":
					expectedValues = new double[] { 0.0389, 0.9611 };
					break;
				case "Anomaly":
					expectedValues = new double[] { 0.9725, 0.0275 };
					break;
				case "X-ray":
					expectedValues = new double[] { 0.9586, 0.0414 };
					break;
				case "Ecography":
					expectedValues = new double[] { 0.9278, 0.0722 };
					break;
				default:
				case "Sign":
					expectedValues = new double[] { 0.3, 0.7 };
					break;
				}
				Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
			}
	}
	
	@Tag(TestSpeed.SLOW)
    @Test
    public void vePropagationWithPostResolutionEvidence2() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		VEPropagation vePropagation;
		EvidenceCase postResolutionEvidence = new EvidenceCase();
		List<Variable> variablesOfInterest = probNet.getVariables();
			// New Finding: Disease 1 = present
			Finding finding1 = new Finding(probNet.getVariable("Disease 1"), 1);
			postResolutionEvidence.addFinding(finding1);

			// New Finding: Disease 2 = present
			Finding finding2 = new Finding(probNet.getVariable("Disease 2"), 1);
			postResolutionEvidence.addFinding(finding2);

			// New Finding: X-ray = negative
			Finding finding3 = new Finding(probNet.getVariable("X-ray"), 0);
			postResolutionEvidence.addFinding(finding3);
			vePropagation = new VEPropagation(probNet);
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(preResolutionEvidence);
			vePropagation.setPostResolutionEvidence(postResolutionEvidence);
			HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();
			for (Variable variable : variablesOfInterest) {
				double[] expectedValues = new double[0];
				switch (variable.getName()) {
				case "Virus A":
					expectedValues = new double[] { 0.3286, 0.6714 };
					break;
				case "Virus B":
					expectedValues = new double[] { 0.6640, 0.3360 };
					break;
				case "Vaccination":
					expectedValues = new double[] { 0.5556, 0.4444 };
					break;
				case "Disease 1":
					expectedValues = new double[] { 0, 1 };
					break;
				case "Disease 2":
					expectedValues = new double[] { 0, 1 };
					break;
				case "Symptom":
					expectedValues = new double[] { 0.0028, 0.9972 };
					break;
				case "Anomaly":
					expectedValues = new double[] { 0.0909, 0.9091 };
					break;
				case "X-ray":
					expectedValues = new double[] { 1, 0 };
					break;
				case "Ecography":
					expectedValues = new double[] { 0.2136, 0.7864 };
					break;
				case "Sign":
					expectedValues = new double[] { 0.3, 0.7 };
					break;

				}
				Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
			}

	}

}
