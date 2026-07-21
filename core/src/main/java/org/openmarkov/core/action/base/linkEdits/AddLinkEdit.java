/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.PurposeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.UniformPotential;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a directed or undirected link between two nodes associated to two
 * variables in a {@code ProbNet}
 */
public final class AddLinkEdit extends BaseLinkEdit {
    
    /**
     * Resulting link of addition or removal.
     */
    private Link<Node> link;
    /**
     * The new {@code Potential} of the second node
     */
    private final List<Potential> newPotentials = new ArrayList<>();
    /**
     * parent node
     */
    private final Node nodeFrom;
    /**
     * child node
     */
    private Node nodeTo;
    private boolean updatePotentials;
    
    public void setUpdatePotentials(boolean updatePotentials) {
        this.updatePotentials = updatePotentials;
    }
    
    /**
     * The last {@code Potential} of the second node before the edition
     */
    private List<Potential> oldPotentials;
    
    // Constructor
    public AddLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected) {
        super(probNet, variable1, variable2, isDirected);
        nodeFrom = probNet.getNode(variable1);
        nodeTo = probNet.getNode(variable2);
        this.updatePotentials = true;
        this.isDirected = isDirected;
        this.link = null;
    }
    
    @Override
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(DistinctLinks.class) instanceof DistinctLinks constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            boolean directed = this.isDirected();
            if (!DistinctLinks.checkLink(probNet, node1, node2, directed)) {
                constraintChecker.addException(new ConstraintViolatedException.LinkAlreadyExists(constraint, node1, node2));
            }
        }
        constraintChecker.checkConstraint(MaxNumParents.class, constraint -> {
            if (this.isDirected()) {
                Node node2 = probNet.getNode(this.getVariableTo());
                int numParents = probNet.getNumParents(node2);
                if (numParents >= constraint.getMaxNumParents()) {
                    constraintChecker.addException(new ConstraintViolatedException.NodeCannotHaveMoreParents(constraint, node2, numParents));
                }
            }
        });
        if (probNet.getConstraintOfClass(NoBackwardLink.class) instanceof NoBackwardLink constraint) {
            if (!NoBackwardLink.allowedLink(this.getVariableFrom(), this.getVariableTo())) {
                constraintChecker.addException(new ConstraintViolatedException.CannotAddLinkToAPreviousTimeSlice(constraint, this.getVariableFrom(), this.getVariableTo()));
            }
        }
        if (probNet.getConstraintOfClass(NoCycle.class) instanceof NoCycle constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            if (probNet.existsPath(node2, node1, true, Collections.emptyList())) {
                constraintChecker.addException(new ConstraintViolatedException.ThereIsACycle(constraint, node1, node2));
            }
        }
        if (probNet.getConstraintOfClass(NoLoops.class) instanceof NoLoops constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            if (probNet.existsPath(node2, node1, false, Collections.emptyList())) {
                constraintChecker.addException(new ConstraintViolatedException.ThereIsALoop(constraint, node1, node2));
            }
        }
        if (probNet.getConstraintOfClass(NoMixedParents.class) instanceof NoMixedParents constraint) {
            if (this.isDirected()) {
                Node nodeTo = probNet.getNode(this.getVariableTo());
                if (nodeTo.getNodeType() == NodeType.UTILITY) {
                    Node nodeFrom = probNet.getNode(this.getVariableFrom());
                    var newParentsList = new ArrayList<>(nodeTo.getParents());
                    newParentsList.add(nodeFrom);
                    constraint.checkParents(nodeTo, newParentsList, constraintChecker);
                }
            }
        }
        if (probNet.getConstraintOfClass(NoMultipleLinks.class) instanceof NoMultipleLinks constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            boolean directed = this.isDirected();
            constraint.checkLink(this.getProbNet(), constraintChecker, node1, node2, directed);
        }
        if (probNet.getConstraintOfClass(NoSelfLoop.class) instanceof NoSelfLoop constraint) {
            if (this.getVariableFrom().equals(this.getVariableTo())) {
                constraintChecker.addException(new ConstraintViolatedException.CannotSelfLink(constraint, this.getNodeFrom()));
            }
        }
        if (probNet.getConstraintOfClass(NoSuperValueNode.class) instanceof NoSuperValueNode constraint) {
            if (this.getNodeFrom().getNodeType() == NodeType.UTILITY) {
                constraintChecker.addException(new ConstraintViolatedException.NoSuperValueNodeAllowed(constraint, this.getNodeFrom()));
            }
        }
        if (probNet.getConstraintOfClass(NoUtilityParent.class) instanceof NoUtilityParent constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            if (node1.getNodeType() == NodeType.UTILITY && node2.getNodeType() != NodeType.UTILITY) {
                constraintChecker.addException(new ConstraintViolatedException.CannotHaveUtilityParent(constraint, node1, node2));
            }
        }
        if (probNet.getConstraintOfClass(OnlyDirectedLinks.class) instanceof OnlyDirectedLinks constraint) {
            if (!this.isDirected()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyDirectedLinksAllowed(constraint, this.nodeFrom, List.of(this.nodeTo)));
            }
        }
        if (probNet.getConstraintOfClass(OnlyUndirectedLinks.class) instanceof OnlyUndirectedLinks constraint) {
            if (this.isDirected()) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyUndirectedLinksCannotHaveChildren(constraint, nodeFrom, List.of(nodeTo)));
            }
        }
        if (probNet.getConstraintOfClass(OnlyOneOrphanInitialEvent.class) instanceof OnlyOneOrphanInitialEvent constraint) {
            Node destinationNode = probNet.getNode(this.getVariableTo());
            if ((destinationNode.getPurpose().equals(PurposeType.INITIAL_EVENT.getName()))) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyOneOrphanInitialEventException(constraint, List.of(this.nodeFrom, destinationNode)));
            }
        }
        
        if (probNet.getConstraintOfClass(OnlySelfLoopsWithEventAndChanceNodes.class) instanceof OnlySelfLoopsWithEventAndChanceNodes constraint) {
            Variable originVariable = this.getVariableFrom();
            Variable destinationVariable = this.getVariableTo();
            if ((originVariable.equals(destinationVariable)) && ( (probNet.getNode(originVariable).getNodeType() != NodeType.EVENT) && (probNet.getNode(originVariable).getNodeType() != NodeType.CHANCE ) )){
                constraintChecker.addException(new ConstraintViolatedException.OnlySelfLoopsWithEventAndChanceNodesException(constraint, this.getNodeFrom()));
            }
        }
        
        if (probNet.getConstraintOfClass(ModelNetworkConstraint.class) instanceof ModelNetworkConstraint constraint
                && !constraint.isLinkAdditionAllowed() && !constraint.canEditBeDone(this)) {
            constraintChecker.addException(new ConstraintViolatedException.ModelDoesNotAllowAddingLink(constraint, this.getVariableFrom(), this.getVariableTo()));
        }
        if (probNet.getConstraintOfClass(NoAlwaysObservedDescendantOfDecision.class) instanceof NoAlwaysObservedDescendantOfDecision constraint) {
            List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
            List<Node> alwaysObservedNodes = probNet.getNodes().stream().filter(Node::isAlwaysObserved).toList();
            AddLinkEdit addLinkEdit = this;
            if (addLinkEdit.isDirected()) { // checks constraint
                Node node1 = probNet.getNode(addLinkEdit.getVariableFrom());
                Node node2 = probNet.getNode(addLinkEdit.getVariableTo());
                boolean isAlwaysObserved = node1.isAlwaysObserved();
                if (isAlwaysObserved) {
                    for (Node ancestor : NoAlwaysObservedDescendantOfDecision.ancestorInList(probNet, node1, decisionNodes)) {
                        constraintChecker.addException(new ConstraintViolatedException.AlwaysObservedVariableIsDescendantOfDecisionNode(constraint, node1, ancestor));
                    }
                    for (Node descendant : NoAlwaysObservedDescendantOfDecision.descendantInList(probNet, node2, alwaysObservedNodes)) {
                        constraintChecker.addException(new ConstraintViolatedException.AlwaysObservedVariableIsDescendantOfDecisionNode(constraint, node1, descendant));
                    }
                }
            }
        }
    }
    
    @Override protected void doEdit() {
        probNet.addLink(nodeFrom, nodeTo, isDirected);
        this.link = probNet.getLink(nodeFrom, nodeTo, isDirected);
        if (updatePotentials) {
            this.oldPotentials = nodeTo.getPotentials();
            // TODO Check if this UTILITY label is outdated
            if (nodeTo.getNodeType() == NodeType.UTILITY && nodeTo.onlyNumericalParents()) {
                // Add a default Sum potential to utility supervalue nodes
                for (Potential oldPotential : oldPotentials) {
                    // Update potential
                    List<Variable> variables = oldPotential.getVariables();
                    if (!variables.contains(nodeFrom.getVariable())) {
                        variables.add(nodeFrom.getVariable());
                    }
                    Potential newPotential = new SumPotential(variables, oldPotential.getPotentialRole());
                    newPotentials.add(newPotential);
                }
                
                // 05/04/2020 - There may be self-loops in DESNets for EVENT and CHANCE nodes.
                // Previous code is supposing there is no self loops so methods consider there is no duplicated variables
                // method Potential#addVariable only adds the variable if the variable is not there.
                //To avoid regressions, and keep the changes in previous classes to a minumun, the Uniform Potential is created here.
                //Because of this previous assumption I am also checking the constrain.
                //Currently networks with OnlySelfLoopsWithEventAndChanceNodes only have one potential
            } else if ((nodeFrom == nodeTo ) && ( (nodeFrom.getNodeType() ==NodeType.EVENT) || (nodeFrom.getNodeType() ==NodeType.CHANCE))
                    && nodeFrom.getProbNet().getNetworkType().isApplicableConstraint(new OnlySelfLoopsWithEventAndChanceNodes())){
                for (Potential oldPotential : oldPotentials) {
                    // Update potential
                    List<Variable> variables = oldPotential.getVariables();
                    variables.add(nodeFrom.getVariable());
                    Potential newPotential = new UniformPotential(variables, oldPotential.getPotentialRole());
                    newPotentials.add(newPotential);
                }
                //
                
                
            } else {
                for (Potential oldPotential : oldPotentials) {
                    // Update potential
                    Potential newPotential = oldPotential.addVariable(nodeFrom.getVariable());
                    newPotentials.add(newPotential);
                }
            }
            nodeTo.setPotentials(newPotentials);
        }
    }
    
    @Override public void undo() {
        super.undo();
        
        nodeTo = probNet.getNode(variableTo.getName());
        
        if (updatePotentials) {
            nodeTo.setPotentials(oldPotentials);
        }
        probNet.removeLink(variableFrom, variableTo, isDirected);
    }
    
    /**
     * Method to compare two AddLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     *
     * @param obj AddLinkEdit to be compared
     *
     * @return result of the comparison
     */
    public int compareTo(AddLinkEdit obj) {
        int result;
        
        if ((
                result = variableFrom.getName().compareTo(obj.getVariableFrom().
                                                             getName())
        ) != 0)
            return result;
        if ((
                result = variableTo.getName().compareTo(obj.getVariableTo().
                                                           getName())
        ) != 0)
            return result;
        return 0;
    }
    
    @Override public String getOperationName() {
        return "Add link";
    }
    
    /**
     * Gets the first {@code Node} object in the link.
     *
     * @return the first {@code Node} object in the link.
     */
    public Node getNodeFrom() {
        return nodeFrom;
    }
    
    /**
     * Gets the second {@code Node} object in the link.
     *
     * @return the second {@code Node} object in the link.
     */
    public Node getNodeTo() {
        return nodeTo;
    }
    
    /**
     * Returns the link.
     *
     * @return the link.
     */
    public Link<Node> getLink() {
        return link;
    }
    
    @Override public BaseLinkEdit getUndoEdit() {
        return new RemoveLinkEdit(getProbNet(), getVariableFrom(), getVariableTo(), isDirected());
    }
}
