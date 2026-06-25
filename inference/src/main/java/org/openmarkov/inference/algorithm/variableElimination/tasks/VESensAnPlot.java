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
import org.openmarkov.core.inference.tasks.SensAnPlot;
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
public class VESensAnPlot implements SensAnPlot {

	private final HashMap<UncertainParameter, TablePotential> uncertainParametersPotentials;
	private final ProbNet probNet;

	/**
	 * @param probNet The network used in the inference
	 * @throws NotEvaluableNetworkException if the network cannot be evaluated
	 */
	public VESensAnPlot(ProbNet probNet, EvidenceCase preResolutionEvidence, UncertainParameter uncertainParameter,
			AxisVariation axisVariation, int numberOfIntervals)
			throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		this(probNet, preResolutionEvidence, uncertainParameter, axisVariation, numberOfIntervals, null);
	}

	public VESensAnPlot(ProbNet probNet, EvidenceCase preResolutionEvidence, UncertainParameter uncertainParameter,
			AxisVariation axisVariation, int numberOfIntervals, Variable decisionVariable)
			throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		this.probNet = probNet.copy();
		uncertainParametersPotentials = new HashMap<>();

		String iterationVariableName = "***Iteration***";
        double hMin = axisVariation.getMinValue(uncertainParameter);
		double hMax = axisVariation.getMaxValue(uncertainParameter);
		ProbNet sampledProbNet = SystematicSampling
				.sampleNetwork(this.probNet, uncertainParameter, hMin, hMax, numberOfIntervals, iterationVariableName);

		List<Variable> variablesConditioning = new ArrayList<>();
        Variable conditionedVariable = sampledProbNet.getVariable(iterationVariableName);
        variablesConditioning.add(conditionedVariable);
        
        if (decisionVariable != null) {
			variablesConditioning.add(decisionVariable);
		}
        
        VEEvaluation veEvaluation = new VEEvaluation(sampledProbNet);
		veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
		veEvaluation.setConditioningVariables(variablesConditioning);

		// Collect the conditional potential
        
        TablePotential globalUtility = veEvaluation.getUtility();
        
        if (decisionVariable != null && globalUtility != null) {
            globalUtility = globalUtility.reorder(variablesConditioning);

        }
        
        uncertainParametersPotentials.put(uncertainParameter, globalUtility);

	}

	@Override public HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials() {
		return uncertainParametersPotentials;
	}

	@Override public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {

	}

	@Override public void setConditioningVariables(List<Variable> conditioningVariables) {

	}
}
