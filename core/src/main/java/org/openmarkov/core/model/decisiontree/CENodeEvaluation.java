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
public class CENodeEvaluation extends DecisionTreeNodeEvaluation {

	private CEP cep;

	public CEP getCep() {
		return cep;
	}

	public void setCep(CEP cep) {
		this.cep = cep;
	}



}
