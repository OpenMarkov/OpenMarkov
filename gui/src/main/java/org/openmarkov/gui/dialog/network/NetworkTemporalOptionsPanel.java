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
		
		CycleLength temporalUnit;
		if (probNet.getCycleLength() != null) {
			temporalUnit = probNet.getCycleLength();
		} else {
			temporalUnit = new CycleLength();
			probNet.setCycleLength(temporalUnit);
		}
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
