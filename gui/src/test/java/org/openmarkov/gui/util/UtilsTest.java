/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;


/**
 * This class tests the NodePropertiesDialog class (not the visual
 * behaviour).
 *
 * @author jlgozalo
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UtilsTest {

	@BeforeEach public void setUp() {
	}

	/**
	 * test to verify that hasLimitBracketSymbols return true if a limit is
	 * found
	 */
	@Test public void testHasLimitBracketSymbols() {

		State[] states = new State[4];
		states[0] = new State("(0.7,1]");
		states[1] = new State("(0.4,0.7]");
		states[2] = new State("(0.15,0.4]");
		states[3] = new State("[0,0.15]");

		assertTrue(Util.hasLimitBracketSymbols(states));

	}

	/**
	 * test to verify that hasLimitBracketSymbols return true if a limit is
	 * found
	 */
	@Test public void testHasNoLimitBracketSymbols() {

		State[] states = new State[3];
		states[0] = new State("ausente");
		states[1] = new State("presente");
		states[2] = new State("desconocido");

		assertTrue(!Util.hasLimitBracketSymbols(states));

	}

}
