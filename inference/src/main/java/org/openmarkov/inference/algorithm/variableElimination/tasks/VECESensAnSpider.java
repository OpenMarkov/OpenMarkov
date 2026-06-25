/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 license
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Sensitivity analysis spider diagram for cost-effectiveness models. For each uncertain
 * parameter, systematically samples the parameter over its uncertainty interval and performs
 * a cost-effectiveness analysis at each sample point.
 *
 * @author jperez-martin
 */
public class VECESensAnSpider extends VariableElimination  {

	private static final Logger logger = LogManager.getLogger(VECESensAnSpider.class);

	private Collection<GTablePotential> ceaResults = null;
	private int iterations;
	private Variable decisionVariable;	
	private List<UncertainParameter> uncertainParameters;	
	private AxisVariation axisVariation;	
	private HashMap<UncertainParameter, List<GTablePotential>> uncertainParametersPotentials;	

	/**
	 * @param network a symmetric network having at least two criteria (and usually decisions and utility nodes)
	 */
	public VECESensAnSpider(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		super(network);		
	}
	
	/**
	 * obtains for each parameter the potentials for each iteration.
	 * Each iteration is a point at which the uncertainty interval is divided 
	 * 
	 */
	private void resolve() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		
		uncertainParametersPotentials = new HashMap<UncertainParameter, List<GTablePotential>>();
		
		ProbNet probnet  =probNet.copy();		
		
		for (UncertainParameter uncertainParameter : uncertainParameters)  {
			
			List<GTablePotential> ceaResults = new ArrayList<>();
			double hMin = axisVariation.getMinValue(uncertainParameter);
			double hMax = axisVariation.getMaxValue(uncertainParameter);
			String iterationVariableName = "***Iteration***";
			
			for (int i = 0; i <= (iterations+1); ++i) {
				ProbNet net = SystematicSampling.networkSample(probnet, uncertainParameter, hMin, hMax,
					iterations, iterationVariableName, i);
                logger.debug("Potentials: {}", net.getPotentials());
                CEAnalysis veEvaluation = new VECEAnalysis(net);
				veEvaluation.setPreResolutionEvidence(getPreResolutionEvidence());
				veEvaluation.setDecisionVariable(this.decisionVariable);
				if(i<=iterations) {
					ceaResults.add(veEvaluation.getUtility());
				}							
			}
			uncertainParametersPotentials.put(uncertainParameter, ceaResults);			
		}
	}		
						
	/**
	 * Gets for a parameter as many potentials as iterations
	 * 
	 **/
	public HashMap<UncertainParameter, List<GTablePotential>> getCEPotentials()
			throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		resolve();
		return uncertainParametersPotentials;
	}
	
	/**
	 * Sets the decision variable for the cost-effectiveness analysis.
	 *
	 * @param decisionSelected the decision variable to condition on
	 */
	public void setDecisionVariable(Variable decisionSelected) {
		this.decisionVariable = decisionSelected;
	}		
	
	/**
	 * Sets the number of sample points (iterations) along the uncertainty interval.
	 *
	 * @param numSimulations number of iterations
	 */
	public void setNumIterations(int numSimulations) {
		this.iterations = numSimulations;
	}
	
	/**
	 * Sets the uncertain parameters to be varied in the sensitivity analysis.
	 *
	 * @param uncertainParameters the uncertain parameters to analyze
	 */
	public void setUncertainParameters(List<UncertainParameter> uncertainParameters) {
		this.uncertainParameters = uncertainParameters;
	}
	
	/**
	 * Sets the axis variation mode that determines how the uncertainty interval is computed.
	 *
	 * @param axisVariation the axis variation strategy
	 */
	public void setAxisVariation(AxisVariation axisVariation) {
		this.axisVariation = axisVariation;		
	}
}