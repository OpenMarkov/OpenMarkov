package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement a table with Events in the configuration of parents
 *
 * @author cmyago
 * @version 2.0 -14/08/2022 - changed for rework sampling  for avoiding nuisance variance
 * FIXME Check if this has to extend Potential
 *
 */


@PotentialType(names = "TableWithEvents")
public class TableWithEvents extends Potential implements DESSimulablePotential {
    //14/08/2022  ImpossibleConfiguration removed
    //implements ImpossibleConfiguration {
    
    protected TablePotential tablePotential = null;
    
    /**
     * True if a tableWithFunctions is used
     */
    protected boolean useTableWithFunctions = false;
    protected TableWithFunctions tableWithFunctions = null;
    
    protected Variable events = null;
    
    protected List<Variable> tableVariables = null;
    
    //Incompatible configurations. Looking the better way of representing them.
    protected boolean hasImpossibleConfigurations;
    protected ArrayList<Configuration> impossibleConfigurations;
    
    
    public TableWithEvents(List<Variable> variables, PotentialRole role) {
        this(variables, role, false);
    }
    
    
    /**
     *
     * @param variables
     * @param role
     * @param useTableWithFunctions true if the node containing this potential has parents with variables with VariableType.NUMERIC
     */
    public TableWithEvents(List<Variable> variables, PotentialRole role, boolean useTableWithFunctions) {
        
        super(variables, role);
        List<Variable> parents = variables.subList(1, variables.size());
        setEventAsStates(parents);
        tableVariables = new ArrayList<>();
        tableVariables.add(variables.get(0));
        
        for (Variable variable : parents) {
            if (variable.getVariableType() != VariableType.EVENT)
                tableVariables.add(variable);
        }
        if (events != null) {
            tableVariables.add(events);
        }
        //At the moment there is always a TablePotential
        setTablePotential(new TablePotential(tableVariables, role));
        //TablePotential when there is no numeric parents, TableWithFunctions when there are numeric parents
        this.useTableWithFunctions = useTableWithFunctions;
        if (useTableWithFunctions) {
            tableWithFunctions = new TableWithFunctions(tableVariables, role);
            
        }
        impossibleConfigurations = new ArrayList<>();
        
    }
    
    
    //TODO
    public TableWithEvents(TableWithEvents potential) {
        super(potential);
        this.setTablePotential(new TablePotential(potential.getTablePotential()));
    }
    
    
    /**
     * Returns true  if certain Potential type makes sense given the
     * variables and the potential role.
     * This potential makes sense when at least one of the parents
     * is an event and no event parents have Finite States or Discretized variables
     *
     * @param node      . <code>Node</code> where the potential is set
     * @param variables . <code>List</code> of <code>Variable</code>.
     * @param role      . <code>PotentialRole</code>.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        
        return false;
    }
    
    
    /**
     * Extract the event parents and fill the states of eventAsStates with its names. For example,
     * if node A has as parents events E1, E2 and E3 eventAsStates will be a Finite-States variable with
     * three states: E1, E2, E3
     *
     * @param variables
     */
    public void setEventAsStates(List<Variable> variables) {
        ArrayList<State> states = new ArrayList<>();
        int i = 0;
        for (Variable variable : variables) {
            if (variable.getVariableType() == VariableType.EVENT) {
                states.add(new State(variable.getName()));
            }
        }
        events = null;
        if (!states.isEmpty()) {
            events = new Variable("Events", states.toArray(new State[0]));
        }
    }
    
    
    /**
     * Converts a Configuration of variables to the Configuration structure of a TableWithEvents
     *
     * @param configuration configuration to be converted
     *
     * @return a Configuration of variables with Configuration structure of a TableWithEvents
     */
    public EvidenceCase convert(EvidenceCase configuration) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        EvidenceCase tteConfiguration = new EvidenceCase();
        for (Finding finding : configuration.getFindings()) {
            switch (finding.getVariable().getVariableType()) {
                case EVENT:
                    tteConfiguration.addFinding(new Finding(events, events.getState(finding.getVariable().getName())));
                    break;
                case FINITE_STATES:
                    tteConfiguration.addFinding(finding);
                    break;
                default:
                    break;
            }
        }
        return tteConfiguration;
    }
    
    
    public Variable getEvents() {
        return events;
    }
    
    public void setEvents(Variable events) {
        this.events = events;
    }
    
    public TablePotential getTablePotential() {
        return tablePotential;
    }
    
    public void setTablePotential(TablePotential tablePotential) {
        this.tablePotential = tablePotential;
    }
    
    public TableWithFunctions getTableWithFunctions() {
        return tableWithFunctions;
    }
    
    public void setTableWithFunctions(TableWithFunctions tableWithFunctions) {
        this.tableWithFunctions = tableWithFunctions;
    }
    
    /**
     * The list of variables of tablePotential and tableWithFunctions
     */
    public List<Variable> getTableVariables() {
        return tableVariables;
    }
    
    public void setTableVariables(List<Variable> tableVariables) {
        this.tableVariables = tableVariables;
    }
    
    public Variable getChildVariable() {
        return this.getVariable(0);
    }
    
    public void setChildVariable(Variable childVariable) {
        this.getVariables().set(0, childVariable);
    }
    
    public UncertainValue[] getUncertainValues() {
        return getTablePotential().getUncertainValues();
    }
    
    public void setUncertainValues(UncertainValue[] uncertainValues) {
        getTablePotential().setUncertainValues(uncertainValues);
    }
    
    public double[] getValues() {
        return getTablePotential().getValues();
    }
    
    public void setValues(double[] values) {
        tablePotential.setValues(values);
        if (tableWithFunctions == null) {
            //TODO
        }
    }
    
    @Override public List<Variable> getVariables() {
        return variables;
    }
    
    
    @Override public void setVariables(List<Variable> variables) {
        super.setVariables(variables);
        this.getTablePotential().setVariables(variables.subList(1, variables.size()));
    }
    
    /**
     * Combination of state_value and events that cannot be possible
     */
    public ArrayList<Configuration> getImpossibleConfigurations() {
        return impossibleConfigurations;
    }
    
    public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations) {
        this.impossibleConfigurations = impossibleConfigurations;
    }

    public void setHasImpossibleConfigurations(boolean hasImpossibleConfigurations) {
        this.hasImpossibleConfigurations = hasImpossibleConfigurations;
    }

    public boolean hasImpossibleConfigurations() {
        return hasImpossibleConfigurations;
    }

    //TODO
    @Override public void setComment(String comment) {
        super.setComment(comment);
        this.getTablePotential().setComment(comment);
    }
    
    //TODO
    @Override public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(variables.get(0).getName());
        if (variables.size() == 1) {
            buffer.append(" = ");
        } else if (variables.size() > 1) {
            buffer.append(" | ");
            // Print variables
            for (int i = 1; i < variables.size() - 1; i++) {
                buffer.append(variables.get(i));
                buffer.append(", ");
            }
            buffer.append(variables.get(variables.size() - 1));
            buffer.append(" = ");
        }
        
        if (getTablePotential().getValues().length == 1) {
            buffer.append(getTablePotential().getValues()[0]);
        } else if (getTablePotential().getValues().length > 1) {
            buffer.append("{");
            for (int i = 0; i < getTablePotential().getValues().length; i++) {
                buffer.append(getTablePotential().getValues()[i]);
                if (i != getTablePotential().getValues().length - 1) {
                    buffer.append(",");
                }
            }
            buffer.append("}");
        }
        buffer.append("\n Role: " + this.getPotentialRole());
        buffer.append("\n Criterion: " + ((criterion == null) ? "null" : criterion.toString()));
        return buffer.toString();
    }
    
    /**
     * True if a tableWithFunctions is used
     */
    public boolean isUseTableWithFunctions() {
        return useTableWithFunctions;
    }
    
    public void setUseTableWithFunctions(boolean useTableWithFunctions) {
        this.useTableWithFunctions = useTableWithFunctions;
    }


//    /**
//     * Converts a List of Finding with event Findings to the Configuration structure of a TableWithEvents
//     * @param findings list of findings to be converted to TableWithEvents Configuration format
//     * @return a Configuration object where its Finding object are translated to be used in a TableWithEvents
//     */
//    public Configuration convert(List<Finding> findings){
//        Configuration tteConfiguration = new Configuration();
//        for (Finding finding:findings) {
//            try  {
//                switch (finding.getVariable().getVariableType()){
//                    case EVENT:
//                        tteConfiguration.addFinding(new Finding(events, events.getState(finding.getVariable().getName())));
//                        break;
//                    case FINITE_STATES:
//                        tteConfiguration.addFinding(finding);
//                        break;
//                    default:
//                        break;
//                }
//            } catch (InvalidStateException |IncompatibleEvidenceException e) {
//                e.printStackTrace();
//            }
//        }
//        return tteConfiguration;
//    }


//    /**
//     * Returns true if the list of findings form an impossible configuration
//     * Useful when having events treated internally in a TableWithEvents. Events are joined together in a TableWithEvents whereas treated as different variables in other cases
//     *
//     * @param findings - set of findings
//     * @return
//     */
//    @Override
//    public boolean isImpossibleConfiguration(List<Finding> findings) {
//        return isImpossibleConfiguration(convert(findings));
//    }
//
//
//    /**
//     * Returns true if there is a possible configuration with finding
//     *
//     * @param finding finding to check a possible configuration with it
//     * @return true if there is a possible configuration with finding
//     * @throws NoFindingException
//     */
//    @Override
//    public boolean hasCompatiblePossibleConfiguration(Finding finding) throws NoFindingException {
//        return false;
//    }

//    @Override
//    public void addImpossibleConfiguration(Configuration configuration) {
//        getImpossibleConfigurations().add(configuration);
//    }
//
//
//    @Override
//    public void removeImpossibleConfiguration(Configuration configuration) {
//        impossibleConfigurations.removeIf(configuration1 ->configuration1.equals(configuration));
//    }
//
//
//    @Override
//    public boolean hasImpossibleConfiguration(){
//        boolean hasIC = !(impossibleConfigurations.isEmpty());
//        return hasIC;
//    }
//
//    @Override
//    public boolean isImpossibleConfiguration(Configuration configuration) {
//        boolean isThere = getImpossibleConfigurations().contains(configuration);
//        return isThere;
//    }
    
    
    //TODO
    @Override public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    //TODO
    @Override public TableWithEvents project(EvidenceCase evidenceCase)
            throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    //TODO
    @Override public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                          List<TablePotential> alreadyProjectedPotentials)
            throws NonProjectablePotentialException {
        // get the projected TablePotential, which will be returned inside a list
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    //TODO
    @Override public Potential copy() {
        return new TableWithEvents(this);
    }
    
    //TODO
    @Override public boolean isUncertain() {
        return false;
    }
    
    //TODO
    @Override public void scalePotential(double scale) {
        this.getTablePotential().scalePotential(scale);
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

    /**
     * Not implemented for this potential: always returns {@link Double#NaN}.
     */
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
        return Double.NaN;
    }

}

