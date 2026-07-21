/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import org.openmarkov.core.model.network.State;


/**
 * This class tests the NodeDiscretizeValuesTablePanel class (not the visual
 * behaviour).
 *
 * @author jlgozalo
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NodeDomainValuesTablePanelTest {

	NodeDomainValuesTablePanel panel = null;

	@BeforeEach public void setUp() {
		panel = new NodeDomainValuesTablePanel(false);

	}

	/**
	 * test to verify the getter and setter methods (non visual elements)
	 */
	@Test public void testGetterAndSetters() {

	}

	/**
	 * test to verify the convertStringsToTableFormat method
	 */
	@Test public void testConvertStringsToTableFormat() {

		State[] stateValues = new State[4];
		stateValues[0] = new State("(0.7,1]");
		stateValues[1] = new State("nameTest(0.4,0.7]");
		stateValues[2] = new State("name Test(0.15,0.4]");
		stateValues[3] = new State("name TEST 1 [0,0.15]");

		Object[][] result = panel.convertStringsToTableFormat(stateValues);

		//assertEquals(result.length,20);
		assertEquals("", result[0][0]);
		assertEquals("(", result[0][1]);
		assertEquals(0.7, result[0][2]);
		assertEquals(",", result[0][3]);
		assertEquals(1.0, result[0][4]);
		assertEquals("nameTest", result[1][0]);
		assertEquals("]", result[0][5]);
		assertEquals("(", result[1][1]);
		assertEquals(0.4, result[1][2]);
		assertEquals(",", result[1][3]);
		assertEquals(0.7, result[1][4]);
		assertEquals("]", result[1][5]);
		assertEquals("name Test", result[2][0]);
		assertEquals("(", result[2][1]);
		assertEquals(0.15, result[2][2]);
		assertEquals(",", result[2][3]);
		assertEquals(0.4, result[2][4]);
		assertEquals("]", result[2][5]);
		assertEquals("name TEST 1 ", result[3][0]);
		assertEquals("[", result[3][1]);
		assertEquals(0.0, result[3][2]);
		assertEquals(",", result[3][3]);
		assertEquals(0.15, result[3][4]);
		assertEquals("]", result[3][5]);

	}

}
