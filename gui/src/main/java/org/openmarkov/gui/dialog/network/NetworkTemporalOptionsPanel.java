/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openmarkov.core.action.core.CycleLengthEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.localize.StringDatabase;

/**
 * Panel to set network temporal options.
 */
public class NetworkTemporalOptionsPanel extends JPanel {
	
	private static final long serialVersionUID = -7384917480313414961L;
	
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	
	private ProbNet probNet;
	private JComboBox<String> temporalUnits; 
	private JTextField unitScale;
	private Unit probNetUnit;
	private double probNetScale;
    
    
    /**
	 * Constructor. If the probNet is not constraint to non-temporary
	 * variables only, the panel is activated
	 *
	 * @param probNet    manage the network access
	 */
	public NetworkTemporalOptionsPanel(ProbNet probNet) {
		this.setName("NetworkTemporalOptionsPanel");
		this.probNet = probNet;
        if (!probNet.hasConstraintOfClass(OnlyAtemporalVariables.class)) {
			initialize();
		}
	}
	
	/**
	 * Initialises temporal units and its scales 
	 */
	private void initialize() {
		add(getUnitScale());
		add(getTemporalUnits());
		
		// Opening the tab does not change the network: a network without a cycle length shows the default one
		CycleLength temporalUnit = probNet.getCycleLength() != null ? probNet.getCycleLength() : new CycleLength();
		probNetUnit = temporalUnit.getUnit();
		probNetScale = temporalUnit.getValue();

		temporalUnits.setSelectedItem(
				stringDatabase.getString("NetworkTemporalOptionsPanel.TemporalOptions.Unit." + probNetUnit.toString()));
		unitScale.setText(String.valueOf(probNetScale));					
	}
	
	private void reset(){
		removeAll();
	}
	
	private Component getTemporalUnits() {
		if (temporalUnits == null) {
			temporalUnits = new JComboBox<String>();
			temporalUnits.setName("cycleLengthUnit");
			for (Unit unit : Unit.values()) {
				String newUnit = StringDatabase.getUniqueInstance()
						.getString("NetworkTemporalOptionsPanel.TemporalOptions.Unit." + unit.toString());
				temporalUnits.addItem(newUnit);
			}
		}
		return temporalUnits;
	}

	private Component getUnitScale() {
		if (unitScale == null) {
			unitScale = new JTextField();
			unitScale.setName("cycleLengthValue");
		}
		return unitScale;
	}

	/**
	 * Initialises, if not already to, temporal options for probNets
	 * sets or changes in the definition panel to networks not restricted to
	 * non-temporal variables only.
	 *
	 * @param probNet the prob net
	 *
	 * @return
	 */
	/**
	 * Gives the network the cycle length written in the panel, if it is a different one, through an edit that can be
	 * undone. A length that nobody has changed is not checked: the one that a file brings is kept, whatever it is.
	 *
	 * @return false, without changing the network, if the length is not a positive number
	 */
	public boolean applyCycleLength() {
		if (unitScale == null || probNet.hasConstraintOfClass(OnlyAtemporalVariables.class)) {
			return true;
		}
		if (unitScale.getText().trim().equals(String.valueOf(probNetScale))
				&& Unit.values()[temporalUnits.getSelectedIndex()] == probNetUnit) {
			return true;
		}
		double value;
		try {
			value = Double.parseDouble(unitScale.getText().trim());
		} catch (NumberFormatException e) {
			value = Double.NaN;
		}
		if (!(value > 0) || Double.isInfinite(value)) {
			return false;
		}
		Unit unit = Unit.values()[temporalUnits.getSelectedIndex()];
		CycleLength current = probNet.getCycleLength();
		if (current == null || current.getUnit() != unit || current.getValue() != value) {
			try {
				new CycleLengthEdit(probNet, new CycleLength(unit, value)).executeEdit();
			} catch (DoEditException e) {
				throw new UnreachableException(e);
			}
		}
		return true;
	}

	public boolean update(ProbNet probNet) {
		this.probNet = probNet;
		reset();
		boolean isUsable = !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);
		if (isUsable) {
			initialize();
		}
		return isUsable;
	}
}
