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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Adding utilities keeps the criterion, which is what makes the result a utility. It is taken the
 * same way as when multiplying: the first one that is not null, constants included.
 *
 * @author Manuel Arias
 */
public class SumKeepsTheCriterionTest {

    private final Criterion cost = new Criterion("cost");
    private final Variable a = new Variable("A", 2);

    private TablePotential aPotentialOver(Variable variable, double... values) {
        return new TablePotential(List.of(variable), PotentialRole.UNSPECIFIED, values);
    }

    private TablePotential aConstantWorth(double value, Criterion criterion) {
        TablePotential constant = new TablePotential(List.of(), PotentialRole.UNSPECIFIED,
                new double[] { value });
        constant.setCriterion(criterion);
        return constant;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theCriterionIsTakenFromAnySummandAndNotOnlyFromTheFirst() {
        TablePotential withoutCriterion = aPotentialOver(a, 1, 2);
        TablePotential withCriterion = aPotentialOver(a, 3, 4);
        withCriterion.setCriterion(cost);

        TablePotential sum = DiscretePotentialOperations.sum(List.of(withoutCriterion, withCriterion));

        assertSame(cost, sum.getCriterion(), "the criterion of the second summand must survive");
    }

    @Tag(TestSpeed.FAST)
    @Test public void addingUpConstantsKeepsTheirCriterion() {
        TablePotential sum = DiscretePotentialOperations.sum(
                List.of(aConstantWorth(1.5, cost), aConstantWorth(2.3, cost)));

        assertEquals(3.8, sum.getValues()[0], 1e-12, "the sum of the two constants");
        assertSame(cost, sum.getCriterion(), "a sum of utilities must still be a utility");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aConstantGivesItsCriterionToTheRest() {
        TablePotential withoutCriterion = aPotentialOver(a, 1, 2);

        TablePotential sum = DiscretePotentialOperations.sum(
                List.of(withoutCriterion, aConstantWorth(5.0, cost)));

        assertSame(cost, sum.getCriterion(),
                "the criterion of a constant summand must reach the result");
    }

    @Tag(TestSpeed.FAST)
    @Test public void addingUpIsTakenTheSameWayAsMultiplying() {
        TablePotential withoutCriterion = aPotentialOver(a, 1, 2);
        TablePotential withCriterion = aPotentialOver(a, 3, 4);
        withCriterion.setCriterion(cost);
        List<TablePotential> summands = List.of(withoutCriterion, withCriterion);

        assertSame(DiscretePotentialOperations.multiply(summands).getCriterion(),
                DiscretePotentialOperations.sum(summands).getCriterion(),
                "adding up and multiplying must agree on the criterion");
    }

    @Tag(TestSpeed.FAST)
    @Test public void withNoCriterionAnywhereThereIsNoneInTheResult() {
        TablePotential sum = DiscretePotentialOperations.sum(
                List.of(aPotentialOver(a, 1, 2), aPotentialOver(a, 3, 4)));

        assertSame(null, sum.getCriterion(), "no summand had a criterion");
    }
}
