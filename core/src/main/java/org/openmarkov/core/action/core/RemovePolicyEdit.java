/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit that removes the imposed policy from a decision node, restoring it to the
 * optimal policy state. Supports undo by storing the previous potentials.
 */
public class RemovePolicyEdit extends PNEdit {
    
    private final List<Potential> oldPotentials;
    private final Node node;

	/**
	 * @param node Node
	 */
	public RemovePolicyEdit(Node node) {
		super(node.getProbNet());
        this.node = node;
        oldPotentials = new ArrayList<>(node.getPotentials());
	}

	
	@Override protected void doEdit() {
        node.clearPotentials();
	}
    
    @Override public void undo() {
		super.undo();
        node.setPotentials(new ArrayList<>(oldPotentials));
	}
}
