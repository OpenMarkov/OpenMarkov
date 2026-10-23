/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.model.network.potential.StrategyTree;

/**
 * The evaluation of a node in a tree whose utility is a single number, as in ordinary
 * expected-utility evaluation.
 *
 * @author Manuel Arias
 */
public final class ProbAndUtilityAndStrategy extends DecisionModelEvaluation {

	private final double utility;
	private final StrategyTree strategy;

	public ProbAndUtilityAndStrategy(double prob, double utility) {
		this(prob, utility, null);
	}

	/**
	 * @param strategy the optimal strategy of the subproblem; {@code null} if there is nothing to decide in it.
	 */
	public ProbAndUtilityAndStrategy(double prob, double utility, StrategyTree strategy) {
		super(prob);
		this.utility = utility;
		this.strategy = strategy;
	}

	public double getUtility() {
		return utility;
	}

	@Override
	public EvaluationType getEvaluationType() {
		return EvaluationType.UNICRITERION;
	}

	/** The optimal strategy of the subproblem, or {@code null} if it is unknown or there is nothing to decide. */
	public StrategyTree getStrategy() {
		return strategy;
	}

}
