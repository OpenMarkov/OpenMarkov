/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.PNEdit;

import java.util.stream.Collectors;

/**
 * Edit that adds a potential to the network's global potential list.
 */
@SuppressWarnings("serial") public class AddPotentialEdit extends PNEdit {

	protected final Potential potential;

	// Constructor

	/**
	 * @param probNet   {@code ProbNet}
	 * @param potential {@code Potential}
	 */
	public AddPotentialEdit(ProbNet probNet, Potential potential) {
		super(probNet);
		this.potential = potential;
	}

	// Methods
    @Override protected void doEdit() {
		probNet.addPotential(potential);
	}
    
    @Override public void undo() {
		super.undo();
		probNet.removePotential(potential);
	}

	/**
	 * @return potential {@code Potential}
	 */
	public Potential getPotential() {
		return potential;
	}

	/**
	 * @return A {@code String} with the potential variables.
	 */
	public String toString() {
		if (potential != null) {
			return potential.getVariables().stream().map(Variable::getName).collect(Collectors.joining(" "));
		}
		return "null !!!!";
	}

}
