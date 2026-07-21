/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.localize.StringDatabase;

/**
 * Panel to set the Agents of a network.
 */
public class NetworkAgentsPanel extends JPanel {
	
	private static final long serialVersionUID = 4263545721155432856L;	
	
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	
	private NetworkAgentsTablePanel newtworkAgentsTablePanel;
	private ProbNet probNet;
    
    /**
	 * Constructor. If the probNet has agents, the panel is activated
	 * 
	 * @param probNet the prob net
	 */
	public NetworkAgentsPanel(ProbNet probNet) {
		this.setName("NetworkAgentsPanel");
		this.probNet = probNet;
		if(probNet.getAgents() != null) {
			initialize();			
		}
	}

	/**
	 * Gets the panel table and sets its properties  
	 */
	private void initialize() {
		add(getNetworkAgentsTablePanel());
		setFieldFromProperties(probNet);		
	}
	
	private void reset() {
		removeAll();
	}

	/**
	 * Gets the agents table
	 * 
	 * @return a table of agents
	 */
	private NetworkAgentsTablePanel getNetworkAgentsTablePanel() {
		if (newtworkAgentsTablePanel == null) {
			String[] columnNames = { "Key", "Agents" };
			newtworkAgentsTablePanel = new NetworkAgentsTablePanel(columnNames, probNet);
			newtworkAgentsTablePanel.setName("networkAgentsPanel");
			newtworkAgentsTablePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		return newtworkAgentsTablePanel;
	}
	
	/**
	 * Sets the agents table fields of the probNet 
	 * 
	 * @param probNet the prob net
	 */
	public void setFieldFromProperties(ProbNet probNet) {
		// StringsWithProperties agents = probNet.getAgents();
		List<StringWithProperties> agents = probNet.getAgents();
		if (agents != null) {
			Object[][] data = new Object[agents.size()][1];
			for (int i = 0; i < agents.size(); i++) {
				data[i][0] = agents.get(i).getString();
			}
			// initialising data structure for the table model
			getNetworkAgentsTablePanel().setData(data);
			// initialising data structure for supervising data order in GUI
			getNetworkAgentsTablePanel().setDataTable(data);
		}
	}
	
	/**
	 * Updates the probNet agents table and sets it when the network
	 * is set or changed in the definition panel to a probNet with agents.
	 *
	 * @param probNet the prob net
	 *
	 * @return
	 */
	public boolean update(ProbNet probNet) {
		this.probNet = probNet;
		this.reset();
		boolean isUsable = probNet.getAgents() != null;
		if(isUsable) {
			initialize();
		}
		return isUsable;
	}

}
