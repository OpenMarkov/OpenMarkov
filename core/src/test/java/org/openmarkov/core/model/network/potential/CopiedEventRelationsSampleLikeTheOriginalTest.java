/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * A relation of the discrete-event simulation with two event parents answers differently depending
 * on which of them has happened. Its copy read always the column of the first event, because its
 * table kept naming the events variable of the original; and the copy of a transition table was not
 * a transition table, so it could not be sampled at all (errors 19 and 20 of the pending list).
 *
 * @author Manuel Arias
 */
public class CopiedEventRelationsSampleLikeTheOriginalTest {

    private static final double[] RANDOM = {0.5};

    private Variable firstEvent;
    private Variable secondEvent;

    private static Variable event(String name) {
        Variable variable = new Variable(name, new State[]{new State("no"), new State("yes")});
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    @BeforeEach
    public void setUp() {
        firstEvent = event("E1");
        secondEvent = event("E2");
    }

    private static EvidenceCase happened(Variable event, Finding... others) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(event, 1));
        for (Finding other : others) {
            evidence.addFinding(other);
        }
        return evidence;
    }

    private static ProbNet networkWith(List<Variable> variables) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        for (Variable variable : variables) {
            net.addNode(variable, NodeType.CHANCE);
        }
        return net;
    }

    /** Exponential time to the event: rate 0.1 if E1 triggered it, 0.3 if E2 did. */
    private DistributionTablePotential timeToEvent() {
        DistributionTablePotential potential = new DistributionTablePotential(
                new ArrayList<>(List.of(event("Death"), firstEvent, secondEvent)),
                PotentialRole.CONDITIONAL_PROBABILITY, "Exponential", "Lambda");
        potential.getTableWithEvents().setValues(new double[]{0.1, 0.3});
        return potential;
    }

    /** The node becomes "yes" when E1 happens and "no" when E2 does. */
    private TransitionTablePotential transitionTable() {
        Variable ill = new Variable("Ill", new State[]{new State("no"), new State("yes")});
        TransitionTablePotential potential = new TransitionTablePotential(
                new ArrayList<>(List.of(ill, firstEvent, secondEvent)), PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setValues(new double[]{0, 1, 1, 0});
        return potential;
    }

    private void assertSamplesLikeTheOriginal(DESSimulablePotential original, Potential copy) throws Exception {
        DESSimulablePotential simulableCopy = assertInstanceOf(DESSimulablePotential.class, copy);
        for (Variable event : List.of(firstEvent, secondEvent)) {
            assertEquals(original.sampleConditionedVariable(RANDOM, happened(event)),
                    simulableCopy.sampleConditionedVariable(RANDOM, happened(event)),
                    "after " + event.getName() + " the copy must answer what the original answers");
        }
    }

    @Test
    public void theOriginalsTellTheEventsApart() throws Exception {
        assertEquals(Math.log(2) / 0.1, timeToEvent().sampleConditionedVariable(RANDOM, happened(firstEvent)), 1e-12);
        assertEquals(Math.log(2) / 0.3, timeToEvent().sampleConditionedVariable(RANDOM, happened(secondEvent)), 1e-12);
        assertEquals(1.0, transitionTable().sampleConditionedVariable(RANDOM, happened(firstEvent)));
        assertEquals(0.0, transitionTable().sampleConditionedVariable(RANDOM, happened(secondEvent)));
    }

    @Test
    public void aCopiedTimeToEventReadsTheParametersOfTheEventThatHappened() throws Exception {
        DistributionTablePotential original = timeToEvent();

        assertSamplesLikeTheOriginal(original, original.copy());
    }

    /** What the window of the relation puts back in the node when the user cancels. */
    @Test
    public void aDeepCopiedTimeToEventReadsTheParametersOfTheEventThatHappened() throws Exception {
        DistributionTablePotential original = timeToEvent();

        assertSamplesLikeTheOriginal(original, original.deepCopy(networkWith(original.getVariables())));
    }

    @Test
    public void aCopiedTimeToEventWithANumericParentReadsTheFunctionOfTheEventThatHappened() throws Exception {
        Variable age = new Variable("Age");
        DistributionTablePotential original = new DistributionTablePotential(
                new ArrayList<>(List.of(event("Death"), firstEvent, secondEvent, age)),
                PotentialRole.CONDITIONAL_PROBABILITY, "Exponential", "Lambda");
        String[] functions = original.getTableWithEvents().getTableWithFunctions().getFunctionValues();
        functions[0] = "0.1";
        functions[1] = "0.3";
        Finding ageIsKnown = new Finding(age, 40.0);

        DistributionTablePotential copy = (DistributionTablePotential) original.copy();

        assertEquals(Math.log(2) / 0.3, original.sampleConditionedVariable(RANDOM, happened(secondEvent, ageIsKnown)), 1e-12);
        for (Variable event : List.of(firstEvent, secondEvent)) {
            assertEquals(original.sampleConditionedVariable(RANDOM, happened(event, ageIsKnown)),
                    copy.sampleConditionedVariable(RANDOM, happened(event, ageIsKnown)));
        }
    }

    @Test
    public void theCopyOfATransitionTableIsATransitionTable() {
        assertInstanceOf(TransitionTablePotential.class, transitionTable().copy());
    }

    @Test
    public void aCopiedTransitionTableGoesToTheStateOfTheEventThatHappened() throws Exception {
        TransitionTablePotential original = transitionTable();

        assertSamplesLikeTheOriginal(original, original.copy());
    }

    @Test
    public void aDeepCopiedTransitionTableGoesToTheStateOfTheEventThatHappened() throws Exception {
        TransitionTablePotential original = transitionTable();

        assertSamplesLikeTheOriginal(original, original.deepCopy(networkWith(original.getVariables())));
    }
}
