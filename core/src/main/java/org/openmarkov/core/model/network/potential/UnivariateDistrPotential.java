/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A potential defined by a univariate probability density function (e.g., Normal, Beta,
 * Gamma) whose parameters may depend on the parent variables. The distribution type
 * is specified by a {@link ProbDensFunction}
 * class, and its parameters are stored in an {@link AugmentedProbTable}. Used for numeric
 * conditioned variables.
 */
@PotentialType(names = "UnivariateDistr") public class UnivariateDistrPotential extends Potential {
    
    public static final String PSEUDO_VARIABLE = "pseudoVariableDistributionName";
    private static final VariableExpression INITIALIZATION_VALUE = new VariableExpression(Collections.emptyList(), "1");
    protected AugmentedProbTable distributionTable;
    /**
     * finiteStateVariables contains the node variable (Numeric) and the finite-states parents
     */
    protected List<Variable> finiteStatesVariables;
    /**
     * parameterStateVariables contains the numeric  parents
     */
    protected List<Variable> parameterVariables;
    protected Variable pseudoVariableDistribution;
    /**
     * Represents the probability distribution of the values of the table. It is internally described as a finite-states
     * variable whose states are given by the parameters of the distribution
     */
    private Class<? extends ProbDensFunction> probDensFunctionClass;
    private String probDensFunctionName;
    private String probDensFunctionUnivariateName;
    private String probDensFunctionParametrizationName;
    private String[] probDensFunctionParametersName;
    private ProbDensFunctionManager probDensFunctionManager;
    
    /**
     * Constructor
     *
     * @param variables List of variables
     * @param role      Potential role
     */
    
    public UnivariateDistrPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        if (this.role == null) {
            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
        }
        finiteStatesVariables = new ArrayList<>();
        parameterVariables = new ArrayList<>();
        
        for (Variable variable : variables.subList(1, variables.size())) {
            if ((variable.getVariableType() == VariableType.FINITE_STATES) || (
                    variable.getVariableType() == VariableType.DISCRETIZED
            )) {
                finiteStatesVariables.add(variable);
            } else {
                parameterVariables.add(variable);
            }
        }
        
        setProbDensFunctionClass(ExactFunction.class);
        setDistributionTable();
        
    }
    
    /**
     * Constructor
     *
     * @param variables       List of variables
     * @param name            Name
     * @param parametrization Parametrization
     * @param role            Potential role
     */
    public UnivariateDistrPotential(List<Variable> variables, String name, String parametrization, PotentialRole role) {
        
        this(variables, role);
        setProbDensFunctionClass(getProbDensFunction(name, parametrization));
        setDistributionTable();
    }
    
    /**
     * Constructor
     *
     * @param variables             List of variables
     * @param probDensFunctionClass Class of the probability density function
     * @param role                  Potential role
     */
    public UnivariateDistrPotential(List<Variable> variables, Class<? extends ProbDensFunction> probDensFunctionClass,
                                    PotentialRole role) {
        this(variables, role);
        setProbDensFunctionClass(probDensFunctionClass);
        setDistributionTable();
    }
    
    /**
     * Constructor
     *
     * @param potential Univariate distribution potential
     */
    public UnivariateDistrPotential(UnivariateDistrPotential potential) {
        
        super(potential);
        finiteStatesVariables = potential.getFiniteStatesVariables();
        parameterVariables = potential.getParameterVariables();
        
        setProbDensFunctionClass(potential.getProbDensFunctionClass());
        setDistributionTable((AugmentedProbTable) (potential.getDistributionTable()).copy());
        
    }
    
    /**
     * Constructor
     *
     * @param variables List of variables
     */
    public UnivariateDistrPotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    /**
     * Now it is always true
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      . {@code Node}
     * @param variables . {@code List} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        return (node.getVariable().getVariableType() == VariableType.NUMERIC);
        
    }
    
    /**
     * @return the probDensFunctionManager
     */
    public ProbDensFunctionManager getProbDensFunctionManager() {
        if (probDensFunctionManager == null) {
            probDensFunctionManager = ProbDensFunctionManager.getUniqueInstance();
        }
        return probDensFunctionManager;
    }
    
    /**
     * @param probDensFunctionManager the probDensFunctionManager to set
     */
    public void setProbDensFunctionManager(ProbDensFunctionManager probDensFunctionManager) {
        this.probDensFunctionManager = probDensFunctionManager;
    }
    
    public Class<? extends ProbDensFunction> getProbDensFunction(String univariateName, String parametrization) {
        
        return getProbDensFunctionManager().getProbDensFunctionClass(univariateName, parametrization);
    }
    
    /**
     * @return the distribution
     */
    public Class<? extends ProbDensFunction> getProbDensFunctionClass() {
        return probDensFunctionClass;
    }
    
    /**
     * @param distributionClass the distribution to set
     */
    public void setProbDensFunctionClass(Class<? extends ProbDensFunction> distributionClass) {
        this.probDensFunctionClass = distributionClass;
        ProbDensFunctionType annotation = distributionClass.getAnnotation(ProbDensFunctionType.class);
        probDensFunctionName = annotation.name();
        probDensFunctionUnivariateName = annotation.univariateName();
        if (probDensFunctionUnivariateName.equals("default")) {
            probDensFunctionUnivariateName = probDensFunctionName;
        }
        probDensFunctionParametersName = annotation.parameters();
        setProbDensFunctionParametrizationName(probDensFunctionParametersName[0]);
        for (int i = 1; i < probDensFunctionParametersName.length; i++) {
            setProbDensFunctionParametrizationName(
                    getProbDensFunctionParametrizationName() + ", " + probDensFunctionParametersName[i]);
        }
        translateDistributionIntoPseudoVariable(probDensFunctionParametersName);
    }
    
    /**
     * @return the probDensFunctionName
     */
    public String getProbDensFunctionName() {
        return probDensFunctionName;
    }
    
    /**
     * @param probDensFunctionName the probDensFunctionName to set
     */
    public void setProbDensFunctionName(String probDensFunctionName) {
        this.probDensFunctionName = probDensFunctionName;
    }
    
    /**
     * @return the probDensUnivariateParameters
     */
    public String[] getProbDensFunctionParametersName() {
        return probDensFunctionParametersName;
    }
    
    /**
     * @param probDensFunctionParametersName the probDensUnivariateParameters to set
     */
    public void setProbDensFunctionParametersName(String[] probDensFunctionParametersName) {
        this.probDensFunctionParametersName = probDensFunctionParametersName;
    }
    
    /**
     * @return the probDensUnivariateName
     */
    public String getProbDensFunctionUnivariateName() {
        return probDensFunctionUnivariateName;
    }
    
    /**
     * @param probDensUnivariateName the probDensUnivariateName to set
     */
    public void setProbDensFunctionUnivariateName(String probDensUnivariateName) {
        this.probDensFunctionUnivariateName = probDensUnivariateName;
    }
    
    public String getProbDensFunctionParametrizationName() {
        return probDensFunctionParametrizationName;
    }
    
    public void setProbDensFunctionParametrizationName(String probDensFunctionParametrizationName) {
        this.probDensFunctionParametrizationName = probDensFunctionParametrizationName;
    }
    
    /**
     * @return the finiteStatesVariables
     */
    public List<Variable> getFiniteStatesVariables() {
        return finiteStatesVariables;
    }
    
    /**
     * @param finiteStatesVariables the finiteStatesVariables to set
     */
    public void setFiniteStatesVariables(List<Variable> finiteStatesVariables) {
        this.finiteStatesVariables = finiteStatesVariables;
    }
    
    /**
     * @return the parameterVariables
     */
    public List<Variable> getParameterVariables() {
        return parameterVariables;
    }
    
    /**
     * @param parameterVariables the parameterVariables to set
     */
    public void setParameterVariables(List<Variable> parameterVariables) {
        this.parameterVariables = parameterVariables;
    }
    
    /**
     * @param probDensFunctionParametersName Probability function parameters name
     */
    protected void translateDistributionIntoPseudoVariable(String[] probDensFunctionParametersName) {
        pseudoVariableDistribution = new Variable(PSEUDO_VARIABLE, probDensFunctionParametersName);
    }
    
    /**
     * @return the pseudoVariableDistribution
     */
    public Variable getPseudoVariableDistribution() {
        return pseudoVariableDistribution;
    }
    
    /**
     * @param pseudoVariableDistribution the pseudoVariableDistribution to set
     */
    public void setPseudoVariableDistribution(Variable pseudoVariableDistribution) {
        this.pseudoVariableDistribution = pseudoVariableDistribution;
    }
    
    public AugmentedProbTable getAugmentedProbTable() {
        return distributionTable;
    }
    
    public AugmentedProbTable getDistributionTable() {
        return distributionTable;
    }
    
    public void setDistributionTable(AugmentedProbTable tableDistr) {
        this.distributionTable = tableDistr;
    }
    
    public void setDistributionTable() {
        List<Variable> vDistributionTable = new ArrayList<>(finiteStatesVariables);
        vDistributionTable.addFirst(pseudoVariableDistribution);
        setDistributionTable(new AugmentedProbTable(vDistributionTable, role));
        initializeAugmentedProbTable();
    }
    
    /**
     * Sets every cell of the distribution table to the default initialization expression.
     */
    protected void initializeAugmentedProbTable() {
        VariableExpression[] functionValues = distributionTable.getFunctionValues();
        Arrays.fill(functionValues, INITIALIZATION_VALUE);
    }
    
    /**
     * Verifies that the given values are valid parameters for the configured probability
     * density function, delegating to the function's own parameter check.
     *
     * @param values candidate distribution parameter values
     */
    public void checkDistributionValues(double[] values) {
        ProbDensFunction p = getProbDensFunctionManager().newInstance(probDensFunctionName, values);
        p.verifyParameters(values);
    }
    
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override public UnivariateDistrPotential project(EvidenceCase evidenceCase) {
        return null;
    }
    
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override public Potential copy() {
        return new UnivariateDistrPotential(this);
    }

    @Override public void scalePotential(double scale) {
        this.getDistributionTable().scalePotential(scale);
    }
    
    public Variable getChildVariable() {
        return this.getVariable(0);
    }
    
    public void setChildVariable(Variable childVariable) {
        this.getVariables().set(0, childVariable);
    }
    
    public UncertainValue[] getUncertainValues() {
        return getDistributionTable().getUncertainValues();
    }
    
    public void setUncertainValues(UncertainValue[] uncertainValues) {
        getDistributionTable().setUncertainValues(uncertainValues);
    }
    
    public double[] getValues() {
        return getDistributionTable().getValues();
    }
    
    public void setValues(double[] values) {
        this.getDistributionTable().setValues(values);
    }
    
    @Override public String toString() {
        String out = variables.getFirst().getName();
        if (variables.size() == 1) {
            out += " = ";
        } else if (variables.size() > 1) {
            out += " | ";
            // Print variables
            out += variables.stream().skip(1).map(Variable::getName).collect(Collectors.joining(", "));
            out += " = ";
        }
        out += "UnivariateName" + probDensFunctionUnivariateName + " Parametrization" + probDensFunctionParametrizationName + " ";
        
        if (getDistributionTable().getValues().length == 1) {
            out += getDistributionTable().getValues()[0];
        } else if (getDistributionTable().getValues().length > 1) {
            out += "{" + Arrays.stream(getDistributionTable().getValues())
                               .mapToObj(Double::toString)
                               .collect(Collectors.joining(", ")) + "}";
        }
        return out.toString();
    }
    
    /**
     * Returns a copy with the finite-state variables reordered as given; the numeric parameter
     * variables are re-added and the distribution table is reordered accordingly, keeping the
     * pseudo-variable as its first variable.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        int size = newOrderOfVariables.size();
        // orderVariables has the order of the parents of the AugmentedProbTable, so
        // parameterVariables should be added
        newOrderOfVariables.addAll(getParameterVariables());
        UnivariateDistrPotential newPotential = new UnivariateDistrPotential(newOrderOfVariables,
                                                                             getProbDensFunctionClass(), getPotentialRole());
        newOrderOfVariables.removeFirst();
        // I do use getVariable(0) for be compliant with the comparison in int[]
        // accOffsets = potential.getAccumulatedOffsets(orderVariables);
        newOrderOfVariables.addFirst(getAugmentedProbTable().getVariable(0));
        AugmentedProbTable newDistributionTable = (AugmentedProbTable) getAugmentedProbTable()
                .reorder(newOrderOfVariables.subList(0, size));
        newPotential.setDistributionTable(newDistributionTable);
        return newPotential;
    }
    
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        UnivariateDistrPotential copy = new UnivariateDistrPotential(this);
        AugmentedProbTable reorderedTable = distributionTable.reorder(variable, newOrder);
        copy.setDistributionTable(reorderedTable);
        return copy;
    }
    
}
