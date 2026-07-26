/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Where the order of elimination comes from, and what that means for a variable that the network it
 * was computed on does not have.
 * <p>
 * The order is a pruning of the partial order of one network: whatever is a query, evidence or
 * conditioning variable comes out, and so does whatever is not in the list of variables to eliminate.
 * The consequence is easy to miss and it decides what inference can be asked to do - a variable that is
 * <em>not in that network</em> can never enter the order, however loudly it is asked for, because the
 * order is built by removing things from what that network has and never by adding.
 * <p>
 * That matters because the network the elimination runs on is not always the one the order was
 * computed from. Elimination runs on the Markov network built from the projected potentials, and
 * projection is free to introduce variables the original does not have: that is exactly what a
 * canonical model does if it hands over its factorization, whose factors are written on a pseudo
 * variable. Such a variable is never eliminated, and since the delta factor of the factorization holds
 * negative numbers that only become a probability once the pseudo variable is summed out, what comes
 * out the other end is a negative posterior rather than an error.
 *
 * @author Manuel Arias
 */
public class EliminationOrderComesFromOneNetworkTest {

    private static ProbNet networkOf(Variable... variables) {
        ProbNet net = new ProbNet();
        for (Variable variable : variables) {
            net.addNode(variable, NodeType.CHANCE);
        }
        return net;
    }

    @Test public void aVariableOfTheNetworkIsInTheOrderWhenItIsAskedToBeEliminated() {
        Variable a = new Variable("A", 2);
        Variable b = new Variable("B", 2);
        ProbNet net = networkOf(a, b);

        List<List<Variable>> order = BasicOperations.projectPartialOrder(
                net, List.of(), List.of(), List.of(), List.of(a, b));

        // As a set: within a block the order is not specified - it comes out of a hash set - and
        // asserting it produced a check that passed alone and failed in the whole suite.
        assertEquals(java.util.Set.of(a, b), new java.util.HashSet<>(order.stream().flatMap(List::stream).toList()));
    }

    /** Query, evidence and conditioning variables are pruned out, which is the intended behaviour. */
    @Test public void whatIsKeptForTheAnswerIsNotEliminated() {
        Variable a = new Variable("A", 2);
        Variable b = new Variable("B", 2);
        ProbNet net = networkOf(a, b);

        List<List<Variable>> order = BasicOperations.projectPartialOrder(
                net, List.of(a), List.of(), List.of(), List.of(a, b));

        assertEquals(List.of(b), order.stream().flatMap(List::stream).toList(),
                     "the query variable must survive the elimination");
    }

    /**
     * The one that decides what inference can be asked to do: asking for a variable the network does
     * not have achieves nothing at all. Not an error, not a warning - it is simply absent from the
     * order, because the order is only ever a pruning of what the network already has.
     */
    @Test public void askingToEliminateAVariableTheNetworkDoesNotHaveDoesNothing() {
        Variable a = new Variable("A", 2);
        Variable strangerToTheNetwork = new Variable("pseudo-A", 2);
        ProbNet net = networkOf(a);

        List<List<Variable>> order = BasicOperations.projectPartialOrder(
                net, List.of(), List.of(), List.of(), List.of(a, strangerToTheNetwork));

        List<Variable> flattened = order.stream().flatMap(List::stream).toList();
        assertTrue(flattened.contains(a), "the variable the network has is there");
        assertFalse(flattened.contains(strangerToTheNetwork),
                    "and the one it does not have cannot be put there by asking");
    }
}
