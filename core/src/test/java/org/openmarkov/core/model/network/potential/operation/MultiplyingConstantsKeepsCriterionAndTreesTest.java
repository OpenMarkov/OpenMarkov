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
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Multiplying potentials that are all constants must keep the criterion and the strategy trees,
 * as the general case does.
 *
 * @author Manuel Arias
 */
public class MultiplyingConstantsKeepsCriterionAndTreesTest {

    private static TablePotential unitProbability() {
        return new TablePotential(new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 1.0 });
    }

    private static TablePotential constantUtility(double value) {
        TablePotential utility = new TablePotential(new ArrayList<>(), PotentialRole.UNSPECIFIED, new double[] { value });
        utility.setCriterion(new Criterion("cost"));
        return utility;
    }

    @Tag(TestSpeed.FAST)
    @Test public void aConstantUtilityTimesTheUnitProbabilityKeepsItsCriterion() {
        TablePotential result = DiscretePotentialOperations.multiply(List.of(constantUtility(10), unitProbability()));

        assertArrayEquals(new double[] { 10 }, result.getValues(), 1E-12);
        assertNotNull(result.getCriterion(), "the criterion was lost");
        assertEquals("cost", result.getCriterion().getCriterionName());
        assertTrue(result.isAdditive());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aUtilityOverASingleStateVariableKeepsItsCriterion() {
        Variable single = new Variable("E", 1);
        TablePotential utility = new TablePotential(List.of(single), PotentialRole.UNSPECIFIED, new double[] { 10 });
        utility.setCriterion(new Criterion("cost"));

        TablePotential result = DiscretePotentialOperations.multiply(List.of(unitProbability(), utility));

        assertEquals("cost", result.getCriterion().getCriterionName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aConstantCarrierKeepsItsTree() {
        Variable decision = new Variable("D", 2);
        StrategyTree tree = new StrategyTree(decision, decision.getStates()[1]);
        StrategicTablePotential carrier = new StrategicTablePotential(new ArrayList<>(), PotentialRole.UNSPECIFIED,
                new double[] { 5 });
        carrier.setCriterion(new Criterion("cost"));
        carrier.strategyTrees = new StrategyTree[] { tree };

        TablePotential result = DiscretePotentialOperations.multiply(List.of(unitProbability(), carrier));

        StrategicTablePotential strategic = assertInstanceOf(StrategicTablePotential.class, result,
                "the result lost its trees");
        assertSame(tree, strategic.strategyTrees[0]);
        assertArrayEquals(new double[] { 5 }, result.getValues(), 1E-12);
    }

    @Tag(TestSpeed.FAST)
    @Test public void constantProbabilitiesStayWithoutCriterion() {
        TablePotential half = new TablePotential(new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5 });

        TablePotential result = DiscretePotentialOperations.multiply(List.of(half, unitProbability()));

        assertNull(result.getCriterion());
        assertInstanceOf(TablePotential.class, result);
        assertFalse(result instanceof StrategicTablePotential);
        assertArrayEquals(new double[] { 0.5 }, result.getValues(), 1E-12);
    }
}
