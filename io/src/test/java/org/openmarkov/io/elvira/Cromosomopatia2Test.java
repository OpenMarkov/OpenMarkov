/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/**
 *
 */
package org.openmarkov.io.elvira;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class Cromosomopatia2Test {

	private final double epsilon = 1E-9;
	private ProbNet probNet;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws ProbNetParserException, IOException {
        ProbNetReader probNetReader = new ElviraParser();
		probNet = probNetReader.read(this.getClass().getClassLoader().getResource("RedCromosomopatia2.elv"))
                               .probNet();
	}

	@Test public void testCromosopatia() {
		// Variables exist
		Variable diagnostico = probNet.getVariable("Diagnostico");
		assertNotNull(diagnostico);
		Variable testFetal = probNet.getVariable("Test_fetal");
		assertNotNull(testFetal);
		Variable scr2tr = probNet.getVariable("Scr_2_tr");
		assertNotNull(scr2tr);
		Variable amniocentesis = probNet.getVariable("Amniocentesis");
		assertNotNull(amniocentesis);
		Variable ECO2tr = probNet.getVariable("ECO_2_tr");
		assertNotNull(ECO2tr);
		// States
		String[] diagnosticoStatesNames = { "Edwards", "Down", "Normal" };
		testStates(3, diagnostico, diagnosticoStatesNames);
		String[] testFetalStatesNames = { "positivo", "negativo" };
		testStates(2, testFetal, testFetalStatesNames);
		String[] scr2trStatesNames = { "positivo", "negativo" };
		testStates(2, testFetal, scr2trStatesNames);
		String[] testAmnioCentesis = { "positivo", "negativo" };
		testStates(2, amniocentesis, testAmnioCentesis);
		String[] ECO2trStatesNames = { "positivo", "negativo" };
		testStates(2, ECO2tr, ECO2trStatesNames);
		// Test number of links
		List<Node> diagnosticoNodes = probNet.getNode(diagnostico).getChildren();
		assertEquals(4, diagnosticoNodes.size());
		// Test potentials
		List<Potential> diagnosticoPotentials = probNet.getNode(diagnostico).getPotentials();
		assertEquals(1, diagnosticoPotentials.size());
		TablePotential diagnosticoPotential = (TablePotential) diagnosticoPotentials.get(0);
		assertEquals(3, diagnosticoPotential.getValues().length);
		assertEquals(0.9975, diagnosticoPotential.getValues()[0], epsilon);
	}

	/**
	 * Checks the number of states of a variable and its names
	 *
	 * @param numStates
	 * @param variable
	 * @param statesNames
	 */
	private void testStates(int numStates, Variable variable, String[] statesNames) {
		int numStatesVariable = variable.getNumStates();
		assertEquals(numStates, numStatesVariable);
		State[] variableStates = variable.getStates();
		for (int i = 0; i < numStatesVariable; i++) {
			// OpenMarkov reversed the names of states on Elvira
			assertTrue(statesNames[numStatesVariable - 1 - i].contentEquals(variableStates[i].getName()));
		}
	}

}
