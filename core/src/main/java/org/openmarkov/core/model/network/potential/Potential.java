/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredMethod;
import org.openmarkov.core.developmentStaticAnalysis.requirements.SelfClass;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.java.cloneUtils.CloneUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for all potentials in OpenMarkov. A potential represents a
 * conditional probability table (CPT), a utility function, or any parametric
 * distribution associated with a node in a probabilistic graphical model.
 * <p>
 * Subclasses must implement {@link #project} and {@link #copy}.
 * The methods {@link #tableProject}, {@link #isUncertain}, {@link #scalePotential},
 * and {@link #reorder} have default implementations in this class that either return
 * a safe default ({@link #isUncertain}) or throw {@link UnsupportedOperationException}
 * / {@link NonProjectablePotentialException}. Subclasses should override them as needed.
 * <p>
 * Potentials are discovered at runtime via the {@code @PotentialType} annotation
 * and the plugin system.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@ImplementationRequirements(
        requiresOneOfTheseConstructors = {
                @RequiredConstructor({List.class, CycleLength.class}),
                @RequiredConstructor({List.class, PotentialRole.class}),
                @RequiredConstructor(List.class),
                @RequiredConstructor(SelfClass.class)
        },
        requiresMethods = @RequiredMethod(
                methodKind = RequiredMethod.MethodKind.Instance, methodName = "validate",
                returnType = Boolean.class, parameters = {Node.class, List.class, PotentialRole.class}
        )
)
public abstract class Potential implements Localizable {
    
    // Constants
    /**
     * Maximum size of a String used in toString()
     */
    protected static final int STRING_MAX_LENGTH = 300;
    // Attributes
    /**
     * This object contains all the information that the parser reads from disk
     * that does not have a direct connection with the attributes stored in the
     * {@code Potential} object.
     */
    public Map<String, Object> properties;
    //    /**
    //     * Utility variable associated to the <code>Node</code> that contains
    //     * this potential.
    //     */
    //    protected Variable             utilityVariable;
    
    protected List<Variable> variables;
    /**
     * Decision criterion. It is used only during inference. In edition,
     * the node/variable has a criterion, but the potential does not.
     */
    protected Criterion criterion;
    
    protected PotentialRole role;
    protected String comment = "";
    
    // Constructor
    
    /**
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}
     */
    public Potential(List<Variable> variables, PotentialRole role) {
        this.variables = variables != null ? new ArrayList<>(variables) : new ArrayList<>();
        properties = new HashMap<>();
        this.role = role;
    }
    
    //    /**
    //     * @param variables <code>List</code> of <code>Variable</code>s.
    //     * @param utilityVariable
    //     */
    //    public Potential (Variable utilityVariable, List<Variable> variables)
    //    {
    //        this(variables, PotentialRole.UTILITY);
    //        this.utilityVariable = utilityVariable;
    //    }
    
    /**
     * Copy-constructor helper for subclasses. {@link Potential} is abstract, so this
     * constructor is never invoked from outside the hierarchy: subclasses call it via
     * {@code super(potential)} from their own copy constructor to replicate the shared
     * state (variables, role, comment, criterion). Public copying is offered through
     * {@link #copy()}, which every subclass overrides.
     *
     * @param potential potential whose shared state is copied into the new instance
     */
    protected Potential(Potential potential) {
        this(potential.getVariables(), potential.getPotentialRole());
        this.comment = potential.getComment();
        this.criterion = CloneUtils.safeClone(potential.getCriterion());
    }
    
    // Methods
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      {@code Node}
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}.
     *
     * @return if an instance of a certain Potential type makes sense given the variables and the potential role.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        // Default implementation: always return true
        return true;
    }
    
    /**
     * Converts a variable array to a list.
     *
     * @param variables array of variables
     *
     * @return a new {@code List} containing the given variables
     */
    protected static List<Variable> toList(Variable[] variables) {
        List<Variable> variablesArrayList = new ArrayList<>();
        Collections.addAll(variablesArrayList, variables);
        return variablesArrayList;
    }
    
    /**
     * Finds the first potential in the list whose conditioned variable matches the given variable.
     *
     * @param variable   the variable to search for
     * @param potentials list of table potentials to search
     *
     * @return the matching potential, or {@code null} if not found
     */
    protected static TablePotential findPotentialByVariable(Variable variable, List<TablePotential> potentials) {
        int i = 0;
        TablePotential potential = null;
        while (i < potentials.size() && potential == null) {
            if (variable.equals(potentials.get(i).getConditionedVariable())) {
                potential = potentials.get(i);
            }
            ++i;
        }
        return potential;
    }
    
    /**
     * @param evidenceCase {@code EvidenceCase}
     *
     * @return The conditional probability table of this potential given the
     * evidence
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public TablePotential getCPT(EvidenceCase evidenceCase)
            throws NonProjectablePotentialException {
        List<TablePotential> potentials = Collections.singletonList(tableProject(evidenceCase, null));
        HashSet<Variable> variablesToEliminate = new HashSet<>();
        // Fill it with variables appearing in all potentials except this
        for (TablePotential tablePotential : potentials) {
            variablesToEliminate.addAll(tablePotential.getVariables());
        }
        variables.forEach(variablesToEliminate::remove);
        return DiscretePotentialOperations
                .multiplyAndMarginalize(potentials, variables, new ArrayList<>(variablesToEliminate));
    }
    
    /**
     * The conditional probability table given by this potential
     *
     * @return {@code TablePotential}
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public TablePotential getCPT() throws NonProjectablePotentialException {
        return getCPT(new EvidenceCase());
    }
    
    /**
     * Checks if all the variables belongs to the type received. The utility
     * variable is not considered.
     *
     * @return {@code boolean}
     */
    protected boolean noNumericVariables() {
        if (variables != null) {
            for (Variable variable : variables) {
                if (variable.getVariableType() == VariableType.NUMERIC) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * @return A {@code List} of {@code Variable}s
     */
    public List<Variable> getVariables() {
        return new ArrayList<>(variables);
    }
    
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
    
    /**
     * @param position Position
     *
     * @return The variable in the place {@code position}
     */
    public Variable getVariable(int position) {
        return variables.get(position);
    }
    
    /**
     * Replaces one variable with another in this potential's variable list.
     *
     * @param variableToReplace the variable to be replaced
     * @param variable          the replacement variable
     */
    public void replaceVariable(Variable variableToReplace, Variable variable) {
        // TODO - Check if OOPN and ConditionalGaussian potential are still running
        //        if (variableToReplace.equals (utilityVariable))
        //        {
        //        	utilityVariable = variable;
        //        }
        //        else
        
        if (variables.contains(variableToReplace)) {
            replaceVariable(variables.indexOf(variableToReplace), variable);
        }
    }
    
    /**
     * Replaces the variable at the given position with a new variable.
     *
     * @param position index of the variable to replace
     * @param variable the replacement variable
     */
    public void replaceVariable(int position, Variable variable) {
        variables.remove(position);
        variables.add(position, variable);
    }
    
    /**
     * @param variable {@code Variable}
     *
     * @return {@code true} if contains the received {@code Variable}.
     */
    public boolean contains(Variable variable) {
        return variables.contains(variable);
    }
    
    /**
     * @param evidenceCase               {@code EvidenceCase}
     * @param inferenceOptions           Inference options
     * @param alreadyProjectedPotentials {@code List} of already projected potentials
     *
     * @return List of potentials resulting from the projection
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                List<TablePotential> alreadyProjectedPotentials)
            throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    //    /** @return isUtility <code>boolean</code> */
    //    public boolean isUtility ()
    //    {
    //        return role == PotentialRole.UTILITY;
    //    }
    
    /**
     * Projects this potential onto the given evidence, returning a single table potential.
     * Convenience overload that delegates to the three-argument version with an empty list.
     *
     * @param evidenceCase     evidence to project onto
     * @param inferenceOptions inference options
     *
     * @return the projected table potential
     *
     * @throws NonProjectablePotentialException if the potential cannot be projected
     */
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException {
        return tableProject(evidenceCase, inferenceOptions, new ArrayList<TablePotential>());
    }
    
    /**
     * Projects this potential onto the given evidence, returning a potential
     * (not necessarily a {@link TablePotential}).
     *
     * @param evidenceCase evidence to project onto
     *
     * @return the projected potential
     *
     * @throws NonProjectablePotentialException if the potential cannot be projected
     */
    public abstract Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException;
    
    /**
     * @return isAdditive {@code boolean}
     * Whether the potential is additive in the inference. Only the potentials that are in a Markov network
     * and were associated to a utility node/variable in the original network have a criterion and we must maximize them.
     * This characterization of "utility potentials" is relevant only during inference.
     */
    public boolean isAdditive() {
        return criterion != null;
    }
    
    /**
     * @return number of variables: {@code int}
     */
    public int getNumVariables() {
        return variables.size();
    }
    
    public Variable getConditionedVariable() {
        return variables.isEmpty() ? null : variables.getFirst();
    }
    
    /**
     * Generates new {@code Finding}s generated by an
     * {@code EvidenceCase}. In principle this method does not generate any
     * new finding, but it is overridden in some of its subclasses.
     *
     * @param evidenceCase {@code EvidenceCase}
     *
     * @return {@code Collection} of {@code Finding}s
     */
    public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
        return new ArrayList<>();
    }
    
    /**
     * @return role. {@code PotentialRole}
     */
    public PotentialRole getPotentialRole() {
        return role;
    }
    
    /**
     * Modifies the frozen variable role. This method exists to avoid some
     * problems with legacy code in DiscretePotentialOperations class and it
     * does not be used except in very special cases.
     *
     * @param role {@code PotentialRole}
     */
    public void setPotentialRole(PotentialRole role) {
        this.role = role;
    }
    
    /**
     * @return comment. {@code String}
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param comment {@code String}
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * Shifts the potential in time as indicated by {@code timeDifference}.<p>
     * Subclasses of Potential must override this method.
     *
     * @param timeDifference {@code int}
     * @param probNet        This parameter is necessary because the shifted variables
     *                       are taken from the network. {@code ProbNet}
     */
    public void shift(ProbNet probNet, int timeDifference) {
        setVariables(getShiftedVariables(probNet, timeDifference));
    }
    
    /**
     * Creates links between the first variable of a potential and the rest of the variables.
     * <p>
     * Condition: The role of the potential must be utility of conditional
     * probability
     * Condition: The network must contain all the variables of the potential
     *
     * @param probNet Network
     */
    public void createDirectedLinks(ProbNet probNet) {
        int numVariables = variables.size();
        if (numVariables > 1) {
            Variable childVariable = variables.getFirst();
            for (int i = 1; i < numVariables; i++) {
                probNet.addLink(variables.get(i), childVariable, true);
                
            }
        }
    }
    
    /**
     * Returns a list with the same variables as this potential, including the
     * utility variable but shifted in time as indicated by timeDifference
     *
     * @param probNet        Network
     * @param timeDifference Time difference
     *
     * @return a list with the same variables as this potential, including the
     * utility variable but shifted in time as indicated by timeDifference
     * Condition: The network must contain the shifted variables.
     */
    public List<Variable> getShiftedVariables(ProbNet probNet, int timeDifference) {
        List<Variable> shiftedVariables = new ArrayList<>(variables.size());
        
        // also shift variables within the tree
        for (Variable variable : variables) {
            if (variable.isTemporal()) {
                shiftedVariables.add(probNet.getShiftedVariable(variable, timeDifference));
            } else {
                shiftedVariables.add(variable);
            }
        }
        
        return shiftedVariables;
    }
    
    /**
     * Overrides {@code toString} method. Mainly for test purposes
     */
    public String toString() {
        return toShortString();
    }
    
    /**
     * Returns a compact string representation of this potential, showing variables
     * and role (e.g., "P(X | Y, Z)" for conditional probability).
     *
     * @return short string representation
     */
    public String toShortString() {
        String out = new String();
        int numVariables = (variables != null) ? variables.size() : 0;
        if (numVariables != 0) { // Constant potential
            switch (role) {
                case CONDITIONAL_PROBABILITY -> {
                    out += "P(" + variables.getFirst().getName();
                    if (numVariables > 1) {
                        out += " | " + stringifyVariables(1);
                    }
                    out += ")";
                }
                case JOINT_PROBABILITY -> {
                    out += "P(" + stringifyVariables(0) + ")";
                }
                case null, default -> {
                    out += numVariables + " Variables: " + variables.getFirst().getName();
                    for (int i = 1; i < numVariables - 1; i++) {
                        out += ", " + variables.get(i).getName();
                    }
                    if (numVariables > 1) {
                        out += ", " + variables.get(numVariables - 1).getName();
                    }
                }
            }
        }
        return out;
    }
    
    @Override public @NotNull String path() {
        return "";
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        return this.toShortString();
    }
    
    
    /**
     * Prints in buffer the variables and in case of TablePotential the
     * configurations
     */
    private String stringifyVariables(int firstVariable) {
        // Print variables
        return variables.stream().skip(firstVariable).map(Variable::getName).collect(Collectors.joining(", "));
    }
    
    /**
     * Returns a string representation suitable for display in a Tree/ADD potential.
     *
     * @return tree ADD string representation
     */
    public String treeADDString() {
        return toString();
    }
    
    /**
     * @return A sampled potential. By default, itself, i.e., not sampled.
     * TODO This method must be commented further
     */
    public Potential sample() {
        return this; // By default
    }
    
    @Override public boolean equals(Object arg0) {
        if (arg0.getClass() != this.getClass()) {
            return false;
        }
        Potential potential = (Potential) arg0;
        return variables.equals(potential.variables) && role == potential.role;
    }
    
    /**
     * When this potential represents a conditional probability, returns a value for the first variable,
     * sampled with the probability distribution. If this variable is finite-states, it returns the index of
     * the sampled state. If the variable is numeric, it returns the value sampled.
     *
     * <p>Default implementation throws {@link UnsupportedOperationException}; subclasses that support
     * sampling must override it. A future redesign may promote this to a dedicated
     * {@code Sampleable} capability interface.</p>
     *
     * @param randomGenerator Random generator
     * @param sampledParents  Sampled parents
     *
     * @return a value for the first variable, sampled with the probability distribution.
     * @throws UnsupportedOperationException if this potential does not support sampling.
     */
    public int sampleConditionedVariable(Random randomGenerator, Map<Variable, Integer> sampledParents) {
        throw new UnsupportedOperationException(
                "sampleConditionedVariable not implemented for " + getClass().getSimpleName());
    }
    
    /**
     * Return a copy instance of the potential
     *
     * @return potential copy
     */
    public abstract Potential copy();
    
    /**
     * Return true if potential has uncertainty values
     *
     * @return whether the potential has uncertainty or not
     */
    public boolean isUncertain() {
        return false;
    }
    
    /**
     * Adds variable to a potential implemented in each child class
     *
     * @param variable Variable
     *
     * @return Uniform potential
     */
    public Potential addVariable(Variable variable) {
        Potential newPotential;
        if (!variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.add(variable);
            newPotential = new UniformPotential(newVariables, role);
        } else {
            //DESnets - 18/03/2023 - due to self-loop, the conditioned variable can be repeated - FIXME
            if (variable.equals(variables.get(0))){
                List<Variable> newVariables = new ArrayList<Variable>(variables);
                newVariables.add(variable);
                newPotential = new UniformPotential(newVariables, role);
            } else{
                newPotential = this;
            }
        }
        return newPotential;
    }
    
    /**
     * Creates a new uniform potential removing the received variable from the variables list of this potential.
     *
     * @param variable Variable
     *
     * @return Uniform potential
     */
    public Potential removeVariable(Variable variable) {
        Potential newPotential;
        if (variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.remove(variable);
            newPotential = new UniformPotential(newVariables, role);
        } else {
            newPotential = this;
        }
        return newPotential;
    }
    
    /**
     * Returns the probability for the given configuration of state indices.
     *
     * @param sampledStateIndexes map from each variable to its state index
     *
     * @return the probability value for the configuration
     *
     * @throws NonProjectablePotentialException if the potential cannot compute the probability
     */
    public double getProbability(HashMap<Variable, Integer> sampledStateIndexes) throws NonProjectablePotentialException {
        return 0;
    }
    
    /**
     * Returns the probability for the configuration specified by the evidence case.
     *
     * @param evidenceCase evidence case defining the variable-state configuration
     *
     * @return the probability value for the configuration
     *
     * @throws NonProjectablePotentialException if the potential cannot compute the probability
     */
    public double getProbability(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
        HashMap<Variable, Integer> configuration = new HashMap<>();
        for (Finding finding : evidenceCase.getFindings()) {
            configuration.put(finding.getVariable(), finding.getStateIndex());
        }
        return getProbability(configuration);
    }
    
    /**
     * Replaces a numeric variable with its discretized counterpart, matching by name.
     *
     * @param convertedParentVariable the discretized version of a previously numeric variable
     */
    public void replaceNumericVariable(Variable convertedParentVariable) {
        int varIndex = -1;
        for (int i = 0; i < variables.size(); ++i) {
            if (variables.get(i).getName().equals(convertedParentVariable.getName())) {
                varIndex = i;
            }
        }
        if (varIndex != -1) {
            variables.set(varIndex, convertedParentVariable);
        }
    }
    
    /**
     * Multiply the potential by a scale. If the Potential is not scalable it must throw NotSupportedOperationException
     *
     * @param scale Scale
     */
    public void scalePotential(double scale) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not scalable");
    }
    
    /**
     * Copy this potential attributes to the newPotential potential of the copyNet
     *
     * @param copyNet Network
     *
     * @return A deep copy of the potential
     */
    public Potential deepCopy(ProbNet copyNet) {
        Potential potential;
        try {
            //this creates an instance of the subclass
            potential = this.getClass().getConstructor(this.getClass()).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new UnreachableException(e);
        }
        
        List<Variable> newReferences = new ArrayList<>();
        for (Variable variable : this.variables) {
            newReferences.add(copyNet.getVariable(variable.getName()));
        }
        
        potential.setVariables(newReferences);
        potential.setPotentialRole(this.getPotentialRole());
        potential.setComment(this.comment);
        
        return potential;
    }
    
    public Criterion getCriterion() {
        return criterion;
    }
    
    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }
    
    
    /**
     * Copy this potential to another potential with the same variables but
     * with the order received in {@code newOrderOfVariables}
     *
     * @param newOrderOfVariables {@code ArrayList} of {@code Variable}
     *
     * @return The {@code Potential} generated Condition:
     * {@code newOrderOfVariables} are the same variables than the variables of this potential
     */
    public Potential reorder(List<Variable> newOrderOfVariables) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not reorderable");
    }
    
    /**
     * Copy this potential to another potential with the same variables but
     * with changes in the order of states in one of the variables
     *
     * @param variable {@code VariableList} whose order of states has changed
     * @param newOrder array of {@code State}s in the new order
     *
     * @return The {@code Potential} generated
     */
    public Potential reorder(Variable variable, State[] newOrder) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not reorderable");
    }
}
