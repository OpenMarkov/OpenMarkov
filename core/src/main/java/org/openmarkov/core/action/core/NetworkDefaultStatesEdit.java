/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.action.base.PNEdit;

/**
 * {@code NetworkDefaultStatesEdit} is a simple edit that allows modify
 * the default states of the network
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/2010
 */
@SuppressWarnings("serial") public class NetworkDefaultStatesEdit extends PNEdit {

	private final State[] currentDefaultStates;
	private final State[] newDefaultStates;

	/**
	 * Creates a {@code NetworkDefaultStatesEdit} with the network and new
	 * default states specified.
	 *
	 * @param probNet       the network that will be modified.
	 * @param defaultStates the new default states.
	 */
	public NetworkDefaultStatesEdit(ProbNet probNet, State[] defaultStates) {
		super(probNet);
		this.currentDefaultStates = probNet.getDefaultStates();
		this.newDefaultStates = defaultStates;
	}

	// Methods
	@Override protected void doEdit() {
		if (newDefaultStates != null) {
			probNet.setDefaultStates(newDefaultStates);
		}
	}
    
    @Override public void undo() {
		super.undo();
		if (currentDefaultStates != null) {
			probNet.setDefaultStates(currentDefaultStates);
		}
	}
}
