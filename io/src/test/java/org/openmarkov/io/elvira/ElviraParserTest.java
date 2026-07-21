/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Manuel Arias
 * @version 1.1 carmenyago fixed the way mechaVitreaPotential is located
 * After reading a Bayesian network coded in Elvira format, tests the allocation of the general info, the nodes, links and potentials in the ProbNet
 * Test two networks, the first with binary variables and the second with a variable with three states
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraParserTest {

	// Attributes
	private static final String testFile = "cataratas-escenarios-091123.elv";
	// Constants
    final double maxError = 1E-5;
	private ProbNet probNet;
	private ElviraParser elviraParser;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws ProbNetParserException, IOException {
		elviraParser = new ElviraParser();
        probNet = elviraParser.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
	}

	// Unit tests
	@Test public void getConstraints() {
		assertEquals(BayesianNetworkType.class, probNet.getNetworkType().getClass());
	}

	@Test public void getGeneralInfo() {
		String kindOfGraph = (String) probNet.getAdditionalProperties().get("KindOfGraph");
		assertNotNull(kindOfGraph);
		assertTrue(kindOfGraph.contentEquals("mixed"));
		Double version = Double.parseDouble(probNet.getAdditionalProperties().get("Version"));
		assertEquals(1.0, version.doubleValue(), maxError);
		State[] defaultNodeStates = probNet.getDefaultStates();
		assertNotNull(defaultNodeStates);
		assertEquals(2, defaultNodeStates.length);
		assertTrue(defaultNodeStates[0].getName().contentEquals("ausente"));
		assertTrue(defaultNodeStates[1].getName().contentEquals("presente"));
	}

	@Test public void getNodes() {
		assertEquals(65, probNet.getNumNodes()); // Test number of nodes
		// Test some attributes of one random node
		String nameOfVariable = "v_ganancia_deslu";
		Node node = probNet.getNode(nameOfVariable);
		assertNotNull(node);
		Variable variable = node.getVariable();
		assertEquals(7, variable.getNumStates());
		assertEquals(3, variable.getStateIndex("igual"));
		Integer posX = (int) node.getCoordinateX();
		assertNotNull(posX);
		assertEquals(429, posX.intValue());
		// Test variables
		List<Variable> variables = probNet.getVariables();
		assertEquals(65, variables.size());
	}

	@Test public void getLinks() {
		// Test children of one random node
		String nameOfVariable = "retinopatia_diabetic";
		Node node = probNet.getNode(nameOfVariable);
		List<Node> children = node.getChildren();
		assertEquals(6, children.size());
	}

	@Test public void getPotentials() {
		List<Potential> potentials = probNet.getPotentials();
		assertEquals(65, potentials.size());
		// Test a canonical potential
		// Gets variable mechaVitrea
		Variable mechaVitreaVariable = probNet.getVariable("mecha_vitrea");
		assertNotNull(mechaVitreaVariable);
		// Gets potentials corresponding to variable mechaVitrea
		List<Potential> mechaVitreaPotentials = probNet.getPotentials(mechaVitreaVariable);
		assertEquals(5, mechaVitreaPotentials.size());
		//CMI
		/*
		ICIPotential mechaVitreaPotential = 
			(ICIPotential) mechaVitreaPotentials.get(0);
		*/
		ICIPotential mechaVitreaPotential = (ICIPotential) probNet.getNode(mechaVitreaVariable).getPotentials().get(0);
		//CMF
		// Test sub-potentials
		Variable miopiaMagnaVariable = probNet.getVariable("miopia_magna");
		assertNotNull(miopiaMagnaVariable);
		// Test conversion to OpenMarkov potential
		// Test ordination of variables
		assertEquals(miopiaMagnaVariable, mechaVitreaPotential.getVariable(1));
		assertEquals(mechaVitreaVariable, mechaVitreaPotential.getVariable(0));
		// Test potential table
		assertEquals(1.0, mechaVitreaPotential.getNoisyParameters(miopiaMagnaVariable)[0], maxError);
		assertEquals(0.0, mechaVitreaPotential.getNoisyParameters(miopiaMagnaVariable)[1], maxError);
		assertEquals(0.95, mechaVitreaPotential.getNoisyParameters(miopiaMagnaVariable)[2], maxError);
		assertEquals(0.05, mechaVitreaPotential.getNoisyParameters(miopiaMagnaVariable)[3], maxError);
	}

	// Integration tests

	/**
	 * Load a probNet with a non binary variable.
	 *
	 * @throws Exception
	 */
	@Test public void loadANodeThreeState() throws ProbNetParserException, IOException {
		String testFile = "UnNodoTresEstados.elv";
		elviraParser = new ElviraParser();
		ProbNet unNodoTresEstados = elviraParser.read(this.getClass().getClassLoader().getResource(testFile))
                                                .probNet();
		Node uniqueNode = unNodoTresEstados.getNode("Tres_estados");
		Variable uniqueVariable = uniqueNode.getVariable();
		assertEquals(3, uniqueVariable.getNumStates());
	}

}
