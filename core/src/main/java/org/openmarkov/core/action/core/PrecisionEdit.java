/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;

/**
 * Edit that modifies the precision property of a node.
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
@SuppressWarnings("serial")
public class PrecisionEdit extends PNEdit {
	/**
	 * The last purpose before the edition
	 */
	private final Double lastPrecision;
	/**
	 * The new purpose after the edition
	 */
	private final double newPrecision;
	/**
	 * The edited node
	 */
    private final Node node;

	/**
	 * Creates a new {@code PurposeEdit} with the node and its new purpose.
	 *
	 * @param node         the edited node
	 * @param newPrecision the new precision
	 */
	public PrecisionEdit(Node node, double newPrecision) {
		super(node.getProbNet());
		this.lastPrecision = node.getVariable().getPrecision();
		this.newPrecision = newPrecision;
		this.node = node;
	}
	
	@Override protected void doEdit() {
		node.getVariable().setPrecision(newPrecision);
	}
    
    @Override public void undo() {
		super.undo();
		node.getVariable().setPrecision(lastPrecision);
	}

}

