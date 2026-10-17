/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.core.NetworkCurrencyEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NetworkCurrency;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Panel to set network decision criteria.
 */
public class NetworkDecisionCriteriaPanel extends JPanel {
	
	private static final long serialVersionUID = -6784346878691434149L;
	
	private DecisionCriteriaTablePanel decisionCriteriaTablePanel;
	
	private JPanel currencyPanel;
	
	private JComboBox<String> currencyComboBox;
	
	/** The currency of each item of the selector; null for the item that says what the cost is measured in. */
	private final List<NetworkCurrency> currencyOfEachItem = new ArrayList<>();
	
	private boolean showingTheCurrency;
	
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
		JPanel criteriaAndCurrency = new JPanel(new BorderLayout(0, 5));
		criteriaAndCurrency.add(getDecisionCriteriaTablePanel(), BorderLayout.CENTER);
		criteriaAndCurrency.add(getCurrencyPanel(), BorderLayout.SOUTH);
		add(criteriaAndCurrency);
		setFieldFromProperties(probNet);
		showTheCurrency();
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
	 * The selector of the currency of the network, which follows the table of criteria.
	 */
	private JPanel getCurrencyPanel() {
		if (currencyPanel == null) {
			currencyComboBox = new JComboBox<>();
			currencyComboBox.setName("currencyComboBox");
			currencyComboBox.addActionListener(e -> currencyChosen());
			getDecisionCriteriaTablePanel().getValuesTable().getModel()
					.addTableModelListener(e -> SwingUtilities.invokeLater(this::showTheCurrency));
			currencyPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
			currencyPanel.add(new JLabel(stringDatabase.getString("NetworkDecisionCriteriaPanel.Currency.Text")));
			currencyPanel.add(currencyComboBox);
		}
		return currencyPanel;
	}
	
	/**
	 * Shows the currency of the network; when it is in none, shows what its cost is measured in, and changes nothing.
	 */
	private void showTheCurrency() {
		showingTheCurrency = true;
		currencyComboBox.removeAllItems();
		currencyOfEachItem.clear();
		Optional<NetworkCurrency> current = NetworkCurrency.of(probNet);
		List<NetworkCurrency> offered = NetworkCurrency.offered();
		current.filter(currency -> !offered.contains(currency)).ifPresent(offered::add);
		if (current.isEmpty()) {
			currencyOfEachItem.add(null);
			currencyComboBox.addItem(NetworkCurrency.costUnitOf(probNet)
					.map(unit -> stringDatabase.getString("NetworkDecisionCriteriaPanel.Currency.Other") + " (" + unit + ")")
					.orElse(stringDatabase.getString("NetworkDecisionCriteriaPanel.Currency.None")));
		}
		for (NetworkCurrency currency : offered) {
			currencyOfEachItem.add(currency);
			currencyComboBox.addItem(currency.code() + " (" + currency.symbol() + ")");
		}
		currencyComboBox.setSelectedIndex(current.map(currencyOfEachItem::indexOf).orElse(0));
		showingTheCurrency = false;
	}
	
	private void currencyChosen() {
		int index = currencyComboBox.getSelectedIndex();
		if (showingTheCurrency || index < 0 || currencyOfEachItem.get(index) == null) {
			return;
		}
		NetworkCurrency chosen = currencyOfEachItem.get(index);
		if (NetworkCurrency.of(probNet).equals(Optional.of(chosen))) {
			return;
		}
		try {
			new NetworkCurrencyEdit(probNet, chosen).executeEdit();
		} catch (DoEditException e) {
			throw new UnrecoverableException(e);
		}
		setFieldFromProperties(probNet);
		SwingUtilities.invokeLater(this::showTheCurrency);
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
