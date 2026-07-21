/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * Edit that removes the imposed policy from a decision node, restoring it to the
 * optimal policy state. Supports undo by storing the previous potential.
 */
public class RemovePolicyEdit extends PNEdit {
    
    private final @Nullable Potential oldPotential;
    private final Node node;

	/**
	 * @param node Node
	 */
	public RemovePolicyEdit(Node node) {
		super(node.getProbNet());
        this.node = node;
        if (node.getNodeType() == NodeType.DECISION && node.getPolicyType() != PolicyType.OPTIMAL) {
            oldPotential = node.getPotentials().get(0);
        } else {
            oldPotential = null;
        }
	}

	
	@Override protected void doEdit() {
        node.clearPotentials();
	}
    
    @Override public void undo() {
		super.undo();
        if (oldPotential != null) {
            node.setPotential(oldPotential);
        } else {
            node.clearPotentials();
        }
	}
}
