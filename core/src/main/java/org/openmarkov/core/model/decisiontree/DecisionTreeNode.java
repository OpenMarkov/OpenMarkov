/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node of a decision tree.
 * Nodes can be of type CHANCE, DECISION, or UTILITY.
 * <p>
 * A node hangs from one branch, its parent branch, and has one branch per
 * state of its variable, its child branches; only branches hang from a node.
 */
public non-sealed class DecisionTreeNode implements DecisionTreeVisualNode {

	private Variable variable;
	private NodeType nodeType;
	private List<DecisionTreeBranch> childBranches;
	private DecisionTreeBranch parentBranch = null;
	private ProbNet network;
	/** The evidence the algorithm evaluated this node with: what has been observed on the path down to it. */
	private final EvidenceCase evidence;
	/** The only place where the probability and the utility of this node are kept. */
	private DecisionModelEvaluation evaluation;

	/**
	 * Constructor creating a decision tree node from a network node, with no
	 * evidence.
	 *
	 * @param node The network node (variable + potential).
	 */
	public DecisionTreeNode(Node node) {
		this(node, null, null);
	}

	/**
	 * Constructor creating a decision tree node from a network node and the network
	 * itself, with no evidence.
	 *
	 * @param node    The network node.
	 * @param network The probabilistic network.
	 */
	public DecisionTreeNode(Node node, ProbNet network) {
		this(node, network, null);
	}

	/**
	 * Constructor creating a decision tree node from a network node, the network
	 * itself and the evidence it is evaluated with.
	 *
	 * @param node     The network node.
	 * @param network  The probabilistic network.
	 * @param evidence What has been observed on the path down to this node; {@code null} means nothing.
	 */
	public DecisionTreeNode(Node node, ProbNet network, EvidenceCase evidence) {
		this.variable = node.getVariable();
		this.nodeType = node.getNodeType();
		this.network = network;
		// A copy, so that what the algorithm does next with its own evidence does not change the node's;
		// copying null gives an empty evidence
		this.evidence = new EvidenceCase(evidence);
		childBranches = new ArrayList<>();
	}

	/**
	 * Constructor creating a decision tree node from a variable and the network,
	 * with no evidence.
	 *
	 * @param variable The variable associated with the node.
	 * @param probNet  The probabilistic network.
	 */
	public DecisionTreeNode(Variable variable, ProbNet probNet) {
		this(variable, probNet, null);
	}

	/**
	 * Constructor creating a decision tree node from a variable, the network and
	 * the evidence it is evaluated with.
	 *
	 * @param variable The variable associated with the node.
	 * @param probNet  The probabilistic network.
	 * @param evidence What has been observed on the path down to this node; {@code null} means nothing.
	 */
	public DecisionTreeNode(Variable variable, ProbNet probNet, EvidenceCase evidence) {
		this(probNet.getNode(variable), probNet, evidence);
	}

	/**
	 * Returns the evaluation attached to this node, or {@code null} if the node
	 * has not been evaluated yet.
	 *
	 * @return the evaluation of this node.
	 */
	public DecisionModelEvaluation getEvaluation() {
		return evaluation;
	}

	/**
	 * Attaches the evaluation of this node.
	 *
	 * @param evaluation the evaluation of this node.
	 */
	public void setEvaluation(DecisionModelEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	/**
	 * Returns the variable.
	 *
	 * @return the Variable.
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * Returns the type of the node.
	 *
	 * @return The node type (CHANCE, DECISION, UTILITY).
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>They are the child branches of this node, and the list cannot be
	 * modified: branches are added with {@link #addChild}.
	 */
	@Override
	public List<DecisionTreeVisualNode> getChildren() {
		return Collections.unmodifiableList(childBranches);
	}

	/**
	 * Returns the branches that hang from this node, one per state of its variable.
	 *
	 * @return the child branches, in a list that cannot be modified.
	 */
	public List<DecisionTreeBranch> getChildBranches() {
		return Collections.unmodifiableList(childBranches);
	}

	/**
	 * Returns the branch this node hangs from.
	 *
	 * @return the parent branch, or {@code null} if the node does not hang from a branch yet.
	 */
	public DecisionTreeBranch getParentBranch() {
		return parentBranch;
	}

	/**
	 * Returns the evidence the algorithm evaluated this node with: what has been
	 * observed on the path down to it. A decision taken on the path is not
	 * there, because it is already imposed on the network of the node.
	 *
	 * @return the evidence of this node; empty if it was created without evidence.
	 */
	public EvidenceCase getEvidence() {
		return evidence;
	}

	/**
	 * Returns the probability of the scenario leading to this node. It is the
	 * probability kept in the evaluation of this node, or
	 * {@link Double#NEGATIVE_INFINITY} if the node has not been evaluated yet.
	 *
	 * @return The scenario probability.
	 */
	public double getScenarioProbability() {
		return (evaluation == null) ? Double.NEGATIVE_INFINITY : evaluation.getProb();
	}

	/**
	 * Whether the scenario leading to this node cannot happen, because its
	 * probability is 0. Such a node has no utility to show and is not expanded.
	 *
	 * @return true if this node has been evaluated and its scenario has probability 0.
	 */
	public boolean isImpossible() {
		return evaluation != null && evaluation.getProb() == 0;
	}

	/**
	 * Whether the tree can still go on from this node: it has been evaluated, has
	 * no branches, and is neither a utility node, where the tree ends, nor a
	 * scenario that cannot happen.
	 *
	 * @return true if this node is a leaf that can be expanded.
	 */
	public boolean canBeExpanded() {
		return evaluation != null && childBranches.isEmpty() && nodeType != NodeType.UTILITY && !isImpossible();
	}

	/**
	 * Hangs a branch from this node.
	 *
	 * @param child The branch.
	 */
	public void addChild(DecisionTreeBranch child) {
		child.setParent(this);
		childBranches.add(child);
	}

	/**
	 * Sets on each child branch the probability of its state given the path down
	 * to this node: the probability of the scenario of its child divided by the
	 * one of this node, or 0 when the one of this node is 0. The algorithms that
	 * build the tree call it once this node and the children of its branches have
	 * their evaluations.
	 */
	public void setBranchProbabilitiesFromEvaluations() {
		double probability = getScenarioProbability();
		for (DecisionTreeBranch branch : childBranches) {
			branch.setBranchProbability(
					(probability != 0) ? branch.getChild().getScenarioProbability() / probability : 0);
		}
	}

	@Override
	public String toString() {
		return "DecisionTreeNode [variable=" +
				variable.getName() +
				", children=" + childBranches +
				"]";
	}

	/** Called by {@link DecisionTreeBranch#setChild}, which is the only way to hang a node from a branch. */
	void setParentBranch(DecisionTreeBranch parentBranch) {
		this.parentBranch = parentBranch;
	}

	/**
	 * Returns the probabilistic network associated with this node.
	 *
	 * @return The ProbNet.
	 */
	public ProbNet getNetwork() {
		return network;
	}

	/**
	 * Copies the state from another node into this one, keeping this node's place
	 * in the tree. Its one use is grafting a freshly built subtree onto a leaf of
	 * an existing tree: the leaf takes the subtree root's content and stays where
	 * it hangs.
	 * <p>
	 * The list of child branches is duplicated as a new {@link ArrayList}
	 * containing the same branches (shallow copy of the list, not of the
	 * branches), and each branch is re-parented to this node — otherwise the
	 * branches would keep pointing at the source, an orphan outside the tree. The
	 * parent branch and the evidence are left untouched for the same reason: they
	 * are this node's place in the tree.
	 *
	 * @param node The node to copy from.
	 */
	public void copy(DecisionTreeNode node) {
		variable = node.variable;
		nodeType = node.getNodeType();
		evaluation = node.evaluation;
		childBranches = new ArrayList<>(node.childBranches);
		for (DecisionTreeBranch branch : childBranches) {
			branch.setParent(this);
		}
		network = node.network;
	}

	/**
	 * Checks if a specific branch represents the best decision from this node: in
	 * a decision node, the branch whose child has the highest utility. In
	 * cost-effectiveness analysis there is no single best decision, because the
	 * optimal choice depends on the willingness-to-pay threshold. A branch whose
	 * child has not been evaluated yet is not the best one either, and the other
	 * branches are compared without it: the interface asks while painting, and a
	 * half-built tree has such branches.
	 *
	 * @param treeBranch The branch to check.
	 * @return True if it is the best decision, false otherwise.
	 */
	public boolean isBestDecision(DecisionTreeBranch treeBranch) {
		if (nodeType != NodeType.DECISION
				|| !(evaluationOfTheChild(treeBranch) instanceof ProbAndUtilityAndStrategy candidate)) {
			return false;
		}
		for (DecisionTreeBranch otherBranch : childBranches) {
			if (evaluationOfTheChild(otherBranch) instanceof ProbAndUtilityAndStrategy other
					&& !(candidate.getUtility() >= other.getUtility())) {
				return false;
			}
		}
		return true;
	}

	private static DecisionModelEvaluation evaluationOfTheChild(DecisionTreeBranch branch) {
		DecisionTreeNode child = branch.getChild();
		return (child == null) ? null : child.getEvaluation();
	}

	/**
	 * Whether the utility of this node is a single number or a cost-effectiveness function of the
	 * willingness to pay, as its evaluation says.
	 *
	 * @return the kind of evaluation, or {@code null} if the node has not been evaluated yet.
	 */
	public EvaluationType getEvaluationType() {
		return (evaluation == null) ? null : evaluation.getEvaluationType();
	}

}
