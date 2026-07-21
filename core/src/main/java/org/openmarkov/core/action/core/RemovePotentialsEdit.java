/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;

/**
 * Edit that removes one or more potentials from a network. Potentials
 * can be added incrementally via {@link #addPotentials}.
 */
@SuppressWarnings("serial")
public class RemovePotentialsEdit extends PNEdit {

	private final ArrayList<Potential> potentialsToDelete;

	/**
	 * @param probNet    {@code ProbNet}
	 * @param potentials {@code ArrayList} of {@code Potential}s
	 */
    public RemovePotentialsEdit(ProbNet probNet, ArrayList<Potential> potentials) {
		super(probNet);
		potentialsToDelete = new ArrayList<>(potentials);
	}

	/**
	 * Adds more potentials to delete
	 *
	 * @param morePotentials {@code ArrayList} of {@code Potential}s
	 */
	public void addPotentials(ArrayList<Potential> morePotentials) {
		potentialsToDelete.addAll(morePotentials);
	}

	/**
	 * @return {@code String}
	 */
	public String toString() {
        String auxString = this.getClass().getSimpleName() + ":\n";
		for (Potential potential : potentialsToDelete) {
			auxString = auxString + potential.getVariables().toString() + " ";
		}
		return auxString;
	}
	
	@Override protected void doEdit() {
		for (Potential potential : potentialsToDelete) {
			probNet.removePotential(potential);
		}
	}
 
}
