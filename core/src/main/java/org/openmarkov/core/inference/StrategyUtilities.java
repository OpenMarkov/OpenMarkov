/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;
import java.util.Map;

public class StrategyUtilities {

	Map<Variable, TablePotential> utilities;

	/**
	 *
	 */
	public StrategyUtilities() {
		super();
		utilities = new HashMap<>();
	}

	/**
	 * @return the utilities
	 */
	public Map<Variable, TablePotential> getUtilities() {
		return utilities;
	}

	/**
	 * @param utilities the utilities to set
	 */
	public void setUtilities(Map<Variable, TablePotential> utilities) {
		this.utilities = utilities;
	}

	/**
	 * @param decision Decision variable
	 * @return the utilities
	 */
	public TablePotential getUtilities(Variable decision) {
		return utilities.get(decision);
	}

	public void assignUtilityTable(Variable decision, TablePotential globalUtilityTable) {
		utilities.put(decision, globalUtilityTable);

	}

}
