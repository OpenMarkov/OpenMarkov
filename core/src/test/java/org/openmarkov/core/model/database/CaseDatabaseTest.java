/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * @author Manuel Arias
 */
class CaseDatabaseTest {

	private static final Variable RAIN = new Variable("Rain", 2);
	private static final Variable WET = new Variable("Wet", 2);

	private static CaseDatabase database() {
		return new CaseDatabase(List.of(CaseDatabaseTest.RAIN, CaseDatabaseTest.WET),
								new int[][] { { 0, 0 }, { 1, 1 }, { 0, 1 } });
	}

	// -----------------------------------------------------------------------
	// Shape and construction
	// -----------------------------------------------------------------------

	@Test final void aDatabaseKnowsItsCasesAndVariables() {
		CaseDatabase database = CaseDatabaseTest.database();

		Assertions.assertEquals(3, database.getNumCases());
		Assertions.assertEquals(List.of(CaseDatabaseTest.RAIN, CaseDatabaseTest.WET), database.getVariables());
		Assertions.assertEquals(1, database.getState(1, 0));
	}

	/** B1: what the caller does with its own array afterwards must not reach the database. */
	@Test final void theCasesGivenToTheConstructorAreCopied() {
		int[][] cases = { { 0, 0 }, { 1, 1 } };
		CaseDatabase database = new CaseDatabase(List.of(CaseDatabaseTest.RAIN, CaseDatabaseTest.WET), cases);

		cases[0][0] = 9;

		Assertions.assertEquals(0, database.getState(0, 0));
	}

	/**
	 * B3/B4: a row with a number of values other than the number of variables leaves the database with
	 * no defined shape. It used to be accepted, and only blew up later, when copying it: a short row
	 * threw ArrayIndexOutOfBoundsException and a long one lost its extra values in silence.
	 */
	@Test final void aCaseWithTheWrongNumberOfValuesIsRefused() {
		List<Variable> twoVariables = List.of(CaseDatabaseTest.RAIN, CaseDatabaseTest.WET);

		Assertions.assertThrows(IllegalArgumentException.class,
							    () -> new CaseDatabase(twoVariables, new int[][] { { 0 } }));
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> new CaseDatabase(twoVariables, new int[][] { { 0, 0, 0 } }));
	}

	@Test final void nothingEssentialMayBeNull() {
		Assertions.assertThrows(NullPointerException.class, () -> new CaseDatabase(null, new int[0][]));
		Assertions.assertThrows(NullPointerException.class, () -> new CaseDatabase(List.of(), null));
		Assertions.assertThrows(NullPointerException.class,
								() -> new CaseDatabase(List.of(CaseDatabaseTest.RAIN), new int[][] { null }));
	}

	@Test final void aDatabaseWithNoCasesIsFine() {
		CaseDatabase empty = new CaseDatabase(List.of(CaseDatabaseTest.RAIN), new int[0][]);

		Assertions.assertEquals(0, empty.getNumCases());
		Assertions.assertEquals(0, empty.getCases(CaseDatabaseTest.RAIN).length);
	}

	/** The copy must be independent of the original, in both directions. */
	@Test final void theCopyOfADatabaseSharesNothingWithIt() {
		CaseDatabase original = CaseDatabaseTest.database();
		CaseDatabase copy = new CaseDatabase(original);

		original.setState(0, 0, 7);
		copy.setState(1, 1, 8);

		Assertions.assertEquals(0, copy.getState(0, 0));
		Assertions.assertEquals(1, original.getState(1, 1));
	}

	// -----------------------------------------------------------------------
	// Looking variables up
	// -----------------------------------------------------------------------

	@Test final void aVariableIsFoundByItsName() {
		CaseDatabase database = CaseDatabaseTest.database();

		Assertions.assertSame(CaseDatabaseTest.RAIN, database.getVariable("Rain"));
		Assertions.assertNull(database.getVariable("NoSuchVariable"));
	}

	/**
	 * B2: Variable does not define equality by value, so looking a column up by variable means looking
	 * it up by identity. A variable of the same name read from somewhere else does not match — which is
	 * why getCases(String) exists.
	 */
	@Test final void aColumnIsFoundByIdentityByVariableAndByNameByName() {
		CaseDatabase database = CaseDatabaseTest.database();
		Variable anotherRain = new Variable("Rain", 2);

		Assertions.assertArrayEquals(new int[] { 0, 1, 0 }, database.getCases(CaseDatabaseTest.RAIN));
		Assertions.assertNull(database.getCases(anotherRain));
		Assertions.assertArrayEquals(new int[] { 0, 1, 0 }, database.getCases("Rain"));
		Assertions.assertNull(database.getCases("NoSuchVariable"));
	}

	// -----------------------------------------------------------------------
	// What can be changed, and what cannot
	// -----------------------------------------------------------------------

	/** B1: adding or removing a variable would leave more columns than variables, so it is refused. */
	@Test final void theListOfVariablesCannotBeModifiedThroughTheGetter() {
		List<Variable> variables = CaseDatabaseTest.database().getVariables();

		Assertions.assertThrows(UnsupportedOperationException.class, () -> variables.add(CaseDatabaseTest.RAIN));
		Assertions.assertThrows(UnsupportedOperationException.class, () -> variables.removeFirst());
	}

	/** Replacing the variable of a column is what adapting a database to a network needs. */
	@Test final void theVariableOfAColumnCanBeReplaced() {
		CaseDatabase database = CaseDatabaseTest.database();
		Variable rainOfTheNetwork = new Variable("Rain", 2);

		database.setVariable(0, rainOfTheNetwork);

		Assertions.assertSame(rainOfTheNetwork, database.getVariables().getFirst());
		Assertions.assertArrayEquals(new int[] { 0, 1, 0 }, database.getCases(rainOfTheNetwork));
		Assertions.assertNull(database.getCases(CaseDatabaseTest.RAIN));
	}

	@Test final void aCaseIsHandedOutAsACopy() {
		CaseDatabase database = CaseDatabaseTest.database();

		int[] firstCase = database.getCase(0);
		firstCase[0] = 5;

		Assertions.assertEquals(0, database.getState(0, 0));
	}
}
