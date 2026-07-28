/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Removing a variable from a linear combination must remove its covariate and its coefficient
 * — and only those. It used to rebuild the coefficients from an empty list, so the potential
 * came back with all its covariates and no coefficients at all, while its twin
 * {@code addVariable}, one screen above, appended both correctly.
 *
 * @author Manuel Arias
 */
public class LinearCombinationKeepsItsCoefficientsTest {

	@Test
	public void removingAVariableKeepsTheOtherCoefficients() {
		Variable y = new Variable("Y");
		Variable a = new Variable("A", new State[] { new State("no"), new State("yes") });
		Variable b = new Variable("B", new State[] { new State("no"), new State("yes") });
		List<Variable> variables = new ArrayList<>(List.of(y, a, b));

		LinearCombinationPotential original = new LinearCombinationPotential(variables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		original.setCovariates(new VariableExpression[] { VariableExpression.Common.CONSTANT,
				new VariableExpression(variables, "{A}"), new VariableExpression(variables, "{B}") });
		original.setCoefficients(new double[] { 1.5, 2.0, 3.0 });

		LinearCombinationPotential smaller = (LinearCombinationPotential) original.removeVariable(a);

		assertEquals(2, smaller.getCovariates().length, "The intercept and {B} must survive");
		assertArrayEquals(new double[] { 1.5, 3.0 }, smaller.getCoefficients(),
				"Each surviving covariate must keep its own coefficient");
	}
}
