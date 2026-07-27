/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Represents a branch in the decision tree, connecting a parent node to a child
 * node.
 * A branch is typically associated with a specific state of a variable.
 *
 * @param <T> The utility type carried by the surrounding tree (e.g. {@code Double}
 *            for plain expected-utility evaluation, {@code CEP} for cost-effectiveness).
 */
public non-sealed class DecisionTreeBranch<T> implements DecisionTreeElement {

	protected double scenarioProbability = Double.NEGATIVE_INFINITY;
	private final Variable branchVariable;
	private final State branchState;
	private DecisionTreeNode<T> parent;
	private DecisionTreeNode<T> child;
	private final ProbNet probNet;
	private EvidenceCase scenarioEvidence = null;

	/**
	 * Constructor creating a branch associated with a specific variable state.
	 * 
	 * @param probNet        The probabilistic network.
	 * @param branchVariable The variable determining this branch.
	 * @param branchState    The specific state of the variable for this branch.
	 */
	public DecisionTreeBranch(ProbNet probNet, Variable branchVariable, State branchState) {
		this.probNet = probNet;
		this.branchState = branchState;
		this.branchVariable = branchVariable;
	}

	/**
	 * Constructor creating a generic branch without specific variable/state
	 * association.
	 * 
	 * @param probNet The probabilistic network.
	 */
	public DecisionTreeBranch(ProbNet probNet) {
		this(probNet, null, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>A branch has at most one child, so the list is empty until {@link #setChild}
	 * is called and holds exactly that child afterwards. It is never a list whose
	 * single element is {@code null}: every consumer of the tree recurses over the
	 * children of each element, and a {@code null} among them is a
	 * {@link NullPointerException} one frame below.
	 */
	@Override
	public List<DecisionTreeElement> getChildren() {
		return (child == null) ? List.of() : List.of(child);
	}

	/**
	 * Returns the probability of this branch given its parent's scenario.
	 * 
	 * @return The conditional probability of this branch.
	 */
	public double getBranchProbability() {
		double parentScenarioProb = parent.getScenarioProbability();
		return (parentScenarioProb != 0) ? getScenarioProbability() / parentScenarioProb : 0;
	}

	@Override
	public EvidenceCase getBranchStates() {
		if (scenarioEvidence == null) {
			scenarioEvidence = (parent != null) ? new EvidenceCase(parent.getBranchStates()) : new EvidenceCase();
			if (branchVariable != null) {
				try {
					scenarioEvidence.addFinding(new Finding(branchVariable, branchState));
				} catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
					throw new UnreachableException(e);
				}
			}
		}
		return scenarioEvidence;
	}

	/**
	 * Returns the branchVariable.
	 *
	 * @return the branchVariable.
	 */
	public Variable getBranchVariable() {
		return branchVariable;
	}

	/**
	 * Returns the branch state
	 *
	 * @return the branch state
	 */
	public State getBranchState() {
		return branchState;
	}

	@Override
	public double getScenarioProbability() {
		return scenarioProbability;
	}

	/**
	 * Sets the probability of the scenario (path) leading up to and including this
	 * branch.
	 * 
	 * @param scenarioProbability The scenario probability.
	 */
	public void setScenarioProbability(double scenarioProbability) {
		this.scenarioProbability = scenarioProbability;
	}

	/**
	 * Returns the child node attached to this branch.
	 *
	 * @return The child DecisionTreeNode.
	 */
	public DecisionTreeNode<T> getChild() {
		return child;
	}

	/**
	 * Sets the child.
	 *
	 * @param child the child to set.
	 */
	public void setChild(DecisionTreeNode<T> child) {
		this.child = child;
		child.setParent(this);
	}

	@Override
	public String toString() {
		if (branchVariable == null) {
			return "DecisionTreeBranch [root]";
		}
		return "DecisionTreeBranch [branchVariable=" + branchVariable.getName() + ", branchState=" +
				branchState + "]";
	}

	/**
	 * Returns the parent node of this branch.
	 *
	 * @return The parent DecisionTreeNode.
	 */
	public DecisionTreeNode<T> getParent() {
		return parent;
	}

	@Override
	public void setParent(DecisionTreeElement parent) {
		@SuppressWarnings("unchecked")
		DecisionTreeNode<T> typedParent = (DecisionTreeNode<T>) parent;
		this.parent = typedParent;
		// Invalidate the lazily-computed evidence cache: the path to the root has changed.
		this.scenarioEvidence = null;
	}

	/**
	 * Returns the utility of the child node, or {@code null} while the branch has
	 * no child yet — a half-built tree has such branches, and "no utility yet" is
	 * an answer its consumers already understand, where dereferencing the missing
	 * child was a {@code NullPointerException} away from the cause.
	 *
	 * @return The utility value, or {@code null} if the branch has no child.
	 */
	public T getUtility() {
		return (child == null) ? null : child.getUtility();
	}

}
