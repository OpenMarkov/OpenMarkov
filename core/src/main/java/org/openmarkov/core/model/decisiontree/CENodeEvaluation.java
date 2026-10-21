/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.model.network.CEP;

/**
 * The evaluation of a node in a cost-effectiveness tree, where the value of a scenario is not a
 * number but a partition of the willingness to pay into intervals, each with its own optimal
 * intervention.
 *
 * @author Manuel Arias
 */
public final class CENodeEvaluation extends DecisionTreeNodeEvaluation {

	private final CEP cep;

	public CENodeEvaluation(double prob, CEP cep) {
		super(prob);
		this.cep = cep;
	}

	public CEP getCep() {
		return cep;
	}

	@Override
	public EvaluationType getEvaluationType() {
		return EvaluationType.CE;
	}

}
