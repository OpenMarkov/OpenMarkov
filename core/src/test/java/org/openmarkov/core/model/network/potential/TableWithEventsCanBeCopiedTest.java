/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copying a {@link TableWithEvents} — the potential the discrete-event simulation uses to hold, in a
 * single table, the parents that are events. Its copy constructor carried the table and nothing
 * else, so everything else came back empty, and {@code TransitionTablePotential} worked around that
 * by putting the fields back from the source, which made the copy share them.
 *
 * <p>The variable that stands for all the event parents together and the list of variables the table
 * is built on are <em>derived</em> from the variables of the potential, so what is checked here is
 * that a copy works them out again for itself, and for the network it is copied into.
 *
 * @author Manuel Arias
 */
public class TableWithEventsCanBeCopiedTest {

    private Variable child;
    private Variable parent;
    private Variable firstEvent;
    private Variable secondEvent;
    private List<Variable> variables;

    private static Variable event(String name) {
        Variable variable = new Variable(name, new State[]{new State("no"), new State("yes")});
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    @BeforeEach
    public void setUp() {
        child = new Variable("C", new State[]{new State("a"), new State("b")});
        parent = new Variable("P", new State[]{new State("a"), new State("b")});
        firstEvent = event("E1");
        secondEvent = event("E2");
        variables = new ArrayList<>(List.of(child, parent, firstEvent, secondEvent));
    }

    private TableWithEvents original(boolean withFunctions) throws Exception {
        TableWithEvents potential = new TableWithEvents(new ArrayList<>(variables),
                PotentialRole.CONDITIONAL_PROBABILITY, withFunctions);
        potential.setHasImpossibleConfigurations(true);
        potential.getImpossibleConfigurations().add(new Configuration());
        return potential;
    }

    /** A network holding a node for each variable, so a deep copy has somewhere to point at. */
    private ProbNet destinationNetwork() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        for (Variable variable : variables) {
            net.addNode(new Variable(variable), NodeType.CHANCE);
        }
        return net;
    }

    // -----------------------------------------------------------------------
    // copy()
    // -----------------------------------------------------------------------

    @Test
    public void theCopyKnowsItsEventsAndTheVariablesOfItsTable() throws Exception {
        TableWithEvents copy = (TableWithEvents) original(false).copy();

        assertNotNull(copy.getEvents(), "the copy must know which of its parents are events");
        assertEquals(2, copy.getEvents().getNumStates(), "one state per event parent");
        assertEquals(List.of("C", "P", "Events"),
                copy.getTableVariables().stream().map(Variable::getName).toList());
    }

    @Test
    public void theCopyKeepsBeingATableOfFunctions() throws Exception {
        TableWithEvents copy = (TableWithEvents) original(true).copy();

        assertTrue(copy.isUseTableWithFunctions());
        assertNotNull(copy.getTableWithFunctions(),
                "a table whose values are given by functions cannot come back without them");
    }

    @Test
    public void theCopyKeepsTheImpossibleConfigurations() throws Exception {
        TableWithEvents copy = (TableWithEvents) original(false).copy();

        assertTrue(copy.hasImpossibleConfigurations());
        assertNotNull(copy.getImpossibleConfigurations(),
                "an empty list means none; null means whoever adds one gets an exception");
        assertEquals(1, copy.getImpossibleConfigurations().size());
    }

    @Test
    public void addingAnImpossibleConfigurationToTheCopyDoesNotAddItToTheOriginal() throws Exception {
        TableWithEvents source = original(false);

        TableWithEvents copy = (TableWithEvents) source.copy();
        copy.getImpossibleConfigurations().add(new Configuration());

        assertEquals(1, source.getImpossibleConfigurations().size());
    }

    // -----------------------------------------------------------------------
    // deepCopy(ProbNet)
    // -----------------------------------------------------------------------

    @Test
    public void theDeepCopyBuildsItsOwnEventsVariable() throws Exception {
        TableWithEvents source = original(false);

        TableWithEvents copy = (TableWithEvents) source.deepCopy(destinationNetwork());

        assertNotNull(copy.getEvents());
        assertNotSame(source.getEvents(), copy.getEvents(),
                "the events variable is not a node of the network: it is built, not looked up");
    }

    @Test
    public void theTableOfTheDeepCopyIsBuiltOnTheVariablesOfTheDestination() throws Exception {
        ProbNet destination = destinationNetwork();

        TableWithEvents copy = (TableWithEvents) original(false).deepCopy(destination);

        List<Variable> tableVariables = copy.getTablePotential().getVariables();
        assertEquals(List.of("C", "P", "Events"), tableVariables.stream().map(Variable::getName).toList(),
                "the table joins the event parents into one variable; it does not list them apart");
        assertSame(destination.getVariable("C"), tableVariables.get(0),
                "the variables of the table must be those of the network it was copied into");
        assertSame(destination.getVariable("P"), tableVariables.get(1));
    }

    @Test
    public void theDeepCopyDoesNotShareItsTableWithTheOriginal() throws Exception {
        TableWithEvents source = original(false);

        TableWithEvents copy = (TableWithEvents) source.deepCopy(destinationNetwork());

        assertNotSame(source.getTablePotential(), copy.getTablePotential());
        copy.getTablePotential().getValues()[0] = 0.25;
        assertNotEquals(0.25, source.getTablePotential().getValues()[0],
                "writing a number on the copy must not write it on the original");
    }

    // -----------------------------------------------------------------------
    // The same, through the subclass that used to work around the copy constructor
    // -----------------------------------------------------------------------

    @Test
    public void aTransitionTableIsCopiedLikeAnyOtherTableWithEvents() {
        TransitionTablePotential source = new TransitionTablePotential(new ArrayList<>(variables),
                PotentialRole.CONDITIONAL_PROBABILITY);

        TransitionTablePotential copy = (TransitionTablePotential) source.deepCopy(destinationNetwork());

        assertNotSame(source.getTablePotential(), copy.getTablePotential(),
                "a deep copy that hands back the table of the original is not a deep copy");
        assertNotSame(source.getEvents(), copy.getEvents());
        assertNotSame(source.getTableVariables(), copy.getTableVariables());
        assertEquals(List.of("C", "P", "Events"),
                copy.getTableVariables().stream().map(Variable::getName).toList());
    }

    @Test
    public void aCopiedTransitionTableKeepsTheProperties() {
        TransitionTablePotential source = new TransitionTablePotential(new ArrayList<>(variables),
                PotentialRole.CONDITIONAL_PROBABILITY);
        source.properties.put("nameOfRelation", "the one the file gave it");

        Potential copy = source.deepCopy(destinationNetwork());

        assertEquals("the one the file gave it", copy.properties.get("nameOfRelation"));
    }
}
