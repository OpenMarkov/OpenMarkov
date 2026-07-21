/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.java.cloneUtils.CloneUtils;
import org.openmarkov.java.nullUtils.NullUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * A variable (for instance, a random variable or a decision). Each
 * {@code Node} in a {@code ProbNet}work represents a
 * {@code Variable}
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @see Node
 * @see ProbNet
 */
public class Variable implements Cloneable, Comparable<Variable>, ClassLocalizable {
    
    // Constant
    /**
     * Time slice value when the variable is not temporal.
     */
    public static final int noTemporalTimeSlice = Integer.MIN_VALUE;
    
    // Attributes
    private static final String STATE_BASE_NAME = "state";
    /**
     * A string (usually in English) that identifies this variable.
     */
    protected String name;
    /**
     * List of states that this variable can take on. Each state will be a
     * {@code String}.
     */
    private State[] states;
    /**
     * Variable role: discrete, continuous, discretized, ...
     */
    private VariableType variableType;
    /**
     * Interval sets where this variable is defined in the case of continuous or
     * discretized variable type.
     */
    protected PartitionedInterval partitionedInterval;
    private Map<String, String> additionalProperties = new HashMap<>();
    protected HashMap<String, HashMap<String, String>> statesAdditionalProperties = new HashMap<>();
    /**
     * The time Slice of the node. The default value is no temporal.
     */
    private int timeSlice = noTemporalTimeSlice;
    // Name without the time slice index. For example, if the name is "X [0]",
    // the baseName is "X"
    private String baseName;
    private @Nullable StringWithProperties unit = new StringWithProperties("");
    /**
     * Max error.
     */
    private double precision = 0.01;
    /**
     * Agent for decision nodes
     */
    private @Nullable StringWithProperties agent;
    /**
     * Decision criterion for utility nodes
     */
    private Criterion decisionCriterion;
    
    // Constructors
    
    /**
     * Constructor for discrete variables.
     *
     * @param name   {@code String}
     * @param states {@code String[]}
     *               Condition: All the states must be different
     */
    public Variable(String name, State[] states) {
        this.states = states;
        this.variableType = VariableType.FINITE_STATES;
        this.partitionedInterval = null;
        parseNameIntoParts(name);
    }
    
    /**
     * Constructor for discrete variables. It takes advantage of the feature of
     * variable-length argument lists of Java 5 in order to accept the names of
     * the states.
     * <p>
     * Creates a {@code FSVariable} whose states are given by the names
     * {@code namesStates} states.
     *
     * @param nameVariable a {@code String}
     * @param stateNames   a sequence of {@code String} by using the facilities of
     *                     Java 5.
     */
    public Variable(String nameVariable, String... stateNames) {
        int numStates = stateNames.length;
        states = new State[numStates];
        for (int i = 0; i < numStates; i++) {
            states[i] = new State(stateNames[i]);
        }
        this.variableType = VariableType.FINITE_STATES;
        this.partitionedInterval = null;
        parseNameIntoParts(nameVariable);
    }
    
    /**
     * Constructor for discrete variables.
     * <p>
     * Creates a {@code FSVariable} with {@code numStates} states. The
     * i-th state is named as "i".
     *
     * @param name      a {@code String}
     * @param numStates {@code int}
     */
    public Variable(String name, int numStates) {
        this.states = new State[numStates];
        for (int i = 0; i < numStates; i++) {
            states[i] = new State("" + i);
        }
        this.variableType = VariableType.FINITE_STATES;
        this.partitionedInterval = null;
        parseNameIntoParts(name);
    }
    
    /**
     * Copy constructor for Variable.
     *
     * @param variable Variable
     */
    public Variable(Variable variable) {
        this.states = variable.states.clone();
        this.variableType = variable.variableType;
        this.partitionedInterval = CloneUtils.safeClone(variable.partitionedInterval);
        this.additionalProperties = variable.additionalProperties != null
                ? new HashMap<>(variable.additionalProperties) : new HashMap<>();
        this.statesAdditionalProperties = CloneUtils.safeClone(variable.statesAdditionalProperties);
        this.precision = variable.precision;
        this.unit = CloneUtils.safeClone(variable.unit);
        this.agent = CloneUtils.safeClone(variable.agent);
        this.decisionCriterion = CloneUtils.safeClone(variable.decisionCriterion);
        parseNameIntoParts(variable.name);
    }
    
    /**
     * Default constructor for continuous variables.
     * <p>
     * A continuous variable is defined in an interval. In this case the
     * interval is (-infinity, +infinity)
     *
     * @param name {@code String}
     */
    public Variable(String name) {
        
        this(name, new State[]{new State("0")},
             new PartitionedInterval(false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false), 0.0);
        this.variableType = VariableType.NUMERIC;
    }
    
    /**
     * Constructor for continuous variables.
     * <p>
     * A continuous variable is defined in an interval.
     *
     * @param name        . {@code String}
     * @param leftClosed  . {@code boolean}
     * @param min         . {@code double}
     * @param max         . {@code double}
     * @param rightClosed . {@code boolean}
     * @param precision   . {@code double}
     */
    public Variable(String name, boolean leftClosed, double min, double max, boolean rightClosed, double precision) {
        
        this(name, new State[]{new State("Only one state")},
             new PartitionedInterval(leftClosed, min, max, rightClosed), precision);
        this.variableType = VariableType.NUMERIC;
    }
    
    /**
     * Constructor for hybrid variables (discrete and continuous).
     * <p>
     * The interval in with is defined the continuous variable is the addition
     * of the set of intervals.
     *
     * @param name                . {@code String}
     * @param states              . {@code String[]}
     * @param partitionedInterval . {@code PartitionedInterval}
     * @param precision           . {@code double}
     *                            Condition: states.length = partitionedInterval.getNumSubintervals()
     */
    public Variable(String name, State[] states, PartitionedInterval partitionedInterval, double precision) {
        
        this(name, states);
        this.partitionedInterval = partitionedInterval;
        this.precision = precision;
        this.variableType = VariableType.DISCRETIZED;
    }
    
    // Methods
    
    @Override public Variable clone() {
        return new Variable(this);
    }
    
    
    // Methods
    
    /**
     * Returns an unmodifiable view of the additional properties map.
     *
     * @return unmodifiable map; never {@code null}
     */
    public Map<String, String> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }

    /**
     * Replaces all additional properties with the entries from the given map.
     *
     * @param additionalProperties new properties; {@code null} is treated as empty
     */
    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties.clear();
        if (additionalProperties != null) {
            this.additionalProperties.putAll(additionalProperties);
        }
    }
    
    /**
     * @param propertyName Property name
     *
     * @return property value if exists, otherwise {@code null}
     * {@code String} and value = {@code String}
     */
    public String getAdditionalProperty(String propertyName) {
        String property = null;
        if (additionalProperties != null) {
            property = additionalProperties.get(propertyName);
        }
        return property;
    }
    
    /**
     * @param propertyValue Property value
     * @param propertyName  Property name
     */
    public void setAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }
    
    public void setStateAdditionalProperties(String stateName, HashMap<String, String> stateAdditionalProperties) {
        statesAdditionalProperties.put(stateName, stateAdditionalProperties);
    }

    public HashMap<String, String> getStateAdditionalProperties(String stateName) {
        return statesAdditionalProperties.get(stateName);
    }
    
    public boolean isTemporal() {
        
        return timeSlice != Integer.MIN_VALUE;
    }
    
    public void setStateAdditionalProperty(String stateName, String propertyName, String propertyValue) {
        statesAdditionalProperties
                .computeIfAbsent(stateName, k -> new HashMap<>())
                .put(propertyName, propertyValue);
    }

    public String getStateAdditionalProperty(String stateName, String propertyName) {
        HashMap<String, String> stateProperties = statesAdditionalProperties.get(stateName);
        return stateProperties != null ? stateProperties.get(propertyName) : null;
    }
    
    public void renameState(State state, String newName) {
        for (State s : states) {
            if (s.getName().contentEquals(newName)) {
                throw new InvalidArgumentException(newName, "new name", "that name already belongs to another state");
            }
        }
        String oldName = state.getName();
        // Move additional properties from old key to new key if they exist
        HashMap<String, String> additionalPropertiesOldName = statesAdditionalProperties.remove(oldName);
        if (additionalPropertiesOldName != null) {
            statesAdditionalProperties.put(newName, additionalPropertiesOldName);
        }
        state.setName(newName);
    }
    
    public int getStateIndex(String stateName) {
        return IntStream.range(0, states.length)
                        .filter(i -> states[i].getName().contentEquals(stateName))
                        .findFirst()
                        .orElse(-1);
    }
    
    public boolean containsState(String stateName) {
        return getStateIndex(stateName) != -1;
    }
    
    /**
     * @param state . {@code State}
     *
     * @return stateIndex of state. {@code int}
     *
     * @throws Error if state does not exist
     */
    public int getStateIndex(State state) {
        for (int i = 0; i < states.length; i++) {
            if (NullUtils.equals(states[i], state)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * @param value . {@code double}
     *
     * @return The state index corresponding to value. {@code int}
     */
    public int getStateIndex(double value) {
        if (variableType == VariableType.FINITE_STATES) {
            return getStateIndex(String.valueOf(round(value)));
        }
        int indexOfSubinterval = partitionedInterval.indexOfSubinterval(value);
        return indexOfSubinterval;
    }
    
    /**
     * @return variableType. {@code VariableType}
     */
    public VariableType getVariableType() {
        
        return variableType;
    }
    
    /**
     * @param variableType the variableType to set
     */
    public void setVariableType(VariableType variableType) {
        
        this.variableType = variableType;
        // TODO this method assume that the states exists a priori
        // when Node is created, Variable is created, then when node
        // is changed from continuous to discrete, the edit has to
        // assign the default state indicated in probNet.
        switch (variableType) {
            case NUMERIC:
                this.setStates(new State[]{new State("")});
                setPartitionedInterval(new PartitionedInterval(getDefaultInterval(1), getDefaultBelongs(1)));
                break;
            case DISCRETIZED:
                setPartitionedInterval(
                        new PartitionedInterval(getDefaultInterval(getNumStates()), getDefaultBelongs(getNumStates())));
                break;
            //2019 - created type Event - 11/04/2020 added a state named event
            case EVENT:
                //FIXME 13/01/2023; check partitioned interval; this is set because DeltaPotentialPanel uses it. Not needed for EVENT variables
                this.setStates(new State[] { new State("event") });
                setPartitionedInterval(
                        new PartitionedInterval(new double[]{0.0,Double.POSITIVE_INFINITY}, getDefaultBelongs(getNumStates())));
                break;
            //
            default:
                break;
        }
        
    }
    
    /**
     * @return name. {@code String}
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param newName . {@code String}
     */
    public void setName(String newName) {
        parseNameIntoParts(newName);
    }

    /**
     * @return base name without time slice suffix; never {@code null}
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * @param newBaseName . {@code String}
     */
    public void setBaseName(String newBaseName) {
        this.baseName = newBaseName;
        rebuildFullName();
    }
    
    /**
     * @return Number of states.
     */
    public int getNumStates() {
        
        return states.length;
    }
    
    /**
     * Returns a defensive copy of the states array so that callers cannot
     * modify the internal state of this variable.
     *
     * @return copy of the states array; never {@code null}
     */
    public State[] getStates() {
        return states.clone();
    }

    /**
     * Replaces the states with a defensive copy of the given array.
     *
     * @param states new states; must not be {@code null}
     */
    public void setStates(State[] states) {
        this.states = states.clone();
    }
    
    /**
     * Replaces all states and updates potentials. Delegates to
     * {@link VariableStateOperations#replaceStates}.
     */
    public void replaceStates(Node node, State[] newStates) {
        VariableStateOperations.replaceStates(this, node, newStates);
    }
    
    /**
     * Dispatches a state modification action. Delegates to
     * {@link VariableStateOperations#modifyState}.
     */
    public void modifyState(Node node, StateAction stateAction, int stateIndex, String newName) {
        VariableStateOperations.modifyState(this, node, stateAction, stateIndex, newName);
    }
    
    /**
     * @param index . {@code int}
     *
     * @return Name of states[index]. {@code String}.
     * Condition: index must be a number between 0 and (number-of-states -
     * 1).
     */
    public String getStateName(int index) {
        
        return states[index].getName();
    }
    
    /**
     * @param name Name
     *
     * @return The state whose name is 'name'
     */
    public @Nullable State getState(String name) {
        int index = getStateIndex(name);
        if (index == -1) {
            return null;
        }
        return states[index];
    }
    
    /**
     * @return partitionedInterval. {@code PartitionedInterval}
     */
    public PartitionedInterval getPartitionedInterval() {
        
        return partitionedInterval;
    }
    
    /**
     * @param partitionedInterval the partitionedInterval to set
     */
    public void setPartitionedInterval(PartitionedInterval partitionedInterval) {
        
        this.partitionedInterval = partitionedInterval;
    }
    
    /**
     * @return precision. {@code double}
     */
    public double getPrecision() {
        
        return precision;
    }
    
    /**
     * @param precision the precision to set
     */
    public void setPrecision(double precision) {
        
        this.precision = precision;
    }
    
    public TablePotential deltaTablePotential(String stateName) {
        return createDeltaTablePotential(getStateIndex(stateName));
    }

    public TablePotential deltaTablePotential(State state) {
        return createDeltaTablePotential(getStateIndex(state));
    }

    /**
     * Creates a delta potential (spike at 1.0) for the state at the given index.
     * All other values are set to 0.0.
     */
    public TablePotential createDeltaTablePotential(int stateIndex) {
        TablePotential potential = new TablePotential(List.of(this), PotentialRole.CONDITIONAL_PROBABILITY);
        Arrays.fill(potential.getValues(), 0.0);
        potential.getValues()[stateIndex] = 1.0;
        return potential;
    }
    
    public double[] getDefaultInterval(int numStates) {
        double[] interval = new double[numStates + 1];
        interval[0] = Double.NEGATIVE_INFINITY;
        interval[numStates] = Double.POSITIVE_INFINITY;
        double count = 0;
        for (int i = 1; i <= numStates - 1; i++) {
            interval[i] = count;
            double precision = getPrecision();
            count += precision;
        }
        return interval;
    }
    
    /*
     * private double[] getDefaultInterval(int numStates) { double [] interval =
     * new double[numStates+1]; interval[0] = 0; int count = 2; for (int i=1;i
     * <= numStates; i++){ interval[i] = count; count += 2; } return interval; }
     */
    public static boolean[] getDefaultBelongs(int numStates) {
        boolean[] limits = new boolean[numStates + 1];
        limits[0] = true;
        for (int i = 1; i < numStates; i++) {
            limits[i] = false;
        }
        return limits;
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
    public int getTimeSlice() {
        return timeSlice;
    }

    /**
     * Changes or sets for the first time the time slice. Modifies the variable
     * name adding or changing [timeSlice]. If the variable is not temporal
     * change to temporal.
     *
     * @param timeSlice . {@code int}
     */
    public void setTimeSlice(int timeSlice) {
        this.timeSlice = timeSlice;
        rebuildFullName();
    }

    // -----------------------------------------------------------------------
    // Name / baseName / timeSlice synchronization
    // -----------------------------------------------------------------------

    /**
     * Parses a full variable name (e.g. "X [3]") into {@code baseName} and
     * {@code timeSlice}, then rebuilds {@code name} from those parts.
     * This is the single entry point for decomposing a name string.
     */
    private void parseNameIntoParts(String variableName) {
        int parsedTimeSlice = noTemporalTimeSlice;
        String parsedBaseName = variableName;

        if (variableName.contains(" [")) {
            int lastOpenBracket = variableName.lastIndexOf(" [");
            int lastClosedBracket = variableName.lastIndexOf(']');
            if (lastClosedBracket > lastOpenBracket) {
                try {
                    parsedTimeSlice = Integer.parseInt(
                            variableName.substring(lastOpenBracket + 2, lastClosedBracket));
                    parsedBaseName = variableName.substring(0, lastOpenBracket);
                } catch (NumberFormatException e) {
                    // Not a valid time slice — treat entire string as base name
                }
            }
        }

        this.baseName = parsedBaseName;
        this.timeSlice = parsedTimeSlice;
        rebuildFullName();
    }

    /**
     * Rebuilds {@code name} from the current {@code baseName} and
     * {@code timeSlice}.  This is the single place where the full name
     * is composed, ensuring the invariant is always maintained.
     */
    private void rebuildFullName() {
        if (timeSlice != noTemporalTimeSlice) {
            this.name = baseName + " [" + timeSlice + "]";
        } else {
            this.name = baseName;
        }
    }
    
    /**
     * @return the unit
     */
    public @Nullable StringWithProperties getUnit() {
        return unit;
    }
    
    /**
     * @param unit the unit to set
     */
    public void setUnit(@Nullable StringWithProperties unit) {
        this.unit = unit;
    }
    
    public @Nullable StringWithProperties getAgent() {
        return agent;
    }
    
    public void setAgent(@Nullable StringWithProperties agent) {
        this.agent = agent;
    }
    
    public Criterion getDecisionCriterion() {
        return decisionCriterion;
    }
    
    public void setDecisionCriterion(Criterion decisionCriterion) {
        this.decisionCriterion = decisionCriterion;
    }
    
    public double round(double value) {
        return Math.round(value / precision) * precision;
    }
    
    @Override public int compareTo(Variable o) {
        return this.name.compareTo(o.name);
    }
    
    public VariableExpression asVariableExpression() {
        return new VariableExpression(List.of(this), "{" + this.getName() + "}");
    }
    
    /**
     * This method returns a valid status name based on the current
     * states and a based prefix defined as a constant in this class
     *
     * @return a new valid name for a state
     */
    public String getNewValidName() {
        String newValidName;
        int actualState = 0;
        boolean validName;
        do {
            validName = true;
            newValidName = STATE_BASE_NAME + actualState;
            for (State state : states) {
                if (state.getName().equals(newValidName)) {
                    validName = false;
                    break;
                }
            }
            actualState++;
        } while (!validName);
        
        return newValidName;
    }
    
    // TODO Ver si este código resuelve el problema que se puede dar en
    // Elvira con los dos nombres de las variables que tiene.
    
}
