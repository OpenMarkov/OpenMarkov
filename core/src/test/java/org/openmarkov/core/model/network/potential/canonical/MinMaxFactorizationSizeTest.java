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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What a noisy-MAX costs as one table and what it costs as its factors, measured.
 * <p>
 * The factorization of Diez and Galan is already implemented here: one accrued potential per parent
 * over the pseudo variable, one for the leak, and the delta that turns the cumulative back into a
 * probability. What no code does yet is hand those factors to inference. Every route into the
 * inference engine goes through {@code tableProject}, which builds the factors and then multiplies
 * and marginalizes them straight back into a single table over the child and its parents - so the
 * exponential object is built at projection time, before variable elimination sees anything.
 * <p>
 * These checks measure both sides of that trade so the size of the prize is known before the
 * architecture is touched, and so that the same numbers say afterwards whether the cost really fell.
 * They are deliberately written as literal numbers as well as formulas: a change of order of growth
 * has to show up in the diff, not hide inside a formula that still holds.
 *
 * @author Manuel Arias
 */
public class MinMaxFactorizationSizeTest {

    /** A noisy-MAX over a child with {@code numStates} states and {@code numParents} parents. */
    private static MaxPotential noisyMax(int numStates, int numParents) {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("Y", numStates));
        for (int i = 0; i < numParents; i++) {
            variables.add(new Variable("X" + i, numStates));
        }
        return new MaxPotential(variables);
    }

    /** The numbers held by the single table that projection materializes today. */
    private static int sizeOfTheTable(MaxPotential potential) {
        return potential.getCPT().getValues().length;
    }

    /** The numbers held by all the factors together. */
    private static int sizeOfTheFactors(MaxPotential potential) {
        int total = 0;
        for (TablePotential factor : potential.getTablePotentials()) {
            total += factor.getValues().length;
        }
        return total;
    }

    // ------------------------------------------------------- one table: exponential

    @Test public void theTableMaterializedTodayDoublesWithEveryParentAdded() {
        for (int numParents = 1; numParents <= 14; numParents++) {
            assertEquals(1 << (numParents + 1), sizeOfTheTable(noisyMax(2, numParents)),
                         "the child and its parents, two states each");
        }
    }

    // ------------------------------------------------------- the factors: linear

    /**
     * One accrued potential per parent over (pseudo variable, that parent), one more for the leak over
     * the pseudo variable alone, and the delta over (pseudo variable, child). With m states that is
     * m*m for each parent, m for the leak and m*m for the delta.
     */
    @Test public void theFactorsGrowLinearlyWithTheParents() {
        for (int numParents = 1; numParents <= 14; numParents++) {
            int states = 2;
            int expected = numParents * states * states + states + states * states;

            assertEquals(expected, sizeOfTheFactors(noisyMax(states, numParents)));
        }
    }

    /** Linear, said as a property rather than as a formula: each new parent costs the same. */
    @Test public void eachNewParentCostsTheSameNumberOfNumbers() {
        int previous = sizeOfTheFactors(noisyMax(2, 1));
        int firstIncrement = sizeOfTheFactors(noisyMax(2, 2)) - previous;

        for (int numParents = 2; numParents <= 14; numParents++) {
            int current = sizeOfTheFactors(noisyMax(2, numParents));
            assertEquals(firstIncrement, current - previous,
                         "parent " + numParents + " cost a different amount than the ones before it");
            previous = current;
        }
        assertEquals(4, firstIncrement, "one accrued table over the pseudo variable and one parent");
    }

    // ------------------------------------------------------- the two side by side

    /**
     * The prize, as plain numbers. Left column the single table, right column all the factors
     * together, for a child of two states.
     */
    @Test public void theTwoCostsSideBySide() {
        assertEquals(4, sizeOfTheTable(noisyMax(2, 1)));
        assertEquals(10, sizeOfTheFactors(noisyMax(2, 1)));

        assertEquals(32, sizeOfTheTable(noisyMax(2, 4)));
        assertEquals(22, sizeOfTheFactors(noisyMax(2, 4)));

        assertEquals(2048, sizeOfTheTable(noisyMax(2, 10)));
        assertEquals(46, sizeOfTheFactors(noisyMax(2, 10)));

        assertEquals(32768, sizeOfTheTable(noisyMax(2, 14)));
        assertEquals(62, sizeOfTheFactors(noisyMax(2, 14)));
    }

    /**
     * Where the two cross, which is later than one would guess: at four parents, not three. With three
     * the factors still hold more numbers than the table does, 18 against 16. That is worth knowing
     * rather than glossing over - the factorization is not a saving for small models, so an
     * implementation that always factorizes would make those slightly worse, and one that chooses
     * needs a threshold. The crossover moves with the number of states, so it cannot be a constant.
     */
    @Test public void theFactorsOnlyStartPayingFromFourParentsOn() {
        assertEquals(4, sizeOfTheTable(noisyMax(2, 1)));
        assertEquals(10, sizeOfTheFactors(noisyMax(2, 1)));

        assertEquals(8, sizeOfTheTable(noisyMax(2, 2)));
        assertEquals(14, sizeOfTheFactors(noisyMax(2, 2)));

        assertEquals(16, sizeOfTheTable(noisyMax(2, 3)));
        assertEquals(18, sizeOfTheFactors(noisyMax(2, 3)), "still dearer than the table at three");

        assertEquals(32, sizeOfTheTable(noisyMax(2, 4)));
        assertEquals(22, sizeOfTheFactors(noisyMax(2, 4)), "and cheaper from four on");
    }

    /** And with more states per variable the table grows faster still, while the factors do not. */
    @Test public void moreStatesMakeTheTableWorseAndTheFactorsBarelyChange() {
        assertEquals(59049, sizeOfTheTable(noisyMax(3, 9)));
        assertEquals(93, sizeOfTheFactors(noisyMax(3, 9)));

        assertEquals(42, sizeOfTheFactors(noisyMax(2, 9)),
                     "three states instead of two takes the factors from 42 to 93, a bit over double...");
        assertEquals(1024, sizeOfTheTable(noisyMax(2, 9)),
                     "...and the table from 1024 to 59049, fifty-eight times as much");
    }
}
