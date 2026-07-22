/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link NetworkDatabaseCoherence}, the pure coherence analysis extracted from the evaluation
 * dialog (F3). Covers the three coherence levels, translation of state indices by name, the reported
 * variable lists, and that the input database is not modified.
 *
 * @author Manuel Arias
 */
class NetworkDatabaseCoherenceTest {

    private static ProbNet net(Variable... variables) {
        ProbNet net = new ProbNet();
        for (Variable v : variables) {
            net.addNode(v, NodeType.CHANCE);
        }
        return net;
    }

    private static List<String> names(List<Variable> variables) {
        return variables.stream().map(Variable::getName).toList();
    }

    @Test
    void strongCoherenceWhenEveryNetworkVariableMatches() {
        ProbNet net = net(new Variable("A", "x", "y"), new Variable("B", "p", "q"));
        CaseDatabase db = new CaseDatabase(
                List.of(new Variable("A", "x", "y"), new Variable("B", "p", "q")),
                new int[][] { { 0, 0 }, { 1, 1 } });

        NetworkDatabaseCoherence.Result r = NetworkDatabaseCoherence.analyze(net, db);

        assertEquals(Coherence.STRONG, r.level());
        assertEquals(List.of("A", "B"), r.matchedVariableNames());
        assertTrue(r.variablesNotInDatabase().isEmpty());
        assertTrue(r.variablesWithIncompatibleStates().isEmpty());
        assertNotNull(r.netDatabase());
        assertEquals(2, r.netDatabase().getVariables().size());
    }

    @Test
    void weakCoherenceWhenSomeNetworkVariablesAreMissing() {
        ProbNet net = net(new Variable("A", "x", "y"), new Variable("B", "p", "q"), new Variable("C", "m", "n"));
        CaseDatabase db = new CaseDatabase(
                List.of(new Variable("A", "x", "y"), new Variable("B", "p", "q")),
                new int[][] { { 0, 0 } });

        NetworkDatabaseCoherence.Result r = NetworkDatabaseCoherence.analyze(net, db);

        assertEquals(Coherence.WEAK, r.level());
        assertEquals(List.of("A", "B"), r.matchedVariableNames());
        assertEquals(List.of("C"), names(r.variablesNotInDatabase()));
    }

    @Test
    void zeroCoherenceWhenFewerThanTwoVariablesMatch() {
        ProbNet net = net(new Variable("A", "x", "y"), new Variable("B", "p", "q"));
        CaseDatabase db = new CaseDatabase(
                List.of(new Variable("A", "x", "y")), new int[][] { { 0 } });

        NetworkDatabaseCoherence.Result r = NetworkDatabaseCoherence.analyze(net, db);

        assertEquals(Coherence.ZERO, r.level());
        assertNull(r.netDatabase());
        assertEquals(List.of("B"), names(r.variablesNotInDatabase()));
    }

    @Test
    void variableWithAStateAbsentFromTheNetworkIsReportedIncompatible() {
        ProbNet net = net(new Variable("A", "x", "y"), new Variable("B", "p", "q"), new Variable("C", "m", "n"));
        CaseDatabase db = new CaseDatabase(
                List.of(new Variable("A", "x", "z"),   // "z" is not a state of the network's A
                        new Variable("B", "p", "q"),
                        new Variable("C", "m", "n")),
                new int[][] { { 0, 0, 0 } });

        NetworkDatabaseCoherence.Result r = NetworkDatabaseCoherence.analyze(net, db);

        assertEquals(Coherence.WEAK, r.level());                     // B and C still match
        assertEquals(List.of("B", "C"), r.matchedVariableNames());
        assertEquals(List.of("A"), names(r.variablesWithIncompatibleStates()));
    }

    @Test
    void statesAreTranslatedByNameAndTheInputDatabaseIsNotModified() {
        ProbNet net = net(new Variable("A", "x", "y"), new Variable("B", "p", "q")); // net A: x=0, y=1
        Variable dbA = new Variable("A", "y", "x");                                  // db  A: y=0, x=1 (reversed)
        Variable dbB = new Variable("B", "p", "q");
        CaseDatabase db = new CaseDatabase(List.of(dbA, dbB), new int[][] { { 0, 0 } }); // A=0("y"), B=0("p")

        NetworkDatabaseCoherence.Result r = NetworkDatabaseCoherence.analyze(net, db);

        assertEquals(Coherence.STRONG, r.level());
        // A: db index 0 ("y") maps to network index 1; B: 0 maps to 0. Columns follow network order.
        assertArrayEquals(new int[] { 1, 0 }, r.netDatabase().getCases()[0]);
        // The input database is untouched: its A column still reads the original index 0.
        assertArrayEquals(new int[] { 0 }, db.getCases(dbA));
    }
}
