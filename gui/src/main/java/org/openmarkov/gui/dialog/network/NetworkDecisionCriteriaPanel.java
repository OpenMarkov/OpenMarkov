/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import java.awt.Window;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.localize.StringDatabase;

/**
 * Panel to set network decision criteria.
 */
public class NetworkDecisionCriteriaPanel extends JPanel {
	
	private static final long serialVersionUID = -6784346878691434149L;
	
	private DecisionCriteriaTablePanel decisionCriteriaTablePanel;
	
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	
	private ProbNet probNet;	
	private final Window parent;
    
    /**
	 * Constructor.
	 * If not a network of only chance nodes, the panel is initialized
	 *
	 * @param owner parent window
	 * @param probNet    manage the network access
	 */
	public NetworkDecisionCriteriaPanel(Window owner, ProbNet probNet) {
		this.setName("NetworkDecisionCriteriaPanel");
		this.probNet = probNet;
		this.parent = owner;
		if(!probNet.onlyChanceNodes()) {			
			initialize();
		}		
	}

	/**
	 * Initialises the decision criteria table
	 */
	private void initialize() {
		add(getDecisionCriteriaTablePanel());
		setFieldFromProperties(probNet);		
	}
	
	private void reset(){
		removeAll();
	}
	
	
	/**
	 * Gets the decision criteria table
	 * @return the result
	 */
	private DecisionCriteriaTablePanel getDecisionCriteriaTablePanel() {
		if (decisionCriteriaTablePanel == null) {
			StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
			String[] columnNames = {
					stringDatabase.getString("NetworkDecisionCriteriaPanel.DecisionCriteria.ValuesTable.Columns.Id.Text"),
					stringDatabase.getString("NetworkDecisionCriteriaPanel.DecisionCriteria.ValuesTable.Columns.Name.Text"),
					stringDatabase.getString("NetworkDecisionCriteriaPanel.DecisionCriteria.ValuesTable.Columns.Unit.Text") };
			decisionCriteriaTablePanel = new DecisionCriteriaTablePanel(columnNames, probNet, parent);
			decisionCriteriaTablePanel.setName("DecisionCriteriaPanel");
			decisionCriteriaTablePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		return decisionCriteriaTablePanel;
	}
	
	/**
	 * Sets the decision criteria table fields of the probNet 
	 * @param probNet the prob net
	 */
	public void setFieldFromProperties(ProbNet probNet) {
		// StringsWithProperties agents = probNet.getAgents();
		List<Criterion> decisionCriteria = probNet.getDecisionCriteria();
		if (decisionCriteria != null) {
			Object[][] data = new Object[decisionCriteria.size()][2];
			for (int i = 0; i < decisionCriteria.size(); i++) {
				data[i][0] = decisionCriteria.get(i).getCriterionName();
				data[i][1] = decisionCriteria.get(i).getCriterionUnit();
			}
			// initialising data structure for the table model
			getDecisionCriteriaTablePanel().setData(data);
			// initialising data structure for supervising data order in GUI
			getDecisionCriteriaTablePanel().setDataTable(data);
		}
	}
	
	/**
	 * Initialises the probNet decision criteria table if it was not,
	 * for networks sets or changes in the definition panel to a
	 * not only chance nodes probNet
	 *
	 * @param probNet the prob net
	 *
	 * @return
	 */
	public boolean update(ProbNet probNet) {
		this.probNet = probNet;
        this.reset();
		boolean isUsable = !probNet.onlyChanceNodes();
		if(isUsable) {
			initialize();
		}
		return isUsable;
	}

}
