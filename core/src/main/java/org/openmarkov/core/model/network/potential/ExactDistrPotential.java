/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for TablePotential
 *
 * @author Manuel Arias
 */
@PotentialType(names = "Exact") public class ExactDistrPotential extends Potential implements DESSimulablePotential {
    
    
    // Attributes
    private TablePotential tablePotential;
    
    // Constructors
    public ExactDistrPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        if (this.role == null) {
            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
        }
        // Use UncertainTablePotential so that setUncertainValues() works on this potential
        tablePotential = new UncertainTablePotential(variables.subList(1, variables.size()), PotentialRole.UNSPECIFIED);
    }
    
    public ExactDistrPotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    public ExactDistrPotential(List<Variable> variables, PotentialRole role, double[] table) {
        this(variables, role);
        this.tablePotential.setValues(table);
    }
    
    public ExactDistrPotential(ExactDistrPotential potential) {
        super(potential);
        this.tablePotential = (TablePotential) potential.getTablePotential().copy();
    }
    
    // Methods
    /**
     * {@inheritDoc}
     * <p>Delegates the projection to the wrapped {@link TablePotential} and sets the
     * child variable's decision criterion and an unspecified role on the result.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException {
        // get the projected TablePotential, which will be returned inside a list
        TablePotential projectedPotential = tablePotential.tableProject(evidenceCase, inferenceOptions);
        projectedPotential.setCriterion(getChildVariable().getDecisionCriterion());
        projectedPotential.setPotentialRole(PotentialRole.UNSPECIFIED);
        return projectedPotential;
    }
    
    @Override public ExactDistrPotential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
        TablePotential projectedPotential = tablePotential.tableProject(evidenceCase, null);
        List<Variable> newVariables = new ArrayList<>();
        newVariables.add(variables.get(0));
        newVariables.addAll(projectedPotential.getVariables());
        ExactDistrPotential exactDistrPotential = new ExactDistrPotential(newVariables, PotentialRole.UNSPECIFIED);
        exactDistrPotential.setTablePotential(projectedPotential);
        return exactDistrPotential;
    }
    
    /**
     * {@inheritDoc}
     * <p>Delegates the projection to the wrapped {@link TablePotential} and sets the
     * child variable's decision criterion and an unspecified role on the result.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException {
        // get the projected TablePotential, which will be returned inside a list
        TablePotential projectedPotential = tablePotential
                .tableProject(evidenceCase, inferenceOptions, alreadyProjectedPotentials);
        projectedPotential.setCriterion(getChildVariable().getDecisionCriterion());
        projectedPotential.setPotentialRole(PotentialRole.UNSPECIFIED);
        return projectedPotential;
    }
    
    @Override public Potential sample() {
        ExactDistrPotential sampled = (ExactDistrPotential) copy();
        sampled.tablePotential = (TablePotential) tablePotential.sample(true);
        return sampled;
    }
    
    @Override public Potential copy() {
        return new ExactDistrPotential(this);
    }
    
    @Override public boolean isUncertain() {
        return this.tablePotential.isUncertain();
    }
    
    @Override public void scalePotential(double scale) {
        this.tablePotential.scalePotential(scale);
    }
    
    public TablePotential getTablePotential() {
        return tablePotential;
    }
    
    public void setTablePotential(TablePotential tablePotential) {
        this.tablePotential = tablePotential;
    }
    
    public Variable getChildVariable() {
        return this.getVariable(0);
    }
    
    public void setChildVariable(Variable childVariable) {
        this.getVariables().set(0, childVariable);
    }
    
    public UncertainValue[] getUncertainValues() {
        return tablePotential.getUncertainValues();
    }
    
    public void setUncertainValues(UncertainValue[] uncertainValues) {
        tablePotential.setUncertainValues(uncertainValues);
    }
    
    public double[] getValues() {
        return tablePotential.getValues();
    }
    
    public void setValues(double[] values) {
        this.tablePotential.setValues(values);
    }
    
    /**
     * Returns the exact stored value for the given parent configuration. Since this
     * is an exact (degenerate) distribution, no sampling is performed and
     * {@code randomNumbers} is ignored.
     */
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) {
        return tablePotential.getValue(parents);
    }
    
    @Override public List<Variable> getVariables() {
        return variables;
    }
    
    @Override public void setVariables(List<Variable> variables) {
        super.setVariables(variables);
        this.tablePotential.setVariables(variables.subList(1, variables.size()));
    }
    
    @Override public void setComment(String comment) {
        super.setComment(comment);
        this.tablePotential.setComment(comment);
    }
    
    @Override public String toString() {
        String out = variables.get(0).getName();
        if (variables.size() == 1) {
            out += " = ";
        } else if (variables.size() > 1) {
            out += " | ";
            // Print variables
            for (int i = 1; i < variables.size() - 1; i++) {
                out += variables.get(i).getName() + ", ";
            }
            out += variables.get(variables.size() - 1).getName() + " = ";
        }
        if (tablePotential.getValues().length == 1) {
            out += tablePotential.getValues()[0];
        } else if (tablePotential.getValues().length > 1) {
            out += "{";
            for (int i = 0; i < tablePotential.getValues().length; i++) {
                out += tablePotential.getValues()[i];
                if (i != tablePotential.getValues().length - 1) {
                    out += ",";
                }
            }
            out += "}";
        }
        // Comment these lines to fix issue 477: Wrong text in Tree/ADD potentials
		/*
		out.append("\n Role: " + this.getPotentialRole());
		if (criterion != null) {
			out.append("\n Criterion: " + criterion.toString());
		}*/
        return out;
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given
     * the variables and the potential role.
     *
     * @param node      {@code Node}
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        //11/01/2023 FIXME Provisional; validate for DESnets
        if ((node.getProbNet().getNetworkType() instanceof DESNetworkType)){
            return !variables.stream().anyMatch(variable -> variable.getVariableType()==VariableType.EVENT) && node.getVariable().getVariableType() == VariableType.NUMERIC;
        }
        if (node.getNodeType() != NodeType.CHANCE) {
            return true;
        }
        if (node.getVariable().getVariableType() == VariableType.NUMERIC) {
            return true;
        }
        return variables.stream().skip(1).anyMatch(parent -> parent.getVariableType() == VariableType.NUMERIC);
    }
    
    
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        TablePotential auxPotential = (TablePotential) getTablePotential().reorder(newOrderOfVariables);
        List<Variable> newPotentialVariables = new ArrayList<>();
        newPotentialVariables.add(getVariables().get(0));
        newPotentialVariables.addAll(newOrderOfVariables);
        ExactDistrPotential potential = new ExactDistrPotential(newPotentialVariables, getPotentialRole());
        potential.setTablePotential(auxPotential);
        return potential;
    }
    
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        ExactDistrPotential copyPotential = (ExactDistrPotential) copy();
        copyPotential.setTablePotential(copyPotential.tablePotential.reorder(variable, newOrder));
        return copyPotential;
    }
    
}
