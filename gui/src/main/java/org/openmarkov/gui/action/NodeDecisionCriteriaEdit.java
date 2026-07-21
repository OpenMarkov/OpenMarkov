/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;

/**
 * Edit that changes the decision criterion assigned to a node's variable, supporting undo.
 */
@SuppressWarnings("serial") public class NodeDecisionCriteriaEdit extends PNEdit {

	private final Criterion currentDecisionCriteria;
	private final Criterion newDecisionCriteria;
	private final Node node;

	/**
	 * Creates a new edit that sets the decision criterion of the given node.
	 *
	 * @param node              the node whose decision criterion will be changed
	 * @param decisionCriteria  the new decision criterion to assign
	 */
	public NodeDecisionCriteriaEdit(Node node, Criterion decisionCriteria) {
		super(node.getProbNet());
		this.node = node;
		this.currentDecisionCriteria = node.getVariable().getDecisionCriterion();
		this.newDecisionCriteria = decisionCriteria;
	}
	
	@Override protected void doEdit() {
		node.getVariable().setDecisionCriterion(newDecisionCriteria);
	}
    
    @Override public void undo() {
		super.undo();
		node.getVariable().setDecisionCriterion(currentDecisionCriteria);
	}

}

