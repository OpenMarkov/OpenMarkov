/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A choice is a value assignment to a decision variable. It is possible that
 * one variable can have more than one assignment in case of draw.
 */
public class Choice {

	// Attributes
	private final Variable variable;

	/**
	 * Value(s) assignment; if there is no draws only the first one.
     * <p>
	 * invariant value[i] != value[j] when i != j and i &lt; numValues
	 * and j &lt; numValues
	 * ({@code int[]}).
	 */
	private int[] values;

	/**
	 * Number of assignments to {@code Variable}.
	 */
	private int numValues;

	private boolean initialized = false;

	// Constructors

	/**
	 * @param variable {@code Variable}
	 * @param values   {@code int[]}. Most times only one value; in case of
	 *                 draws more than one value.
	 */
	public Choice(Variable variable, int[] values) {
		this.variable = variable;
		this.setValues(values);
		numValues = values.length;
	}

	/**
	 * @param variable {@code Variable}
	 * @param value    {@code int}. Only one value (no draws)
	 */
	public Choice(Variable variable, int value) {
		values = new int[1];
		values[0] = value;
		this.variable = variable;
		numValues = 1;
		initialized = true;
	}

	// Methods

	/**
	 * @return values {@code int[]}.
	 */
	public int[] getValues() {
		return values;
	}

	/**
	 * @param values {@code int[]}.
	 */
	public void setValues(int[] values) {
		this.values = values;
		numValues = values.length;
		initialized = true;
	}

	public List<State> getStates() {
		List<State> states = new ArrayList<>(numValues);
		State[] variableStates = variable.getStates();
		for (int i = 0; i < numValues; i++) {
			states.add(variableStates[values[i]]);
		}
		return states;
	}

	/**
	 * Used in case of draw.
	 *
	 * @param value {@code int}.
	 */
	public void addValue(int value) {
		int[] newValues = new int[numValues + 1];
        if (numValues >= 0) System.arraycopy(values, 0, newValues, 0, numValues);
		newValues[numValues++] = value;
		values = newValues;
	}

	/**
	 * @param value {@code int}.
	 */
	public void setValue(int value) {
		numValues = 1;
		values = new int[numValues];
		values[0] = value;
		initialized = true;
	}

	/**
	 * @return numValues {@code int}.
	 */
	public int getNumValues() {
		return numValues;
	}

	/**
	 * @return variable {@code Variable}.
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * @return A deep copy of this object. {@code Choice}
	 */
	public Choice copy() {
		int[] copyValues = new int[numValues];
        System.arraycopy(values, 0, copyValues, 0, numValues);
		return new Choice(variable, copyValues);
	}

	/**
	 * Overrides {@code toString} method. Mainly for test purposes.
	 *
	 * @return String
	 */
	public String toString() {
		if (numValues == 1) {
			return variable.getName() + "=" + variable.getStateName(values[0]);
		}
		return variable.getName() + "={" + IntStream.range(0, numValues)
													.mapToObj(i -> variable.getStateName(values[i]))
													.collect(Collectors.joining(",")) + "}";
	}

	/**
	 * Overrides {@code equals} method. Mainly for test purposes.
	 *
	 * @param object {@code Object}. {@code Object} must be of type {@code Choice}
	 * @return {@code true} if the object received has the same variable
	 * and the same option (or options set)
	 */
	public boolean sameInformation(Object object) {
		Choice choice = (Choice) object;
		if (choice.variable.getName().equals(this.variable.getName())) {
			if (choice.getNumValues() != numValues) {
				return false;
			}
			int[] otherValues = choice.getValues();
			for (int i = 0; i < numValues; i++) {
				if (values[i] != otherValues[i]) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

}
