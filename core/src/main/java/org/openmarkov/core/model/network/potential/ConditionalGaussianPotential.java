/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import cern.jet.random.engine.MersenneTwister;
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
import java.util.List;

/**
 * This class represents a conditional Gaussian potential for discrete variables.
 * It is defined by two potentials, namely the mean and the variance potentials
 * In the case of discrete variables it uses each state index
 *
 * @author Manuel Arias
 */
@PotentialType(names = "Conditional Gaussian") public class ConditionalGaussianPotential extends Potential
        implements Projectable {
    
    private Potential mean;
    private Potential variance;
    
    public ConditionalGaussianPotential(Potential potential) {
        super(potential);
        
        if (potential instanceof ConditionalGaussianPotential) {
            mean = ((ConditionalGaussianPotential) potential).mean.copy();
            variance = ((ConditionalGaussianPotential) potential).variance.copy();
        }
    }
    
    //	public ConditionalGaussianPotential(Variable utilityVariable,
    //			List<Variable> variables) {
    //		super(utilityVariable, variables);
    //		mean = getDefaultMeanPotential();
    //		variance = getDefaultVariancePotential();
    //	}
    
    public ConditionalGaussianPotential(ConditionalGaussianPotential potential) {
        super(potential);
        this.mean = potential.mean.copy();
        this.variance = potential.variance.copy();
    }
    
    public ConditionalGaussianPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        
        mean = getDefaultMeanPotential();
        variance = getDefaultVariancePotential();
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}.
     *
     * @return True if valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        // not a utility potential, only discrete or discretized conditioned variables
        return role != PotentialRole.UNSPECIFIED && !variables.isEmpty()
                && variables.getFirst().getVariableType() != VariableType.NUMERIC;
    }
    
    public Potential getMean() {
        return mean;
    }
    
    public void setMean(Potential mean) {
        this.mean = mean;
    }
    
    public Potential getVariance() {
        return variance;
    }
    
    public void setVariance(Potential variance) {
        this.variance = variance;
    }
    
    /**
     * Projects this potential to a table by discretizing a normal distribution: for each parent
     * configuration it builds a {@code Normal} distribution from the projected mean and variance
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
            // Project mean and variance potentials
            TablePotential projectedMeanPotential = mean.tableProject(evidenceCase, inferenceOptions, projectedPotentials);
            TablePotential projectedVariancePotential = variance.tableProject(evidenceCase, inferenceOptions, projectedPotentials);
            
            int numConfigurations = projectedMeanPotential.tableSize;
            // Go trough this potential using accumulatedOffests
            int numStates = getConditionedVariable().getNumStates();
            // TODO define thresholds
            double[] thresholds = getThresholds();
            // Copy configurations using the accumulated offsets algorithm
            for (int configuration = 0; configuration < numConfigurations; configuration++) {
                int configurationIndex = configuration * numStates;
                double mean = projectedMeanPotential.getValues()[configuration];
                double variance = projectedVariancePotential.getValues()[configuration];
                cern.jet.random.Normal dist = new cern.jet.random.Normal(mean, variance, new MersenneTwister());
                double lastCdf = 0;
                for (int i = 0; i < numStates - 1; i++) {
                    double cdf = dist.cdf(thresholds[i]);
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
            for (int i = 0; i < numStates; ++i) {
                thresholds[i] = i + 0.5;
            }
        }
        return thresholds;
    }
    
    @Override public Potential copy() {
        return new ConditionalGaussianPotential(this);
    }

    private Potential getDefaultMeanPotential() {
        // TODO use next line for something or remove it
        // Variable meanVariable = new Variable("Mean");
        List<Variable> meanPotentialVariables = new ArrayList<>(variables);
        // Remove conditioned variable
        // meanPotentialVariables.remove(0);
        // We create a utility potential because it is the only kind of
        // potential assumed to have a numeric conditioned variable
        return new TablePotential(meanPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        //return new TablePotential(meanVariable, meanPotentialVariables);
        
    }
    
    private Potential getDefaultVariancePotential() {
        // TODO use next line for something or remove it
        // Variable varianceVariable = new Variable("Variance");
        List<Variable> variancePotentialVariables = new ArrayList<>(variables);
        // Remove conditioned variable
        //variancePotentialVariables.remove(0);
        // We create a utility potential because it is the only kind of
        // potential assumed to have a numeric conditioned variable
        //TablePotential variancePotential = new TablePotential(varianceVariable, variancePotentialVariables);
        TablePotential variancePotential = new TablePotential(variancePotentialVariables,
                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        // Set variance to 1 for all configurations (except for the first one; previously, when
        // using a utility potential, the first variable was removed)
        //for(int i=0;i<variancePotential.getValues().length;++i) {
        for (int i = 1; i < variancePotential.getValues().length; ++i) {
            variancePotential.getValues()[i] = 1;
        }
        return variancePotential;
    }
    
    // This methods are not still used as they were not completely implemented in the GUI
    //	@Override
    //	public Potential addVariable(Variable variable) {
    //		variables.add(variable);
    //		//Variable meanVariable = mean.getUtilityVariable();
    //		Variable meanVariable = mean.getVariable(0);
    //		mean = mean.addVariable(variable);
    //		mean.setUtilityVariable(meanVariable);
    //		Variable varianceVariable = variance.getUtilityVariable();
    //		Variable varianceVariable = variance.getVariable(0);
    //		variance = variance.addVariable(variable);
    //		//variance.setUtilityVariable(varianceVariable);
    //		return this;
    //	}
    //
    //	@Override
    //	public Potential removeVariable(Variable variable) {
    //		variables.remove(variable);
    //		//Variable meanVariable = mean.getUtilityVariable();
    //		Variable meanVariable = mean.getVariable(0);
    //		mean = mean.removeVariable(variable);
    //		mean.setUtilityVariable(meanVariable);
    //		Variable varianceVariable = variance.getUtilityVariable();
    //		Variable varianceVariable = variance.getVariable(0);
    //		variance = variance.removeVariable(variable);
    //		//variance.setUtilityVariable(varianceVariable);
    //		return this;
    //	}
    
    @Override public void scalePotential(double scale) {
        throw new NotSupportedOperationException();
        
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        ConditionalGaussianPotential potential = (ConditionalGaussianPotential) super.deepCopy(copyNet);
        potential.mean = this.mean.deepCopy(copyNet);
        potential.variance = this.variance.deepCopy(copyNet);
        
        return potential;
    }
    
    @Override public void replaceVariable(int position, Variable variable) {
        Variable oldVariable = variables.get(position);
        super.replaceVariable(position, variable);
        mean.replaceVariable(oldVariable, variable);
        variance.replaceVariable(oldVariable, variable);
    }
    
    /**
     * Returns a copy with the variables in the given order, reordering both the mean and the
     * variance potentials accordingly.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        ConditionalGaussianPotential copy = new ConditionalGaussianPotential(this);
        copy.variables = new ArrayList<>(newOrderOfVariables);
        Potential reorderedMean = this.mean.reorder(newOrderOfVariables);
        copy.mean = (reorderedMean != null) ? reorderedMean : this.mean.copy();
        Potential reorderedVariance = this.variance.reorder(newOrderOfVariables);
        copy.variance = (reorderedVariance != null) ? reorderedVariance : this.variance.copy();
        return copy;
    }

    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        ConditionalGaussianPotential copy = new ConditionalGaussianPotential(this);
        Potential reorderedMean = this.mean.reorder(variable, newOrder);
        copy.mean = (reorderedMean != null) ? reorderedMean : this.mean.copy();
        Potential reorderedVariance = this.variance.reorder(variable, newOrder);
        copy.variance = (reorderedVariance != null) ? reorderedVariance : this.variance.copy();
        return copy;
    }
    
}
