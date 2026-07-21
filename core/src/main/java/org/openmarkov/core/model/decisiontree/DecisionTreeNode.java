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
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for nodes in a decision tree.
 * Nodes can be of type CHANCE, DECISION, or UTILITY.
 * 
 * @param <T> The type of utility associated with this node (e.g., Double,
 *            CostEffectiveness).
 */
public abstract non-sealed class DecisionTreeNode<T> implements DecisionTreeElement {

	protected double scenarioProbability = Double.NEGATIVE_INFINITY;
	private Variable variable;
	protected NodeType nodeType;
	protected List<DecisionTreeElement> children;
	private DecisionTreeElement parent = null;
	private ProbNet network;
	/**
	 * This is attribute represents what in the past was the utility for
	 * uni-criteria decision trees, but now it is generalized
	 * to use the same decision tree structure in the case of cost-effectiveness
	 * analysis decision trees.
	 */
	protected T utility;

	public void setUtility(T utility) {
		this.utility = utility;
	}

	public T getUtility() {
		return utility;
	}

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

	@Override
	public double getScenarioProbability() {
		return scenarioProbability;
	}

	/**
	 * Sets the probability of the scenario up to this node.
	 * 
	 * @param scenarioProbability The accumulated probability.
	 */
	public void setScenarioProbability(double scenarioProbability) {
		this.scenarioProbability = scenarioProbability;
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
	 * Copies the state from another node. The children list is duplicated as a new
	 * {@link ArrayList} containing the same element references (shallow copy of the
	 * list, not of the elements). The parent link is reset to {@code null}: callers
	 * are expected to re-attach the copied node to its new place in the tree.
	 *
	 * @param node The node to copy from.
	 */
	public void copy(DecisionTreeNode<T> node) {
		utility = node.utility;
		scenarioProbability = node.scenarioProbability;
		variable = node.variable;
		nodeType = node.getNodeType();
		children = new ArrayList<>(node.children);
		parent = null;
		network = node.network;
	}

	/**
	 * Checks if a specific branch represents the best decision from this node.
	 *
	 * @param treeBranch The branch to check.
	 * @return True if it is the best decision, false otherwise.
	 */
	public abstract boolean isBestDecision(DecisionTreeBranch<T> treeBranch);

	/**
	 * Sets the utility based on a TablePotential.
	 *
	 * @param tablePotential The potential containing utility values.
	 */
	public abstract void setOnlyValueForUtility(Potential tablePotential);

}
