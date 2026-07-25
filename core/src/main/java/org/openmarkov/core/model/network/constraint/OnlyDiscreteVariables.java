/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

/**
 * Only discrete variables constraint: the one the user switches on with «Only discrete» in the
 * properties of a network.
 * <p>
 * It used to reject the very variables it is meant to allow. The check asked for
 * {@code != DISCRETIZED}, but {@code DISCRETIZED} is the type of a <em>continuous</em> variable
 * already cut into intervals; a plain discrete variable, the one {@code new Variable(name, states)}
 * builds, is {@code FINITE_STATES}. So switching the constraint on was impossible on a network of
 * ordinary discrete variables, and with it on such a variable could not be added.
 * <p>
 * Worse, the same question was asked in four places with three different answers: this class demanded
 * {@code DISCRETIZED}, so did {@code AddNodeEdit} and {@code VariableTypeEdit}, while
 * {@code VariableTypeConstraintEdit} — the edit that switches the constraint on — demanded
 * {@code FINITE_STATES}. The feature contradicted itself: it could only be switched on over a network
 * whose variables were all finite-states, and the moment it was on it reported every one of them as a
 * violation. All four now ask {@link #isDiscrete}.
 */
@Constraint(name = "OnlyDiscreteVariables", defaultBehavior = ConstraintBehavior.OPTIONAL) public class OnlyDiscreteVariables
		extends PNConstraint {

	@Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		for (Variable variable : probNet.getVariables()) {
			Node node = probNet.getNode(variable);
			if (!OnlyDiscreteVariables.isDiscrete(node.getNodeType(), variable.getVariableType())) {
				constraintChecker.addException(
						new ConstraintViolatedException.OnlyDiscreteVariablesAllowed(this, variable));
			}
		}
	}

	/**
	 * Whether a variable counts as discrete: one that takes a finite set of values, be it a symbolic
	 * finite-states variable or a continuous one already cut into intervals.
	 * <p>
	 * The variable of a node that is neither chance nor decision is left alone, exactly as
	 * {@code OnlyFiniteStatesVariables.nodeIsFinite} does: the variable of a utility node is a number
	 * by construction — {@code VariableType.getVariableTypesFor(UTILITY)} allows it nothing else — so
	 * demanding that it be discrete would ask for the impossible and leave this constraint unusable in
	 * any influence diagram.
	 *
	 * @param nodeType     the kind of node the variable belongs to
	 * @param variableType the type of the variable
	 *
	 * @return {@code true} if such a variable is acceptable under this constraint
	 */
	public static boolean isDiscrete(NodeType nodeType, VariableType variableType) {
		if (nodeType != NodeType.CHANCE && nodeType != NodeType.DECISION) {
			return true;
		}
		return variableType == VariableType.FINITE_STATES || variableType == VariableType.DISCRETIZED;
	}

}
