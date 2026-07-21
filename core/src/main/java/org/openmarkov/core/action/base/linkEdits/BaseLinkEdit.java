/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.action.base.PNEdit;

/**
 * Abstract base class for all link-related edits (add, remove, invert, orient).
 * Holds the two endpoint variables and the link directionality.
 *
 * @author Inigo
 */
@SuppressWarnings("serial") public abstract sealed class BaseLinkEdit extends PNEdit
        permits AddLinkEdit, InvertLinkAndUpdatePotentialsEdit, InvertLinkEdit, OrientLinkEdit, RemoveLinkEdit {

	// Attributes
    protected final Variable variableFrom;
    
    protected final Variable variableTo;

	protected boolean isDirected;

	// Constructor

	/**
	 * @param probNet      the probabilistic network
	 * @param variableFrom the source variable of the link
	 * @param variableTo   the destination variable of the link
	 * @param isDirected   {@code true} for a directed link, {@code false} for undirected
	 */
    public BaseLinkEdit(ProbNet probNet, Variable variableFrom, Variable variableTo, boolean isDirected) {
		super(probNet);
        this.variableFrom = variableFrom;
        this.variableTo = variableTo;
		this.isDirected = isDirected;
	}

	/**
	 * @return the source variable of the link
	 */
    public Variable getVariableFrom() {
        return variableFrom;
	}

	/**
	 * @return the destination variable of the link
	 */
    public Variable getVariableTo() {
        return variableTo;
	}

	/**
	 * @return {@code true} if this is a directed link
	 */
	public boolean isDirected() {
		return isDirected;
	}

	@Override public int hashCode() {
        return variableFrom.hashCode() + 7 * variableTo.hashCode() + 17 * ((isDirected) ? 1 : 0);
	}

	@Override public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		return (
                (this.variableFrom.equals(((BaseLinkEdit) obj).variableFrom)) && (
                        this.variableTo.equals(((BaseLinkEdit) obj).variableTo)
				) && (this.isDirected == (((BaseLinkEdit) obj).isDirected))
		);
	}

	/**
	 * @return A {@code String} with the type of link and the names of
	 * {@code variable1} and {@code variable2}.
	 */
	@Override public String toString() {
		String out = getOperationName() + ": ";
        if (variableFrom == null) {
			out += "null";
		} else {
			out += variableFrom.getName();
		}
		if (isDirected) {
			out += " --> ";
		} else {
			out += " --- ";
		}
        if (variableTo == null) {
			out += "null";
		} else {
			out += variableTo.getName();
		}
		return out.toString();
	}

	/**
	 * @return a human-readable name for the link operation (e.g., "Add link")
	 */
	public abstract String getOperationName();

	/**
	 * Returns the opposite edit. E.g. an AddLinkEdit would return a
	 * RemoveLinkEdit instance
	 *
	 * @return the opposite edit
	 */
	public abstract BaseLinkEdit getUndoEdit();
}
