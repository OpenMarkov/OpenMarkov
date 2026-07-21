/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.tasks.SensAnMap;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author jperez-martin
 */
public class VESensAnMap implements SensAnMap {
	private final HashMap<UncertainParameter, TablePotential> uncertainParametersPotentials;
	private final ProbNet probNet;

	public VESensAnMap(ProbNet probNet, EvidenceCase preResolutionEvidence, UncertainParameter hUncertainParameter,
			AxisVariation hAxisVariation, UncertainParameter vUncertainParameter, AxisVariation vAxisVariation,
					   int numberOfIntervals) throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {
		this(probNet, preResolutionEvidence, hUncertainParameter, hAxisVariation, vUncertainParameter, vAxisVariation,
				numberOfIntervals, null);
	}

	public VESensAnMap(ProbNet probNet, EvidenceCase preResolutionEvidence, UncertainParameter hUncertainParameter,
			AxisVariation hAxisVariation, UncertainParameter vUncertainParameter, AxisVariation vAxisVariation,
			int numberOfIntervals, Variable decisionVariable)
			throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {

		this.probNet = probNet.copy();
		uncertainParametersPotentials = new HashMap<>();

		String iterationFirstVariableName = "***Iteration***";
		String iterationSecondVariableName = "***Iteration2***";
        double hMin = hAxisVariation.getMinValue(hUncertainParameter);
		double hMax = hAxisVariation.getMaxValue(hUncertainParameter);
		double vMin = vAxisVariation.getMinValue(vUncertainParameter);
		double vMax = vAxisVariation.getMaxValue(vUncertainParameter);

		ProbNet sampledProbNet = SystematicSampling
				.sampleNetwork(this.probNet, hUncertainParameter, hMin, hMax, vUncertainParameter, vMin, vMax,
						numberOfIntervals, iterationFirstVariableName, iterationSecondVariableName);

		List<Variable> variablesConditioning = new ArrayList<>();
        variablesConditioning.add(sampledProbNet.getVariable(iterationFirstVariableName));
        variablesConditioning.add(sampledProbNet.getVariable(iterationSecondVariableName));
        
        if (decisionVariable != null) {
			variablesConditioning.add(decisionVariable);
		}
        
        VEEvaluation veEvaluation = new VEEvaluation(sampledProbNet);
		veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
		veEvaluation.setConditioningVariables(variablesConditioning);

		// Collect the conditional potential
		TablePotential globalUtility = null;
            globalUtility = veEvaluation.getUtility();
            if (decisionVariable != null && globalUtility != null) {
				globalUtility = globalUtility.reorder(variablesConditioning);
			}
        uncertainParametersPotentials.put(hUncertainParameter, globalUtility);
	}

	@Override public HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials() {
		return uncertainParametersPotentials;
	}

	@Override public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {

	}

	@Override public void setConditioningVariables(List<Variable> conditioningVariables) {

	}
}
