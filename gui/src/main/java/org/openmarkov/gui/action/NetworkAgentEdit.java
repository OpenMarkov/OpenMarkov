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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code NetworkAgentEdit} is a simple edit that allow modify
 * the agents of a network
 *
 * @author myebra
 */
@SuppressWarnings("serial") public class NetworkAgentEdit extends PNEdit {

	private final String agentName;
	private final StateAction stateAction;
	private List<StringWithProperties> lastAgents;
	private final Map<Node, StringWithProperties> lastAgentOfEachNode = new HashMap<>();
	private final Object[][] dataTable;


	/**
	 * Creates a new edit that modifies the agents of a network.
	 *
	 * @param probnet     the network whose agents will be modified
	 * @param stateAction the action to perform (ADD, REMOVE, etc.)
	 * @param agentName   the name of the agent being modified
	 * @param dataTable   the data table with agent properties
	 */
	public NetworkAgentEdit(ProbNet probnet, StateAction stateAction, String agentName,
							Object[][] dataTable) {
		super(probnet);
		this.agentName = agentName;
		this.stateAction = stateAction;
		this.dataTable = dataTable;
	}
	
	@Override protected void doEdit() {
		this.lastAgents = super.getProbNet().getAgents().stream()
				.collect(Collectors.toList());
		lastAgentOfEachNode.clear();
		for (Node node : probNet.getNodes()) {
			lastAgentOfEachNode.put(node, node.getVariable().getAgent());
		}
		probNet.modifyAgent(stateAction,agentName,dataTable);

	}
    
    @Override public void undo() {
		super.undo();
		probNet.setAgents(lastAgents);
		lastAgentOfEachNode.forEach((node, agent) -> node.getVariable().setAgent(agent));
	}

}
