/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.ModelNetworkConstraint;

/**
 * Converts an undirected link into a directed one between two variables.
 */
@SuppressWarnings("serial") public final class OrientLinkEdit extends BaseLinkEdit {

	/**
	 * @param probNet    the probabilistic network
	 * @param variable1  the source variable
	 * @param variable2  the destination variable
	 * @param isDirected whether the resulting link should be directed
	 */
	public OrientLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected) {
		super(probNet, variable1, variable2, isDirected);
	}
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(ModelNetworkConstraint.class) instanceof ModelNetworkConstraint constraint
                && !constraint.isLinkInversionAllowed() && !constraint.canEditBeDone(this)) {
            constraintChecker.addException(new ConstraintViolatedException.ModelDoesNotAllowInvertingLink(constraint, this.variableTo, this.variableFrom));
        }
    }
    
    // Methods
	/**
	 * Do the edition by removing the existing link and adding a new directed link between the same two variables.
	 */
	@Override protected void doEdit() {
        probNet.removeLink(variableFrom, variableTo, false);
        probNet.addLink(variableFrom, variableTo, true);
    }
    
	/**
	 * Undo the edition by removing the existing link and adding
	 * a new undirected link between the same two variables.
	 */
	@Override public void undo() {
		super.undo();
        probNet.removeLink(variableFrom, variableTo, true);
        probNet.addLink(variableFrom, variableTo, false);
    }

	/**
	 * Method to compare two directLinkEdits comparing the names of
	 * the source and destination variables alphabetically.
	 * @param obj Edit
	 * @return result of the comparison
	 */
	public int compareTo(OrientLinkEdit obj) {
		int result;

		if ((
                result = variableFrom.getName().compareTo(obj.getVariableFrom().
                                                             getName())
		) != 0)
			return result;
		if ((
                result = variableTo.getName().compareTo(obj.getVariableTo().
                                                           getName())
		) != 0)
			return result;
        return 0;
    }

	@Override public String getOperationName() {
		return "Orient link";
	}

	@Override public BaseLinkEdit getUndoEdit() {
		return this;
	}
}
