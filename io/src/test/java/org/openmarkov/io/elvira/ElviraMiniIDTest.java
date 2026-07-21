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
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Manuel Arias
 * @version 1.1 carmenyago changed to include the node variable inside the utility potential
 * After reading an influence diagram coded in Elvira format,
 * tests the allocation and type of Decision and Utility variables and the allocation of variables in utility potential
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraMiniIDTest {

	// Attributes
	private static final String testFile = "trivial3jensen.elv";

	private ProbNet probNet;

	private Node nodeA;

	private Node nodeD1;

	private Node nodeD2;

	private Variable variableA;

	private Variable variableD1;

	private Variable variableD2;

	private Variable variableU;

	private List<Potential> utilityPotentials;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws ProbNetParserException, IOException {
		ProbNetReader probNetReader = new ElviraParser();
        probNet = probNetReader.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
		nodeA = probNet.getNode("A");
		nodeD1 = probNet.getNode("D1");
		nodeD2 = probNet.getNode("D2");
		variableA = nodeA.getVariable();
		variableD1 = nodeD1.getVariable();
		variableD2 = nodeD2.getVariable();
		variableU = probNet.getVariable("U");
		utilityPotentials = probNet.getUtilityPotentials(variableD2);
	}

	@Test public void testDecisionAndUtilityVariables() {
		assertNotNull(variableD1);
		assertNotNull(variableD2);
		assertNotNull(variableU);

		assertEquals(3, variableD1.getNumStates());
		assertEquals(2, variableD2.getNumStates());

		assertEquals(VariableType.FINITE_STATES, variableD1.getVariableType());
		assertEquals(VariableType.FINITE_STATES, variableD2.getVariableType());
		assertEquals(VariableType.NUMERIC, variableU.getVariableType());
	}

	@Test public void testUtilityPotential() {
		assertEquals(1, utilityPotentials.size());
		TablePotential utilityPotential = (TablePotential) utilityPotentials.get(0);
		List<Variable> utilityVariables = utilityPotential.getVariables();
		//CMI
		/* Test fails because it does not consider that now the utility variable is in the utility potential */
		/*
		assertEquals(2, utilityVariables.size());
		assertEquals(variableA, utilityVariables.get(0));
		assertEquals(variableD2, utilityVariables.get(1));
        */
		assertEquals(3, utilityVariables.size());
		assertEquals(variableU, utilityVariables.get(0));
		assertEquals(variableA, utilityVariables.get(1));
		assertEquals(variableD2, utilityVariables.get(2));
		//CMF
		assertTrue(utilityPotential.getVariable(0).getDecisionCriterion() != null
				|| utilityPotential.getCriterion() != null);
	}

}