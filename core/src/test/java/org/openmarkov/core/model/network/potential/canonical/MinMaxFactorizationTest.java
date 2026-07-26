/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The pieces a noisy-MAX or noisy-MIN breaks into, tested one by one.
 * <p>
 * Until now these were covered only through the table they produce: {@code ICIPotentialDifferentialTest}
 * checks the finished conditional probability table against brute force, and everything in between was
 * taken on trust. That is enough while the factorization is a private step on the way to a table, and
 * not enough once the factors become things inference multiplies on their own - which is exactly what
 * the ICI paper's algorithm does with them.
 * <p>
 * What the factorization says, for MAX. Each parent contributes a noisy table P(z | x). Its
 * <em>accrued</em> form is the running sum over the states of the child, P(z &le; y | x), written on a
 * pseudo variable that stands for "the max so far". Multiply one accrued factor per parent and the
 * product is P(Y &le; y | parents), the cumulative distribution of the child. The <em>delta</em> factor
 * then turns the cumulative back into a probability by subtracting the state below, and marginalizing
 * the pseudo variable away leaves the table. For MIN the sums run the other way and delta subtracts
 * the state above.
 *
 * @author Manuel Arias
 */
public class MinMaxFactorizationTest {

    private static final double PRECISION = 1E-9;

    /** Y with three states and two parents with two and three, so no dimension can stand in for another. */
    private static List<Variable> variables() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("Y", 3));
        variables.add(new Variable("A", 2));
        variables.add(new Variable("B", 3));
        return variables;
    }

    private static MaxPotential maxModel() {
        List<Variable> variables = variables();
        MaxPotential potential = new MaxPotential(variables);
        potential.setNoisyParameters(variables.get(1), new double[]{0.7, 0.2, 0.1, 0.1, 0.3, 0.6});
        potential.setNoisyParameters(variables.get(2),
                                     new double[]{0.5, 0.3, 0.2, 0.2, 0.5, 0.3, 0.1, 0.1, 0.8});
        potential.setLeakyParameters(new double[]{0.8, 0.15, 0.05});
        return potential;
    }

    private static MinPotential minModel() {
        List<Variable> variables = variables();
        MinPotential potential = new MinPotential(variables);
        potential.setNoisyParameters(variables.get(1), new double[]{0.7, 0.2, 0.1, 0.1, 0.3, 0.6});
        potential.setNoisyParameters(variables.get(2),
                                     new double[]{0.5, 0.3, 0.2, 0.2, 0.5, 0.3, 0.1, 0.1, 0.8});
        potential.setLeakyParameters(new double[]{0.05, 0.15, 0.8});
        return potential;
    }

    // ------------------------------------------------------------- the pseudo variable

    @Test public void thePseudoVariableStandsForTheChildAndIsNotOneOfTheVariables() {
        MaxPotential potential = maxModel();

        Variable pseudoVariable = potential.getPseudoVariable();

        assertNotNull(pseudoVariable);
        assertEquals(potential.getConditionedVariable().getNumStates(), pseudoVariable.getNumStates(),
                     "the pseudo variable ranges over the same states as the child");
        assertFalse(potential.getVariables().contains(pseudoVariable),
                    "the pseudo variable belongs to the factorization, not to the model");
        assertNotSame(potential.getConditionedVariable(), pseudoVariable);
    }

    // ------------------------------------------------------------- the shape of the factorization

    /**
     * Exactly one factor - the delta - joins the child to the pseudo variable; every other factor is
     * written on the pseudo variable and its own parent. That shape is what lets inference multiply
     * the parents' contributions before ever mentioning the child.
     */
    @Test public void onlyTheDeltaFactorMentionsTheChild() {
        MaxPotential potential = maxModel();
        Variable child = potential.getConditionedVariable();
        Variable pseudoVariable = potential.getPseudoVariable();

        List<TablePotential> factors = potential.getTablePotentials();

        List<TablePotential> mentioningTheChild = factors.stream()
                                                         .filter(factor -> factor.getVariables().contains(child))
                                                         .toList();
        assertEquals(1, mentioningTheChild.size(), "only the delta factor may mention the child");
        assertTrue(mentioningTheChild.getFirst().getVariables().contains(pseudoVariable),
                   "the delta factor joins the child to the pseudo variable");
        for (TablePotential factor : factors) {
            assertTrue(factor.getVariables().contains(pseudoVariable),
                       "every factor of the factorization is written on the pseudo variable");
        }
    }

    /** One factor per parent, one for the leak, and the delta. */
    @Test public void thereIsOneFactorPerParentPlusTheLeakAndTheDelta() {
        assertEquals(4, maxModel().getTablePotentials().size());
        assertEquals(4, minModel().getTablePotentials().size());
    }

    // ------------------------------------------------------------- the contract of the factorization

    /**
     * The whole point of the factorization: multiply its factors, marginalize the pseudo variable
     * away, and what is left is the conditional probability table - the same one
     * {@code ICIPotentialDifferentialTest} checks against brute force.
     */
    @Test public void theFactorsOfAMaxModelMultiplyBackIntoItsTable() {
        assertFactorsReproduceTheTable(maxModel());
    }

    @Test public void theFactorsOfAMinModelMultiplyBackIntoItsTable() {
        assertFactorsReproduceTheTable(minModel());
    }

    private static void assertFactorsReproduceTheTable(MinMaxPotential potential) {
        TablePotential table = potential.getCPT();

        TablePotential fromTheFactors = DiscretePotentialOperations.multiplyAndMarginalize(
                potential.getTablePotentials(), potential.getVariables(),
                List.of(potential.getPseudoVariable()));

        assertArrayEquals(table.getValues(), fromTheFactors.getValues(), PRECISION,
                          "multiplying the factors must give back the table the model materializes");
    }

    // ------------------------------------------------------------- what an accrued factor means

    /**
     * The semantic heart, and the property the factorized algorithm rests on: the product of the
     * accrued factors - everything but the delta - is the CUMULATIVE distribution of the child. For
     * MAX, its value at state k is the probability that the child is at k or below.
     */
    @Test public void theAccruedFactorsOfAMaxModelMultiplyIntoTheCumulativeOfItsTable() {
        MaxPotential potential = maxModel();
        double[] cumulative = productOfTheAccruedFactors(potential);
        double[] table = potential.getCPT().getValues();
        int numStates = potential.getConditionedVariable().getNumStates();

        for (int configuration = 0; configuration < table.length / numStates; configuration++) {
            double runningSum = 0;
            for (int state = 0; state < numStates; state++) {
                runningSum += table[configuration * numStates + state];
                assertEquals(runningSum, cumulative[configuration * numStates + state], PRECISION,
                             "the accrued factors must multiply into P(Y <= y) at state " + state);
            }
        }
    }

    /** And for MIN the same, running the other way: the product is the probability of k or above. */
    @Test public void theAccruedFactorsOfAMinModelMultiplyIntoTheReverseCumulativeOfItsTable() {
        MinPotential potential = minModel();
        double[] cumulative = productOfTheAccruedFactors(potential);
        double[] table = potential.getCPT().getValues();
        int numStates = potential.getConditionedVariable().getNumStates();

        for (int configuration = 0; configuration < table.length / numStates; configuration++) {
            double runningSum = 0;
            for (int state = numStates - 1; state >= 0; state--) {
                runningSum += table[configuration * numStates + state];
                assertEquals(runningSum, cumulative[configuration * numStates + state], PRECISION,
                             "the accrued factors must multiply into P(Y >= y) at state " + state);
            }
        }
    }

    /**
     * Multiplies every factor except the delta - the delta is the one that mentions the child - and
     * returns the values laid out over (pseudo variable, parents), which is the same layout the table
     * has over (child, parents) because the pseudo variable ranges over the child's states.
     */
    private static double[] productOfTheAccruedFactors(MinMaxPotential potential) {
        Variable child = potential.getConditionedVariable();
        List<TablePotential> accrued = potential.getTablePotentials().stream()
                                                .filter(factor -> !factor.getVariables().contains(child))
                                                .toList();
        List<Variable> layout = new ArrayList<>(potential.getVariables());
        layout.set(0, potential.getPseudoVariable());
        return DiscretePotentialOperations.multiplyAndMarginalize(accrued, layout, List.of()).getValues();
    }
}
