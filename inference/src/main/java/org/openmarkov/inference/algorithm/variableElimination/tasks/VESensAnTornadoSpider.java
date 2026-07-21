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
import org.openmarkov.core.inference.tasks.SensAnTornadoSpider;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DomainInterval;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author jperez-martin
 */
public class VESensAnTornadoSpider implements SensAnTornadoSpider {

	/**
     * Potentials of each {@code UncertainParameter } conditioned on a dummy
	 * variable
	 */
	private final HashMap<UncertainParameter, TablePotential> uncertainParametersPotentials;
	private final ProbNet probNet;
	private final HashMap<UncertainParameter, DomainInterval> sampledInterval;

	

	public VESensAnTornadoSpider(ProbNet network, EvidenceCase preResolutionEvidence,
			List<UncertainParameter> uncertainParameters, AxisVariation axisVariation, int numberOfIntervals)
			throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		this(network, preResolutionEvidence, uncertainParameters, axisVariation, numberOfIntervals, null);
	}

	public VESensAnTornadoSpider(ProbNet network, EvidenceCase preResolutionEvidence,
			List<UncertainParameter> uncertainParameters, AxisVariation axisVariation, int numberOfIntervals,
								 Variable decisionVariable) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		this.probNet = network.copy();
		uncertainParametersPotentials = new HashMap<>();
		
		sampledInterval = new HashMap<>();

		String iterationVariableName = "***Iteration***";
		for (UncertainParameter uncertainParameter : uncertainParameters) {
            double hMin = axisVariation.getMinValue(uncertainParameter);
			double hMax = axisVariation.getMaxValue(uncertainParameter);
			sampledInterval.put(uncertainParameter, new DomainInterval(hMin,hMax));
			 
			ProbNet sampledProbNet = SystematicSampling.sampleNetwork(this.probNet, uncertainParameter, hMin, hMax,
					numberOfIntervals, iterationVariableName);

			List<Variable> conditioningVariables = new ArrayList<>();
            Variable conditionedVariable = sampledProbNet.getVariable(iterationVariableName);
            conditioningVariables.add(conditionedVariable);
            
            if (decisionVariable != null) {
				conditioningVariables.add(decisionVariable);
			}
            
            VEEvaluation veEvaluation = new VEEvaluation(sampledProbNet);
			veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
			veEvaluation.setConditioningVariables(conditioningVariables);

			// Collect the conditional potential
            
            TablePotential globalUtility = veEvaluation.getUtility();
            
            if (decisionVariable != null && globalUtility != null) {
                globalUtility = globalUtility.reorder(conditioningVariables);
            }
            
            uncertainParametersPotentials.put(uncertainParameter, globalUtility);
		}
	}

	@Override public HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials() {
		return uncertainParametersPotentials;
	}


	@Override
	public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {

	}

	@Override
	public void setConditioningVariables(List<Variable> conditioningVariables) {

	}
	
	public HashMap<UncertainParameter, DomainInterval> getSampledInterval() {
		return sampledInterval;
	}
}
