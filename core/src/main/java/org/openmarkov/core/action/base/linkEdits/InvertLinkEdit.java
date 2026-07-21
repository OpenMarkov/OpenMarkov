/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inverts an existing link.
 */
@SuppressWarnings("serial") public final class InvertLinkEdit extends BaseLinkEdit {
    
    /**
     * parent node
     */
    private final Node parent;
    /**
     * child node
     */
    private final Node child;
    
    /**
     * Parent node's old potentials
     */
    private List<Potential> parentOldPotentials;
    /**
     * Child node's old potentials
     */
    private List<Potential> childOldPotentials;
    
    // Constructor
    
    /**
     * @param probNet    the probabilistic network
     * @param variable1  the current source variable of the link
     * @param variable2  the current destination variable of the link
     * @param isDirected whether the link is directed
     */
    public InvertLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected) {
        super(probNet, variable1, variable2, isDirected);
        parent = probNet.getNode(this.getVariableFrom());
        child = probNet.getNode(this.getVariableTo());
    }
    
    
    @Override
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(DistinctLinks.class) instanceof DistinctLinks constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            boolean directed = this.isDirected();
            if (!DistinctLinks.checkLink(probNet, node2, node1, directed)) {
                constraintChecker.addException(new ConstraintViolatedException.LinkAlreadyExists(constraint, node2, node1));
            }
        }
        if (probNet.getConstraintOfClass(NoCycle.class) instanceof NoCycle constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            boolean existsPath = probNet.existsPath(node1, node2, true, Collections.singletonList(probNet.getLink(parent, child, true)));
            if (existsPath) {
                constraintChecker.addException(new ConstraintViolatedException.ThereIsACycle(constraint, node2, node1));
            }
        }
        if (probNet.getConstraintOfClass(NoMixedParents.class) instanceof NoMixedParents constraint) {
            Node nodeTo = probNet.getNode(this.getVariableTo());
            if (nodeTo.getNodeType() == NodeType.UTILITY) {
                Node nodeFrom = probNet.getNode(this.getVariableFrom());
                var newParents = new ArrayList<>(nodeFrom.getParents());
                newParents.add(nodeTo);
                constraint.checkParents(nodeFrom, newParents, constraintChecker);
            }
        }
        if (probNet.getConstraintOfClass(NoMultipleLinks.class) instanceof NoMultipleLinks constraint) {
            Node node1 = probNet.getNode(this.getVariableFrom());
            Node node2 = probNet.getNode(this.getVariableTo());
            boolean directed = this.isDirected();
            constraint.checkLink(probNet, constraintChecker, node2, node1, directed);
        }
        if (probNet.getConstraintOfClass(ModelNetworkConstraint.class) instanceof ModelNetworkConstraint constraint
                && !constraint.isLinkInversionAllowed() && !constraint.canEditBeDone(this)) {
            constraintChecker.addException(new ConstraintViolatedException.ModelDoesNotAllowInvertingLink(constraint, this.getVariableFrom(), this.getVariableTo()));
        }
    }
    
    @Override protected void doEdit() throws DoEditException.CannotInvertLink {
        // Remove links first
        probNet.removeLink(parent, child, isDirected);
        if (child.getNodeType() != NodeType.DECISION) {
            // Update potentials
            List<Potential> newPotentials = new ArrayList<>();
            this.childOldPotentials = child.getPotentials();
            for (Potential oldPotential : childOldPotentials) {
                Potential newPotential = oldPotential.removeVariable(parent.getVariable());
                newPotentials.add(newPotential);
            }
            child.setPotentials(newPotentials);
        }
        
        // Add inverse link
        probNet.addLink(child, parent, isDirected);
        if (child.getNodeType() != NodeType.DECISION) {
            this.parentOldPotentials = parent.getPotentials();
            List<Potential> newPotentials = new ArrayList<>();
            for (Potential oldPotential : parentOldPotentials) {
                // Update potential
                Potential newPotential = oldPotential.addVariable(child.getVariable());
                newPotentials.add(newPotential);
            }
            parent.setPotentials(newPotentials);
        }
        
        // Checks that the inversion is legal, i.e. it does not produce cycles.
        if (!probNet.checkProbNet()) {
            undo();
            throw new DoEditException.CannotInvertLink(parent, child, probNet, this, probNet.getUnsatisfiedConstraints());
        }
    }
    
    @Override public void undo() {
        super.undo();
        probNet.removeLink(variableTo, variableFrom, isDirected);
        probNet.addLink(variableFrom, variableTo, isDirected);
        parent.setPotentials(parentOldPotentials);
        child.setPotentials(childOldPotentials);
    }
    
    /**
     * Method to compare two InvertLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     *
     * @param obj InvertLinkEdit
     *
     * @return Result of the comparison
     */
    public int compareTo(InvertLinkEdit obj) {
        int result;
        
        if ((
                result = variableFrom.getName().compareTo(obj.getVariableFrom().
                                                             getName())
        ) != 0) {
            return result;
        }
        if ((
                result = variableTo.getName().compareTo(obj.getVariableTo().
                                                           getName())
        ) != 0) {
            return result;
        }
        return 0;
    }
    
    @Override public String getOperationName() {
        return "Invert link";
    }
    
    /**
     * This method assumes that the link is directed, otherwise has no sense.
     *
     * @return {@code String}
     */
    public String toString() {
        return getOperationName() + ": " + variableFrom.getName() + "-->" + variableTo.getName() +
                " ==> " + variableTo.getName() + "-->" + variableFrom.getName();
    }
    
    @Override public BaseLinkEdit getUndoEdit() {
        return new InvertLinkEdit(getProbNet(), getVariableTo(), getVariableFrom(), isDirected());
    }
    
}