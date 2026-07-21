/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author jperez-martin
 * @author artasom
 */
public interface Evaluation extends Task {

	/**
	 * @return The global expected utility
	 */
	TablePotential getProbability()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther;

	/**
	 * @return The global expected utility
	 * defined over the conditioning variables.
	 */
	TablePotential getUtility()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther;

	/**
	 * @return The optimal strategy tree
	 */
	StrategyTree getOptimalStrategyTree()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther;

}