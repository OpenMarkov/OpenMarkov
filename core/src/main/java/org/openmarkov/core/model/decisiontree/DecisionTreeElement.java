/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.model.network.EvidenceCase;

import java.util.List;

/**
 * Interface that represents an element in a decision tree.
 * Both nodes and branches implement this interface.
 */
public sealed interface DecisionTreeElement permits DecisionTreeBranch, DecisionTreeNode {
	/**
	 * Returns the list of children of this element.
	 * 
	 * @return List of children.
	 */
	List<DecisionTreeElement> getChildren();

	/**
	 * Returns the evidence case associated with the path to this element.
	 * 
	 * @return The accumulated evidence.
	 */
	EvidenceCase getBranchStates();

	/**
	 * Returns the probability of the scenario leading to this element.
	 * 
	 * @return The scenario probability.
	 */
	double getScenarioProbability();

	/**
	 * Sets the parent of this element.
	 * 
	 * @param parent The parent element.
	 */
	void setParent(DecisionTreeElement parent);

}
