/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.UsesVariable;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit that removes a node from a Markov network, marrying (connecting) all its
 * former siblings to maintain the correct graph structure.
 */
@SuppressWarnings("serial") public class RemoveMarkovNetNodeEdit extends PNEdit implements UsesVariable {
    
    private final Node node;
    
    public RemoveMarkovNetNodeEdit(ProbNet probNet, Node node) {
        super(probNet);
        this.node = node;
    }
    
    @Override public Variable getVariable() {
        return node.getVariable();
    }
    
    @Override protected void doEdit() {
        List<Node> siblings = new ArrayList<Node>(node.getSiblings());
        Variable variableToDelete = node.getVariable();
        for (Node sibling : siblings) {
            probNet.removeLink(node, sibling, false);
        }
        probNet.removeNode(probNet.getNode(variableToDelete));
        // marry siblings
        probNet.marry(new ArrayList<Node>(siblings));
    }
}
