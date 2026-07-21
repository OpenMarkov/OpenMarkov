/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.Projectable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for Independent Causal Influence (ICI) potentials.
 * ICI models decompose a joint conditional probability into independent
 * noisy contributions from each parent, combined via a deterministic function
 * (e.g., OR/MAX, AND/MIN, or Tuning). Each parent has its own noisy parameter
 * table, and there is a leak parameter for background causes.
 * <p>
 * Concrete subclasses include {@link MaxPotential} (OR/MAX family),
 * {@link MinPotential} (AND/MIN family), and {@link TuningPotential}.
 *
 * @author Manuel Arias
 * @see ICIFamily
 * @see ICIModelType
 */
@PotentialType(names = "ICIModel")
public abstract class ICIPotential extends Potential implements Projectable {
    
    /* Model type may be OR, causal MAX, AND, etc. */
    protected ICIModelType modelType;
    
    /* ICI family will be MAX (which includes OR, causal MAX...), MIN, etc. */
    protected ICIFamily family;
    
    /**
     * List of Z variables we are going to use in the canonical model
     */
    private Map<Variable, Variable> zVariables;
    
    /**
     * Noisy parameters for the canonical model
     * The leak parameter is in the last position
     */
    private double[][] noisyParameters;
    
    /**
     * Leak parameters for the canonical model
     */
    private double[] leakyParameters;
    
    private Variable leakyVariable = null;
    
    private @Nullable TablePotential expandedPotential = null;
    
    // Constructor
    
    /**
     * @param variables {@code ArrayList} of {@code Variable}
     * @param modelType {@code ICIModel}
     */
    public ICIPotential(ICIModelType modelType, List<Variable> variables) {
        // In principle, role will be "conditional probability"
        // and the first variable will be the conditioned variable
        super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        Variable conditionedVariable = getConditionedVariable();
        this.modelType = modelType;
        this.family = modelType.getFamily();
        this.noisyParameters = getDefaultNoisyParameters();
        this.leakyParameters = getDefaultLeakyParameters(conditionedVariable.getNumStates());
        zVariables = new LinkedHashMap<>();
        for (int i = 1; i < variables.size(); ++i) {
            zVariables.put(variables.get(i), createZVariable(variables.get(i), conditionedVariable));
        }
        leakyVariable = new Variable(conditionedVariable.getName() + "-leaky", conditionedVariable.getStates());
        
    }
    
    /**
     * Copy constructor. Copies the model type, noisy parameters, and leaky parameters
     * from the given potential.
     *
     * @param potential the ICI potential to copy
     */
    public ICIPotential(ICIPotential potential) {
        super(potential);
        this.modelType = potential.modelType;
        this.family = modelType.getFamily();
        this.noisyParameters = getDefaultNoisyParameters();
        Variable conditionedVariable = getConditionedVariable();
        this.leakyParameters = getDefaultLeakyParameters(conditionedVariable.getNumStates());
        zVariables = new LinkedHashMap<>();
        for (int i = 1; i < variables.size(); ++i) {
            zVariables.put(variables.get(i), createZVariable(variables.get(i), conditionedVariable));
        }
        leakyVariable = new Variable(conditionedVariable.getName() + "-leaky", conditionedVariable.getStates());
        for (int i = 1; i < variables.size(); ++i) {
            setNoisyParameters(variables.get(i), potential.getNoisyParameters(variables.get(i)).clone());
        }
        setLeakyParameters(potential.getLeakyParameters().clone());
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the variables and the potential role
     *
     * @param node      Node
     * @param variables List of variables
     * @param role      Potential role
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        return variables.size() > 1;
    }
    
    /**
     * Computes default noisy parameters for all parent variables. Each parent's parameters
     * are initialized so that the identity mapping holds (state i of parent maps to state i of child).
     *
     * @return a 2D array where each row corresponds to a parent variable's noisy parameters
     */
    public double[][] getDefaultNoisyParameters() {
        double[][] noisyParameters = new double[variables.size() - 1][];
        
        for (int i = 1; i < variables.size(); ++i) {
            Variable parent = variables.get(i);
            noisyParameters[i - 1] = initializeNoisyParameters(variables.getFirst(), parent);
        }
        return noisyParameters;
    }
    
    /**
     * Initializes noisy parameters values
     *
     * @param conditionedVariable Conditioned variable
     * @param parent              Parent variable
     *
     * @return Array of noisy parameters values
     */
    public static double[] initializeNoisyParameters(Variable conditionedVariable, Variable parent) {
        double[] probabilities = new double[conditionedVariable.getNumStates() * parent.getNumStates()];
        for (int j = 0; j < parent.getNumStates(); ++j) {
            for (int k = 0; k < conditionedVariable.getNumStates(); ++k) {
                probabilities[j * conditionedVariable.getNumStates() + k] = (k == j) ? 1.0 : 0.0;
            }
        }
        return probabilities;
    }
    
    /**
     * Returns the default leak parameters for the ICI model. The leak represents
     * background causes not explicitly modeled.
     *
     * @param numStates number of states of the conditioned variable
     * @return default leak parameter array
     */
    public abstract double[] getDefaultLeakyParameters(int numStates);
    
    // Methods
    
    /**
     * Returns the f function potential
     *
     * @return TablePotential containing the f function
     */
    public abstract TablePotential getFFunctionPotential();
    
    /**
     * @param inferenceOptions Inference options
     * @param evidenceCase     {@code EvidenceCase}
     *
     * @return {@code ArrayList} of {@code Potential}
     */
    public List<TablePotential> internalTableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException {
        List<TablePotential> projectedPotentials = new ArrayList<>();
        for (TablePotential subPotential : getSubpotentials()) {
            projectedPotentials.add(subPotential.tableProject(evidenceCase, null));
        }
        return projectedPotentials;
    }
    
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException {
        List<TablePotential> potentials = internalTableProject(evidenceCase, inferenceOptions);
        HashSet<Variable> variablesToEliminate = new HashSet<>();
        // Fill it with variables appearing in all potentials except this
        for (TablePotential tablePotential : potentials) {
            variablesToEliminate.addAll(tablePotential.getVariables());
        }
        variables.forEach(variablesToEliminate::remove);
        
        List<Variable> allVariables = new ArrayList<>(variables);
        allVariables.addAll(variablesToEliminate);
        while (allVariables.size() > variables.size()) {
            Variable variableToEliminate = allVariables.getLast();
            allVariables.removeLast();
            List<TablePotential> relatedPotentials = new ArrayList<>();
            int i = 0;
            while (i < potentials.size()) {
                if (potentials.get(i).getVariables().contains(variableToEliminate)) {
                    // remove potentials related to the deleted variable
                    relatedPotentials.add(potentials.get(i));
                    potentials.remove(i);
                } else {
                    ++i;
                }
            }
            //add resulting potential
            potentials.addFirst(DiscretePotentialOperations.multiplyAndMarginalize(relatedPotentials, allVariables));
        }
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variables);
    }
    
    /**
     * Returns the noisy parameters for the given parent variable.
     *
     * @param variable the parent variable
     * @return the noisy parameter array for that parent
     */
    public double[] getNoisyParameters(Variable variable) {
        return noisyParameters[variables.indexOf(variable) - 1];
    }
    
    /**
     * Sets the noisy parameters, i.e. <i>P(z<sub>i</sub>|x<sub>i</sub>)</i>
     *
     * @param parent     parent variable (<i>X<sub>i</sub></i>) whose noisy parameters we want to set
     * @param parameters the noisy parameters. The length of the array must be the multiplication of the parent's and child's state number
     */
    public void setNoisyParameters(Variable parent, double[] parameters) {
        if (parameters.length != variables.getFirst().getNumStates() * parent.getNumStates()) {
            throw new UnrecoverableException(new InvalidArgumentException(Arrays.stream(parameters)
                    .boxed()
                    .toList(), "parameters", "The length of the array must be the multiplication of the parent's and child's state number "
                    + variables.getFirst().getNumStates() * parent.getNumStates() + " and is " + parameters.length));
        }
        if (!getVariables().contains(parent)) {
            throw new UnrecoverableException(new InvalidArgumentException(this, "potential", "There is no variable " + parent.getName() + " in this ICI family."));
        }
        expandedPotential = null;
        noisyParameters[variables.indexOf(parent) - 1] = parameters;
    }
    
    /**
     * There will be a potential for each link, plus the leak potential and the f function
     *
     * @return {@code ArrayList} of {@code TablePotential}.
     */
    public List<TablePotential> getSubpotentials() {
        List<TablePotential> subpotentials = new ArrayList<>();
        
        // F function
        subpotentials.add(getFFunctionPotential());
        
        //Noisy potentials
        subpotentials.addAll(getNoisyPotentials());
        
        // Leak potential
        TablePotential leakyPotential = getLeakyPotential();
        if (leakyPotential != null) {
            subpotentials.add(leakyPotential);
        }
        
        return subpotentials;
    }
    
    /**
     * There will be a potential for each link, plus the leak potential
     *
     * @return {@code ArrayList} of {@code TablePotential}.
     */
    public List<TablePotential> getNoisyPotentials() {
        List<TablePotential> noisyPotentials = new ArrayList<>();
        
        //Noisy parents
        for (Variable parent : zVariables.keySet()) {
            List<Variable> linkVariables = Arrays.asList(zVariables.get(parent), parent);
            noisyPotentials.add(new TablePotential(linkVariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                   noisyParameters[variables.indexOf(parent) - 1]));
        }
        
        return noisyPotentials;
    }
    
    /**
     * Updates the noisy parameters from a list of table potentials, one per parent variable.
     *
     * @param noisyPotentials list of table potentials whose values replace the noisy parameters
     */
    public void setNoisyPotentials(List<TablePotential> noisyPotentials) {
        for (TablePotential noisyPotential : noisyPotentials) {
            noisyParameters[variables.indexOf(noisyPotential.getVariable(0)) - 1] = noisyPotential.getValues();
        }
    }
    
    /**
     * @return Leak potential. {@code TablePotential}
     */
    public double[] getLeakyParameters() {
        return leakyParameters;
    }
    
    /**
     * Sets Leak parameters
     *
     * @param leakyParameters Array of leaky parameters
     */
    public void setLeakyParameters(double[] leakyParameters) {
        if (leakyParameters.length != variables.getFirst().getNumStates()) {
            throw new UnrecoverableException(new InvalidArgumentException(Arrays.stream(leakyParameters)
                       .boxed()
                       .toList(), "parameters",
                       "The length of the array must be the conditioned variable's state number " + variables.getFirst()
                                                                                                                                                                .getNumStates() + " and is " + leakyParameters.length));
        }
        expandedPotential = null;
        this.leakyParameters = leakyParameters;
    }
    
    /**
     * Returns the leak potential as a table potential with a single variable (the leak variable).
     *
     * @return the leak potential, or {@code null} if no leak parameters are set
     */
    public TablePotential getLeakyPotential() {
        TablePotential leakyPotential = null;
        if (this.leakyParameters != null) {
            ArrayList<Variable> leakVariables = new ArrayList<>();
            leakVariables.add(leakyVariable); // conditioned variable
            leakyPotential = new TablePotential(leakVariables, PotentialRole.CONDITIONAL_PROBABILITY, leakyParameters);
        }
        return leakyPotential;
    }
    
    /**
     * Returns leaky variable
     *
     * @return leaky variable
     */
    protected Variable getLeakyVariable() {
        return this.leakyVariable;
    }
    
    /**
     * @return collection of Z variables
     */
    protected Collection<Variable> getAuxiliaryVariables() {
        return zVariables.values();
    }
    
    /**
     * @return model. {@code ICIModel}
     */
    public ICIModelType getModelType() {
        return modelType;
    }
    
    /**
     * @return model. {@code ICIModel}
     */
    public ICIFamily getFamily() {
        return modelType.getFamily();
    }
    
    public String toString() {
        return super.toString()
                + "\nFamily: " + family + ". " +
                "Model: " + modelType + "\n" +
                "Number of variables: " + variables.size()
                + "\nVariables: " + "[" + variables.stream()
                                                   .map(Variable::getName)
                                                   .collect(Collectors.joining(", ")) + "]\n";
    }
    
    @Override public boolean equals(Object arg0) {
        boolean isEqual = super.equals(arg0) && arg0 instanceof ICIPotential;
        if (isEqual) {
            ICIPotential otherPotential = (ICIPotential) arg0;
            for (int j = 1; j < variables.size(); ++j) {
                double[] values = getNoisyParameters(variables.get(j));
                Variable otherVariable = null;
                int k = 0;
                while (otherVariable == null && k < otherPotential.variables.size()) {
                    otherVariable = (
                            otherPotential.variables.get(k).getName().equals((variables.get(j).getName()))
                    ) ? otherPotential.variables.get(k) : null;
                    ++k;
                }
                double[] otherValues = otherPotential.getNoisyParameters(otherVariable);
                if (values.length == otherValues.length) {
                    for (int i = 0; i < values.length; i++) {
                        isEqual &= values[i] == otherValues[i];
                    }
                } else {
                    isEqual = false;
                }
            }
            
            double[] values = getLeakyParameters();
            double[] otherValues = otherPotential.getLeakyParameters();
            if (values.length == otherValues.length) {
                for (int i = 0; i < values.length; i++) {
                    isEqual &= values[i] == otherValues[i];
                }
            } else {
                isEqual = false;
            }
        }
        return isEqual;
    }
    
    @Override public void replaceVariable(int position, Variable variable) {
        Variable oldVariable = variables.get(position);
        variables.remove(position);
        variables.add(position, variable);
        
        // if position == 0, it is the conditioned variable, not a noisy one
        if (position > 0) {
            zVariables.remove(oldVariable);
            zVariables.put(variable, createZVariable(variables.getFirst(), variable));
        }
        
    }
    
    /**
     * Creates analogous Z variable for the parent variable
     *
     * @param parent Parent variable
     * @param child  Child variable
     *
     * @return Analogous Z variable for the parent variable
     */
    private static Variable createZVariable(Variable parent, Variable child) {
        return new Variable("z_" + parent.getName() + "_" + child.getName(), child.getStates());
    }
    
    @Override public int sampleConditionedVariable(Random randomGenerator, Map<Variable, Integer> sampledParents) {
        int[] iciSampledStates = new int[noisyParameters.length + 1];
        int childNumStates = variables.getFirst().getNumStates();
        
        // Sample noisy
        for (int i = 1; i < variables.size(); ++i) {
            double[] probabilities = noisyParameters[i - 1];
            int index = childNumStates * sampledParents.get(variables.get(i));
            int sampleIndex = 0;
            double randomPick = randomGenerator.nextDouble();
            double accumulatedProbability = probabilities[index + sampleIndex];
            while (accumulatedProbability < randomPick) {
                ++sampleIndex;
                accumulatedProbability += probabilities[index + sampleIndex];
            }
            iciSampledStates[i - 1] = sampleIndex;
        }
        
        // Sample leaky
        int sampleIndex = 0;
        double randomPick = randomGenerator.nextDouble();
        double accumulatedProbability = leakyParameters[sampleIndex];
        while (accumulatedProbability < randomPick) {
            ++sampleIndex;
            accumulatedProbability += leakyParameters[sampleIndex];
        }
        iciSampledStates[iciSampledStates.length - 1] = sampleIndex;
        // Sample child
        return computeFFunction(iciSampledStates);
    }
    
    /**
     * Computes the deterministic combination function (e.g., MAX, MIN) applied to the
     * sampled states of all ICI auxiliary variables plus the leak.
     *
     * @param iciSampledStates sampled state indices for each parent's Z-variable and the leak
     * @return the resulting state index for the conditioned variable
     */
    protected abstract int computeFFunction(int[] iciSampledStates);
    
    @Override
    public double getProbability(HashMap<Variable, Integer> sampledStateIndexes) throws NonProjectablePotentialException {
        if (expandedPotential == null) {
            expandedPotential = getCPT();
        }
        return expandedPotential.getProbability(sampledStateIndexes);
    }
    
    /**
     * Reorders the variable list and the corresponding noisy-parameter arrays.
     * The noisyParameters entry for each parent is moved to the new parent index.
     * The expandedPotential cache is invalidated.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        ICIPotential copy = (ICIPotential) copy();
        // Build new noisyParameters in the order of the new parent list
        double[][] newNoisyParams = new double[newOrderOfVariables.size() - 1][];
        for (int i = 1; i < newOrderOfVariables.size(); i++) {
            Variable parent = newOrderOfVariables.get(i);
            // getNoisyParameters uses this.variables (old order)
            newNoisyParams[i - 1] = this.getNoisyParameters(parent).clone();
        }
        // Rebuild zVariables in new parent order
        copy.zVariables = new LinkedHashMap<>();
        for (int i = 1; i < newOrderOfVariables.size(); i++) {
            Variable parent = newOrderOfVariables.get(i);
            copy.zVariables.put(parent, this.zVariables.get(parent));
        }
        copy.variables = new ArrayList<>(newOrderOfVariables);
        copy.noisyParameters = newNoisyParams;
        copy.expandedPotential = null;
        return copy;
    }

    /**
     * Reorders state entries within the noisy-parameter arrays and leaky parameters
     * when a variable's states are permuted.
     *
     * <p>If the reordered variable is the conditioned variable, the {@code k} (conditioned-state)
     * dimension of every noisy-parameter row and of leakyParameters is permuted.
     * If it is a parent variable, the {@code j} (parent-state) dimension of that parent's
     * noisy-parameter entry is permuted.
     */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        ICIPotential copy = (ICIPotential) copy();
        Variable conditioned = variables.getFirst();
        int numCondStates = conditioned.getNumStates();
        if (variable == conditioned) {
            // Build old-index map: oldIndex[newPos] = position of newOrder[newPos] in old state array
            State[] oldStates = conditioned.getStates();
            int[] oldIndex = buildOldIndex(oldStates, newOrder);
            // Reorder k-dimension of each noisy-parameter entry
            double[][] newNoisyParams = new double[noisyParameters.length][];
            for (int p = 0; p < noisyParameters.length; p++) {
                int numParentStates = variables.get(p + 1).getNumStates();
                double[] oldParams = noisyParameters[p];
                double[] newParams = new double[numParentStates * numCondStates];
                for (int j = 0; j < numParentStates; j++) {
                    for (int newK = 0; newK < numCondStates; newK++) {
                        newParams[j * numCondStates + newK] = oldParams[j * numCondStates + oldIndex[newK]];
                    }
                }
                newNoisyParams[p] = newParams;
            }
            copy.noisyParameters = newNoisyParams;
            // Reorder leakyParameters (indexed by conditioned state)
            double[] newLeaky = new double[numCondStates];
            for (int newK = 0; newK < numCondStates; newK++) {
                newLeaky[newK] = leakyParameters[oldIndex[newK]];
            }
            copy.leakyParameters = newLeaky;
        } else {
            int parentIdx = variables.indexOf(variable) - 1;
            if (parentIdx >= 0) {
                State[] oldStates = variable.getStates();
                int[] oldIndex = buildOldIndex(oldStates, newOrder);
                int numParentStates = variable.getNumStates();
                double[] oldParams = noisyParameters[parentIdx];
                double[] newParams = new double[numParentStates * numCondStates];
                for (int newJ = 0; newJ < numParentStates; newJ++) {
                    for (int k = 0; k < numCondStates; k++) {
                        newParams[newJ * numCondStates + k] = oldParams[oldIndex[newJ] * numCondStates + k];
                    }
                }
                copy.noisyParameters = copy.noisyParameters.clone();
                copy.noisyParameters[parentIdx] = newParams;
            }
        }
        copy.expandedPotential = null;
        return copy;
    }

    /** Returns the displacement array: oldIndex[newPos] = where newOrder[newPos] sat in oldStates. */
    private static int[] buildOldIndex(State[] oldStates, State[] newOrder) {
        int[] oldIndex = new int[newOrder.length];
        for (int newPos = 0; newPos < newOrder.length; newPos++) {
            for (int oldPos = 0; oldPos < oldStates.length; oldPos++) {
                if (oldStates[oldPos] == newOrder[newPos]) {
                    oldIndex[newPos] = oldPos;
                    break;
                }
            }
        }
        return oldIndex;
    }

    @Override public Potential deepCopy(ProbNet copyNet) {
        ICIPotential potential = (ICIPotential) super.deepCopy(copyNet);
        potential.expandedPotential = this.expandedPotential == null ? null : (TablePotential) this.expandedPotential.deepCopy(copyNet);
        potential.family = this.family;
        potential.modelType = this.modelType;
        potential.leakyParameters = this.leakyParameters.clone();
        if (this.leakyVariable != null) {
            potential.leakyVariable = copyNet.getVariable(this.leakyVariable.getName());
        }
        potential.noisyParameters = this.noisyParameters.clone();
        potential.zVariables = new HashMap<>(this.zVariables);
        return potential;
    }
    
}
