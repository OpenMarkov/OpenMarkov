/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.MultiStepEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Compound edit that pastes previously copied nodes and links into a network.
 * Duplicate variable names are resolved by appending apostrophes. Potentials
 * are copied and variable references are updated to point to the new nodes.
 */
@SuppressWarnings("serial")
public class PasteEdit extends MultiStepEdit {
    private final SelectedContent clipboardContent;
    private final Point2D.Double centerNodesTo;
    private @Nullable SelectedContent pastedContent;


    /**
     * Creates a new paste edit.
     *
     * @param probNet          the target network to paste into
     * @param clipboardContent the nodes and links to paste
     * @param centerNodesTo
     */
    public PasteEdit(ProbNet probNet, SelectedContent clipboardContent, Point2D.Double centerNodesTo) {
        super(probNet);
        this.clipboardContent = clipboardContent;
        this.centerNodesTo = centerNodesTo;
        this.pastedContent = null;
    }
    
    
    @Override protected void doMultiStepEdit(StepExecuter stepExecuter) throws DoEditException {
        this.newVariables = new HashMap<>();
        // Gather new node creation edits
        List<Node> nodes = this.clipboardContent.nodes();
        double maxX = nodes.stream().mapToDouble(Node::getCoordinateX).max().getAsDouble();
        double minX = nodes.stream().mapToDouble(Node::getCoordinateX).min().getAsDouble();
        double maxY = nodes.stream().mapToDouble(Node::getCoordinateY).max().getAsDouble();
        double minY = nodes.stream().mapToDouble(Node::getCoordinateY).min().getAsDouble();
        Rectangle2D.Double nodesRect = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        Point2D.Double diff = null;
        if (this.centerNodesTo != null) {
            diff = new Point2D.Double(this.centerNodesTo.getX() - nodesRect.getCenterX(), this.centerNodesTo.getY() - nodesRect.getCenterY());
        }
        
        for (Node node : nodes) {
            String oldName = node.getName();
            String newName = oldName;
            while (this.probNet.containsVariable(newName)) {
                newName += "'";
            }
            Variable variable = new Variable(node.getVariable());
            variable.setName(newName);
            this.newVariables.put(oldName, newName);
            Point2D.Double position = new Point2D.Double(node.getCoordinateX() + 7.0, node.getCoordinateY());
            if (diff != null) {
                position = new Point2D.Double(node.getCoordinateX() + diff.getX(), node.getCoordinateY() + diff.getY());
            } else {
                position = new Point2D.Double(node.getCoordinateX() + 7.0, node.getCoordinateY());
            }
            stepExecuter.execute(new AddNodeEdit(this.probNet, variable, node.getNodeType(), position));
        }
        
        
        //Gather link creation edits
        for (Link<Node> link : this.clipboardContent.links()) {
            String originalSourceNodeName = link.getFrom().getName();
            String originalDestinationNodeName = link.getTo().getName();
            AddLinkEdit addLinkEdit = new AddLinkEdit(this.probNet, this.probNet.getVariable(this.newVariables.get(originalSourceNodeName)),
                                                      this.probNet.getVariable(this.newVariables.get(originalDestinationNodeName)), link.isDirected());
            stepExecuter.execute(addLinkEdit);
        }
        
        PNEdit finalizer = new PNEdit(this.probNet) {
            @Override protected void doEdit() {
                // Apply node generation edits
                ArrayList<Node> pastedNodes = new ArrayList<>();
                // Apply link creation edits
                List<Link<Node>> pastedLinks = new ArrayList<>();
                for (PNEdit edit : stepExecuter.currentlyExecutedEdits().toList()) {
                    switch (edit) {
                        case AddNodeEdit addNodeEdit -> pastedNodes.add(addNodeEdit.getNode());
                        case AddLinkEdit linkEdit -> pastedLinks.add(linkEdit.getLink());
                        default -> {
                        }
                    }
                }
                PasteEdit.this.pastedContent = new SelectedContent(pastedNodes, pastedLinks);
                //Replace potentials to already created nodes with copies of copied nodes
                for (Node originalNode : nodes) {
                    ArrayList<Potential> newPotentials = new ArrayList<>();
                    Node newNode = this.probNet.getNode(PasteEdit.this.newVariables.get(originalNode.getName()));
                    for (Potential originalPotential : originalNode.getPotentials()) {
                        Potential potential = originalPotential.copy();
                        List<Variable> externalVars = new ArrayList<>();
                        for (int i = 0; i < potential.getNumVariables(); ++i) {
                            String variableName = potential.getVariable(i).getName();
                            if (PasteEdit.this.newVariables.containsKey(variableName)) {
                                Variable variable = this.probNet.getVariable(PasteEdit.this.newVariables.get(variableName));
                                potential.replaceVariable(i, variable);
                            } else {
                                externalVars.add(potential.getVariable(i));
                            }
                        }
                        for (Variable extVar : externalVars) {
                            potential = potential.removeVariable(extVar);
                        }
                        if (potential instanceof ExactDistrPotential) {
                            Variable child = ((ExactDistrPotential) potential).getChildVariable();
                            if (PasteEdit.this.newVariables.containsKey(child.getName())) {
                                ((ExactDistrPotential) potential)
                                        .setChildVariable(this.probNet.getVariable(PasteEdit.this.newVariables.get(child.getName())));
                            }
                        }
                        newPotentials.add(potential);
                    }
                    newNode.setPotentials(newPotentials);
                    // Copy comment too!
                    newNode.setComment(originalNode.getComment());
                    newNode.setRelevance(originalNode.getRelevance());
                    newNode.setPurpose(originalNode.getPurpose());
                    newNode.setAdditionalProperties(originalNode.getAdditionalProperties());
                }
            }
        };
        stepExecuter.execute(finalizer);
        PNEdit verifier = new PNEdit(probNet) {
            
            @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
                probNet.checkConstraintsIn(constraintChecker);
            }
            
            @Override protected void doEdit() {
            
            }
        };
        stepExecuter.execute(verifier);
        
    }
    
    
    /**
     * Returns the pasted content.
     *
     * @return the pastedContent.
     */
    public SelectedContent getPastedContent() {
        return this.pastedContent;
    }
    
    private HashMap<String, String> newVariables;
}
