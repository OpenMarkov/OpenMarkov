/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.MultiStepEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlyFiniteStatesVariables;
import org.openmarkov.core.model.network.constraint.OnlyNumericVariables;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Arias
 */
@SuppressWarnings("serial")
public class VariableTypeEdit extends MultiStepEdit {
    
    private final @NotNull Node node;
    private final @NotNull VariableType newType;
    private final boolean updatePotential;
    
    public VariableTypeEdit(@NotNull Node node, @NotNull VariableType newType, boolean updatePotential) {
        super(node.getProbNet());
        this.node = node;
        this.newType = newType;
        this.updatePotential = updatePotential;
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(OnlyDiscreteVariables.class) instanceof OnlyDiscreteVariables constraint) {
            if (!OnlyDiscreteVariables.isDiscrete(this.node.getNodeType(), this.newType)) {
                constraintChecker.addException(
                        new ConstraintViolatedException.OnlyDiscreteVariablesAllowed(constraint, this.node
                                .getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(
                OnlyFiniteStatesVariables.class) instanceof OnlyFiniteStatesVariables constraint) {
            if (!OnlyFiniteStatesVariables.nodeIsFinite(this.node.getNodeType(), this.newType)) {
                constraintChecker
                        .addException(new ConstraintViolatedException.OnlyFiniteStatesAllowed(constraint, this.node
                                .getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(OnlyNumericVariables.class) instanceof OnlyNumericVariables constraint) {
            if (this.newType != VariableType.NUMERIC) {
                constraintChecker.addException(
                        new ConstraintViolatedException.OnlyNumericVariablesAllowed(constraint, this.node
                                .getVariable()));
            }
        }
        super.checkConstraintsWillBeMet(constraintChecker);
    }
    
    @Override protected void doMultiStepEdit(StepExecuter stepExecuter) throws DoEditException {
        // Save the current states and interval
        var originalStates = node.getVariable().getStates();
        var originalVariableType = node.getVariable().getVariableType();
        // If there is only one interval, set default partitioned interval
        var originalPartitionedInterval = node.getVariable().getPartitionedInterval();
        
        stepExecuter.execute(new RawSetType(originalStates, originalVariableType, originalPartitionedInterval));
        
        if (originalVariableType != newType) {
            switch (originalVariableType) {
                case FINITE_STATES, DISCRETIZED -> {
                    if (newType == VariableType.NUMERIC) {
                        setPotentialsNodeAndChildren(node, stepExecuter);
                    }
                }
                case NUMERIC -> {
                    setPotentialsNodeAndChildren(node, stepExecuter);
                    if (newType == VariableType.DISCRETIZED && node.getVariable()
                                                                   .getPartitionedInterval()
                                                                   .getNumSubintervals() == 1) {
                        
                        PartitionedInterval interval = new PartitionedInterval(
                                node.getVariable().getDefaultInterval(node.getVariable().getNumStates()),
                                Variable.getDefaultBelongs(node.getVariable().getNumStates())
                        );
                        stepExecuter.execute(new RawSetPartitionedInterval(interval, originalPartitionedInterval));
                    }
                }
                case EVENT -> {
                }
            }
            
            
            if (originalVariableType == VariableType.NUMERIC) {
                // Build the list of variables (node and its parents)
                List<Variable> variables = new ArrayList<>();
                if (node.getNodeType() != NodeType.UTILITY) {
                    variables.add(node.getVariable());
                }
                for (Node parent : node.getProbNet().getParents(node)) {
                    variables.add(parent.getVariable());
                }
                
                // Create and assign uniform potential
                UniformPotential uniformPotential = new UniformPotential(
                        variables,
                        node.getPotentials().getFirst().getPotentialRole()
                );
                
                node.setPotentials(new ArrayList<>(List.of(uniformPotential)));
                if (this.updatePotential) {
                    VariableTypeEdit.setUniformPotentialToNode(node, stepExecuter);
                }
            }
        }
        
        
        for (Node child : probNet.getChildren(node)) {
            Link<Node> link = probNet.getLink(node, child, true);
            if (LinkOperations.hasRevealingConditions(link)) {
                var originalRevealingIntervals = link.getRevealingIntervals();
                var originalRevealingStates = link.getRevealingStates();
                var newRevealingIntervals = new ArrayList<PartitionedInterval>();
                var newRevealingStates = new ArrayList<State>();
                stepExecuter.execute(new RawSetRevelation(link, newRevealingIntervals, newRevealingStates, originalRevealingIntervals, originalRevealingStates));
            }
        }
        
        for (Link<Node> link : probNet.getLinks(node)) {
            if (link.hasRestrictions()) {
                stepExecuter.execute(new RawSetRestriction(link));
            }
        }
        
    }
    
    public void setPotentialsNodeAndChildren(Node node, StepExecuter stepExecuter) throws DoEditException {
        ProbNet probNet = node.getProbNet();
        if (this.updatePotential) {
            VariableTypeEdit.setUniformPotentialToNode(node, stepExecuter);
        }
        for (Node child : probNet.getChildren(node)) {
            if (child.getNodeType() == NodeType.UTILITY) {
                List<Potential> newPotentials = new ArrayList<>();
                if (child.onlyNumericalParents()) {
                    for (Potential oldPotential : child.getPotentials()) {
                        Potential newPotential = new SumPotential(oldPotential.getVariables(),
                                                                  oldPotential.getPotentialRole());
                        newPotentials.add(newPotential);
                    }
                } else {
                    for (Potential oldPotential : child.getPotentials()) {
                        Potential newPotential = new UniformPotential(oldPotential.getVariables(),
                                                                      oldPotential.getPotentialRole());
                        newPotentials.add(newPotential);
                    }
                }
                stepExecuter.execute(new RawSetPotentialEdit(child, newPotentials));
            } else {
                if (child.getPotentials() != null && !child.getPotentials().isEmpty()) {
                    VariableTypeEdit.setUniformPotentialToNode(child, stepExecuter);
                }
            }
        }
    }
    
    public static void setUniformPotentialToNode(Node node, StepExecuter stepExecuter) throws DoEditException {
        stepExecuter.execute(new RawSetPotentialEdit(node, PotentialOperations.getUniformPotential(
                node.getProbNet(), node.getVariable(), node.getNodeType())));
    }
    
    private class RawSetType extends PNEdit {
        private final State[] originalStates;
        private final VariableType originalVariableType;
        private final PartitionedInterval originalPartitionedInterval;
        
        public RawSetType(State[] originalStates, VariableType originalVariableType, PartitionedInterval originalPartitionedInterval) {
            super(VariableTypeEdit.this.probNet);
            this.originalStates = originalStates;
            this.originalVariableType = originalVariableType;
            this.originalPartitionedInterval = originalPartitionedInterval;
        }
        
        @Override protected void doEdit() {
            node.getVariable().setStates(originalStates.length == 1
                                                 ? node.getProbNet().getDefaultStates()
                                                 : originalStates);
            node.getVariable().setVariableType(newType);
        }
        
        @Override public void undo() {
            node.getVariable().setVariableType(originalVariableType);
            node.getVariable().setStates(originalStates);
            node.getVariable().setPartitionedInterval(originalPartitionedInterval);
        }
        
        
    }
    
    private class RawSetPartitionedInterval extends PNEdit {
        
        private final PartitionedInterval interval;
        private final PartitionedInterval originalPartitionedInterval;
        
        public RawSetPartitionedInterval(PartitionedInterval interval, PartitionedInterval originalPartitionedInterval) {
            super(VariableTypeEdit.this.probNet);
            this.interval = interval;
            this.originalPartitionedInterval = originalPartitionedInterval;
        }
        
        @Override protected void doEdit() {
            node.getVariable().setPartitionedInterval(interval);
        }
        
        @Override public void undo() {
            node.getVariable().setPartitionedInterval(originalPartitionedInterval);
        }
        
    }
    
    private static class RawSetPotentialEdit extends PNEdit {
        
        private final @NotNull Node node;
        private final @NotNull List<Potential> oldPotentials;
        private ArrayList<Potential> newPotentials;
        
        RawSetPotentialEdit(Node node, Potential potential) {
            super(node.getProbNet());
            this.node = node;
            this.oldPotentials = node.getPotentials();
            this.newPotentials = new ArrayList<>(List.of(potential));
        }
        
        RawSetPotentialEdit(Node node, List<Potential> potentials) {
            super(node.getProbNet());
            this.node = node;
            this.oldPotentials = node.getPotentials();
            this.newPotentials = new ArrayList<>(potentials);
        }
        
        @Override public void undo() {
            node.setPotentials(oldPotentials);
        }
        
        @Override protected void doEdit() {
            node.setPotentials(newPotentials);
        }
        
    }
    
    private class RawSetRevelation extends PNEdit {
        
        private final Link<Node> link;
        private final ArrayList<PartitionedInterval> newRevealingIntervals;
        private final ArrayList<State> newRevealingStates;
        private final List<PartitionedInterval> originalRevealingIntervals;
        private final List<State> originalRevealingStates;
        
        public RawSetRevelation(Link<Node> link, ArrayList<PartitionedInterval> newRevealingIntervals, ArrayList<State> newRevealingStates, List<PartitionedInterval> originalRevealingIntervals, List<State> originalRevealingStates) {
            super(VariableTypeEdit.this.probNet);
            this.link = link;
            this.newRevealingIntervals = newRevealingIntervals;
            this.newRevealingStates = newRevealingStates;
            this.originalRevealingIntervals = originalRevealingIntervals;
            this.originalRevealingStates = originalRevealingStates;
        }
        
        @Override protected void doEdit() {
            link.setRevealingIntervals(newRevealingIntervals);
            link.setRevealingStates(newRevealingStates);
        }
        
        @Override public void undo() {
            link.setRevealingIntervals(originalRevealingIntervals);
            link.setRevealingStates(originalRevealingStates);
        }
        
        
    }
    
    private class RawSetRestriction extends PNEdit {
        
        private final Link<Node> link;
        
        public RawSetRestriction(Link<Node> link) {
            super(VariableTypeEdit.this.probNet);
            this.link = link;
        }
        
        TablePotential newRestrictionPotential;
        TablePotential oldRestrictionPotential;
        
        @Override protected void doEdit() {
            oldRestrictionPotential = link.getRestrictionsPotential();
            link.setRestrictionsPotential(newRestrictionPotential);
        }
        
        @Override public void undo() {
            newRestrictionPotential = link.getRestrictionsPotential();
            link.setRestrictionsPotential(oldRestrictionPotential);
        }
        
        
    }
}
