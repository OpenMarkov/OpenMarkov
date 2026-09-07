/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The maximum of utilities is a utility, so it keeps the criterion. The three ways of maximizing
 * have to agree on that, because a result without a criterion is filed with the probabilities.
 *
 * @author Manuel Arias
 */
public class MaximizingKeepsTheCriterionTest {

    private final Criterion cost = new Criterion("cost");
    private final Variable decision = new Variable("D", 2);
    private final Variable kept = new Variable("K", 2);

    private TablePotential aUtilityOver(List<Variable> variables, double... values) {
        TablePotential utility = new TablePotential(variables, PotentialRole.UNSPECIFIED, values);
        utility.setCriterion(cost);
        return utility;
    }

    @Tag(TestSpeed.FAST)
    @Test public void maximizingOverACollectionKeepsIt() {
        TablePotential result = TablePotentialMaximization.maximize(
                List.of(aUtilityOver(List.of(decision), 1, 5), aUtilityOver(List.of(decision), 3, 2)));

        assertSame(cost, result.getCriterion(), "the maximum of two utilities is a utility");
        assertArrayEquals(new double[] { 3, 5 }, result.getValues(), 1e-12, "the numbers themselves");
    }

    @Tag(TestSpeed.FAST)
    @Test public void maximizingOutAVariableKeepsIt() {
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                List.of(aUtilityOver(List.of(decision, kept), 1, 5, 3, 2)), List.of(kept), decision);

        assertSame(cost, ((TablePotential) result[0]).getCriterion(),
                "maximizing a decision out of a utility leaves a utility");
    }

    @Tag(TestSpeed.FAST)
    @Test public void maximizingOutAVariableUniformlyKeepsIt() {
        TablePotential[] result = DiscretePotentialOperations.multiplyAndMaximizeUniformly(
                List.of(aUtilityOver(List.of(decision, kept), 1, 5, 3, 2)), List.of(kept), decision);

        assertSame(cost, result[0].getCriterion(),
                "maximizing a decision out of a utility leaves a utility");
    }

    @Tag(TestSpeed.FAST)
    @Test public void everyPotentialOfTheCollectionIsReadOnlyOnce() {
        TablePotential first = aUtilityOver(List.of(decision), 1, 5);

        TablePotential result = TablePotentialMaximization.maximize(List.of(first, aUtilityOver(List.of(decision), 3, 2)));

        assertArrayEquals(new double[] { 1, 5 }, first.getValues(), 1e-12,
                "the first potential of the collection was touched");
        assertArrayEquals(new double[] { 3, 5 }, result.getValues(), 1e-12, "the numbers themselves");
    }
}
