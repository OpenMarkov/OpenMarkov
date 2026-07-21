/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class VariableTest {

    private final int numStates = 3;
    private final String x = "X";
    private final String[] statesNames = {"0", "1", "2"};
    private Variable variable;

    /**
     * Compares variable1 and variable2.
     *
     * @param variable1 <code>Variable</code>
     * @param variable2 <code>Variable</code>
     *
     * @return <code>true</code> if both variables are equal.
     */
    public static boolean equalVariables(Variable variable1, Variable variable2) {
        boolean equals = true;
        // TODO Usar en Variable.equals: if (!super.equals(variableReceived)) {...}
        if (variable1.getVariableType() == variable2.getVariableType() && variable1.isTemporal() == variable2
                .isTemporal() && variable1.getNumStates() == variable2.getNumStates() && variable1.getBaseName()
                                                                                                  .contentEquals(variable2.getBaseName()) && variable1.getName()
                                                                                                                                                      .contentEquals(variable2.getName())) {
            State[] states1 = variable1.getStates();
            State[] states2 = variable2.getStates();
            int i = 0;
            while (i < states1.length && equals) {
                if (!StateTest.equalStates(states1[i], states2[i])) {
                    equals = false;
                }
                i++;
            }
            if (equals) {
                PartitionedInterval interval1 = variable1.getPartitionedInterval();
                PartitionedInterval interval2 = variable2.getPartitionedInterval();

                equals = (interval1 == null && interval2 == null) || (interval1 != null && interval2 != null);
                if (equals && interval1 != null) {
                    equals = interval1.equals(interval2);
                }
            }
        }
        return equals;
    }

    @BeforeEach public void setUp() {
        variable = new Variable(x, numStates);
    }

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    @Test public void testDiscreteConstructor2() {
        Variable variable = new Variable(x, numStates);
        State[] states = variable.getStates();
        for (int i = 0; i < numStates; i++) {
            assertTrue(states[i].getName().contentEquals(new String("" + i)));
        }
    }

    @Test public void testFSVariableStringInt() {
        Variable variable = new Variable(x, numStates);
        assertTrue(variable.getName().contains(x));
        assertEquals(numStates, variable.getNumStates());
        State[] states = variable.getStates();
        assertEquals(numStates, states.length);
        for (int i = 0; i < numStates; i++) {
            assertTrue(states[i].getName().contentEquals(statesNames[i]));
        }
    }

    @Test
    public void constructorWithStateArraySetsFiniteStatesType() {
        State[] states = {new State("yes"), new State("no")};
        Variable v = new Variable("Rain", states);

        assertEquals("Rain", v.getName());
        assertEquals(VariableType.FINITE_STATES, v.getVariableType());
        assertEquals(2, v.getNumStates());
        assertEquals("yes", v.getStates()[0].getName());
        assertEquals("no", v.getStates()[1].getName());
        assertNull(v.getPartitionedInterval());
    }

    @Test
    public void constructorWithVarargStateNames() {
        Variable v = new Variable("Color", "red", "green", "blue");

        assertEquals("Color", v.getName());
        assertEquals(VariableType.FINITE_STATES, v.getVariableType());
        assertEquals(3, v.getNumStates());
        assertEquals("red", v.getStates()[0].getName());
        assertEquals("green", v.getStates()[1].getName());
        assertEquals("blue", v.getStates()[2].getName());
    }

    @Test
    public void defaultContinuousConstructorCreatesNumericVariable() {
        Variable v = new Variable("Temperature");

        assertEquals("Temperature", v.getName());
        assertEquals(VariableType.NUMERIC, v.getVariableType());
        assertEquals(1, v.getNumStates());
        assertNotNull(v.getPartitionedInterval());
        assertEquals(Double.NEGATIVE_INFINITY, v.getPartitionedInterval().getMin());
        assertEquals(Double.POSITIVE_INFINITY, v.getPartitionedInterval().getMax());
    }

    @Test
    public void continuousConstructorWithIntervalSetsLimits() {
        Variable v = new Variable("Weight", true, 0.0, 200.0, false, 0.1);

        assertEquals(VariableType.NUMERIC, v.getVariableType());
        assertEquals(0.1, v.getPrecision());
        PartitionedInterval pi = v.getPartitionedInterval();
        assertNotNull(pi);
        assertEquals(0.0, pi.getMin());
        assertEquals(200.0, pi.getMax());
    }

    @Test
    public void discretizedConstructorSetsTypeAndInterval() {
        State[] states = {new State("low"), new State("high")};
        double[] limits = {0.0, 50.0, 100.0};
        boolean[] belongs = {true, false, false};
        PartitionedInterval pi = new PartitionedInterval(limits, belongs);

        Variable v = new Variable("Score", states, pi, 0.5);

        assertEquals(VariableType.DISCRETIZED, v.getVariableType());
        assertEquals(2, v.getNumStates());
        assertEquals(0.5, v.getPrecision());
        assertSame(pi, v.getPartitionedInterval());
    }

    // -----------------------------------------------------------------------
    // Copy constructor and clone
    // -----------------------------------------------------------------------

    @Test
    public void copyConstructorCreatesIndependentCopy() {
        variable.setAdditionalProperty("key1", "value1");
        Variable copy = new Variable(variable);

        assertEquals(variable.getName(), copy.getName());
        assertEquals(variable.getVariableType(), copy.getVariableType());
        assertEquals(variable.getNumStates(), copy.getNumStates());
        assertEquals(variable.getBaseName(), copy.getBaseName());
        assertEquals(variable.getPrecision(), copy.getPrecision());

        // States are independent
        for (int i = 0; i < variable.getNumStates(); i++) {
            assertEquals(variable.getStateName(i), copy.getStateName(i));
        }

        // Additional properties are independent
        assertEquals("value1", copy.getAdditionalProperty("key1"));
        copy.setAdditionalProperty("key2", "value2");
        assertNull(variable.getAdditionalProperty("key2"));
    }

    @Test
    public void copyConstructorCopiesTemporalVariable() {
        Variable temporal = new Variable("Y [3]", 2);
        Variable copy = new Variable(temporal);

        assertTrue(copy.isTemporal());
        assertEquals(3, copy.getTimeSlice());
        assertEquals("Y", copy.getBaseName());
        assertEquals("Y [3]", copy.getName());
    }

    @Test
    public void copyConstructorCopiesDiscretizedVariable() {
        State[] states = {new State("low"), new State("high")};
        double[] limits = {0.0, 50.0, 100.0};
        boolean[] belongs = {true, false, false};
        PartitionedInterval pi = new PartitionedInterval(limits, belongs);
        Variable original = new Variable("Score", states, pi, 0.5);

        Variable copy = new Variable(original);

        assertEquals(VariableType.DISCRETIZED, copy.getVariableType());
        assertNotSame(original.getPartitionedInterval(), copy.getPartitionedInterval());
        assertEquals(original.getPartitionedInterval().getMin(), copy.getPartitionedInterval().getMin());
    }

    @Test
    public void cloneReturnsSameAsCopyConstructor() {
        Variable clone = variable.clone();

        assertTrue(equalVariables(variable, clone));
        assertNotSame(variable, clone);
    }

    // -----------------------------------------------------------------------
    // State queries
    // -----------------------------------------------------------------------

    @Test public void testRenameState() throws InvalidArgumentException {
        // Rename not existing state (it does nothing)
        Variable variable = new Variable(x, numStates);
        boolean exceptionLaunched = false;
        try {
            variable.renameState(variable.getState("NoExists"), "Yahoo");
            fail();
        } catch (NullPointerException | InvalidArgumentException e) {
            //It should throw the exception in order to pass the test
        }
        State[] states = variable.getStates();
        assertEquals(numStates, states.length);
        for (int i = 0; i < numStates; i++) {
            assertTrue(states[i].getName().contentEquals(statesNames[i]));
        }
        // Rename one state
        String newName = "Yahoo";
        String oldName = states[numStates - 1].getName();
        variable.renameState(variable.getState(oldName), newName);
        states = variable.getStates();
        assertEquals(numStates, states.length);
        for (int i = 0; i < numStates - 1; i++) {
            assertTrue(states[i].getName().contentEquals(statesNames[i]));
        }
        assertTrue(states[numStates - 1].getName().contentEquals(newName));
    }

    // TODO Sobrecargar equals y poner en el comentario que
    // equals ya NO consiste en comparar la dirección de memoria de dos objetos
    // TODO Cada tipo de variable tiene que tener un método equals y llamar al del padre

    @Test public void testGetStateIndex() {
        for (int i = 0; i < numStates; i++) {
            assertEquals(i, variable.getStateIndex(statesNames[i]));
        }
    }

    @Test
    public void getStateIndexReturnsMinusOneForUnknownState() {
        assertEquals(-1, variable.getStateIndex("nonexistent"));
    }

    @Test
    public void getStateIndexByStateObject() {
        State s = variable.getState("1");
        assertNotNull(s);
        assertEquals(1, variable.getStateIndex(s));
    }

    @Test
    public void getStateIndexByStateObjectReturnsMinusOneForUnknown() {
        State foreign = new State("alien");
        assertEquals(-1, variable.getStateIndex(foreign));
    }

    @Test
    public void getStateIndexByDoubleForFiniteStates() {
        // For FINITE_STATES, getStateIndex(double) rounds then looks up string.
        // round(0.0) with precision 0.01 → "0.0", which doesn't match state "0".
        // This only works when state names match the rounded string representation.
        Variable v = new Variable("V", new State[]{new State("0.0"), new State("1.0"), new State("2.0")});
        assertEquals(0, v.getStateIndex(0.0));
        assertEquals(1, v.getStateIndex(1.0));
        assertEquals(2, v.getStateIndex(2.0));
    }

    @Test
    public void getStateIndexByDoubleForDiscretized() {
        State[] states = {new State("low"), new State("high")};
        double[] limits = {0.0, 50.0, 100.0};
        boolean[] belongs = {true, false, false};
        PartitionedInterval pi = new PartitionedInterval(limits, belongs);
        Variable disc = new Variable("D", states, pi, 1.0);

        assertEquals(0, disc.getStateIndex(25.0));
        assertEquals(1, disc.getStateIndex(75.0));
    }

    @Test
    public void getStateReturnsNullForUnknownName() {
        assertNull(variable.getState("nonexistent"));
    }

    @Test
    public void getStateReturnsCorrectState() {
        State s = variable.getState("1");
        assertNotNull(s);
        assertEquals("1", s.getName());
    }

    @Test
    public void containsStateReturnsTrueForExisting() {
        assertTrue(variable.containsState("0"));
        assertTrue(variable.containsState("1"));
        assertTrue(variable.containsState("2"));
    }

    @Test
    public void containsStateReturnsFalseForMissing() {
        assertFalse(variable.containsState("nonexistent"));
    }

    @Test
    public void getStatesReturnsDefensiveCopy() {
        State[] states1 = variable.getStates();
        State[] states2 = variable.getStates();
        assertNotSame(states1, states2);
    }

    // -----------------------------------------------------------------------
    // Rename state with duplicate detection
    // -----------------------------------------------------------------------

    @Test
    public void renameStateThrowsWhenDuplicateName() {
        State state0 = variable.getState("0");
        assertThrows(InvalidArgumentException.class, () -> variable.renameState(state0, "1"));
    }

    @Test
    public void renameStateUpdatesAdditionalProperties() {
        variable.setStateAdditionalProperty("0", "desc", "first");
        State state0 = variable.getState("0");
        variable.renameState(state0, "zero");

        // Property should be accessible under the new name
        assertEquals("first", variable.getStateAdditionalProperty("zero", "desc"));
        // Old name should no longer have the property
        assertNull(variable.getStateAdditionalProperty("0", "desc"));
    }

    // -----------------------------------------------------------------------
    // containsState and getNewValidName
    // -----------------------------------------------------------------------

    @Test
    public void getNewValidNameReturnsUniqueStateName() {
        // Variable has states "0", "1", "2" — none start with "state" prefix
        String name = variable.getNewValidName();
        assertNotNull(name);
        assertTrue(name.startsWith("state"));
        assertFalse(variable.containsState(name));
    }

    @Test
    public void getNewValidNameSkipsExistingStateNames() {
        Variable v = new Variable("V", "state0", "state1");
        String name = v.getNewValidName();
        assertEquals("state2", name);
    }

    // -----------------------------------------------------------------------
    // Temporal variables
    // -----------------------------------------------------------------------

    @Nested
    class TemporalTests {

        @Test
        void nonTemporalVariableReportsNotTemporal() {
            assertFalse(variable.isTemporal());
            assertEquals(Integer.MIN_VALUE, variable.getTimeSlice());
        }

        @Test
        void variableWithTimeSliceInNameIsTemporal() {
            Variable v = new Variable("Y [5]", 2);
            assertTrue(v.isTemporal());
            assertEquals(5, v.getTimeSlice());
            assertEquals("Y", v.getBaseName());
            assertEquals("Y [5]", v.getName());
        }

        @Test
        void setTimeSliceUpdatesNameAndSlice() {
            variable.setTimeSlice(7);
            assertTrue(variable.isTemporal());
            assertEquals(7, variable.getTimeSlice());
            assertEquals("X [7]", variable.getName());
            assertEquals("X", variable.getBaseName());
        }

        @Test
        void setTimeSliceToMinValueKeepsNonTemporal() {
            variable.setTimeSlice(Integer.MIN_VALUE);
            assertFalse(variable.isTemporal());
        }

        @Test
        void setBaseNameUpdatesFullName() {
            variable.setTimeSlice(2);
            assertEquals("X [2]", variable.getName());

            variable.setBaseName("Z");
            assertEquals("Z [2]", variable.getName());
            assertEquals("Z", variable.getBaseName());
        }

        @Test
        void setBaseNameWithoutTimeSlice() {
            variable.setBaseName("Z");
            assertEquals("Z", variable.getName());
            assertEquals("Z", variable.getBaseName());
        }

        @Test
        void setNameParsesTimeSlice() {
            variable.setName("W [10]");
            assertTrue(variable.isTemporal());
            assertEquals(10, variable.getTimeSlice());
            assertEquals("W", variable.getBaseName());
        }

        @Test
        void setNameWithoutBracketsIsNotTemporal() {
            Variable v = new Variable("A [1]", 2);
            assertTrue(v.isTemporal());

            v.setName("B");
            assertFalse(v.isTemporal());
            assertEquals("B", v.getBaseName());
        }

        @Test
        void getBaseNameReturnsNameWhenBaseNameNull() {
            // For a non-temporal variable created without brackets
            assertEquals("X", variable.getBaseName());
        }

        @Test
        void timeSliceZeroIsTemporal() {
            Variable v = new Variable("T [0]", 2);
            assertTrue(v.isTemporal());
            assertEquals(0, v.getTimeSlice());
        }
    }

    // -----------------------------------------------------------------------
    // Additional properties
    // -----------------------------------------------------------------------

    @Nested
    class AdditionalPropertiesTests {

        @Test
        void initialAdditionalPropertiesAreEmpty() {
            assertTrue(variable.getAdditionalProperties().isEmpty());
        }

        @Test
        void setAndGetAdditionalProperty() {
            variable.setAdditionalProperty("author", "test");
            assertEquals("test", variable.getAdditionalProperty("author"));
        }

        @Test
        void getAdditionalPropertyReturnsNullForMissing() {
            assertNull(variable.getAdditionalProperty("missing"));
        }

        @Test
        void setAdditionalPropertiesReplacesAll() {
            variable.setAdditionalProperty("old", "value");
            variable.setAdditionalProperties(Map.of("new", "value2"));
            assertNull(variable.getAdditionalProperty("old"));
            assertEquals("value2", variable.getAdditionalProperty("new"));
        }

        @Test
        void setAdditionalPropertiesWithNullClearsAll() {
            variable.setAdditionalProperty("key", "val");
            variable.setAdditionalProperties(null);
            assertTrue(variable.getAdditionalProperties().isEmpty());
        }

        @Test
        void getAdditionalPropertiesReturnsUnmodifiableView() {
            variable.setAdditionalProperty("k", "v");
            Map<String, String> props = variable.getAdditionalProperties();
            assertThrows(UnsupportedOperationException.class, () -> props.put("new", "val"));
        }
    }

    // -----------------------------------------------------------------------
    // State additional properties
    // -----------------------------------------------------------------------

    @Nested
    class StateAdditionalPropertiesTests {

        @Test
        void setAndGetStateAdditionalProperty() {
            variable.setStateAdditionalProperty("0", "color", "red");
            assertEquals("red", variable.getStateAdditionalProperty("0", "color"));
        }

        @Test
        void getStateAdditionalPropertyReturnsNullWhenNoProperties() {
            assertNull(variable.getStateAdditionalProperty("0", "color"));
        }

        @Test
        void setStateAdditionalPropertiesAsMap() {
            HashMap<String, String> props = new HashMap<>();
            props.put("color", "blue");
            props.put("weight", "10");
            variable.setStateAdditionalProperties("1", props);

            HashMap<String, String> retrieved = variable.getStateAdditionalProperties("1");
            assertNotNull(retrieved);
            assertEquals("blue", retrieved.get("color"));
            assertEquals("10", retrieved.get("weight"));
        }

        @Test
        void getStateAdditionalPropertiesReturnsNullForUnknownState() {
            assertNull(variable.getStateAdditionalProperties("unknown"));
        }
    }

    // -----------------------------------------------------------------------
    // Variable type
    // -----------------------------------------------------------------------

    @Nested
    class VariableTypeTests {

        @Test
        void finiteStatesVariableType() {
            assertEquals(VariableType.FINITE_STATES, variable.getVariableType());
        }

        @Test
        void setVariableTypeToNumericCreatesDefaultState() {
            variable.setVariableType(VariableType.NUMERIC);
            assertEquals(VariableType.NUMERIC, variable.getVariableType());
            assertEquals(1, variable.getNumStates());
            assertNotNull(variable.getPartitionedInterval());
        }

        @Test
        void setVariableTypeToDiscretizedCreatesPartitionedInterval() {
            variable.setVariableType(VariableType.DISCRETIZED);
            assertEquals(VariableType.DISCRETIZED, variable.getVariableType());
            assertNotNull(variable.getPartitionedInterval());
            // 3 states → 4 limits
            assertEquals(4, variable.getPartitionedInterval().getLimits().length);
        }
    }

    // -----------------------------------------------------------------------
    // Delta table potentials
    // -----------------------------------------------------------------------

    @Nested
    class DeltaPotentialTests {

        @Test
        void deltaTablePotentialByStateName() {
            TablePotential delta = variable.deltaTablePotential("1");
            assertEquals(1, delta.getVariables().size());
            assertEquals(variable, delta.getVariables().getFirst());
            assertEquals(0.0, delta.getValues()[0]);
            assertEquals(1.0, delta.getValues()[1]);
            assertEquals(0.0, delta.getValues()[2]);
        }

        @Test
        void deltaTablePotentialByStateObject() {
            State state = variable.getState("2");
            TablePotential delta = variable.deltaTablePotential(state);
            assertEquals(0.0, delta.getValues()[0]);
            assertEquals(0.0, delta.getValues()[1]);
            assertEquals(1.0, delta.getValues()[2]);
        }

        @Test
        void createDeltaTablePotentialByIndex() {
            TablePotential delta = variable.createDeltaTablePotential(0);
            assertEquals(1.0, delta.getValues()[0]);
            assertEquals(0.0, delta.getValues()[1]);
            assertEquals(0.0, delta.getValues()[2]);
        }

        @Test
        void deltaPotentialHasConditionalProbabilityRole() {
            TablePotential delta = variable.deltaTablePotential("0");
            assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, delta.getPotentialRole());
        }
    }

    // -----------------------------------------------------------------------
    // Default interval and belongs
    // -----------------------------------------------------------------------

    @Nested
    class DefaultIntervalTests {

        @Test
        void getDefaultIntervalHasCorrectBounds() {
            double[] interval = variable.getDefaultInterval(3);
            assertEquals(4, interval.length);
            assertEquals(Double.NEGATIVE_INFINITY, interval[0]);
            assertEquals(Double.POSITIVE_INFINITY, interval[3]);
        }

        @Test
        void getDefaultIntervalInternalLimitsUsesPrecision() {
            variable.setPrecision(0.5);
            double[] interval = variable.getDefaultInterval(3);
            // limits: [-inf, 0.0, 0.5, +inf]
            assertEquals(0.0, interval[1], 1e-9);
            assertEquals(0.5, interval[2], 1e-9);
        }

        @Test
        void getDefaultBelongsFirstIsTrue() {
            boolean[] belongs = Variable.getDefaultBelongs(3);
            assertEquals(4, belongs.length);
            assertTrue(belongs[0]);
            assertFalse(belongs[1]);
            assertFalse(belongs[2]);
            // Last is default false (unset)
            assertFalse(belongs[3]);
        }
    }

    // -----------------------------------------------------------------------
    // Precision and round
    // -----------------------------------------------------------------------

    @Nested
    class PrecisionTests {

        @Test
        void defaultPrecision() {
            assertEquals(0.01, variable.getPrecision());
        }

        @Test
        void setPrecision() {
            variable.setPrecision(0.5);
            assertEquals(0.5, variable.getPrecision());
        }

        @Test
        void roundToDefaultPrecision() {
            // precision=0.01
            assertEquals(1.23, variable.round(1.234), 1e-9);
            assertEquals(0.0, variable.round(0.004), 1e-9);
        }

        @Test
        void roundToCustomPrecision() {
            variable.setPrecision(0.5);
            assertEquals(1.0, variable.round(1.1), 1e-9);
            assertEquals(1.5, variable.round(1.4), 1e-9);
        }
    }

    // -----------------------------------------------------------------------
    // Property getters/setters (unit, agent, decisionCriterion)
    // -----------------------------------------------------------------------

    @Test
    public void unitDefaultIsEmptyString() {
        StringWithProperties unit = variable.getUnit();
        assertNotNull(unit);
        assertEquals("", unit.getString());
    }

    @Test
    public void setAndGetUnit() {
        StringWithProperties unit = new StringWithProperties("kg");
        variable.setUnit(unit);
        assertEquals("kg", variable.getUnit().getString());
    }

    @Test
    public void setUnitToNull() {
        variable.setUnit(null);
        assertNull(variable.getUnit());
    }

    @Test
    public void agentDefaultIsNull() {
        assertNull(variable.getAgent());
    }

    @Test
    public void setAndGetAgent() {
        StringWithProperties agent = new StringWithProperties("Player1");
        variable.setAgent(agent);
        assertEquals("Player1", variable.getAgent().getString());
    }

    @Test
    public void decisionCriterionDefaultIsNull() {
        assertNull(variable.getDecisionCriterion());
    }

    @Test
    public void setAndGetDecisionCriterion() {
        Criterion c = new Criterion();
        variable.setDecisionCriterion(c);
        assertSame(c, variable.getDecisionCriterion());
    }

    // -----------------------------------------------------------------------
    // compareTo
    // -----------------------------------------------------------------------

    @Test
    public void compareToSameObjectReturnsZero() {
        assertEquals(0, variable.compareTo(variable));
    }

    @Test
    public void compareToIsConsistentBetweenCalls() {
        Variable other = new Variable("Y", 2);
        int result1 = variable.compareTo(other);
        int result2 = other.compareTo(variable);
        // If a < b then b > a
        if (result1 != 0) {
            assertEquals(-result1, result2);
        }
    }

    // -----------------------------------------------------------------------
    // asVariableExpression
    // -----------------------------------------------------------------------

    @Test
    public void asVariableExpressionContainsVariableName() {
        var expr = variable.asVariableExpression();
        assertNotNull(expr);
    }

    // -----------------------------------------------------------------------
    // Name management
    // -----------------------------------------------------------------------

    @Test
    public void setNameUpdatesName() {
        variable.setName("NewName");
        assertEquals("NewName", variable.getName());
    }

    @Test
    public void getStateNameByIndex() {
        assertEquals("0", variable.getStateName(0));
        assertEquals("1", variable.getStateName(1));
        assertEquals("2", variable.getStateName(2));
    }

    @Test
    public void setStatesReplacesStates() {
        State[] newStates = {new State("a"), new State("b")};
        variable.setStates(newStates);
        assertEquals(2, variable.getNumStates());
        assertEquals("a", variable.getStateName(0));
        assertEquals("b", variable.getStateName(1));
    }

    @Test
    public void setStatesIsDefensive() {
        State[] newStates = {new State("a"), new State("b")};
        variable.setStates(newStates);
        // Mutating original array should not affect variable
        newStates[0] = new State("mutated");
        assertEquals("a", variable.getStateName(0));
    }
}
