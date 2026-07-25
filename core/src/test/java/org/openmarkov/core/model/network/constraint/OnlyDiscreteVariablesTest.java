/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.core.VariableTypeConstraintEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

/**
 * «Only discrete» is the constraint a user switches on in the properties of a network. It used to
 * reject the very variables it is meant to allow, so there is one test per kind of variable, and one
 * for the contradiction between switching it on and checking it afterwards.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyDiscreteVariablesTest {

	private ProbNet probNet;
	private OnlyDiscreteVariables onlyDiscrete;

	@BeforeEach public void setUp() {
		probNet = new ProbNet();
		onlyDiscrete = new OnlyDiscreteVariables();
		probNet.addConstraint(onlyDiscrete);
	}

	/**
	 * The plain discrete variable, the one {@code new Variable(name, numberOfStates)} builds, is of type
	 * FINITE_STATES. The check asked for DISCRETIZED — the type of a <em>continuous</em> variable cut
	 * into intervals — so every ordinary discrete variable was reported as a violation.
	 */
	@Test public void aFiniteStatesVariableIsDiscrete() {
		probNet.addNode(new Variable("A", 2), NodeType.CHANCE);

		Assertions.assertTrue(onlyDiscrete.isMetBy(probNet));
	}

	/** A continuous variable already cut into intervals takes a finite set of values as well. */
	@Test public void aDiscretizedVariableIsDiscreteToo() {
		Variable discretized = new Variable("A", 2);
		discretized.setVariableType(VariableType.DISCRETIZED);
		probNet.addNode(discretized, NodeType.CHANCE);

		Assertions.assertTrue(onlyDiscrete.isMetBy(probNet));
	}

	@Test public void aNumericChanceVariableIsNotDiscrete() {
		probNet.addNode(new Variable("A"), NodeType.CHANCE);   // this constructor makes it NUMERIC

		Assertions.assertFalse(onlyDiscrete.isMetBy(probNet));
	}

	/**
	 * The variable of a utility node is a number by construction — VariableType allows it nothing else —
	 * so it is left alone, as OnlyFiniteStatesVariables already does. Otherwise this constraint could
	 * never be satisfied by an influence diagram.
	 */
	@Test public void theNumericVariableOfAUtilityNodeDoesNotBreakTheConstraint() {
		probNet.addNode(new Variable("A", 2), NodeType.CHANCE);
		probNet.addNode(new Variable("U"), NodeType.UTILITY);

		Assertions.assertTrue(onlyDiscrete.isMetBy(probNet));
	}

	/**
	 * The contradiction that made the feature unusable: the edit that switches the constraint on used to
	 * demand FINITE_STATES, and the constraint itself demanded DISCRETIZED. So it could only be switched
	 * on over a network of finite-states variables, and the moment it was on it reported every one of
	 * them as a violation. Switching it on and checking it must agree.
	 */
	@Test public void switchingTheConstraintOnAndCheckingItAgree() throws DoEditException {
		ProbNet net = new ProbNet();
		net.addNode(new Variable("A", 2), NodeType.CHANCE);
		net.addNode(new Variable("B", 3), NodeType.CHANCE);

		new VariableTypeConstraintEdit(net, new OnlyDiscreteVariables()).executeEdit();

		Assertions.assertTrue(new OnlyDiscreteVariables().isMetBy(net));
	}

	/** And it must refuse to be switched on over a network that does have a continuous variable. */
	@Test public void itCannotBeSwitchedOnOverAContinuousVariable() {
		ProbNet net = new ProbNet();
		net.addNode(new Variable("A"), NodeType.CHANCE);

		Assertions.assertThrows(DoEditException.class,
							    () -> new VariableTypeConstraintEdit(net, new OnlyDiscreteVariables()).executeEdit());
	}
}
