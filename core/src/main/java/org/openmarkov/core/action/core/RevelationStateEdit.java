/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;

/*******
 * <p>
 * A simple edit which allows to add or remove a revealing state of a link.
 *
 */
@SuppressWarnings("serial") public class RevelationStateEdit extends PNEdit {

	private final Node from;
	private final Node to;
	private final boolean directed;

	/**
	 * The link of the network, looked for each time: undoing and redoing the drawing of the link replaces it by
	 * another object, and the edit used to write in the old one (error 27 of the pending list).
	 */
	private Link<Node> link() {
		return probNet.getLink(from, to, directed);
	}

	private final State newState;

	private final boolean selected;

	public RevelationStateEdit(Link<Node> link, State state, boolean selected) {
        super(link.getFrom().getProbNet());
		this.from = link.getFrom();
		this.to = link.getTo();
		this.directed = link.isDirected();
		this.selected = selected;
		this.newState = state;

	}
	
	@Override protected void doEdit() {
		if (selected) {
			link().addRevealingState(newState);
		} else {
			link().removeRevealingState(newState);
		}
	}
    
    @Override public void undo() {
		super.undo();
		if (selected) {
			link().removeRevealingState(newState);
		} else {
			link().addRevealingState(newState);

		}
	}

}
