/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.inference.Choice;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The maximization compares each candidate against the largest one so far allowing for a round
 * error. That error has to be a fraction of what is being compared: with a fixed threshold, a
 * column whose products all fall below it never beat the first one, so the maximization answered
 * the value of the first state and filed the real maximum as a tie with it. A product of several
 * probabilities falls below a hundred-millionth without being small for the model that holds it.
 *
 * @author Manuel Arias
 */
class MaximizingSmallNumbersFindsTheMaximumTest {

    private final Variable decision = new Variable("D", "d0", "d1", "d2");

    private ArrayList<Potential> aUtilityOver(double... values) {
        ArrayList<Potential> potentials = new ArrayList<>();
        potentials.add(new TablePotential(List.of(decision), PotentialRole.UNSPECIFIED, values));
        return potentials;
    }

    @Tag(TestSpeed.FAST)
    @Test void theMaximumOfSmallNumbersIsTheLargestOfThem() {
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                aUtilityOver(1e-12, 1e-9, 1e-11), List.of(), decision);

        assertArrayEquals(new double[] { 1e-9 }, ((TablePotential) result[0]).getValues(),
                          "the maximum of the three, not the value of the first state");
    }

    @Tag(TestSpeed.FAST)
    @Test void theStateFiledAsOptimalIsTheOneThatHoldsTheMaximum() {
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                aUtilityOver(1e-12, 1e-9, 1e-11), List.of(), decision);

        @SuppressWarnings("unchecked")
        Choice choice = ((GTablePotential<Choice>) result[1]).elementTable.getFirst();
        assertArrayEquals(new int[] { 1 }, choice.getValues(),
                          "only the second state holds the maximum, so it is not a tie");
    }

    @Tag(TestSpeed.FAST)
    @Test void numbersOfEverydaySizeAreStillMaximized() {
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                aUtilityOver(2.0, 7.0, 5.0), List.of(), decision);

        assertEquals(7.0, ((TablePotential) result[0]).getValues()[0]);
    }

    @Tag(TestSpeed.FAST)
    @Test void theUniformVariantAlsoFindsIt() {
        List<TablePotential> tables = new ArrayList<>();
        tables.add(new TablePotential(List.of(decision), PotentialRole.UNSPECIFIED,
                                      new double[] { 1e-12, 1e-9, 1e-11 }));

        TablePotential[] result = DiscretePotentialOperations.multiplyAndMaximizeUniformly(
                tables, List.of(), decision);

        assertArrayEquals(new double[] { 1e-9 }, result[0].getValues());
    }
}
