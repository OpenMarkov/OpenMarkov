/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.action.base.PNEdit;

/**
 * {@code NodeReplaceStatesEdit} is a simple edit that allows modify the
 * states of node
 *
 * @author Miguel Palacios
 * @version 1.0 10/05/2011
 */

@SuppressWarnings("serial") public class NodeReplaceStatesEdit extends PNEdit {
    
    
    private final State[] lastStates;
    
    private final State[] newStates;

	private final Node node;
    
    public NodeReplaceStatesEdit(Node node, State[] newStates) {
		super(node.getProbNet());
		this.node = node;
		this.lastStates = node.getVariable().getStates();
		this.newStates = newStates;
	}

	// Methods
	@Override protected void doEdit() {
		node.getVariable().replaceStates(node,newStates);
	}
    
    @Override public void undo() {
		super.undo();
        node.getVariable().replaceStates(node,lastStates);
	}


}
