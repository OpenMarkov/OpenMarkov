/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;

/**
 * @author jperez-martin
 */
/**
 * The task behind the three sensitivity-analysis views of the interface — map, plot, and
 * tornado/spider. It used to be three interfaces, {@code SensAnMap}, {@code SensAnPlot} and
 * {@code SensAnTornadoSpider}, each declaring exactly this method: the same rule written in
 * three places. What tells the analyses apart is how each implementation is configured when it
 * is built, not the contract.
 */
public interface SensitivityAnalysis extends Task {

	/**
	 * @return For each uncertain parameter of the analysis, the potential with the results
	 * sampled for it.
	 */
	HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials();
}
