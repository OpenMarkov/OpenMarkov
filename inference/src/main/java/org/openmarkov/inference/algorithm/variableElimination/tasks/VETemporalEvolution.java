/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.inference.tasks.TemporalEvolution;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author jperez-martin
 */
public class VETemporalEvolution extends VariableElimination implements TemporalEvolution {

	private HashMap<Variable, TablePotential> temporalEvolution;

	private Variable temporalVariable;

	private Variable decisionVariable;

	/**
	 * @param network The network used in the inference
	 */
	public VETemporalEvolution(ProbNet network, Variable temporalVariable) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		super(network);
		this.temporalVariable = temporalVariable;
	}
	
	private void resolve() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {

		this.probNet = TaskUtilities.expandNetwork(this.probNet, true);

		List<Variable> variablesOfInterest = new ArrayList<>();
		for (int i = 0; i <= probNet.getInferenceOptions().getTemporalOptions().getHorizon(); i++) {
            // if the variable exists in this slice add to variables of intereset
            Variable variableInSlice = this.probNet.getVariable(temporalVariable.getBaseName(), i);
            if (variableInSlice != null) {
                variablesOfInterest.add(variableInSlice);
            }
            
        }

		VEPropagation vePosteriorValues;
		if (decisionVariable != null) {
			vePosteriorValues = new VEPropagation(this.probNet);
			vePosteriorValues.setVariablesOfInterest(variablesOfInterest);
			vePosteriorValues.setPreResolutionEvidence(getPreResolutionEvidence());
			vePosteriorValues.setConditioningVariables(Collections.singletonList(decisionVariable));
		} else {
			vePosteriorValues = new VEPropagation(this.probNet);
			vePosteriorValues.setVariablesOfInterest(variablesOfInterest);
			vePosteriorValues.setPreResolutionEvidence(getPreResolutionEvidence());
		}
		temporalEvolution = vePosteriorValues.getPosteriorValues();
	}

	@Override public HashMap<Variable, TablePotential> getTemporalEvolution()
			throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
		if (temporalEvolution == null) {
			resolve();
		}
		return temporalEvolution;
	}

	@Override public void setDecisionVariable(Variable decisionVariable) {
		this.decisionVariable = decisionVariable;
	}

	@Override public ProbNet getExpandedNetwork() {
		return probNet;
	}
}
