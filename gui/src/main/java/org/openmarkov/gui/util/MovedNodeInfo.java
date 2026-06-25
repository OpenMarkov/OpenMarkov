/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;

/**
 * This class contains the information of a node that has been moved. This class
 * only serves to contain the information for undo and redo.
 *
 * @author jmendoza
 */
public class MovedNodeInfo {

	/**
	 * Node whose position must be undone and redone.
	 */
    private final Node nodeWrapper;

	/**
	 * Original position of the node before it has been moved.
	 */
    private Point2D.Double diffPosition;

	/**
	 * Constructor. Only saves the information.
	 *
	 * @param newNodeWrapper  node moved.
	 * @param newDiffPosition difference with the original position.
	 */
	public MovedNodeInfo(Node newNodeWrapper, Point2D.Double newDiffPosition) {

		nodeWrapper = newNodeWrapper;
		diffPosition = newDiffPosition;
	}

	/**
	 * Returns the node that has been moved.
	 *
	 * @return the node that has been moved.
	 */
	public Node getNode() {

		return nodeWrapper;
	}

	/**
	 * Returns the difference of position of the node.
	 *
	 * @return two values that represents the difference of position of the
	 * node.
	 */
	public Point2D.Double getDiffPosition() {

		return diffPosition;
	}

	/**
	 * Sets the value of the differente of position.
	 *
	 * @param newDiffPosition new difference of position.
	 */
	public void setDiffPosition(Point2D.Double newDiffPosition) {

		diffPosition = newDiffPosition;
	}
}
