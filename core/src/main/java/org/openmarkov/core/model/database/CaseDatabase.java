/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.database;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The CaseDatabase class represents a database of cases for a set of variables.
 * Each case is a specific instance or sample of the variables, and the database
 * consists of multiple such cases.
 * <p>
 * <strong>Shape.</strong> There is one column per variable, in the order of {@link #getVariables()},
 * and one row per case; the number in a cell is the index of the state that the variable of that
 * column takes in that case. Every row has exactly as many columns as there are variables, and the
 * constructor refuses anything else.
 * <p>
 * <strong>What can be changed.</strong> A database is <em>not</em> a snapshot: the variable of a
 * column can be replaced with {@link #setVariable} and a single cell written with {@link #setState},
 * which is what adapting a database to a network needs. Everything else is read-only —
 * {@link #getVariables()} hands out a list that cannot be modified — with one exception kept for
 * speed: {@link #getCases()} returns the live matrix, so what is read from it must not be written
 * to. {@link #getState} and {@link #getCase} are the safe way of reading a single value or a row.
 */
public class CaseDatabase {
	private final List<Variable> variables;

	/**
	 * Cases in the database. First argument: case number. Second argument: variable.
	 * The content is the state of the variable.
	 */
	private final int[][] cases;

	/**
	 * Constructor for CaseDatabase.
	 *
	 * @param variables List of variables
	 * @param cases     bidimensional array with the cases; it is copied, so what the caller does with
	 *                  its own array afterwards does not reach the database
	 *
	 * @throws IllegalArgumentException if a row has a number of columns other than the number of
	 *                                  variables, which would leave the database with no defined shape
	 */
	public CaseDatabase(List<Variable> variables, int[][] cases) {
		Objects.requireNonNull(variables, "The list of variables of a case database cannot be null");
		Objects.requireNonNull(cases, "The cases of a case database cannot be null; use new int[0][] for none");
		this.variables = new ArrayList<>(variables);
		this.cases = new int[cases.length][];
		for (int i = 0; i < cases.length; ++i) {
			Objects.requireNonNull(cases[i], "The case " + i + " of a case database cannot be null");
			if (cases[i].length != this.variables.size()) {
				throw new IllegalArgumentException("The case " + i + " has " + cases[i].length
						+ " values, but the database has " + this.variables.size() + " variables");
			}
			this.cases[i] = cases[i].clone();
		}
	}

	public CaseDatabase(CaseDatabase database) {
		Objects.requireNonNull(database, "The case database to copy cannot be null");
		this.variables = new ArrayList<>(database.variables);
		int[][] casesToCopy = database.cases;
		// Cloning row by row, instead of over a matrix built with variables.size() columns: that
		// assumed every row had exactly that many values, and a shorter one aborted the copy with
		// ArrayIndexOutOfBoundsException while a longer one lost its extra values in silence.
		this.cases = new int[casesToCopy.length][];
		for (int i = 0; i < casesToCopy.length; ++i) {
			this.cases[i] = casesToCopy[i].clone();
		}
	}

	/**
	 * Returns the cases.
	 * <p>
	 * <strong>This is the live matrix, not a copy</strong> — writing into it changes the database.
	 * It is handed out as it is because a database can hold thousands of cases and this is read in
	 * loops; use {@link #getState} or {@link #getCase} to read a single value or row safely, and
	 * {@link #setState} to change one.
	 *
	 * @return the cases.
	 */
	public int[][] getCases() {
		return cases;
	}

	/**
	 * Returns the variables, in the order of the columns.
	 *
	 * @return the variables. The list cannot be modified: adding or removing one would leave the
	 * database with more or fewer variables than its matrix has columns. Use {@link #setVariable} to
	 * replace the variable of a column.
	 */
	public List<Variable> getVariables() {
		return Collections.unmodifiableList(variables);
	}

	/**
	 * Replaces the variable of a column, leaving the cases untouched.
	 * <p>
	 * It is what adapting a database to a network needs: the same variable, read from the data and
	 * read from the network, are two different objects, and the database has to end up holding the
	 * one of the network. Whoever replaces a variable is responsible for the state indexes of that
	 * column still meaning what they should — see {@link #setState}.
	 *
	 * @param variableIndex the column
	 * @param variable      the variable that column stands for from now on
	 */
	public void setVariable(int variableIndex, Variable variable) {
		Objects.requireNonNull(variable, "The variable of a column cannot be null");
		variables.set(variableIndex, variable);
	}

	/**
	 * Writes the state that the variable of a column takes in a case.
	 *
	 * @param caseIndex     the row
	 * @param variableIndex the column
	 * @param stateIndex    index of the state, within the states of that variable
	 */
	public void setState(int caseIndex, int variableIndex, int stateIndex) {
		cases[caseIndex][variableIndex] = stateIndex;
	}

	/**
	 * @param caseIndex     the row
	 * @param variableIndex the column
	 *
	 * @return the index of the state that the variable of that column takes in that case.
	 */
	public int getState(int caseIndex, int variableIndex) {
		return cases[caseIndex][variableIndex];
	}

	/**
	 * @param caseIndex the row
	 *
	 * @return a copy of that case: one state index per variable, in the order of
	 * {@link #getVariables()}.
	 */
	public int[] getCase(int caseIndex) {
		return cases[caseIndex].clone();
	}

	/**
	 * Returns the variable given the name
	 *
	 * @param name name of the variable
	 *
	 * @return the variable of that name, or {@code null} if the database has no variable so called.
	 */
	public @Nullable Variable getVariable(String name) {
		for (Variable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	/**
	 * Returns the cases for a given variable.
	 * <p>
	 * The variable is looked for <strong>by identity</strong>, because {@link Variable} does not
	 * define equality by value: it has to be the very object the database holds. A variable of the
	 * same name read from somewhere else — a network, another database — does not match, and the
	 * answer is then {@code null}. {@link #getCases(String)} looks it up by name instead.
	 *
	 * @param variable Variable
	 *
	 * @return the cases for a given variable, or {@code null} if that very variable is not one of the
	 * variables of the database.
	 */
	public int @Nullable [] getCases(Variable variable) {
		return getCasesOfColumn(variables.indexOf(variable));
	}

	/**
	 * Returns the cases for the variable of a given name, which is how {@link #getVariable} looks a
	 * variable up as well.
	 *
	 * @param name name of the variable
	 *
	 * @return the cases for that variable, or {@code null} if the database has no variable so called.
	 */
	public int @Nullable [] getCases(String name) {
		Variable variable = getVariable(name);
		return variable == null ? null : getCasesOfColumn(variables.indexOf(variable));
	}

	private int @Nullable [] getCasesOfColumn(int variableIndex) {
		if (variableIndex == -1) {
			return null;
		}
		int[] casesOfVariable = new int[cases.length];
		for (int i = 0; i < cases.length; ++i) {
			casesOfVariable[i] = cases[i][variableIndex];
		}
		return casesOfVariable;
	}

	/**
	 * Returns the number of cases
	 *
	 * @return number of cases
	 */
	public int getNumCases() {
		return cases.length;
	}

}
