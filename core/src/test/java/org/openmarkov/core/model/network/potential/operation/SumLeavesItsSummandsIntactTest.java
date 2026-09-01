/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Summing potentials that carry strategy trees must not write into them. The concatenation that
 * the sum uses hangs one tree from the leaves of the other, so without a copy the trees of the
 * summands grew with every sum.
 *
 * @author Manuel Arias
 */
public class SumLeavesItsSummandsIntactTest {

    private final Variable decision = new Variable("D", 2);
    private final Variable chance = new Variable("X", 2);

    /**
     * A tree with a single leaf branch: take this state of the decision.
     */
    private StrategyTree takeState(int state) {
        return new StrategyTree(decision, decision.getStates()[state]);
    }

    private StrategicTablePotential utilityWithTree(double[] values, StrategyTree tree) {
        StrategicTablePotential potential = new StrategicTablePotential(List.of(chance), PotentialRole.UNSPECIFIED,
                values);
        potential.strategyTrees = new StrategyTree[] { tree, tree };
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void summingTwoPotentialsWithTreesLeavesBothTreesAsTheyWere() {
        StrategyTree first = takeState(0);
        StrategyTree second = takeState(1);
        StrategicTablePotential one = utilityWithTree(new double[] { 1, 2 }, first);
        StrategicTablePotential other = utilityWithTree(new double[] { 10, 20 }, second);

        TablePotential result = DiscretePotentialOperations.sum(List.of(one, other));

        assertArrayEquals(new double[] { 11, 22 }, result.getValues(), 0, "the sum itself");
        // The leaf branches of both summands must still be leaves: nothing hung from them.
        assertNull(first.getBranches().getFirst().getPotential(), "the first summand grew");
        assertNull(second.getBranches().getFirst().getPotential(), "the second summand grew");
        assertEquals(1, first.getBranches().size());
        assertEquals(1, second.getBranches().size());
    }

    @Tag(TestSpeed.FAST)
    @Test public void theResultStillCarriesACombinedTree() {
        StrategicTablePotential one = utilityWithTree(new double[] { 1, 2 }, takeState(0));
        StrategicTablePotential other = utilityWithTree(new double[] { 10, 20 }, takeState(1));

        TablePotential result = DiscretePotentialOperations.sum(List.of(one, other));

        StrategicTablePotential strategic = (StrategicTablePotential) result;
        assertNotNull(strategic.strategyTrees[0], "the result lost its tree");
        // The combined tree hangs the second decision from the leaf of the first.
        assertNotNull(strategic.strategyTrees[0].getBranches().getFirst().getPotential(),
                "the result tree did not combine the two summands");
    }

    @Tag(TestSpeed.FAST)
    @Test public void summingConstantPotentialsWithTreesLeavesThemIntactToo() {
        StrategyTree first = takeState(0);
        StrategyTree second = takeState(1);
        StrategicTablePotential one = constantWithTree(3, first);
        StrategicTablePotential other = constantWithTree(4, second);
        StrategicTablePotential overChance = utilityWithTree(new double[] { 1, 2 }, takeState(0));

        TablePotential result = DiscretePotentialOperations.sum(List.of(one, other, overChance));

        assertArrayEquals(new double[] { 8, 9 }, result.getValues(), 0, "the sum itself");
        assertNull(first.getBranches().getFirst().getPotential(), "the first constant summand grew");
        assertNull(second.getBranches().getFirst().getPotential(), "the second constant summand grew");
    }

    private StrategicTablePotential constantWithTree(double value, StrategyTree tree) {
        StrategicTablePotential potential = new StrategicTablePotential(List.of(), PotentialRole.UNSPECIFIED,
                new double[] { value });
        potential.strategyTrees = new StrategyTree[] { tree };
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void copyingAStrategyTreeKeepsItsClassDownTheBranches() {
        // concatenate casts every branch potential to StrategyTree, so a copy that degrades the
        // children to plain tree potentials would break the next concatenation.
        StrategyTree inner = takeState(1);
        StrategyTree outer = new StrategyTree(decision, List.of(decision.getStates()[0]), inner);

        StrategyTree copied = outer.clone();

        assertEquals(StrategyTree.class, copied.getBranches().getFirst().getPotential().getClass());
    }
}
