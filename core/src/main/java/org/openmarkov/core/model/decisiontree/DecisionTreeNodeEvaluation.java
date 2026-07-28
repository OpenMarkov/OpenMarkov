/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

/**
 * The result the inference computes for a node of the decision tree: the probability of the
 * scenario that leads to it, plus whatever its subclasses add as the value of that scenario.
 *
 * @author Manuel Arias
 */
public class DecisionTreeNodeEvaluation {

	private double prob;

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}



}
