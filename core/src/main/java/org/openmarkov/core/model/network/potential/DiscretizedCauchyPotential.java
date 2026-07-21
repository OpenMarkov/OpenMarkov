/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a conditional Cauchy potential for discrete variables.
 * It is defined by two potentials, namely the median and the scale potentials
 * In the case of discrete variables it uses each state index
 */
@PotentialType(names = "Discretized Cauchy") public class DiscretizedCauchyPotential extends Potential {
    
    private Potential median;
    private Potential scale;
    
    public DiscretizedCauchyPotential(Potential potential) {
        super(potential);
        
        if (potential instanceof DiscretizedCauchyPotential) {
            median = ((DiscretizedCauchyPotential) potential).getMedian().copy();
            scale = ((DiscretizedCauchyPotential) potential).getScale().copy();
        }
    }
    
    //    public DiscretizedCauchyPotential(Variable utilityVariable,
    //                                      List<Variable> variables) {
    //        super(utilityVariable, variables);
    //        median = getDefaultMedianPotential();
    //        scale = getDefaultScalePotential();
    //    }
    
    public DiscretizedCauchyPotential(DiscretizedCauchyPotential potential) {
        super(potential);
        this.median = potential.getMedian().copy();
        this.scale = potential.getScale().copy();
        
    }
    
    public DiscretizedCauchyPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        
        median = getDefaultMedianPotential();
        scale = getDefaultScalePotential();
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      {@code Node}
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        // not a utility potential, only discrete or discretized conditioned variables
        return role != PotentialRole.UNSPECIFIED && !variables.isEmpty()
                && variables.getFirst().getVariableType() != VariableType.NUMERIC;
    }
    
    public Potential getMedian() {
        return median;
    }
    
    public void setMedian(Potential median) {
        this.median = median;
    }
    
    public Potential getScale() {
        return scale;
    }
    
    public void setScale(Potential scale) {
        this.scale = scale;
    }
    
    /**
     * Projects this potential to a table by discretizing a Cauchy distribution: for each parent
     * configuration it builds a {@code CauchyDistribution} from the projected median and scale
     * potentials and assigns each conditioned state the probability mass between consecutive
     * thresholds.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException {
        List<Variable> unobservedVariables = new ArrayList<>(variables);
        if (evidenceCase != null) {
            unobservedVariables.removeAll(evidenceCase.getVariables());
        }
        // Create projected potential
        TablePotential projectedPotential = new TablePotential(unobservedVariables, role);
        // If there is no unobserved variables, the resulting potential is constant
        if (unobservedVariables.isEmpty()) {// Projection = constant potential
            // For the time being no utility potentials are supported
            projectedPotential.getValues()[0] = 1.0;
        } else {
            // Project median and scale potentials
            TablePotential projectedMedianPotential = median
                    .tableProject(evidenceCase, inferenceOptions, projectedPotentials);
            TablePotential projectedScalePotential = scale
                    .tableProject(evidenceCase, inferenceOptions, projectedPotentials);
            
            int numConfigurations = projectedMedianPotential.tableSize;
            // Go trough this potential using accumulatedOffests
            int numStates = getConditionedVariable().getNumStates();
            // TODO define thresholds
            double[] thresholds = getThresholds();
            // Copy configurations using the accumulated offsets algorithm
            for (int configuration = 0; configuration < numConfigurations; configuration++) {
                int configurationIndex = configuration * numStates;
                double median = projectedMedianPotential.getValues()[configuration];
                double scale = projectedScalePotential.getValues()[configuration];
                org.apache.commons.math3.distribution.CauchyDistribution dist = new org.apache.commons.math3.distribution.CauchyDistribution(
                        median, scale);
                double lastCdf = 0;
                for (int i = 0; i < numStates - 1; i++) {
                    double cdf = dist.cumulativeProbability(thresholds[i]);
                    projectedPotential.getValues()[configurationIndex + i] = cdf - lastCdf;
                    lastCdf = cdf;
                }
                // The remaining probability is assigned to the last state
                projectedPotential.getValues()[configurationIndex + (numStates - 1)] = 1 - lastCdf;
            }
        }
        return projectedPotential;
    }
    
    /**
     * Not supported: this potential can only be projected to a table via {@link #tableProject}.
     *
     * @throws NotSupportedOperationException always
     */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    private double[] getThresholds() {
        Variable conditionedVariable = getConditionedVariable();
        int numStates = conditionedVariable.getNumStates();
        double[] thresholds = new double[numStates];
        if (conditionedVariable.getVariableType() == VariableType.DISCRETIZED) {
            double[] limits = conditionedVariable.getPartitionedInterval().getLimits();
            // Ignore first limit, as it is considered minus infinity
            System.arraycopy(limits, 1, thresholds, 0, numStates);
        } else {
            // Default thresholds
            for (int i = 0; i < numStates; ++i)
                thresholds[i] = i + 0.5;
        }
        return thresholds;
    }
    
    @Override public Potential copy() {
        return new DiscretizedCauchyPotential(this);
    }

    private Potential getDefaultMedianPotential() {
        List<Variable> medianPotentialVariables = new ArrayList<>(variables);
        return new TablePotential(medianPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    private Potential getDefaultScalePotential() {
        List<Variable> scalePotentialVariables = new ArrayList<>(variables);
        TablePotential scalePotential = new TablePotential(scalePotentialVariables,
                                                           PotentialRole.CONDITIONAL_PROBABILITY);
        Arrays.fill(scalePotential.getValues(), 1);
        return scalePotential;
    }
    
    // This methods are not still used as they were not completely implemented in the GUI
/*
    @Override
    public Potential addVariable(Variable variable) {
        variables.add(variable);
        Variable medianVariable = median.getUtilityVariable();
        median = median.addVariable(variable);
        median.setUtilityVariable(medianVariable);
        Variable scaleVariable = scale.getUtilityVariable();
        scale = scale.addVariable(variable);
        scale.setUtilityVariable(scaleVariable);
        return this;
    }

    @Override
    public Potential removeVariable(Variable variable) {
        variables.remove(variable);
        Variable medianVariable = median.getUtilityVariable();
        median = median.removeVariable(variable);
        median.setUtilityVariable(medianVariable);
        Variable scaleVariable = scale.getUtilityVariable();
        scale = scale.removeVariable(variable);
        scale.setUtilityVariable(scaleVariable);
        return this;
    }
*/
    
    @Override public void scalePotential(double scale) {
        throw new NotSupportedOperationException();
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        DiscretizedCauchyPotential potential = (DiscretizedCauchyPotential) super.deepCopy(copyNet);
        potential.median = this.median.deepCopy(copyNet);
        potential.scale = this.scale.deepCopy(copyNet);
        
        return potential;
    }
    
    @Override public void replaceVariable(int position, Variable variable) {
        Variable oldVariable = variables.get(position);
        super.replaceVariable(position, variable);
        median.replaceVariable(oldVariable, variable);
        scale.replaceVariable(oldVariable, variable);
    }
    
    @Override public void replaceNumericVariable(Variable convertedParentVariable) {
        replaceNumericVariablePotentialVariableSet(convertedParentVariable, variables);
        replaceNumericVariablePotentialVariableSet(convertedParentVariable, median.variables);
        replaceNumericVariablePotentialVariableSet(convertedParentVariable, scale.variables);
    }
    
    private static void replaceNumericVariablePotentialVariableSet(Variable convertedParentVariable,
                                                                   List<Variable> potentialVariables) {
        int varIndex = -1;
        for (int i = 0; i < potentialVariables.size(); ++i) {
            if (potentialVariables.get(i).getName().equals(convertedParentVariable.getName())) {
                varIndex = i;
            }
        }
        if (varIndex != -1) {
            potentialVariables.set(varIndex, convertedParentVariable);
        }
    }
    
    /**
     * Returns a copy with the variables in the given order, reordering both the median and the
     * scale potentials accordingly.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        DiscretizedCauchyPotential copy = new DiscretizedCauchyPotential(this);
        copy.variables = new ArrayList<>(newOrderOfVariables);
        Potential reorderedMedian = this.median.reorder(newOrderOfVariables);
        copy.median = (reorderedMedian != null) ? reorderedMedian : this.median.copy();
        Potential reorderedScale = this.scale.reorder(newOrderOfVariables);
        copy.scale = (reorderedScale != null) ? reorderedScale : this.scale.copy();
        return copy;
    }

    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        DiscretizedCauchyPotential copy = new DiscretizedCauchyPotential(this);
        Potential reorderedMedian = this.median.reorder(variable, newOrder);
        copy.median = (reorderedMedian != null) ? reorderedMedian : this.median.copy();
        Potential reorderedScale = this.scale.reorder(variable, newOrder);
        copy.scale = (reorderedScale != null) ? reorderedScale : this.scale.copy();
        return copy;
    }
}