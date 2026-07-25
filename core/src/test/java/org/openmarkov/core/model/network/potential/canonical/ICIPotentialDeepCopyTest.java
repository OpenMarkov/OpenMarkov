/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What a deep copy of a canonical model owes the network it is copied into.
 * <p>
 * These checks exist because the general clone battery cannot make them. It compares a potential
 * against its copy field by field, by value, and two Variable objects with the same name and states
 * compare equal that way - so a potential still pointing at the variables of the source network
 * passes it. Variable compares by identity everywhere else, which is what makes that a defect.
 *
 * @author Manuel Arias
 */
public class ICIPotentialDeepCopyTest {

    private ProbNet originalNetwork;
    private ICIPotential originalPotential;

    @BeforeEach public void setUp() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("Y", 2));
        variables.add(new Variable("P0", 2));
        variables.add(new Variable("P1", 2));
        originalPotential = new MaxPotential(variables);
        originalPotential.setNoisyParameters(variables.get(1), new double[]{0.8, 0.2, 0.3, 0.7});
        originalPotential.setNoisyParameters(variables.get(2), new double[]{0.6, 0.4, 0.1, 0.9});
        originalPotential.setLeakyParameters(new double[]{0.9, 0.1});

        originalNetwork = new ProbNet();
        for (Variable variable : variables) {
            originalNetwork.addNode(variable, NodeType.CHANCE);
        }
        originalNetwork.getNode(variables.getFirst()).setPotential(originalPotential);
    }

    /**
     * The one that used to fail. Asking a copied canonical model for its factorized form threw
     * ArrayIndexOutOfBoundsException with index -2, because the map from parents to z variables kept
     * the parents of the source network as its keys: looking one of them up among the copy's
     * variables gave -1.
     */
    @Test public void aCopiedModelCanStillBeAskedForItsNoisyPotentials() {
        ICIPotential copy = copyOfPotential();

        List<TablePotential> noisy = copy.getNoisyPotentials();

        assertEquals(2, noisy.size());
        assertArrayEquals(new double[]{0.8, 0.2, 0.3, 0.7}, noisy.get(0).getValues(), 1E-9);
        assertArrayEquals(new double[]{0.6, 0.4, 0.1, 0.9}, noisy.get(1).getValues(), 1E-9);
    }

    /**
     * Not equal by value: the same objects. Every other part of the model looks variables up by
     * identity, so a copy holding equal-but-distinct variables is a copy that will misbehave later.
     */
    @Test public void theFactorsOfACopyAreBuiltOnTheVariablesOfTheCopiedNetwork() {
        ProbNet copiedNetwork = originalNetwork.deepCopy();
        ICIPotential copy = (ICIPotential) copiedNetwork.getNode("Y").getPotentials().getFirst();

        List<TablePotential> noisy = copy.getNoisyPotentials();

        assertSame(copiedNetwork.getVariable("P0"), noisy.get(0).getVariables().get(1));
        assertSame(copiedNetwork.getVariable("P1"), noisy.get(1).getVariables().get(1));
    }

    /**
     * The leak is not a node of the network, so it cannot be found by looking it up there - which is
     * how the copy used to try, silently getting null and losing the leak.
     */
    @Test public void aCopyKeepsItsLeak() {
        ICIPotential copy = copyOfPotential();

        assertNotNull(copy.getLeakyVariable(), "the copy lost the leak variable");
        assertEquals(originalPotential.getLeakyVariable().getName(), copy.getLeakyVariable().getName());
        assertArrayEquals(originalPotential.getLeakyParameters(), copy.getLeakyParameters(), 1E-9);
    }

    /** The order of the parents is the order of the noisy parameters, so the copy must keep it. */
    @Test public void aCopyKeepsTheOrderOfItsParents() {
        ICIPotential copy = copyOfPotential();

        List<TablePotential> noisy = copy.getNoisyPotentials();

        assertEquals("P0", noisy.get(0).getVariables().get(1).getName());
        assertEquals("P1", noisy.get(1).getVariables().get(1).getName());
    }

    private ICIPotential copyOfPotential() {
        return (ICIPotential) originalNetwork.deepCopy().getNode("Y").getPotentials().getFirst();
    }
}
