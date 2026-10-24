/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.tasks.GenerateDecisionTree;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Builds the decision tree of an influence diagram or a DAN with {@link DecisionTreeExpansion},
 * as a unicriterion or a cost-effectiveness analysis according to the inference options of the network.
 *
 * @author Manuel Arias
 */
public class DecisionTreeExpansionGenerateDecisionTree implements GenerateDecisionTree {

	private final ProbNet probNet;
	private final int depth;
	private EvidenceCase evidence = new EvidenceCase();

	/**
	 * @param probNet the decision model; the tree is built on a copy of it.
	 * @param depth   how many levels to expand.
	 */
	public DecisionTreeExpansionGenerateDecisionTree(ProbNet probNet, int depth) {
		this.probNet = probNet.copy();
		this.depth = depth;
	}

	/** The evidence of the branch the tree starts from; {@code null} means none. */
	@Override
	public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {
		this.evidence = preresolutionEvidence != null ? preresolutionEvidence : new EvidenceCase();
	}

	@Override
	public void setConditioningVariables(List<Variable> conditioningVariables) {
		throw new UnsupportedOperationException(
				"setConditioningVariables not implemented for " + getClass().getSimpleName());
	}

	@Override
	public DecisionTreeNode getDecisionTree() throws NotEvaluableNetworkException, IncompatibleEvidenceException,
			NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		boolean isUnicriterion = probNet.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType()
				== MulticriteriaOptions.Type.UNICRITERION;
		return new DecisionTreeExpansion(isUnicriterion ? EvaluationType.UNICRITERION : EvaluationType.CE)
				.expandTree(probNet, evidence, depth);
	}
}
