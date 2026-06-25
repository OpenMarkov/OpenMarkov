/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;

/**
 * Edit that replaces the partitioned interval of a node's variable, supporting undo.
 */
@SuppressWarnings("serial") public class PartitionedIntervalEdit extends PNEdit {

	private final PartitionedInterval currentPartitionedInterval;

	private final PartitionedInterval newPartitionedInterval;

    private final Node node;

	/**
	 * Creates a new edit that sets the partitioned interval of the given node.
	 *
	 * @param node                     the node whose partitioned interval will be changed
	 * @param newPartitionedInterval   the new partitioned interval to assign
	 */
	public PartitionedIntervalEdit(Node node, PartitionedInterval newPartitionedInterval) {
		super(node.getProbNet());
		this.node = node;
		this.newPartitionedInterval = newPartitionedInterval;
		this.currentPartitionedInterval = node.getVariable().getPartitionedInterval();
	}
	
	@Override protected void doEdit() {
		node.getVariable().setPartitionedInterval(newPartitionedInterval);

	}
	@Override public void undo() {
		super.undo();
		node.getVariable().setPartitionedInterval(currentPartitionedInterval);
	}

}
