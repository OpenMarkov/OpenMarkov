/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.em;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Learning the parameters of a canonical model, and the size of the table that stops it scaling.
 * <p>
 * None of the other tests of this algorithm uses a canonical potential, so the whole branch of
 * {@code adaptNetwork} that deals with them - one z variable per parent, a leak variable, and the
 * f function on the child - ran untested. These tests cover it, and measure what it costs.
 * <p>
 * What it costs is the point. To learn a canonical node, the algorithm replaces the child's
 * potential with the f function, a single table over the child, one z variable per parent and the
 * leak. Every one of those has as many states as the child, so the table has m raised to the number
 * of parents plus two. That is the monolithic f function: the size grows exponentially in the
 * parents, and it is what a cascade of binary tables would replace with something linear. These
 * tests pin the present numbers so the change is visible when it happens.
 *
 * @author Manuel Arias
 */
public class EMWithCanonicalModelsTest {

    /** A noisy-MAX over a child with {@code numStates} states and {@code numParents} parents. */
    private static MaxPotential noisyMax(int numStates, int numParents) {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("Y", numStates));
        for (int i = 0; i < numParents; i++) {
            variables.add(new Variable("X" + i, numStates));
        }
        return new MaxPotential(variables);
    }

    // ------------------------------------------------------- what the f function costs

    /**
     * The size of the f function, exactly, as the parents grow. One variable for the child, one per
     * parent's z, and one for the leak, all with the child's number of states.
     */
    @Test public void theFFunctionGrowsExponentiallyWithTheNumberOfParents() {
        for (int numParents = 1; numParents <= 14; numParents++) {
            TablePotential fFunction = noisyMax(2, numParents).getFFunctionPotential();

            assertEquals(numParents + 2, fFunction.getVariables().size(),
                         "the child, one z per parent and the leak");
            assertEquals(1 << (numParents + 2), fFunction.getValues().length,
                         "two states raised to the child plus the z variables plus the leak");
        }
    }

    /** The same for a three-state child, where the base of the exponential is three. */
    @Test public void theBaseOfTheExponentialIsTheNumberOfStatesOfTheChild() {
        for (int numParents = 1; numParents <= 8; numParents++) {
            TablePotential fFunction = noisyMax(3, numParents).getFFunctionPotential();

            assertEquals(Math.round(Math.pow(3, numParents + 2.0)), fFunction.getValues().length);
        }
    }

    /**
     * The numbers spelled out, so that a change of order of growth is unmistakable in the diff
     * rather than hidden inside a formula that still holds.
     */
    @Test public void theSizesAsPlainNumbers() {
        assertEquals(8, noisyMax(2, 1).getFFunctionPotential().getValues().length);
        assertEquals(64, noisyMax(2, 4).getFFunctionPotential().getValues().length);
        assertEquals(4096, noisyMax(2, 10).getFFunctionPotential().getValues().length);
        assertEquals(65536, noisyMax(2, 14).getFFunctionPotential().getValues().length);
        assertEquals(531441, noisyMax(3, 10).getFFunctionPotential().getValues().length);
    }

    // ------------------------------------------------------- learning one, at all

    /**
     * The branch that had no test: a network whose child carries a canonical model. What is checked
     * is that the algorithm completes and that what it hands back is still a canonical model whose
     * noisy parameters are probability distributions - one column per parent state, summing to one.
     */
    @Test public void learningACanonicalNodeKeepsItCanonicalAndItsParametersProper() throws Exception {
        int numParents = 3;
        ProbNet net = networkWithANoisyMax(numParents);
        CaseDatabase database = databaseOver(net, numParents);

        ProbNet learned = new EMAlgorithm(net, database, 0.0).parametricLearning();

        assertNotNull(learned);
        var potential = learned.getNode("Y").getPotentials().getFirst();
        assertInstanceOf(ICIPotential.class, potential, "the child must still carry a canonical model");
        assertNoisyParametersAreDistributions((ICIPotential) potential, learned, numParents);
    }

    /**
     * The same with many parents, which is where the f function stops being free: with eight parents
     * its table already holds 1024 numbers, and every one of them takes part in the inference the
     * expectation step runs once per case. This pins that it still works today.
     */
    @Tag(TestSpeed.MEDIUM)
    @Test public void learningStillWorksWithManyParents() throws Exception {
        int numParents = 8;
        ProbNet net = networkWithANoisyMax(numParents);
        assertEquals(1024, ((ICIPotential) net.getNode("Y").getPotentials().getFirst())
                .getFFunctionPotential().getValues().length, "the size this test is about");
        CaseDatabase database = databaseOver(net, numParents);

        ProbNet learned = new EMAlgorithm(net, database, 0.0).parametricLearning();

        assertNotNull(learned);
        var potential = learned.getNode("Y").getPotentials().getFirst();
        assertInstanceOf(ICIPotential.class, potential);
        assertNoisyParametersAreDistributions((ICIPotential) potential, learned, numParents);
    }

    // ------------------------------------------------------- helpers

    private static ProbNet networkWithANoisyMax(int numParents) {
        ProbNet net = new ProbNet();
        Variable child = new Variable("Y", 2);
        net.addNode(child, NodeType.CHANCE);
        List<Variable> variables = new ArrayList<>();
        variables.add(child);
        for (int i = 0; i < numParents; i++) {
            Variable parent = new Variable("X" + i, 2);
            net.addNode(parent, NodeType.CHANCE);
            net.addLink(parent, child, true);
            variables.add(parent);
            net.addPotential(new TablePotential(List.of(parent),
                                                org.openmarkov.core.model.network.potential.PotentialRole.CONDITIONAL_PROBABILITY));
        }
        net.getNode(child).setPotential(new MaxPotential(variables));
        return net;
    }

    /** Cases over the parents alone, so the child is the unobserved variable EM has to work on. */
    private static CaseDatabase databaseOver(ProbNet net, int numParents) {
        List<Variable> observed = new ArrayList<>();
        for (int i = 0; i < numParents; i++) {
            observed.add(net.getVariable("X" + i));
        }
        int numCases = 12;
        int[][] cases = new int[numCases][numParents];
        for (int c = 0; c < numCases; c++) {
            for (int i = 0; i < numParents; i++) {
                cases[c][i] = (c / (i + 1)) % 2;
            }
        }
        return new CaseDatabase(observed, cases);
    }

    private static void assertNoisyParametersAreDistributions(ICIPotential potential, ProbNet net,
                                                              int numParents) {
        for (int i = 0; i < numParents; i++) {
            Variable parent = net.getVariable("X" + i);
            double[] parameters = potential.getNoisyParameters(parent);
            assertNotNull(parameters, "no noisy parameters for " + parent.getName());
            int numStates = net.getVariable("Y").getNumStates();
            for (int column = 0; column < parameters.length / numStates; column++) {
                double sum = 0;
                for (int state = 0; state < numStates; state++) {
                    sum += parameters[column * numStates + state];
                }
                assertEquals(1.0, sum, 1e-9,
                             "column " + column + " of " + parent.getName() + " is not a distribution");
            }
        }
    }
}
