/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;

/*****
 * A simple edit which allows to add and modify intervals and modify them.
 *
 * @author caroline
 *
 */
@SuppressWarnings("serial") public class RevelationIntervalEdit extends PNEdit {

	/***
	 * Object which stores the revelation conditions
	 */
	private final Link<Node> link;

	/**
	 * The action to carry out
	 */
	private final StateAction stateAction;

	/****
	 * The new value of the limit
	 */
	private final double newValue;

	/***
	 * The old value of the limit
	 */
	private double oldValue;
	/**
	 * index of the row selected
	 */
	private final int rowSelected;
	/***
	 * Indicates whether the lower limit or upper limit is modified
	 */
	private final boolean isLower;
	/*****
	 * The interval prior to the modification
	 */
	private PartitionedInterval lastInterval;

	/**
	 * Creates a RevelationConditionEdit which carries out the modifications of
	 * a revealing condition interval.
	 *
	 * @param link        the link containing the revelation conditions
	 * @param stateAction the action to perform (ADD, REMOVE, MODIFY_VALUE_INTERVAL, MODIFY_DELIMITER_INTERVAL)
	 * @param row         the index of the selected interval row
	 * @param newValue    the new limit value (used for value modifications)
	 * @param isLower     whether the modification targets the lower limit ({@code true}) or upper limit ({@code false})
	 */

	public RevelationIntervalEdit(Link<Node> link, StateAction stateAction, int row, double newValue, boolean isLower) {
        super(link.getFrom().getProbNet());
		this.link = link;
		this.stateAction = stateAction;
		this.rowSelected = row;
		this.newValue = newValue;
		this.isLower = isLower;
	}
	
	@Override protected void doEdit() {

		switch (stateAction) {
		case ADD:
			PartitionedInterval newPartitionedInterval = getNewPartitionedInterval();
			lastInterval = newPartitionedInterval;
			link.addRevealingInterval(newPartitionedInterval);

			break;

		case REMOVE: {
			lastInterval = link.getRevealingIntervals().get(rowSelected);
			link.getRevealingIntervals().remove(rowSelected);
		}
		break;
		case MODIFY_VALUE_INTERVAL: {
			PartitionedInterval currentPartitionedInterval = link.getRevealingIntervals().get(rowSelected);
			int intervalIndex = isLower ? 0 : 1;
			oldValue = currentPartitionedInterval.getLimits()[intervalIndex];
			currentPartitionedInterval.getLimits()[intervalIndex] = newValue;

		}
		break;

		case MODIFY_DELIMITER_INTERVAL: {
			PartitionedInterval currentPartitionedInterval = link.getRevealingIntervals().get(rowSelected);
			int intervalIndex = isLower ? 0 : 1;
			currentPartitionedInterval.getBelongsToLeftSide()[intervalIndex] = !currentPartitionedInterval
					.getBelongsToLeftSide(intervalIndex);
		}
		break;

		}

	}
    
    @Override public void undo() {
		super.undo();
		switch (stateAction) {
		case ADD:
			link.getRevealingIntervals().remove(lastInterval);
			break;
		case REMOVE:
			link.getRevealingIntervals().add(rowSelected, lastInterval);
			break;
		case MODIFY_VALUE_INTERVAL: {
			PartitionedInterval interval = link.getRevealingIntervals().get(rowSelected);
			int intervalIndex = isLower ? 0 : 1;
			interval.getLimits()[intervalIndex] = oldValue;
		}
		break;
		case MODIFY_DELIMITER_INTERVAL: {
			PartitionedInterval interval = link.getRevealingIntervals().get(rowSelected);
			int intervalIndex = isLower ? 0 : 1;
			interval.getBelongsToLeftSide()[intervalIndex] = !interval.getBelongsToLeftSide(intervalIndex);
		}
		break;
		}

	}

	/**
	 * This method add a new default subInterval, in the current
	 * PartitionedInterval object
	 *
	 * @return The PartitionedInterval object with a new default subInterval
	 */

	private PartitionedInterval getNewPartitionedInterval() {
		if (link.getRevealingIntervals().isEmpty()) {
			return new PartitionedInterval(false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
        }
        PartitionedInterval interval = link.getRevealingIntervals().get(link.getRevealingIntervals().size() - 1);
        return new PartitionedInterval(false, interval.getLimit(1), Double.POSITIVE_INFINITY, false);
    }

}
