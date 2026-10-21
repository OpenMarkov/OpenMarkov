/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.inference.Strategy;

/**
 * The evaluation of a node in a tree whose utility is a single number, as in ordinary
 * expected-utility evaluation.
 *
 * @author Manuel Arias
 */
public final class ProbAndUtilityAndStrategy extends DecisionModelEvaluation {

	private final double utility;
	private Strategy strategy;

	public ProbAndUtilityAndStrategy(double prob, double utility) {
		super(prob);
		this.utility = utility;
	}

	public double getUtility() {
		return utility;
	}

	@Override
	public EvaluationType getEvaluationType() {
		return EvaluationType.UNICRITERION;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

}
