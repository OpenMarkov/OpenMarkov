/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * First tests for {@link DBGenerator}. They pin down the reproducibility of the seeded overload.
 *
 * @author Manuel Arias
 */
public class DBGeneratorTest {

    private ProbNet probNet;

    /**
     * A -> B, with non-degenerate probabilities so that sampling actually varies.
     */
    @BeforeEach public void setUp() {
        State[] states = {new State("absent"), new State("present")};
        Variable variableA = new Variable("A", states);
        Variable variableB = new Variable("B", states);

        probNet = new ProbNet();
        probNet.addNode(variableA, NodeType.CHANCE);
        probNet.addNode(variableB, NodeType.CHANCE);
        probNet.addLink(variableA, variableB, true);

        ArrayList<Variable> variablesA = new ArrayList<>();
        variablesA.add(variableA);
        // P(A): 0.4 / 0.6
        probNet.addPotential(new TablePotential(variablesA, PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.4, 0.6}));

        ArrayList<Variable> variablesBA = new ArrayList<>();
        variablesBA.add(variableB);
        variablesBA.add(variableA);
        // P(B|A)
        probNet.addPotential(new TablePotential(variablesBA, PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.7, 0.3, 0.2, 0.8}));
    }

    @Test public void sameSeedProducesIdenticalDatabases() {
        CaseDatabase first = DBGenerator.generate(probNet, 500, 12345L);
        CaseDatabase second = DBGenerator.generate(probNet, 500, 12345L);
        assertArrayEquals(first.getCases(), second.getCases());
    }

    @Test public void differentSeedsProduceDifferentDatabases() {
        CaseDatabase first = DBGenerator.generate(probNet, 500, 12345L);
        CaseDatabase second = DBGenerator.generate(probNet, 500, 67890L);
        assertFalse(java.util.Arrays.deepEquals(first.getCases(), second.getCases()),
                "two different seeds produced the same 500-case database");
    }
}
