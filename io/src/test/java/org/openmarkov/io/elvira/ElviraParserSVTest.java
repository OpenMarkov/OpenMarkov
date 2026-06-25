/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author Manuel Arias
 * @version 1.1 carmenyago changed the number for finding the TablePotential of U3 from 0 to 1 due to getPotentials put first the potential of the children
 * After reading an influence diagram with super value nodes coded in Elvira format, test general information, links and TablePotential
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraParserSVTest {

	// Attributes
	private static final String testFile = "ejemplo_Tatman_y_Shachter.elv";
	// Constants
	private final double maxError = 1E-5;
	private ProbNet probNet;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws org.openmarkov.core.exception.ParserException, IOException {
        ProbNetReader probNetReader = new ElviraParser();
        probNet = probNetReader.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
	}

	// Unit tests
	@Test public void getGeneralInfo() {
		//String kindOfGraph = (String)probNet.properties.get("KindOfGraph");
		//assertNotNull(kindOfGraph);
		//assertTrue(kindOfGraph.contentEquals("directed"));
		Double version = Double.parseDouble(probNet.getAdditionalProperties().get("Version"));
		assertEquals(1.0, version.doubleValue(), maxError);
		State[] defaultNodeStates = (State[]) probNet.getDefaultStates();
		assertNotNull(defaultNodeStates);
		assertEquals(2, defaultNodeStates.length);
		assertTrue(defaultNodeStates[0].getName().contentEquals("absent"));
		assertTrue(defaultNodeStates[1].getName().contentEquals("present"));
	}
	
	@Test public void getNodes() {
		assertEquals(8, probNet.getNumNodes()); // Test number of nodes
		// Test some attributes of one random node
		String nameOfVariable = "A";
		Node node = probNet.getNode(nameOfVariable);
		assertNotNull(node);
		Variable variable = node.getVariable();
		assertEquals(2, variable.getNumStates());
		assertEquals(0, variable.getStateIndex("absent"));
		Integer posX = (int) node.getCoordinateX();
		assertNotNull(posX);
		assertEquals(229, posX.intValue());
		// Test variables
		List<Variable> variables = probNet.getVariables();
		assertEquals(8, variables.size());
	}

	@Test public void getLinks() {
		// Test children of one random node
		String nameOfVariable = "A";
		Node node = probNet.getNode(nameOfVariable);
		List<Node> children = node.getChildren();
		assertEquals(3, children.size());
	}

	@Test public void getPotentials() {
		List<Potential> potentials = probNet.getPotentials();
		assertEquals(7, potentials.size());
		// Test a canonical potential
		// Gets variable Quality_of_life_U1
		Variable U1Variable = probNet.getVariable("U3");
		assertNotNull(U1Variable);
		// Gets potentials corresponding to variable Quality_of_life_U1
		List<Potential> U1Potentials = probNet.getPotentials(U1Variable);
		assertEquals(2, U1Potentials.size());
		TablePotential U1Potential = (TablePotential) U1Potentials.get(1);
		// Test sub-potentials
		/*ArrayList<TablePotential> U1SubPotentials = 
			U1Potential.getSubPotentials();*/
/*		for (TablePotential potential : U1SubPotentials) {
			ArrayList<Variable> variables = potential.getVariables();
				// Test conversion to OpenMarkov potential
				// Test ordination of variables
				assertEquals(U1Variable, variables.get(0));*/
		// Test potential table
		//System.out.println(U1Potential.table[1]);
		assertEquals(0.0, U1Potential.getValues()[0], maxError);
		assertEquals(1.0, U1Potential.getValues()[1], maxError);
		assertEquals(3.0, U1Potential.getValues()[2], maxError);
		assertEquals(2.0, U1Potential.getValues()[3], maxError);
		//			}
	}
}
