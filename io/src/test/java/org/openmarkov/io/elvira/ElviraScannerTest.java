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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Manuel Arias
 * @vesion 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraScannerTest {

	// Attributes
	private static final String testFile = "catarnet1.elv";
	// Constants
	private final double maxError = 1E-5;
	private ElviraScanner elviraScanner;
	private ElviraToken token;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws FileNotFoundException {
			elviraScanner = ElviraScanner.getUniqueInstance();
			URL url = this.getClass().getClassLoader().getResource(testFile);
		elviraScanner.initializeScanner(url.getFile(), new FileInputStream(url.getFile()));

	}
	
	@Test public void testGetNextToken() throws ProbNetParserException, java.io.IOException {
		// bnet token
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.BNET, token.getReservedWord());
		assertTrue(token.getStringValue1().contentEquals(""));

		// ProbNet attributes
		// kindofgraph
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.KIND_OF_GRAPH, token.getReservedWord());
		assertTrue(token.getStringValue1().contentEquals("mixed"));
		// visualprecision
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.VISUALPRECISION, token.getReservedWord());
		assertEquals(0.0, token.getDoubleValue(), maxError);
		// version
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.VERSION, token.getReservedWord());
		assertEquals(1.0, token.getDoubleValue(), maxError);
		// default node states
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.DEFAULT, token.getReservedWord());
		String[] defaultStates = token.getStringListValue();
		assertEquals(2, defaultStates.length);
		assertTrue(defaultStates[0].contentEquals("presente"));
		assertTrue(defaultStates[1].contentEquals("ausente"));

		// NODE
		// ----
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.NODE, token.getReservedWord());
		assertEquals("av_sin_catar", token.getStringValue1());
		// Node type
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.FINITE_STATES, token.getReservedWord());
		// comment
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.COMMENT, token.getReservedWord());
		assertEquals(token.getStringValue1(), "Disminución agudeza por causas distintas de la catarata");
		// kind-of-node = ...
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.KIND_OF_NODE, token.getReservedWord());
		// ... chance
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.CHANCE, token.getReservedWord());
		// type-of-variable = ...
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.TYPE_OF_VARIABLE, token.getReservedWord());
		// ... finite-states
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.FINITE_STATES, token.getReservedWord());
		// pos_x
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.POSX, token.getReservedWord());
		assertEquals(723, token.getIntegerValue());
		// pos_y
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.POSY, token.getReservedWord());
		assertEquals(157, token.getIntegerValue());
		// relevance
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.RELEVANCE, token.getReservedWord());
		assertEquals(9.0, token.getDoubleValue(), maxError);
		// purpose
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.PURPOSE, token.getReservedWord());
		assertTrue(token.getStringValue1().contentEquals(""));
		// num-states
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.NUM_STATES, token.getReservedWord());
		assertEquals(4, token.getIntegerValue());
		// states
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.STATES, token.getReservedWord());
		String[] states = token.getStringListValue();
		assertEquals(4, states.length);
		assertTrue(states[0].contentEquals("(0.7,1]"));
		assertTrue(states[1].contentEquals("(0.4,0.7]"));
		assertTrue(states[2].contentEquals("(0.15,0.4]"));
		assertTrue(states[3].contentEquals("[0,0.15]"));
		// rightb (})
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.RIGHTCB, token.getReservedWord());

		// LINKS
		do {
			token = elviraScanner.getNextToken();
		} while (ReservedWord.LINK != token.getReservedWord());
		assertTrue(token.getStringValue1().contentEquals("alter_incision"));
		assertTrue(token.getStringValue2().contentEquals("despr_coroideo"));

		// RELATIONS
		// Relation name
		do {
			token = elviraScanner.getNextToken();
		} while (ReservedWord.RELATION != token.getReservedWord());
		String[] stringListValue = token.getStringListValue();
		assertNotNull(stringListValue);
		assertEquals(1, stringListValue.length);
		assertTrue(stringListValue[0].contentEquals("camara_estrecha"));
		// commnent
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.COMMENT, token.getReservedWord());
		assertTrue(token.getStringValue1().contentEquals(""));
		// kind of relation
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.KIND_OF_RELATION, token.getReservedWord());
		// deterministic
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.DETERMINISTIC, token.getReservedWord());
		assertFalse(token.getBooleanValue());
		// values
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.VALUES, token.getReservedWord());
		// table
		token = elviraScanner.getNextToken();
		assertEquals(TokenType.RESERVED, token.getTokenType());
		assertEquals(ReservedWord.TABLE, token.getReservedWord());
		double[] table = token.getDoublesTableValue();
		assertNotNull(table);
		assertEquals(2, table.length);
		assertEquals(0.02, table[0], maxError);
		assertEquals(0.98, table[1], maxError);
	}

}
