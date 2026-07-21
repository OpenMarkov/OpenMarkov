/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.apache.logging.log4j.LogManager;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeAbsorptionHandler;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;


import java.util.*;
import java.util.stream.Collectors;

/**
 * This edit makes the net absorb a node, merging all the utility children into one and updating its potential.
 * @author iagoparis - summer 2018
 * @version 1.0
 * @since OpenMarkov 0.3
 */

@SuppressWarnings("serial") public class AbsorbNodeEdit extends PNEdit {
    
    
    // Both node and variable attributes are created for convenience but one could be extracted from the other
    private final Variable absorbedVariable;

    private final Node absorbedNode;

    /* Undo attributes */
    // Un-absorb node
    private List<Link<Node>> linksDeleted;

    private final List<Link<Node>> newParentLinks;

    private List<Potential> oldUtilityPotentials;

    // Unmerge utility node
    private boolean utilityNodesMerged;

    private List<Node> oldUtilityChildren;

    private ArrayList<Link<Node>> oldChildrenLinks;

    /* Redo attributes */
    private List<Potential> newPotentials;

    private Node mergedUtility;

    private Set<Node> mergedParents;

    // Constructor

    /**
     * @param probNet    {@code ProbNet}
     * @param absorbedVariable  {@code Variable}
     */
    public AbsorbNodeEdit(ProbNet probNet, Variable absorbedVariable) {
        super(probNet);
        this.absorbedVariable = absorbedVariable;
        this.absorbedNode = probNet.getNode(absorbedVariable);
        this.linksDeleted = new ArrayList<>();
        this.newParentLinks = new ArrayList<>();
        
        LogManager.getLogger(AbsorbNodeEdit.class.getName());
    }

    @Override
    protected void doEdit() throws DoEditException.CannotDoEditException {
        // If there are more than one utility children, merge them into one node
        if (absorbedNode.getChildren().size() > 1) {
            try {
                mergeUtilityChildren();
            } catch (NonProjectablePotentialException e) {
                throw new DoEditException.CannotDoEditException(e, this);
            }
            utilityNodesMerged = true;
        } else {
            utilityNodesMerged = false;
        }
        
        try {
            NodeAbsorptionHandler.absorbNodeConsistently(absorbedNode, absorbedVariable);
        } catch (NonProjectablePotentialException e) {
            throw new DoEditException.CannotDoEditException(e, this);
        }
        // Links saved for the undo()
        linksDeleted = getLinksWithNode(absorbedNode);
        probNet.removeNode(absorbedNode);
    }
    
    private void mergeUtilityChildren() throws NonProjectablePotentialException {

        // Save the old children for undoing
        oldUtilityChildren = absorbedNode.getChildren();
        oldChildrenLinks = new ArrayList<>();
        for (Node child : oldUtilityChildren) {
            oldChildrenLinks.addAll(getLinksWithNode(child));
        }

        /* Create the merged node */
        // Create the name
        String mergedName = oldUtilityChildren.stream().map(Node::getName).collect(Collectors.joining(" + "));
        
        // Get the position for the new node, the gravity center of children (which is the average by coordinate).
        double x = 0;
        double y = 0;
        for (Node child : oldUtilityChildren) {
            x += child.getCoordinateX();
            y += child.getCoordinateY();
        }

        x /= oldUtilityChildren.size(); // Division by 0 is tested in the Validator class.
        y /= oldUtilityChildren.size();

        // Gather all parents of every component node merged
        mergedParents = new HashSet<>();
        for (Node child : oldUtilityChildren) {
            mergedParents.addAll(child.getParents());
        }

        // Create the node
        Variable mergedVariable = new Variable(mergedName.toString());
        mergedUtility = new Node(probNet, mergedVariable, NodeType.UTILITY);
        mergedUtility.setCoordinateX(x);
        mergedUtility.setCoordinateY(y);
        probNet.addNode(mergedUtility);

        // Create the links
        for (Node parent : mergedParents) {
            probNet.addLink(parent, mergedUtility, true);
        }

        /* Create the potential */

        // Get the potential table of every component child
        List<TablePotential> utilityChildrenPotentials = new ArrayList<>();
        for (Node child : oldUtilityChildren) {
            // Change the variable of the component potentials to the merged variable
            TablePotential componentPotential = child.getPotentials().getFirst().getCPT();
            componentPotential.replaceVariable(componentPotential.getVariable(0), mergedVariable);
            // Add the potential to the list to be summed
            utilityChildrenPotentials.add(componentPotential);
        }
        
        // Sum the component potentials
        TablePotential sumPotential = DiscretePotentialOperations.sum(utilityChildrenPotentials);
        mergedUtility.setPotential(sumPotential); // Set the potential to the node

        // Remove the children that merged into the new utility node
        for (Node child : oldUtilityChildren) {
            probNet.removeNode(child);
        }

    }

    @Override public void undo() {
        super.undo();
        probNet.addNode(absorbedNode);
        // Restore deleted links
        if (!linksDeleted.isEmpty()) {
            for (Link<Node> link : linksDeleted) {
                probNet.addLink(link.getFrom(), link.getTo(), true);
            }
        }

        absorbedNode.getChildren().getFirst().setPotentials(oldUtilityPotentials);
        // Destroy created utility links
        if (!newParentLinks.isEmpty()) {
            for (Link<Node> link : newParentLinks) {
                probNet.removeLink(link.getFrom(), link.getTo(), true);
            }
        }

        // If utility children were merged, oldUtilityPotentials contains the potential of the merged utility children
        // and not its component potentials, however, restoring the component nodes will restore their respective
        // potentials ignoring the merged one.
        if (utilityNodesMerged) {
            probNet.removeNode(absorbedNode.getChildren().getFirst());
            // Restore merged nodes
            for (Node utilityChild : oldUtilityChildren) {
                probNet.addNode(utilityChild);
            }
            // Restore their links
            for (Link<Node> link : oldChildrenLinks) {
                probNet.addLink(link.getFrom(), link.getTo(), true);
            }

        }


    }

    @Override public void redo() {
        setTypicalRedo(false);
        super.redo();
        if (utilityNodesMerged) {
            probNet.addNode(mergedUtility);
            // Re-create the links //
            for (Node parent : mergedParents) {
                probNet.addLink(parent, mergedUtility, true);
            }
            // Remove the children that merged into the new utility node
            for (Node child : oldUtilityChildren) {
                probNet.removeNode(child);
            }
        }

        // Re-create utility links
        if (!newParentLinks.isEmpty()) {
            for (Link<Node> link : newParentLinks) {
                probNet.addLink(link.getFrom(), link.getTo(), true);
            }
        }
        absorbedNode.getChildren().getFirst().setPotentials(newPotentials);

        probNet.removeNode(absorbedNode);

    }

    /*
     * Returns all the incoming and outcoming links of a given node.
     */
    private ArrayList<Link<Node>> getLinksWithNode(Node node) {
        ArrayList<Link<Node>> links = new ArrayList<>();
        for (Link<Node> link : probNet.getLinks() ) {
            if (link.contains(node)) {
                links.add(link);
            }
        }
        return links;
    }

}

