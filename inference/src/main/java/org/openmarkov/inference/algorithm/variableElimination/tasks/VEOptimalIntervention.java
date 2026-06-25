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
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.inference.tasks.OptimalIntervention;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoMixedParents;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jperez-martin
 */
public class VEOptimalIntervention implements OptimalIntervention {
    
    private final VEEvaluation veEvaluation;
	private final ProbNet probNet;

	/**
	 * @param probNet a network (usually containing decisions and utility nodes)
	 * @throws NotEvaluableNetworkException if the network cannot be evaluated
	 */
	public VEOptimalIntervention(ProbNet probNet) throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {
		this(probNet, new EvidenceCase());
	}

	/**
	 * @param network               a network (usually containing decisions and utility nodes)
	 * @param preResolutionEvidence pre-resolution evidence
	 * @throws NotEvaluableNetworkException if the network cannot be evaluated
	 */
	public VEOptimalIntervention(ProbNet network, EvidenceCase preResolutionEvidence)
			throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {
		probNet = network.copy();

		veEvaluation = new VEEvaluation(network);
		veEvaluation.setPreResolutionEvidence(preResolutionEvidence);
	}

	@Override public StrategyTree getOptimalIntervention()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		TablePotential utility = veEvaluation.getUtility();
		StrategicTablePotential strategic;
		if (utility instanceof StrategicTablePotential stp) {
			strategic = stp;
		} else {
			// Wrap in a StrategicTablePotential so we can assign strategyTrees
			strategic = new StrategicTablePotential(utility.getVariables(), utility.getPotentialRole(), utility.getValues());
		}
		StrategyTree[] strategyTrees = strategic.getStrategyTrees();
		// Create strategy tree if it is empty
		if ((strategyTrees==null)||(strategyTrees.length==0)) {
			List<Variable> decisions = BasicOperations.getAnAdmissibleOrderOfDecisions(probNet);
            if (!decisions.isEmpty()) {
			Variable lastVar = decisions.getLast();
			StrategyTree auxST = new StrategyTree(lastVar,lastVar.getStates());
				for (int i=decisions.size()-2;i>=0;i--) {
					lastVar = decisions.get(i);
					auxST = new StrategyTree(lastVar, Arrays.asList(lastVar.getStates()), auxST);
				}
				strategic.strategyTrees = new StrategyTree[1];
				strategic.strategyTrees[0] = auxST;
			}
		}
		return strategic.getStrategyTrees()[0];
	}

	/**
     * @return A new {@code ArrayList} of {@code PNConstraint}.
	 */
    public static List<PNConstraint> initializeAdditionalConstraints() {
		List<PNConstraint> constraints = new ArrayList<>();
		constraints.add(new NoMixedParents());
		//constraints.add(new NoSuperValueNode());
		return constraints;
	}

	/**
     * @return An {@code ArrayList} of {@code NetworkType} where the
	 * algorithm can be applied: Bayesian networks and influence
	 * diagrams.
	 */
	public List<NetworkType> initializeNetworkTypesApplicable() {
		List<NetworkType> networkTypes = new ArrayList<>();
		networkTypes.add(BayesianNetworkType.getUniqueInstance());
		networkTypes.add(InfluenceDiagramType.getUniqueInstance());
		networkTypes.add(MIDType.getUniqueInstance());
		if (probNet.getNetworkType().equals(DecisionAnalysisNetworkType.getUniqueInstance())) {
			if (!ProbNetOperations.hasOrderAsymmetry(probNet) && !ProbNetOperations.hasStructuralAsymmetry(probNet)) {
				networkTypes.add(DecisionAnalysisNetworkType.getUniqueInstance());
			}
		}
		return networkTypes;
	}

	@Override public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {

	}

	@Override public void setConditioningVariables(List<Variable> conditioningVariables) {

	}
}
