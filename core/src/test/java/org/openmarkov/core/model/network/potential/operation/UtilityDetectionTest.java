/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The maximization decides the role of its resulting potential by asking whether any of the
 * potentials it multiplies is a utility. The body of that question was commented out when the
 * UTILITY role was retired from the enum, so it answered "no utilities here" for every list. A
 * utility potential is nowadays one measured under a decision criterion — the same question
 * {@code sumByCriterion} asks.
 *
 * @author Manuel Arias
 */
public class UtilityDetectionTest {

	private static TablePotential potentialOverOneVariable() {
		Variable a = new Variable("A", new State[] { new State("no"), new State("yes") });
		return new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY);
	}

	@Test
	public void aPotentialWithACriterionIsAUtility() {
		TablePotential utility = potentialOverOneVariable();
		utility.setCriterion(new Criterion("cost"));

		assertTrue(TablePotentialMaximization.isThereAUtilityPotential(List.of(utility)),
				"A potential measured under a decision criterion is a utility, and the check said no");
	}

	@Test
	public void aListOfPlainProbabilitiesHasNoUtility() {
		assertFalse(TablePotentialMaximization.isThereAUtilityPotential(List.of(potentialOverOneVariable())));
	}
}
