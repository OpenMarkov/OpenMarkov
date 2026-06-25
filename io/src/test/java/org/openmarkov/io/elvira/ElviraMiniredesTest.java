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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author Manuel Arias
 * @vesion 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraMiniredesTest {

	// Attributes
	private static final String testFile = "peque.elv";
	// Constants
	private final double maxError = 1E-5;
	private ProbNet probNet;

	private Node nodeA;

	private Node nodeB;

	private Node nodeC;

	private Variable variableA;

	private Variable variableB;

	private Variable variableC;

	private List<Potential> potentialsWithA;

	private List<Potential> potentialsWithB;

	private List<Potential> potentialsWithC;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws org.openmarkov.core.exception.ParserException, IOException {
        ProbNetReader probNetReader = new ElviraParser();
        probNet = probNetReader.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
		nodeA = probNet.getNode("A");
		nodeB = probNet.getNode("B");
		nodeC = probNet.getNode("C");
		variableA = nodeA.getVariable();
		variableB = nodeB.getVariable();
		variableC = nodeC.getVariable();
		potentialsWithA = probNet.getPotentials(variableA);
		potentialsWithB = probNet.getPotentials(variableB);
		potentialsWithC = probNet.getPotentials(variableC);
	}

	@Test public void testNodes() {
		assertNotNull(nodeA);
		assertNotNull(nodeB);
		assertNotNull(nodeC);
	}

	@Test public void testLinks() {
		List<Node> childrenA = nodeA.getChildren();
		assertEquals(2, childrenA.size());
		assertTrue(childrenA.contains(nodeB));
		assertTrue(childrenA.contains(nodeC));
		List<Node> childrenB = nodeB.getChildren();
		assertEquals(1, childrenB.size());
		assertTrue(childrenB.contains(nodeC));
	}

	@Test public void testRelations() {
		assertEquals(3, potentialsWithA.size());
		assertEquals(2, potentialsWithB.size());
		assertEquals(1, potentialsWithC.size());
		potentialsWithA.removeAll(potentialsWithB); // gets only p(A)
		potentialsWithB.removeAll(potentialsWithC); // gets only p(B|A))
		// Test potentials
		// Potential P(A)
		TablePotential probA = (TablePotential) potentialsWithA.get(0);
		List<Variable> variablesA = probA.getVariables();
		assertEquals(1, variablesA.size());
		assertEquals(variableA, variablesA.get(0));
		// A probabilities
		assertEquals(2, probA.getValues().length);
		assertEquals(0.2, probA.getValues()[0], maxError);
		assertEquals(0.8, probA.getValues()[1], maxError);
		// Potential P(B|A)
		TablePotential probBA = (TablePotential) potentialsWithB.get(0);
		List<Variable> variablesBA = probBA.getVariables();
		assertEquals(2, variablesBA.size());
		assertEquals(variableB, variablesBA.get(0));
		assertEquals(variableA, variablesBA.get(1));
		// A probabilities
		assertEquals(4, probBA.getValues().length);
		assertEquals(0.7, probBA.getValues()[0], maxError);
		assertEquals(0.3, probBA.getValues()[1], maxError);
		assertEquals(0.9, probBA.getValues()[2], maxError);
		assertEquals(0.1, probBA.getValues()[3], maxError);
		// Potential P(C|B,A)
		TablePotential probCBA = (TablePotential) potentialsWithC.get(0);
		List<Variable> variablesCBA = probCBA.getVariables();
		assertEquals(3, variablesCBA.size());
		assertEquals(variableC, variablesCBA.get(0));
		assertEquals(variableA, variablesCBA.get(1));
		assertEquals(variableB, variablesCBA.get(2));
		// A probabilities
		assertEquals(8, probCBA.getValues().length);
		assertEquals(0.15, probCBA.getValues()[0], maxError);
		assertEquals(0.85, probCBA.getValues()[1], maxError);
		assertEquals(0.84, probCBA.getValues()[2], maxError);
		assertEquals(0.16, probCBA.getValues()[3], maxError);
		assertEquals(0.29, probCBA.getValues()[4], maxError);
		assertEquals(0.71, probCBA.getValues()[5], maxError);
		assertEquals(0.98, probCBA.getValues()[6], maxError);
		assertEquals(0.02, probCBA.getValues()[7], maxError);
	}

}
