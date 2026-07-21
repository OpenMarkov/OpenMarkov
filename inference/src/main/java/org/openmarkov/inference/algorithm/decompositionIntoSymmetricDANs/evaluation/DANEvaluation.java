/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation;

import java.util.List;

import org.openmarkov.core.inference.tasks.Evaluation;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANInference;

public abstract class DANEvaluation implements Evaluation {
	
	DANInference inferenceProcess;
	
		
	public static boolean hasIncorrectProbability(TablePotential pot) {
		boolean isCorrect = true;
		if (pot != null) {
			double[] values = pot.getValues();
			for (int i = 0; i < values.length && isCorrect; i++) {
				double value = values[i];
				isCorrect = value >= 0.0 && value <= 1.0;
			}
		}
		return !isCorrect;
	}

	public static boolean containsValue(TablePotential pot, double v) {
		boolean containsValue = false;
		if (pot != null) {
			double[] values = pot.getValues();
			for (int i = 0; i < values.length && !containsValue; i++) {
				containsValue = values[i] == v;
			}
		}
		return containsValue;
	}


	@Override public TablePotential getProbability() {
		return inferenceProcess.getProbability();
	}

	@Override public TablePotential getUtility() {
		return (TablePotential) inferenceProcess.getUtility();
	}
	
	@Override
	public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {
		throw new UnsupportedOperationException(
				"setPreResolutionEvidence not implemented for " + getClass().getSimpleName());
	}

	@Override
	public void setConditioningVariables(List<Variable> conditioningVariables) {
		throw new UnsupportedOperationException(
				"setConditioningVariables not implemented for " + getClass().getSimpleName());
	}

	@Override
	public StrategyTree getOptimalStrategyTree() {
		throw new UnsupportedOperationException(
				"getOptimalStrategyTree not implemented for " + getClass().getSimpleName());
	}


	
}
