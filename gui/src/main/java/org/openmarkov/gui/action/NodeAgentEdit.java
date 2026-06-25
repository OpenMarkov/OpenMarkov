/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.StringWithProperties;

/**
 * Edit that changes the agent assigned to a node's variable, supporting undo.
 *
 * @author myebra
 */
@SuppressWarnings("serial") public class NodeAgentEdit extends PNEdit {

	private final StringWithProperties currentAgent;
	private final StringWithProperties newAgent;
	private final Node node;

	/**
	 * Creates a new edit that sets the agent of the given node.
	 *
	 * @param node  the node whose agent will be changed
	 * @param agent the new agent to assign
	 */
	public NodeAgentEdit(Node node, StringWithProperties agent) {
		super(node.getProbNet());
		this.node = node;
		this.currentAgent = node.getVariable().getAgent();
		this.newAgent = agent;
	}
	
	@Override protected void doEdit() {
		node.getVariable().setAgent(newAgent);
	}
    
    @Override public void undo() {
		super.undo();
		node.getVariable().setAgent(currentAgent);
	}

}
