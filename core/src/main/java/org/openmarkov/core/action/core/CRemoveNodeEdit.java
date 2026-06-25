/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;

import java.util.List;
import java.util.ArrayList;

/**
 * {@code CRemoveNodeEdit} is an compound edit that removes a node
 * performing this steps:
 * <ol>
 * <li>Remove links between the node and its children
 * <li> Remove links between the node and its children
 * <li> Removes the node
 * </ol>
 */
@SuppressWarnings("serial") public class CRemoveNodeEdit extends CompoundPNEdit { //implements UsesVariable{

	// Attributes
    
    protected final Node node;

	protected final NodeType nodeType;

	protected List<Node> parents;

	protected List<Node> children;

	protected List<Node> siblings;

	protected List<Potential> marginalizedPotentials;

	protected List<Potential> allPotentials;

	// Constructor

	/**
	 * @param probNet {@code ProbNet}
	 * @param node    {@code Node}
	 */
	public CRemoveNodeEdit(ProbNet probNet, Node node) {
		super(probNet);
		this.probNet = probNet;
		this.node = node;
		this.nodeType = node.getNodeType();
	}
    
    @Override public ArrayList<PNEdit> generateEdits() {
		// gets neighbors of this node
        ArrayList<PNEdit> edits = new ArrayList<>();
		parents = probNet.getParents(node);
		children = probNet.getChildren(node);

		for (Node parent : parents) {
			String name = parent.getName();
            edits.add(new RemoveLinkEdit(node.getProbNet(), probNet.getVariable(name),
                                         probNet.getVariable(node.getName()), true));
        }
		for (Node child : children) {
            edits.add(new RemoveLinkEdit(node.getProbNet(), probNet.getVariable(node.getName()),
                    probNet.getVariable(child.getName()), true));
        }
        
        // add an edit to remove the variable
        edits.add(new RemoveNodeEdit(probNet, node));
        
        return edits;
    }

	/**
	 * @return variable {@code Variable}
	 */
	public Variable getVariable() {
		return node.getVariable();
	}

	/**
	 * @return {@code String}
	 */
	public String toString() {
        return "CompoundRemoveNodeEdit: " + node.getName();
	}

}