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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Multiplying and marginalizing must carry along what its inputs carry: the decision criterion,
 * as multiply already did, and the strategy trees. Summing out a chance variable combines the
 * trees of its states into one rooted at that variable, keeping only the possible states — the
 * same rule the probability-and-utility variant already applied.
 *
 * @author Manuel Arias
 */
public class MarginalizingKeepsCriterionAndTreesTest {

    private final Variable decision = new Variable("D", 2);
    private final Variable chance = new Variable("V", 2);
    private final Variable kept = new Variable("W", 2);

    /**
     * A tree with a single leaf branch: take this state of the decision.
     */
    private StrategyTree takeState(int state) {
        return new StrategyTree(decision, decision.getStates()[state]);
    }

    private TablePotential probability(double... values) {
        return new TablePotential(List.of(chance), PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    private StrategicTablePotential utility(StrategyTree[] trees, double... values) {
        StrategicTablePotential utility = new StrategicTablePotential(List.of(chance), PotentialRole.UNSPECIFIED,
                values);
        utility.setCriterion(new Criterion("cost"));
        utility.strategyTrees = trees;
        return utility;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theCriterionOfTheInputsReachesTheResult() {
        StrategicTablePotential util = utility(new StrategyTree[] { takeState(0), takeState(1) }, 10, 20);

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(probability(0.6, 0.4), util), chance);

        assertNotNull(result.getCriterion(), "the criterion was lost");
        assertEquals("cost", result.getCriterion().getCriterionName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void summingOutAVariableRootsTheCombinedTreeAtThatVariable() {
        StrategicTablePotential util = utility(new StrategyTree[] { takeState(0), takeState(1) }, 10, 20);

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(probability(0.6, 0.4), util), chance);

        assertArrayEquals(new double[] { 0.6 * 10 + 0.4 * 20 }, result.getValues(), 1E-12, "the number itself");
        StrategicTablePotential strategic = assertInstanceOf(StrategicTablePotential.class, result,
                "the result lost its trees");
        StrategyTree tree = strategic.strategyTrees[0];
        assertNotNull(tree);
        assertSame(chance, tree.getRootVariable(), "the tree must be contingent on the summed-out variable");
        assertEquals(2, tree.getBranches().size(), "one branch per possible state");
    }

    @Tag(TestSpeed.FAST)
    @Test public void anImpossibleStateLeavesNoBranch() {
        StrategyTree whenPossible = takeState(0);
        StrategicTablePotential util = utility(new StrategyTree[] { whenPossible, takeState(1) }, 10, 20);

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(probability(1.0, 0.0), util), chance);

        StrategicTablePotential strategic = (StrategicTablePotential) result;
        assertSame(whenPossible, strategic.strategyTrees[0],
                "with a single possible state, its tree is the whole answer");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theInputsKeepTheirOwnTreesIntact() {
        StrategyTree first = takeState(0);
        StrategyTree second = takeState(1);
        StrategicTablePotential util = utility(new StrategyTree[] { first, second }, 10, 20);

        DiscretePotentialOperations.multiplyAndMarginalize(List.of(probability(0.6, 0.4), util), chance);

        assertNull(first.getBranches().getFirst().getPotential(), "the tree of the first state grew");
        assertNull(second.getBranches().getFirst().getPotential(), "the tree of the second state grew");
    }

    @Tag(TestSpeed.FAST)
    @Test public void severalVariablesAreSummedOutOneAtATime() {
        Variable other = new Variable("V2", 2);
        StrategicTablePotential util = new StrategicTablePotential(List.of(chance, other),
                PotentialRole.UNSPECIFIED, new double[] { 1, 2, 3, 4 });
        util.setCriterion(new Criterion("cost"));
        util.strategyTrees = new StrategyTree[] { takeState(0), takeState(1), takeState(0), takeState(1) };
        TablePotential probabilityOfOther = new TablePotential(List.of(other),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 });

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(probability(0.5, 0.5), probabilityOfOther, util),
                List.of(), List.of(chance, other));

        double expected = 0.5 * 0.3 * 1 + 0.5 * 0.3 * 2 + 0.5 * 0.7 * 3 + 0.5 * 0.7 * 4;
        assertArrayEquals(new double[] { expected }, result.getValues(), 1E-12);
        StrategicTablePotential strategic = assertInstanceOf(StrategicTablePotential.class, result);
        assertNotNull(strategic.strategyTrees[0]);
        assertEquals("cost", result.getCriterion().getCriterionName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void plainMarginalizationOfACarrierKeepsItsTrees() {
        StrategicTablePotential util = new StrategicTablePotential(List.of(chance, kept),
                PotentialRole.UNSPECIFIED, new double[] { 1, 2, 3, 4 });
        util.strategyTrees = new StrategyTree[] { takeState(0), takeState(1), takeState(0), takeState(1) };

        TablePotential result = DiscretePotentialOperations.marginalize(util, chance);

        assertEquals(List.of(kept), result.getVariables());
        StrategicTablePotential strategic = assertInstanceOf(StrategicTablePotential.class, result,
                "marginalizing dropped the trees");
        assertNotNull(strategic.strategyTrees[0]);
        assertNotNull(strategic.strategyTrees[1]);
    }
}
