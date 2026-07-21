/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeBranchPanel;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeElementPanel;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeNodePanel;

import java.util.HashSet;
import java.util.Set;

/**
 * Adapter model for visualizing decision trees in Swing components (JTree).
 * It transforms the {@link DecisionTreeElement} logical structure into GUI panels.
 */
public class DecisionTreeModel implements TreeModel {

	/** Listeners for changes in the tree structure. */
	private final Set<TreeModelListener> listeners;

	/** Root node of the tree panel hierarchy. */
	private final DecisionTreeElementPanel root;

	/**
	 * Constructor for DecisionTreeModel.
	 * @param root The base element of the logical model.
	 */
	public DecisionTreeModel(DecisionTreeElement root) {
		super();
		this.listeners = new HashSet<>();
		this.root = buildPanelTree(root);
	}
    
	/**
     * Recursively builds the panel hierarchy (GUI) from the logical model.
     * @param treeElement The logical element to process.
     * @return The corresponding panel (Node or Branch) with its children linked.
     */
	private static DecisionTreeElementPanel buildPanelTree(DecisionTreeElement treeElement) {
		DecisionTreeElementPanel treeElementPanel = switch (treeElement) {
			case DecisionTreeBranch decisionTreeBranch -> new DecisionTreeBranchPanel(decisionTreeBranch);
			case DecisionTreeNode decisionTreeNode -> new DecisionTreeNodePanel(decisionTreeNode);
		};
		for (DecisionTreeElement child : treeElement.getChildren()) {
			treeElementPanel.addChild(buildPanelTree(child));
		}
		return treeElementPanel;
	}

	/** {@inheritDoc} */
	@Override public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	/** {@inheritDoc} */
	@Override public Object getChild(Object parent, int index) {
		return ((DecisionTreeElementPanel) parent).getChildren().get(index);
	}

	/** {@inheritDoc} */
	@Override public int getChildCount(Object parent) {
		return ((DecisionTreeElementPanel) parent).getChildren().size();
	}

	/** {@inheritDoc} */
	@Override public int getIndexOfChild(Object parent, Object child) {
		return ((DecisionTreeElementPanel) parent).getChildren().indexOf(child);
	}

	/** {@inheritDoc} */
	@Override public Object getRoot() {
		return root;
	}

	/** {@inheritDoc} */
	@Override public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	/** {@inheritDoc} */
	@Override public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	/** {@inheritDoc} */
	@Override public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}
}