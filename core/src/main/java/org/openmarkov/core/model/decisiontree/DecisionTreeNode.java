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
import java.util.List;

/**
 * A node of a decision tree.
 * Nodes can be of type CHANCE, DECISION, or UTILITY.
 */
public non-sealed class DecisionTreeNode implements DecisionTreeElement {

	private Variable variable;
	private NodeType nodeType;
	private List<DecisionTreeElement> children;
	private DecisionTreeElement parent = null;
	private ProbNet network;
	/** The only place where the probability and the utility of this node are kept. */
	private DecisionTreeNodeEvaluation evaluation;

	/**
	 * Constructor creating a decision tree node from a network node.
	 * 
	 * @param node The network node (variable + potential).
	 */
	public DecisionTreeNode(Node node) {
		this.variable = node.getVariable();
		this.nodeType = node.getNodeType();
		children = new ArrayList<>();
	}

	/**
	 * Constructor creating a decision tree node from a network node and the network
	 * itself.
	 * 
	 * @param node    The network node.
	 * @param network The probabilistic network.
	 */
	public DecisionTreeNode(Node node, ProbNet network) {
		this(node);
		this.network = network;
	}

	/**
	 * Constructor creating a decision tree node from a variable and the network.
	 * 
	 * @param variable The variable associated with the node.
	 * @param probNet  The probabilistic network.
	 */
	public DecisionTreeNode(Variable variable, ProbNet probNet) {
		this(probNet.getNode(variable), probNet);
	}

	/**
	 * Returns the evaluation attached to this node, or {@code null} if the node
	 * has not been evaluated yet.
	 *
	 * @return the evaluation of this node.
	 */
	public DecisionTreeNodeEvaluation getEvaluation() {
		return evaluation;
	}

	/**
	 * Attaches the evaluation of this node.
	 *
	 * @param evaluation the evaluation of this node.
	 */
	public void setEvaluation(DecisionTreeNodeEvaluation evaluation) {
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
	 * Returns the children.
	 *
	 * @return the children.
	 */
	@Override
	public List<DecisionTreeElement> getChildren() {
		return children;
	}

	@Override
	public EvidenceCase getBranchStates() {
		return (parent != null) ? parent.getBranchStates() : new EvidenceCase();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>It is the probability kept in the evaluation of this node, or
	 * {@link Double#NEGATIVE_INFINITY} if the node has not been evaluated yet.
	 */
	@Override
	public double getScenarioProbability() {
		return (evaluation == null) ? Double.NEGATIVE_INFINITY : evaluation.getProb();
	}

	/**
	 * Adds a child element to this node.
	 * 
	 * @param child The child element (usually a DecisionTreeBranch).
	 */
	public void addChild(DecisionTreeElement child) {
		child.setParent(this);
		children.add(child);
	}

	@Override
	public String toString() {
		return "DecisionTreeNode [variable=" +
				variable.getName() +
				", children=" + children +
				"]";
	}

	@Override
	public void setParent(DecisionTreeElement parent) {
		this.parent = parent;
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
	 * The children list is duplicated as a new {@link ArrayList} containing the
	 * same element references (shallow copy of the list, not of the elements), and
	 * each child is re-parented to this node — otherwise the children would keep
	 * pointing at the source, an orphan outside the tree, and the path evidence of
	 * the grafted subtree would be computed through it. The parent link is left
	 * untouched for the same reason: resetting it cut this node off from the path
	 * above, and its accumulated evidence came back empty.
	 *
	 * @param node The node to copy from.
	 */
	public void copy(DecisionTreeNode node) {
		variable = node.variable;
		nodeType = node.getNodeType();
		evaluation = node.evaluation;
		children = new ArrayList<>(node.children);
		for (DecisionTreeElement child : children) {
			child.setParent(this);
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
				|| !(evaluationOfTheChild(treeBranch) instanceof UnicritNodeEvaluation candidate)) {
			return false;
		}
		for (DecisionTreeElement otherBranch : children) {
			if (evaluationOfTheChild((DecisionTreeBranch) otherBranch) instanceof UnicritNodeEvaluation other
					&& !(candidate.getUtility() >= other.getUtility())) {
				return false;
			}
		}
		return true;
	}

	private static DecisionTreeNodeEvaluation evaluationOfTheChild(DecisionTreeBranch branch) {
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
