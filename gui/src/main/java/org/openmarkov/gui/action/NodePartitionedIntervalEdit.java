/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;

/**
 * {@code NodePartitionedIntervalEdit} is a simple edit that allows to modify
 * the node partitioned interval.
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
public class NodePartitionedIntervalEdit extends PNEdit {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The predefined increment for new intervals
	 */
    private static final int increment = 2;
	/**
	 * The state index that partitioned interval belongs to.
	 */
	private final int indexState;
	/**
	 * The node edited
	 */
    private final Node node;
	/**
	 * Action to do with the partitioned interval
	 */
	private final StateAction stateAction;
	/**
	 * The current partitioned interval
	 */
	private final PartitionedInterval currentPartitionedInterval;

	/**
	 * A boolean that specify if the edition is in the lower limit
	 * (value or symbol)
	 */
	private final boolean lower;
	/**
	 * The new limit value
	 */
	private double newValue;

	/**
	 * The last value contained in PartitionedInterval
	 */
	private double lastValue;

	/**
     * Creates a new {@code PartiTionedIntervalEdit} to edit the limit
	 * symbol of the interval
	 *
	 * @param node        The node that contain the partionInterval object to be edited.
	 * @param stateAction The action to do in this edit.
	 * @param indexState  The state index that partitionedInterval object belongs to.
	 * @param lower       A boolean that specify if the edition is in the lower symbol
	 */
	public NodePartitionedIntervalEdit(Node node, StateAction stateAction, int indexState, boolean lower) {
		super(node.getProbNet());
		this.node = node;
		this.indexState = indexState;
		this.stateAction = stateAction;
		this.lower = lower;
		this.currentPartitionedInterval = node.getVariable().
				getPartitionedInterval();

	}

	
	@Override protected void doEdit() {

		switch (stateAction) {
		case MODIFY_DELIMITER_INTERVAL:
			if (lower) {
				currentPartitionedInterval.changeLimit(indexState, currentPartitionedInterval.getLimit(indexState),
						!currentPartitionedInterval.getBelongsToLeftSide(indexState));
			} else {
				currentPartitionedInterval
						.changeLimit(indexState + 1, currentPartitionedInterval.getLimit(indexState + 1),
								!currentPartitionedInterval.getBelongsToLeftSide(indexState + 1));
			}
			break;
		case MODIFY_VALUE_INTERVAL:
			if (lower)
				currentPartitionedInterval
						.changeLimit(indexState, newValue, currentPartitionedInterval.getBelongsToLeftSide(indexState));
			else

				currentPartitionedInterval.changeLimit(indexState + 1, newValue, currentPartitionedInterval.
						getBelongsToLeftSide(indexState + 1));
			break;

		}

	}
    
    @Override public void undo() {
		super.undo();
		switch (stateAction) {
		case MODIFY_DELIMITER_INTERVAL:
			if (lower) {
				currentPartitionedInterval.changeLimit(indexState, currentPartitionedInterval.getLimit(indexState),
						!currentPartitionedInterval.getBelongsToLeftSide(indexState));
			} else {
				currentPartitionedInterval
						.changeLimit(indexState + 1, currentPartitionedInterval.getLimit(indexState + 1),
								!currentPartitionedInterval.getBelongsToLeftSide(indexState + 1));
			}
			break;
		case MODIFY_VALUE_INTERVAL:
			if (lower)
				currentPartitionedInterval.changeLimit(indexState, lastValue,
						currentPartitionedInterval.getBelongsToLeftSide(indexState));
			else
				currentPartitionedInterval.changeLimit(indexState + 1, lastValue, currentPartitionedInterval.
						getBelongsToLeftSide(indexState + 1));
			break;

		}

	}

	/**
	 * Gets the node edited
	 *
	 * @return the node edited
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Gets the boolean lower identifier
	 *
	 * @return a boolean lower identifier
	 */
	public boolean getLower() {
		return lower;
	}

	/**
	 * Gets the new value of the limit modified
	 *
	 * @return the new value of the limit modified
	 */
	public Double getNewValue() {
		return newValue;
	}

}
