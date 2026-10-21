/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Represents a branch in the decision tree, connecting a parent node to a child
 * node. A branch is a state of the variable of its parent node, and keeps the
 * probability of that state given the path down to the parent.
 * <p>
 * The only branch without a state is the one the interface hangs the root node
 * from: it has a child but no parent.
 */
public non-sealed class DecisionTreeBranch implements DecisionTreeElement {

	private final State branchState;
	/** Probability of the state of this branch given the path down to its parent; NaN until it is set. */
	private double branchProbability = Double.NaN;
	private DecisionTreeNode parent;
	private DecisionTreeNode child;

	/**
	 * Constructor creating a branch for a state of the variable of the node it
	 * will hang from.
	 *
	 * @param branchState The state of the variable of the parent node.
	 */
	public DecisionTreeBranch(State branchState) {
		this.branchState = branchState;
	}

	/**
	 * Constructor creating the branch without a state that holds the root node.
	 */
	public DecisionTreeBranch() {
		this(null);
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
	 * Returns the probability of the state of this branch given the path down to
	 * its parent, as the algorithm that built the tree set it.
	 *
	 * @return The conditional probability of this branch, or NaN if it has not been set.
	 */
	public double getBranchProbability() {
		return branchProbability;
	}

	/**
	 * Sets the probability of the state of this branch given the path down to its
	 * parent.
	 *
	 * @param branchProbability The conditional probability of this branch.
	 */
	public void setBranchProbability(double branchProbability) {
		this.branchProbability = branchProbability;
	}

	/**
	 * Returns the variable whose state this branch is, which is the variable of
	 * its parent node.
	 *
	 * @return the variable of the parent node, or {@code null} if the branch does not hang from a node yet.
	 */
	public Variable getBranchVariable() {
		return (parent == null) ? null : parent.getVariable();
	}

	/**
	 * Returns the branch state
	 *
	 * @return the branch state
	 */
	public State getBranchState() {
		return branchState;
	}

	/**
	 * Returns the child node attached to this branch.
	 *
	 * @return The child DecisionTreeNode.
	 */
	public DecisionTreeNode getChild() {
		return child;
	}

	/**
	 * Hangs a node from this branch.
	 *
	 * @param child the child to set.
	 */
	public void setChild(DecisionTreeNode child) {
		this.child = child;
		child.setParentBranch(this);
	}

	@Override
	public String toString() {
		if (branchState == null) {
			return "DecisionTreeBranch [root]";
		}
		Variable branchVariable = getBranchVariable();
		return "DecisionTreeBranch [branchVariable=" + (branchVariable == null ? null : branchVariable.getName())
				+ ", branchState=" + branchState + "]";
	}

	/**
	 * Returns the parent node of this branch.
	 *
	 * @return The parent DecisionTreeNode.
	 */
	public DecisionTreeNode getParent() {
		return parent;
	}

	/** Called by {@link DecisionTreeNode#addChild}, which is the only way to hang a branch from a node. */
	void setParent(DecisionTreeNode parent) {
		this.parent = parent;
	}

}
