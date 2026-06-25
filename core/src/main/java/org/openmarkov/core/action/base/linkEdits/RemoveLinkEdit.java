/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.ModelNetworkConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Removes a directed or undirected link between two nodes and optionally
 * updates the destination node's potentials to no longer reference the removed parent.
 */
public final class RemoveLinkEdit extends BaseLinkEdit {
    
    /**
     * Resulting link of addition or removal.
     */
    private Link<Node> link;
    /**
     * The last {@code Potential} of the second node before the edition
     * /**
     * parent node
     */
    private final Node node1;
    /**
     * child node
     */
    private final Node node2;
    /**
     * The new {@code Potential} of the second node
     */
    private final List<Potential> newPotentials = new ArrayList<>();
    private List<Potential> oldPotentials;
    private final boolean updatePotentials;
    // Constructor
    
    /**
     * @param probNet          the probabilistic network
     * @param variable1        the source variable of the link
     * @param variable2        the destination variable of the link
     * @param isDirected       {@code true} for a directed link
     * @param updatePotentials whether to update potentials after removal
     */
    public RemoveLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected,
                          boolean updatePotentials) {
        super(probNet, variable1, variable2, isDirected);
        
        node1 = probNet.getNode(variable1);
        node2 = probNet.getNode(variable2);
        
        this.updatePotentials = updatePotentials;
        this.link = null;
    }
    
    public RemoveLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected) {
        this(probNet, variable1, variable2, isDirected, true);
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(ModelNetworkConstraint.class) instanceof ModelNetworkConstraint constraint
                && !constraint.isLinkRemovalAllowed() && !constraint.canEditBeDone(this)) {
            constraintChecker.addException(new ConstraintViolatedException.ModelDoesNotAllowRemovingLink(constraint, this.getVariableFrom(), this.getVariableTo()));
        }
    }
    
    @Override protected void doEdit() throws DoEditException.CannotDoEditException {
        if (probNet.hasExplicitLinks()) {
            this.link = probNet.getLink(node1, node2, isDirected);
        }
        probNet.removeLink(node1, node2, isDirected);
        if (updatePotentials) {
            this.oldPotentials = node2.getPotentials();
            if (node2.getNodeType() == NodeType.UTILITY) {// supervalue nodes
                
                if (node2.onlyNumericalParents()) {// utility and numerical parents sum
                    for (Potential oldPotential : oldPotentials) {
                        // Update potential
                        List<Variable> variables = oldPotential.getVariables();
                        variables.remove(node1.getVariable());
                        Potential newPotential = new SumPotential(variables, oldPotential.getPotentialRole());
                        newPotentials.add(newPotential);
                    }
                } else if (!node2.onlyNumericalParents()) {//mixture of finite states and numerical Uniform
                    for (Potential oldPotential : oldPotentials) {
                        // Update potential
                        List<Variable> variables = new ArrayList<>(oldPotential.getVariables());
                        variables.remove(node1.getVariable());
                        Potential newPotential = new UniformPotential(variables, oldPotential.getPotentialRole());
                        newPotentials.add(newPotential);
                    }
                }
                node2.setPotentials(newPotentials);
            } else {
                
                // Update potentials
                this.oldPotentials = node2.getPotentials();
                for (Potential oldPotential : oldPotentials) {
                    Potential newPotential;
                    
                    if (node1.equals(node2)){

						/* Potential#removeVariable(Variable variable) removes variable when it if first encountered,
						does not take into account self loops, then creates a new Uniform potential.
						When removing self-loops Variable appears twice: in potential.variables(0) and potential.variables(k) so it is necessary
						to remove potential.variables(k) instead of potential.variables(0).
						Therefore I replicate the behaviour of removeVariable but removing the last ocurrence
						but removing the last occurrence of Variable because if not */
                        List<Variable> variables = oldPotential.getVariables();
                        variables.remove(oldPotential.getVariables().lastIndexOf(node2.getVariable()));
                        newPotential = new UniformPotential(variables, oldPotential.getPotentialRole());
                        
                    } else {
                        
                        newPotential = oldPotential.removeVariable(node1.getVariable());
                        // 10/01/2020 for removing a self-cycle end of "if condition"
                    }
                    
                    //Before having selfloops it was only this line
//					Potential newPotential = oldPotential.removeVariable(node1.getVariable());
                    //
                    
                    
                    // TODO - Implements validate for all potential types, at this moment it always return true.
					/*
					if (!newPotential.validate(node2, newPotential.getVariables(), newPotential.getPotentialRole())){
						newPotential = new UniformPotential(newPotential.getVariables(), newPotential.getPotentialRole());
					};
					 */
                    
                    // Temporal patch to be removed when the above TO-DO is implemented
                    @Nullable Boolean sumIs0;
                    try {
                        sumIs0 = Arrays.stream(newPotential.getCPT().getValues()).sum() == 0;
                    } catch (NonProjectablePotentialException e) {
                        sumIs0 = null;
                    }
                    if (sumIs0 == null || sumIs0) {
                        newPotential = new UniformPotential(newPotential.getVariables(), newPotential.getPotentialRole());
                    }
                    newPotentials.add(newPotential);
                }
                node2.setPotentials(newPotentials);
            }
        }
    }
    
    @Override public void undo() {
        super.undo();
        
        if (updatePotentials) {
            node2.setPotentials(oldPotentials);
        }
        probNet.addLink(variableFrom, variableTo, isDirected);
        if (probNet.hasExplicitLinks()) {
            Link<Node> newLink = probNet.getLink(node1, node2, isDirected);
            if (link != null && newLink != null) {
                TablePotential restrictionsPotential = link.getRestrictionsPotential();
                newLink.setRestrictionsPotential(restrictionsPotential);
                List<State> revealingStates = link.getRevealingStates();
                newLink.setRevealingStates(revealingStates);
                List<PartitionedInterval> revealingIntervals = link.getRevealingIntervals();
                newLink.setRevealingIntervals(revealingIntervals);
            }
        }
    }
    
    /**
     * Method to compare two RemoveLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     *
     * @param obj RemoveLinkEdit to be compared
     * @return Result of comparison
     */
    public int compareTo(RemoveLinkEdit obj) {
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
        return "Remove link";
    }
    
    @Override public BaseLinkEdit getUndoEdit() {
        return new AddLinkEdit(getProbNet(), getVariableFrom(), getVariableTo(), isDirected());
    }
    
    public String toString() {
        return "Remove link between " + variableFrom.getName() + (isDirected ? " -> " : " -- ") + variableTo.getName();
    }
    
}
