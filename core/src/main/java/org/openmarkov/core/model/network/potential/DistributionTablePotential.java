package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction.ParametrizedFunctionManager;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionWithKnownInverseCDF;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


/**
 * Potential when the parameters of the probabilistic distribution which describe the TTE in an Event variable may vary in base of the Configuration of the parents.
 * This potential is described by the probabilistic distribution chosen and a Table where there are the values of the distribution parameters.
 * The rows of the Table represent the distribution parameters of the distribution, and may be indicated by a number or a Funcion of the parents with Numeric Variables.
 * Each configuration of the parents are formed by a state of every Chance Variable and a Event parent. There have to be at least one Event variable.
 * Parents with Numeric Variables are treated as "Function Parameters", and may be used to configure the values of the distribution parameters.
 * This potential may have impossible configuration of parents
 * TODO Rephrase.
 *
 * @author cmyago
 * @version 1.5 -07/10/2022 -cmyago- changed to be used also with continuous variables (UTILITY and CHANCE nodes)
 */

@PotentialType(names = "DistributionTable")
public class DistributionTablePotential extends Potential implements DESSimulablePotential {
    //14/08/2022 ImpossibleConfiguration removed
//        implements ImpossibleConfiguration {


    /**
     * Probabilistic distribution for computing TTE of every configuration
     */
    private String distributionName;

    /**
     * Parametrization of distributionName
     */
    private String parametrizationName;

    /**
     * Name of the parameters of the probabilistic distribution for computing TTE of every configuration
     */
    private String[] distributionParameters;

    /**
     * Variable in which each state is one parameter of the distribution
     */
    private Variable distributionVariable;

    /**
     * ArrayList of parents with continuous variables. Empty if there is not continuous variables
     */
    private ArrayList<Variable> numericVariables;

    /**
     * TableWithEvents which stores the values of the distribution parameters
     */
    private TableWithEvents tableWithEvents;


    /**
     * Constructor of a DistributionTablePotential with probabilistic distribution "Exact"
     *
     * @param variables List of Variable whose first element is the node Variable and the rest are the Variable of the parents
     * @param role      role assumed by the potential
     */
    public DistributionTablePotential(List<Variable> variables, PotentialRole role) {
        this(variables, role, "Exact", "Nu");

    }

    /**
     * Constructor of DistributionTablePotential with probabilistic distribution given by distributionName
     *
     * @param variables        List of Variable whose first element is the node Variable and the rest are the Variable of the parents
     * @param role             role assumed by the potential
     * @param distributionName name of the probabilistic distribution used to compute TTE of the node
     */
    public DistributionTablePotential(List<Variable> variables, PotentialRole role, String distributionName, String parametrizationName) {
        super(variables, role);
        changeDistribution(distributionName, parametrizationName);
    }


    /**
     * Constructor of DistributionTablePotential which creates an object with the same values as potential.
     *
     * @param potential DistributionTablePotential whose values are used to create the new object
     */
    public DistributionTablePotential(DistributionTablePotential potential) {
        this(potential.getVariables(), potential.getPotentialRole(), potential.getDistributionName(), potential.getParametrizationName());
        this.setTableWithEvents(potential.getTableWithEvents());
    }

    public DistributionTablePotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }


    /**
     * Returns true if the node has type Event and one of its parents is an event
     * This potential makes sense when at least one of the parents
     * is an event.
     *
     * @param node      . <code>Node</code> where the potential is set
     * @param variables . <code>List</code> of <code>Variable</code>.
     * @param role      . <code>PotentialRole</code>.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        VariableType variableType = variables.getFirst().getVariableType();
        return ((node.getNodeType() == NodeType.EVENT) || (variableType == VariableType.NUMERIC));

    }


    /**
     * Changes the probabilistic distribution contained is this potential. The values of the distribution parameters are se to default.
     *
     * @param distributionName
     * @param distributionParametrization name of the new probabilistic distribution
     */
    public void changeDistribution(String distributionName, String distributionParametrization) {
        try {
            this.distributionName = distributionName;
            this.parametrizationName = distributionParametrization;
            List<String> distributionParametersList = ParametrizedFunctionManager.getUniqueInstance().getParameters(distributionName, distributionParametrization);
            distributionParameters = distributionParametersList.toArray(new String[distributionParametersList.size()]);

            State[] states = new State[distributionParameters.length];
            for (int i = 0; i < distributionParameters.length; i++) {
                states[i] = new State(distributionParameters[i]);
            }
            setDistributionVariable(new Variable("distributionVariable", states));
            List<Variable> tableParents = new ArrayList<Variable>();
            tableParents.add(getDistributionVariable());
            tableParents.addAll(variables.subList(1, variables.size()));
            tableParents.removeIf(v -> v.getVariableType() == VariableType.NUMERIC);
            //Adding numeric parents
            numericVariables = new ArrayList<>();
            numericVariables.addAll(variables.subList(1, variables.size()));
            numericVariables.removeIf(v -> v.getVariableType() != VariableType.NUMERIC);
            this.tableWithEvents = new TableWithEvents(tableParents, role, (numericVariables.size() > 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if this instance of DistributionTablePotential values may have functions as a value in a cell
     *
     * @return true if this instance of DistributionTablePotential values may have functions as a value in a cell. False otherwise
     */
    public boolean hasFunctionValues() {
        return !(numericVariables.isEmpty());
    }

    /**
     * Returns the TableWithEvents object where the distribution parameters are stored
     *
     * @return the TableWithEvents object where the  distribution parameters (numeric and function) are stored
     */
    public TableWithEvents getTableWithEvents() {
        return tableWithEvents;
    }

    /**
     * Returns the TablePotential associated to its TableWithEvents object where the distribution parameters are stored
     *
     * @return the TablePotentialobject where the numeric distribution parameters are stored
     */
    public TablePotential getTablePotential() {
        return tableWithEvents.getTablePotential();
    }

    /**
     * Sets a TableWithEvents with the values for the distribution parameters
     * TODO Throw an Exception if the table is not compatible with the probabilistic distribution
     *
     * @param tableWithEvents the tableWithEvents to be stored
     */
    public void setTableWithEvents(TableWithEvents tableWithEvents) {
        this.tableWithEvents = tableWithEvents;
    }

    /**
     * Returns a String object with the name of the probabilistic distribution used for computing TTE
     *
     * @return String with the name of the probabilistic distribution used for computing TTE
     */
    public String getDistributionName() {
        return distributionName;
    }

    /**
     * Sets the probabilistic distribution used for computing TTE to distributionName
     *
     * @param distributionName String with the name of the distribution to be set
     */
    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    /**
     * Returns a Variable object with the distribution parameters as states.
     *
     * @return Variable with the distribution parameters as states
     */
    public Variable getDistributionVariable() {
        return distributionVariable;
    }

    /**
     * Sets a Variable object with the distribution parameters as states.
     *
     * @param distributionVariable Variable to be set
     */
    public void setDistributionVariable(Variable distributionVariable) {
        this.distributionVariable = distributionVariable;
    }

    /**
     * List of Variable with the Numeric Parent Variables which can be used as function parameters
     *
     * @return List of Variable with the Numeric Parent Variables which can be used as function parameters
     */
    public ArrayList<Variable> getNumericVariables() {
        return numericVariables;
    }

    /**
     * Sets List of Variable with the Numeric Parent Variables which can be used as function parameters.
     *
     * @param numericVariables
     */
    public void setNumericVariables(ArrayList<Variable> numericVariables) {
        this.numericVariables = numericVariables;
    }

    //14/08/2022 changed for nuisance variable
    //14/08/2022 FIXME revise functions use
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {

        //Extract FINITE_STATES and EVENT Variables and convert to the format of a TableWithEvents to find the position in the table
        EvidenceCase stateConfiguration = tableWithEvents.convert(parents);
        //Extract Numeric Variables which are the Function Variables
        EvidenceCase numericConfiguration = null;
        if (tableWithEvents.isUseTableWithFunctions()) {
            numericConfiguration = new Configuration();
            for (Variable numericVariable : numericVariables) {
                numericConfiguration.addFinding(parents.getFinding(numericVariable));
            }
        }
        //Extract parameters from the table
        ProbDensFunctionWithKnownInverseCDF distribution = null;
        int i = 0;
        double[] paramValues = new double[distributionParameters.length];
        try {
            for (State sParam : distributionVariable.getStates()) {
                Finding f = new Finding(distributionVariable, sParam);

                stateConfiguration.addFinding(f);

                if (tableWithEvents.isUseTableWithFunctions()) {
                    paramValues[i++] = tableWithEvents.getTableWithFunctions().getEvaluatedFunctionValue(stateConfiguration, numericConfiguration);
                } else {
                    paramValues[i++] = tableWithEvents.getTablePotential().getValue(stateConfiguration);
                }
                stateConfiguration.removeFinding(distributionVariable);
            }
            //24/10/2023 clazz.newInstance deprecated since Java 9
            distribution = ParametrizedFunctionManager.getUniqueInstance().getParametrizedClass(distributionName, parametrizationName).getDeclaredConstructor().newInstance();
//            distribution = ParametrizedFunctionManager.getUniqueInstance().getParametrizedClass(distributionName, parametrizationName).newInstance();
            distribution.setParameters(paramValues);
        } catch (InstantiationException | IllegalAccessException |InvocationTargetException | NoSuchMethodException e) {
            throw new UnrecoverableException(e);
        }
        //11/12/2022 I need to control the randomNumber sequence in order to avoid nuisance variance
        return distribution.getInverseCumulativeDistributionFunction(randomNumbers[0]);
    }

    /**
     * Parametrization of distributionName
     */
    public String getParametrizationName() {
        return parametrizationName;
    }

    public void setParametrizationName(String parametrizationName) {
        this.parametrizationName = parametrizationName;
    }


//    /**
//     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
//     * *  The event will happend at triggering_time + TTE
//     *
//     * @param findings one or more findings to create the Configuration for extracting TTE
//     * @param random
//     * @return
//     */
//    public double sampleVariable(List<Finding> findings, Random random) {
//
//        return sampleVariable(random, tableWithEvents.convert(findings));
//    }


    @Override
    public Potential copy() {
        return new DistributionTablePotential(this);
    }

    //Scale potential
    //TODO
    /**
     * Not yet implemented for this potential: does nothing.
     */
    @Override
    public void scalePotential(double scale) {
    }


    //03/01/2023; added after merge because it was added to Potential as an abstract method
    /**
     * Not supported by this potential: always returns {@code null}.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        return null;
    }
    //03/01/2023; added after merge because it was added to Potential as an abstract method
    /**
     * Not supported by this potential: always returns {@code null}.
     */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        return null;
    }

//ImpossibleConfiguration interface


    //Table projects
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }


    @Override
    public DistributionTablePotential project(EvidenceCase evidenceCase)
            throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }


    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                List<TablePotential> alreadyProjectedPotentials)
            throws NonProjectablePotentialException {
        // get the projected TablePotential, which will be returned inside a list
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }


    //Is uncertaing
    @Override
    public boolean isUncertain() {
        return false;
    }


}

